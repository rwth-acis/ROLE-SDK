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
package se.kth.csc.kmr.conserve.security.oauth;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Named;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.dsl.ContempDSL;
import se.kth.csc.kmr.oauth.OAuth1Store;
import se.kth.csc.kmr.oauth.OAuthException;
import se.kth.csc.kmr.oauth.OAuth1Token;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class OAuthStoreImpl implements OAuth1Store {

	private static Logger log = LoggerFactory.getLogger(OAuthStoreImpl.class);

	@Inject
	@Named("conserve.oauth.access.token.context")
	private UUID tokenContextUuid;

	@Inject
	@Named("conserve.oauth.request.token.context")
	private UUID temporaryContextUuid;

	@Inject
	private Injector injector;

	private KeyStore keyStore;

	private Map<String, PublicKey> clientPublicKeyCache = Collections
			.synchronizedMap(new HashMap<String, PublicKey>());

	private KeyPair keyPair;

	private Map<String, String> callbacks = new HashMap<String, String>();;

	public OAuthStoreImpl() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream("cert/oapubkey.jks");
		try {
			keyStore = KeyStore.getInstance("JKS");
			keyStore.load(is, "oapubkey".toCharArray());
		} catch (KeyStoreException e) {
			log.error("Error loading key store", e);
		} catch (NoSuchAlgorithmException e) {
			log.error("Error loading key store", e);
		} catch (CertificateException e) {
			log.error("Error loading key store", e);
		} catch (IOException e) {
			log.error("Error loading key store", e);
		}

		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator gen;
		try {
			gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(2048);
			keyPair = gen.generateKeyPair();
			String hostname = getServerHostname();
			X500Principal dnName = new X500Principal("CN=" + hostname);
			X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
			certGen.setSerialNumber(BigInteger.ONE);
			certGen.setIssuerDN(dnName);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -1);
			certGen.setNotBefore(calendar.getTime());
			calendar.add(Calendar.YEAR, 1);
			certGen.setNotAfter(calendar.getTime());
			certGen.setSubjectDN(dnName);
			certGen.setPublicKey(keyPair.getPublic());
			certGen.setSignatureAlgorithm("SHA256withRSA");
			X509Certificate cert = certGen.generate(keyPair.getPrivate(), "BC");
			keyStore.setCertificateEntry(hostname, cert);
		} catch (NoSuchAlgorithmException e) {
			log.error("Error generating local certificate", e);
		} catch (CertificateEncodingException e) {
			log.error("Error generating local certificate", e);
		} catch (InvalidKeyException e) {
			log.error("Error generating local certificate", e);
		} catch (IllegalStateException e) {
			log.error("Error generating local certificate", e);
		} catch (NoSuchProviderException e) {
			log.error("Error generating local certificate", e);
		} catch (SignatureException e) {
			log.error("Error generating local certificate", e);
		} catch (KeyStoreException e) {
			log.error("Error storing local certificate", e);
		}

		callbacks.put("www.google.com",
				"http://oauth.gmodules.com/gadgets/oauthcallback");
	}

	@Override
	public String getClientCallback(String client) throws OAuthException {
		return callbacks.get(client);
	}

	public KeyPair getLocalKeyPair() {
		return keyPair;
	}

	private ContempDSL store() {
		return (ContempDSL) injector.getInstance(Contemp.class);
	}

	@Override
	public String getClientSharedSecret(String client) {
		return null;
	}

	@Override
	public PublicKey getClientPublicKey(String client) {
		if (clientPublicKeyCache.containsKey(client)) {
			return clientPublicKeyCache.get(client);
		}
		try {
			synchronized (keyStore) {
				Certificate certificate = keyStore.getCertificate(client);
				if (certificate == null) {
					clientPublicKeyCache.put(client, null);
					return null;
				}
				PublicKey publicKey = certificate.getPublicKey();
				clientPublicKeyCache.put(client, publicKey);
				return publicKey;
			}
		} catch (KeyStoreException e) {
			log.error("Error retrieving public key", e);
			return null;
		}
	}

	@Override
	public String initiate(String sharedSecret, String client, String callback,
			Map<String, String> attributes) {
		Concept tokenContext = store().in(temporaryContextUuid).sub().create();
		String tokenMetadata = "oauth_consumer_key=" + percentEncode(client)
				+ "&oauth_callback=" + percentEncode(callback) + "&scope="
				+ percentEncode(attributes.get("scope"));
		store().in(tokenContext).as(ConserveTerms.metadata)
				.type("application/x-www-form-urlencoded")
				.string(tokenMetadata);
		store().in(tokenContext).as(ConserveTerms.representation)
				.type("text/plain").string(sharedSecret);
		return tokenContext.getUuid().toString();
	}

	@Override
	public String authorize(String token, String verifier, String resourceOwner) {
		UUID tokenUuid = UUID.fromString(token);
		Concept tokenContext = store().in(temporaryContextUuid).sub()
				.require(tokenUuid);
		Content metaContent = store().in(tokenContext)
				.as(ConserveTerms.metadata).require();
		String metaContentString = store().as(metaContent).string();
		metaContentString += "&oauth_verifier=" + percentEncode(verifier);
		store().in(tokenContext).put(ConserveTerms.reference, resourceOwner);
		store().as(metaContent).type("application/x-www-form-urlencoded")
				.string(metaContentString);
		String callback = percentDecode(metaContentString.substring(
				metaContentString.indexOf("oauth_callback=")
						+ "oauth_callback=".length(),
				metaContentString.indexOf('&',
						metaContentString.indexOf("oauth_callback="))));
		return callback;
	}

	@Override
	public String getTokenVerifier(String token) {
		UUID tokenUuid = UUID.fromString(token);
		Concept tokenContext = store().in(temporaryContextUuid).sub()
				.require(tokenUuid);
		Content metaContent = store().in(tokenContext)
				.as(ConserveTerms.metadata).require();
		String metaContentString = store().as(metaContent).string();
		String verifier = percentDecode(metaContentString
				.substring(metaContentString.indexOf("oauth_verifier=")
						+ "oauth_verifier=".length()));
		return verifier;
	}

	@Override
	public String token(String temporaryToken, String sharedSecret) {
		Contemp store = injector.getInstance(Contemp.class);
		UUID tokenUuid = UUID.fromString(temporaryToken);
		Concept temporaryTokenContext = store().in(temporaryContextUuid).sub()
				.require(tokenUuid);
		List<Control> temporaryTokenReferences = store.getControls(tokenUuid,
				ConserveTerms.reference);
		String agentUri = temporaryTokenReferences.get(0).getUri();
		Content metaContent = store().in(temporaryTokenContext)
				.as(ConserveTerms.metadata).require();
		String metaContentString = store().as(metaContent).string();
		Concept authorizedTokenContext = store().in(tokenContextUuid).sub()
				.create();
		store().in(authorizedTokenContext).put(ConserveTerms.reference,
				agentUri);
		store().in(authorizedTokenContext).as(ConserveTerms.metadata)
				.type("application/x-www-form-urlencoded")
				.string(metaContentString);
		store().in(authorizedTokenContext).as(ConserveTerms.representation)
				.type("text/plain").string(sharedSecret);
		return authorizedTokenContext.getUuid().toString();
	}

	@Override
	public String getTokenSharedSecret(String token) throws OAuthException {
		UUID tokenUuid = UUID.fromString(token);
		Concept tokenContext = store().require(tokenUuid);
		// if (!(tokenContext.equals(tokenContext.getContext()) ||
		// temporaryContextUuid
		// .equals(tokenContext.getContext()))) {
		// throw new OAuthException("Invalid token");
		// }
		Content tokenSecretContent = store().in(tokenContext)
				.as(ConserveTerms.representation).require();
		return store().as(tokenSecretContent).string();
	}

	@Override
	public OAuth1Token getToken(String token) {
		Contemp store = injector.getInstance(Contemp.class);
		UUID tokenUuid = UUID.fromString(token);
		Concept tokenContext = store().in(tokenContextUuid).sub()
				.require(tokenUuid);
		List<Control> tokenReference = store.getControls(tokenUuid,
				ConserveTerms.reference);
		final String agentUri = tokenReference.get(0).getUri();
		Content metaContent = store().in(tokenContext)
				.as(ConserveTerms.metadata).require();
		String metaContentString = store().as(metaContent).string() + "&";
		final String client = percentDecode(metaContentString.substring(
				metaContentString.indexOf("oauth_consumer_key=")
						+ "oauth_consumer_key=".length(),
				metaContentString.indexOf('&',
						metaContentString.indexOf("oauth_consumer_key="))));
		final String scope = percentDecode(metaContentString.substring(
				metaContentString.indexOf("scope=") + "scope=".length(),
				metaContentString.indexOf('&',
						metaContentString.indexOf("scope="))));
		return new OAuth1Token() {
			@Override
			public String getResourceOwner() {
				return agentUri;
			}

			@Override
			public Map<String, String> getAttributes() {
				Map<String, String> map = new HashMap<String, String>();
				map.put("scope", scope);
				return map;

			}

			@Override
			public String getClient() {
				return client;
			}
		};
	}

	private static String percentEncode(String value) {
		if (value == null) {
			return "";
		}
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static String percentDecode(String value) {
		if (value == null) {
			return "";
		}
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public OAuth1Token getTemporaryToken(String token) throws OAuthException {
		UUID tokenUuid = UUID.fromString(token);
		Concept tokenContext = store().in(temporaryContextUuid).sub()
				.require(tokenUuid);
		Content metaContent = store().in(tokenContext)
				.as(ConserveTerms.metadata).require();
		String metaContentString = store().as(metaContent).string() + "&";
		final String client = percentDecode(metaContentString.substring(
				metaContentString.indexOf("oauth_consumer_key=")
						+ "oauth_consumer_key=".length(),
				metaContentString.indexOf('&',
						metaContentString.indexOf("oauth_consumer_key="))));
		final String scope = percentDecode(metaContentString.substring(
				metaContentString.indexOf("scope=") + "scope=".length(),
				metaContentString.indexOf('&',
						metaContentString.indexOf("scope="))));
		return new OAuth1Token() {
			@Override
			public String getResourceOwner() {
				return null;
			}

			@Override
			public Map<String, String> getAttributes() {
				Map<String, String> map = new HashMap<String, String>();
				map.put("scope", scope);
				return map;

			}

			@Override
			public String getClient() {
				return client;
			}
		};
	}

	protected static String getServerHostname() {
		return System.getProperty("shindig.host") != null ? System
				.getProperty("shindig.host") : (System
				.getProperty("jetty.host") != null ? System
				.getProperty("jetty.host") : "localhost");
	}

}
