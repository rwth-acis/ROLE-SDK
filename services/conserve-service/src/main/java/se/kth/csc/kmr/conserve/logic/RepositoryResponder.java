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

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.security.SecurityInfo;
import se.kth.csc.kmr.conserve.util.Base64UUID;

public class RepositoryResponder extends TemplateResponder {

	@Inject
	SecurityInfo securityInfo;

	public RepositoryResponder() {
		super("/ui/html/browser.html");
	}

	@Override
	public Object doGet(Request request) {
		boolean isAuthorized = false;
		if (securityInfo.getAgent() != null) {
			Concept agent = store.getConcept(store.getRootUuid(),
					securityInfo.getAgent());
			if (agent != null) {
				isAuthorized = true;
			}
		}
		if (!isAuthorized) {
			return Response
					.seeOther(
							((RequestImpl) request)
									.getUriInfo()
									.getBaseUriBuilder()
									.path(Base64UUID.encode(store.getRootUuid()))
									.path(":authentication")
									.queryParam(
											"return",
											((RequestImpl) request)
													.getUriInfo()
													.getRequestUri().getPath())
									.build()).build();
		} else {
			return super.doGet(request);
		}
	}
}
