/*
 * #%L
 * Conserve Concept Server
 * %%
 * Copyright (C) 2010 - 2011 KMR
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.kth.csc.kmr.conserve.logic;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import com.google.inject.Injector;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.Responder;
import se.kth.csc.kmr.conserve.core.AbstractResponder;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.util.Base64UUID;

public class ResourceResponder extends AbstractResponder {

	@Inject
	@Named("contemp-namespaces")
	private Map<String, String> namespaces;

	@Inject
	private Injector injector;

	@Override
	public boolean canGet(Request request) {
		return true;
	}

	@Override
	public boolean canPut(Request request) {
		return true;
	}

	@Override
	public boolean canPost(Request request) {
		return true;
	}

	@Override
	public boolean canDelete(Request request) {
		return true;
	}

	@Override
	public Object doGet(Request request) {
		Concept context = request.getContext();
		List<Content> contents = store.getContents(context.getUuid());
		boolean hasRepresentation = false;
		boolean hasMappedRepresentation = false;
		Responder mappedResponder = null;
		boolean hasReference = false;
		boolean wantsInfo = false;
		boolean wantsHTML = false;

		MediaType htmlMt = MediaType.valueOf("text/html");
		MediaType anyMt = MediaType.valueOf("*/*");
		for (MediaType mt : ((RequestImpl) request).getRequestHeaders()
				.getAcceptableMediaTypes()) {
			if (mt.equals(htmlMt) || mt.equals(anyMt)) {
				wantsHTML = true;
				break;
			}
		}

		// MediaType rdfXmlMt = MediaType.valueOf("application/rdf+xml");
		// for (MediaType mt : ((RequestImpl) request).getRequestHeaders()
		// .getAcceptableMediaTypes()) {
		// if (mt.equals(rdfXmlMt)) {
		// wantsInfo = true;
		// break;
		// }
		// }
		// if (request.getRequestHeaders().getRequestHeader("User-Agent") !=
		// null
		// && request.getRequestHeaders().getRequestHeader("User-Agent")
		// .contains("Java/1.6.0_07")) {
		// wantsInfo = true;
		// }

		List<Control> controls = store.getControls(context.getUuid());
		for (Control control : controls) {
			if (control.getPredicate().equals(ConserveTerms.reference)) {
				hasReference = true;
			}
		}
		for (Content content : contents) {
			if (content.getPredicate().equals(ConserveTerms.representation)) {
				hasRepresentation = true;
			}
		}

		if (request.mapResponder(ConserveTerms.representation) != null) {
			mappedResponder = request
					.getResponder(ConserveTerms.representation);
			hasMappedRepresentation = mappedResponder != null
					&& mappedResponder.canGet(request);
		}

		if (wantsHTML) {
			request.setTopic(ConserveTerms.INSTANCE.uri(ConserveTerms.app)
					.toString());
			return injector.getInstance(HTMLAppResponder.class).doGet(request);
		} else if (wantsInfo) {
			request.setTopic(ConserveTerms.INSTANCE.uri(ConserveTerms.context)
					.toString());
			return injector.getInstance(ContextResponder.class).doGet(request);
		} else if (hasReference) {
			request.setTopic(ConserveTerms.INSTANCE
					.uri(ConserveTerms.reference).toString());
			return injector.getInstance(ReferenceResponder.class)
					.doGet(request);
		} else if (hasMappedRepresentation) {
			request.setTopic(ConserveTerms.INSTANCE.uri(
					ConserveTerms.representation).toString());
			return mappedResponder.doGet(request);
		} else if (hasRepresentation) {
			request.setTopic(ConserveTerms.INSTANCE.uri(
					ConserveTerms.representation).toString());
			return injector.getInstance(RepresentationResponder.class).doGet(
					request);
		} else {
			return Response.noContent().header("Cache-Control", "no-store")
					.build();
		}
	}

	@Override
	public Object doPut(Request request, byte[] data) {
		return injector.getInstance(RepresentationResponder.class).doPut(
				request, data);
	}

	@Override
	public Object doPost(Request request, byte[] data) {
		return injector.getInstance(ContextResponder.class).doPost(request,
				data);
	}

	@Override
	public Object doDelete(Request request) {
		Concept context = request.getContext();
		store.deleteConcept(context);
		return Response.ok().header("Cache-Control", "no-store").build();
	}

	@Override
	public Resolution resolve(Request request) {
		PathSegment segment = request.getId();
		String id = segment.getPath();
		if (id.length() == 0) {
			return new Resolution(Resolution.StandardType.CONTEXT,
					request.getContext());
		}
		Resolution.Type type = Resolution.StandardType.CONTEXT;
		Concept contextObj = request.getContext();
		UUID context = contextObj.getUuid();
		Pattern qualified = Pattern
				.compile("\\A((?:(?:[A-Za-z0-9_\\.\\-]|[^\\x00-\\x7F])(?:[A-Za-z0-9_\\.\\-]|[^\\x00-\\x7F])*)?):((?:[A-Za-z0-9_\\.\\-]|[^\\x00-\\x7F])+)\\z");
		Matcher qmatcher = qualified.matcher(id);
		if (":".equals(id)
				&& segment.getMatrixParameters().containsKey("predicate")) {
			id = segment.getMatrixParameters().get("predicate").get(0);
			type = Resolution.StandardType.RELATION;
			context = store.getRootUuid();
		} else if (qmatcher.find() && namespaces.containsKey(qmatcher.group(1))) {
			id = namespaces.get(qmatcher.group(1)) + qmatcher.group(2);
			type = Resolution.StandardType.RELATION;
			context = store.getRootUuid();
		}
		try {
			UUID uuid = Base64UUID.decode(id);
			Concept res;
			if (type.equals(Resolution.StandardType.CONTEXT)
					&& request.getContext() == request.getRoot()) {
				res = store.getConcept(uuid);
			} else {
				res = store.getConcept(context, uuid);
			}
			if (res != null) {
				return new Resolution(type, res);
			}
		} catch (IllegalArgumentException e) {
		}
		return new Resolution(type, store.getConcept(context, id));
	}

}
