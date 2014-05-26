package eu.role_project.service.realtime;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.NodeType;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubElementType;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.packet.PubSub;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;
import org.jivesoftware.smackx.pubsub.packet.SyncPacketSend;
import org.jivesoftware.smackx.search.UserSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Listener;
import se.kth.csc.kmr.conserve.core.Contapp;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

import eu.role_project.service.realtime.iwc.Intent;
import eu.role_project.service.realtime.xmpp.OwnerAffiliation;
import eu.role_project.service.realtime.xmpp.OwnerAffiliationsExtension;
import eu.role_project.service.resource.ROLETerms;

/**
 * @author Dominik Renzel (renzel@dbis.rwth-aachen.de)
 * 
 *         The Realtime module provides management support for various real-time
 *         features of spaces. The core of this module consists of a connection
 *         to an XMPP server hosting two services we make use of:
 *         <ul>
 *         <li>Multi-User Chat Rooms - space-centered chat rooms</li>
 *         <li>Publish-Subscribe Nodes - space-centered channels for
 *         broadcasting events</li>
 *         </ul>
 * 
 *         This implementation has been tested successfully with ejabberd (v2.1.8).
 */
public class RealtimeModule extends AbstractModule {

	// default values for XMPP connection parameters, if none provided as system
	// properties
	public static final String DEFAULT_XMPP_HOST = ""; // Empty string implies that XMPP should not be used
	public static final int DEFAULT_XMPP_PORT = 5222;
	public static final String DEFAULT_XMPP_USER = "role-realtime-service";
	public static final String DEFAULT_XMPP_PASS = "807343V3R";
	public static final String DEFAULT_XMPP_MUC_SUBDOMAINPREF = "conference";

	private static Logger log = LoggerFactory.getLogger(RealtimeModule.class);

	// XMPP connection and pubsub manager
	private Connection xc;
	private PubSubManager pubsub;

	// XMPP connection parameters
	private String xmppHost;
	private int xmppPort;
	private String xmppMucServiceSubdomainNode;
	private String xmppUser;
	private String xmppPass;

	@Override
	protected void configure() {
		log.info("Configuring Real-time module");

		MapBinder<UUID, Listener> listeners = Contapp
				.newListenerBinder(binder());
		bind(RealtimeModule.class).toInstance(this);
		listeners.addBinding(
				UUID.fromString("237b34eb-b71b-4b69-a68b-c97249f759f6")).to(
				UserListener.class); // UUID from user-service's
										// UserService.java
		listeners.addBinding(
				UUID.fromString("4cbde6b5-f7ea-4436-9a90-c14e5440d2ad")).to(
				SpaceListener.class); // UUID from space-service's
										// SpaceService.java
		listeners.addBinding(ROLETerms.member).to(SpaceListener.class);

		// Set XMPP server host name.
		// Use system property "xmpp.host", if available; else, use standard
		// value.
		if (System.getProperty("xmpp.host") != null) {
			xmppHost = System.getProperty("xmpp.host");
		} else {
			xmppHost = DEFAULT_XMPP_HOST;
		}
		bindConstant().annotatedWith(Names.named("xmpp.host")).to(xmppHost);

		// Set XMPP server port.
		// Use system property "xmpp.mucs", if available and can be parsed as
		// int; else, use standard value.
		if (System.getProperty("xmpp.port") != null) {
			try {
				xmppPort = Integer.parseInt(System.getProperty("xmpp.port"));
			} catch (NumberFormatException e) {
				xmppPort = DEFAULT_XMPP_PORT;
			}
		} else {
			xmppPort = DEFAULT_XMPP_PORT;
		}

		// Set XMPP user name used by the service.
		// Use system property "xmpp.user", if available; else, use standard
		// value.
		if (System.getProperty("xmpp.user") != null) {
			xmppUser = System.getProperty("xmpp.user");
		} else {
			xmppUser = DEFAULT_XMPP_USER;
		}

		// Set XMPP password used by the service.
		// Use system property "xmpp.pass", if available; else, use standard
		// value.
		if (System.getProperty("xmpp.pass") != null) {
			xmppPass = System.getProperty("xmpp.pass");
		} else {
			xmppPass = DEFAULT_XMPP_PASS;
		}

		// Set XMPP Multiuser Chat Service subdomain
		// Use system property "xmpp.mucs", if available; else, use standard
		// value
		if (System.getProperty("xmpp.mucs") != null) {
			xmppMucServiceSubdomainNode = System.getProperty("xmpp.mucs");
		} else {
			xmppMucServiceSubdomainNode = DEFAULT_XMPP_MUC_SUBDOMAINPREF;
		}
		bindConstant().annotatedWith(Names.named("xmpp.mucs")).to(xmppMucServiceSubdomainNode);

		// If we don't have a proper XMPP configuration, then it is time to quit now
		if (!isConfigured()) {
			return;
		}

		try {
			connect();
			pubsub = new PubSubManager(xc);

		} catch (XMPPException e) {
			log.error(e.getMessage());
		}
	}

