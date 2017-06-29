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
package se.kth.csc.kmr.conserve.security.oauth;

import java.net.URI;
import java.util.UUID;

import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractListener;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.security.SecurityInfo;
import se.kth.csc.kmr.conserve.util.Base64UUID;
import se.kth.csc.kmr.oauth.OAuth1;
import se.kth.csc.kmr.oauth.OAuthException;
import se.kth.csc.kmr.oauth.OAuth1Token;

public class OAuthListener extends AbstractListener {

	private static final Logger log = LoggerFactory
			.getLogger(OAuthListener.class);

	@Inject
	@Named("conserve.oauth.access.token.context")
	private UUID tokenContextUuid;

	@Inject
	@Named("conserve.oauth.request.token.context")
	private UUID temporaryContextUuid;

	@Inject
	private OAuth1 provider;

	@Inject
	private SecurityInfo securityInfo;

	@Override
	public void initialize() {
		store().in(ConserveTerms.root).sub(ConserveTerms.hasPart)
				.acquire(tokenContextUuid);
		store().in(ConserveTerms.root).sub(ConserveTerms.hasPart)
				.acquire(temporaryContextUuid);

		store().in(store.getRootUuid())
				.sub(ConserveTerms.configuration)
				.acquire(
						URI.create("http://kmr.csc.kth.se/rdf/conserve/config/oauth"));
	}

	@Override
	public Object doRequest(Request request, byte[] data) {
		log.info("OAuthListener invoked");

		UUID agent = null;

		if (request instanceof RequestImpl) {
			OAuth1Token token;
			try {
				token = provider.validate(((RequestImpl) request)
						.getHttpServletRequest());
			} catch (OAuthException e) {
				return Response.status(Status.BAD_REQUEST).entity(e.toString())
						.type(MediaType.TEXT_PLAIN)
						.header("Cache-Control", "no-store").build();
			}
			if (token != null) {
				log.info("OAuth validation successful");
				String agentUri = (String) token.getResourceOwner();
				if (agentUri.startsWith("urn:uuid:")) {
					agent = UUID.fromString(agentUri.substring(9));
				}
			}
		}

		if (agent != null) {
			log.info("Setting the agent for this request to: "
					+ Base64UUID.encode(agent));
			securityInfo.setAgent(agent);
		} else {
			log.info("No valid agent was found for this request");
		}

		return request;
	}

}
