package eu.role_project.service.space;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.shindig.auth.BlobCrypterSecurityToken;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypterException;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.common.util.Utf8UrlCoder;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.GadgetSpecFactory;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.apache.shindig.gadgets.spec.ModulePrefs;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.logic.SystemResponder;
import se.kth.csc.kmr.conserve.security.SecurityInfo;
import se.kth.csc.kmr.conserve.util.Base64UUID;

public class SpaceSystemResponder extends SystemResponder {

	private static Logger log = LoggerFactory
			.getLogger(SpaceSystemResponder.class);

	@Inject
	private SecurityInfo securityInfo;

	@Inject
	@Named("xmpp.host")
	private String xmppHost;

	@Inject
	@Named("xmpp.mucs")
	private String xmppMucServiceSubdomainNode;

	@Inject
	private GadgetSpecFactory gadgetSpecFactory;

	@Inject
	private BlobCrypter crypter;
	
	@Inject
	@Named("conserve.user.predicate")
	private UUID userPredicateUuid;
	
	@Inject
	@Named("conserve.session.context")
	private UUID sessionContext;

	private JSONParser parser = new JSONParser();
	
	@Override
	public void addTriples(Graph graph, Request request, Concept context,
			UriBuilder uriBuilder) {
		final ValueFactory valueFactory = graph.getValueFactory();
		super.addTriples(graph, request, context, uriBuilder);

		final java.net.URI contextJavaUri = uriBuilder.build();
		final URI contextUri = valueFactory
				.createURI(contextJavaUri.toString());

		// final URI sameAs = valueFactory.createURI(
		// "http://www.w3.org/2002/07/owl#", "sameAs");
		// final URI rdfType = valueFactory.createURI(
		// "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
		setNamespace("role", "http://purl.org/role/terms/");
		setNamespace("widget", "http://purl.org/role/widget/");
		setNamespace("foaf", "http://xmlns.com/foaf/0.1/");
		setNamespace("dcterms", "http://purl.org/dc/terms/");
		setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		final URI securityTokenPredicate = valueFactory.createURI(
				"http://purl.org/role/widget/", "securityToken");
		final URI moduleIdPredicate = valueFactory.createURI(
				"http://purl.org/role/widget/", "moduleId");
		final URI preferencesPredicate = valueFactory.createURI(
				"http://purl.org/role/widget/", "preferences");
		final URI jidPredicate = valueFactory.createURI(
				"http://xmlns.com/foaf/0.1/", "jabberID");
		final URI externalPasswordPredicate = valueFactory.createURI(
				"http://purl.org/role/terms/", "externalPassword");
		final URI widgetPredicate = valueFactory.createURI(
				"http://purl.org/role/terms/", "widget");
		
		final URI deviceConfigPredicate = valueFactory.createURI("http://purl.org/role/terms/", "deviceConfig");
		final URI devicePredicate = valueFactory.createURI("http://purl.org/role/terms/", "device");
		final URI displayWidgetPredicate = valueFactory.createURI("http://purl.org/role/terms/", "displayWidget");
		final URI deviceProfilePredicate = valueFactory.createURI("http://purl.org/role/terms/", "deviceProfile");

		UUID agent = securityInfo.getAgent();
		String authUserUri = null;
		Concept authenticatedUser = null;
		if (agent != null) {
			authenticatedUser = store().require(agent);
			authUserUri = store().in(authenticatedUser).uri().toString();
		}
		
		// Jabber id
		if (context.getPredicate().equals(ROLETerms.space)) {
			String spaceId = context.getId();
			graph.add(
					contextUri,
					jidPredicate,
					valueFactory.createURI("xmpp:space-" + spaceId + "@"
							+ xmppMucServiceSubdomainNode + "." + xmppHost));
		} else if (context.getPredicate().equals(ROLETerms.member)) {
			String userId = context.getUuid().toString();
			graph.add(contextUri, jidPredicate,
					valueFactory.createURI("xmpp:" + userId + "@" + xmppHost));
			if (context.getUuid().equals(agent)) {
				Content externalPassword = store().in(context)
						.as(ROLETerms.externalPassword).get();
				if (externalPassword != null) {
					graph.add(
							contextUri,
							externalPasswordPredicate,
							valueFactory.createLiteral(store().as(
									externalPassword).string()));
				}
			}
		}

		final List<Concept> concepts = store.getConcepts(context.getUuid());
		for (Concept concept : concepts) {
			String id = concept.getId();
			if (id == null || id.contains(":") || id.contains("/")) {
				id = Base64UUID.encode(concept.getUuid());
			}
			String conceptUriString = uriBuilder.clone().path(id).build()
					.toString();
			URI conceptUri = valueFactory.createURI(conceptUriString);

			if (ROLETerms.tool.equals(concept.getPredicate())) {

				boolean isOpenSocialGadget = null != store.getControls(
						concept.getUuid(), ConserveTerms.type,
						ROLETerms.OpenSocialGadget);
				if (!isOpenSocialGadget) {
					continue;
				}

				// Module id
				long moduleId = Math.abs(concept.getUuid()
						.getMostSignificantBits()
						^ concept.getUuid().getLeastSignificantBits());
				graph.add(conceptUri, moduleIdPredicate,
						valueFactory.createLiteral(moduleId));

				// Widget URL
				String widgetUrl = getWidgetUrl(request, concept,
						conceptUriString, authUserUri);
				graph.add(conceptUri, widgetPredicate,
						valueFactory.createURI(widgetUrl));

				// Security token
				String securityToken = generateSecurityToken(
						authenticatedUser != null ? authenticatedUser : context,
						authenticatedUser != null ? authenticatedUser : context,
						widgetUrl,
						Math.abs(concept.getUuid().getMostSignificantBits()
								^ concept.getUuid().getLeastSignificantBits()),
						context, concept);
				graph.add(conceptUri, securityTokenPredicate,
						valueFactory.createLiteral(securityToken));

				// Preferences
				Concept toolAnnotations = store().in(securityInfo.getAgent())
						.sub(ConserveTerms.annotates)
						.get(store().in(concept).uri().toString());
				Concept preferencesConcept = null;
				if (toolAnnotations != null) {
					preferencesConcept = store().in(toolAnnotations)
							.sub(ROLETerms.preferences).get("preferences");
				}
				Content preferencesContent = null;
				if (preferencesConcept != null) {
					preferencesContent = store().in(preferencesConcept)
							.as(ConserveTerms.representation).get();
				}
				if (preferencesContent == null) {
					// User has no preferences--check the space instead
					preferencesConcept = store().in(concept)
							.sub(ROLETerms.preferences).get("preferences");
					if (preferencesConcept != null) {
						preferencesContent = store().in(preferencesConcept)
								.as(ConserveTerms.representation).get();
					}
				}
				if (preferencesContent != null) {
					graph.add(
							conceptUri,
							preferencesPredicate,
							valueFactory.createLiteral(store().as(
									preferencesContent).string()));
				}

			} else if (ROLETerms.member.equals(concept.getPredicate())) {

				List<Control> references = store().in(concept).get(
						ConserveTerms.reference);
				if (!references.isEmpty()) {
					// Jabber id
					URI conceptUserRef = valueFactory.createURI(store()
							.control(references.get(0)).uri().toString());
					String userId = references.get(0).getObject().toString();
					graph.add(
							conceptUserRef,
							jidPredicate,
							valueFactory.createURI("xmpp:" + userId + "@"
									+ xmppHost));
					if (securityInfo.getAgent()!= null && securityInfo.getAgent().equals(references.get(0).getObject())){
						log.info("The member concept for the user is found!!");
						//get the list of device config
						List<Concept> dcl = store().in(concept).sub(ROLETerms.dc).list();
						List<UUID> devices = getDeviceList(securityInfo.getAgent());
						for (Concept deviceConfig: dcl){
							//use the device config uri as uri ref
							URI conceptDeviceCfgRef = valueFactory.createURI(store().in(deviceConfig).uri().toString());
							//get the control that points to the device
							List<Control> dvs = store().in(deviceConfig).get(ConserveTerms.reference);
							if (dvs != null && dvs.iterator().hasNext()){
								Control dv = dvs.get(0);
								//get the concept of the device
								Concept ccpt = store().get(dv.getObject());
								//if exists, i.e. the device has not been deleted
								if (ccpt != null){
									devices.remove(ccpt.getUuid());
									graph.add(conceptDeviceCfgRef, devicePredicate, 
											//get the device uri and fill it into the graph
											valueFactory.createURI(store().in(ccpt).uri().toString()));
								//or has been deleted and this device config must not exist either
								}
								else{
									store.deleteConcept(deviceConfig);
									continue;
								}
								//get the list of displayed widget
								List<String> dwList = getDisplayWidgets(deviceConfig);
								for (String wUri: dwList){
									//get the widget uri
									URI widgetUri = valueFactory.createURI(wUri);
									//fill it into the graph
									graph.add(conceptDeviceCfgRef, displayWidgetPredicate, widgetUri);
								}
							}
							else
								store.deleteConcept(deviceConfig);
							//create the list of references for device configs in the user profile
							graph.add(conceptUserRef, deviceConfigPredicate, conceptDeviceCfgRef);
						}
						for (UUID dUuid: devices){
							Concept d = store.getConcept(securityInfo.getAgent(), dUuid);
							Concept dc = store().in(concept).sub(ROLETerms.dc).acquire(d.getId());
							store().in(dc).put(ConserveTerms.reference, dUuid);
						}
					}
				}

			}
			else if (concept.getPredicate().equals(ROLETerms.device)){
				Content deviceProfile = store().in(concept).as(ROLETerms.dp).get();
				String content = null;
				if (deviceProfile != null){
					content = store().as(deviceProfile).string();
					graph.add(conceptUri, deviceProfilePredicate, valueFactory.createLiteral(content));
				}
			}
		}
	}

