package eu.role_project.service.dui.iface;

import eu.role_project.service.realtime.iwc.Intent;

public interface XmppProxy {
	/**
	 * Publish the Intent object to the space xmpp node
	 * @param intent
	 * @param spaceId
	 */
	public void publishToSpace(Intent intent, String spaceId);
	/**
	 * Publish the Intent object to the user xmpp node
	 * @param intent
	 * @param userId
	 */
	public void publishToUser(Intent intent, String userId);
}
