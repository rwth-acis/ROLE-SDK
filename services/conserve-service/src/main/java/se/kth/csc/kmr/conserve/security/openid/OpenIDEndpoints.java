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
package se.kth.csc.kmr.conserve.security.openid;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.server.ServerManager;
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
import se.kth.csc.kmr.conserve.util.Base64UUID;
import se.kth.csc.kmr.conserve.util.TemplateLayout;
import se.kth.csc.kmr.conserve.util.TemplateManager;

@Path("/o/openid")
public class OpenIDEndpoints {

	private static final Logger log = LoggerFactory
			.getLogger(OpenIDEndpoints.class);

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
	private ConsumerManager manager;

	@Inject
	public ServerManager server;

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
	@Path("request")
	public Response getRequest(@QueryParam("openid") String openIdUri,
			@QueryParam("return") String localReturnUri) {
		try {
			log.info("Initiating OpenID request");

			UriBuilder returnUriBuilder = uriInfo.getBaseUriBuilder().path(
					"o/openid/authenticate");
			if (openIdUri != null) {
				returnUriBuilder.queryParam("openid", openIdUri);
			}
			if (localReturnUri != null) {
				returnUriBuilder.queryParam("return", localReturnUri);
			}
			String returnUri = returnUriBuilder.build().toString();

			// String userSuppliedString =
			// "https://www.google.com/accounts/o8/id";
			// String userSuppliedString = uriInfo.getBaseUriBuilder()
			// .path("o/openid").build().toString();
			String userSuppliedString = openIdUri;

			// perform discovery on the user-supplied identifier
			List<?> discoveries;
			try {
				discoveries = manager.discover(userSuppliedString);
			} catch (DiscoveryException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
						.build();
			}

			// attempt to associate with the OpenID provider
			// and retrieve one service endpoint for authentication
			DiscoveryInformation discovered = manager.associate(discoveries);

			// store the discovery information in the user's session for later
			// use
			// leave out for stateless operation / if there is no session
			// session.setAttribute("discovered", discovered);

			// obtain a AuthRequest message to be sent to the OpenID provider
			AuthRequest authReq = null;
			try {
				authReq = manager.authenticate(discovered, returnUri);
			} catch (MessageException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
						.build();
			} catch (ConsumerException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
						.build();
			}

			FetchRequest fetch = FetchRequest.createFetchRequest();
			try {
				fetch.addAttribute("FirstName",
						"http://axschema.org/namePerson/first", true);
				fetch.addAttribute("LastName",
						"http://axschema.org/namePerson/last", true);
				fetch.addAttribute("Email",
						"http://schema.openid.net/contact/email", true);

				// wants up to three email addresses
				// fetch.setCount("Email", 3);

				authReq.addExtension(fetch);
			} catch (MessageException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
						.build();
			}

			if (discovered.isVersion2()) {
				// POST redirect
				log.info("OpenID version 2 - using HTML FORM redirect");
				StringBuffer html = new StringBuffer();
				html.append("<!doctype html><html><head><title>OpenID redirect</title></head><body onload=\"document.forms['openid-form-redirection'].submit();\">");
				html.append("<form name=\"openid-form-redirection\" action=\"");
				html.append(authReq.getDestinationUrl(false).replace("&", "&amp;").replace("\"", "&quot;"));
				html.append("\" method=\"post\" accept-charset=\"utf-8\">");
				for (Map.Entry<?,?> entry : ((Map<?,?>)authReq.getParameterMap()).entrySet()) {
					html.append("<input type=\"hidden\" name=\"");
					html.append(entry.getKey().toString().replace("&", "&amp;").replace("\"", "&quot;"));
					html.append("\" value=\"");
					html.append(entry.getValue().toString().replace("&", "&amp;").replace("\"", "&quot;"));
					html.append("\"/>");
				}
				html.append("<input type=\"submit\" value=\"Continue\"></form></body></html>");
				return Response
						.ok()
						.entity(html.toString().getBytes(
								Charset.forName("UTF-8")))
						.type("text/html; charset=UTF-8")
						.header("Cache-Control", "no-store").build();
			} else {
				// GET redirect
				log.info("Not OpenID version 2 - must use 303 redirect");
				try {
					return Response.seeOther(
							new URI(authReq.getDestinationUrl(true))).build();
				} catch (URISyntaxException e) {
					return Response.status(Status.INTERNAL_SERVER_ERROR)
							.entity(e.getMessage())
							.type(MediaType.TEXT_PLAIN_TYPE).build();
				}
			}
		} finally {
			store.disconnect();
		}
	}

