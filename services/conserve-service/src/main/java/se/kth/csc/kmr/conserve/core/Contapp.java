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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Application;
import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Guard;
import se.kth.csc.kmr.conserve.Initializer;
import se.kth.csc.kmr.conserve.Listener;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Responder;
import se.kth.csc.kmr.conserve.data.jpa.JPAConserve;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

@Singleton
public class Contapp implements Application {

	private static Logger log = LoggerFactory.getLogger(Contapp.class);

	@Inject
	private Injector injector;

	@Inject
	private Map<UUID, Responder> responderMap;

	@Inject
	private Map<UUID, Guard> guardMap;

	@Inject
	private Map<UUID, Listener> listenerMap;

	private volatile boolean isSetUp = false;

	private volatile boolean isSettingUp = false;

	@Override
	public boolean isSetUp() {
		return isSetUp;
	}

	@Override
	public boolean isSettingUp() {
		return isSettingUp;
	}

	@Override
	public synchronized void setUp(Request request) {
		if (isSetUp) {
			return;
		}
		isSettingUp = true;
		Contemp conserve = injector.getInstance(Contemp.class);
		((JPAConserve) conserve).replicate();
		Set<Initializer> initializers = injector.getInstance(Key
				.get(new TypeLiteral<Set<Initializer>>() {
				}));
		for (Initializer i : initializers) {
			log.info("Initializing: " + i);
			i.initialize(request);
			conserve.endEntry();
		}
		for (Responder r : responderMap.values()) {
			log.info("Initializing responder: " + r);
			r.initialize(request);
			conserve.endEntry();
		}
		for (Guard g : guardMap.values()) {
			log.info("Initializing guard: " + g);
			g.initialize(request);
			conserve.endEntry();
		}
		for (Listener l : listenerMap.values()) {
			log.info("Initializing listener: " + l);
			l.initialize(request);
			conserve.endEntry();
		}
		isSetUp = true;
		isSettingUp = false;
	}

	@Override
	public UUID getRootUuid(Request request) {
		String baseUri = ((RequestImpl) request).getUriInfo().getBaseUri()
				.toString();
		Contemp conserve = injector.getInstance(Contemp.class);
		Concept domain = conserve.getConcept(conserve.getRootUuid(), baseUri);
		if (domain != null
				&& ConserveTerms.hasService.equals(domain.getPredicate())) {
			return domain.getUuid();
		} else {
			throw new IllegalArgumentException("This domain does not exist: "
					+ baseUri);
		}
	}

	public static MapBinder<UUID, Responder> newResponderBinder(Binder binder) {
		return MapBinder.newMapBinder(binder, UUID.class, Responder.class);
	}

	public static MapBinder<UUID, Guard> newGuardBinder(Binder binder) {
		return MapBinder.newMapBinder(binder, UUID.class, Guard.class);
	}

	public static MapBinder<UUID, Listener> newListenerBinder(Binder binder) {
		return MapBinder.newMapBinder(binder, UUID.class, Listener.class)
				.permitDuplicates();
	}

	public static Multibinder<Initializer> newInitializerBinder(Binder binder) {
		return Multibinder.newSetBinder(binder, Initializer.class);
	}

	public static Multibinder<UUID> newTopicBinder(Binder binder) {
		return Multibinder.newSetBinder(binder, UUID.class,
				Names.named("contemp-global-topics"));
	}

	public static MapBinder<String, String> newNamespaceBinder(Binder binder) {
		return MapBinder.newMapBinder(binder, String.class, String.class,
				Names.named("contemp-namespaces"));
	}

}
