package eu.role_project.service.dui;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractGuard;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

public class DuiGuard extends AbstractGuard {
	private static Logger log = LoggerFactory.getLogger(DuiGuard.class);
	
	@Inject
	private SecurityInfo securityInfo;
	
	public boolean canGet(Request request) {
		if (securityInfo.getAgent() != null){
			log.info("get allowed");
			return true;
		}
		return false;
	}

	public boolean canPut(Request request) {
		if (securityInfo.getAgent() != null){
			log.info("put allowed");
			return true;
		}
		return false;
	}

	public boolean canPost(Request request) {
		if (securityInfo.getAgent() != null){
			log.info("post allowed");
			return true;
		}
		return false;
	}

	public boolean canDelete(Request request) {
		if (securityInfo.getAgent() != null){
			log.info("delete allowed");
			return true;
		}
		return false;
	}

}