	@GET
	@POST
	@Path("authenticate")
	public Response getAuthentication(@QueryParam("openid") String openIdUri,
			@QueryParam("return") String localReturnUri) {
		try {
			log.info("OpenID authentication in progress");

			// extract the parameters from the authentication response
			// (which comes in as a HTTP request from the OpenID provider)
			ParameterList openidResp = new ParameterList(
					request.getParameterMap());

			// retrieve the previously stored discovery information
			// DiscoveryInformation discovered = (DiscoveryInformation)
			// session.getAttribute("discovered");
			// String userSuppliedString =
			// "https://www.google.com/accounts/o8/id";
			// String userSuppliedString = uriInfo.getBaseUriBuilder()
			// .path("o/openid").build().toString();
			String userSuppliedString = openIdUri;

			// perform discovery on the user-supplied identifier
			List<?> discoveries;
			try {
				discoveries = manager.discover(userSuppliedString);
			} catch (DiscoveryException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
						.build();
			}
			// attempt to associate with the OpenID provider
			// and retrieve one service endpoint for authentication
			DiscoveryInformation discovered = manager.associate(discoveries);

			// extract the receiving URL from the HTTP request
			StringBuffer receivingURL = request.getRequestURL();
			String queryString = request.getQueryString();
			if (queryString != null && queryString.length() > 0)
				receivingURL.append("?").append(request.getQueryString());

			// verify the response
			VerificationResult verification;
			try {
				verification = manager.verify(receivingURL.toString(),
						openidResp, discovered);
			} catch (MessageException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
						.build();
			} catch (DiscoveryException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
						.build();
			} catch (AssociationException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
						.build();
			}

			// examine the verification result and extract the verified
			// identifier
			Identifier verified = verification.getVerifiedId();

			if (verified != null) {
				log.info("OpenID successfully verified: "
						+ verified.getIdentifier());
				// success, use the verified identifier to identify the user

				Message authSuccess = verification.getAuthResponse();
				if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
					MessageExtension ext;
					try {
						ext = authSuccess.getExtension(AxMessage.OPENID_NS_AX);
						if (ext instanceof FetchResponse) {
							FetchResponse fetchResp = (FetchResponse) ext;

							String firstName = fetchResp
									.getAttributeValue("FirstName");
							String lastName = fetchResp
									.getAttributeValue("LastName");
							String email = fetchResp.getAttributeValue("Email");

							// String userName = verified.getIdentifier();
							String userName = "mailto:" + email;

							Concept user = store().in(userContext).sub()
									.get(userName);
							if (user == null) {

								user = store().in(userContext)
										.sub(userPredicate).create(userName);

								Graph graph = new GraphImpl();
								ValueFactory valueFactory = graph
										.getValueFactory();
								org.openrdf.model.URI userUri = valueFactory
										.createURI(store().in(user).uri()
												.toString());
								graph.add(valueFactory.createStatement(
										userUri,
										valueFactory
												.createURI("http://purl.org/dc/terms/title"),
										valueFactory.createLiteral(firstName
												+ " " + lastName)));
								graph.add(valueFactory.createStatement(
										userUri,
										valueFactory
												.createURI("http://xmlns.com/foaf/0.1/mbox"),
										valueFactory.createURI("mailto:"
												+ email)));
								store().in(user).as(ConserveTerms.metadata)
										.type("application/json").graph(graph);

								// // can have multiple values
								// List emails =
								// fetchResp.getAttributeValues("Email");

								requestNotifier.setResolution(
										Resolution.StandardType.CONTEXT,
										store.getConcept(userContext));
								requestNotifier.setResolution(
										Resolution.StandardType.CREATED, user);
								requestNotifier.doPost();

							}

							Concept session = store().in(sessionContext).sub()
									.create(randomString());
							store().in(session).put(ConserveTerms.reference,
									user.getUuid());

							NewCookie cookie = new NewCookie(
									"conserve_session", session.getId(), "/",
									uriInfo.getBaseUri().getHost(),
									"conserve session id", 1200000, false);

							URI returnUri;
							if (localReturnUri != null) {
								try {
									returnUri = new URI(localReturnUri);
								} catch (URISyntaxException e) {
									returnUri = uriInfo.getBaseUriBuilder()
											.build();
								}
							} else {
								returnUri = uriInfo.getBaseUriBuilder().build();
							}

							return Response.seeOther(returnUri).cookie(cookie)
									.header("Cache-Control", "no-store")
									.build();
						}
						// else if (ext instanceof StoreResponse)
						// {
						// ...
						// }
					} catch (MessageException e) {
						log.warn("OpenID message exception: ", e);
						 return Response.status(Status.INTERNAL_SERVER_ERROR)
								 .entity(e.getMessage())
								 .type(MediaType.TEXT_PLAIN_TYPE).build();
					}
				}
			}
		} finally {
			store.disconnect();
		}
		log.info("OpenID verification failed");
		return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity("OpenID verification failed")
				.type(MediaType.TEXT_PLAIN_TYPE).build();
	}

	private static String randomString() {
		byte[] secret = new byte[16];
		RAND.nextBytes(secret);
		return Base64.encodeBase64URLSafeString(secret);
	}

	@GET
	@Path("server")
	public Response postServer() {
		return getServer(null);
	}

	@POST
	@Path("server")
	public Response getServer(String formData) {
		log.info("OpenID server invoked");
		try {
			server.setOPEndpointUrl(uriInfo.getBaseUriBuilder()
					.path("o/openid/server").build().toString());

			// extract the parameters from the request
			ParameterList params = new ParameterList();
			String queryString = request.getQueryString();
			if (queryString != null && queryString.length() > 0) {
				params.addParams(ParameterList
						.createFromQueryString(queryString));
			}
			if (formData != null) {
				params.addParams(ParameterList.createFromQueryString(formData));
			}

			String mode = params.hasParameter("openid.mode") ? params
					.getParameterValue("openid.mode") : null;

			log.info("Parameter count: " + params.getParameters().size());
			for (Object param : params.getParameters()) {
				log.info("Parameter: " + param.toString());
			}

			Message response;
			String responseText;

			if ("associate".equals(mode)) {
				log.info("Processing an association request");

				// --- process an association request ---
				response = server.associationResponse(params);
				responseText = response.keyValueFormEncoding();
			} else if ("checkid_setup".equals(mode)
					|| "checkid_immediate".equals(mode)) {
				// interact with the user and obtain data needed to continue
				log.info("Checking id");

				if ("checkid_setup".equals(mode)) {

					String realm = params.hasParameter("openid.realm") ? params
							.getParameterValue("openid.realm") : null;
					try {
						realm = new URI(realm).getHost();
					} catch (URISyntaxException e) {
						// Leave the realm value as-is
					}

					Map<String, String> paramMap = new HashMap<String, String>();
					for (Object p : params.getParameters()) {
						Parameter param = (Parameter) p;
						paramMap.put(param.getKey(), param.getValue());
					}

					TemplateLayout layout = templates.layout("standard")
							.param("realm", realm).params("form", paramMap)
							.base("openid.authorize");
					return Response.ok().entity(layout.render())
							.type(MediaType.TEXT_HTML_TYPE)
							.header("Cache-Control", "no-store").build();

				} else {

					String userSelectedId = null;
					String userSelectedClaimedId = null;
					boolean authenticatedAndApproved = false;

					// TODO: use agent = getAgent(request);
					UUID agent = UUID
							.fromString("16ef569e-6671-11e0-82df-705ab6abc097");
					if (agent != null) {
						// Context authUser =
						// store().in(user_root).sub(foaf_member)
						// .require(agent);
						userSelectedId = uriInfo.getBaseUriBuilder()
								.path(Base64UUID.encode(agent)) // .path("foaf:openid")
								.build().toString();
						userSelectedClaimedId = userSelectedId;
						authenticatedAndApproved = true;
					}

					// --- process an authentication request ---
					response = server.authResponse(params, userSelectedId,
							userSelectedClaimedId, authenticatedAndApproved);

					if (response instanceof DirectError) {
						log.warn("Check id resulted in an error");
						return Response.ok()
								.entity(response.keyValueFormEncoding())
								.type(MediaType.TEXT_PLAIN_TYPE)
								.header("Cache-Control", "no-store").build();
					} else {
						log.info("Check id successful; redirecting");

						// caller will need to decide which of the following to
						// use:

						// option1: GET HTTP-redirect to the return_to URL
						return Response
								.seeOther(
										URI.create(response
												.getDestinationUrl(true)))
								.type(MediaType.TEXT_PLAIN_TYPE)
								.header("Cache-Control", "no-store").build();

						// option2: HTML FORM Redirection
						// RequestDispatcher dispatcher =
						// getServletContext().getRequestDispatcher("formredirection.jsp");
						// httpReq.setAttribute("prameterMap",
						// response.getParameterMap());
						// httpReq.setAttribute("destinationUrl",
						// response.getDestinationUrl(false));
						// dispatcher.forward(request, response);
						// return null;
					}

				}
			} else if ("check_authentication".equals(mode)) {
				log.info("Processing a verification request");

				// --- processing a verification request ---
				response = server.verify(params);
				responseText = response.keyValueFormEncoding();
			} else {
				log.info("OpenID server invoked with an unknown mode: " + mode);

				// --- error response ---
				response = DirectError.createDirectError("Unknown request");
				responseText = response.keyValueFormEncoding();
			}

			// return the result to the user
			return Response.ok().entity(responseText)
					.type(MediaType.TEXT_PLAIN_TYPE)
					.header("Cache-Control", "no-store").build();

		} catch (Exception e) {
			log.error("OpenID provider error", e);

			Message response = DirectError
					.createDirectError("OpenID provider error");
			String responseText = response.keyValueFormEncoding();
			return Response.ok().entity(responseText)
					.type(MediaType.TEXT_PLAIN_TYPE)
					.header("Cache-Control", "no-store").build();
		} finally {
			store.disconnect();
		}
	}

	@POST
	@Path("authorized")
	public Response getAuthorized(String formData) {
		log.info("OpenID server processing authorization");
		try {
			// extract the parameters from the request
			ParameterList params = new ParameterList();
			if (formData != null) {
				params.addParams(ParameterList.createFromQueryString(formData));
			}

			Message response;
			// String responseText;

			String userSelectedId = null;
			String userSelectedClaimedId = null;
			boolean authenticatedAndApproved = false;

			// TODO: use agent = getAgent(request);
			UUID agent = UUID
					.fromString("16ef569e-6671-11e0-82df-705ab6abc097");
			if (agent != null) {
				// Context authUser = store().in(user_root).sub(foaf_member)
				// .require(agent);
				userSelectedId = uriInfo.getBaseUriBuilder()
						.path(Base64UUID.encode(agent)) // .path("foaf:openid")
						.build().toString();
				userSelectedClaimedId = userSelectedId;
				authenticatedAndApproved = true;
			}

			// --- process an authentication request ---
			response = server.authResponse(params, userSelectedId,
					userSelectedClaimedId, authenticatedAndApproved);

			if (response instanceof DirectError) {
				log.warn("Check id resulted in an error");
				return Response.ok().entity(response.keyValueFormEncoding())
						.type(MediaType.TEXT_PLAIN_TYPE)
						.header("Cache-Control", "no-store").build();
			} else {
				log.info("Check id successful; redirecting");

				// caller will need to decide which of the following to use:

				// option1: GET HTTP-redirect to the return_to URL
				return Response
						.seeOther(URI.create(response.getDestinationUrl(true)))
						.type(MediaType.TEXT_PLAIN_TYPE)
						.header("Cache-Control", "no-store").build();

				// option2: HTML FORM Redirection
				// RequestDispatcher dispatcher =
				// getServletContext().getRequestDispatcher("formredirection.jsp");
				// httpReq.setAttribute("prameterMap",
				// response.getParameterMap());
				// httpReq.setAttribute("destinationUrl",
				// response.getDestinationUrl(false));
				// dispatcher.forward(request, response);
				// return null;
			}
		} catch (Exception e) {
			log.error("OpenID provider error", e);

			Message response = DirectError
					.createDirectError("OpenID provider error");
			String responseText = response.keyValueFormEncoding();
			return Response.ok().entity(responseText)
					.type(MediaType.TEXT_PLAIN_TYPE)
					.header("Cache-Control", "no-store").build();
		} finally {
			store.disconnect();
		}
	}

	@POST
	public Response postOpenID(String formData) {
		return getServer(formData);
	}

	@GET
	public Response getOpenID(@QueryParam("id") String id) {
		try {
			UriBuilder uri = uriInfo.getBaseUriBuilder()
					.path("o/openid/server");
			// if (id != null) {
			// uri.queryParam("id", id);
			// }
			StringBuffer xrds = new StringBuffer();
			xrds.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			xrds.append("<xrds:XRDS xmlns:xrds=\"xri://$xrds\" xmlns=\"xri://$xrd*($v*2.0)\">\n");
			xrds.append(" <XRD>\n");
			xrds.append("  <Service priority=\"0\">\n");
			xrds.append("   <Type>http://specs.openid.net/auth/2.0/server</Type>\n");
			// xrds.append("   <Type>http://openid.net/srv/ax/1.0</Type>\n");
			// xrds.append("   <Type>http://specs.openid.net/extensions/ui/1.0/mode/popup</Type>\n");
			// xrds.append("   <Type>http://specs.openid.net/extensions/ui/1.0/icon</Type>\n");
			// xrds.append("   <Type>http://specs.openid.net/extensions/pape/1.0</Type>\n");
			xrds.append("   <URI>" + uri.build().toString() + "</URI>\n");
			xrds.append("  </Service>\n");
			xrds.append(" </XRD>\n");
			xrds.append("</xrds:XRDS>\n");
			return Response.ok()
					.entity(xrds.toString().getBytes(Charset.forName("UTF-8")))
					.type("application/xrds+xml; charset=UTF-8")
					.header("Cache-Control", "no-store").build();
		} finally {
			store.disconnect();
		}
	}

}
