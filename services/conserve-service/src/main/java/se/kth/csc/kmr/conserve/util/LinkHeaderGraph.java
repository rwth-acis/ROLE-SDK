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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;

public class LinkHeaderGraph {

	public static final String RELATION_NS = "http://www.iana.org/assignments/relation/";

	private final Graph graph;

	private final ValueFactory valueFactory;

	private final Resource baseSubject;

	private final URI defaultPredicate;

	public LinkHeaderGraph(String baseUri) {
		graph = new GraphImpl();
		valueFactory = graph.getValueFactory();
		baseSubject = valueFactory.createURI(baseUri);
		defaultPredicate = valueFactory
				.createURI("http://purl.org/dc/terms/relation");
	}

	public LinkHeaderGraph(String baseUri, String header) {
		this(baseUri);
		parseLinkHeader(header);
	}

	public Graph getGraph() {
		return graph;
	}

	public void parseLinkHeader(String header) {
		Pattern linkPattern = Pattern
				.compile("<[^>]*>\\s*(\\s*;\\s*[^\\(\\)<>@,;:\"\\/\\[\\]\\?={} \t]+=(([^\\(\\)<>@,;:\"\\/\\[\\]\\?={} \t]+)|(\"[^\"]*\")))*(,|$)");
		Pattern paramPattern = Pattern
				.compile("[^\\(\\)<>@,;:\"\\/\\[\\]\\?={} \t]+=(([^\\(\\)<>@,;:\"\\/\\[\\]\\?={} \t]+)|(\"[^\"]*\"))");
		Matcher matches = linkPattern.matcher(header);
		while (matches.find()) {
			String[] split = matches.group().split(">");
			String href = split[0].substring(1);
			String ps = split[1];
			Map<String, String> link = new HashMap<String, String>();
			link.put("href", href);
			Matcher s = paramPattern.matcher(ps);
			while (s.find()) {
				String p = s.group();
				String[] paramsplit = p.split("=");
				String name = paramsplit[0];
				link.put(name, unquote(paramsplit[1]));
			}
			Resource subject = baseSubject;
			if (link.containsKey("anchor")) {
				subject = valueFactory.createURI(link.get("anchor"));
			}
			URI predicate = defaultPredicate;
			if (link.containsKey("rel")) {
				predicate = valueFactory.createURI(link.get("rel"));
			}
			URI object = valueFactory.createURI(href);
			graph.add(subject, predicate, object);
		}
	}

	private String unquote(String value) {
		if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

	@Override
	public String toString() {
		List<Map<String, String>> rels = new ArrayList<Map<String, String>>();
		for (Statement statement : graph) {
			Map<String, String> rel = new HashMap<String, String>();
			if (!statement.getSubject().equals(baseSubject)) {
				rel.put("anchor", statement.getSubject().stringValue());
			}
			String predicate = statement.getPredicate().stringValue();
			if (predicate.startsWith(RELATION_NS)) {
				predicate = predicate.substring(RELATION_NS.length());
			}
			rel.put("rel", predicate);
			rel.put("href", statement.getObject().stringValue());
			rels.add(rel);
		}
		return stringifyLinkHeader(rels);
	}

	private String stringifyLinkHeader(List<Map<String, String>> rels) {
		StringBuilder sb = new StringBuilder();
		// Pattern safeAttr = Pattern.compile("[^,;]+");
		for (Map<String, String> rel : rels) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			sb.append('<');
			sb.append(rel.get("href"));
			sb.append('>');
			sb.append("; rel=");
			String relAttr = rel.get("rel");
			// if (safeAttr.matcher(relAttr).matches()) {
			// sb.append(relAttr);
			// } else {
			sb.append('"');
			sb.append(relAttr);
			sb.append('"');
			// }
			for (Entry<String, String> attr : rel.entrySet()) {
				if (!"href".equals(attr.getKey())
						&& !"rel".equals(attr.getKey())) {
					sb.append(';');
					sb.append(' ');
					sb.append(attr.getKey());
					sb.append('=');
					// if (safeAttr.matcher(attr.getValue()).matches()) {
					// sb.append(attr.getValue());
					// } else {
					sb.append('"');
					sb.append(attr.getValue());
					sb.append('"');
					// }
				}
			}
		}
		return sb.toString();
	}

}
