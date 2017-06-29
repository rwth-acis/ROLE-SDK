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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/d")
@Singleton
public class StaticRS {

	private static Logger log = LoggerFactory.getLogger(StaticRS.class);

	@Inject
	Map<String, Static> files;

	@Path("{key}")
	@GET
	public Response getFile(@PathParam("key") String key) {
		Static stat = files.get(key);
		if (stat == null) {
			log.warn("Static resource not found: " + key);
			return Response.status(Status.NOT_FOUND).build();
		}

		// TODO: Only in debug mode
		// ClassLoader loader = Thread.currentThread().getContextClassLoader();
		// InputStream is = loader.getResourceAsStream(stat.name);
		// if (is == null) {
		// log.error("Resource " + key + " not found: " + stat.name);
		// } else {
		// ByteBuffer buffer;
		// try {
		// ByteArrayOutputStream os = new ByteArrayOutputStream();
		// for (;;) {
		// int r = is.read();
		// if (r == -1) {
		// break;
		// }
		// os.write(r);
		// }
		// buffer = ByteBuffer.wrap(os.toByteArray());
		// stat.buffer = buffer;
		// log.info("Reread resource " + key + ": " + stat.name);
		// } catch (IOException e) {
		// log.error("Error reading resource " + key + ": " + stat.name, e);
		// }
		// }

		log.info("Serving static resource: " + key);
		return Response.ok().type(stat.type).entity(stat.buffer.array())
				.header("Cache-Control", "max-age=3600").build();
	}

}
