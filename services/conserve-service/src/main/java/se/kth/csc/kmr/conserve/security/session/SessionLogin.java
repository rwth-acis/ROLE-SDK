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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.dsl.ContempDSL;
import se.kth.csc.kmr.conserve.iface.BaseRequest;

@Path("/o/session")
public class SessionLogin {

	private static final Logger log = LoggerFactory
			.getLogger(SessionLogin.class);

	@Inject
	@Named("conserve.session.context")
	private UUID sessionContext;

	@javax.ws.rs.core.Context
	private UriInfo uriInfo;

	@javax.ws.rs.core.Context
	private HttpHeaders httpHeaders;

	@Inject
	@Named("password-salt")
	private String salt;

	@Inject
	private Contemp store;

	@Inject
	private BaseRequest resolutionRequest;

	private static final SecureRandom RAND = new SecureRandom();

	private ContempDSL store() {
		return (ContempDSL) store;
	}

	private UUID getAgent(boolean logOut) {
		UUID agent = null;
		Map<String, Cookie> cookies = httpHeaders.getCookies();
		if (cookies.containsKey("conserve_session")) {
			log.info("Cookie found");
			Cookie sessionCookie = cookies.get("conserve_session");
			String sessionCookieValue = sessionCookie.getValue();
			log.info("Cookie value: " + sessionCookieValue);
			try {
				UUID sessionUuid = UUID.fromString(sessionCookieValue);
				Concept session = store.getConcept(sessionContext,
						sessionCookieValue);
				if (session != null) {
					List<Control> sessionReferences = store.getControls(
							session.getUuid(), ConserveTerms.reference);
					if (!logOut) {
						if (sessionReferences != null
								&& sessionReferences.size() != 0) {
							agent = sessionReferences.get(0).getObject();
						}
					} else {
						for (Control sessionReference : sessionReferences) {
							store.deleteControl(sessionReference);
						}
						store.deleteConcept(store.getConcept(sessionContext,
								sessionUuid));
						log.info("Session deleted");
					}
				}
			} catch (IllegalArgumentException e) {
				// Move on...
			}
		}
		return agent;
	}

	@Path("logout")
	@POST
	public Response submitLogout(@FormParam("return") String returnUri) {
		getAgent(true);
		JSONObject response = new JSONObject();
		try {
			response.put("result", "logged_out");
		} catch (JSONException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.header("Cache-Control", "no-store").build();
		}
		NewCookie cookie = new NewCookie("conserve_session", "", "/", uriInfo
				.getBaseUri().getHost(), "contemp session id", 1200000, false);
		return Response.ok().cookie(cookie).header("Cache-Control", "no-store")
				.type(MediaType.APPLICATION_JSON_TYPE)
				.entity(response.toString()).build();
	}

	@Path("login")
	@POST
	public Response submitLogin(String jsonString) {
		String userName, providedPassword, realm, context;
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			userName = jsonObject.getString("username");
			providedPassword = jsonObject.getString("password");
			realm = jsonObject.getString("realm");
			context = jsonObject.getString("context");
		} catch (JSONException e) {
			JSONObject response = new JSONObject();
			try {
				response.put("result", "invalid_request");
			} catch (JSONException e2) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.header("Cache-Control", "no-store").build();
			}
			return Response.status(Status.BAD_REQUEST)
					.header("Cache-Control", "no-store")
					.type(MediaType.APPLICATION_JSON_TYPE)
					.entity(response.toString()).build();
		}

		Concept realmConcept = resolutionRequest.resolve(realm);

		Concept user = store().in(realmConcept).sub().get(userName);
		if (user == null) {
			JSONObject response = new JSONObject();
			try {
				response.put("result", "incorrect_user_or_pass");
			} catch (JSONException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.header("Cache-Control", "no-store").build();
			}
			return Response.status(Status.BAD_REQUEST)
					.header("Cache-Control", "no-store")
					.type(MediaType.APPLICATION_JSON_TYPE)
					.entity(response.toString()).build();
		}

		Content meta = store().in(user).as(ConserveTerms.secret).require();
		byte[] actualDigest = store().as(meta).bytes();
		byte[] providedDigest = null;
		if (providedPassword != null) {
			MessageDigest sha1;
			try {
				sha1 = MessageDigest.getInstance("SHA1");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
			providedDigest = sha1.digest((providedPassword + salt).getBytes());
		}
		if (providedDigest == null
				|| !Arrays.equals(providedDigest, actualDigest)) {
			JSONObject response = new JSONObject();
			try {
				response.put("result", "incorrect_user_or_pass");
			} catch (JSONException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.header("Cache-Control", "no-store").build();
			}
			return Response.status(Status.BAD_REQUEST)
					.header("Cache-Control", "no-store")
					.type(MediaType.APPLICATION_JSON_TYPE)
					.entity(response.toString()).build();
		}

		Concept session = store().in(sessionContext).sub()
				.create(randomString());
		store().in(session).put(ConserveTerms.reference, user.getUuid());
		//TODO set session expiresOn in store

		NewCookie cookie = new NewCookie("conserve_session", session.getId(),
				"/", uriInfo.getBaseUri().getHost(), "contemp session id",
				1200000, false);
		JSONObject response = new JSONObject();
		try {
			response.put("result", "logged_in");
		} catch (JSONException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.header("Cache-Control", "no-store").build();
		}
		return Response.ok().cookie(cookie).header("Cache-Control", "no-store")
				.type(MediaType.APPLICATION_JSON_TYPE)
				.entity(response.toString()).build();
	}

	private static String randomString() {
		byte[] secret = new byte[16];
		RAND.nextBytes(secret);
		return Base64.encodeBase64URLSafeString(secret);
	}

}
