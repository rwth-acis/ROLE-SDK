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
package se.kth.csc.kmr.conserve.iface.jaxrs;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Guard;
import se.kth.csc.kmr.conserve.Listener;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.Responder;
import se.kth.csc.kmr.conserve.core.AbstractRequest;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.RequestHandle;
import se.kth.csc.kmr.conserve.logic.ErrorResponder;
import se.kth.csc.kmr.conserve.logic.LinkResponder;
import se.kth.csc.kmr.conserve.util.Base64UUID;
import se.kth.csc.kmr.conserve.util.LinkHeaderGraph;

@Path("/")
public class RequestImpl extends AbstractRequest {

	private static Logger log = LoggerFactory.getLogger(RequestImpl.class);

	@javax.ws.rs.core.Context
	private UriInfo uriInfo;

	@javax.ws.rs.core.Context
	private HttpHeaders requestHeaders;

	@javax.ws.rs.core.Context
	private HttpServletRequest httpServletRequest;

	@javax.ws.rs.core.Context
	private HttpServletResponse httpServletResponse;

	@javax.ws.rs.core.Context
	private ServletContext servletContext;

	@javax.ws.rs.core.Context
	private SecurityContext securityContext;
	
	private MultivaluedMap<String, String> formMap; 

	@Inject
	public RequestImpl(RequestHandle handle) {
		handle.setRequest(this);
	}

	public PathSegment getId() {
		return pathSegment;
	}

	public PathSegment getId(boolean encoded) {
		return encoded ? pathSegmentEncoded : pathSegment;
	}

