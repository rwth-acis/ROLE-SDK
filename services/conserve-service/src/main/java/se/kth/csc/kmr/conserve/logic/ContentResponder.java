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
package se.kth.csc.kmr.conserve.logic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.UriBuilder;

import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;

public class ContentResponder extends RDFResponder {

	@Override
	public void addTriples(Graph graph, Request request, Concept context,
			UriBuilder uriBuilder) {
		final ValueFactory valueFactory = graph.getValueFactory();
		final URI contextUri = valueFactory.createURI(uriBuilder.build()
				.toString());
		final List<Content> contents = store.getContents(context.getUuid());
		final URI dctermsFormat = valueFactory.createURI(
				"http://purl.org/dc/terms/", "format");
		final URI rdfType = valueFactory.createURI(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
		final URI foafDocument = valueFactory.createURI(
				"http://xmlns.com/foaf/0.1/", "Document");
		final URI foafTemporal = valueFactory.createURI(
				"http://purl.org/dc/terms/", "temporal");

		for (Content content : contents) {
			Concept predicate = store.getConcept(content.getPredicate());
			if (predicate == null) {
				continue;
			}
			URI predicateUri = valueFactory.createURI(predicate.getId());
			Entry<String, String> namespace = ((RequestImpl) request)
					.getNamespace(predicate.getId());
			if (namespace == null) {
				continue;
			}
			URI objectUri = valueFactory.createURI(uriBuilder
					.clone()
					.path(namespace.getKey()
							+ ':'
							+ predicate.getId().substring(
									namespace.getValue().length())).build()
					.toString());
			graph.add(contextUri, predicateUri, objectUri);
			graph.add(objectUri, dctermsFormat,
					valueFactory.createLiteral(content.getType()));
			graph.add(objectUri, rdfType, foafDocument);

			StringBuffer temporal = new StringBuffer();
			temporal.append("start=");
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ssZ");
			temporal.append(dateFormat.format(new Date(context.getTimestamp())));
			// if (context.getIndex() == null) {
			// temporal.append("; end=");
			// temporal.append(dateFormat.format(new
			// Date(context.getExpiry())));
			// }
			temporal.append("; scheme=W3C-DTF;");
			graph.add(objectUri, foafTemporal,
					valueFactory.createLiteral(temporal.toString()));
		}
	}

}
