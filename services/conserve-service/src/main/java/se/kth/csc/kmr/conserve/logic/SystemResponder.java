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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

import com.google.inject.Injector;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Responder;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.BaseRequest;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.util.Base64UUID;

public class SystemResponder extends RDFResponder {

	@Inject
	private MetadataResponder metadataResponder;

	@Inject
	private Injector injector;

	@Override
	public void addTriples(Graph graph, Request request, Concept context,
			UriBuilder uriBuilder) {
		metadataResponder.topicOverride = ConserveTerms.metadata;

		final ValueFactory valueFactory = graph.getValueFactory();
		final String contextRelativeUri = context.getUuid().equals(
				request.getRoot().getUuid()) ? "" : (context.getId() == null
				|| context.getId().contains(":") ? Base64UUID.encode(context
				.getUuid()) : context.getId());
		final URI contextUri = valueFactory.createURI(uriBuilder.build()
				.toString());
		final List<Control> controls = store.getControls(context.getUuid());

		final URI sameAs = valueFactory.createURI(
				"http://www.w3.org/2002/07/owl#", "sameAs");
		final URI rdfType = valueFactory.createURI(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
		final URI seeAlso = valueFactory.createURI(
				"http://www.w3.org/2000/01/rdf-schema#", "seeAlso");

		if (context.getId() == null) {
			graph.add(contextUri, sameAs,
					valueFactory.createURI("urn:uuid:" + context.getUuid()));
		} else if (context.getId().contains(":")) {
			graph.add(contextUri, sameAs,
					valueFactory.createURI(context.getId()));
		}

		List<Control> impliedTypes = store.getControls(context.getPredicate(),
				ConserveTerms.range, null);
		for (Control impliedType : impliedTypes) {
			graph.add(contextUri, rdfType,
					valueFactory.createURI(impliedType.getUri()));
		}

		for (Control control : controls) {
			UUID predicateUuid = control.getPredicate();
			URI predicateUri = null;
			if (ConserveTerms.type.equals(predicateUuid)) {
				predicateUri = rdfType;
			} else if (ConserveTerms.reference.equals(predicateUuid)) {
				predicateUri = sameAs;
			} else {
				Concept controlPredicate = store.getConcept(
						store.getRootUuid(), control.getPredicate());
				if (controlPredicate != null) {
					predicateUri = valueFactory.createURI(controlPredicate
							.getId());
				}
			}
			if (predicateUri != null) {
				try {
					URI controlObjectUri = valueFactory.createURI(store()
							.control(control).uri().toString());
					graph.add(contextUri, predicateUri, controlObjectUri);
				} catch (IllegalArgumentException e) {
					// TODO: Find out why the URI isn't always absolute here
				}
			}
		}

		List<String> seeAlsoUris = Arrays.asList(
				"http://purl.org/openapp/representation",
				"http://purl.org/openapp/index"
		/*
		 * "http://purl.org/openapp/concept", "http://purl.org/openapp/context",
		 * "http://purl.org/openapp/content", "http://purl.org/openapp/control"
		 */);
		for (String seeAlsoUri : seeAlsoUris) {
			Entry<String, String> seeAlsoNamespace = ((RequestImpl) request)
					.getNamespace(seeAlsoUri);
			if (seeAlsoNamespace == null) {
				continue;
			}
			URI objectUri = valueFactory.createURI(contextUri
					+ (contextUri.toString().endsWith("/") ? "" : "/")
					+ seeAlsoNamespace.getKey()
					+ ':'
					+ seeAlsoUri
							.substring(seeAlsoNamespace.getValue().length()));
			graph.add(contextUri, seeAlso, objectUri);
		}

		final List<Concept> concepts = store.getConcepts(context.getUuid());
		for (Concept concept : concepts) {
			String id = concept.getId();
			if (id == null || id.contains(":") || id.contains("/")) {
				id = Base64UUID.encode(concept.getUuid());
			}
			UriBuilder conceptUriBuilder = uriBuilder.clone().path(id);
			URI conceptUri = valueFactory.createURI(conceptUriBuilder.build()
					.toString());

			if (concept.getId() == null) {
				graph.add(conceptUri, sameAs,
						valueFactory.createURI("urn:uuid:" + concept.getUuid()));
			} else if (concept.getId().contains(":")) {
				graph.add(conceptUri, sameAs,
						valueFactory.createURI(concept.getId()));
			}

			Concept predicate = store.getConcept(store.getRootUuid(),
					concept.getPredicate());
			URI predicateUri;
			if (predicate != null) {
				predicateUri = valueFactory.createURI(predicate.getId());
			} else {
				predicateUri = valueFactory.createURI("urn:uuid:"
						+ concept.getPredicate());
			}
			graph.add(contextUri, predicateUri, conceptUri);

			List<Control> conceptImpliedTypes = store.getControls(
					concept.getPredicate(), ConserveTerms.range, null);
			for (Control impliedType : conceptImpliedTypes) {
				graph.add(conceptUri, rdfType,
						valueFactory.createURI(impliedType.getUri()));
			}

			List<Control> conceptControls = store
					.getControls(concept.getUuid());
			for (Control control : conceptControls) {
				UUID controlPredicateUuid = control.getPredicate();
				URI controlPredicateUri = null;
				if (ConserveTerms.type.equals(controlPredicateUuid)) {
					controlPredicateUri = rdfType;
				} else if (ConserveTerms.reference.equals(controlPredicateUuid)) {
					controlPredicateUri = sameAs;
				} else {
					Concept controlPredicate = store.getConcept(
							store.getRootUuid(), control.getPredicate());
					if (controlPredicate != null) {
						controlPredicateUri = valueFactory
								.createURI(controlPredicate.getId());
					}
				}
				if (controlPredicateUri != null) {
					URI controlObjectUri;
					try {
						controlObjectUri = valueFactory.createURI(store()
								.control(control).uri().toString());
					} catch (IllegalArgumentException e) {
						// TODO: Find out why the URI isn't always absolute
						continue;
					}
					graph.add(conceptUri, controlPredicateUri, controlObjectUri);
					if (ConserveTerms.reference.equals(controlPredicateUuid)) {
						Concept controlObject = store.getConcept(control
								.getObject());
						if (controlObject != null) {
							BaseRequest controlRequest = injector
									.getInstance(BaseRequest.class);
							controlRequest.resolve(controlObjectUri.toString());
							Responder controlResponder = controlRequest
									.getResponder(ConserveTerms.metadata);
							if (controlResponder instanceof RDFResponder) {
								((RDFResponder) controlResponder).addTriples(
										graph, controlRequest, controlObject,
										uriBuilder);
							}
						}
					}
				}
			}

			BaseRequest conceptRequest = injector
					.getInstance(BaseRequest.class);
			conceptRequest.resolve(conceptUri.toString());
			Responder conceptResponder = conceptRequest
					.getResponder(ConserveTerms.metadata);
			if (conceptResponder instanceof RDFResponder) {
				((RDFResponder) conceptResponder).addTriples(graph,
						conceptRequest, concept, uriBuilder);
			}
		}

		Responder contextMetaResponder = request
				.getResponder(ConserveTerms.metadata);
		if (contextMetaResponder instanceof RDFResponder) {
			if (contextMetaResponder instanceof MetadataResponder) {
				((MetadataResponder) contextMetaResponder).topicOverride = ConserveTerms.metadata;
			}
			((RDFResponder) contextMetaResponder).addTriples(graph, request,
					context, uriBuilder);
		}
	}

}
