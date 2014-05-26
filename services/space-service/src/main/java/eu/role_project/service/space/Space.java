package eu.role_project.service.space;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.name.Named;

import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.internal.RequestNotifier;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.logic.ResourceResponder;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

public class Space extends ResourceResponder {

	private static Logger log = LoggerFactory.getLogger(Space.class);

	@Inject
	private SecurityInfo securityInfo;

	//LTI included stuff
	@Inject
	@Named("conserve.user.context")
	private UUID userContext;

	@Inject
	@Named("conserve.user.predicate")
	private UUID userPredicate;
	
	@Inject
	@Named("conserve.session.context")
	private UUID sessionContext;
	
	private static final SecureRandom RAND = new SecureRandom();

	private static String randomString() {
		byte[] secret = new byte[16];
		RAND.nextBytes(secret);
		return Base64.encodeBase64URLSafeString(secret);
	}
	//End LTI included stuff
	
	@Inject
	private RequestNotifier requestNotifier;

	@Override
	public void hit(Request request) {
		request.mapResponder(ConserveTerms.representation, ConserveTerms.app);
		request.mapResponder(ConserveTerms.system, ROLETerms.spaceSystemData);
	}

	@Override
	public Object doPost(Request request, byte[] data) {
		
		MultivaluedMap<String, String> formMap = request.getFormMap();
		if (formMap != null && formMap.getFirst("lti_version") != null && System.getProperty("lti.enabled", "false").equals("true")) {
			return serveLTI(request, formMap);
		}
		
		UUID agent = securityInfo.getAgent();
		if (agent == null) {
			return Response.status(Status.FORBIDDEN).build();
		}

		Object response = super.doPost(request, data);
		Concept resource = request.getCreated();
		if (resource != null) {
			log.info("Created space resource: " + resource.getUuid());

			Concept owner = store().in(resource).sub(ConserveTerms.owner)
					.create();
			store().in(owner).put(ConserveTerms.reference, agent);

			Concept member = store().in(resource).sub(ROLETerms.member)
					.create();
			store().in(member).put(ConserveTerms.reference, agent);

			String spaceTitle = ((RequestImpl) request).getLinkRelations().get(
					"http://purl.org/dc/terms/title");
			Graph graph = new GraphImpl();
			ValueFactory valueFactory = graph.getValueFactory();
			if (spaceTitle == null) {
				spaceTitle = resource.getId();
			}
			graph.add(valueFactory.createStatement(valueFactory
					.createURI(store().in(resource).uri().toString()),
					valueFactory
							.createURI("http://purl.org/dc/terms/", "title"),
					valueFactory.createLiteral(spaceTitle)));
			store().in(resource).as(ConserveTerms.metadata)
					.type("application/json").graph(graph);

			requestNotifier.setResolution(Resolution.StandardType.CONTEXT,
					resource);
			requestNotifier.setResolution(Resolution.StandardType.CREATED,
					member);
			requestNotifier.doPost();

			String label = ((RequestImpl) request).getLinkRelations().get(
					"http://www.w3.org/2000/01/rdf-schema#label");
			if (label != null && label.length() > 0) {
				return Response
						.seeOther(
								((RequestImpl) request).getUriInfo()
										.getBaseUriBuilder().path("spaces")
										.path(label).build())
						.header("Cache-Control", "no-store").build();

			} else {
				return Response.seeOther(store().in(resource).uri())
						.header("Cache-Control", "no-store").build();
			}
		}
		return response;
	}
	
