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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.sql.Blob;


import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.*;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.*;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.dsl.ContempDSL;
import se.kth.csc.kmr.conserve.dsl.ContentDSL;
import se.kth.csc.kmr.conserve.iface.internal.RequestNotifier;
import se.kth.csc.kmr.conserve.util.TemplateManager;

import com.google.common.io.CharStreams;


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
	
	@Inject
	@Named("conserve.oidc.context")
	private UUID oidcContext;
	
	@Inject
	@Named("conserve.oidc.predicate")
	private UUID oidcPredicate;

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
	@Path("request")
	public Response getRequest(@QueryParam("discovery") String discoveryUri,
			@QueryParam("client_id") String client_id,
			@QueryParam("client_secret") String client_secret,
			@QueryParam("return") String return_url) {
		try {


				DefaultHttpClient httpClient = new DefaultHttpClient();
				Concept provider = store().in(oidcContext).sub(oidcPredicate).get(discoveryUri);
				if(provider != null && client_secret == null && client_id == null){
					List<Content> contents = store.getContents(provider.getUuid());
					for(Content x : contents){
						if(x.getType().equals("application/json")){
							Blob asdf = x.getData();
							byte[] bdata = asdf.getBytes(1, (int) asdf.length());
							String z = new String(bdata);
							JSONObject buffer = (JSONObject) JSONValue.parse(z);
							if(buffer.get("") != null){
								JSONObject tmp = (JSONObject) buffer.get("");
								JSONArray secret = (JSONArray) tmp.get("http://purl.org/openapp/secret");
								JSONArray id = (JSONArray) tmp.get("http://purl.org/openapp/app");
								tmp = (JSONObject) secret.get(0);
								client_secret = (String) tmp.get("value");
								tmp = (JSONObject) id.get(0);
								client_id = (String) tmp.get("value");
							}
						}
					}
				}
				if(client_id.equals("") || client_secret.equals("")) return Response.status(404).build();

				
				HttpGet discovery = new HttpGet(discoveryUri);
				HttpResponse response = httpClient.execute(discovery);
				int status = response.getStatusLine().getStatusCode();
				if(status>=200 && status < 300){
					Object obj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
					JSONObject finalResult = (JSONObject) obj;
					// send authentication request
					// http://openid.net/specs/openid-connect-core-1_0.html#rfc.section.3.1.2.1
					JSONObject state = new JSONObject();
					state.put("userinfo_endpoint", (String) finalResult.get("userinfo_endpoint"));
					state.put("token_endpoint", (String) finalResult.get("token_endpoint"));
					state.put("client_id",client_id);
					state.put("client_secret",client_secret);
					if(return_url!=null)
						state.put("return_url",return_url);
					String scopeParam = null;
					if(((String) finalResult.get("token_endpoint")).indexOf("google") >= 0) {
						// google has non standard way to retrieve refresh_token
						// https://developers.google.com/identity/protocols/OpenIDConnect#refresh-tokens
						scopeParam = "scope=openid+profile+email&access_type=offline&prompt=consent";
					} else {
						scopeParam = "scope=openid+profile+email+offline_access";
					}
					return Response.seeOther(
							URI.create((String) finalResult.get("authorization_endpoint") +
									"?" + scopeParam +
									"&client_id=" + client_id +
									"&redirect_uri=" + uriInfo.getBaseUri().toString()+"o/oauth2/authorize" +
									"&response_type=code" +
									"&state=" + Base64.encodeBase64URLSafeString(state.toString().getBytes())
							)).build();
	
				}
				else{
					return Response.status(status).build();
				}
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
					.build();
		} finally {
			store.disconnect();
		}
	}

	
	@GET
	@POST
	@Path("authorize")
	public Response getAccessToken(@QueryParam("code") String code,
			@QueryParam("state") String state){
		try{
			// extract information for oidc provider
			Object stateRep = JSONValue.parse (new String(Base64.decodeBase64(state)));
			JSONObject stateObj = (JSONObject) stateRep;
			String userEP = (String) stateObj.get("userinfo_endpoint");
			String tokenEP = (String) stateObj.get("token_endpoint");
			String clientId = (String) stateObj.get("client_id");
			String clientSecret = (String) stateObj.get("client_secret");

			// send token request
			// http://openid.net/specs/openid-connect-core-1_0.html#rfc.section.3.1.3.1
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(tokenEP);
			post.setEntity( new StringEntity(
					"grant_type=authorization_code" +
							"&client_id="+clientId+
							"&client_secret="+clientSecret+
							"&code="+code+
							"&redirect_uri="+uriInfo.getBaseUri().toString()+"o/oauth2/authorize"
			));
			post.setHeader("Content-Type","application/x-www-form-urlencoded");
			HttpResponse response = client.execute(post);
			Object obj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
			JSONObject tokenResult = (JSONObject) obj;
			String accessToken = (String) tokenResult.get("access_token");
			String refreshToken = (String) tokenResult.get("refresh_token");
			long expiresIn = Long.parseLong(tokenResult.get("expires_in").toString());

			// send userinfo request
			// http://openid.net/specs/openid-connect-core-1_0.html#rfc.section.5.3
			HttpGet userinfoRequest = new HttpGet(userEP);
			userinfoRequest.setHeader("Authorization", "Bearer " + accessToken);
			HttpResponse userinfoResponse;
			userinfoResponse = client.execute(userinfoRequest);
			Object userinfoObj = JSONValue.parse(EntityUtils.toString(userinfoResponse.getEntity()));
			JSONObject finalResult = (JSONObject) userinfoObj;
			String firstName = (String) finalResult.get("given_name");
			String lastName = (String) finalResult.get("family_name");
			String email = (String) finalResult.get("email");
			String userName;

			// TODO remove special case for google (migrate database)
			if(userEP.indexOf("google")>=0) userName = "mailto:" + email;
			else {
				String sub = (String) finalResult.get("sub");
				userName = userEP+":"+sub;
				userName = Base64.encodeBase64URLSafeString(userName.getBytes());
			}

			Concept user = store().in(userContext).sub().get(userName);
			Content userContent;
			org.json.JSONObject userMetadata;

			if (user == null) {
				// create user in store
				user = store().in(userContext).sub(userPredicate)
						.create(userName);
				userContent = (Content) store().in(user).as(ConserveTerms.metadata).type("application/json");
				userMetadata = new org.json.JSONObject();
				requestNotifier.setResolution(
						Resolution.StandardType.CONTEXT,
						store.getConcept(userContext));
				requestNotifier.setResolution(
						Resolution.StandardType.CREATED, user);
				requestNotifier.doPost();
			} else {
				userContent = store().in(user).as(ConserveTerms.metadata).get();
				userMetadata = new org.json.JSONObject(	store().as(userContent).string() );
			}

			// update user claims (name, email, access_token) and used provider (token_endpoint,client_id,client_secret)
			// but keep refresh_token from last time if no new one
			// TODO would be better to retrieve provider from store
			// TODO support non-rdf json in /:index
			userMetadata
				.put("http://purl.org/dc/terms/title", firstName + " " + lastName)
				.put("http://xmlns.com/foaf/0.1/mbox", "mailto:" + email)
				.put(":access_token", accessToken)
				.put(":access_token_expiration_date", new java.util.Date().getTime() + 1000*expiresIn)
				.put(":token_endpoint", tokenEP)
				.put(":oidc_client_id", clientId)
				.put(":oidc_client_secret", clientSecret);
			if (refreshToken != null)
				userMetadata.put(":refresh_token", refreshToken);
			store().as(userContent).type("application/json").string(userMetadata.toString());

			// Create new session and its expiration in store
			Concept session = store().in(sessionContext).sub().create(randomString());
			store().in(session).put(ConserveTerms.reference, user.getUuid());
			store().in(session).as(ConserveTerms.expiresOn).type(MediaType.TEXT_PLAIN).string(
					Long.toString(new Date().getTime() + 24*60*60*1000));

			NewCookie cookie = new NewCookie("conserve_session",
					session.getId(), "/", uriInfo.getBaseUri().getHost(),
					"conserve session id", 1000*60*60*24*7, false);
			UriBuilder spacereturn = UriBuilder.fromUri(uriInfo.getBaseUri().toString());
			spacereturn.path(":authentication");
			spacereturn.queryParam("access_token", accessToken);
			spacereturn.queryParam("userinfo_endpoint", userEP);
			if(stateObj.get("return_url") != null){
				spacereturn.queryParam("return",(String) stateObj.get("return_url"));
			}
			return Response.seeOther(spacereturn.build()).cookie(cookie)
					.header("Cache-Control", "no-store").build();
		}
		catch(Exception e){
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
					.build();
		}
		finally{
			store.disconnect();
		}
	}
	
	@GET
	@Path("provider")
	public Response getProvider(){
		try{
			List<Concept> concepts = store()
				.in(oidcContext)
				.sub(oidcPredicate)
				.list();
			int count=0;
			JSONArray arr = new JSONArray();
			for(Concept s : concepts){
				JSONObject obj = new JSONObject();
				List<Content> contents = store.getContents(s.getUuid());
				for(Content x : contents){
					if(x.getType().equals("application/json")){
						Blob asdf = x.getData();
						byte[] bdata = asdf.getBytes(1, (int) asdf.length());
						String z = new String(bdata);
						log.info(z);
						JSONObject buffer = (JSONObject) JSONValue.parse(z);
						if(buffer.get("") != null){
							JSONObject tmp = (JSONObject) buffer.get("");
							JSONArray config = (JSONArray) tmp.get("http://purl.org/openapp/configuration");
							JSONArray title = (JSONArray) tmp.get("http://purl.org/dc/terms/title");
							tmp = (JSONObject) config.get(0);
							String abc = (String) tmp.get("value");
							obj.put("config",abc);
							tmp = (JSONObject) title.get(0);
							abc = (String) tmp.get("value");
							obj.put("title",abc);
						}
					}
				}
				arr.add(obj);
			}
		return Response.ok().type("application/json").entity(arr.toString()).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
					.build();
		}
		
	}
	
	@POST
	@Path("provider")
	public Response createProvider(@Context HttpServletRequest request){
		try{
			JSONObject obj;
			String data;
			if ("POST".equalsIgnoreCase(request.getMethod())) {
				data = CharStreams.toString(request.getReader());
			}
			else{
				return Response.notModified().build();
			}
			log.info(data);
			obj = (JSONObject) JSONValue.parse(data);
			String config = "";
			String name = "";
			String clientId = "";
			String clientSecret = "";
			String dynon = "";
			boolean dynreg;
			if((obj == null || obj.get("config") == null || obj.get("name") == null)){
				return Response.status(Status.BAD_REQUEST).build();
			}
			config = (String) obj.get("config");
			name = (String) obj.get("name");
			
			if(obj.get("dynreg")!=null) dynon = (String) obj.get("dynreg");
			
			if(dynon.equals("on")) dynreg = true;
			else	dynreg = false;
			
			if(dynreg == false){
				if(obj.get("client_id")!=null){
					clientId = (String) obj.get("client_id");
				}else{
					return Response.status(Status.BAD_REQUEST).build();
				}
				if(obj.get("client_secret")!=null){
					clientSecret = (String) obj.get("client_secret");
				}else{
					return Response.status(Status.BAD_REQUEST).build();
				}
			}
			
			JSONObject dynRegData;
			if(dynreg == true){
				HttpClient client = new DefaultHttpClient();
				HttpGet getReq = new HttpGet(config);
				HttpResponse response = client.execute(getReq);
				Object conf = JSONValue.parse(EntityUtils.toString(response.getEntity()));
				JSONObject finConf = (JSONObject) conf;
				String uri = (String) finConf.get("registration_endpoint");
				dynRegData = attemptDynRegistration(uri);
				if(dynRegData == null){
					return Response.serverError().build();
				}
				clientId = (String) dynRegData.get("client_id");
				clientSecret = (String) dynRegData.get("client_secret");
			}
			
			Concept provider = store().in(oidcContext).sub(oidcPredicate).get(config);
			if(provider == null){
				provider = store().in(oidcContext).sub(oidcPredicate)
						.create(config);
				Graph graph = new GraphImpl();
				ValueFactory valueFactory = graph.getValueFactory();
				org.openrdf.model.URI providerUri = valueFactory
						.createURI(store()
								.in(provider)
								.uri()
								.toString());
				
				graph.add(valueFactory.createStatement(
						providerUri,
						valueFactory
								.createURI("http://purl.org/dc/terms/title"),
						valueFactory.createLiteral(name)));
				graph.add(valueFactory.createStatement(
						providerUri,
						valueFactory
								.createURI("http://purl.org/openapp/configuration"),
						valueFactory.createLiteral(config)));

				graph.add(valueFactory.createStatement(
						providerUri,
						valueFactory
								.createURI("http://purl.org/openapp/app"),
						valueFactory.createLiteral(clientId)));
				graph.add(valueFactory.createStatement(
						providerUri,
						valueFactory
								.createURI("http://purl.org/openapp/secret"),
						valueFactory.createLiteral(clientSecret)));
				store().in(provider).as(ConserveTerms.metadata)
						.type("application/json").graph(graph);
				requestNotifier.setResolution(
						Resolution.StandardType.CONTEXT,
						store.getConcept(userContext));
				requestNotifier.setResolution(
						Resolution.StandardType.CREATED, provider);
				requestNotifier.doPost();
			}else{	
				return Response.status(Status.CONFLICT).build();
			}
			return Response.ok().build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
					.build();
		}
	}
	

	public JSONObject attemptDynRegistration(String uri){
		try{
			String regData = "{'redirect_uris':['"+uriInfo.getBaseUriBuilder().path("o/oauth2/authorize").build().toString()+"'],'client_name':'ROLE-SDK',"
					+"'response_types':['code'],'grant_types':['authorization_code']}";

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(uri);
			post.setHeader("Content-Type","application/json");
			post.setHeader("Accept","application/json");
			StringEntity data = new StringEntity(regData);
			post.setEntity(data);
			HttpResponse response = client.execute(post);
			Object obj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
			JSONObject regResult = (JSONObject) obj;
			log.info((String) regResult.get("client_id"));
			log.info((String) regResult.get("client_secret"));
			int status = response.getStatusLine().getStatusCode();
			regResult.put("status_code",status);
			return regResult;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@GET
	@Path("access_token")
	public Response getAccessToken() {

//		if session is not valid
//			return 'man you are not logged in!'
//		if access_token has not expired yet
//			return access_token
//		else
//			request new access_token
//				if success
//					return access_token
//				else
//					// should not happen
//					return error: please logout and in


		// check session is valid
		UUID user = null;
		Concept session = null;
		javax.servlet.http.Cookie[] cookies = request.getCookies();

		if (cookies != null)
			for(javax.servlet.http.Cookie cookie : cookies)
				if("conserve_session".equals(cookie.getName()))
					session = store.getConcept(sessionContext, cookie.getValue());
		if (session != null) {
			if ( new Date().getTime() <= Long.parseLong(
					((ContentDSL) store().in(session).as(ConserveTerms.expiresOn).get()).string())) {
				List<Control> sessionReferences = store.getControls(session.getUuid(), ConserveTerms.reference);
				if (sessionReferences != null && sessionReferences.size() != 0) {
					user = sessionReferences.get(0).getObject();
				}
			}
		}
		if (user == null)
			return Response.status(Status.FORBIDDEN)
					.entity("Man, you didn't log in!").type(MediaType.TEXT_PLAIN_TYPE)
					.build();

		// return access_token if it does not expire soon
		Content userMetadata = store().in(user).as(ConserveTerms.metadata).get();
		org.json.JSONObject userMetadataJson = null;
		try {
			userMetadataJson = new org.json.JSONObject(
					store().as(userMetadata).string()  );

			if ( ((Long)userMetadataJson.get(":access_token_expiration_date")).longValue() >= new Date().getTime() + 10*60*1000) {
				return Response.status(Status.OK)
					.entity(new org.json.JSONObject()
							.put("access_token",userMetadataJson.get(":access_token"))
							.put("access_token_expiration_date",userMetadataJson.get(":access_token_expiration_date"))
							.put("access_token_expires_in",((Long)userMetadataJson.get(":access_token_expiration_date")).longValue() - new Date().getTime())
							.toString())
					.type(MediaType.APPLICATION_JSON_TYPE).build();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		// access_token has expired or expires soon, so retrieve a new one
		try {
			if (!userMetadataJson.has(":refresh_token"))
				return Response.status(Status.FORBIDDEN).entity("Please logout and login!").build();
			String clientId = (String) userMetadataJson.get(":oidc_client_id");
			String clientSecret = (String) userMetadataJson.get(":oidc_client_secret");

			// spec: https://tools.ietf.org/html/rfc6749#section-6
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost((String) userMetadataJson.get(":token_endpoint"));
			post.setEntity(	 new StringEntity(
					"grant_type=refresh_token" +
							"&refresh_token=" + (String) userMetadataJson.get(":refresh_token")) );
			post.setHeader("Content-Type","application/x-www-form-urlencoded");
			post.setHeader("Authorization",
					"Basic " + java.util.Base64.getEncoder().encodeToString(String.format("%s:%s", clientId, clientSecret).getBytes()) );
			HttpResponse response = client.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				org.json.JSONObject responseBody = new org.json.JSONObject(EntityUtils.toString(response.getEntity()));
				userMetadataJson
					.put(":access_token", responseBody.get("access_token"))
					.put(":access_token_expiration_date", new java.util.Date().getTime() + 1000*(Integer)responseBody.get("expires_in"));
				store().as(userMetadata).type("application/json").string(userMetadataJson.toString());
				return Response.ok().entity(new org.json.JSONObject()
								.put("access_token",userMetadataJson.get(":access_token"))
								.put("access_token_expiration_date",userMetadataJson.get(":access_token_expiration_date"))
								.put("access_token_expires_in",((Long)userMetadataJson.get(":access_token_expiration_date")).longValue() - new Date().getTime())
										.toString()
						).type(MediaType.APPLICATION_JSON_TYPE).build();
			} else
				return Response.status(Status.FORBIDDEN).entity("Please logout and login!").build();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}

	private static String randomString() {
		byte[] secret = new byte[16];
		RAND.nextBytes(secret);
		return Base64.encodeBase64URLSafeString(secret);
	}

}
