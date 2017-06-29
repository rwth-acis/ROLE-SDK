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
package se.kth.csc.kmr.oauth;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import se.kth.csc.kmr.conserve.util.TemplateLayout;
import se.kth.csc.kmr.conserve.util.TemplateManager;
import se.kth.csc.kmr.conserve.util.TemplateLayout.ParamFilter;

/**
 * 
 * @author Erik Isaksson <erikis@kth.se>
 * 
 */
@Path("/o/oauth")
@Singleton
public class OAuth1Endpoints {

	@Inject
	private OAuth1 provider;

	@Inject
	private OAuthPrincipal principal;

	@Inject
	@Named("oauth")
	private TemplateManager templates;

	@Context
	private HttpServletRequest request;

	@Inject
	@Named("oauth.attributes")
	private Map<String, ParamFilter> attributeFilters;

	@GET
	@Path("initiate")
	@Produces("application/x-www-form-urlencoded")
	public Response initiate() {
		String response;
		try {
			response = provider.initiate(request);
		} catch (OAuthException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.toString())
					.type(MediaType.TEXT_PLAIN)
					.header("Cache-Control", "no-store").build();
		}
		return Response.ok().entity(response)
				.header("Cache-Control", "no-store").build();
	}

	@GET
	@Path("authorize")
	@Produces("text/html")
	public Response authorize(@QueryParam("oauth_token") String token) {
		URI loginUri = principal.ensureAuthenticated(request);
		if (loginUri != null) {
			return Response.seeOther(loginUri)
					.header("Cache-Control", "no-store").build();
		}
		String resourceOwner;
		OAuth1Token temporary;
		try {
			resourceOwner = principal.getResourceOwnerDisplayName(request);
			temporary = provider.preAuthorize(token);
		} catch (OAuthException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.toString())
					.type(MediaType.TEXT_PLAIN)
					.header("Cache-Control", "no-store").build();
		}
		String consumerKey = temporary.getClient();
		Map<String, String> attributeValues = temporary.getAttributes();
		Map<String, String> attributeIncludes = new HashMap<String, String>();
		Map<String, String> attributeTitles = new HashMap<String, String>();
		for (String key : attributeValues.keySet()) {
			attributeIncludes.put(key, "oauth.authorize.attributes." + key);
			attributeTitles.put(key, "oauth_attributes." + key + ".title");
		}
		TemplateLayout layout = templates.layout("standard")
				.param("oauth_token", token)
				.param("resource_owner", resourceOwner)
				.param("consumer_key", consumerKey)
				.params("attributes", attributeValues, attributeFilters)
				.literals("attributes", attributeTitles)
				.include("attributes", attributeIncludes)
				.base("oauth.authorize");
		return Response.ok().entity(layout.render())
				.type(MediaType.TEXT_HTML_TYPE)
				.header("Cache-Control", "no-store").build();
	}

	@POST
	@Path("authorized")
	@Consumes("application/x-www-form-urlencoded")
	public Response authorized(@FormParam("oauth_token") String token) {
		URI callback;
		try {
			callback = provider.authorize(token,
					principal.getResourceOwner(request));
		} catch (OAuthException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.toString())
					.type(MediaType.TEXT_PLAIN)
					.header("Cache-Control", "no-store").build();
		}
		return Response.seeOther(callback).header("Cache-Control", "no-store")
				.build();
	}

	@GET
	@Path("token")
	@Produces("application/x-www-form-urlencoded")
	public Response token() {
		String response;
		try {
			response = provider.token(request);
		} catch (OAuthException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.toString())
					.type(MediaType.TEXT_PLAIN)
					.header("Cache-Control", "no-store").build();
		}
		return Response.ok().entity(response)
				.header("Cache-Control", "no-store").build();
	}

}
