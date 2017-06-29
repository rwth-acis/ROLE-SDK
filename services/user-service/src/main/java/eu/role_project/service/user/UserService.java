package eu.role_project.service.user;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.logic.ResourceResponder;

public class UserService extends ResourceResponder {

	private static Logger log = LoggerFactory.getLogger(UserService.class);

	public static final UUID ID = UUID
			.fromString("237b34eb-b71b-4b69-a68b-c97249f759f6");
	
	public static final UUID providerId = UUID.fromString("911a07a5-f5dd-403d-9b74-0d4921c19fda");
	
	@Override
	public void initialize(Request request) {
		// Initialize the "users" context
		log.info("Initializing /users");
		
		if(store()
				.in(app.getRootUuid(request))
				.sub(ROLETerms.userService)
				.get("people") == null){
		Concept users = store().in(app.getRootUuid(request))
				.sub(ROLETerms.userService).acquire(ID, "people");
		Concept provider = store().in(app.getRootUuid(request)).sub().acquire(providerId,"provider");
		Concept secretAuth = store().in(users)
				.sub(ConserveTerms.authentication)
				.acquire("urn:uuid:f8ef80e1-dcd2-4e07-94d8-deda60e8366a");
		store().in(secretAuth).put(ConserveTerms.type,
				"http://kmr.csc.kth.se/rdf/conserve/auth/Secret");

		Concept googleAuth = store().in(users)
				.sub(ConserveTerms.authentication)
				.acquire("urn:uuid:c0a211bd-bbad-43ba-ae30-f6427d1c55bb");
		store().in(googleAuth).put(ConserveTerms.type,
				"http://kmr.csc.kth.se/rdf/conserve/auth/OpenID");
		store().in(googleAuth).put(ConserveTerms.reference,
				"https://www.google.com/accounts/o8/id");
		}
	}

}