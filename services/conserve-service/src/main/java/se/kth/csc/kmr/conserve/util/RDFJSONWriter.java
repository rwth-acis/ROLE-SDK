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

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * RDFWriter implementation for the proposed RDF/JSON format (see
 * http://n2.talis.com/wiki/RDF_JSON_Specification)
 * <p/>
 * Date: Dec 21, 2010 Time: 2:55:55 PM
 * 
 * @author Joshua Shinavier (http://fortytwo.net), modified by Erik Isaksson
 *         <erikis@kth.se>. Builds on code by Hannes Ebner
 */
public class RDFJSONWriter implements RDFWriter {
	public static final RDFFormat RDFJSON_FORMAT = new RDFFormat("RDF/JSON",
			"application/json", // TODO: has a more specific MIME type been
								// suggested for RDF/JSON?
			Charset.forName("UTF-8"), // See section 3 of the JSON RFC:
										// http://www.ietf.org/rfc/rfc4627.txt
			"json", false, // namespaces are not supported
			true); // contexts are supported

	private final Writer writer;
	private Graph graph;

	public RDFJSONWriter(final OutputStream out) {
		this.writer = new OutputStreamWriter(out);
	}

	public RDFJSONWriter(final Writer writer) {
		this.writer = writer;
	}

	public RDFFormat getRDFFormat() {
		return RDFJSON_FORMAT;
	}

	public void startRDF() throws RDFHandlerException {
		graph = new GraphImpl();
	}

	public void endRDF() throws RDFHandlerException {
		// String s = RDFJSON.graphToRdfJson(graph);
		// System.out.println("written: " + s);
		try {
			// writer.write(s);
			RDFJSON.graphToRdfJsonJackson(graph, writer, "");
			// writer.flush();
		} catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	public void handleNamespace(final String prefix, final String uri)
			throws RDFHandlerException {
		// Namespace prefixes are not used in RDF/JSON.
	}

	public void handleStatement(final Statement statement)
			throws RDFHandlerException {
		// System.out.println("got it: " + statement);
		graph.add(statement);
	}

	public void handleComment(final String comment) throws RDFHandlerException {
		// Comments are ignored.
	}
}
