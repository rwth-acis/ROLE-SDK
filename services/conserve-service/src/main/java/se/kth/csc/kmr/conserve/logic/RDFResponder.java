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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractResponder;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;

public abstract class RDFResponder extends AbstractResponder {

	private static Logger log = LoggerFactory.getLogger(RDFResponder.class);

	private Map<String, String> namespaces = new HashMap<String, String>();

	@Override
	public boolean canGet(Request request) {
		return true;
	}

	@Override
	public boolean canPut(Request request) {
		return false;
	}

	@Override
	public boolean canPost(Request request) {
		return false;
	}

	@Override
	public boolean canDelete(Request request) {
		return false;
	}

	@Override
	public Object doGet(final Request request) {
		RDFWriterRegistry writerRegistry = RDFWriterRegistry.getInstance();
		for (RDFWriterFactory wf : writerRegistry.getAll()) {
			log.info("RDFWriterFactory: "
					+ wf.getRDFFormat().getDefaultMIMEType());
		}
		RDFWriterFactory writerFactory = null;
		String mediaType = null;
		for (MediaType mt : ((RequestImpl) request).getRequestHeaders()
				.getAcceptableMediaTypes()) {
			if (mediaType == null) {
				if (mt.isCompatible(MediaType.TEXT_HTML_TYPE)
						|| mt.isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE)) {
					return new TemplateResponder("/ui/html/graph.html")
							.doGet(request);
				}
			}
			mediaType = mt.toString();
			RDFFormat fileFormat = writerRegistry.getFileFormatForMIMEType(mt
					.toString());
			if (fileFormat != null) {
				mediaType = fileFormat.getDefaultMIMEType();
				writerFactory = writerRegistry.get(fileFormat);
				break;
			}
		}
		if (writerFactory == null) {
			mediaType = "text/rdf+n3";
			writerFactory = writerRegistry.get(writerRegistry
					.getFileFormatForMIMEType(mediaType));
		}
		final RDFWriterFactory chosenWriterFactory = writerFactory;

		final Graph graph = new GraphImpl();
		Concept context = request.getContext();
		Concept requestedRelation = request.getRelation();
		if (requestedRelation != null
				&& ConserveTerms.context.equals(requestedRelation.getUuid())) {
			requestedRelation = null;
		}
		UriBuilder contextUb = store().in(context).uriBuilder();
		addTriples(graph, request, context, contextUb);

		StreamingOutput streamingOutput = new StreamingOutput() {
			@Override
			public void write(OutputStream outputStream) throws IOException,
					WebApplicationException {
				try {
					RDFWriter rdfWriter = chosenWriterFactory
							.getWriter(outputStream);
					rdfWriter.startRDF();
					for (Entry<String, String> namespace : namespaces
							.entrySet()) {
						rdfWriter.handleNamespace(namespace.getKey(),
								namespace.getValue());
					}
					for (Statement statement : graph) {
						rdfWriter.handleStatement(statement);
					}
					rdfWriter.endRDF();
					outputStream.close();
				} catch (OpenRDFException e) {
					log.error("RDF error", e);
				}
			}
		};

		return Response.ok().type(mediaType).entity(streamingOutput)
				.header("Cache-Control", "no-store").build();
	}

	protected void setNamespace(String prefix, String uri) {
		namespaces.put(prefix, uri);
	}

	public abstract void addTriples(Graph graph, Request request,
			Concept context, UriBuilder uriBuilder);

}
