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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

public class TemplateManager {

	private static Logger log = LoggerFactory.getLogger(TemplateManager.class);

	private STGroup templateGroup;

	private Map<ResourceBundle, Map<String, Object>> bundles = new HashMap<ResourceBundle, Map<String, Object>>();

	private final String resourceRoot;

	private final String resourceRootPath;

	Map<String, List<LayoutMethod>> schemas = new HashMap<String, List<LayoutMethod>>();

	Map<String, Map<String, String>> maps = new HashMap<String, Map<String, String>>();

	Map<String, Map<String, Object>> mapsTreeified = new HashMap<String, Map<String, Object>>();

	public TemplateManager(String resourceRoot) {
		this.resourceRoot = resourceRoot;
		resourceRootPath = resourceRoot.replace('.', '/');
		templateGroup = new STGroupDir(resourceRootPath, "UTF-8", '$', '$');
		templateGroup.delimiterStartChar = '$';
		templateGroup.delimiterStopChar = '$';
		// templateGroup.setFileCharEncoding("UTF-8");

		// TODO: Only in debug mode
		// STGroup.verbose = true;
		// templateGroup.setRefreshInterval(0);
	}

	public TemplateManager renderer(AttributeRenderer renderer) {
		return renderer(ExtValue.class, renderer);
	}

	static class ExtValue {
		private String value;

		public ExtValue(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExtValue other = (ExtValue) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	public TemplateManager renderer(Class<?> attributeClassType,
			AttributeRenderer renderer) {
		templateGroup.registerRenderer(attributeClassType, renderer);
		return this;
	}

	public TemplateManager map(String name, Map<String, String> map) {
		maps.put(name, map);
		return this;
	}

	static Map<String, Object> treeify(Map<String, ? extends Object> map) {
		Map<String, Object> convertedMap = new HashMap<String, Object>();
		for (Entry<String, ? extends Object> entry : map.entrySet()) {
			String[] segments = entry.getKey().split("\\.");
			Map<String, Object> segmentMap = convertedMap;
			for (int i = 0; i < segments.length; i++) {
				if (i < segments.length - 1) {
					@SuppressWarnings("unchecked")
					Map<String, Object> nestedMap = (Map<String, Object>) segmentMap
							.get(segments[i]);
					if (nestedMap == null) {
						nestedMap = new HashMap<String, Object>();
						segmentMap.put(segments[i], nestedMap);
					}
					segmentMap = nestedMap;
				} else {
					segmentMap.put(segments[i], entry.getValue());
				}
			}
		}
		return convertedMap;
	}

	protected ST getTemplate(String templateName, int leafs) {
		String[] segments = templateName.split("\\.");
		StringBuilder path = new StringBuilder(/*
												 * resourceRootPath.length() +
												 */templateName.length() /* + 2 */);
		// path.append(resourceRootPath);
		// path.append('/');
		for (int i = 0; i < segments.length; i++) {
			if (i > 0) {
				path.append((i < segments.length - leafs) ? '/' : '_');
			}
			path.append(segments[i]);
		}
		ST st = templateGroup.getInstanceOf(path.toString());
		if (st == null) {
			log.error("Template not found: " + path.toString());
		}
		return st;
	}

	protected Map<String, Object> loadLiterals(String bundleName, Locale locale) {
		String baseName = resourceRoot + ".locales." + bundleName;
		ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
		Map<String, Object> map = bundles.get(bundle);
		if (map == null) {
			synchronized (this) {
				if (bundles.containsKey(bundle)) {
					return bundles.get(bundle);
				}
				Enumeration<String> en = bundle.getKeys();
				map = new HashMap<String, Object>();
				while (en.hasMoreElements()) {
					String key = en.nextElement();
					String[] segments = key.split("\\.");
					Map<String, Object> segmentMap = map;
					for (int i = 0; i < segments.length; i++) {
						if (i < segments.length - 1) {
							@SuppressWarnings("unchecked")
							Map<String, Object> nestedMap = (Map<String, Object>) segmentMap
									.get(segments[i]);
							if (nestedMap == null) {
								nestedMap = new HashMap<String, Object>();
								segmentMap.put(segments[i], nestedMap);
							}
							segmentMap = nestedMap;
						} else {
							segmentMap.put(segments[i],
									new ExtValue(bundle.getString(key)));
						}
					}
				}
				bundles.put(bundle, map);
			}
		}
		return map;
	}

	public TemplateLayout layout(String layoutName) {
		return new TemplateLayout(this, layoutName);
	}

	public LayoutSchema newLayout(String layoutName) {
		return new LayoutSchema(layoutName);
	}

	public class LayoutSchema {

		private String layoutName;

		private List<LayoutMethod> methods = new LinkedList<LayoutMethod>();

		LayoutSchema(String layoutName) {
			this.layoutName = layoutName;
		}

		public LayoutSchema literal(final String name) {
			methods.add(new LayoutMethod() {
				@Override
				public void invoke(TemplateLayout layout, String base) {
					layout.literal(name, base + "." + name);
				}
			});
			return this;
		}

		public LayoutSchema include(final String name) {
			methods.add(new LayoutMethod() {
				@Override
				public void invoke(TemplateLayout layout, String base) {
					layout.include(name, base + "." + name);
				}
			});
			return this;
		}

		public TemplateManager build() {
			schemas.put(layoutName, methods);
			return TemplateManager.this;
		}

	}

	interface LayoutMethod {

		public void invoke(TemplateLayout layout, String base);

	}

}
