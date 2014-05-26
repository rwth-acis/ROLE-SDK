package eu.role_project.service.resource;

import java.net.URI;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.logic.ACLGuard;

public class SimpleACL extends ACLGuard {

	// private static Logger log = LoggerFactory.getLogger(SimpleACL.class);

	@Inject
	@Named("password-salt")
	private String salt;

	@Override
	public void initialize(Request request) {
		UUID adminUuid = UUID
				.fromString("76509a35-990e-46cb-b1ad-c05399267c4a");
		super.initialize();

		String aclTemplateRoot = "@prefix acl: <http://www.w3.org/ns/auth/acl#> .\n"
				+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
				+ "[] rdf:type acl:Authorization ;\n"
				+ "acl:mode acl:Read ; \n"
				+ "acl:mode acl:Write ; \n"
				+ "acl:mode acl:Append ; \n"
				+ "acl:accessTo %s ; \n"
				+ "acl:defaultForNew %s ; \n"
				+ "acl:agent <urn:uuid:"
				+ adminUuid.toString() + "> ;\n" + ".";
		UUID root = store.getRootUuid();
		String rootUri = "<urn:uuid:" + root + ">";
		Content rootACL = store.createContent(root, ConserveTerms.acl);
		rootACL.setType("text/n3;charset=utf-8");
		byte[] rootBytes = String.format(aclTemplateRoot, rootUri, rootUri,
				rootUri, rootUri).getBytes(Charset.forName("UTF-8"));
		Blob rootData = rootACL.getData();
		try {
			rootData.setBytes(1L, rootBytes);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		store.putContent(rootACL);

		// //////////
		store().in(store.getRootUuid()).put(ConserveTerms.realm,
				store.getRootUuid());
		Concept secretAuth = store().in(store.getRootUuid())
				.sub(ConserveTerms.authentication)
				.acquire("urn:uuid:4767dd22-c074-407a-9c04-9107a625d553");
		store().in(secretAuth).put(ConserveTerms.type,
				"http://kmr.csc.kth.se/rdf/conserve/auth/Secret");
		Concept user1 = store().in(store.getRootUuid()).sub(ROLETerms.member)
				.acquire(adminUuid, "admin");
		store().in(user1)
				.as(ConserveTerms.metadata)
				.type("application/json")
				.string("{\"\": { \"http://purl.org/dc/terms/title\": [{ \"value\": \"Admin\", \"type\": \"literal\" }]}}");
		MessageDigest sha1;
		try {
			sha1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		byte[] digest = sha1.digest(("roleabdc" + salt).getBytes());
		store().in(user1).as(ConserveTerms.secret)
				.type("application/octet-stream").bytes(digest);
		// //////////

		String aclTemplateDomain = "@prefix acl: <http://www.w3.org/ns/auth/acl#> .\n"
				+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
				+ "[] rdf:type acl:Authorization ;\n"
				+ "acl:mode acl:Read ;\n"
				+ "acl:accessTo %s ; \n"
				+ "acl:defaultForNew %s ; \n"
				+ "acl:agent <http://xmlns.com/foaf/0.1/Agent> ;\n"
				+ ".\n"
				+ "[] rdf:type acl:Authorization ;\n"
				+ "acl:mode acl:Read ; \n"
				+ "acl:mode acl:Write ; \n"
				+ "acl:mode acl:Append ; \n"
				+ "acl:accessTo %s ; \n"
				+ "acl:defaultForNew %s ; \n"
				+ "acl:agent <urn:uuid:"
				+ adminUuid.toString() + "> ;\n" + ".";
		UUID domain = app.getRootUuid(request);
		String domainUri = "<urn:uuid:" + domain + ">";
		Content domainACL = store.createContent(domain, ConserveTerms.acl);
		domainACL.setType("text/n3;charset=utf-8");
		byte[] domainBytes = String.format(aclTemplateDomain, domainUri,
				domainUri, domainUri, domainUri).getBytes(
				Charset.forName("UTF-8"));
		Blob domainData = domainACL.getData();
		try {
			domainData.setBytes(1L, domainBytes);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		store.putContent(domainACL);

	}

	@Override
	public boolean canGet(Request request) {
		if (ROLETerms.tool.equals(request.getContext().getPredicate())) {
			return true;
		}
		return super.canGet(request);
	}

}