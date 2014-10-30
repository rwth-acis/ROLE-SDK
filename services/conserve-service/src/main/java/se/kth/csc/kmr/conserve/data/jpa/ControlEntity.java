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
import java.util.UUID;

import javax.persistence.*;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.eclipse.persistence.annotations.Converters;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Partitioned;

import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.dsl.ControlDSL;

/**
 * The persistent class for the control database table.
 * 
 */
@Entity
@Table(name = "control", uniqueConstraints = { @UniqueConstraint(columnNames = {
		"context", "predicate", "object", "head" }) })
@IdClass(ControlId.class)
@NamedQueries({
		@NamedQuery(name = "updateControlUriByObject", // uri+object
		query = "UPDATE ControlEntity control SET control.uri = :uri WHERE control.object = :object"),
		@NamedQuery(name = "selectControlByContextPredicatePredicate", // context+predicate+predicate+timestamp
		query = "SELECT OBJECT(two) FROM ControlEntity two WHERE two.object IN (SELECT one.object FROM ControlEntity one WHERE one.context = :context AND one.predicate = :predicate1 AND one.timestamp < :timestamp AND (:timestamp < one.expiry OR one.expiry IS NULL)) AND two.predicate = :predicate2 AND two.timestamp < :timestamp AND (:timestamp < two.expiry OR two.expiry IS NULL)") })
@Converters({ @Converter(name = "UUIDConverter", converterClass = UUIDConverter.class) })
@Cacheable(true)
@Partitioned("contextPartitioning")
public class ControlEntity extends ControlDSL implements Control, Serializable {

	private static final long serialVersionUID = -7276618086298320322L;

	@Id
	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexControlContext")
	private UUID context;

	@Id
	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexControlPredicate")
	private UUID predicate;

	@Id
	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexControlObject")
	private UUID object;

	@Column(nullable = true, length = 8192)
	private String uri;

	@Id
	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexControlEntry")
	private UUID entry;

	@Column(nullable = false)
	@Index(name = "indexControlTimestamp")
	private Long timestamp;

	@Column(nullable = true)
	@Index(name = "indexControlExpiry")
	private Long expiry;

	@Column(nullable = true)
	@Index(name = "indexControlHead")
	private Boolean head;

	public ControlEntity() {
		this.expiry = null;
		this.head = true;
	}

	public ControlEntity(UUID context, UUID predicate, UUID object, String uri,
			UUID entry, Long timestamp) {
		this.context = context;
		this.predicate = predicate;
		this.object = object;
		this.uri = uri;
		this.entry = entry;
		this.timestamp = timestamp;
		this.expiry = null;
		this.head = true;
	}

	public ControlEntity(Control control, UUID entry, Long timestamp) {
		this(control, entry, timestamp, false);
	}

	public ControlEntity(Control control, UUID entry, Long timestamp,
			boolean deleted) {
		this.context = control.getContext();
		this.predicate = control.getPredicate();
		this.object = control.getObject();
		this.uri = control.getUri();
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
	public void setPredicate(UUID predicate) {
		this.predicate = predicate;
	}

	@Override
	public UUID getObject() {
		return object;
	}

	@Override
	public void setObject(UUID object) {
		this.object = object;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public void setUri(String uri) {
		this.uri = uri;
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
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result
				+ ((predicate == null) ? 0 : predicate.hashCode());
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		ControlEntity other = (ControlEntity) obj;
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
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
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
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

}
