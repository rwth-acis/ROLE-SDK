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
package se.kth.csc.kmr.conserve.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.h2.value.Value;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class H2UUIDType extends AbstractSingleColumnStandardBasicType<UUID> {

	private static final long serialVersionUID = 4094138749298920668L;

	public static final H2UUIDType INSTANCE = new H2UUIDType();

	public H2UUIDType() {
		super(H2UUIDSqlTypeDescriptor.INSTANCE, UUIDTypeDescriptor.INSTANCE);
	}

	public String getName() {
		return "h2-uuid";
	}

	public static class H2UUIDSqlTypeDescriptor implements SqlTypeDescriptor {

		private static final long serialVersionUID = -8452471468449068077L;

		public static final H2UUIDSqlTypeDescriptor INSTANCE = new H2UUIDSqlTypeDescriptor();

		public int getSqlType() {
			return Value.UNKNOWN;
		}

		public <X> ValueBinder<X> getBinder(
				final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new BasicBinder<X>(javaTypeDescriptor, this) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index,
						WrapperOptions options) throws SQLException {
					st.setObject(index, javaTypeDescriptor.unwrap(value,
							UUID.class, options), getSqlType());
				}
			};
		}

		public <X> ValueExtractor<X> getExtractor(
				final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new BasicExtractor<X>(javaTypeDescriptor, this) {
				@Override
				protected X doExtract(ResultSet rs, String name,
						WrapperOptions options) throws SQLException {
					return javaTypeDescriptor.wrap(rs.getObject(name), options);
				}
			};
		}

	}

}
