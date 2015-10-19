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

import se.kth.csc.kmr.conserve.Application;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Initializer;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.dsl.ContempDSL;

import com.google.inject.Inject;

public abstract class AbstractInitializer implements Initializer {

	@Inject
	protected Application app;

	@Inject
	protected Contemp store;

	@Override
	public void initialize(Request request) {
		initialize();
	}

	public void initialize() {
	}

	protected final ContempDSL store() {
		return (ContempDSL) store;
	}

}
