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

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Property;
import se.kth.csc.kmr.conserve.Type;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

public abstract class Terms extends AbstractInitializer {

	private static Logger log = LoggerFactory.getLogger(Terms.class);

	private final Map<UUID, URI> uuidUriMap = new HashMap<UUID, URI>();

	private final Map<URI, UUID> uriUuidMap = new HashMap<URI, UUID>();

	private final Set<UUID> uuidSet = Collections.unmodifiableSet(uuidUriMap
			.keySet());

	private final Set<URI> uriSet = Collections.unmodifiableSet(uriUuidMap
			.keySet());

	private final Terms instance;

	public Terms(Terms instance) {
		this.instance = instance;
	}

	protected final UUID uuid(String uri) {
		NameBasedGenerator uuidGenerator = Generators
				.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL);
		URI uriObj = URI.create(uri);
		UUID uuid = uuidGenerator.generate(uri);
		if (uuidUriMap.containsKey(uuid) || uriUuidMap.containsKey(uriObj)) {
			throw new IllegalStateException("Duplicate UUIDs/URIs");
		}
		uuidUriMap.put(uuid, uriObj);
		uriUuidMap.put(uriObj, uuid);
		return uuid;
	}

	public final URI uri(UUID uuid) {
		return uuidUriMap.get(uuid);
	}

	public final UUID uuid(URI uri) {
		return uriUuidMap.get(uri);
	}

	public final Set<UUID> uuidSet() {
		return uuidSet;
	}

	public final Set<URI> uriSet() {
		return uriSet;
	}

	@Override
	public void initialize() {
		log.info("Initializing terms for " + getClass().getName());
		UUID root = store.getRootUuid();
		for (Field field : getClass().getFields()) {
			UUID uuid;
			try {
				uuid = (UUID) field.get(null);
			} catch (ClassCastException e) {
				// Assume this is an irrelevant field
				continue;
			} catch (IllegalAccessException e) {
				// Assume this is an irrelevant field
				log.warn("Could not get value of field: " + field.getName());
				continue;
			}
			URI uri = instance.uri(uuid);
			log.info("Initializing term: " + uri);
			store().in(root).sub(ConserveTerms.hasTerm).acquire(uri);
			Type typeAnnotation = field.getAnnotation(Type.class);
			Property propertyAnnotation = field.getAnnotation(Property.class);
			String defaultType = null;
			if (typeAnnotation != null) {
				defaultType = "http://www.w3.org/2000/01/rdf-schema#Class";
				for (String type : typeAnnotation.value()) {
					defaultType = null;
					log.info(" Setting type: " + type);
					store().in(uuid).put(ConserveTerms.type, type);
				}
			}
			if (propertyAnnotation != null) {
				defaultType = "http://www.w3.org/2000/01/rdf-schema#Property";
				for (String domain : propertyAnnotation.domain()) {
					log.info(" Setting domain: " + domain);
					store().in(uuid).put(ConserveTerms.domain, domain);
				}
				for (String range : propertyAnnotation.range()) {
					log.info(" Setting range: " + range);
					store().in(uuid).put(ConserveTerms.range, range);
				}
			}
			if (defaultType != null) {
				log.info(" Setting type: " + defaultType);
				store().in(uuid).put(ConserveTerms.type, defaultType);
			}
		}
	}

}