	public boolean isConfigured() {
		// If no XMPP host was specified, then assume that there is no XMPP server
		// to integrate with
		return xmppHost.length() > 0;
	}

	/**
	 * Connects and authenticates this service to its related XMPP server as
	 * part of the configuration process.
	 * 
	 * @throws XMPPException
	 *             in case connection or authentication fail.
	 */
	private void connect() throws XMPPException {

		// setup connection to XMPP server
		ConnectionConfiguration xcc = new ConnectionConfiguration(
				getXmppHost(), getXmppPort());
		xcc.setCompressionEnabled(true);
		xcc.setSASLAuthenticationEnabled(true);

		xc = new XMPPConnection(xcc);

		try {
			
			xc.connect();
			log.info("Connected to XMPP host " + getXmppHost() + ":" + getXmppPort());
		} catch (XMPPException e) {
			log.error("Connection to XMPP host " + getXmppHost() + ":" + getXmppPort() + " failed!");
			throw e;
		}

		// authenticate service at XMPP server
		try {
			xc.login(getXmppUser(), getXmppPass());
			log.info("Authenticated at XMPP host.");
		} catch (XMPPException e) {
			log.error("Authentication at XMPP host failed!");
			throw e;
		}
	}

	/**
	 * Returns the host name of the XMPP server to be used by this service.
	 * 
	 * @return String a host name
	 */
	private String getXmppHost() {
		return this.xmppHost;
	}

	/**
	 * Returns the port number of the XMPP server to be used by this service.
	 * 
	 * @return int a port number
	 */
	private int getXmppPort() {
		return this.xmppPort;
	}

	/**
	 * Returns the username for XMPP authentication used by this service.
	 * 
	 * @return String a username
	 */
	private String getXmppUser() {
		return xmppUser;
	}

	/**
	 * Returns the password for XMPP authentication used by this service.
	 * 
	 * @return String a password
	 */
	private String getXmppPass() {
		return xmppPass;
	}

	/**
	 * Returns the subdomain node identifier of the XMPP Multi-User chat service
	 * used by this service.
	 * 
	 * @return String an XMPP service subdomain node identifier
	 */
	private String getXmppMucServiceSubdomainNode() {
		return xmppMucServiceSubdomainNode;
	}

	/**
	 * Returns the Jabber ID (JID) of the XMPP Multi-User chat (MUC) service
	 * used by this service.
	 * 
	 * @return String a MUC service JID
	 */
	private String getXmppMucServiceJid() {
		return getXmppMucServiceSubdomainNode() + "." + getXmppHost();
	}

	/**
	 * Computes a Jabber ID (JID) for a given user identifier.
	 * 
	 * @param uid
	 *            String a user identifier
	 * @return String a user JID
	 */
	public String getUserJid(String uid) {
		return uid + "@" + getXmppHost();
	}

	/**
	 * Computes a Jabber ID (JID) of a chat room for a given space identifier.
	 * 
	 * @param sid
	 *            String a space identifier
	 * @return String a chat room JID
	 */
	public String getSpaceRoomJid(String sid) {
		return "space-" + sid + "@" + getXmppMucServiceJid();
	}

