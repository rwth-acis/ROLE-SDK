package eu.role_project.service.resource;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractInitializer;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;

public class TestDomainInitializer extends AbstractInitializer {

	private static Logger log = LoggerFactory
			.getLogger(TestDomainInitializer.class);

	@Inject
	private Contemp conserve;

	@Override
	public void initialize(Request request) {
		URI domainUri = ((RequestImpl) request).getUriInfo().getBaseUri();
		log.info("Initializing a default domain: " + domainUri);
		Concept domain = store().in(conserve.getRootUuid())
				.sub(ConserveTerms.hasService).acquire(domainUri);

		store().in(domain).put(ConserveTerms.realm,
				UUID.fromString("237b34eb-b71b-4b69-a68b-c97249f759f6"));
	}

}