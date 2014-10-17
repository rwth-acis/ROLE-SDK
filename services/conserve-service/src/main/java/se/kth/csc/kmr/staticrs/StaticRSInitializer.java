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
package se.kth.csc.kmr.staticrs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractInitializer;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;

public class StaticRSInitializer extends AbstractInitializer {

	private static Logger log = LoggerFactory
			.getLogger(StaticRSInitializer.class);

	@Inject
	Map<String, Static> files;

	@Inject
	@Named("staticrs")
	Map<String, String> hrefs;

	@Override
	public void initialize(Request request) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		for (Entry<String, Static> entry : files.entrySet()) {
			String key = entry.getKey();
			Static stat = entry.getValue();
			InputStream is = loader.getResourceAsStream(stat.name);
			if (is == null) {
				log.error("Resource " + key + " not found: " + stat.name);
			} else {
				ByteBuffer buffer;
				try {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					for (;;) {
						int r = is.read();
						if (r == -1) {
							break;
						}
						os.write(r);
					}
					buffer = ByteBuffer.wrap(os.toByteArray());
				} catch (IOException e) {
					log.error("Error reading resource " + key + ": "
							+ stat.name, e);
					continue;
				}
				stat.buffer = buffer;
				UriBuilder uriBuilder = ((RequestImpl) request).getUriInfo()
						.getBaseUriBuilder();
				uriBuilder.path("d");
				uriBuilder.path(key);
				String href = uriBuilder.build().toString(); // uriBuilder.build().getRawPath();
				href.replace("137.226.58.33", "cloud33.dbis.rwth-aachen.de");
				hrefs.put(key, href);
				log.info("Read resource " + key + ": " + stat.name
						+ " Bound to: " + href);
			}
		}
	}
}
