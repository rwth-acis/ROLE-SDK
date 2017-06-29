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
package se.kth.csc.kmr.conserve.logic;

import java.util.UUID;

import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractGuard;

public final class StaticGuard extends AbstractGuard {

	private final boolean allowGet;

	private final boolean allowPut;

	private final boolean allowPost;

	private final boolean allowDelete;

	public StaticGuard(boolean allowGet, boolean allowPut, boolean allowPost,
			boolean allowDelete, UUID... contexts) {
		this.allowGet = allowGet;
		this.allowPut = allowPut;
		this.allowPost = allowPost;
		this.allowDelete = allowDelete;
	}

	@Override
	public boolean canGet(Request request) {
		return allowGet;
	}

	@Override
	public boolean canPut(Request request) {
		return allowPut;
	}

	@Override
	public boolean canPost(Request request) {
		return allowPost;
	}

	@Override
	public boolean canDelete(Request request) {
		return allowDelete;
	}

}
