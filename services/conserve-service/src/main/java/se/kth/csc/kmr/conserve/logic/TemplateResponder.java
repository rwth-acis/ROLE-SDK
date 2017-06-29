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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractResponder;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;

public class TemplateResponder extends ResourceResponder {

	private static final int BUFFER_SIZE = 1024;

	private final String templatePath;

	public TemplateResponder(String templatePath) {
		this.templatePath = templatePath;
	}

	@Override
	public boolean canGet(Request request) {
		return true;
	}

	@Override
	public Object doGet(Request request) {
		final InputStream input = ((RequestImpl) request).getServletContext()
				.getResourceAsStream(templatePath);
		if (input == null) {
			throw new RuntimeException("Template not found: " + templatePath);
		}
		StreamingOutput streamingOutput = new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
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
			}
		};

		return Response.ok().type(MediaType.TEXT_HTML_TYPE)
				.entity(streamingOutput).header("Cache-Control", "no-store")
				.build();
	}

}
