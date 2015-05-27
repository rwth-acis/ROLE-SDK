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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.dsl.ContempDSL;
import se.kth.csc.kmr.oauth.OAuthException;
import se.kth.csc.kmr.oauth.OAuthPrincipal;

public class OAuthSessionPrincipal extends OAuthPrincipal {

	private static Logger log = LoggerFactory
			.getLogger(OAuthSessionPrincipal.class);

	@Inject
	private Injector injector;

	@Inject
	@Named("conserve.session.context")
	private UUID sessionContext;

	private ContempDSL store() {
		return (ContempDSL) injector.getInstance(Contemp.class);
	}

	private UUID getAgent(HttpServletRequest request) {
		log.info("Checking for an agent");
		UUID agent = null;
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		Contemp store = injector.getInstance(Contemp.class);
		for (Cookie cookie : cookies) {
			log.info("Cookie: " + cookie.getName());
			if ("conserve_session".equals(cookie.getName())) {
				String sessionCookieValue = cookie.getValue();
				Concept session = store.getConcept(sessionContext,
						sessionCookieValue);
				if (session != null) {
					List<Control> sessionReferences = store.getControls(
							session.getUuid(), ConserveTerms.reference);
					if (sessionReferences != null
							&& sessionReferences.size() != 0) {
						agent = sessionReferences.get(0).getObject();
						log.info("Agent found: " + agent.toString());
					}
				}
				break;
			}
		}
		return agent;
	}

	@Override
	public URI ensureAuthenticated(HttpServletRequest request) {
		if (getAgent(request) == null) {
			try {
				String openid = request.getParameter("openid");
				if (openid != null) {
					return URI.create(request.getContextPath()
							+ "/o/openid/request?return="
							+ URLEncoder.encode(request.getRequestURI() + "?"
									+ request.getQueryString(), "UTF-8")
							+ "&openid=" + URLEncoder.encode(openid, "UTF-8"));
				} else {
					return URI.create("/%3Aauthentication?return="
							+ URLEncoder.encode(request.getRequestURI() + "?"
									+ request.getQueryString(), "UTF-8"));
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public String getResourceOwner(HttpServletRequest request)
			throws OAuthException {
		UUID agent = getAgent(request);
		if (agent == null) {
			throw new OAuthException("Not logged in");
		}
		return "urn:uuid:" + agent.toString();
	}

	@Override
	public String getResourceOwnerDisplayName(HttpServletRequest request)
			throws OAuthException {
		UUID agent = getAgent(request);
		if (agent == null) {
			throw new OAuthException("Not logged in");
		}
		return store().require(agent).getId();
	}

}