	private String getWidgetUrl(Request request, Concept resource,
			String resourceUri, String authUserUri) {
		boolean isOpenAppFeatureEnabled = false;
		List<Control> references = store.getControls(resource.getUuid(),
				ConserveTerms.reference);
		final String refUri = references.get(0).getUri();
		try {
			GadgetContext gadgetContext = new GadgetContext() {
				@Override
				public Uri getUrl() {
					return Uri.parse(refUri);
				}
			};
			// Gadget gadget = processor.process(gadgetContext);
			GadgetSpec spec = gadgetSpecFactory.getGadgetSpec(gadgetContext);
			ModulePrefs modulePrefs = spec.getModulePrefs();
			isOpenAppFeatureEnabled = modulePrefs.getFeatures().containsKey(
					"openapp");
		} catch (GadgetException e) {
			// Assume not openapp-enabled
		}

		if (isOpenAppFeatureEnabled) {
			UriBuilder url = UriBuilder.fromPath(resourceUri);
			url.queryParam("openapp.ns.role", "http://purl.org/role/terms/");
			// if (resourceUri != null) {
			url.queryParam("openapp.role.space",
					store().in(resource.getContext()).uri());
			// }
			if (authUserUri != null) {
				url.queryParam("openapp.role.user", authUserUri);
			}
			url.queryParam("openapp.role.spaceService", ((RequestImpl) request)
					.getUriInfo().getBaseUriBuilder().path("users").build()
					.toString());
			url.queryParam("openapp.role.userService", ((RequestImpl) request)
					.getUriInfo().getBaseUriBuilder().path("users").build()
					.toString());
			return url.build().toString();
		} else {
			// If the gadget does not specify the OpenApp feature, then
			// then let Shindig have the original gadget XML URL rather than a
			// proxied URL (which breaks non-compatible gadgets)
			return refUri;
		}
	}

