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
package se.kth.csc.kmr.conserve.iface.internal;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Listener;
import se.kth.csc.kmr.conserve.core.AbstractRequest;

public class RequestNotifier extends AbstractRequest {

	@Inject
	private Map<UUID, Set<Listener>> listenerMap;

	public void doGet() {
		Set<Listener> listeners = listenerMap.get(getContext().getUuid());
		if (listeners != null) {
			for (Listener l : listeners) {
				l.doGet(this);
			}
		}
	}

	public void doPut() {
		Set<Listener> listeners = listenerMap.get(getContext().getUuid());
		if (listeners != null) {
			for (Listener l : listeners) {
				l.doPut(this, null);
			}
		}
	}

	public void doPost() {
		Set<Listener> listeners = listenerMap.get(getContext().getUuid());
		if (listeners != null) {
			for (Listener l : listeners) {
				l.doPost(this, null);
			}
		}
		Concept created = getCreated();
		if (created != null) {
			listeners = listenerMap.get(created.getPredicate());
			if (listeners != null) {
				for (Listener l : listeners) {
					l.doPost(this, null);
				}
			}
		}
	}

	public void doDelete() {
		Set<Listener> listeners = listenerMap.get(getContext().getUuid());
		if (listeners != null) {
			for (Listener l : listeners) {
				l.doDelete(this);
			}
		}
	}

	@Override
	public MultivaluedMap<String, String> getFormMap() {
		return null;
	}

}
