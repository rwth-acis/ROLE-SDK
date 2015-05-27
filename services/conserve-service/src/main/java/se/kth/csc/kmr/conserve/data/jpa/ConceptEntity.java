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

import java.io.Serializable;

import javax.persistence.*;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.eclipse.persistence.annotations.Converters;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Partitioned;
import org.eclipse.persistence.annotations.Partitioning;
import org.eclipse.persistence.annotations.ReplicationPartitioning;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.dsl.ConceptDSL;

import java.util.UUID;

/**
 * The persistent class for the concept database table.
 * 
 */
@Entity
@Table(name = "concept", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uuid", "head" }),
		@UniqueConstraint(columnNames = { "context", "id", "head" }) })
@IdClass(ConceptId.class)
@NamedQueries({
		@NamedQuery(name = "selectConceptByUuid", // uuid+timestamp
		query = "SELECT concept FROM ConceptEntity concept WHERE concept.uuid = :uuid AND concept.timestamp <= :timestamp AND (:timestamp < concept.expiry OR concept.expiry IS NULL)"),
		@NamedQuery(name = "selectConceptByContextUuid", // context+uuid+timestamp
		query = "SELECT concept FROM ConceptEntity concept WHERE concept.context = :context AND concept.uuid = :uuid AND concept.timestamp <= :timestamp AND (:timestamp < concept.expiry OR concept.expiry IS NULL)"),
		@NamedQuery(name = "selectConceptByUuidEntry", // uuid+entry
		query = "SELECT OBJECT(concept) FROM ConceptEntity concept WHERE concept.uuid = :uuid AND concept.entry = :entry"),
		@NamedQuery(name = "selectConceptById", // context+id+timestamp
		query = "SELECT OBJECT(concept) FROM ConceptEntity concept WHERE concept.context = :context AND concept.id = :id AND concept.timestamp <= :timestamp AND (:timestamp < concept.expiry OR concept.expiry IS NULL)"),
		@NamedQuery(name = "selectConceptByPredicateId", // context+predicate+id+timestamp
		query = "SELECT OBJECT(concept) FROM ConceptEntity concept WHERE concept.context = :context AND concept.predicate = :predicate AND concept.id = :id AND concept.timestamp <= :timestamp AND (:timestamp < concept.expiry OR concept.expiry IS NULL)"),
		@NamedQuery(name = "selectConceptsByContext", // context+timestamp
		query = "SELECT OBJECT(concept) FROM ConceptEntity concept WHERE concept.context = :context AND concept.timestamp <= :timestamp AND (:timestamp < concept.expiry OR concept.expiry IS NULL)") })
@Converters({ @Converter(name = "UUIDConverter", converterClass = UUIDConverter.class) })
@Cacheable(true)
@ReplicationPartitioning(name = "replicatationPartitioning")
@Partitioning(name = "contextPartitioning", partitioningClass = ContextPartitioning.class)
@Partitioned("contextPartitioning")
public class ConceptEntity extends ConceptDSL implements Concept, Serializable {

	private static final long serialVersionUID = -7315713701643523892L;

	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexConceptContext")
	private UUID context;

	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexConceptPredicate")
	private UUID predicate;

	@Id
	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexConceptUuid")
	private UUID uuid;

	@Column(nullable = true, length = 8192)
	@Index(name = "indexConceptId")
	private String id;

	@Id
	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexConceptEntry")
	private UUID entry;

	@Column(nullable = false)
	@Index(name = "indexConceptTimestamp")
	private Long timestamp;

	@Column(nullable = true)
	@Index(name = "indexConceptExpiry")
	private Long expiry;

	@Column(nullable = true)
	@Index(name = "indexConceptHead")
	private Boolean head;

	public ConceptEntity() {
		this.expiry = null;
		this.head = true;
	}

	public ConceptEntity(UUID context, UUID predicate, UUID uuid, String id,
			UUID entry, Long timestamp) {
		this.context = context;
		this.predicate = predicate;
		this.uuid = uuid;
		this.id = id;
		this.entry = entry;
		this.timestamp = timestamp;
		this.expiry = null;
		this.head = true;
	}

	public ConceptEntity(Concept concept, UUID entry, Long timestamp) {
		this(concept, entry, timestamp, false);
	}

	public ConceptEntity(Concept concept, UUID entry, Long timestamp,
			boolean deleted) {
		this.context = concept.getContext();
		this.predicate = concept.getPredicate();
		this.uuid = concept.getUuid();
		this.id = concept.getId();
		this.entry = entry;
		this.timestamp = timestamp;
		this.expiry = deleted ? timestamp : null;
		this.head = deleted ? null : true;
	}

	@Override
	public UUID getContext() {
		return context;
	}

	@Override
	public void setContext(UUID context) {
		this.context = context;
	}

	@Override
	public UUID getPredicate() {
		return predicate;
	}

	@Override
	public void setPredicate(UUID relation) {
		this.predicate = relation;
	}

	@Override
	public UUID getUuid() {
		return uuid;
	}

	@Override
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public UUID getEntry() {
		return entry;
	}

	@Override
	public void setEntry(UUID entry) {
		this.entry = entry;
	}

	@Override
	public Long getTimestamp() {
		return timestamp;
	}

	@Override
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getExpiry() {
		return expiry;
	}

	public void setExpiry(Long expiry) {
		this.expiry = expiry;
	}

	public Boolean getHead() {
		return head;
	}

	public void setHead(Boolean head) {
		this.head = head;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((entry == null) ? 0 : entry.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((predicate == null) ? 0 : predicate.hashCode());
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConceptEntity other = (ConceptEntity) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (!predicate.equals(other.predicate))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

}