	public String generateSecurityToken(Concept owner, Concept viewer,
			String gadgetUrl, long moduleId, Concept space, Concept tool) {
		BlobCrypterSecurityToken st = new BlobCrypterSecurityToken(crypter,
				"default", "localhost");
		st.setOwnerId(owner != null ? Base64UUID.encode(owner.getUuid()) : null);
		st.setViewerId(viewer != null ? Base64UUID.encode(viewer.getUuid())
				: null);
		// st.setOwnerId("john.doe");
		// st.setViewerId("john.doe");
		st.setModuleId(moduleId);
		st.setAppUrl(gadgetUrl);
		JSONObject trustedJson = new JSONObject();
		try {
			trustedJson.put("space", Base64UUID.encode(space.getUuid()));
			trustedJson.put("tool", Base64UUID.encode(tool.getUuid()));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		st.setTrustedJson(trustedJson.toString());
		String token = null;
		try {
			token = Utf8UrlCoder.encode(st.encrypt());
		} catch (BlobCrypterException e) {
			log.error("Error creating security token", e);
		}
		return token;
	}
/*
	@Override
	public Object doGet(Request request) {
		String str = store().in(request.getContext()).uri().toString();
		log.info("doGet: " + str);
		String preDv = null;
		String preUsr = null;
		if (request instanceof RequestImpl 
				&& request.getRelation().getUuid().equals(ConserveTerms.system)
				&& request.getContext().getPredicate().equals(userPredicateUuid)) {
			
			HttpHeaders httpHeaders = ((RequestImpl) request).getRequestHeaders();
			Map<String, Cookie> cookies = httpHeaders.getCookies();
			if (cookies.containsKey("conserve_session")) {
				Cookie sessionCookie = cookies.get("conserve_session");
				String sessionCookieValue = sessionCookie.getValue();
				Concept session = store.getConcept(sessionContext, sessionCookieValue);
				if (session != null) {					
					Content previousDevice = store().in(session).as(ROLETerms.pd).get();
					Content previousUser = store().in(session).as(ROLETerms.pu).get();
					if (previousDevice != null && previousUser != null){
						preDv = store().as(previousDevice).string();
						preDv = preDv.equals("")?null:preDv;
						preUsr = store().as(previousUser).string();
						preUsr = preUsr.equals("")?null:preUsr;
					}
					if (cookies.containsKey("device") && cookies.containsKey("duiUser")){
						String clientCookie = cookies.get("device").getValue();
						String clientCookieUsr = cookies.get("duiUser").getValue();
						if (!clientCookie.equals("") && !clientCookieUsr.equals("")){
							preDv = cookies.get("device").getValue();
							preUsr = cookies.get("duiUser").getValue();
							store().in(session).as(ROLETerms.pd).type("text/plain").string(preDv);
							store().in(session).as(ROLETerms.pu).type("text/plain").string(preUsr);
						}
					}
				}
			}
		}
		
		if (preDv != null && preUsr != null){
			String host = ((RequestImpl)request).getUriInfo().getBaseUri().getHost();
			Response response = (Response)super.doGet(request);
			NewCookie cookie = new NewCookie("device", preDv, "/", host, "previous used device", 1200000, false);
			NewCookie cookieUsr = new NewCookie("duiUser", preUsr, "/", host, "previous user uri", 1200000, false);
			return Response.fromResponse(response).cookie(cookie, cookieUsr).build();
		}
		else
			return super.doGet(request);
	}
*/	
	private List<String> getDisplayWidgets(Concept deviceConfig){
		Content aw = store().in(deviceConfig).as(ROLETerms.aw).get();
		if (aw != null){
			String widgetsListJson = store().as(aw).string();
			Object obj;
			List<String> dwList = new LinkedList<String>();
			try {
				obj = parser.parse(widgetsListJson);
				JSONArray widgets = (JSONArray)obj;
				for (Object wUri: widgets){
					String widgetUri = (String)wUri;
					Concept widget = store().resolve(widgetUri);
					if (widget != null && ROLETerms.tool.equals(widget.getPredicate())){
						boolean isOpenSocialGadget = null != store.getControls(
								widget.getUuid(), ConserveTerms.type,
								ROLETerms.OpenSocialGadget);
						if (isOpenSocialGadget)
							dwList.add(widgetUri);
					}
				}
				store().as(aw).string(JSONValue.toJSONString(dwList));
				return dwList;
			} catch (ParseException e) {
				e.printStackTrace();
				return dwList;
			}
		}
		else{
			LinkedList<String> awl = new LinkedList<String>();
			return awl;
		}
	}
	
	private List<UUID> getDeviceList(UUID user){
		List<Concept> concepts = store.getConcepts(user);
		List<UUID> devices = new ArrayList<UUID>();
		for (Concept c: concepts){
			if (c.getPredicate().equals(ROLETerms.device))
				devices.add(c.getUuid());
		}
		return devices;
	}

}