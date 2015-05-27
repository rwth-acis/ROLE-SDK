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
package se.kth.csc.kmr.conserve.security.openid;

import org.openid4java.association.AssociationSessionType;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;

public class OpenIDModule extends AbstractModule {

	private static final Logger log = LoggerFactory
			.getLogger(OpenIDModule.class);

	@Override
	protected void configure() {
		log.info("Configuring OpenID module");

		binder().bind(ConsumerManager.class)
				.toProvider(new Provider<ConsumerManager>() {
					@Override
					public ConsumerManager get() {
						ConsumerManager mgr = new ConsumerManager();
						mgr.setAssociations(new InMemoryConsumerAssociationStore());
						mgr.setNonceVerifier(new InMemoryNonceVerifier(5000));
						mgr.setMinAssocSessEnc(AssociationSessionType.DH_SHA256);
						return mgr;
					}
				}).in(Scopes.SINGLETON);

		binder().bind(ServerManager.class).in(Scopes.SINGLETON);

		binder().bind(OpenIDEndpoints.class);
	}

}
