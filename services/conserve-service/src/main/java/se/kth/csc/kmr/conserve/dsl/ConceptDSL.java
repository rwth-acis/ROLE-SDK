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
package se.kth.csc.kmr.conserve.dsl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.RequestHandle;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.util.Base64UUID;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.google.inject.Inject;
import com.google.inject.Injector;

public abstract class ConceptDSL {

	@Inject
	private se.kth.csc.kmr.conserve.Contemp store;

	@Inject
	private Injector injector;

	private final Concept impl;

	public ConceptDSL() {
		if (!(this instanceof Concept)) {
			throw new IllegalStateException();
		}
		impl = (Concept) this;
	}

	public final Relation sub() {
		return new Relation(ConserveTerms.hasPart);
	}

	public final Relation sub(UUID relation) {
		return new Relation(relation);
	}

	public final Relation sub(URI relation) {
		NameBasedGenerator generator = Generators
				.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL);
		return new Relation(generator.generate(relation.toString()));
	}

	public final class Relation {

		private final UUID relation;

		Relation(UUID relation) {
			this.relation = relation;
		}

		public Concept create() {
			return create(null);
		}

		public Concept create(String id) {
			Concept context = store.createConcept(impl.getUuid(), relation);
			context.setId(id != null ? id : Base64UUID.encodeShortened(context
					.getUuid()));
			store.putConcept(context);
			injector.injectMembers(context);
			return context;
		}

		public Concept acquire(UUID uuid) {
			Concept context = store.getConcept(impl.getUuid(), uuid);
			if (context == null) {
				context = store.createConcept(impl.getUuid(), relation, uuid);
				context.setId(Base64UUID.encodeShortened(uuid));
				store.putConcept(context);
			} else {
				// TODO: Verify parent, relation, alias
			}
			injector.injectMembers(context);
			return context;
		}

		public Concept acquire(URI uri) {
			NameBasedGenerator generator = Generators
					.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL);
			UUID uuid = generator.generate(uri.toString());
			Concept context = store.getConcept(impl.getUuid(), uuid);
			if (context == null) {
				context = store.createConcept(impl.getUuid(), relation, uuid);
				context.setId(uri.toString());
				store.putConcept(context);
			} else {
				// TODO: Verify parent, relation, alias
			}
			injector.injectMembers(context);
			return context;
		}

		public Concept acquire(String id) {
			Concept context = store.getConcept(impl.getUuid(), id);
			if (context == null) {
				context = store.createConcept(impl.getUuid(), relation);
				context.setId(id);
				store.putConcept(context);
			} else {
				// TODO: Verify parent, relation, alias
			}
			injector.injectMembers(context);
			return context;
		}

		public Concept acquire(UUID uuid, String id) {
			Concept context = store.getConcept(impl.getUuid(), uuid);
			if (context == null) {
				context = store.createConcept(impl.getUuid(), relation, uuid);
				context.setId(id);
				store.putConcept(context);
			} else {
				// TODO: Verify parent, relation, alias
			}
			injector.injectMembers(context);
			return context;
		}

		public Concept require(UUID uuid) {
			Concept context = store.getConcept(impl.getUuid(), uuid);
			if (context == null) {
				throw new IllegalArgumentException(
						"The subcontext does not exist");
			}
			injector.injectMembers(context);
			return context;
		}

		public Concept require(String id) {
			Concept context = store.getConcept(impl.getUuid(), id);
			if (context == null) {
				throw new IllegalArgumentException(
						"The subcontext does not exist");
			}
			injector.injectMembers(context);
			return context;
		}

		public Concept get(UUID uuid) {
			Concept context = store.getConcept(impl.getUuid(), uuid);
			if (context != null) {
				injector.injectMembers(context);
			}
			return context;
		}

		public Concept get(String id) {
			Concept context = store.getConcept(impl.getUuid(), id);
			if (context != null) {
				injector.injectMembers(context);
			}
			return context;
		}

		public List<Concept> list() {
			return store.getConcepts(impl.getUuid());
		}

	}

	public final Topic as(UUID topic) {
		return new Topic(topic);
	}

	public final class Topic {

		private final UUID topic;

		Topic(UUID topic) {
			this.topic = topic;
		}

		public ContentDSL type(String mediaType) {
			// Content content = store.getContent(impl.getUuid(), topic);
			// if (content == null) {
			Content content = store.createContent(impl.getUuid(), topic);
			// }
			content.setType(mediaType);
			injector.injectMembers(content);
			((ContentDSL) content).context = impl;
			return (ContentDSL) content;
		}

		public Content require() {
			Content content = store.getContent(impl.getUuid(), topic);
			if (content == null) {
				throw new IllegalArgumentException(
						"The content does not exist: Context: "
								+ impl.getUuid() + " Predicate: " + topic);
			}
			injector.injectMembers(content);
			((ContentDSL) content).context = impl;
			return content;
		}

		public Content get() {
			Content content = store.getContent(impl.getUuid(), topic);
			if (content != null) {
				injector.injectMembers(content);
				((ContentDSL) content).context = impl;
			}
			return content;
		}

	}

	public void put(UUID predicate, UUID object) {
		// Concept obj = store.getConcept(object);
		// String uri = null;
		// if (obj != null && store.getRootUuid().equals(obj.getContext())) {
		// uri = obj.getId();
		// try {
		// if (!new URI(uri).isAbsolute()) {
		// uri = null;
		// }
		// } catch (URISyntaxException e) {
		// uri = null;
		// }
		// }
		// if (uri == null) {
		// if (obj != null) {
		// LinkedList<Concept> path = new LinkedList<Concept>();
		// for (Concept o = obj; !ConserveTerms.root.equals(o
		// .getPredicate()); o = store.getConcept(o.getContext())) {
		// path.addFirst(o);
		// }
		// StringBuffer uriBuffer = null;
		// boolean addSlashNextTime = false;
		// for (Concept o : path) {
		// if (uriBuffer == null) {
		// uriBuffer = new StringBuffer();
		// uriBuffer.append(o.getId());
		// } else {
		// if (addSlashNextTime) {
		// uriBuffer.append('/');
		// } else {
		// addSlashNextTime = true;
		// }
		// String id = o.getId();
		// if (id.contains(":") || id.contains("/")) {
		// id = Base64UUID.encode(o.getUuid());
		// }
		// uriBuffer.append(id);
		// }
		// }
		// uri = uriBuffer.toString();
		// }
		// if (uri != null) {
		// try {
		// if (!new URI(uri).isAbsolute()) {
		// uri = null;
		// }
		// } catch (URISyntaxException e) {
		// uri = null;
		// }
		// }
		// if (uri == null) {
		// uri = "urn:uuid:" + object;
		// }
		// }
		// String uri = "urn:uuid:" + object;
		store.putControl(store.createControl(impl.getUuid(), predicate, object,
				null));
	}

	public void put(UUID predicate, String objectUri) {
		UUID object = Generators.nameBasedGenerator(
				NameBasedGenerator.NAMESPACE_URL).generate(objectUri);
		store.putControl(store.createControl(impl.getUuid(), predicate, object,
				objectUri));
	}

	public List<Control> get(UUID predicate) {
		return store.getControls(impl.getUuid(), predicate, null);
	}

	public boolean ask(UUID predicate, UUID object) {
		return store.getControl(impl.getUuid(), predicate, object) != null;
	}

	public UriBuilder uriBuilder() {
		Concept c = impl;
		LinkedList<String> segments = new LinkedList<String>();
		LinkedList<UUID> uuids = new LinkedList<UUID>();
		UriBuilder ub = null;
		while (!c.getUuid().equals(c.getContext())) {
			if (ConserveTerms.hasService.equals(c.getPredicate())) {
				ub = UriBuilder.fromUri(c.getId());
				break;
			}
			if (c.getId() != null) {
				segments.addFirst(c.getId());
				uuids.addFirst(c.getUuid());
				c = store.getConcept(c.getContext());
			} else {
				break;
			}
		}
		if (ub == null) {
			RequestHandle requestHandle = injector
					.getInstance(RequestHandle.class);
			Request request = requestHandle.getRequest();
			ub = ((RequestImpl) request).getUriInfo().getBaseUriBuilder()
					.path(Base64UUID.encode(c.getUuid()));
		}
		Iterator<UUID> uuidIter = uuids.iterator();
		for (String segment : segments) {
			UUID uuid = uuidIter.next();
			if (!segment.contains(":") && !segment.contains("/")) {
				ub.path(percentEncode(segment));
			} else {
				ub.path(percentEncode(Base64UUID.encode(uuid)));
			}
		}
		return ub;
	}

	public URI uri() {
		return uriBuilder().build();
	}

	private static String percentEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}