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
package se.kth.csc.kmr.conserve.security.oauth2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONTokener;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.dsl.ContempDSL;
import se.kth.csc.kmr.conserve.iface.internal.RequestNotifier;
import se.kth.csc.kmr.conserve.util.TemplateManager;

@Path("/o/oauth2")
public class OAuth2Endpoints {

	private static final Logger log = LoggerFactory
			.getLogger(OAuth2Endpoints.class);

	@Inject
	@Named("conserve.session.context")
	private UUID sessionContext;

	@Inject
	@Named("conserve.user.context")
	private UUID userContext;

	@Inject
	@Named("conserve.user.predicate")
	private UUID userPredicate;

	@javax.ws.rs.core.Context
	private UriInfo uriInfo;

	@javax.ws.rs.core.Context
	private HttpServletRequest request;

	@Inject
	private Contemp store;

	@Inject
	private RequestNotifier requestNotifier;

	@Inject
	@Named("oauth")
	private TemplateManager templates;

	private static final SecureRandom RAND = new SecureRandom();

	private ContempDSL store() {
		return (ContempDSL) store;
	}

	@GET
	@POST
	@Path("authenticate")
	public Response getAuthentication(
			@QueryParam("access_token") String accessToken,
			@QueryParam("return") String localReturnUri) {
		try {
			log.info("OAuth2 authentication in progress");

			// Query Google's userinfo service for the family_name and
			// given_name.

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(
					"https://www.googleapis.com/oauth2/v2/userinfo");
			request.setHeader("Authorization", "Bearer " + accessToken);
			HttpResponse response;
			try {
				response = client.execute(request);

				// Get the response
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				StringBuilder builder = new StringBuilder();
				for (String line = null; (line = reader.readLine()) != null;) {
					builder.append(line).append("\n");
				}

				Object obj = JSONValue.parse(builder.toString());
				JSONObject finalResult = (JSONObject) obj;

				String firstName = (String) finalResult.get("given_name");
				String lastName = (String) finalResult.get("family_name");
				String email = (String) finalResult.get("email");

				// String userName = verified.getIdentifier();
				String userName = "mailto:" + email;

				Concept user = store().in(userContext).sub().get(userName);
				if (user == null) {

					user = store().in(userContext).sub(userPredicate)
							.create(userName);

					Graph graph = new GraphImpl();
					ValueFactory valueFactory = graph.getValueFactory();
					org.openrdf.model.URI userUri = valueFactory
							.createURI(store().in(user).uri().toString());
					graph.add(valueFactory.createStatement(
							userUri,
							valueFactory
									.createURI("http://purl.org/dc/terms/title"),
							valueFactory.createLiteral(firstName + " "
									+ lastName)));

					// email
					graph.add(valueFactory.createStatement(
							userUri,
							valueFactory
									.createURI("http://xmlns.com/foaf/0.1/mbox"),
							valueFactory.createURI("mailto:" + email)));

					// access_token
					graph.add(valueFactory.createStatement(
							userUri,
							valueFactory
									.createURI("http://xmlns.com/foaf/0.1/openid"),
							valueFactory.createLiteral(accessToken)));

					store().in(user).as(ConserveTerms.metadata)
							.type("application/json").graph(graph);

					requestNotifier.setResolution(
							Resolution.StandardType.CONTEXT,
							store.getConcept(userContext));
					requestNotifier.setResolution(
							Resolution.StandardType.CREATED, user);
					requestNotifier.doPost();

				}

				Concept session = store().in(sessionContext).sub()
						.create(randomString());
				store().in(session)
						.put(ConserveTerms.reference, user.getUuid());

				NewCookie cookie = new NewCookie("conserve_session",
						session.getId(), "/", uriInfo.getBaseUri().getHost(),
						"conserve session id", 1200000, false);

				URI returnUri;
				if (localReturnUri != null) {
					try {
						returnUri = new URI(localReturnUri);
					} catch (URISyntaxException e) {
						returnUri = uriInfo.getBaseUriBuilder().build();
					}
				} else {
					returnUri = uriInfo.getBaseUriBuilder().build();
				}

				return Response.seeOther(returnUri).cookie(cookie)
						.header("Cache-Control", "no-store").build();

			} catch (Exception e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
						.build();
			}

		} finally {
			store.disconnect();
		}
	}

	private static String randomString() {
		byte[] secret = new byte[16];
		RAND.nextBytes(secret);
		return Base64.encodeBase64URLSafeString(secret);
	}

}
