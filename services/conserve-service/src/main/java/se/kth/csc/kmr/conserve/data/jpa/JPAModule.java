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
package se.kth.csc.kmr.conserve.data.jpa;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Contemp;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.servlet.ServletScopes;

public class JPAModule extends AbstractModule {

	private static Logger log = LoggerFactory.getLogger(JPAModule.class);

	@Override
	protected void configure() {
		log.info("Configuring JPA module");
		bind(Contemp.class).to(JPAConserve.class).in(
				ServletScopes.REQUEST);
		EntityManagerFactory factory = Persistence
				.createEntityManagerFactory("conserve");
		bind(EntityManagerFactory.class).toInstance(factory);
		bind(EntityManager.class).toProvider(new Provider<EntityManager>() {
			@Inject
			private EntityManagerFactory factory;

			@Override
			public EntityManager get() {
				return factory.createEntityManager();
			}
		});
	}

}
