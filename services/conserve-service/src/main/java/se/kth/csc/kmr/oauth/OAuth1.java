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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

/**
 * Implementation of RFC 5849 (The OAuth 1.0 Protocol).
 * 
 * @author Erik Isaksson <erikis@kth.se>
 * 
 */
@Singleton
public class OAuth1 {

	private static final Logger log = LoggerFactory.getLogger(OAuth1.class);

	private static final SecureRandom RAND = new SecureRandom();

	@Inject
	private OAuth1Store store;

	private long nonceEarliestTime = new Date().getTime() / 1000;

	private final LinkedList<Nonce> nonceList = new LinkedList<Nonce>();

	private final Map<Nonce, Set<String>> nonceMap = new HashMap<Nonce, Set<String>>();

	/**
	 * Validates the provided HttpServletRequest according to RFC 5849.
	 * 
	 * @param request
	 * @return the token credential details if the request is valid and token
	 *         credentials exist, otherwise null
	 * @throws OAuthException
	 *             if the request uses OAuth but validation failed
	 */
	public final OAuth1Token validate(HttpServletRequest request)
			throws OAuthException {
		Map<String, List<String>> params = extract(request);
		if (params == null || !params.containsKey("oauth_token")) {
			return null;
		}
		return store.getToken(params.get("oauth_token").get(0));
	}

	/**
	 * 
	 * @param request
	 * @return
	 * @throws OAuthException
	 */
	public final String initiate(HttpServletRequest request)
			throws OAuthException {
		Map<String, List<String>> params = extract(request);
		if (params == null) {
			throw new OAuthException("The request is not OAuth validated");
		}
		String client = params.get("oauth_consumer_key").get(0);
		String callback = params.containsKey("oauth_callback") ? params.get(
				"oauth_callback").get(0) : null;
		if (callback == null) {
			callback = store.getClientCallback(client);
		} else {
			String registeredCallback = store.getClientCallback(client);
			if (registeredCallback != null
					&& !callback.equals(registeredCallback)) {
				throw new OAuthException(
						"The provided oauth_callback does not equal the client's registered callback");
			}
		}
		if (callback == null) {
			throw new OAuthException("No oauth_callback was provided");
		}
		try {
			new URI(callback);
		} catch (URISyntaxException e) {
			throw new OAuthException("The provided oauth_callback is invalid");
		}
		Map<String, String> attributes = new HashMap<String, String>();
		for (String key : params.keySet()) {
			if (!key.startsWith("oauth_")) {
				log.info(("Attr Key: " + key + " Value: " + params.get(key)
						.get(0)));
				attributes.put(key, params.get(key).get(0));
			}
		}
		String sharedSecret = randomString();
		String temporary = store.initiate(sharedSecret, client, callback,
				attributes);
		return "oauth_token=" + percentEncode(temporary)
				+ "&oauth_token_secret=" + percentEncode(sharedSecret)
				+ "&oauth_callback_confirmed=true";
	}

	/**
	 * 
	 * @param token
	 * @param resourceOwner
	 * @return
	 * @throws OAuthException
	 */
	public final OAuth1Token preAuthorize(String token) throws OAuthException {
		OAuth1Token temporary = store.getTemporaryToken(token);
		if (temporary == null) {
			throw new OAuthException("The provided oauth_token is invalid");
		}
		if (temporary.getResourceOwner() != null) {
			throw new OAuthException("The token has already been authorized");
		}
		return temporary;
	}

	/**
	 * 
	 * @param token
	 * @param resourceOwner
	 * @return
	 * @throws OAuthException
	 */
	public final URI authorize(String token, String resourceOwner)
			throws OAuthException {
		String verifier = randomString();
		String callback = store.authorize(token, verifier, resourceOwner);
		callback += callback.indexOf('?') < 0 ? '?' : '&';
		callback += "oauth_token=" + percentEncode(token) + "&oauth_verifier="
				+ percentEncode(verifier);
		return URI.create(callback);
	}

	/**
	 * 
	 * @param request
	 * @return
	 * @throws OAuthException
	 */
	public final String token(HttpServletRequest request) throws OAuthException {
		Map<String, List<String>> params = extract(request);
		if (params == null) {
			throw new OAuthException("The request is not OAuth validated");
		}
		String token = params.get("oauth_token").get(0);
		String signatureMethod = params.get("oauth_signature_method").get(0);
		if (!"RSA-SHA1".equals(signatureMethod)) {
			String expectedVerifier = params.containsKey("oauth_verifier") ? params
					.get("oauth_verifier").get(0) : null;
			if (expectedVerifier == null) {
				throw new OAuthException("No oauth_verifier was provided");
			}
			String actualVerifier = store.getTokenVerifier(token);
			if (!actualVerifier.equals(expectedVerifier)) {
				throw new OAuthException(
						"The provided oauth_verifier is invalid");
			}
		}
		String sharedSecret = randomString();
		String authorized = store.token(token, sharedSecret);
		return "oauth_token=" + percentEncode(authorized)
				+ "&oauth_token_secret=" + percentEncode(sharedSecret);
	}

