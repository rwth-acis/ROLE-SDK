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
package se.kth.csc.kmr.conserve.security.session;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractListener;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.security.SecurityInfo;
import se.kth.csc.kmr.conserve.util.Base64UUID;

public class SessionListener extends AbstractListener {

	private static Logger log = LoggerFactory.getLogger(SessionListener.class);

	@Inject
	@Named("conserve.session.context")
	private UUID sessionContext;

	@Inject
	private SecurityInfo securityInfo;

	@Override
	public void initialize() {
		store().in(store.getRootUuid()).sub(ConserveTerms.hasPart)
				.acquire(sessionContext);
	}

	@Override
	public Object doRequest(Request request, byte[] data) {
		UUID agent = null;

		if (request instanceof RequestImpl) {
			HttpHeaders httpHeaders = ((RequestImpl) request)
					.getRequestHeaders();
			Map<String, Cookie> cookies = httpHeaders.getCookies();
			if (cookies.containsKey("conserve_session")) {
				Cookie sessionCookie = cookies.get("conserve_session");
				String sessionCookieValue = sessionCookie.getValue();
				Concept session = store.getConcept(sessionContext,
						sessionCookieValue);
				if (session != null) {
					List<Control> sessionReferences = store.getControls(
							session.getUuid(), ConserveTerms.reference);
					if (sessionReferences != null && sessionReferences.size() != 0) {
						agent = sessionReferences.get(0).getObject();
						log.info("Agent found: " + agent.toString());
					}
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
