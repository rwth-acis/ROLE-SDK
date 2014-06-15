package se.kth.csc.kmr.conserve.iface;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.PathSegmentImpl;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.Responder;
import se.kth.csc.kmr.conserve.core.AbstractRequest;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.logic.ErrorResponder;

public class BaseRequest extends AbstractRequest {

	public Concept resolve(String uri) {
		String[] segments = uri.split("/");
		if (segments.length < 4) {
			return null;
		}
		String serviceId = segments[0] + "/" + segments[1] + "/" + segments[2]
				+ "/";
		store = injector.getInstance(Contemp.class);
		Concept service = store.getConcept(store.getRootUuid(),
				ConserveTerms.hasService, serviceId);
		if (service == null) {
			return null;
		}
		Responder responder = resolveResponder(service);
		Concept concept = service;
		this.resolutionPath.add(new Resolution(Resolution.StandardType.ROOT,
				service));
		this.resolutions.put(Resolution.StandardType.ROOT, service);
		for (int i = 3; i < segments.length; i++) {
			if (concept == null) {
				return null;
			}
			setId(new PathSegmentImpl(segments[i], true));
			Resolution resolution = responder != null ? responder.resolve(this)
					: null;
			if (resolution == null || resolution.getContext() == null) {
				this.responder = new ErrorResponder(Response.Status.NOT_FOUND);
				resolution = new Resolution(Resolution.StandardType.ERROR, null);
			} else {
				if (!Resolution.StandardType.CONTEXT.equals(resolution
						.getType())
						&& (this.resolutions.size() == 1 || (this.resolutions
								.size() == 2 && this.resolutions
								.containsKey(Resolution.StandardType.CONTEXT)))) {
					responder.hit(this);
				}
				Responder agent = resolveResponder(resolution.getContext());
				responder = agent != null ? agent : new ErrorResponder(
						Response.Status.NOT_FOUND);
			}
			this.resolutionPath.add(resolution);
			this.resolutions.put(resolution.getType(), resolution.getContext());
			this.pathSegments.put(resolution.getType(), this.pathSegment);
			if (!Resolution.StandardType.CONTEXT.equals(resolution.getType())) {
				return concept;
			}
			concept = resolution.getContext();
		}
		return concept;
	}

	@Override
	public MultivaluedMap<String, String> getFormMap() {
		return null;
	}

}
