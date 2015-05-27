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
package se.kth.csc.kmr.conserve.logic;

import java.io.IOException;

import javax.inject.Inject;

import org.openrdf.OpenRDFException;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractGuard;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

public class ACLGuard extends AbstractGuard {

	@Inject
	private SecurityInfo securityInfo;

	private boolean evaluate(String mode, Request resource) {
		Concept context = resource.getContext();
		Concept guardContext = resource.getGuardContext();
		Content guardContent = store.getContent(guardContext.getUuid(),
				ConserveTerms.acl);
		if (guardContent == null) {
			return false;
		}

		if ("Read".equals(mode)
				&& resource.getRelation() != null
				&& ConserveTerms.authentication.equals(resource.getRelation()
						.getUuid())) {
			return true;
		}

		String authenticatedUuidString = securityInfo.getAgent() != null ? "urn:uuid:"
				+ securityInfo.getAgent().toString()
				: null;
		if (authenticatedUuidString == null) {
			authenticatedUuidString = "http://xmlns.com/foaf/0.1/Agent";
		}

		Repository myRepository = new SailRepository(new MemoryStore());
		ValueFactory f = myRepository.getValueFactory();
		try {
			myRepository.initialize();
			RepositoryConnection con = myRepository.getConnection();
			try {
				con.add(guardContent.getDataStream(), "urn:uuid:"
						+ guardContext.getUuid().toString(), RDFFormat.N3,
						f.createURI("urn:uuid:" + ConserveTerms.acl));
				String accessTo = "<urn:uuid:" + context.getUuid() + ">";
				String defaultForNew = "<urn:uuid:" + guardContext.getUuid()
						+ ">";
				String agent = "<" + authenticatedUuidString + ">";
				String queryString;
				boolean isSame = context.getUuid().equals(
						guardContext.getUuid());
				if (isSame) {
					queryString = String
							.format("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX acl: <http://www.w3.org/ns/auth/acl#>\nASK {\n ?rule acl:accessTo %s .\n  ?rule rdf:type acl:Authorization ;\n acl:mode acl:%s .\n { ?rule acl:agent %s} UNION { ?rule acl:agent %s} .\n}",
									accessTo, mode, agent, "<http://xmlns.com/foaf/0.1/Agent>");
				} else {
					queryString = String
							.format("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX acl: <http://www.w3.org/ns/auth/acl#>\nASK {\n { ?rule acl:accessTo %s } union { ?rule acl:defaultForNew %s } .\n  ?rule rdf:type acl:Authorization ;\n acl:mode acl:%s .\n {?rule acl:agent %s} UNION {?rule acl:agent %s} .\n}",
									accessTo, defaultForNew, mode, agent, "<http://xmlns.com/foaf/0.1/Agent>");
				}
				BooleanQuery query = con.prepareBooleanQuery(
						QueryLanguage.SPARQL, queryString);
				return query.evaluate();
			} catch (IOException e) {
				return false;
			} finally {
				con.close();
			}
		} catch (OpenRDFException e) {
			return false;
		}
	}

	@Override
	public boolean canGet(Request resource) {
		return evaluate("Read", resource);
	}

	@Override
	public boolean canPut(Request resource) {
		return evaluate("Write", resource);
	}

	@Override
	public boolean canPost(Request resource) {
		return evaluate("Append", resource);
	}

	@Override
	public boolean canDelete(Request resource) {
		return evaluate("Write", resource);
	}

}
