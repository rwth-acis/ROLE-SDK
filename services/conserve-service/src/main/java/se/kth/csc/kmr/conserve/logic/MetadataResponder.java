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

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.openrdf.model.Graph;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.ConserveTerms;

public class MetadataResponder extends RDFResponder {

	private static Logger log = LoggerFactory
			.getLogger(MetadataResponder.class);

	@Inject
	private Injector injector;

	protected UUID topicOverride = null;

	@Override
	public void addTriples(Graph graph, Request request, Concept context,
			UriBuilder uriBuilder) {
		UUID predicate = topicOverride != null ? topicOverride : (request
				.getTopic() != null ? request.getTopic().getUuid()
				: ConserveTerms.metadata);
		Content content = store.getContent(context.getUuid(), predicate);
		if (content == null) {
			return;
		}

		RDFParserRegistry parserRegistry = RDFParserRegistry.getInstance();
		final Repository myRepository = new SailRepository(new MemoryStore());
		try {
			myRepository.initialize();
			RepositoryConnection conn = myRepository.getConnection();
			String contentType = content.getType();
			if (contentType.indexOf(";") >= 0) {
				contentType = contentType
						.substring(0, contentType.indexOf(";"));
			}
			conn.add(content.getDataStream(), store().in(context).uri()
					.toString(),
					parserRegistry.getFileFormatForMIMEType(contentType));
			conn.getStatements(null, null, null, false).addTo(graph);
		} catch (Exception e) {
			log.error("Error parsing metadata", e);
		}

	}

	@Override
	public Object doPut(Request request, byte[] data) {
		return injector.getInstance(RepresentationResponder.class).doPut(
				request, data);
	}

}
