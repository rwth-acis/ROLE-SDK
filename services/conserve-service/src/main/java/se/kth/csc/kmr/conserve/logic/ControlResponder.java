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

import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;

public class ControlResponder extends RDFResponder {

	@Override
	public void addTriples(Graph graph, Request request, Concept context,
			UriBuilder uriBuilder) {
		final ValueFactory valueFactory = graph.getValueFactory();
		final URI contextUri = valueFactory.createURI(uriBuilder.build()
				.toString());
		final List<Control> controls = store.getControls(context.getUuid());
		for (Control control : controls) {
			Concept predicate = store.getConcept(control.getPredicate());
			URI predicateUri;
			if (predicate != null) {
				predicateUri = valueFactory.createURI(predicate.getId());
			} else {
				continue;
//				predicateUri = valueFactory.createURI("urn:uuid:"
//						+ control.getPredicate());
			}
			graph.add(contextUri, predicateUri,
					valueFactory.createURI(control.getUri()));
		}
	}

}
