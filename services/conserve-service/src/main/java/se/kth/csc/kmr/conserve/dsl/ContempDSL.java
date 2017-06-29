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
package se.kth.csc.kmr.conserve.dsl;

import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.BaseRequest;

public abstract class ContempDSL {

	@Inject
	private Injector injector;

	private final Contemp impl;

	public ContempDSL() {
		if (!(this instanceof Contemp)) {
			throw new IllegalStateException();
		}
		impl = (Contemp) this;
	}

	public final ConceptDSL in(UUID context) {
		ConceptDSL cont = (ConceptDSL) impl.loadConcept(context);
		if (cont == null) {
			throw new IllegalArgumentException(
					"The context could not be loaded");
		}
		injector.injectMembers(cont);
		return cont;
	}

	public final ConceptDSL in(Concept context) {
		injector.injectMembers(context);
		return (ConceptDSL) context;
	}

	public final ContentDSL as(Content content) {
		injector.injectMembers(content);
		return (ContentDSL) content;
	}

	public final ControlDSL control(Control control) {
		injector.injectMembers(control);
		return (ControlDSL) control;
	}

	public final Concept get(UUID uuid) {
		Concept context = impl.getConcept(uuid);
		if (context != null) {
			injector.injectMembers(context);
		}
		return context;
	}

	public final Concept require(UUID uuid) {
		Concept context = impl.getConcept(uuid);
		if (context == null) {
			throw new IllegalArgumentException(
					"The context could not be loaded");
		}
		injector.injectMembers(context);
		return context;
	}

	public Concept resolve(String uri) {
		BaseRequest request = injector.getInstance(BaseRequest.class);
		return request.resolve(uri);
	}

}
