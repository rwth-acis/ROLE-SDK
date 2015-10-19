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
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.util.Base64UUID;

public class ConceptResponder extends RDFResponder {

	private static final String W3CDTF = "yyyy-MM-dd'T'HH:mm:ssZ";

	@Override
	public void addTriples(Graph graph, Request request, Concept context,
			UriBuilder uriBuilder) {
		final ValueFactory valueFactory = graph.getValueFactory();
		final URI contextUri = valueFactory.createURI(uriBuilder.build()
				.toString());
		final List<Control> controls = store.getControls(context.getUuid());

		Concept conceptPredicate = store.getConcept(ConserveTerms.concept);
		Entry<String, String> namespace = ((RequestImpl) request)
				.getNamespace(conceptPredicate.getId());
		final URI contentUri = valueFactory.createURI(uriBuilder
				.clone()
				.path(namespace.getKey()
						+ ':'
						+ conceptPredicate.getId().substring(
								namespace.getValue().length())).build()
				.toString());

		final URI sameAs = valueFactory.createURI(
				"http://www.w3.org/2002/07/owl#", "sameAs");
		final URI permaUri = valueFactory.createURI(((RequestImpl) request)
				.getUriInfo().getBaseUriBuilder()
				.path(Base64UUID.encode(context.getUuid())).build().toString());

		graph.add(contentUri, valueFactory.createURI(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type"),
				valueFactory.createURI(
						"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
						"Statement"));
		String subjectUri = store().in(store.getConcept(context.getContext()))
				.uri().toString();
		if (subjectUri.startsWith("urn:uuid:")) {
			subjectUri = ((RequestImpl) request).getUriInfo()
					.getBaseUriBuilder()
					.path(Base64UUID.encode(context.getContext())).build()
					.toString();
		}
		graph.add(contentUri, valueFactory.createURI(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "subject"),
				valueFactory.createURI(subjectUri));
		graph.add(contentUri, valueFactory.createURI(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "predicate"),
				valueFactory.createURI(store.getConcept(context.getPredicate())
						.getId()));
		graph.add(contentUri, valueFactory.createURI(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "object"),
				contextUri);
		graph.add(contextUri, sameAs, permaUri);
		graph.add(contextUri, sameAs,
				valueFactory.createURI("urn:uuid:" + context.getUuid()));
		graph.add(contextUri, valueFactory.createURI(
				"http://www.w3.org/2000/01/rdf-schema#", "isDefinedBy"),
				contentUri);

		StringBuffer temporal = new StringBuffer();
		temporal.append("start=");
		SimpleDateFormat dateFormat = new SimpleDateFormat(W3CDTF);
		temporal.append(dateFormat.format(new Date(context.getTimestamp())));
		// if (context.getIndex() == null) {
		// temporal.append("; end=");
		// temporal.append(dateFormat.format(new Date(context.getExpiry())));
		// }
		temporal.append("; scheme=W3C-DTF;");
		graph.add(
				contentUri,
				valueFactory.createURI("http://purl.org/dc/terms/", "temporal"),
				valueFactory.createLiteral(temporal.toString()));

		for (Control control : controls) {
			Concept predicate = store.getConcept(control.getPredicate());
			URI predicateUri;
			if (predicate != null) {
				predicateUri = valueFactory.createURI(predicate.getId());
			} else {
				continue;
				// predicateUri = valueFactory.createURI("urn:uuid:"
				// + control.getPredicate());
			}
			try {
				graph.add(contextUri, predicateUri,
						valueFactory.createURI(control.getUri()));
			} catch (IllegalArgumentException e) {
				// TODO: Find out why the contro; URI isn't always absolute here
			}
		}
	}
}
