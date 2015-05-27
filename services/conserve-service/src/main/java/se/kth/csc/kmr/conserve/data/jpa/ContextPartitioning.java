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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.partitioning.CustomPartitioningPolicy;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.server.ClientSession;
import org.eclipse.persistence.sessions.server.ConnectionPool;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.core.ConserveTerms;

public class ContextPartitioning extends CustomPartitioningPolicy {

	private static Logger log = LoggerFactory
			.getLogger(ContextPartitioning.class);

	private static final long serialVersionUID = 7388108445286875569L;

	private final DatabaseField contextField = new DatabaseField("context");

	private final DatabaseField uuidField = new DatabaseField("uuid");

	private final DatabaseField predicateField = new DatabaseField("predicate");

	private List<String> connectionPools = new ArrayList<String>();

	private boolean isInitialized = false;

	private static Set<UUID> replicatedContexts = new HashSet<UUID>();

	private SecureRandom random = new SecureRandom();

	public synchronized static void replicate(UUID uuid) {
		if (!replicatedContexts.contains(uuid)) {
			Set<UUID> newSet = new HashSet<UUID>();
			newSet.addAll(replicatedContexts);
			newSet.add(uuid);
			replicatedContexts = newSet;
		}
	}

	@Override
	public void initialize(AbstractSession session) {
		super.initialize(session);
		init(session);
	}

	private synchronized void init(AbstractSession session) {
		if (isInitialized) {
			return;
		}
		log.info("Initializing context partitioning policy");
		log.info(" Session: " + session.toString());
		if (/* getConnectionPools().isEmpty() && */session.isServerSession()) {
			log.info(" Is server session");
			Map<String, ConnectionPool> pools = ((ServerSession) session)
					.getConnectionPools();
			log.info(" Pools: " + pools + " Size: " + pools.size());
			connectionPools.addAll(pools.keySet());
		}
		log.info("Context partitioning policy initialized with onnection pool size = "
				+ connectionPools.size());
		isInitialized = true;
	}

	public List<Accessor> getConnectionsForQuery(AbstractSession session,
			DatabaseQuery query, AbstractRecord arguments) {
		if (!isInitialized) {
			init(session);
		}
		Object context = arguments.get(contextField);
		if (context == null || replicatedContexts.contains(context)) {
			if (query.isReadQuery()
					&& (context != null
							|| replicatedContexts.contains(arguments
									.get(uuidField)) || ConserveTerms.root
							.equals(arguments.get(predicateField)))) {
				// log.info("Using any partition for query: Context: " + context
				// + " Query: " + query);
				List<Accessor> accessors = new ArrayList<Accessor>(1);
				accessors.add(getAccessor(connectionPools.get(random
						.nextInt(connectionPools.size())), session, query,
						false));
				return accessors;
			} else {
				// log.info("Using all partitions for query: Context: " +
				// context
				// + " Query: " + query);
				List<Accessor> accessors = new ArrayList<Accessor>(
						this.connectionPools.size());
				for (String poolName : this.connectionPools) {
					accessors.add(getAccessor(poolName, session, query, false));
				}
				return accessors;
			}
		}
		// log.info("Found context for query: Context: " + context + " Query: "
		// + query);
		int index = Math.abs(context.hashCode() % this.connectionPools.size());
		if (session.getPlatform().hasPartitioningCallback()) {
			session.getPlatform().getPartitioningCallback()
					.setPartitionId(index);
			return null;
		}
		// Use the mapped connection pool.
		List<Accessor> accessors = new ArrayList<Accessor>(1);
		String poolName = this.connectionPools.get(index);
		accessors.add(getAccessor(poolName, session, query, false));
		return accessors;
	}

	@Override
	public void partitionPersist(AbstractSession session, Object object,
			ClassDescriptor descriptor) {
		if (!isInitialized) {
			init(session);
		}
		Object value = extractPartitionValueForPersist(session, object,
				descriptor);
		if (value == null || replicatedContexts.contains(value)) {
			// log.info("Using all partitions for persist: Context: " + value
			// + " Object: " + object.toString() + " Descriptor: "
			// + descriptor.toString());
			return;
		}
		// log.info("Found context for persist: Context: " + value + " Object: "
		// + object.toString() + " Descriptor: " + descriptor.toString());
		int index = Math.abs(value.hashCode() % this.connectionPools.size());
		if (session.getPlatform().hasPartitioningCallback()) {
			session.getPlatform().getPartitioningCallback()
					.setPartitionId(index);
		} else {
			String poolName = this.connectionPools.get(index);
			getAccessor(poolName, session, null, false);
		}
	}

	private Object extractPartitionValueForPersist(AbstractSession session,
			Object object, ClassDescriptor descriptor) {
		if (!isInitialized) {
			init(session);
		}
		if (!session.isClientSession()) {
			return null;
		}
		ClientSession client = (ClientSession) session;
		if (!client.isExclusiveIsolatedClientSession()
				|| client.hasWriteConnection()) {
			return null;
		}
		return descriptor.getObjectBuilder().extractValueFromObjectForField(
				object, this.contextField, session);
	}

}
