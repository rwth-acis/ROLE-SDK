/*
 * #%L
 * Conserve Concept Server
 * %%
 * Copyright (C) 2010 - 2011 KMR
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.kth.csc.kmr.conserve.util;

import java.io.IOException;
import java.io.Writer;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;

import se.kth.csc.kmr.conserve.util.TemplateManager.LayoutMethod;

public class TemplateLayout {

	private final TemplateManager manager;

	private final String layoutName;

	private final ST template;

	private Set<String> bundles = new HashSet<String>();

	private Map<String, Object> paramValues = new TreeMap<String, Object>();

	private Map<String, Object> paramFilters = new TreeMap<String, Object>();

	private Map<String, Object> literalBindings = new TreeMap<String, Object>();

	private Map<String, Object> includeBindings = new TreeMap<String, Object>();

	private Set<ST> includes = new HashSet<ST>();

	TemplateLayout(TemplateManager templateManager, String layoutName) {
		this.manager = templateManager;
		this.layoutName = layoutName;
		this.template = manager.getTemplate("layouts." + layoutName, 0);
	}

	public TemplateLayout param(String name, String value) {
		if (value != null) {
			paramValues.put(name, value);
		}
		return this;
	}

	public TemplateLayout param(String name, Object value, ParamFilter filter) {
		paramValues.put(name, value);
		paramFilters.put(name, filter);
		return this;
	}

	public TemplateLayout params(String name, Map<String, String> valueMap) {
		paramValues.put(name, valueMap);
		return this;
	}

	public TemplateLayout params(String name,
			Map<String, ? extends Object> valueMap,
			Map<String, ParamFilter> filterMap) {
		paramValues.put(name, valueMap);
		paramFilters.put(name, filterMap);
		return this;
	}

	public TemplateLayout literal(String name, String literalName) {
		if (literalName != null) {
			String bundleName = literalName.substring(0,
					literalName.indexOf('.'));
			bundles.add(bundleName);
			ST t = new ST("<bundle." + literalName + ">");
			t.impl.nativeGroup = template.impl.nativeGroup;
			literalBindings.put(name, t);
		}
		return this;
	}

	public TemplateLayout literals(String name,
			Map<String, String> literalNameMap) {
		Map<String, ST> m = new TreeMap<String, ST>();
		for (String key : literalNameMap.keySet()) {
			String literalName = literalNameMap.get(key);
			String bundleName = literalName.substring(0,
					literalName.indexOf('.'));
			bundles.add(bundleName);
			ST t = new ST("<bundle." + literalName + ">");
			t.impl.nativeGroup = template.impl.nativeGroup;
			m.put(key, t);
		}
		literalBindings.put(name, m);
		return this;
	}

	public TemplateLayout include(String name, String templateName) {
		ST t = manager.getTemplate(templateName, 1);
		includes.add(t);
		includeBindings.put(name, t);
		return this;
	}

	public TemplateLayout include(String name,
			Map<String, String> templateNameMap) {
		Map<String, ST> m = new TreeMap<String, ST>();
		for (String key : templateNameMap.keySet()) {
			ST t = manager.getTemplate(templateNameMap.get(key), 1);
			includes.add(t);
			m.put(key, t);
		}
		includeBindings.put(name, m);
		return this;
	}

	public TemplateLayout bundle(String bundleName) {
		bundles.add(bundleName);
		return this;
	}

	public TemplateLayout base(String base) {
		for (LayoutMethod method : manager.schemas.get(layoutName)) {
			method.invoke(this, base);
		}
		return this;
	}

	protected ST bind() {
		return bind(Locale.getDefault());
	}

	protected ST bind(Locale locale) {
		ST t = new ST(template);
		includes.add(t);

		Map<String, Map<String, Object>> bundleBindings = new TreeMap<String, Map<String, Object>>();
		for (String bundleName : bundles) {
			bundleBindings.put(bundleName,
					manager.loadLiterals(bundleName, locale));
		}

		Map<String, Object> paramBindings = new TreeMap<String, Object>();
		for (String paramName : paramValues.keySet()) {
			Object paramValue = paramValues.get(paramName);
			if (paramValue instanceof String) {
				paramBindings.put(paramName, paramValue);
			} else if (paramValue instanceof Map) {
				Map<String, Object> m = new TreeMap<String, Object>();
				Map<?, ?> filterMap = (Map<?, ?>) paramFilters.get(paramName);
				if (filterMap == null) {
					for (Entry<?, ?> entry : ((Map<?, ?>) paramValue)
							.entrySet()) {
						m.put((String) entry.getKey(),
								new TemplateManager.ExtValue(entry.getValue()
										.toString()));
					}
				} else {
					for (Object key : ((Map<?, ?>) paramValue).keySet()) {
						if (filterMap.containsKey(key)) {
							m.put((String) key,
									new TemplateManager.ExtValue(
											((ParamFilter) filterMap.get(key))
													.filter(((Map<?, ?>) paramValue)
															.get(key), locale)
													.toString()));
						}
					}
				}
				paramBindings.put(paramName, m);
			} else if (paramValue != null) {
				paramBindings
						.put(paramName, new TemplateManager.ExtValue(
								((ParamFilter) paramFilters.get(paramName))
										.filter(paramValue, locale).toString()));
			}
		}

		for (String key : manager.maps.keySet()) {
			if (!manager.mapsTreeified.containsKey(key)) {
				synchronized (manager) {
					if (!manager.mapsTreeified.containsKey(key)) {
						manager.mapsTreeified.put(key,
								TemplateManager.treeify(manager.maps.get(key)));
					}
				}
			}
			Map<String, Object> map = manager.mapsTreeified.get(key);
			for (ST i : includes) {
				i.add(key, map);
			}
		}

		Map<String, Object> treeifiedParamBindings = TemplateManager
				.treeify(paramBindings);
		Map<String, Object> treeifiedLiteralBindings = TemplateManager
				.treeify(literalBindings);
		Map<String, Object> treeifiedIncludeBindings = TemplateManager
				.treeify(includeBindings);
		for (ST i : includes) {
			i.add("bundle", bundleBindings);
			i.add("param", treeifiedParamBindings);
			i.add("literal", treeifiedLiteralBindings);
			i.add("include", treeifiedIncludeBindings);
		}
		return t;
	}

	public String render() {
		return bind().render();
	}

	public void render(Writer writer) throws IOException {
		bind().write(new AutoIndentWriter(writer));
	}

	public String render(Locale locale) {
		return bind(locale).render(locale);
	}

	public void render(Writer writer, Locale locale) throws IOException {
		bind().write(new AutoIndentWriter(writer), locale);
	}

	public interface ParamFilter {

		public Object filter(Object value, Locale locale);

	}

}
