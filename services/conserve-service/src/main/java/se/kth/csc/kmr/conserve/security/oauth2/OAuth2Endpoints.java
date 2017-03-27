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

import com.google.common.io.CharStreams;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.dsl.ContempDSL;
import se.kth.csc.kmr.conserve.iface.internal.RequestNotifier;
import se.kth.csc.kmr.conserve.util.TemplateManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Blob;
import java.util.List;
import java.util.UUID;


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
	public Response getRequest(@QueryParam("discovery") String openIdUri,
			@QueryParam("client_id") String client_id,
			@QueryParam("client_secret") String client_secret,
			@QueryParam("return") String return_url) {
		try {


			String discoveryUri = openIdUri;
				DefaultHttpClient httpClient = new DefaultHttpClient();
				Concept provider = store().in(oidcContext).sub(oidcPredicate).get(openIdUri);
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

				
				HttpGet discovery = new HttpGet(openIdUri);
				HttpResponse response = httpClient.execute(discovery);
				int status = response.getStatusLine().getStatusCode();
				if(status>=200 && status < 300){
					Object obj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
					JSONObject finalResult = (JSONObject) obj;
					UriBuilder authorizeUriBuilder = UriBuilder.fromUri(
							(String) finalResult.get("authorization_endpoint"));
					authorizeUriBuilder.queryParam("scope","openid profile email");
					authorizeUriBuilder.queryParam("client_id",client_id);
					authorizeUriBuilder.queryParam("redirect_uri",uriInfo.getBaseUri().toString()+"o/oauth2/authorize");
					authorizeUriBuilder.queryParam("response_type","code");
					JSONObject state = new JSONObject();
					state.put("userinfo_endpoint", (String) finalResult.get("userinfo_endpoint"));
					state.put("token_endpoint", (String) finalResult.get("token_endpoint"));
					state.put("client_id",client_id);
					state.put("client_secret",client_secret);
					if(return_url!=null){
						state.put("return_url",return_url);
					}
					authorizeUriBuilder.queryParam("state",Base64.encodeBase64URLSafeString(state.toString().getBytes()));
					return Response.seeOther(authorizeUriBuilder.build()).build();
	
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
			Object stateRep = JSONValue.parse (new String(Base64.decodeBase64(state)));
			JSONObject stateObj = (JSONObject) stateRep;
			String userEP = (String) stateObj.get("userinfo_endpoint");
			String tokenEP = (String) stateObj.get("token_endpoint");
			String clientId = (String) stateObj.get("client_id");
			String clientSecret = (String) stateObj.get("client_secret"); 
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(tokenEP);
			UriBuilder tokenUriBuilder = UriBuilder.fromUri (tokenEP);
			StringEntity entity = new StringEntity("grant_type=authorization_code&client_id="+clientId+"&client_secret="+clientSecret+"&code="+code+"&redirect_uri="+uriInfo.getBaseUri().toString()+"o/oauth2/authorize"); //+uriInfo.getBaseUri().toString()+"o/oauth2/authenticate");
			post.setEntity(entity);
			post.setHeader("Content-Type","application/x-www-form-urlencoded");
			HttpResponse response = client.execute(post);
			Object obj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
			JSONObject tokenResult = (JSONObject) obj;
			String accessToken = (String) tokenResult.get("access_token");
			long expiresIn = Long.parseLong(tokenResult.get("expires_in").toString());
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
			
			if(userEP.indexOf("google")>=0) userName = "mailto:" + email;
			else{
				String sub = (String) finalResult.get("sub");
				userName = userEP+":"+sub;
				userName = Base64.encodeBase64URLSafeString(userName.getBytes());
			}

			Concept user = store().in(userContext).sub().get(userName);
			if (user == null) {
				user = store().in(userContext).sub(userPredicate)
						.create(userName);
				requestNotifier.setResolution(
						Resolution.StandardType.CONTEXT,
						store.getConcept(userContext));
				requestNotifier.setResolution(
						Resolution.StandardType.CREATED, user);
				requestNotifier.doPost();

			}

			// update user claims (name, email, access_token)
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

			Concept session = store().in(sessionContext).sub()
					.create(randomString());
			store().in(session)
					.put(ConserveTerms.reference, user.getUuid());
			NewCookie cookie = new NewCookie("conserve_session",
					session.getId(), "/", uriInfo.getBaseUri().getHost(),
					"conserve session id", (int) expiresIn, false);
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
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).type(MediaType.TEXT_PLAIN_TYPE)
					.build();
		}
		finally{
			store.disconnect();
		}
	}
	
	@GET
	@POST
	@Path("authorizeImplicit")
	public Response credulousAuthorize(@HeaderParam("access_token") String accessToken,
								   @QueryParam("userinfo_endpoint") String userEP) {
		try {
			// get userinfo from endpoint
			HttpClient client = new DefaultHttpClient();
			HttpGet userinfoRequest = new HttpGet(userEP);
			userinfoRequest.setHeader("Authorization", "Bearer " + accessToken);
			HttpResponse userinfoResponse;
			userinfoResponse = client.execute(userinfoRequest);
			if(userinfoResponse.getStatusLine().getStatusCode() == 401)
				return Response.status(401).entity("Could not retrieve userinfo").build();
			Object userinfoObj = JSONValue.parse(EntityUtils.toString(userinfoResponse.getEntity()));
			JSONObject finalResult = (JSONObject) userinfoObj;
			String firstName = (String) finalResult.get("given_name");
			String lastName = (String) finalResult.get("family_name");
			String email = (String) finalResult.get("email");
			String sub = (String) finalResult.get("sub");
			String userName = userEP + ":" + sub;
			userName = Base64.encodeBase64URLSafeString(userName.getBytes());

			// create user if necessary
			Concept user = store().in(userContext).sub().get(userName);
			Content userContent;
			org.json.JSONObject userMetadata;
			if (user == null) {
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
				userMetadata = new org.json.JSONObject(store().as(userContent).string());
			}

			// update user claims (name, email, access_token)
			Graph g = new GraphImpl();
			ValueFactory vf = g.getValueFactory();
			org.openrdf.model.URI userUri = vf.createURI(store().in(user).uri().toString());
			g.add(vf.createStatement(userUri,
				vf.createURI("http://purl.org/dc/terms/title"), vf.createLiteral(firstName + " " + lastName)));
			// email
			g.add(vf.createStatement(userUri,
				vf.createURI("http://xmlns.com/foaf/0.1/mbox"), vf.createURI("mailto:" + email)));
			// access_token
			g.add(vf.createStatement(userUri,
					vf.createURI("http://xmlns.com/foaf/0.1/openid"), vf.createLiteral(accessToken)));
			store().in(user).as(ConserveTerms.metadata)
					.type("application/json").graph(g);

			// Create new session in store
			Concept session = store().in(sessionContext).sub().create(randomString());
			store().in(session).put(ConserveTerms.reference, user.getUuid());

			// return cookie
			NewCookie cookie = new NewCookie("conserve_session",
					session.getId(), "/", uriInfo.getBaseUri().getHost(),
					"conserve session id", 1000 * 60 * 60 * 24 * 7, false);
			return Response.ok().cookie(cookie).build();
		} catch(JSONException ex) {
			ex.printStackTrace();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		return Response.serverError().build();
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

	private static String randomString() {
		byte[] secret = new byte[16];
		RAND.nextBytes(secret);
		return Base64.encodeBase64URLSafeString(secret);
	}

}
