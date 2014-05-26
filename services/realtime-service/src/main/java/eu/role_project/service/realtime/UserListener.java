package eu.role_project.service.realtime;

import java.security.SecureRandom;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractListener;

public class UserListener extends AbstractListener {

	private static Logger log = LoggerFactory.getLogger(UserListener.class);

	private static final SecureRandom RAND = new SecureRandom();

	@Inject
	private RealtimeModule realtime;

	@Override
	public Object doPost(Request request, byte[] data) {
		if (!realtime.isConfigured()) {
			return null;
		}

		Concept user = request.getCreated();
		if (user != null && user.getPredicate().equals(ROLETerms.member)) {

			String uid = user.getUuid().toString();
			Content externalPassword = store().in(user)
					.as(ROLETerms.externalPassword).get();
			String pass;
			if (externalPassword == null) {
				pass = randomString();
				store().in(user).as(ROLETerms.externalPassword)
						.type("text/plain").string(pass);
			} else {
				pass = store().as(externalPassword).string();
			}
			
			try {
				realtime.registerUser(uid, pass);
				log.info("XMPP account for user " + uid + " created successfully");
			} catch (IllegalArgumentException e) {
				log.error("Error creating XMPP account for user " + uid, e);
			} catch (XMPPException e) {
				if(e.getMessage().equals("conflict(409)")){
					log.info("XMPP account for user " + uid + " already exists.");
				} else {
					log.error("Error creating XMPP account for user " + uid, e);
				}
			}
		}
		return null;
	}

	private static String randomString() {
		byte[] tokenSecret = new byte[16];
		RAND.nextBytes(tokenSecret);
		return Base64.encodeBase64URLSafeString(tokenSecret);
	}

}