	private Object serveLTI(Request request, MultivaluedMap<String, String> formMap) {		
		String realm = formMap.getFirst("tool_consumer_instance_guid");
		String userId = formMap.getFirst("user_id");
		String userName = userId + "_" + realm;
		String givenName = formMap.getFirst("lis_person_name_given");
		String familyName = formMap.getFirst("lis_person_name_family");
		String email = formMap.getFirst("lis_person_contact_email_primary");
		
		Concept user = store().in(userContext).sub().get(userName);
		
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
					valueFactory.createLiteral(givenName
							+ " " + familyName)));
			graph.add(valueFactory.createStatement(
					userUri,
					valueFactory
							.createURI("http://xmlns.com/foaf/0.1/mbox"),
					valueFactory.createURI("mailto:"
							+ email)));
			store().in(user).as(ConserveTerms.metadata)
					.type("application/json").graph(graph);

			requestNotifier.setResolution(
					Resolution.StandardType.CONTEXT,
					store.getConcept(userContext));
			requestNotifier.setResolution(
					Resolution.StandardType.CREATED, user);
			requestNotifier.doPost();
		}
		
		securityInfo.setAgent(user.getUuid());

		//Membership of the current space.
		Concept space = request.getContext();
		List<Concept> members = store().in(space).sub(ROLETerms.member).list();
		boolean isMember = false;
		for (Concept member : members) {
			List<Control> memberReferences = store.getControls(
						member.getUuid(), ConserveTerms.reference);
				if (memberReferences != null
							&& memberReferences.size() != 0
							&& memberReferences.get(0).getObject().equals(user.getUuid())) {
					isMember = true;
					break;
				}
		}
		if (!isMember) {
			Concept memberConcept = store().in(space).sub(ROLETerms.member).create();
			store().in(memberConcept).put(ConserveTerms.reference, user.getUuid());			
			requestNotifier.setResolution(Resolution.StandardType.CONTEXT, space);
			requestNotifier.setResolution(Resolution.StandardType.CREATED, memberConcept);
			requestNotifier.doPost();
		}

		//Ownership of the current space
		String roles = formMap.getFirst("roles");
		if (roles != null && roles.indexOf("lti:sysrole:ims/lis/Administrator") >= 0) {
			List<Concept> owners = store().in(space).sub(ConserveTerms.owner).list();
			boolean isOwner = false;
			for (Concept owner : owners) {
				List<Control> ownerReferences = store.getControls(
							owner.getUuid(), ConserveTerms.reference);
					if (ownerReferences != null
								&& ownerReferences.size() != 0
								&& ownerReferences.get(0).getObject().equals(user.getUuid())) {
						isOwner = true;
						break;
					}
			}
			if (!isOwner) {
				Concept ownerConcept = store().in(space).sub(ConserveTerms.owner).create();
				store().in(ownerConcept).put(ConserveTerms.reference, user.getUuid());			
				requestNotifier.setResolution(Resolution.StandardType.CONTEXT, space);
				requestNotifier.setResolution(Resolution.StandardType.CREATED, ownerConcept);
				requestNotifier.doPost();
			}
		}
		
		//Render the space.
		Response response = (Response) doGet(request);

		//Check if we need to create a session
		Concept session = getSessionForUser(user, (RequestImpl) request);
		if (session == null) {
			session = store().in(sessionContext).sub().create(randomString());
			store().in(session).put(ConserveTerms.reference,user.getUuid());

			UriInfo uriInfo = ((RequestImpl) request).getUriInfo();
			NewCookie cookie = new NewCookie(
				"conserve_session", session.getId(), "/",
				uriInfo.getBaseUri().getHost(),
				"conserve session id", 1200000, false);
			
			//Make a new response containing the cookie.
			response = Response.ok().cookie(cookie).header("Content-Type", "text/html; charset=UTF-8")
				.header("Cache-Control", "no-store").entity(response.getEntity()).build();
		}

		return response;
	}
	
	public Concept getSessionForUser(Concept user, RequestImpl request) {
		Map<String, Cookie> cookies = request.getRequestHeaders().getCookies();
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
					if (sessionReferences != null
								&& sessionReferences.size() != 0) {
						if (sessionReferences.get(0).getObject().equals(user.getUuid())) {
							return session;
						}
						for (Control sessionReference : sessionReferences) {
							store.deleteControl(sessionReference);
						}
					}
					store.deleteConcept(store.getConcept(sessionContext,
									sessionUuid));
					log.info("Session deleted");
				}
			} catch (IllegalArgumentException e) {
				// Move on...
			}
		}
		return null;
	}
}