package eu.role_project.service.space;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
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

		UUID agent = securityInfo.getAgent();
		String authUserUri = null;
		Concept authenticatedUser = null;
		if (agent != null) {
			authenticatedUser = store().require(agent);
			authUserUri = store().in(authenticatedUser).uri().toString();
		}

		// Jabber id
		if (context.getPredicate().equals(ROLETerms.space)) {
			String spaceId = context.getUuid().toString();
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

}