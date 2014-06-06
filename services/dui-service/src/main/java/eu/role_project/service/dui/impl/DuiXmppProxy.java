package eu.role_project.service.dui.impl;

import javax.inject.Inject;

import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.dui.iface.XmppProxy;
import eu.role_project.service.realtime.RealtimeModule;
import eu.role_project.service.realtime.iwc.Intent;

public class DuiXmppProxy implements XmppProxy {
	
	private static Logger log = LoggerFactory.getLogger(DuiXmppProxy.class);
	@Inject
	private RealtimeModule realtimeModule;

	public void publishToSpace(Intent intent, String spaceId) {
		realtimeModule.publishIntentToSpace(spaceId, intent);
		log.info("published an intent to the node for the space " + spaceId);
	}
	
	public void publishToUser(Intent intent, String uid){
		realtimeModule.publishIntentToUser(uid, intent);
		log.info("published an intent to the node of the user " + uid);
	}
}