	/**
	 * 
	 * @param request
	 * @return
	 * @throws OAuthException
	 */
	protected final Map<String, List<String>> extract(HttpServletRequest request)
			throws OAuthException {
		final Base64 BASE64 = new Base64();

		// Storage for parameter values
		Map<String, List<String>> paramsAll = new HashMap<String, List<String>>();
		Map<String, String> paramsOAuth = new HashMap<String, String>();
		boolean isOAuthParamsRead = false;

		// Header parameters (1st priority)
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && !authHeader.startsWith("OAuth ")) {
			authHeader = null;
		}
		if (authHeader != null) {
			Pattern pattern = Pattern.compile("(\\w+)=\"([^\"]*)\"(?:,\\s?)?");
			Matcher matcher = pattern.matcher(authHeader.substring(6));
			while (matcher.find()) {
				String key = percentDecode(matcher.group(1));
				String value = percentDecode(matcher.group(2));
				if ("realm".equals(key)) {
					// We don't care about the realm, also it should not be
					// included in the signature
				} else {
					if (key.startsWith("oauth_")) {
						paramsOAuth.put(key, value);
					}
					List<String> values = paramsAll.get(key);
					if (values == null) {
						values = new LinkedList<String>();
						paramsAll.put(key, values);
					}
					values.add(value);
				}
			}
		}

		// Entity-body parameters (2nd priority)
		isOAuthParamsRead = authHeader != null;
		String contentType = request.getContentType();
		if (contentType != null
				&& contentType.equals("application/x-www-form-urlencoded")) {
			try {
				// BufferedReader reader = request.getReader();
				// StringBuffer buffer = new StringBuffer();
				// for (String line = reader.readLine(); line != null; line =
				// reader
				// .readLine()) {
				// buffer.append(line);
				// }
				// String form = buffer.toString();
				// for (String formParam : form.split("\\&")) {
				// String[] formValue = formParam.split("=");
				// if (formValue.length == 2) {
				// String key = percentDecode(formValue[0]);
				// String value = percentDecode(formValue[1]);
				// List<String> values = paramsAll.get(key);
				// if (!isOAuthParamsRead && key.startsWith("oauth_")) {
				// paramsOAuth.put(key, value);
				// }
				// if (values == null) {
				// values = new LinkedList<String>();
				// paramsAll.put(key, values);
				// }
				// values.add(value);
				// }
				// }
				// } catch (IOException e) {
				// throw new OAuthException("Error reading entity body", e);
			} catch (IllegalStateException e) {
				// Perhaps the entity body has already been read, but it
				// might be that the OAuth parameters are actually in the
				// query, so let's move on
			}
		}

		// Query parameters (3rd priority)
		isOAuthParamsRead = isOAuthParamsRead || paramsOAuth.size() > 0;
		String query = request.getQueryString();
		if (query != null) {
			for (String queryParam : query.split("\\&")) {
				String[] queryValue = queryParam.split("=");
				if (queryValue.length == 2) {
					String key = percentDecode(queryValue[0]);
					String value = percentDecode(queryValue[1]);
					List<String> values = paramsAll.get(key);
					if (!isOAuthParamsRead && key.startsWith("oauth_")) {
						paramsOAuth.put(key, value);
					}
					if (values == null) {
						values = new LinkedList<String>();
						paramsAll.put(key, values);
					}
					values.add(value);
				} else {
					return null;
				}
			}
		}

		// No OAuth
		if (!isOAuthParamsRead && paramsOAuth.size() == 0) {
			log.info("Request does not use OAuth");
			return null;
		}

		// Version
		if (paramsOAuth.containsKey("oauth_version")) {
			if (!"1.0".equals(paramsOAuth.get("oauth_version"))) {
				throw new OAuthException("Unsupported OAuth version");
			}
		}

		// Signature method
		String signatureMethod = paramsOAuth.get("oauth_signature_method");
		if (signatureMethod == null) {
			throw new OAuthException("No oauth_signature_method provided");
		}
		if ("PLAINTEXT".equals(signatureMethod)) {
			if (!request.isSecure()) {
				throw new OAuthException(
						"Signature method PLAINTEXT not allowed over an insecure channel");
			}
		} else if (!("HMAC-SHA1".equals(signatureMethod) || "RSA-SHA1"
				.equals(signatureMethod))) {
			throw new OAuthException("Invalid oauth_signature_method provided");
		}

