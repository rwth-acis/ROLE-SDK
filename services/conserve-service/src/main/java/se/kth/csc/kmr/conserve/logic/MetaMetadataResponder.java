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
import java.util.UUID;
import java.util.Map.Entry;

import javax.ws.rs.core.UriBuilder;

import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;

public class MetaMetadataResponder extends RDFResponder {

	private final UUID metadataForContent;

	public MetaMetadataResponder() {
		this(ConserveTerms.metadata);
	}

	protected MetaMetadataResponder(UUID metadataForContent) {
		this.metadataForContent = metadataForContent;
	}

	private static final String W3CDTF = "yyyy-MM-dd'T'HH:mm:ssZ";

	@Override
	public void addTriples(Graph graph, Request request, Concept context,
			UriBuilder uriBuilder) {
		final ValueFactory valueFactory = graph.getValueFactory();
		final Content content = store.getContent(context.getUuid(),
				metadataForContent);
		Concept predicate = store.getConcept(metadataForContent);
		if (predicate == null) {
			throw new IllegalArgumentException(
					"Representation type's concept not found");
		}
		Entry<String, String> namespace = ((RequestImpl) request)
				.getNamespace(predicate.getId());
		if (namespace == null) {
			throw new IllegalArgumentException(
					"Representation type's namespace not found");
		}
		final String qualifiedContentId = namespace.getKey() + ':'
				+ predicate.getId().substring(namespace.getValue().length());
		URI contentUri = valueFactory.createURI(uriBuilder.clone()
				.path(qualifiedContentId).build().toString());
		final URI contextUri = valueFactory.createURI(uriBuilder.build()
				.toString());
		final URI predicateUri = valueFactory.createURI(predicate.getId());

		graph.add(contextUri, predicateUri, contentUri);

		if (content != null) {
			graph.add(contentUri, valueFactory.createURI(
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type"),
					valueFactory.createURI("http://xmlns.com/foaf/0.1/",
							"Document"));
			graph.add(contentUri, valueFactory.createURI(
					"http://purl.org/dc/terms/", "format"), valueFactory
					.createLiteral(content.getType()));
			StringBuffer temporal = new StringBuffer();
			temporal.append("start=");
			SimpleDateFormat dateFormat = new SimpleDateFormat(W3CDTF);
			temporal.append(dateFormat.format(new Date(context.getTimestamp())));
			// if (context.getIndex() == null) {
			// temporal.append("; end=");
			// temporal.append(dateFormat.format(new
			// Date(context.getExpiry())));
			// }
			temporal.append("; scheme=W3C-DTF;");
			graph.add(contentUri, valueFactory.createURI(
					"http://purl.org/dc/terms/", "temporal"), valueFactory
					.createLiteral(temporal.toString()));
		}
	}

}