	/**
	 * Computes the PubSub node identifier for a given space identifier.
	 * 
	 * @param sid
	 *            String a space identifier
	 * @return
	 */
	public static String getSpaceNodeIdentifier(String sid) {
		return "space-" + sid;
	}

	/**
	 * Registers a new user via XMPP In-Band Registration (XEP-077).
	 * 
	 * @param username
	 *            String user name for new user
	 * @param password
	 *            String password for new user
	 * 
	 * @throws XMPPException
	 *             in case user already exists or connection problem occurred
	 * @throws IllegalArgumentException
	 *             in case user name or password are not specified or too short
	 */
	public void registerUser(String username, String password)
			throws XMPPException, IllegalArgumentException {

		// check parameter availability
		if (username.equals(null) || username.length() == 0) {
			throw new IllegalArgumentException(
					"Username not specified or too short!");
		}
		if (password.equals(null) || password.length() == 0) {
			throw new IllegalArgumentException(
					"Password not specified or too short!");
		}

		// setup anonymous connection to XMPP server for In-Band registration
		ConnectionConfiguration xcc = new ConnectionConfiguration(
				getXmppHost(), getXmppPort());
		xcc.setCompressionEnabled(true);
		Connection c = new XMPPConnection(xcc);

		try {
			c.connect();

			// request user registration form
			Form f = getUserRegistrationForm(c);

			// this case happens if the registration form returned from the
			// server is not compliant
			// with the jabber:x:data namespace for data forms, but still uses
			// the legacy mechanism.
			// This is for example the case for ejabberd 2.1.8.
			if (f == null) {
				Registration riq = new Registration();

				HashMap<String, String> attr = new HashMap<String, String>();
				attr.put("username", username);
				attr.put("password", password);
				riq.setAttributes(attr);
				riq.setType(IQ.Type.SET);
				
				sendUserRegistration(c, riq);
				log.info("User " + username + " registered at XMPP host.");
			}
			// this is the normal case, when Smack returns a registration form
			// compliant with the
			// jabber:x:data namespace for data forms.
			// This is for example the case for Openfire 3.6.4.
			else {

				// complete only mandatory parts of user registration process
				// TODO: do we need more than user name and password?
				f.getField("username").addValue(username);
				f.getField("password").addValue(password);

				// submit user registration form
				sendUserRegistrationForm(c, f);
				log.info("User " + username + " registered at XMPP host.");
			}
		} catch (XMPPException e) {
			throw e;
		} finally {
			// finally tear down anonymous XMPP connection
			c.disconnect();
		}
	}

	/**
	 * Requests a user registration form from the XMPP server. If invoked with
	 * an anonymous connection, an empty registration form is returned. (use
	 * case: register new user) If invoked with an authenticated connection a
	 * form pre-configured with the profile of the current user is returned.
	 * (use case: change own user profile)
	 * 
	 * @param c
	 *            Connection an XMPP connection
	 * @return Form a user registration form
	 * 
	 * @throws XMPPException
	 *             in case no user registration form could be retrieved or
	 *             connection problems occurred.
	 */
	private Form getUserRegistrationForm(Connection c) throws XMPPException {
		Registration r = new Registration();
		r.setType(IQ.Type.GET);
		r.setInstructions(null);

		PacketFilter responseFilter = new PacketIDFilter(r.getPacketID());
		PacketCollector response = c.createPacketCollector(responseFilter);

		c.sendPacket(r);

		IQ answer = (IQ) response.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		// Stop queuing results
		response.cancel();

		if (answer == null) {
			throw new XMPPException(
					"No user registration form retrieved from server!");
		} else if (answer.getError() != null) {
			throw new XMPPException(answer.getError());
		}
		return Form.getFormFrom(answer);
	}

	/**
	 * Sends a completed user registration form to complete the process of
	 * either changing the own user profile (using authenticated connection) or
	 * registering a new user (using an anonymous connection)
	 * 
	 * @param c
	 *            Connection an XMPP connection
	 * @param form
	 *            Form a completed user registration form
	 * 
	 * @throws XMPPException
	 *             in case form was incomplete, user already exists or
	 *             connection problem occurred.
	 */
	private void sendUserRegistrationForm(Connection c, Form form)
			throws XMPPException {
		Registration reg = new Registration();
		reg.setFrom(null);
		reg.setType(IQ.Type.SET);
		reg.addExtension(form.getDataFormToSend());

		sendUserRegistration(c, reg);
	}

