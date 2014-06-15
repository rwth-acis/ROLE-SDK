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

import javax.ws.rs.core.Response;

import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.core.AbstractResponder;

public class SecretResponder extends AbstractResponder {

	@Override
	public boolean canGet(Request request) {
		return false;
	}

	@Override
	public boolean canPut(Request request) {
		return false;
	}

	@Override
	public boolean canPost(Request request) {
		return false;
	}

	@Override
	public boolean canDelete(Request request) {
		return false;
	}

	@Override
	public Object doGet(Request request) {
		return Response.status(Response.Status.FORBIDDEN)
				.header("Cache-Control", "no-store").build();
	}

	@Override
	public Object doPut(Request request, byte[] data) {
		return Response.status(Response.Status.FORBIDDEN)
				.header("Cache-Control", "no-store").build();
	}

	@Override
	public Object doPost(Request request, byte[] data) {
		return Response.status(Response.Status.FORBIDDEN)
				.header("Cache-Control", "no-store").build();
	}

	@Override
	public Object doDelete(Request request) {
		return Response.status(Response.Status.FORBIDDEN)
				.header("Cache-Control", "no-store").build();
	}

	@Override
	public Resolution resolve(Request request) {
		return null;
	}

}