	public Map<Resolution.Type, PathSegment> getPathSegments() {
		return unmodifiablePathSegments;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	public HttpHeaders getRequestHeaders() {
		return requestHeaders;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public HttpServletResponse getHttpServletResponse() {
		return httpServletResponse;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public SecurityContext getSecurityContext() {
		return securityContext;
	}

	@Path("{id}")
	public Object getSubresource(@PathParam("id") String id) {
		prepareRequest(false);
		if (this.responder instanceof ErrorResponder) {
			return this.responder;
		}

		if (this.pathSegmentsIter == null) {
			this.pathSegmentsIter = uriInfo.getPathSegments().iterator();
			this.pathSegmentsEncodedIter = uriInfo.getPathSegments(false)
					.iterator();
		}
		this.pathSegment = this.pathSegmentsIter.next();
		this.pathSegmentEncoded = this.pathSegmentsEncodedIter.next();
		assert this.pathSegment.getPath().equals(id);

		Resolution resolution = this.responder != null ? this.responder
				.resolve(this) : null;
		if (resolution == null || resolution.getContext() == null) {
			this.responder = new ErrorResponder(Response.Status.NOT_FOUND);
			resolution = new Resolution(Resolution.StandardType.ERROR, null);
		} else {
			if (!Resolution.StandardType.CONTEXT.equals(resolution.getType())
					&& (this.resolutions.size() == 1 || (this.resolutions
							.size() == 2 && this.resolutions
							.containsKey(Resolution.StandardType.CONTEXT)))) {
				this.responder.hit(this);
			}
			Responder agent = resolveResponder(resolution.getContext());
			this.responder = agent != null ? agent : new ErrorResponder(
					Response.Status.NOT_FOUND);
		}
		this.resolutionPath.add(resolution);
		this.resolutions.put(resolution.getType(), resolution.getContext());
		this.pathSegments.put(resolution.getType(), this.pathSegment);

		if (resolution.getType().equals(Resolution.StandardType.RELATION)) {
			topic = resolution.getContext().getId();
		}

		return this;
	}

	private void prepareRequest(boolean isLastCall) {
		prepareRequest(isLastCall, null);
	}

	private void prepareRequest(boolean isLastCall, byte[] data) {
		if (!app.isSetUp()) {
			if (!app.isSettingUp()
					&& uriInfo.getPath().contains(bootstrapRequestId)) {
				this.store = injector.getInstance(Contemp.class);
				app.setUp(this);
				this.responder = new ErrorResponder(Response.Status.OK,
						bootstrapRequestId);
				return;
			} else {
				this.responder = new ErrorResponder(
						Response.Status.SERVICE_UNAVAILABLE);
				return;
			}
		} else {
			this.store = injector.getInstance(Contemp.class);
		}
		if (isLastCall) {
			String queryString = httpServletRequest.getQueryString();
			if (queryString != null && queryString.length() > 0) {
				Map<String, String> rels = parseQueryParams(queryString
						.substring(1));
				if (rels != null) {
					this.linkRels.putAll(rels);
				}
			}
			List<String> linkHeaders = requestHeaders.getRequestHeader("Link");
			if (linkHeaders != null) {
				Map<String, String> rels = parseLinkHeader(linkHeaders.get(0));
				if (rels != null) {
					this.linkRels.putAll(rels);
				}
			}
			if (getId() != null && getId().getMatrixParameters() != null) {
				for (Entry<String, List<String>> matrixParam : getId()
						.getMatrixParameters().entrySet()) {
					this.linkRels.put(matrixParam.getKey(), matrixParam
							.getValue().get(0));
				}
			}
			if (data != null
					&& requestHeaders.getRequestHeader("Content-Type") != null
					&& requestHeaders.getRequestHeader("Content-Type").get(0)
							.equals("application/x-www-form-urlencoded")) {
				try {
					String bodyString = new String(data, "UTF-8");
					if (bodyString != null) {
						Map<String, String> rels = parseQueryParams(bodyString);
						if (rels != null) {
							this.linkRels.putAll(rels);
						}
					}
				} catch (UnsupportedEncodingException e) {
				}
			}
			MultivaluedMap<String, String> formParams = getFormMap();
			
			if (formParams != null) {
				Map<String, String> rels = new HashMap<String, String>();
				for (String rel : formParams.keySet()) {
					rels.put(rel, formParams.getFirst(rel));
				}
				rels = expandOpenAppParams(rels);
				this.linkRels.putAll(rels);
			}
			
			if (this.linkRels
					.containsKey("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate")) {
				String pred = this.linkRels
						.get("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");
				Concept predCxt = store.getConcept(store.getRootUuid(), pred);
				if (predCxt != null
						&& ConserveTerms.hasTerm.equals(predCxt.getPredicate())) {
					Resolution.Type resolutionType = Resolution.StandardType.RELATION;
					this.resolutions.put(resolutionType, predCxt);
					Responder agent = resolveResponder(predCxt);
					this.responder = agent != null ? agent
							: new ErrorResponder(Response.Status.NOT_FOUND);
					this.topic = pred;
				} else {
					this.responder = new ErrorResponder(
							Response.Status.NOT_FOUND);
				}
			}
		}
		if (this.resolutionPath.isEmpty()) {
			Concept root = store.getConcept(app.getRootUuid(this));
			this.resolutionPath.add(new Resolution(
					Resolution.StandardType.ROOT, root));
			this.resolutions.put(Resolution.StandardType.ROOT, root);
			this.responder = this.responder == null ? resolveResponder(root)
					: this.responder;
			this.guards = resolveGuard(root);
			for (Listener listener : listenerMap.get(ConserveTerms.root)) {
				listener.doGet(this);
			}
		} else {
			this.guards = resolveGuard(getContext());
		}
		if (isLastCall) {
			if (this.resolutions.size() == 1
					|| (this.resolutions.size() == 2 && this.resolutions
							.containsKey(Resolution.StandardType.CONTEXT))) {
				this.responder.hit(this);
			}
		}
	}

	private String unquote(String value) {
		if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

	private Map<String, String> parseLinkHeader(String value) {
		Pattern linkexp = Pattern
				.compile("<[^>]*>\\s*(\\s*;\\s*[^\\(\\)<>@,;:\"\\/\\[\\]\\?={} \t]+=(([^\\(\\)<>@,;:\"\\/\\[\\]\\?={} \t]+)|(\"[^\"]*\")))*(,|$)");
		Pattern paramexp = Pattern
				.compile("[^\\(\\)<>@,;:\"\\/\\[\\]\\?={} \t]+=(([^\\(\\)<>@,;:\"\\/\\[\\]\\?={} \t]+)|(\"[^\"]*\"))");

		Matcher matches = linkexp.matcher(value);
		// Map<String, Map<String, String>> rels = new HashMap<String,
		// Map<String, String>>();
		Map<String, String> links = new HashMap<String, String>();
		// Map<String, String> titles = new HashMap<String, String>();
		while (matches.find()) {
			String[] split = matches.group().split(">");
			String href = split[0].substring(1);
			String ps = split[1];
			Map<String, String> link = new HashMap<String, String>();
			link.put("href", href);
			Matcher s = paramexp.matcher(ps);
			while (s.find()) {
				String p = s.group();
				String[] paramsplit = p.split("=");
				String name = paramsplit[0];
				link.put(name, unquote(paramsplit[1]));
			}

			if (link.containsKey("rel")) {
				links.put(link.get("rel"), link.get("href"));
				// rels.put(link.get("rel"), link);
			}
			// if (link.containsKey("title")) {
			// rels.put(link.get("title"), link);
			// }
		}
		return links;
	}

	public static Map<String, String> parseQueryParams(String query) {
		Map<String, String> params = new HashMap<String, String>();
		for (String queryParam : query.split("\\&")) {
			String[] queryValue = queryParam.split("=");
			if (queryValue.length == 2) {
				String key = percentDecode(queryValue[0]);
				String value = percentDecode(queryValue[1]);
				params.put(key, value);
			} else {
				return new HashMap<String, String>();
			}
		}
		return expandOpenAppParams(params);
	}
	
	public static Map<String, String> expandOpenAppParams(Map<String, String> params) {
		Map<String, String> rels = new HashMap<String, String>();

		for (Entry<String, String> namespace : params.entrySet()) {
			if (namespace.getKey().startsWith("openapp.ns.")
					&& namespace.getKey().length() > 11) {
				String prefix = "openapp." + namespace.getKey().substring(11)
						+ ".";
				for (Entry<String, String> rel : params.entrySet()) {
					if (rel.getKey().startsWith(prefix)
							&& rel.getKey().length() > prefix.length()) {
						rels.put(
								namespace.getValue()
										+ rel.getKey().substring(
												prefix.length()),
								rel.getValue());
					}
				}
			}
		}
		return rels;
	}

	private static String percentDecode(String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private String stringifyLinkHeader(Map<String, Map<String, String>> rels) {
		StringBuilder sb = new StringBuilder();
		// Pattern safeAttr = Pattern.compile("[^,;]+");
		for (Entry<String, Map<String, String>> rel : rels.entrySet()) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			sb.append('<');
			sb.append(rel.getValue().get("href"));
			sb.append('>');
			sb.append("; rel=");
			String relAttr = rel.getValue().containsKey("rel") ? rel.getValue()
					.get("rel") : rel.getKey();
			// if (safeAttr.matcher(relAttr).matches()) {
			// sb.append(relAttr);
			// } else {
			sb.append('"');
			sb.append(relAttr);
			sb.append('"');
			// }
			for (Entry<String, String> attr : rel.getValue().entrySet()) {
				if (!"href".equals(attr.getKey())
						&& !"rel".equals(attr.getKey())) {
					sb.append(';');
					sb.append(' ');
					sb.append(attr.getKey());
					sb.append('=');
					// if (safeAttr.matcher(attr.getValue()).matches()) {
					// sb.append(attr.getValue());
					// } else {
					sb.append('"');
					sb.append(attr.getValue());
					sb.append('"');
					// }
				}
			}
		}
		return sb.toString();
	}

	@GET
	public Object doGet() {
		try {
			prepareRequest(true);
			if (this.responder instanceof ErrorResponder) {
				return this.responder.doGet(this);
			}
			// for (Listener listener : listenerMap.get(ConserveTerms.root)) {
			// Object response = listener.doGet(this);
			// if (response != this) {
			// return response;
			// }
			// }
			for (Guard.GuardContext guardContext : this.guards) {
				this.guardContext = guardContext.getContext();
				if (guardContext.getGuard().canGet(this)) {

					// Set headers
					Concept context = getContext();
					// Map<String, Map<String, String>> relations = new
					// LinkedHashMap<String, Map<String, String>>();
					UriBuilder uriBuilder = store().in(context).uriBuilder();
					URI contextUri = uriBuilder.build();
					// Map<String, String> relationAttributes;
					// Map<UUID, Content> contents = new TreeMap<UUID,
					// Content>();
					// for (UUID content : globalTopics) {
					// contents.put(content, null);
					// }
					// // for (Control domain : store.getControls(
					// // context.getPredicate(), ConserveTerms.range,
					// // ConserveTerms.domain, null)) {
					// // contents.put(domain.getContext(), null);
					// // }
					// for (Content content :
					// store.getContents(context.getUuid())) {
					// contents.put(content.getPredicate(), content);
					// }
					// for (Entry<UUID, Content> entry : contents.entrySet()) {
					// Concept topic = store.getConcept(entry.getKey());
					// if (topic == null || topic.getId() == null) {
					// continue;
					// }
					// Entry<String, String> namespace = getNamespace(topic
					// .getId());
					// if (namespace == null) {
					// continue;
					// }
					// // URI uri = uriBuilder
					// // .clone()
					// // .path(namespace.getKey()
					// // + ':'
					// // + topic.getId().substring(
					// // namespace.getValue().length()))
					// // .build();
					// relationAttributes = new LinkedHashMap<String, String>();
					// // relationAttributes.put("href", uri.toASCIIString());
					// String contextId = context.getId();
					// if (contextId == null || contextId.contains(":")
					// || contextId.contains("/")) {
					// contextId = Base64UUID.encode(context.getUuid());
					// }
					// relationAttributes.put(
					// "href",
					// (contextId.endsWith("/") ? "" : contextId)
					// + "/"
					// + namespace.getKey()
					// + ':'
					// + topic.getId().substring(
					// namespace.getValue().length()));
					// // if (entry.getValue() != null) {
					// // relationAttributes.put("type", entry.getValue()
					// // .getType());
					// // }
					// String rel = topic.getId();
					// if (ConserveTerms.system.equals(entry.getKey())) {
					// rel = "describedby";
					// } else if (ConserveTerms.metadata
					// .equals(entry.getKey())) {
					// rel = "edit";
					// } else if (ConserveTerms.representation.equals(entry
					// .getKey())) {
					// rel = "edit-media";
					// }
					// relations.put(rel, relationAttributes);
					// }
					// // httpServletResponse.addHeader("Link",
					// // stringifyLinkHeader(relations));
					// //
					// // relations = new LinkedHashMap<String, Map<String,
					// // String>>();
					// contents = new TreeMap<UUID, Content>();
					// for (Control domain : store.getControls(
					// context.getPredicate(), ConserveTerms.range,
					// ConserveTerms.domain, null)) {
					// contents.put(domain.getContext(), null);
					// }

					log.info("Doing response: " + uriInfo.getRequestUri()
							+ " Responder: " + responder);

					Object response = responder.doGet(this);

					log.info("Returning response: " + uriInfo.getRequestUri()
							+ " Responder: " + responder);

					if (topic != null) {
						Entry<String, String> namespace = getNamespace(topic);
						String name = topic.substring(namespace.getValue()
								.length());
						// URI contentLocation = uriBuilder.clone()
						// .path(namespace.getKey() + ':' + name).build();
						// httpServletResponse.addHeader("Content-Location",
						// contentLocation.toASCIIString());
						String contextId = context.getId();
						if (context.getUuid().equals(getRoot().getUuid())) {
							contextId = "/";
						} else if (contextId == null || contextId.contains(":")
								|| contextId.contains("/")) {
							contextId = Base64UUID.encode(context.getUuid());
						}
						httpServletResponse
								.addHeader(
										"Content-Location",
										(contextId.endsWith("/") ? ""
												: contextId)
												+ "/"
												+ namespace.getKey()
												+ ':'
												+ name);

						// Concept topicConcept = store.getConcept(
						// ConserveTerms.root, topic);
						// if (topicConcept != null
						// && topicConcept.getPredicate().equals(
						// ConserveTerms.hasTerm)
						// && !contents
						// .containsKey(topicConcept.getUuid())) {
						// contents.put(topicConcept.getUuid(), null);
						// }
					}
					//
					// for (Entry<UUID, Content> entry : contents.entrySet()) {
					// Concept topic = store.getConcept(entry.getKey());
					// if (topic == null || topic.getId() == null) {
					// continue;
					// }
					// Entry<String, String> namespace = getNamespace(topic
					// .getId());
					// if (namespace == null) {
					// continue;
					// }
					// relationAttributes = new LinkedHashMap<String, String>();
					// String contextId = context.getId();
					// if (contextId == null || contextId.contains(":")
					// || contextId.contains("/")) {
					// contextId = Base64UUID.encode(context.getUuid());
					// }
					// relationAttributes.put(
					// "href",
					// (contextId.endsWith("/") ? "" : contextId)
					// + "/"
					// + namespace.getKey()
					// + ':'
					// + topic.getId().substring(
					// namespace.getValue().length()));
					// // relationAttributes.put("rel", "self");
					// // relationAttributes.put("anchor", topic.getId());
					// relationAttributes.put("rel", topic.getId());
					// relations.put(topic.getId(), relationAttributes);
					// }
					// httpServletResponse.addHeader("Link",
					// stringifyLinkHeader(relations));
					LinkHeaderGraph linkHeaderGraph = new LinkHeaderGraph(
							contextUri.toString());
					injector.getInstance(LinkResponder.class).addTriples(
							linkHeaderGraph.getGraph(), this, context,
							uriBuilder);
					httpServletResponse.addHeader("Link",
							linkHeaderGraph.toString());
					httpServletResponse.addHeader("Content-Base",
							contextUri.toASCIIString());

					return response;
				}
			}

			// TODO: Make this less hard-coded
			if (getContext().getUuid().equals(store.getRootUuid())) {
				return Response.seeOther(
						uriInfo.getBaseUriBuilder()
								.path(Base64UUID.encode(store.getRootUuid()))
								.path(":authentication")
								.queryParam("return",
										uriInfo.getRequestUri().getPath())
								.build()).build();
			}

			return new ErrorResponder(Response.Status.FORBIDDEN).doGet(this);
		} catch (RuntimeException e) {
			log.error("GET request error", e);
			store.rollback();
			return new ErrorResponder(Response.Status.INTERNAL_SERVER_ERROR)
					.doGet(this);
		} finally {
			if (store != null) {
				store.disconnect();
			}
		}
	}

	public Entry<String, String> getNamespace(String uri) {
		for (Entry<String, String> entry : namespaces.entrySet()) {
			String namespace = entry.getValue();
			if (uri.length() > namespace.length()
					&& uri.substring(0, namespace.length()).equals(namespace)) {
				return entry;
			}
		}
		return null;
	}

	public String getQualifedName(String uri) {
		Entry<String, String> namespace = getNamespace(uri);
		return namespace.getKey() + ':'
				+ uri.substring(namespace.getValue().length());
	}

	@PUT
	public Object doPut(byte[] data) {
		try {
			prepareRequest(true, data);
			if (this.responder instanceof ErrorResponder) {
				return this.responder.doGet(this);
			}
			for (Listener listener : listenerMap.get(ConserveTerms.root)) {
				Object response = listener.doPut(this, data);
				if (response != this) {
					return response;
				}
			}
			for (Guard.GuardContext guardContext : this.guards) {
				this.guardContext = guardContext.getContext();
				if (guardContext.getGuard().canPut(this)) {
					return responder.doPut(this, data);
				}
			}
			return new ErrorResponder(Response.Status.FORBIDDEN).doPut(this,
					data);
		} catch (RuntimeException e) {
			log.error("PUT request error", e);
			store.rollback();
			return new ErrorResponder(Response.Status.INTERNAL_SERVER_ERROR)
					.doPut(this, data);
		} finally {
			if (store != null) {
				store.disconnect();
			}
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Object doPost_form(MultivaluedMap<String, String> form) {
		formMap = form;
		return doPost(new byte[0]);
	}

	public MultivaluedMap<String, String> getFormMap() {
		return formMap;
	}
	
	@POST
	public Object doPost(byte[] data) {
		try {
			prepareRequest(true, data);
			if (this.responder instanceof ErrorResponder) {
				return this.responder.doGet(this);
			}
			for (Listener listener : listenerMap.get(ConserveTerms.root)) {
				Object response = listener.doPost(this, data);
				if (response != this) {
					return response;
				}
			}
			for (Guard.GuardContext guardContext : this.guards) {
				this.guardContext = guardContext.getContext();
				if (guardContext.getGuard().canPost(this)) {
					Object response = responder.doPost(this, data);
					Set<Listener> listeners = listenerMap.get(getContext()
							.getUuid());
					if (listeners != null) {
						for (Listener listener : listeners) {
							listener.doPost(this, data);
						}
					}
					if (getRelation() != null) {
						listeners = listenerMap.get(getRelation().getUuid());
						if (listeners != null) {
							for (Listener listener : listeners) {
								listener.doPost(this, data);
							}
						}
					}
					return response;
				}
			}
			return new ErrorResponder(Response.Status.FORBIDDEN).doPost(this,
					data);
		} catch (RuntimeException e) {
			log.error("POST request error", e);
			store.rollback();
			return new ErrorResponder(Response.Status.INTERNAL_SERVER_ERROR)
					.doPost(this, data);
		} finally {
			if (store != null) {
				store.disconnect();
			}
		}
	}

	@DELETE
	public Object doDelete() {
		try {
			prepareRequest(true);
			if (this.responder instanceof ErrorResponder) {
				return this.responder.doGet(this);
			}
			for (Listener listener : listenerMap.get(ConserveTerms.root)) {
				Object response = listener.doDelete(this);
				if (response != this) {
					return response;
				}
			}
			for (Guard.GuardContext guardContext : this.guards) {
				this.guardContext = guardContext.getContext();
				if (guardContext.getGuard().canDelete(this)) {
					return responder.doDelete(this);
				}
			}
			return new ErrorResponder(Response.Status.FORBIDDEN).doDelete(this);
		} catch (RuntimeException e) {
			log.error("DELETE request error", e);
			store.rollback();
			return new ErrorResponder(Response.Status.INTERNAL_SERVER_ERROR)
					.doDelete(this);
		} finally {
			if (store != null) {
				store.disconnect();
			}
		}
	}

}
