package eu.role_project.service.user;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractGuard;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

public class UserGuard extends AbstractGuard {

	private static Logger log = LoggerFactory.getLogger(UserGuard.class);

	@Inject
	private SecurityInfo securityInfo;

	private final Set<UUID> allowedUserRelations;

	public UserGuard() {
		Set<UUID> relations = new HashSet<UUID>();
		relations.add(ROLETerms.tool);
		relations.add(ROLETerms.data);
		relations.add(ROLETerms.activity);
		relations.add(ConserveTerms.annotates);
		allowedUserRelations = Collections.unmodifiableSet(relations);
	}

	public boolean canGet(Request request) {
		log.info("GET always allowed");
		return true;
	}

	public boolean canPut(Request request) {
		Concept context = request.getContext();
		Concept userService = request.getGuardContext();
		Concept user = getuser(context, userService);

		if (user == null) {
			log.info("PUT denied: may not set content for the service context");
			return false;
		}

		Concept relation = request.getRelation();
		if (relation != null
				&& relation.getUuid().equals(ConserveTerms.metadata)) {
			log.info("Setting metadata of any resource is allowed");
		} else if (ROLETerms.data.equals(context.getPredicate())
				&& relation != null
				&& relation.getUuid().equals(ConserveTerms.representation)) {
			log.info("Setting representation of data is allowed");
		} else if (ROLETerms.tool.equals(context.getPredicate())
				&& relation != null
				&& relation.getUuid().equals(ROLETerms.preferences)) {
			log.info("Setting preferences is allowed for tools");
		} else if (ConserveTerms.annotates.equals(context.getPredicate())
				&& relation != null
				&& relation.getUuid().equals(ROLETerms.preferences)) {
			log.info("Setting preferences is allowed for annotations");
		} else {
			log.info("PUT denied: tried to set content that may not be modified");
			return false;
		}

		if (!isAuthUser(user)) {
			log.info("PUT forbidden for this agent");
			return false;
		} else {
			log.info("PUT permitted");
			return true;
		}
	}

	public boolean canPost(Request request) {
		Concept context = request.getContext();
		Concept userService = request.getGuardContext();
		Concept user = getuser(context, userService);
		Concept relation = request.getRelation();

		if (user == null && context.getUuid().equals(userService.getUuid())) {
			log.info("Creating users is not allowed");
			return false;
		} else if (context.getUuid().equals(user.getUuid())) {
			if (relation == null
					|| !allowedUserRelations.contains(relation.getUuid())) {
				log.info("POST denied: not an allowed user relation");
				return false;
			}
		} else {
			if (!(ROLETerms.data.equals(context.getPredicate())
					&& relation != null && ROLETerms.data.equals(relation
					.getUuid()))) {
				log.info("POST denied: not an allowed user relation");
				return false;
			}
		}

		if (!isAuthUser(user)) {
			log.info("POST forbidden for this agent");
			return false;
		} else {
			log.info("POST permitted");
			return true;
		}
	}

	public boolean canDelete(Request request) {
		Concept context = request.getContext();
		Concept userService = request.getGuardContext();
		Concept user = getuser(context, userService);
		Concept relation = request.getRelation();

		if (user == null) {
			log.info("DELETE denied: tried to delete the service context");
			return false;
		}

		if (relation != null) {
			if (relation != null
					&& relation.getUuid().equals(ConserveTerms.metadata)) {
				log.info("Deleting metadata of any resource is allowed");
			} else if (ROLETerms.data.equals(context.getPredicate())
					&& relation != null
					&& relation.getUuid().equals(ConserveTerms.representation)) {
				log.info("Deleting representation of data is allowed");
			} else {
				log.info("DELETE denied: tried to delete content that may not be modified");
				return false;
			}
		} else if (context.getUuid().equals(user.getUuid())) {
			log.info("Deleting users is allowed");
		} else {
			if (!allowedUserRelations.contains(context.getPredicate())) {
				log.info("DELETE denied: not an allowed user relation, and only resources with allowed relations can be deleted");
				return false;
			}
		}

		if (!isAuthUser(user)) {
			log.info("DELETE forbidden for this agent");
			return false;
		} else {
			log.info("DELETE permitted");
			return true;
		}
	}

	private Concept getuser(Concept context, Concept userService) {
		if (context.getUuid().equals(userService.getUuid())) {
			return null;
		}
		Concept user = context;
		while (!userService.getUuid().equals(user.getContext())) {
			user = store.getConcept(user.getContext());
		}
		return user;
	}

	private boolean isAuthUser(Concept user) {
		UUID agent = securityInfo.getAgent();
		if (agent == null) {
			return false;
		}
		return agent.equals(user.getUuid());
	}

}