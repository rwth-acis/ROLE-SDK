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
import se.kth.csc.kmr.conserve.util.LinkHeaderGraph;

public class LinkResponder extends RDFResponder {

	@Override
	public void addTriples(Graph graph, Request request, Concept context,
			UriBuilder uriBuilder) {
		final ValueFactory valueFactory = graph.getValueFactory();
		final String contextRelativeUri = context.getUuid().equals(
				request.getRoot().getUuid()) ? "" : (context.getId() == null
				|| context.getId().contains(":") ? Base64UUID.encode(context
				.getUuid()) : context.getId());
		final URI contextUri = valueFactory.createURI(uriBuilder.build()
				.toString());

		graph.add(
				contextUri,
				valueFactory.createURI(LinkHeaderGraph.RELATION_NS
						+ "edit-media"),
				valueFactory.createURI(contextRelativeUri
						+ "/"
						+ ((RequestImpl) request)
								.getQualifedName("http://purl.org/openapp/representation")));
		graph.add(
				contextUri,
				valueFactory.createURI(LinkHeaderGraph.RELATION_NS + "edit"),
				valueFactory.createURI(contextRelativeUri
						+ "/"
						+ ((RequestImpl) request)
								.getQualifedName("http://purl.org/openapp/metadata")));

		for (Control domain : store.getControls(context.getPredicate(),
				ConserveTerms.range, ConserveTerms.domain, null)) {
			Concept domainConcept = store.getConcept(store.getRootUuid(),
					domain.getContext());
			graph.add(valueFactory.createURI(domainConcept.getId()),
					valueFactory
							.createURI(LinkHeaderGraph.RELATION_NS + "self"),
					valueFactory.createURI(contextRelativeUri
							+ "/"
							+ ((RequestImpl) request)
									.getQualifedName(domainConcept.getId())));
		}
	}

}
