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
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.core.AbstractResponder;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;

public class RepresentationResponder extends AbstractResponder {

	private static final int BUFFER_SIZE = 1024;

	@Override
	public boolean canGet(Request request) {
		return true;
	}

	@Override
	public boolean canPut(Request request) {
		return true;
	}

	@Override
	public boolean canPost(Request request) {
		return false;
	}

	@Override
	public boolean canDelete(Request request) {
		return true;
	}

	@Override
	public Object doGet(Request request) {
		UUID predicate = request.getTopic() != null ? request.getTopic()
				.getUuid() : ConserveTerms.representation;
		Concept context = request.getContext();
		if (context.getUuid().equals(predicate)) {
			return Response.ok().type(MediaType.TEXT_PLAIN_TYPE)
					.entity(this.toString())
					.header("Cache-Control", "no-store").build();
		}
		final Content content = store.getContent(context.getUuid(), predicate);
		if (content == null) {
			return Response.status(Response.Status.NOT_FOUND)
					.header("Cache-Control", "no-store").build();
		} else {
			StreamingOutput streamingOutput = new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException,
						WebApplicationException {
					Blob data = content.getData();
					try {
						InputStream input = data.getBinaryStream();
						byte[] buffer = new byte[BUFFER_SIZE];
						int read = 0, available;
						while (read != -1) {
							output.write(buffer, 0, read);
							available = input.available();
							read = input.read(buffer, 0, Math.min(
									available != 0 ? available : BUFFER_SIZE,
									BUFFER_SIZE));
						}
						output.close();
						input.close();
					} catch (SQLException e) {
						throw new IOException(e);
					}
				}
			};
			return Response.ok().type(MediaType.valueOf(content.getType()))
					.entity(streamingOutput)
					.header("Cache-Control", "no-store").build();
		}
	}

	@Override
	public Object doPut(Request request, byte[] data) {
		UUID predicate = request.getTopic() != null ? request.getTopic()
				.getUuid() : ConserveTerms.representation;
		Concept context = request.getContext();
		if (context.getUuid().equals(predicate)) {
			return Response.status(Response.Status.FORBIDDEN)
					.header("Cache-Control", "no-store").build();
		}
		Content content = store.getContent(context.getUuid(), predicate);
		if (content == null) {
			content = store.createContent(context.getUuid(), predicate);
		}
		content.setType(((RequestImpl) request).getRequestHeaders()
				.getRequestHeader(HttpHeaders.CONTENT_TYPE).get(0));
		Blob contentData = content.getData();
		try {
			contentData.setBytes(1L, data);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		content.setData(contentData);
		store.putContent(content);
		return Response.ok().header("Cache-Control", "no-store").build();
	}

	@Override
	public Object doPost(Request request, byte[] data) {
		return Response.status(Response.Status.FORBIDDEN)
				.header("Cache-Control", "no-store").build();
	}

	@Override
	public Object doDelete(Request request) {
		UUID predicate = request.getTopic() != null ? request.getTopic()
				.getUuid() : ConserveTerms.representation;
		Concept context = request.getContext();
		if (context.getUuid().equals(predicate)) {
			return Response.status(Response.Status.FORBIDDEN)
					.header("Cache-Control", "no-store").build();
		}
		Content content = store.getContent(context.getUuid(), predicate);
		if (content == null) {
			return Response.status(Response.Status.NOT_FOUND)
					.header("Cache-Control", "no-store").build();
		}
		store.deleteContent(content);
		return Response.ok().header("Cache-Control", "no-store").build();
	}

	@Override
	public Resolution resolve(Request request) {
		return null;
	}

}