	/**
	 * Sends a prepared registration stanza to complete user registration
	 * 
	 * @param c
	 *            Connection an XMPP connection
	 * @param reg
	 *            Registration an IQ stanza designed for registration
	 * @throws XMPPException
	 */
	private void sendUserRegistration(Connection c, Registration reg)
			throws XMPPException {
		PacketFilter filter = new AndFilter(new PacketIDFilter(
				reg.getPacketID()), new PacketTypeFilter(IQ.class));
		PacketCollector collector = c.createPacketCollector(filter);
		c.sendPacket(reg);
		IQ result = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		collector.cancel();
		if (result == null) {
			throw new XMPPException(
					"No user registration response from server!");
		} else if (result.getType() == IQ.Type.ERROR) {
			throw new XMPPException(result.getError());
		}
	}

	/**
	 * Destroys the pubsub node for a given space.
	 * 
	 * @param sid
	 *            String a space identifier
	 * @throws XMPPException
	 *             in case node could not be destroyed
	 */
	public void destroySpacePubSubNode(String sid) throws XMPPException {
		pubsub.deleteNode(getSpaceNodeIdentifier(sid));
		log.info("Space " + sid + ": PubSub node destroyed successfully");
	}

	/**
	 * Gets the XMPP PubSub node for the given space. If such a node does not
	 * yet exist, it is created automatically.
	 * 
	 * @param sid
	 *            String a space identifier
	 * @return Node a PubSub leaf node
	 * @throws XMPPException
	 *             in case node creation fails.
	 */
	public Node getSpacePubSubNode(String sid) throws XMPPException {

		Node r;
		try {
			r = pubsub.getNode(getSpaceNodeIdentifier(sid));
			return r;
		} catch (XMPPException e) {
			Node n = createSpacePubSubNode(sid);
			return n;
		}
	}

	/**
	 * Creates an XMPP PubSub node for a given space and returns a configured
	 * node, if creation was successful.
	 * 
	 * @param sid
	 *            String a space identifier
	 * @return Node a PubSub node
	 * @throws XMPPException
	 *             in case a node with the same identifier already exists or
	 *             connection problems occurred.
	 */
	public Node createSpacePubSubNode(String sid) throws XMPPException {

		// Complete and send node configuration form
		// TODO: Do we need to refine standard configuration?
		// Documentation of all configuration options available at
		// http://xmpp.org/extensions/xep-0060.html#registrar-formtypes-config

		ConfigureForm form = new ConfigureForm(FormType.submit);

		//dumpFormFields(form);

		// TODO: set back to controlled node access once hook calls are clarified
		
		// for now continue with open access model
		// form.setAccessModel(AccessModel.whitelist);
		form.setPublishModel(PublishModel.open);
		form.setAccessModel(AccessModel.open);
		

		form.setDeliverPayloads(true);
		form.setPersistentItems(false);
		form.setNotifyConfig(false);
		form.setNotifyDelete(true);
		form.setNotifyRetract(false);
		
		form.setNodeType(NodeType.leaf);
		// TODO: see if this feature is supported and how it is realizable
		form.setPresenceBasedDelivery(true);

		// TODO: link pubsub node with chat room. Is it needed? Does it make
		// sense?
		// form.setReplyRoom(replyRooms)

		// submit completed form
		// Create the node
		LeafNode node = (LeafNode) pubsub.createNode(
				getSpaceNodeIdentifier(sid), form);

		log.info("Space " + sid + ": PubSub node created successfully");
		// return configured node
		return node;
	}

