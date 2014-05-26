package eu.role_project.service.resource;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.logic.ResourceResponder;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

public class Home extends ResourceResponder {

	private static Logger log = LoggerFactory.getLogger(Home.class);

	@Inject
	private Injector injector;

	@Override
	public Resolution resolve(Request request) {
		if (request.getId().getPath().equals("user")) {
			log.info("Special user context");
			SecurityInfo securityInfo = injector
					.getInstance(SecurityInfo.class);
			UUID agent = securityInfo.getAgent();
			log.info("Agent found: " + agent);
			Concept user;
			if (agent != null) {
				user = store().require(agent);
			} else {
				// Default user (currently created in TestInitializer)
				user = store()
						.require(
								UUID.fromString("e4008a52-cf77-4e58-953a-65dc2d9c567f"));
			}
			log.info("User found: " + user);
			return new Resolution(Resolution.StandardType.CONTEXT, user);
		} else {
			return super.resolve(request);
		}
	}

}