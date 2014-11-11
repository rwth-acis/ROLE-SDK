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

import org.cometd.bayeux.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

public class ServerAttributeListener implements ServletContextAttributeListener {

	private static Logger log = LoggerFactory
			.getLogger(ServerAttributeListener.class);

	private static BayeuxServer bayeux = null;

	@Override
	public void attributeAdded(ServletContextAttributeEvent event) {
		if (BayeuxServer.ATTRIBUTE.equals(event.getName())) {
			log.info("Setting up Bayeux server");

			// Grab the Bayeux object
			bayeux = (BayeuxServer) event.getValue();
			log.info("Server instance: " + bayeux);

			// Create other services here

			// This is also the place where you can configure the Bayeux object
			// by adding extensions or specifying a SecurityPolicy
			bayeux.setSecurityPolicy(new SecurityPolicy() {
				@Override
				public boolean canSubscribe(BayeuxServer arg0,
						ServerSession arg1, ServerChannel arg2,
						ServerMessage arg3) {
					return true;
				}

				@Override
				public boolean canPublish(BayeuxServer arg0,
						ServerSession arg1, ServerChannel arg2,
						ServerMessage arg3) {
					return true;
				}

				@Override
				public boolean canHandshake(BayeuxServer arg0,
						ServerSession arg1, ServerMessage arg2) {
					return true;
				}

				@Override
				public boolean canCreate(BayeuxServer arg0, ServerSession arg1,
						String arg2, ServerMessage arg3) {
					return true;
				}
			});
		}
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent scab) {
	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent scab) {
	}

	public static BayeuxServer getBayeux() {
		return bayeux;
	}

}