	/**
	 * Provides access to an XMPP Multi-User Chat room for a given space. If a
	 * respective room does not exist yet, it is automatically created with a
	 * standard configuration.
	 * 
	 * @param space
	 *            String a space identifier
	 * @return MultiUserChat an object allowing interaction in space chat room
	 * @throws XMPPException
	 *             in case room creation failed or connection problems occurred
	 */
	public MultiUserChat getSpaceChatRoom(Concept space) throws XMPPException {

		RoomInfo r = null;
		String sUUID = space.getUuid().toString();

		try {
			r = MultiUserChat.getRoomInfo(xc, getSpaceRoomJid(sUUID));
			// if the respective room already exists, return a MUC object to
			// interact.
			MultiUserChat muc = new MultiUserChat(xc, getSpaceRoomJid(sUUID));
			return muc;
		} catch (XMPPException e) {
			// if the respective room does not yet exist, create and configure
			// it first and then return it.
			MultiUserChat muc = new MultiUserChat(xc, getSpaceRoomJid(sUUID));

			// create room; will be locked, since it is yet unconfigured.
			muc.create("role-realtime-service");

			// request room configuration form
			Form f = muc.getConfigurationForm();

			// dumpFormFields(f);

			// complete room configuration
			Form s = f.createAnswerForm();

			// the configuration options used below are available for both
			// Openfire and ejabberd
			s.setAnswer("muc#roomconfig_roomname", "Space " + space.getId() + " Chat");
			s.setAnswer("muc#roomconfig_roomdesc", "Chat room for ROLE space "
					+ space.getId());
			s.setAnswer("muc#roomconfig_publicroom", true);
			s.setAnswer("muc#roomconfig_persistentroom", true);
			s.setAnswer("muc#roomconfig_changesubject", true);
			s.setAnswer("muc#roomconfig_moderatedroom", false);
			s.setAnswer("muc#roomconfig_membersonly", true);
			s.setAnswer("muc#roomconfig_whois", Arrays.asList(new String[] { "anyone" }));

			// TODO: Which other room configuration would we need?
			// Full set of room configuration options available at
			// http://xmpp.org/extensions/xep-0045.html#registrar-formtype-owner

			// submit room configuration
			muc.sendConfigurationForm(s);
			log.info("Space " + space.getId() + ": chat room created and configured successfully");

			return muc;
		}
	}

	/**
	 * Destroys the chat room for a given space.
	 * 
	 * @param sid
	 *            String a space identifier
	 * @throws XMPPException
	 *             in case room could not be destroyed
	 */
	public void destroySpaceChatRoom(String sid) throws XMPPException {

		try {
			RoomInfo r = MultiUserChat.getRoomInfo(xc, getSpaceRoomJid(sid));
		} catch (XMPPException e) {
			return;
		}
		MultiUserChat muc = new MultiUserChat(xc, getSpaceRoomJid(sid));
		muc.destroy(null, null);
		log.info("Space " + sid + ": chat room destroyed");
	}

	/**
	 * Grants membership for a given user to a given space, in particular in
	 * terms of access for both space PubSub node and chat room.
	 * 
	 * @param space
	 *            a space
	 * @param uid
	 *            a user identifier
	 * @throws XMPPException
	 */
	public void grantSpaceMembership(Concept space, String uid)
			throws XMPPException {
		grantSpaceRoomMembership(space, uid);
		grantSpaceNodeMembership(space, uid);
	}

	/**
	 * Revokes membership for a given user from a given space, in particular in
	 * terms of access for both space PubSub node and chat room.
	 * 
	 * @param space
	 *            a space
	 * @param uid
	 *            a user identifier
	 * @throws XMPPException
	 */
	public void revokeSpaceMembership(Concept space, String uid)
			throws XMPPException {
		revokeSpaceRoomMembership(space, uid);
		revokeSpaceNodeMembership(space, uid);
	}

	/**
	 * Grants membership for a given user to the chat room for a given space.
	 * 
	 * @param space
	 *            a space
	 * @param uid
	 *            a user identifier
	 * @throws XMPPException
	 *             in case room cannot be found, membership cannot be granted or
	 *             connection problems occurred
	 */
	public void grantSpaceRoomMembership(Concept space, String uid)
			throws XMPPException {
		MultiUserChat muc = getSpaceChatRoom(space);
		muc.grantMembership(getUserJid(uid));
		log.info("Space " + space.getId() + ": granted chat room membership to user" + uid);
	}

