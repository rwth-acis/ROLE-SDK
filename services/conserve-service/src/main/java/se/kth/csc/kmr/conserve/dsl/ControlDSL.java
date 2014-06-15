package se.kth.csc.kmr.conserve.dsl;

import java.net.URI;

import se.kth.csc.kmr.conserve.Control;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class ControlDSL {

	@Inject
	private se.kth.csc.kmr.conserve.Contemp store;

	@Inject
	private Injector injector;
	
	private final Control impl;

	public ControlDSL() {
		if (!(this instanceof Control)) {
			throw new IllegalStateException();
		}
		impl = (Control) this;
	}

	public URI uri() {
		// TODO: Remove check for urn:uuid
		if (impl.getUri() != null && !impl.getUri().startsWith("urn:uuid:")) {
			return URI.create(impl.getUri());
		}
		ConceptDSL object = (ConceptDSL) store.getConcept(impl.getObject());
		if (object == null) {
			return URI.create("urn:uuid:" + impl.getObject());
		}
		injector.injectMembers(object);
		return object.uri();
	}
	
}