		// Client credentials
		String client = paramsOAuth.get("oauth_consumer_key");
		if (client == null) {
			throw new OAuthException("No oauth_consumer_key provided");
		}
		String clientSharedSecret = null;
		PublicKey clientPublicKey = null;
		if ("RSA-SHA1".equals(signatureMethod)) {
			clientPublicKey = store.getClientPublicKey(client);
			if (clientPublicKey == null) {
				throw new OAuthException(
						"Client RSA public key not found for the provided oauth_consumer_key");
			}
		} else {
			clientSharedSecret = store.getClientSharedSecret(client);
			if (clientSharedSecret == null) {
				throw new OAuthException(
						"Client shared secret not found for the provided oauth_consumer_key");
			}
		}

		// Token credentials if token was provided
		String token = paramsOAuth.get("oauth_token");
		String tokenSharedSecret = "";
		if (token != null) {
			tokenSharedSecret = store.getTokenSharedSecret(token);
			if (tokenSharedSecret == null) {
				throw new OAuthException(
						"Token secret not found for the provided oauth_token");
			}
		}

		// Signature
		String providedSignature = paramsOAuth.get("oauth_signature");
		if (providedSignature == null) {
			throw new OAuthException("No oauth_signature was provided");
		}
		if ("PLAINTEXT".equals(signatureMethod)) {

			// Signature verification
			String[] signatureParts = providedSignature.split("\\&");
			if (signatureParts.length != 2
					|| !clientSharedSecret
							.equals(percentDecode(signatureParts[0]))
					|| (tokenSharedSecret != null && !tokenSharedSecret
							.equals(percentDecode(signatureParts[1])))) {
				throw new OAuthException(
						"Expected and actual PLAINTEXT signatures do not match");
			}

		} else {

			// Signature base string
			StringBuffer signatureBase = new StringBuffer();
			signatureBase.append(percentEncode(request.getMethod()));
			signatureBase.append('&');
			StringBuffer baseUri = new StringBuffer();
			baseUri.append(request.getScheme().toLowerCase(Locale.US));
			baseUri.append("://");
			baseUri.append(request.getServerName().toLowerCase(Locale.US));
			if ((request.getScheme().equalsIgnoreCase("http") && request
					.getServerPort() != 80)
					|| (request.getScheme().equalsIgnoreCase("https") && request
							.getServerPort() != 443)) {
				baseUri.append(':');
				baseUri.append(request.getServerPort());
			}
			baseUri.append(request.getRequestURI());
			signatureBase.append(percentEncode(baseUri.toString()));
			signatureBase.append('&');
			Map<String, Set<String>> sortedEncodedParams = new TreeMap<String, Set<String>>();
			for (String key : paramsAll.keySet()) {
				for (String value : paramsAll.get(key)) {
					if ("oauth_signature".equals(key)) {
						continue;
					}
					key = percentEncode(key);
					value = percentEncode(value);
					Set<String> values = sortedEncodedParams.get(key);
					if (values == null) {
						values = new TreeSet<String>();
						sortedEncodedParams.put(key, values);
					}
					values.add(value);
				}
			}
			StringBuffer normalizedParameters = new StringBuffer();
			for (String key : sortedEncodedParams.keySet()) {
				for (String value : sortedEncodedParams.get(key)) {
					if (normalizedParameters.length() > 0) {
						normalizedParameters.append('&');
					}
					normalizedParameters.append(key);
					normalizedParameters.append('=');
					normalizedParameters.append(value);
				}
			}
			signatureBase
					.append(percentEncode(normalizedParameters.toString()));
			String signatureBaseString = signatureBase.toString();

			// Signature verification
			byte[] expectedSignature = BASE64.decode(providedSignature
					.getBytes(Charset.forName("ISO-8859-1")));
			byte[] signatureBaseBytes = signatureBaseString.getBytes(Charset
					.forName("ISO-8859-1"));
			if ("HMAC-SHA1".equals(signatureMethod)) {
				try {
					String signatureKey = percentEncode(clientSharedSecret)
							+ '&' + percentEncode(tokenSharedSecret);
					Mac mac = Mac.getInstance("HMACSHA1");
					mac.init(new SecretKeySpec(signatureKey.getBytes(Charset
							.forName("US-ASCII")), "HMACSHA1"));
					byte[] actualSignature = mac.doFinal(signatureBaseBytes);
					if (actualSignature.length != expectedSignature.length) {
						throw new OAuthException(
								"Expected and actual HMAC-SHA1 signatures do not match");
					}
					for (int i = 0; i < actualSignature.length; i++) {
						if (actualSignature[i] != expectedSignature[i]) {
							throw new OAuthException(
									"Expected and actual HMAC-SHA1 signatures do not match");
						}
					}
				} catch (GeneralSecurityException e) {
					throw new OAuthException(
							"HMAC-SHA1 signature verification error", e);
				}
			} else if ("RSA-SHA1".equals(signatureMethod)) {
				try {
					synchronized (clientPublicKey) {
						Signature signatureAlgorithm = Signature
								.getInstance("SHA1withRSA");
						signatureAlgorithm.initVerify(clientPublicKey);
						signatureAlgorithm.update(signatureBaseBytes);
						if (!signatureAlgorithm.verify(expectedSignature)) {
							log.warn("Expected and actual RSA-SHA1 signatures do not match");
							log.warn("Signature base string: "
									+ signatureBaseString);
							throw new OAuthException(
									"Expected and actual RSA-SHA1 signatures do not match");
						}
					}
				} catch (NoSuchAlgorithmException e) {
					throw new OAuthException(
							"SHA1withRSA signature algorithm not found", e);
				} catch (InvalidKeyException e) {
					throw new OAuthException("Invalid consumer RSA public key",
							e);
				} catch (SignatureException e) {
					throw new OAuthException("RSA-SHA1 signature error", e);
				}
			}

			// Nonce and timestamp
			String nonce = paramsOAuth.get("oauth_nonce");
			if (nonce == null) {
				throw new OAuthException("No oauth_nonce was provided");
			}
			String timestampString = paramsOAuth.get("oauth_timestamp");
			if (timestampString == null) {
				throw new OAuthException("No oauth_timestamp was provided");
			}
			long timestamp;
			try {
				timestamp = Long.valueOf(timestampString);
			} catch (NumberFormatException e) {
				throw new OAuthException("Invalid timestamp value");
			}
			if (!verifyNonce(nonce, timestamp, client, token)) {
				throw new OAuthException("Nonce was rejected");
			}

		}

