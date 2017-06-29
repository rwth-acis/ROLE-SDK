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
package se.kth.csc.kmr.conserve.data.jpa;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.dsl.ContempDSL;
import se.kth.csc.kmr.conserve.util.Base64UUID;

public class JPAConserve extends ContempDSL implements Contemp {

	private static Logger log = LoggerFactory.getLogger(JPAConserve.class);

	@Inject
	private EntityManager entityManager;

	private UUID root = null;

	private UUID entry = null;

	private long timestamp = new Date().getTime();

	private final RandomBasedGenerator uuidGenerator = Generators
			.randomBasedGenerator();

	private boolean isReplicating = false;

	@Override
	public final ContempDSL query() {
		return this;
	}

	@Override
	public void disconnect() {
		if (entityManager != null) {
			try {
				if (entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().commit();
				}
			} catch (RuntimeException e) {
				log.error("Persistence error", e);
				throw e;
			} finally {
				//entityManager.close();
				//entityManager = null;
			}
		}
	}

	@Override
	public void rollback() {
		entry = null;
		if (entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().rollback();
		}
	}

	private synchronized void beginEntry() {
		assert entry == null;
		entityManager.getTransaction().begin();
		entry = uuidGenerator.generate();
		timestamp = new Date().getTime();
		// putConcept(createConcept(getRootUuid(), ConserveTerms.hasEntry,
		// entry));
	}

	@Override
	public synchronized void endEntry() {
		if (entry != null) {
			entry = null;
			timestamp = new Date().getTime();
			try {
				if (entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().commit();
				}
			} catch (RuntimeException e) {
				log.error("Persistence error", e);
				throw e;
			}
		}
	}

	public void replicate() {
		isReplicating = true;
	}

