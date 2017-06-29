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

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.UUID;

import javax.persistence.*;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.eclipse.persistence.annotations.Converters;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.Partitioned;

import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.dsl.ContentDSL;

/**
 * The persistent class for the content database table.
 * 
 */
@Entity
@Table(name = "content", uniqueConstraints = { @UniqueConstraint(columnNames = {
		"concept", "predicate", "head" }) })
@IdClass(ContentId.class)
@NamedQueries({
		@NamedQuery(name = "selectContentByContextPredicateEntry", // context+predicate+entry
		query = "SELECT OBJECT(content) FROM ContentEntity content WHERE content.context = :context AND content.predicate = :predicate AND content.entry = :entry"),
		@NamedQuery(name = "selectContentByContextPredicateTimestamp", // context+predicate+timestamp
		query = "SELECT content FROM ContentEntity content WHERE content.context = :context AND content.predicate = :predicate AND content.timestamp <= :timestamp AND (:timestamp < content.expiry OR content.expiry IS NULL)"),
		@NamedQuery(name = "selectContentsByContextTimestamp", // context+timestamp
		query = "SELECT content FROM ContentEntity content WHERE content.context = :context AND content.timestamp <= :timestamp AND (:timestamp < content.expiry OR content.expiry IS NULL)"),
		@NamedQuery(name = "selectContentHeadByContextPredicate", // context+predicate
		query = "SELECT OBJECT(content) FROM ContentEntity content WHERE content.context = :context AND content.predicate = :predicate AND content.head IS NOT NULL"),
		@NamedQuery(name = "updateContentHeadExpiryByContextPredicate", // context+predicate
		query = "UPDATE ContentEntity content SET content.head = NULL, content.expiry = :expiry WHERE content.context = :context AND content.predicate = :predicate AND content.head IS NOT NULL"),
		@NamedQuery(name = "updateContentHeadExpiryByContextPredicateEntry", // context+predicate+entry
		query = "UPDATE ContentEntity content SET content.head = NULL, content.expiry = :expiry WHERE content.context = :context AND content.predicate = :predicate AND content.entry = :entry AND content.head IS NOT NULL") })
@Converters({
		@Converter(name = "UUIDConverter", converterClass = UUIDConverter.class),
		@Converter(name = "BLOBConverter", converterClass = BLOBConverter.class) })
@Cacheable(true)
@Partitioned("contextPartitioning")
public class ContentEntity extends ContentDSL implements Content, Serializable {

	private static final long serialVersionUID = 646260547999574236L;

	@Id
	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexContentContext")
	private UUID context;

	@Id
	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexContentPredicate")
	private UUID predicate;

	@Column(nullable = true, length = 255)
	private String type;

	@Column(nullable = true)
	@Convert("BLOBConverter")
	private Blob data;

	@Id
	@Column(nullable = false)
	@Convert("UUIDConverter")
	@Index(name = "indexContentEntry")
	private UUID entry;

	@Column(nullable = false)
	@Index(name = "indexContentTimestamp")
	private Long timestamp;

	@Column(nullable = true)
	@Index(name = "indexContentExpiry")
	private Long expiry;

	@Column(nullable = true)
	@Index(name = "indexContentHead")
	private Boolean head;

	public ContentEntity() {
		this.expiry = null;
		this.head = true;
	}

	public ContentEntity(Object[] columns) {
		this.context = (UUID) columns[0];
		this.predicate = (UUID) columns[1];
		this.type = (String) columns[2];
		this.entry = (UUID) columns[3];
		this.timestamp = (Long) columns[4];
	}

	public ContentEntity(UUID context, UUID predicate, String type, Blob data,
			UUID entry, Long timestamp) {
		this.context = context;
		this.predicate = predicate;
		this.type = type;
		this.data = data;
		this.entry = entry;
		this.timestamp = timestamp;
		this.expiry = null;
		this.head = true;
	}

	public ContentEntity(UUID context, UUID predicate, UUID entry,
			Long timestamp) {
		this.context = context;
		this.predicate = predicate;
		this.entry = entry;
		this.timestamp = timestamp;
		this.expiry = null;
		this.head = true;
	}

	public ContentEntity(Content content, UUID entry, Long timestamp) {
		this(content, entry, timestamp, false);
	}

	public ContentEntity(Content content, UUID entry, Long timestamp,
			boolean deleted) {
		this.context = content.getContext();
		this.predicate = content.getPredicate();
		this.type = deleted ? null : content.getType();
		this.data = deleted ? null : content.getData();
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
	public void setPredicate(UUID topic) {
		this.predicate = topic;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Blob getData() {
		return data;
	}

	@Override
	public InputStream getDataStream() {
		try {
			return data != null ? data.getBinaryStream() : null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setData(Blob data) {
		this.data = data;
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
		this.head = true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		// result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((entry == null) ? 0 : entry.hashCode());
		result = prime * result
				+ ((predicate == null) ? 0 : predicate.hashCode());
		result = prime * result
				+ ((timestamp == null) ? 0 : timestamp.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ContentEntity other = (ContentEntity) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		// if (data == null) {
		// if (other.data != null)
		// return false;
		// } else if (!data.equals(other.data))
		// return false;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
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
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
