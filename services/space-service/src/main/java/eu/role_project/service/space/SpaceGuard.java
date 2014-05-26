package eu.role_project.service.space;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractGuard;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

public class SpaceGuard extends AbstractGuard {

	private static Logger log = LoggerFactory.getLogger(SpaceGuard.class);

	@Inject
	private SecurityInfo securityInfo;

	private final Set<UUID> allowedSpaceRelations;

	public SpaceGuard() {
		Set<UUID> relations = new HashSet<UUID>();
		relations.add(ROLETerms.member);
		relations.add(ROLETerms.tool);
		relations.add(ROLETerms.data);
		relations.add(ROLETerms.activity);
		allowedSpaceRelations = Collections.unmodifiableSet(relations);
	}

	public boolean canGet(Request request) {
		log.info("GET always allowed");
		return true;
	}

	public boolean canPut(Request request) {
		Concept context = request.getContext();
		Concept spaceService = request.getGuardContext();
		Concept space = getSpace(context, spaceService);

		if (space == null) {
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
			log.info("Setting representation of preferences is allowed for tools");
		} else {
			log.info("PUT denied: tried to set content that may not be modified");
			return false;
		}

		if (!isOwner(space) && !isMember(space)) {
			log.info("PUT forbidden for this agent");
			return false;
		} else {
			log.info("PUT permitted");
			return true;
		}
	}

	public boolean canPost(Request request) {
		Concept context = request.getContext();
		Concept spaceService = request.getGuardContext();
		Concept space = getSpace(context, spaceService);
		Concept relation = request.getRelation();

		MultivaluedMap<String, String> formMap = request.getFormMap();
		if (formMap != null && formMap.getFirst("lti_version") != null && System.getProperty("lti.enabled", "false").equals("true")) {
			return true;
		}
		
		if (space == null && context.getUuid().equals(spaceService.getUuid())) {
			log.info("Creating spaces is allowed");
		} else if (context.getUuid().equals(space.getUuid())) {
			if (relation == null
					|| !allowedSpaceRelations.contains(relation.getUuid())) {
				log.info("POST denied: not an allowed space relation");
				return false;
			}
		} else {
			if (!((ROLETerms.data.equals(context.getPredicate()) || ROLETerms.activity
					.equals(context.getPredicate())) && relation != null && ROLETerms.data
						.equals(relation.getUuid()))) {
				log.info("POST denied: not an allowed space relation");
				return false;
			}
		}

		if (space != null) {
			boolean isOwner = isOwner(space);
			boolean isMember = isMember(space);
			if (securityInfo.getAgent() == null
					|| (!isOwner && !isMember && !ROLETerms.member
							.equals(relation.getUuid()))
					|| (isMember && ROLETerms.member.equals(relation.getUuid()))) {
				log.info("POST forbidden for this agent");
				return false;
			} else {
				log.info("POST permitted");
				return true;
			}
		} else {
			if (securityInfo.getAgent() == null) {
				log.info("POST forbidden since user not authenticated");
				return false;
			} else {
				log.info("POST permitted: setting up space");
				return true;
			}
		}
	}

	public boolean canDelete(Request request) {
		Concept context = request.getContext();
		Concept spaceService = request.getGuardContext();
		Concept space = getSpace(context, spaceService);
		Concept relation = request.getRelation();

		if (space == null) {
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
		} else if (context.getUuid().equals(space.getUuid())) {
			log.info("Deleting spaces is allowed");
		} else {
			if (!allowedSpaceRelations.contains(context.getPredicate())) {
				log.info("DELETE denied: not an allowed space relation, and only resources with allowed relations can be deleted");
				return false;
			}
		}

		if (!isOwner(space)
				&& !(relation == null
						&& context.getPredicate().equals(ROLETerms.member)
						&& isMember(space) && context.getUuid().equals(
						securityInfo.getAgent()))) {
			log.info("DELETE forbidden for this agent");
			return false;
		} else {
			log.info("DELETE permitted");
			return true;
		}
	}

	private Concept getSpace(Concept context, Concept spaceService) {
		if (context.getUuid().equals(spaceService.getUuid())) {
			return null;
		}
		Concept space = context;
		while (!spaceService.getUuid().equals(space.getContext())) {
			space = store.getConcept(space.getContext());
		}
		return space;
	}

	private boolean isOwner(Concept space) {
		UUID agent = securityInfo.getAgent();
		if (agent == null) {
			return false;
		}
		// String agentUri =
		// store().in(store.getConcept(agent)).uri().toString();
		for (Concept sub : store.getConcepts(space.getUuid())) {
			if (ConserveTerms.owner.equals(sub.getPredicate())) {
				log.info("Found owner");
				List<Control> references = store.getControls(sub.getUuid(),
						ConserveTerms.reference);
				if (references == null || references.size() == 0) {
					continue;
				}
				if (agent.equals(references.get(0).getObject())) {
					return true;
				}
			}
		}
		
		// TODO: For now, allow members to act as owners, until our implementation is more complete
		return isMember(space);
		//return false;
	}

	private boolean isMember(Concept space) {
		UUID agent = securityInfo.getAgent();
		if (agent == null) {
			return false;
		}
		// String agentUri =
		// store().in(store.getConcept(agent)).uri().toString();
		for (Concept sub : store.getConcepts(space.getUuid())) {
			if (ROLETerms.member.equals(sub.getPredicate())) {
				log.info("Found member");
				List<Control> references = store.getControls(sub.getUuid(),
						ConserveTerms.reference);
				if (references == null || references.size() == 0) {
					continue;
				}
				if (agent.equals(references.get(0).getObject())) {
					return true;
				}
			}
		}
		return false;
	}

}