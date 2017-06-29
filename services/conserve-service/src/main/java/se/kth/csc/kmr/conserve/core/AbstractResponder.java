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
package se.kth.csc.kmr.conserve.core;

import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.Responder;

public abstract class AbstractResponder extends AbstractInitializer implements
		Responder {

	@Override
	public Object doGet(Request request) {
		return doRequest(request, null);
	}

	@Override
	public Object doPut(Request request, byte[] data) {
		return doRequest(request, data);
	}

	@Override
	public Object doPost(Request request, byte[] data) {
		return doRequest(request, data);
	}

	@Override
	public Object doDelete(Request request) {
		return doRequest(request, null);
	}

	public Object doRequest(Request request, byte[] data) {
		return null;
	}

	@Override
	public Resolution resolve(Request request) {
		return null;
	}

	@Override
	public void hit(Request request) {
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
	public boolean canGet(Request request) {
		return false;
	}

	@Override
	public boolean canDelete(Request request) {
		return false;
	}

}