		// All clear
		log.info("OAuth validation of request successful");

		return paramsAll;
	}

	/**
	 * 
	 * @param nonce
	 * @param timestamp
	 * @param client
	 * @param token
	 * @return
	 */
	protected synchronized boolean verifyNonce(String nonce, long timestamp,
			String client, String token) {
		if (timestamp < nonceEarliestTime) {
			log.warn("Timestamp " + timestamp + " is before known time "
					+ nonceEarliestTime);
			return false;
		} else if (timestamp > new Date().getTime() / 1000 + 10) {
			log.warn("Timestamp " + timestamp + " is in the future");
			return false;
		}
		Nonce entry = new Nonce(nonce, timestamp, client, token);
		Set<String> nonces = nonceMap.get(entry);
		if (nonces != null) {
			if (nonces.contains(nonce)) {
				log.warn("Nonce already used");
				// return false;
				return true; // TODO
			}
		} else {
			nonces = new HashSet<String>(1);
			nonceMap.put(entry, nonces);
		}
		nonces.add(nonce);
		nonceList.addLast(entry);
		if (nonceList.size() > 10000) {
			Nonce removed = null;
			for (int i = 0; i < 1000; i++) {
				removed = nonceList.removeFirst();
				nonces = nonceMap.get(removed);
				if (nonces.size() == 1) {
					nonceMap.remove(removed);
				} else {
					nonces.remove(removed.nonce);
				}
			}
			nonceEarliestTime = removed.internalTimestamp;
		}
		return true;
	}

	/**
	 * 
	 * @author Erik Isaksson <erikis@kth.se>
	 * 
	 */
	private final class Nonce {
		long internalTimestamp;
		String nonce;
		long timestamp;
		String client;
		String token;

		Nonce(String nonce, long timestamp, String client, String token) {
			this.internalTimestamp = new Date().getTime() / 1000;
			this.nonce = nonce;
			this.timestamp = timestamp;
			this.client = client;
			this.token = token;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Nonce))
				return false;
			Nonce o = (Nonce) obj;
			return timestamp == o.timestamp
					&& client.equals(o.client)
					&& ((token == null && o.token == null) || (token != null && token
							.equals(o.token)));
		}

		@Override
		public int hashCode() {
			final int prime = 47;
			int hash = 17;
			hash = hash * prime + (int) timestamp;
			hash = hash * prime + client.hashCode();
			if (token != null) {
				hash = hash * prime + token.hashCode();
			}
			return hash;
		}
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static String percentEncode(String value) {
		if (value == null) {
			return "";
		}
		try {
			return URLEncoder.encode(value, "UTF-8").replace("+", "%20")
					.replace("*", "%2A").replace("%7E", "~");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static String percentDecode(String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @return
	 */
	public static String randomString() {
		byte[] tokenSecret = new byte[16];
		RAND.nextBytes(tokenSecret);
		return new String(Base64.encodeBase64(tokenSecret),
				Charset.forName("US-ASCII"));
	}

}
