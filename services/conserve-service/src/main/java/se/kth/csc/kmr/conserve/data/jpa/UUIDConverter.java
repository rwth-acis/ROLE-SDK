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

import java.util.UUID;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.mappings.foundation.AbstractDirectMapping;
import org.eclipse.persistence.sessions.Session;
import org.h2.value.Value;

public class UUIDConverter implements Converter {

	private static final long serialVersionUID = 6627248728831810529L;

	@Override
	public Object convertObjectValueToDataValue(Object objectValue,
			Session session) {
		return objectValue;
		// UUID uuid = (UUID) objectValue;
		// ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
		// buffer.putLong(uuid.getMostSignificantBits());
		// buffer.putLong(uuid.getLeastSignificantBits());
		// return buffer.array();
	}

	@Override
	public Object convertDataValueToObjectValue(Object dataValue,
			Session session) {
		return (UUID) dataValue;
		// if (dataValue instanceof UUID) {
		// return (UUID) dataValue;
		// } else if (dataValue instanceof byte[]) {
		// byte[] uuid = (byte[]) dataValue;
		// ByteBuffer buffer = ByteBuffer.wrap(uuid);
		// return new UUID(buffer.getLong(), buffer.getLong());
		// }
		// return UUID.fromString((String) dataValue);
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {
		AbstractDirectMapping directMapping = (AbstractDirectMapping) mapping;
		directMapping.getField().setSqlType(Value.UUID);
		directMapping.getField().setColumnDefinition("UUID(16) NOT NULL");
	}

}