	public Blob createData() {
		Connection connection = entityManager.unwrap(Connection.class);
		try {
			return connection.createBlob();
		} catch (SQLException e) {
			log.error("Blob creation error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public UUID getRootUuid() {
		if (root != null) {
			return root;
		}
		List<ConceptEntity> conceptEntities = entityManager
				.createQuery(
						"SELECT concept FROM ConceptEntity concept WHERE concept.predicate = :predicate",
						ConceptEntity.class)
				.setParameter("predicate", ConserveTerms.root).getResultList();
		if (conceptEntities.size() > 0) {
			root = conceptEntities.get(0).getUuid();
			if (isReplicating) {
				ContextPartitioning.replicate(root);
			}
			return root;
		} else {
			log.info("Root not found; initializing...");
			if (entry != null) {
				throw new IllegalStateException(
						"Transaction already started before initializing root");
			}
			root = uuidGenerator.generate();
			beginEntry();
			putConcept(new ConceptEntity(root, ConserveTerms.root, root, null,
					entry, timestamp));
			endEntry();
			log.info("Created a root with UUID " + root + "/"
					+ Base64UUID.encode(root));
			return root;
		}
	}

	@Override
	public synchronized Concept createConcept(UUID context, UUID predicate) {
		return createConcept(context, predicate, uuidGenerator.generate());
	}

	@Override
	public synchronized Concept createConcept(UUID context, UUID predicate,
			UUID uuid) {
		if (entry == null) {
			beginEntry();
		}
		Concept c = new ConceptEntity(context, predicate, uuid, null, entry,
				timestamp);
		return c;
	}

	@Override
	public synchronized void putConcept(Concept concept) {
		try {
			if (entry == null) {
				beginEntry();
			}
			if (isReplicating) {
				ContextPartitioning.replicate(concept.getUuid());
			}
			if (entry.equals(concept.getEntry())) {
				entityManager.persist(concept);
			} else {
				ConceptEntity existing = entityManager
						.createNamedQuery("selectConceptByUuidEntry",
								ConceptEntity.class)
						.setParameter("uuid", concept.getUuid())
						.setParameter("entry", concept.getEntry())
						.getSingleResult();
				if (existing.getHead() == null) {
					entry = null;
					if (entityManager.getTransaction().isActive()) {
						entityManager.getTransaction().rollback();
					}
					throw new IllegalArgumentException("Wrong version");
				}
				existing.setExpiry(timestamp);
				existing.setHead(null);
				entityManager.merge(existing);
				entityManager.persist(new ConceptEntity(concept, entry,
						timestamp));
			}
			entityManager.createNamedQuery("updateControlUriByObject")
					.setParameter("uri", concept.getId())
					.setParameter("object", concept.getUuid()).executeUpdate();
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public void deleteConcept(Concept concept) {
		try {
			if (entry == null) {
				beginEntry();
			}
			ConceptEntity existing = entityManager
					.createNamedQuery("selectConceptByUuidEntry",
							ConceptEntity.class)
					.setParameter("uuid", concept.getUuid())
					.setParameter("entry", concept.getEntry())
					.getSingleResult();
			if (existing.getHead() == null) {
				entry = null;
				if (entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().rollback();
				}
				throw new IllegalArgumentException("Wrong version");
			}
			existing.setExpiry(timestamp);
			existing.setHead(null);
			entityManager.merge(existing);
			entityManager.persist(new ConceptEntity(concept, entry, timestamp,
					true));
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public Concept loadConcept(UUID uuid) {
		ConceptEntity concept = new ConceptEntity();
		concept.setUuid(uuid);
		return concept;
	}

	@Override
	public Concept getConcept(UUID uuid) {
		try {
			List<ConceptEntity> conceptEntities = entityManager
					.createNamedQuery("selectConceptByUuid",
							ConceptEntity.class).setParameter("uuid", uuid)
					.setParameter("timestamp", timestamp).getResultList();
			return conceptEntities.size() == 0 ? null : conceptEntities.get(0);
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public Concept getConcept(UUID context, UUID uuid) {
		try {
			if (isReplicating) {
				ContextPartitioning.replicate(context);
			}
			try {
				ConceptEntity conceptImpl = entityManager
						.createNamedQuery("selectConceptByContextUuid",
								ConceptEntity.class)
						.setParameter("context", context)
						.setParameter("uuid", uuid)
						.setParameter("timestamp", timestamp).getSingleResult();
				return conceptImpl;
			} catch (NoResultException e) {
				return null;
			}
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public Concept getConcept(UUID context, String id) {
		try {
			if (isReplicating) {
				ContextPartitioning.replicate(context);
			}
			try {
				ConceptEntity conceptImpl = entityManager
						.createNamedQuery("selectConceptById",
								ConceptEntity.class)
						.setParameter("context", context)
						.setParameter("id", id)
						.setParameter("timestamp", timestamp).getSingleResult();
				return conceptImpl;
			} catch (NoResultException e) {
				return null;
			}
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public Concept getConcept(UUID context, UUID predicate, String id) {
		try {
			if (isReplicating) {
				ContextPartitioning.replicate(context);
			}
			try {
				ConceptEntity conceptImpl = entityManager
						.createNamedQuery("selectConceptByPredicateId",
								ConceptEntity.class)
						.setParameter("context", context)
						.setParameter("predicate", predicate)
						.setParameter("id", id)
						.setParameter("timestamp", timestamp).getSingleResult();
				return conceptImpl;
			} catch (NoResultException e) {
				return null;
			}
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public Content createContent(UUID context, UUID predicate) {
		if (entry == null)
			beginEntry();
		return new ContentEntity(context, predicate, "", createData(), entry,
				timestamp);
	}

	@Override
	public void putContent(Content content) {
		try {
			if (entry == null) {
				beginEntry();
			}
			if (entry.equals(content.getEntry())) {
				entityManager
						.createNamedQuery(
								"updateContentHeadExpiryByContextPredicate",
								ConceptEntity.class)
						.setParameter("context", content.getContext())
						.setParameter("predicate", content.getPredicate())
						.setParameter("expiry", timestamp).executeUpdate();
				entityManager.persist(content);
			} else {
				int updated = entityManager
						.createNamedQuery(
								"updateContentHeadExpiryByContextPredicateEntry",
								ContentEntity.class)
						.setParameter("context", content.getContext())
						.setParameter("predicate", content.getPredicate())
						.setParameter("entry", content.getEntry())
						.setParameter("expiry", timestamp).executeUpdate();
				if (updated != 1) {
					entry = null;
					if (entityManager.getTransaction().isActive()) {
						entityManager.getTransaction().rollback();
					}
					throw new IllegalArgumentException("Wrong version");
				}
				entityManager.persist(new ContentEntity(content, entry,
						timestamp));
			}
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public void deleteContent(Content content) {
		throw new RuntimeException("Deleting content!");
		// try {
		// if (entry == null) {
		// beginEntry();
		// }
		// ContentEntity existing = entityManager
		// .createNamedQuery("selectContentByContextPredicateEntry",
		// ContentEntity.class)
		// .setParameter("context", content.getContext())
		// .setParameter("predicate", content.getPredicate())
		// .setParameter("entry", content.getEntry())
		// .getSingleResult();
		// if (existing.getHead() == null) {
		// if (entityManager.getTransaction().isActive()) {
		// entityManager.getTransaction().rollback();
		// }
		// throw new IllegalArgumentException("Wrong version");
		// }
		// existing.setExpiry(timestamp);
		// existing.setHead(null);
		// entityManager.persist(new ContentEntity(content, entry, timestamp,
		// true));
		// } catch (RuntimeException e) {
		// log.error("Persistence error", e);
		// entry = null;
		// if (entityManager.getTransaction().isActive()) {
		// entityManager.getTransaction().rollback();
		// }
		// throw e;
		// }
	}

	@Override
	public Content getContent(UUID context, UUID predicate) {
		try {
			try {
				ContentEntity contentEntity = entityManager
						.createNamedQuery(
								"selectContentByContextPredicateTimestamp",
								ContentEntity.class)
						.setParameter("context", context)
						.setParameter("predicate", predicate)
						.setParameter("timestamp", timestamp).getSingleResult();
				return contentEntity;
			} catch (NoResultException e) {
				return null;
			}
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public Content loadContent(UUID context, UUID predicate) {
		ContentEntity c = new ContentEntity();
		c.setContext(context);
		c.setPredicate(predicate);
		return c;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Concept> getConcepts(UUID context) {
		try {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<ConceptEntity> query = builder
					.createQuery(ConceptEntity.class);
			Root<ConceptEntity> concept = query.from(ConceptEntity.class);
			query.where(builder.and(
					builder.equal(concept.get("context"), context),
					builder.isNotNull(concept.get("head"))));
			List<ConceptEntity> conceptImpl = entityManager.createQuery(query)
					.getResultList();
			return (List<Concept>) (List<?>) conceptImpl;
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Content> getContents(UUID context) {
		try {
			try {
				List<ContentEntity> contentEntities = entityManager
						.createNamedQuery("selectContentsByContextTimestamp",
								ContentEntity.class)
						.setParameter("context", context)
						.setParameter("timestamp", timestamp).getResultList();
				return (List<Content>) (List<?>) contentEntities;
			} catch (NoResultException e) {
				return null;
			}
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public Control createControl(UUID context, UUID predicate, UUID object,
			String uri) {
		if (entry == null)
			beginEntry();
		return new ControlEntity(context, predicate, object, uri, entry,
				timestamp);
	}

	@Override
	public Control getControl(UUID context, UUID predicate, UUID object) {
		try {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<ControlEntity> query = builder
					.createQuery(ControlEntity.class);
			Root<ControlEntity> control = query.from(ControlEntity.class);
			query.where(builder.and(
					builder.equal(control.get("context"), context),
					builder.equal(control.get("predicate"), predicate),
					builder.equal(control.get("object"), object),
					builder.isNotNull(control.get("head"))));
			try {
				ControlEntity controlImpl = entityManager.createQuery(query)
						.getSingleResult();
				return controlImpl;
			} catch (NoResultException e) {
				return null;
			}
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public List<Control> getControls(UUID context) {
		return getControls(context, null, null);
	}

	@Override
	public List<Control> getControls(UUID context, UUID predicate) {
		return getControls(context, predicate, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Control> getControls(UUID context, UUID predicate, UUID object) {
		try {
			int queryIndex = (context != null ? 1 : 0)
					| (predicate != null ? 2 : 0) | (object != null ? 4 : 0);
			log.info("CONTROLS QUERY INDEX:" + queryIndex);

			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<ControlEntity> query = builder
					.createQuery(ControlEntity.class);
			Root<ControlEntity> control = query.from(ControlEntity.class);
			List<Predicate> predicates = new ArrayList<Predicate>(4);
			if (context != null) {
				predicates.add(builder.equal(control.get("context"), context));
			}
			if (predicate != null) {
				predicates.add(builder.equal(control.get("predicate"),
						predicate));
			}
			if (object != null) {
				predicates.add(builder.equal(control.get("object"), object));
			}
			predicates.add(builder.isNotNull(control.get("head")));
			query.where(builder.and(predicates.toArray(new Predicate[predicates
					.size()])));
			try {
				List<ControlEntity> controlImpl = entityManager.createQuery(
						query).getResultList();
				return (List<Control>) (List<?>) controlImpl;
			} catch (NoResultException e) {
				return null;
			}
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Control> getControls(UUID context1, UUID predicate1,
			UUID predicate2, UUID object) {
		try {
			List<ControlEntity> list = entityManager
					.createNamedQuery(
							"selectControlByContextPredicatePredicate",
							ControlEntity.class)
					.setParameter("context", context1)
					.setParameter("predicate1", predicate1)
					.setParameter("predicate2", predicate2)
					.setParameter("timestamp", timestamp).getResultList();
			return (List<Control>) (List<?>) list;
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public void putControl(Control control) {
		try {
			if (entry == null) {
				beginEntry();
			}
			Control o = getControl(control.getContext(),
					control.getPredicate(), control.getObject());
			if (o instanceof ControlEntity) {
				return;
			}
			entityManager.persist(new ControlEntity(control, entry, timestamp));
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

	@Override
	public void deleteControl(Control control) {
		try {
			if (entry == null) {
				beginEntry();
			}
			Control o = getControl(control.getContext(),
					control.getPredicate(), control.getObject());
			if (o == null) {
				entry = null;
				if (entityManager.getTransaction().isActive()) {
					entityManager.getTransaction().rollback();
				}
				throw new IllegalArgumentException("Wrong version");
			}
			entityManager.persist(new ControlEntity(o, entry, timestamp, true));
		} catch (RuntimeException e) {
			log.error("Persistence error", e);
			entry = null;
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			throw e;
		}
	}

}
