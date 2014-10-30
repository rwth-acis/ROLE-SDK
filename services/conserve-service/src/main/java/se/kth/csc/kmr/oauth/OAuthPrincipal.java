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
package se.kth.csc.kmr.oauth;

import java.net.URI;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility for getting the resource owner. The resource owner is most likely the
 * same as the authenticated user. To be used during the authorization step of
 * OAuth.
 * 
 * @author Erik Isaksson <erikis@kth.se>
 * 
 */
public class OAuthPrincipal {

	/**
	 * Ensures that the user is authentiacted.
	 * 
	 * @param request
	 * @return null if authenticated, or URI for redirect to a login page
	 */
	public URI ensureAuthenticated(HttpServletRequest request) {
		return null;
	}

	/**
	 * Returns the OAuth resource owner. This is probably the same as the
	 * authenticated user.
	 * 
	 * @param request
	 * @return the resource owner (e.g., an internal identifier)
	 */
	public String getResourceOwner(HttpServletRequest request)
			throws OAuthException {
		Principal principal = request.getUserPrincipal();
		if (principal == null) {
			throw new OAuthException("The user is not authenticated");
		}
		return principal.getName();
	}

	/**
	 * Returns the OAuth resource owner. This is probably the same as the
	 * authenticated user.
	 * 
	 * @param request
	 * @return the resource owner (e.g., a user name)
	 */
	public String getResourceOwnerDisplayName(HttpServletRequest request)
			throws OAuthException {
		return getResourceOwner(request);
	}

}
