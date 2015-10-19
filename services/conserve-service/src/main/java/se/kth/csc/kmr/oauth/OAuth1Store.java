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

import java.security.PublicKey;
import java.util.Map;

/**
 * 
 * @author Erik Isaksson <erikis@kth.se>
 * 
 */
public interface OAuth1Store {

	/**
	 * Returns the shared secret of the given client, or null if the client is
	 * not found or has no shared secret.
	 * 
	 * @param client
	 *            the consumer key
	 * @return
	 * @throws OAuthException
	 */
	public String getClientSharedSecret(String client) throws OAuthException;

	/**
	 * Returns the public key of the given client, or null if the client is not
	 * found or has no public key.
	 * 
	 * @param client
	 *            the consumer key
	 * @return
	 * @throws OAuthException
	 */
	public PublicKey getClientPublicKey(String client) throws OAuthException;

	/**
	 * Returns the callback URI of the given client, or null if the client is
	 * not found or has no registered callback URI.
	 * 
	 * @param client
	 *            the consumer key
	 * @return
	 * @throws OAuthException
	 */
	public String getClientCallback(String client) throws OAuthException;

	/**
	 * Creates new temporary credentials and stores them. Corresponds to
	 * "2.1. Temporary Credentials" in RFC 5849.
	 * 
	 * @param sharedSecret
	 *            a shared secret to be associated with the generated token
	 * @param client
	 *            the consumer key
	 * @param callback
	 *            the callback URI
	 * @param attributes
	 *            parameters included with the request, from which
	 *            implementation-specific attributes such as scope can be
	 *            retrieved and stored
	 * @return the generated token
	 * @throws OAuthException
	 */
	public String initiate(String sharedSecret, String client, String callback,
			Map<String, String> attributes) throws OAuthException;

	/**
	 * Returns the shared secret of the temporary credentials or token
	 * credentials identified by the given token.
	 * 
	 * @param token
	 * @return
	 * @throws OAuthException
	 */
	public String getTokenSharedSecret(String token) throws OAuthException;

	/**
	 * Returns details for the temporary credentials identified by the given
	 * token, or null if not found. (se.kth.csc.kmr.oauth.OAuth1Store.getToken
	 * returns details for non-temporary token credentials.)
	 * 
	 * @param token
	 * @return
	 * @throws OAuthException
	 */
	public OAuth1Token getTemporaryToken(String token) throws OAuthException;

	/**
	 * Marks the temporary credentials identified by the given token as being
	 * authorized. Corresponds to "2.2. Resource Owner Authorization" in RFC
	 * 5849.
	 * 
	 * @param token
	 * @param verifier
	 * @param resourceOwner
	 *            the user who has delegated authorization (see
	 *            se.kth.csc.kmr.oauth.OAuthPrincipal)
	 * @return the callback URI
	 * @throws OAuthException
	 */
	public String authorize(String token, String verifier, String resourceOwner)
			throws OAuthException;

	/**
	 * Returns the verifier of the temporary credentials identified by the given
	 * token.
	 * 
	 * @param token
	 * @return the verifier
	 * @throws OAuthException
	 */
	public String getTokenVerifier(String token) throws OAuthException;

	/**
	 * Creates new token credentials and stores them. Corresponds to
	 * "2.3. Token Credentials" in RFC 5849.
	 * 
	 * @param temporaryToken
	 *            the token of the temporary credentials on which the token
	 *            credentials are based
	 * @param sharedSecret
	 *            a shared secret to be associated with the generated token
	 * @return the generated token
	 * @throws OAuthException
	 */
	public String token(String temporaryToken, String sharedSecret)
			throws OAuthException;

	/**
	 * Returns details for the token credentials identified by the given token,
	 * or null if not found. Note that this does not include temporary
	 * credentials, for which null must always be returned.
	 * 
	 * @param token
	 * @return
	 * @throws OAuthException
	 */
	public OAuth1Token getToken(String token) throws OAuthException;

}
