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
package se.kth.csc.kmr.conserve.dsl;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.SQLException;

import org.openrdf.model.Graph;

import com.google.inject.Inject;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.util.RDFJSON;

public abstract class ContentDSL {

	@Inject
	private se.kth.csc.kmr.conserve.Contemp store;

	private final Content impl;

	Concept context;

	public ContentDSL() {
		if (!(this instanceof Content)) {
			throw new IllegalStateException();
		}
		impl = (Content) this;
	}

	public String type() {
		return impl.getType();
	}

	public ContentDSL type(String mediaType) {
		impl.setType(mediaType);
		return this;
	}

	public byte[] bytes() {
		Blob data = impl.getData();
		try {
			return data.getBytes(1L, (int) data.length());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Content bytes(byte[] content) {
		Blob data = impl.getData();
		try {
			data.setBytes(1L, content);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		store.putContent(impl);
		return impl;
	}

	public String string() {
		return new String(bytes(), Charset.forName("UTF-8"));
	}

	public Content string(String content) {
		return bytes(content.getBytes(Charset.forName("UTF-8")));
	}

	public Graph graph() {
		URI baseUri = context == null ? ((ContempDSL) store).in(
				impl.getContext()).uri() : ((ContempDSL) store).in(context)
				.uri();
		return RDFJSON.rdfJsonToGraph(string(), baseUri.toString());
	}

	public void graph(Graph graph) {
		URI baseUri = context == null ? ((ContempDSL) store).in(
				impl.getContext()).uri() : ((ContempDSL) store).in(context)
				.uri();
		StringWriter writer = new StringWriter();
		try {
			RDFJSON.graphToRdfJsonJackson(graph, writer, baseUri.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		string(writer.toString());
	}

}