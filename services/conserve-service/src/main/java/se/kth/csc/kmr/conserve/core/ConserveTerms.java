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

import java.util.UUID;

import se.kth.csc.kmr.conserve.Property;
import se.kth.csc.kmr.conserve.Type;

public final class ConserveTerms extends Terms {

	public static final ConserveTerms INSTANCE = new ConserveTerms();

	@Property
	public static final UUID root = INSTANCE
			.uuid("http://purl.org/openapp/repository");

	@Property
	public static final UUID hasTerm = INSTANCE
			.uuid("http://purl.org/openapp/term");

	@Property
	public static final UUID hasService = INSTANCE
			.uuid("http://purl.org/openapp/domain");

	@Property
	public static final UUID hasEntry = INSTANCE
			.uuid("http://purl.org/openapp/entry");

	@Property
	public static final UUID hasPart = INSTANCE
			.uuid("http://purl.org/dc/terms/hasPart");

	@Property
	public static final UUID seeAlso = INSTANCE
			.uuid("http://www.w3.org/2000/01/rdf-schema#seeAlso");

	@Property
	public static final UUID subject = INSTANCE
			.uuid("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");

	@Property
	public static final UUID predicate = INSTANCE
			.uuid("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");

	@Property
	public static final UUID object = INSTANCE
			.uuid("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");

	@Type
	public static final UUID Property = INSTANCE
			.uuid("http://www.w3.org/2000/01/rdf-schema#Property");

	@Property
	public static final UUID domain = INSTANCE
			.uuid("http://www.w3.org/2000/01/rdf-schema#domain");

	@Property
	public static final UUID range = INSTANCE
			.uuid("http://www.w3.org/2000/01/rdf-schema#range");

	@Type
	public static final UUID Class = INSTANCE
			.uuid("http://www.w3.org/2000/01/rdf-schema#Class");

	@Property
	public static final UUID type = INSTANCE
			.uuid("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

	@Property
	public static final UUID reference = INSTANCE
			.uuid("http://www.w3.org/2002/07/owl#sameAs");

	@Property
	public static final UUID index = INSTANCE
			.uuid("http://purl.org/openapp/index");

	@Property
	public static final UUID representation = INSTANCE
			.uuid("http://purl.org/openapp/representation");

	@Property
	public static final UUID metadata = INSTANCE
			.uuid("http://purl.org/openapp/metadata");

	@Property
	public static final UUID feed = INSTANCE
			.uuid("http://purl.org/openapp/feed");

	@Property
	public static final UUID acl = INSTANCE
			.uuid("http://www.w3.org/ns/auth/acl#acl");

	@Property
	public static final UUID concept = INSTANCE
			.uuid("http://purl.org/openapp/concept");

	@Property
	public static final UUID content = INSTANCE
			.uuid("http://purl.org/openapp/content");

	@Property
	public static final UUID context = INSTANCE
			.uuid("http://purl.org/openapp/context");

	@Property
	public static final UUID control = INSTANCE
			.uuid("http://purl.org/openapp/control");

	@Property
	public static final UUID system = INSTANCE
			.uuid("http://purl.org/openapp/info");

	@Property
	public static final UUID metametadata = INSTANCE
			.uuid("http://purl.org/openapp/metametadata");

	@Property
	public static final UUID representationmetadata = INSTANCE
			.uuid("http://purl.org/openapp/representationmetadata");

	@Property
	public static final UUID secret = INSTANCE
			.uuid("http://purl.org/openapp/secret");

	@Property
	public static final UUID openid = INSTANCE
			.uuid("http://xmlns.com/foaf/0.1/openid");

	@Property
	public static final UUID annotates = INSTANCE
			.uuid("http://purl.org/openapp/annotation");

	public static final UUID configuration = INSTANCE
			.uuid("http://purl.org/openapp/configuration");

	public static final UUID authentication = INSTANCE
			.uuid("http://purl.org/openapp/authentication");

	public static final UUID realm = INSTANCE
			.uuid("http://purl.org/openapp/realm");

	public static final UUID owner = INSTANCE
			.uuid("http://purl.org/openapp/owner");

	public static final UUID app = INSTANCE.uuid("http://purl.org/openapp/app");

	public static final UUID provider = INSTANCE.uuid("http://openid.net");
	
	public ConserveTerms() {
		super(INSTANCE);
	}

}
