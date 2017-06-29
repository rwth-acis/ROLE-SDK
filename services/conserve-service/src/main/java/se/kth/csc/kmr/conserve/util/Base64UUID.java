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

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

public class Base64UUID {

	public static String encode(UUID uuid) {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		return Base64.encodeBase64URLSafeString(buffer.array());
	}

	public static String encodeShortened(UUID uuid) {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[8]);
		buffer.putLong(uuid.getMostSignificantBits()
				^ uuid.getLeastSignificantBits());
		return Base64.encodeBase64URLSafeString(buffer.array())
				.substring(0, 10);
	}

	public static UUID decode(String base64) {
		if (base64.length() != 22) {
			throw new IllegalArgumentException(
					"Not a valid Base64 encoded UUID");
		}
		ByteBuffer buffer = ByteBuffer.wrap(Base64.decodeBase64(base64));
		if (buffer.capacity() != 16) {
			throw new IllegalArgumentException(
					"Not a valid Base64 encoded UUID");
		}
		return new UUID(buffer.getLong(), buffer.getLong());
	}

}