	/**
	 * Revokes membership for a given user from the chat room for a given space.
	 * 
	 * @param space
	 *            a space
	 * @param uid
	 *            a user identifier
	 * @throws XMPPException
	 *             in case room cannot be found, membership cannot be revoked or
	 *             connection problems occurred
	 */
	public void revokeSpaceRoomMembership(Concept space, String uid)
			throws XMPPException {
		MultiUserChat muc = getSpaceChatRoom(space);
		muc.revokeMembership(getUserJid(uid));
		log.info("Space " + space.getId() + ": revoked chat room membership from user " + uid);
		
	}

	/**
	 * Grants membership for a given user to the pubsub node for a given space.
	 * 
	 * @param sid
	 *            a space identifier
	 * @param uid
	 *            a user identifier
	 * @throws XMPPException
	 *             in case node cannot be found, membership cannot be granted or
	 *             connection problems occurred
	 */
	public void grantSpaceNodeMembership(Concept space, String uid)
			throws XMPPException {
		setSpaceNodeAffiliation(space.getUuid().toString(), uid, OwnerAffiliation.Type.publisher);
		log.info("Space " + space.getId() + ": granted PubSub node membership to user " + uid);
	}

	/**
	 * Revokes membership for a given user from the pubsub node for a given
	 * space.
	 * 
	 * @param sid
	 *            a space identifier
	 * @param uid
	 *            a user identifier
	 * @throws XMPPException
	 *             in case node cannot be found, membership cannot be revoked or
	 *             connection problems occurred
	 */
	public void revokeSpaceNodeMembership(Concept space, String uid)
			throws XMPPException {
		setSpaceNodeAffiliation(space.getUuid().toString(), uid, OwnerAffiliation.Type.none);
		log.info("Space " + space.getId() + ": revoked PubSub node membership from user " + uid);
	}

	/**
	 * Publishes a notification about an update of a given resource to a given
	 * space in the form of a ROLE IWC intent. (@see Intent).
	 * 
	 * @param sid
	 *            String a space identifier
	 * @param ruri
	 *            String a resource URI
	 * @throws XMPPException
	 *             in case publication fails.
	 * @throws URISyntaxException
	 *             in case ruri is not a valid URI.
	 */
	public void publishResourceUpdate(String sid, String ruri)
			throws XMPPException, URISyntaxException {

		// retrieve node for space
		LeafNode n = (LeafNode) getSpacePubSubNode(sid);

		// construct ROLE IWC intent describing resource update
		// TODO: design intent
		Intent i = new Intent("", getUserJid(getXmppUser())
				+ "/service?sender=http://url.to/service", // TODO: find better
															// sender URL;
															// probably URL of
															// service?
				"ACTION_UPDATE", ruri.toString(), "application/json", // TODO:
																		// get
																		// MIME
																		// type
																		// of
																		// resource;
																		// will
																		// it be
																		// different
																		// that
																		// JSON?
				null, new String[] { "PUBLISH_GLOBAL" }, null);

		// publish payload including intent XML representation
		n.publish(prepareIntentPayload(i));
	}

	/**
	 * Produces a payload item wrapping a given ROLE IWC intent.
	 * 
	 * @param intent
	 *            Intent the intent to be wrapped.
	 * @return PayloadItem<SimplePayload> a payload item containing an XML
	 *         representation of the ROLE IWC intent
	 */
	private PayloadItem<SimplePayload> prepareIntentPayload(Intent i) {
		SimplePayload payload = new SimplePayload("event",
				Intent.ROLE_IWC_XMLNS, i.toXml());
		PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(
				payload);
		return item;
	}

