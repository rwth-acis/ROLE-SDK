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

import java.util.UUID;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;
import org.hsqldb.types.Types;

public class HSQLUUIDType extends AbstractSingleColumnStandardBasicType<UUID> {

	private static final long serialVersionUID = 4094138749298920668L;

	public static final HSQLUUIDType INSTANCE = new HSQLUUIDType();

	public HSQLUUIDType() {
		super(HSQLUUIDSqlTypeDescriptor.INSTANCE, UUIDTypeDescriptor.INSTANCE);
	}

	public String getName() {
		return "hsql-uuid";
	}

	public static class HSQLUUIDSqlTypeDescriptor extends BinaryTypeDescriptor {

		private static final long serialVersionUID = 4238606390302682071L;

		public static final HSQLUUIDSqlTypeDescriptor INSTANCE = new HSQLUUIDSqlTypeDescriptor();

		public int getSqlType() {
			return Types.SQL_GUID;
		}

	}

}
