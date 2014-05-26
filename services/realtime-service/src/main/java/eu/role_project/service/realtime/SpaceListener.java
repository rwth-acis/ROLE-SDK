package eu.role_project.service.realtime;

import java.util.List;

import javax.inject.Inject;

import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.resource.ROLETerms;
import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractListener;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.util.Base64UUID;

public class SpaceListener extends AbstractListener {

	private static Logger log = LoggerFactory.getLogger(SpaceListener.class);

	@Inject
	private RealtimeModule realtime;

	@Override
	public Object doPost(Request request, byte[] data) {
		if (!realtime.isConfigured()) {
			return null;
		}

		Concept space = request.getCreated();
		if (space != null && space.getPredicate().equals(ROLETerms.space)) {

			String sid = space.getUuid().toString();
			log.info("Adding created space to XMPP server: " + sid);
			try {
				realtime.getSpaceChatRoom(space);
				realtime.getSpacePubSubNode(sid);
			} catch (IllegalArgumentException e) {
				log.error("Error creating space over XMPP", e);
			} catch (XMPPException e) {
				log.error("Error creating space over XMPP", e);
			}
		}

		space = request.getContext();
		Concept member = request.getCreated();
		if (member != null && member.getPredicate().equals(ROLETerms.member)
				&& space != null
				&& space.getPredicate().equals(ROLETerms.space)) {

			List<Control> references = store().in(member).get(
					ConserveTerms.reference);
			if (references.size() != 1) {
				throw new IllegalStateException(
						"Member must have one reference to a user, found "
								+ references.size());
			}
			Concept user = store().require(references.get(0).getObject());

			// String uid; // = user.getId();
			// // TODO: Use a regexp for allowed uid
			// // if (uid.contains(":")) {
			// uid = Base64UUID.encodeShortened(user.getUuid());
			// // }
			String uid = user.getUuid().toString();
			String sid = space.getUuid().toString();
			log.info("Granting user: '" + uid + "' membership to space: '"
					+ sid + "'");
			try {
				log.info("Granting user: '" + uid + "' membership to space room: '"
					+ sid + "'");
				realtime.grantSpaceRoomMembership(space, uid);
				//log.info("Granting user: '" + uid + "' membership to space node: '"
				//	+ sid + "'");
				//realtime.grantSpaceNodeMembership(sid, uid);

			} catch (IllegalArgumentException e) {
				log.error("Error granting over XMPP", e);
			} catch (XMPPException e) {
				log.error("Error creating space over XMPP", e);
			}

		}
		return null;
	}

}