	/**
	 * Sets the affiliation of a given user to the node dedicated to the given
	 * space. It should be noted that only the owner of the respective node is
	 * allowed to perform this operation.
	 * 
	 * @param sid
	 *            String a space identifier
	 * @param uid
	 *            String a user identifier
	 * @param t
	 *            OwnerAffiliationType the type of the affiliation to set
	 * @throws XMPPException
	 */
	private void setSpaceNodeAffiliation(String sid, String uid,
			OwnerAffiliation.Type t) throws XMPPException {
		String nodeId = getSpaceNodeIdentifier(sid);
		String userJid = getUserJid(uid);

		List<OwnerAffiliation> v = new Vector<OwnerAffiliation>();
		OwnerAffiliation a = new OwnerAffiliation(userJid, t);
		v.add(a);

		OwnerAffiliationsExtension ae = new OwnerAffiliationsExtension(nodeId,
				v);
		IQ reply = (IQ) sendPubsubPacket(Type.SET, ae, PubSubNamespace.OWNER);
		//log.info("Getting Reply from XMPP Server after setting Space Node affiliation:");
		//log.info(reply.toXML());
	}

	private Packet sendPubsubPacket(Type type, PacketExtension ext,
			PubSubNamespace ns) throws XMPPException {
		return SyncPacketSend.getReply(xc, createPubsubPacket(type, ext, ns));
	}

	private PubSub createPubsubPacket(Type type, PacketExtension ext,
			PubSubNamespace ns) {
		PubSub request = new PubSub();
		request.setTo("pubsub." + getXmppHost());
		// request.setFrom(getXmppUser()+"@"+getXmppHost()+"/Smack");
		request.setType(type);

		if (ns != null) {
			request.setPubSubNamespace(ns);
		}
		request.addExtension(ext);
		return request;
	}

	// *********** EXPERIMENTAL METHODS *************************************

	/**
	 * Produces a dump of the fields provided in a Form on system out. This is
	 * just a helper method.
	 * 
	 * @param f
	 *            Form the form to be dumped
	 */
	private static void dumpFormFields(Form f) {
		System.out.println("Dumping Form Fields...");
		Iterator<FormField> fields = f.getFields();

		while (fields.hasNext()) {
			FormField field = (FormField) fields.next();
			if (!FormField.TYPE_HIDDEN.equals(field.getType())) {
				System.out.println(field.getVariable() + " (" + field.getType()
						+ ") " + field.getLabel());
			}
		}
	}

	/**
	 * Returns a list of affiliations for the given node.
	 * 
	 * @param nodeId
	 *            a pubsub node identifier
	 * @return List<OwnerAffiliation> a list of affiliations
	 * @throws XMPPException
	 *             in case list of affiliations cannot be retrieved or
	 *             connection problems occurred
	 */
	private List<OwnerAffiliation> getPubSubNodeAffiliations(String nodeId)
			throws XMPPException {
		// IMPORTANT NOTE: For some reason this method does not return any
		// affiliations for both Openfire & ejabberd!
		// However, at least for ejabberd the server enforces the white listing
		// mechanism we need. The method
		// setSpaceNodeAffiliation thus achieves the desired effect, that only
		// those entities can subscribe and publish,
		// which have the 'publisher' affiliation with the respective node.

		PubSub reply = (PubSub) sendPubsubPacket(Type.GET,
				new OwnerAffiliationsExtension(nodeId), PubSubNamespace.OWNER);
		OwnerAffiliationsExtension listElem = (OwnerAffiliationsExtension) reply
				.getExtension(PubSubElementType.AFFILIATIONS);
		
		if (listElem != null) {
			return listElem.getAffiliations();
		} else
			return null;
	}

	/*
	 * Comment Dominik:
	 * 
	 * According to XEP-060 it is not allowed that one user subscribes or
	 * unsubscribes another user. What we could do is to programmatically
	 * establish a temporary XMPP connection for the given user and then
	 * subscribe him automatically.
	 * 
	 * What we actually CAN do is to control the access for subscriptions and
	 * publications as part of the configuration for a given node, since the JID
	 * used by this service has created the node and is thus the owner.
	 * 
	 * Subscription/Unsubscription should be controlled by the user IMHO. We
	 * have support in both our WSXMPP library and the dojox.xmpp patch. The
	 * Strophe PubSub plugin supports these use cases as well.
	 */
	public String subscribeSpace(String sid, String uid) {
		return null;
	}

	public String unsubscribeSpace(String sid, String uid) {
		return null;
	}
}
