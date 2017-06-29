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
package se.kth.csc.kmr.conserve.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Named;
import javax.ws.rs.core.PathSegment;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.kth.csc.kmr.conserve.Application;
import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Guard;
import se.kth.csc.kmr.conserve.Listener;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.Responder;
import se.kth.csc.kmr.conserve.dsl.ContempDSL;

public abstract class AbstractRequest implements Request {

	protected Map<Resolution.Type, Concept> resolutions = new HashMap<Resolution.Type, Concept>();

	protected Map<Resolution.Type, Concept> unmodifiableResolutions = Collections
			.unmodifiableMap(resolutions);

	protected LinkedList<Resolution> resolutionPath = new LinkedList<Resolution>();

	protected Iterator<PathSegment> pathSegmentsIter;

	protected Iterator<PathSegment> pathSegmentsEncodedIter;

	protected Map<Resolution.Type, PathSegment> pathSegments = new HashMap<Resolution.Type, PathSegment>();

	protected Map<Resolution.Type, PathSegment> unmodifiablePathSegments = Collections
			.unmodifiableMap(pathSegments);

	protected PathSegment pathSegment;

	protected PathSegment pathSegmentEncoded;

	@Inject
	protected Injector injector;

	@Inject
	protected Application app;

	// Injected manually
	protected Contemp store;

	@Inject
	protected Map<UUID, Responder> responderMap;

	protected Map<UUID, UUID> responderOverrides = new HashMap<UUID, UUID>();

	@Inject
	protected Map<UUID, Guard> guardMap;

	@Inject
	protected Map<UUID, Set<Listener>> listenerMap;

	@Inject
	@Named("contemp-global-topics")
	protected Set<UUID> globalTopics;

	@Inject
	@Named("contemp-namespaces")
	protected Map<String, String> namespaces;

	protected Map<String, String> linkRels = new HashMap<String, String>();

	protected Responder responder;

	protected Set<Guard.GuardContext> guards;

	protected Concept guardContext;

	protected String topic;

	@Inject
	@Named("bootstrap-request-id")
	protected String bootstrapRequestId;

	protected ContempDSL store() {
		return (ContempDSL) store;
	}
	
	public PathSegment getId() {
		return pathSegment;
	}

	protected void setId(PathSegment pathSegment) {
		this.pathSegment = pathSegment;
	}

	@Override
	public final List<Resolution> getResolutionPath() {
		return Collections.unmodifiableList(resolutionPath);
	}

	@Override
	public final Map<Resolution.Type, Concept> getResolutions() {
		return unmodifiableResolutions;
	}

	@Override
	public final void setResolution(Resolution.Type type, Concept context) {
		resolutions.put(type, context);
	}

	@Override
	public final Concept getRoot() {
		return resolutions.get(Resolution.StandardType.ROOT);
	}

	@Override
	public final Concept getContext() {
		Concept context = resolutions.get(Resolution.StandardType.CONTEXT);
		return context != null ? context : this.getRoot();
	}

	@Override
	public final Concept getGuardContext() {
		return guardContext;
	}

	@Override
	public final Concept getRelation() {
		return resolutions.get(Resolution.StandardType.RELATION);
	}

	@Override
	public final Concept getTopic() {
		return resolutions.get(Resolution.StandardType.RELATION);
	}

	@Override
	public final Concept getCreated() {
		return resolutions.get(Resolution.StandardType.CREATED);
	}

	public final Map<String, String> getLinkRelations() {
		return linkRels;
	}

	public final void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public final void mapResponder(UUID fromTopic, UUID toTopic) {
		responderOverrides.put(fromTopic, toTopic);
	}

	@Override
	public final UUID mapResponder(UUID fromTopic) {
		return responderOverrides.get(fromTopic);
	}

	@Override
	public final Responder getResponder(UUID topic) {
		if (responderOverrides.containsKey(topic)) {
			return responderMap.get(responderOverrides.get(topic));
		} else {
			return responderMap.get(topic);
		}
	}

	@Override
	public final Responder resolveResponder(Concept context) {
		if (responderOverrides.containsKey(context.getUuid())) {
			UUID override = responderOverrides.get(context.getUuid());
			if (responderMap.containsKey(override)) {
				return responderMap.get(override);
			}
		}
		if (responderMap.containsKey(context.getPredicate())) {
			return responderMap.get(context.getPredicate());
		}
		if (responderMap.containsKey(context.getUuid())) {
			return responderMap.get(context.getUuid());
		}
		Responder agent = null;
		for (UUID agentCandidate = context.getUuid(); agentCandidate != null;) {
			agent = responderMap.get(agentCandidate);
			if (agent != null) {
				break;
			}
			Concept agentCandidateContext = store.getConcept(agentCandidate);
			UUID nextCandidate = agentCandidateContext.getContext();
			if (nextCandidate.equals(agentCandidate)) {
				break;
			}
			agentCandidate = nextCandidate;
		}
		return agent;
	}

	@Override
	public final Set<Guard.GuardContext> resolveGuard(Concept context) {
		Set<Guard.GuardContext> guards = new HashSet<Guard.GuardContext>();
		for (Concept guardCandidate = context; guardCandidate != null;) {
			final Guard localGuard = guardMap.get(guardCandidate.getUuid());
			if (localGuard != null) {
				final Concept guardContext = guardCandidate;
				guards.add(new Guard.GuardContext() {
					@Override
					public Guard getGuard() {
						return localGuard;
					}

					@Override
					public Concept getContext() {
						return guardContext;
					}
				});
			}
			for (Content c : store.getContents(guardCandidate.getUuid())) {
				final Guard guard = guardMap.get(c.getPredicate());
				if (guard != null) {
					final Concept guardContext = guardCandidate;
					guards.add(new Guard.GuardContext() {
						@Override
						public Guard getGuard() {
							return guard;
						}

						@Override
						public Concept getContext() {
							return guardContext;
						}
					});
				}
			}
			if (!guards.isEmpty()) {
				break;
			}
			Concept nextCandidate = store.getConcept(guardCandidate
					.getContext());
			if (nextCandidate.equals(guardCandidate)) {
				break;
			}
			guardCandidate = nextCandidate;
		}
		return guards;
	}

}
