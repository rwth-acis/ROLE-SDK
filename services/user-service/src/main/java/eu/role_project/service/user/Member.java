package eu.role_project.service.user;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.logic.ResourceResponder;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

public class Member extends ResourceResponder {

	private static Logger log = LoggerFactory.getLogger(Member.class);

	@Inject
	private SecurityInfo securityInfo;

	@Override
	public void hit(Request request) {
		Concept user = request.getContext();
//		if (user.getUuid().equals(securityInfo.getAgent())) {
//			request.mapResponder(ConserveTerms.representation, ROLETerms.ple);
			request.mapResponder(ConserveTerms.system,
					ROLETerms.spaceSystemData);
//		} else {
			request.mapResponder(ConserveTerms.representation,
					ROLETerms.ple);
//		}

		List<MediaType> acceptTypes = ((RequestImpl) request)
				.getRequestHeaders().getAcceptableMediaTypes();
		MediaType xrdsType = MediaType.valueOf("application/xrds+xml");
		for (MediaType acceptType : acceptTypes) {
			if (xrdsType.isCompatible(acceptType)
					&& !acceptType.isWildcardType()
					&& !acceptType.isWildcardSubtype()) {
				log.info("Setting the representation to be the XRDS document");
				request.mapResponder(ConserveTerms.representation,
						ConserveTerms.openid);
			}
		}
	}

}