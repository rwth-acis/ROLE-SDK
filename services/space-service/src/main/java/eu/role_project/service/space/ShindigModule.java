package eu.role_project.service.space;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.shindig.auth.AuthenticationServletFilter;
import org.apache.shindig.auth.BlobCrypterSecurityTokenCodec;
import org.apache.shindig.auth.DefaultSecurityTokenCodec;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.SecurityTokenCodec;
import org.apache.shindig.auth.SecurityTokenException;
import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.util.CharsetUtil;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.oauth.BasicOAuthStore;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;
import org.apache.shindig.gadgets.oauth.OAuthRequest;
import org.apache.shindig.gadgets.oauth.OAuthStore;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret.KeyType;
import org.apache.shindig.gadgets.oauth.OAuthModule.OAuthCrypterProvider;
import org.apache.shindig.gadgets.oauth.OAuthModule.OAuthRequestProvider;
import org.apache.shindig.gadgets.servlet.ConcatProxyServlet;
import org.apache.shindig.gadgets.servlet.GadgetRenderingServlet;
import org.apache.shindig.gadgets.servlet.HtmlAccelServlet;
import org.apache.shindig.gadgets.servlet.JsServlet;
import org.apache.shindig.gadgets.servlet.MakeRequestServlet;
import org.apache.shindig.gadgets.servlet.OAuthCallbackServlet;
import org.apache.shindig.gadgets.servlet.ProxyServlet;
import org.apache.shindig.gadgets.servlet.RpcServlet;
import org.apache.shindig.protocol.DataServiceServlet;
import org.apache.shindig.protocol.JsonRpcServlet;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.ActivityStreamService;
import org.apache.shindig.social.opensocial.spi.AlbumService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.MediaItemService;
import org.apache.shindig.social.opensocial.spi.MessageService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.sample.oauth.SampleOAuthDataStore;
import org.apache.shindig.social.sample.oauth.SampleOAuthServlet;
import org.bouncycastle.openssl.PEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Initializer;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractInitializer;
import se.kth.csc.kmr.conserve.core.Contapp;
import se.kth.csc.kmr.conserve.security.oauth.OAuthStoreImpl;
import se.kth.csc.kmr.oauth.OAuth1Store;

import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import eu.role_project.service.shindig.ConserveShindigService;

public class ShindigModule extends ServletModule {

	private static Logger log = LoggerFactory.getLogger(ShindigModule.class);

	@Override
	protected void configureServlets() {
		log.info("Configuring Shindig module");

		String serverPort = getServerPort();
		String serverHostname = getServerHostname();
		binder().bindConstant().annotatedWith(Names.named("shindig.port"))
				.to(serverPort);
		binder().bindConstant().annotatedWith(Names.named("shindig.host"))
				.to(serverHostname);
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.contextroot")).to("");

		// # Location of feature manifests (comma separated)
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.features.default"))
				.to("res://features/features.txt");

		// # Location of container configurations (comma separated)
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.containers.default"))
				.to("res://containers/default/container.js");

		// # A file containing blacklisted gadgets.
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.blacklist.file")).to("");

		// Inbound OAuth support
		// The URL base to use for full OAuth support (three-legged)
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.oauth.base-url"))
				.to("/oauth/");
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.oauth.authorize-action"))
				.to("/WEB-INF/authorize.jsp");
		// The range to the past and future of timestamp for OAuth token
		// validation. Default to 5 minutes
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.oauth.validator-max-timestamp-age-ms"))
				.to("300000");

		// Outbound OAuth support
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.signing.state-key")).to("");
		// binder().bindConstant()
		// .annotatedWith(Names.named("shindig.signing.key-name"))
		// .to(System.getProperty("shindig.signing.key-name"));
		// binder().bindConstant()
		// .annotatedWith(Names.named("shindig.signing.key-file"))
		// .to(System.getProperty("shindig.signing.key-file"));
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.signing.global-callback-url"))
				.to("http://" + serverHostname
						+ (!serverPort.equals("80") ? ":" + serverPort : "")
						+ "/gadgets/oauthcallback");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.signing.enable-signed-callbacks"))
				.to("true");

		// Set to true if you want to allow the use of 3-legged OAuth tokens
		// when viewer != owner.
		// This setting is not recommeneded for pages that allow user-controlled
		// javascript, since
		// that javascript could be used to make unauthorized requests on behalf
		// of the viewer of the page
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.signing.viewer-access-tokens-enabled"))
				.to("true");

		// If enabled here, configuration values can be found in container
		// configuration files.
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.locked-domain.enabled"))
				.to("false");

		// If enabled here, configuration values can be found in container
		// configuration files.
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.locked-domain.enabled"))
				.to("false");

		// TODO: This needs to be moved to container configuration.
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.content-rewrite.only-allow-excludes"))
				.to("false");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.content-rewrite.include-urls"))
				.to(".*");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.content-rewrite.exclude-urls"))
				.to("");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.content-rewrite.include-tags"))
				.to("body,embed,img,input,link,script,style");
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.content-rewrite.expires"))
				.to("86400");
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.content-rewrite.proxy-url"))
				.to("/gadgets/proxy?container=default&url=");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.content-rewrite.concat-url"))
				.to("/gadgets/concat?container=default&");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.content-rewrite.enable-split-js-concat"))
				.to("true");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.content-rewrite.enable-single-resource-concat"))
				.to("false");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.content-rewrite.enable-single-resource-concat"))
				.to("false");

		// Default set of forced libs to allow for better caching
		// NOTE: setting this causes the EndToEnd test to fail the
		// opensocial-templates test
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.gadget-rewrite.default-forced-libs"))
				.to("core:rpc");
		// binder().bindConstant().annotatedWith(Names.named("shindig.gadget-rewrite.default-forced-libs")).to("");

		// Allow supported JavaScript features required by a gadget to be
		// externalized on demand
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.gadget-rewrite.externalize-feature-libs"))
				.to("false");

		// Configuration for image rewriter
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.image-rewrite.max-inmem-bytes"))
				.to("1048576");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.image-rewrite.max-palette-size"))
				.to("256");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.image-rewrite.allow-jpeg-conversion"))
				.to("true");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.image-rewrite.jpeg-compression"))
				.to("0.90");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.image-rewrite.min-threshold-bytes"))
				.to("200");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.image-rewrite.jpeg-retain-subsampling"))
				.to("false");
		// Huffman optimization reduces the images size by addition 4-6% without
		// any loss in the quality of the image, but takes extra cpu cycles for
		// computing the optimized huffman tables.
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.image-rewrite.jpeg-huffman-optimization"))
				.to("false");

		// Configuration for the os:Flash tag
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.flash.min-version"))
				.to("9.0.115");

		// Configuration for template rewriter
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.template-rewrite.extension-tag-namespace"))
				.to("http://ns.opensocial.org/2009/extensions");

		// These values provide default TTLs for HTTP responses that don't use
		// caching headers.
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.cache.http.defaultTtl"))
				.to("3600000");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.cache.http.negativeCacheTtl"))
				.to("60000");

		// A default refresh interval for XML files, since there is no natural
		// way for developers to
		// specify this value, and most HTTP responses don't include good cache
		// control headers.
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.cache.xml.refreshInterval"))
				.to("300000");

		// Add entries in the form shindig.cache.lru.<name>.capacity to specify
		// capacities for different
		// caches when using the LruCacheProvider.
		// It is highly recommended that the EhCache implementation be used
		// instead of the LRU cache.
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.cache.lru.default.capacity"))
				.to("1000");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.cache.lru.expressions.capacity"))
				.to("1000");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.cache.lru.gadgetSpecs.capacity"))
				.to("1000");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.cache.lru.messageBundles.capacity"))
				.to("1000");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.cache.lru.httpResponses.capacity"))
				.to("10000");

		// The location of the EhCache configuration file.
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.cache.ehcache.config"))
				.to("res://org/apache/shindig/common/cache/ehcache/ehcacheConfig.xml");

		// True to enable JMX integration with cache stats
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.cache.ehcache.jmx.enabled"))
				.to("true");

		// true to enable JMX stats.
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.cache.ehcache.jmx.stats"))
				.to("true");

		// true to skip expensive encoding detection.
		// if true, will only attempt to validate utf-8. Assumes all other
		// encodings are ISO-8859-1.
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.http.fast-encoding-detection"))
				.to("true");

		// Configuration for the HttpFetcher
		// Connection timeout, in milliseconds, for requests.
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.http.client.connection-timeout-ms"))
				.to("60000");

		// Maximum size, in bytes, of the object we fetched, 0 == no limit
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.http.client.max-object-size-bytes"))
				.to("0");

		// Strict-mode parsing for proxy and concat URIs ensures that the
		// authority/host and path
		// for the URIs match precisely what is found in the container config
		// for it. This is
		// useful where statistics and traffic routing patterns, typically in
		// large installations,
		// key on hostname (and occasionally path). Enforcing this does come at
		// the cost that
		// mismatches break, which in turn mandates that URI generation always
		// happen in consistent
		// fashion, ie. by the class itself or tightly controlled code.
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.uri.proxy.use-strict-parsing"))
				.to("false");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.uri.concat.use-strict-parsing"))
				.to("false");

		// Host:port of the proxy to use while fetching urls. Leave blank if
		// proxy is
		// not to be used.
		binder().bindConstant()
				.annotatedWith(
						Names.named("org.apache.shindig.gadgets.http.basicHttpFetcherProxy"))
				.to("");

		binder().bindConstant()
				.annotatedWith(
						Names.named("org.apache.shindig.serviceExpirationDurationMinutes"))
				.to("60");

		// Older versions of shindig used 'data' in the json-rpc response format
		// The spec calls for using 'result' instead, however to avoid breakage
		// we
		// allow you to set it back to the old way here
		//
		// valid values are
		// result - new form
		// data - old broken form
		// both - return both fields for full compatibility
		binder().bindConstant()
				.annotatedWith(Names.named("shindig.json-rpc.result-field"))
				.to("result");

		// Remap "Internal server error"s received from the
		// basicHttpFetcherProxy server to
		// "Bad Gateway error"s, so that it is clear to the user that the proxy
		// server is
		// the one that threw the exception.
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.accelerate.remapInternalServerError"))
				.to("true");
		binder().bindConstant()
				.annotatedWith(
						Names.named("shindig.proxy.remapInternalServerError"))
				.to("true");

		// Add debug data when using VanillaCajaHtmlParser.
		binder().bindConstant().annotatedWith(Names.named("vanillaCajaParser"))
				.to("true");

		// Allow non-SSL OAuth 2.0 bearer tokens
		binder().bindConstant()
				.annotatedWith(
						Names.named("org.apache.shindig.auth.oauth2-require-ssl"))
				.to("false");

		// Set gadget param in proxied uri as authority if this is true
		binder().bindConstant()
				.annotatedWith(
						Names.named("org.apache.shindig.gadgets.uri.setAuthorityAsGadgetParam"))
				.to("false");

		// Maximum Get Url size limit
		binder().bindConstant()
				.annotatedWith(
						Names.named("org.apache.shindig.gadgets.uri.urlMaxLength"))
				.to("2048");

		// Default cachettl value for versioned url in seconds. Here default
		// value is 1 year.
		binder().bindConstant()
				.annotatedWith(
						Names.named("org.apache.shindig.gadgets.servlet.longLivedRefreshSec"))
				.to("31536000");

		// install(new org.apache.shindig.common.PropertiesModule());
		install(new org.apache.shindig.gadgets.DefaultGuiceModule());
		install(new org.apache.shindig.social.core.config.SocialApiGuiceModule());

		// install(new org.apache.shindig.social.sample.SampleModule());
		// install(new
		// org.apache.shindig.social.opensocial.jpa.spi.JPASocialModule());
		// binder().bindConstant()
		// .annotatedWith(Names.named("shindig.canonical.json.db"))
		// .to("sampledata/canonicaldb.json");
		// bind(ShindigDbService.class);
		// bind(ActivityService.class).to(ShindigDbService.class);
		bind(ActivityService.class).to(ConserveShindigService.class);
		// bind(PersonService.class).to(ShindigDbService.class);
		bind(PersonService.class).to(ConserveShindigService.class);
		// bind(AppDataService.class).to(ShindigDbService.class);
		bind(AppDataService.class).to(ConserveShindigService.class);
		// bind(AlbumService.class).to(ShindigDbService.class);
		bind(AlbumService.class).to(ConserveShindigService.class);
		// bind(MediaItemService.class).to(ShindigDbService.class);
		bind(MediaItemService.class).to(ConserveShindigService.class);
		// bind(MessageService.class).to(ShindigDbService.class);
		bind(MessageService.class).to(ConserveShindigService.class);
		bind(ActivityStreamService.class).to(ConserveShindigService.class);
		// bind(OAuthDataStore.class).to(SampleOAuthDataStore.class);
		bind(OAuthDataStore.class).to(ConserveShindigService.class);

		// install(new org.apache.shindig.gadgets.oauth.OAuthModule());
		bind(BlobCrypter.class).annotatedWith(
				Names.named(OAuthFetcherConfig.OAUTH_STATE_CRYPTER))
				.toProvider(OAuthCrypterProvider.class);
		bind(OAuthStore.class).to(BasicOAuthStore.class);
		bind(OAuthRequest.class).toProvider(OAuthRequestProvider.class);

		bind(BlobCrypter.class)
				.toProvider(RandomBasicBlobCrypterProvider.class).in(
						Scopes.SINGLETON);
		bind(SecurityTokenCodec.class)
				.to(RandomDefaultSecurityTokenCodec.class);

		// install(new org.apache.shindig.common.cache.ehcache.EhCacheModule());
		// install(new org.apache.shindig.extras.ShindigExtrasGuiceModule());
		// install(new
		// org.apache.shindig.extras.as.ActivityStreamsGuiceModule());

		Map<String, String> params = new HashMap<String, String>();
		bind(AuthenticationServletFilter.class).in(Singleton.class);
		filter("/social/*", "/gadgets/ifr", "/gadgets/makeRequest",
				"/gadgets/api/rpc/*", "/gadgets/api/rest/*", "/rpc", "/rest/*")
				.through(AuthenticationServletFilter.class);
		bind(GadgetRenderingServlet.class).in(Singleton.class);
		serve("/gadgets/ifr").with(GadgetRenderingServlet.class);
		bind(HtmlAccelServlet.class).in(Singleton.class);
		serve("/gadgets/accel").with(HtmlAccelServlet.class);
		bind(ProxyServlet.class).in(Singleton.class);
		serve("/gadgets/proxy").with(ProxyServlet.class);
		bind(MakeRequestServlet.class).in(Singleton.class);
		serve("/gadgets/makeRequest").with(MakeRequestServlet.class);
		bind(ConcatProxyServlet.class).in(Singleton.class);
		serve("/gadgets/concat").with(ConcatProxyServlet.class);
		bind(RpcServlet.class).in(Singleton.class);
		serve("/gadgets/metadata").with(RpcServlet.class);
		params.put("handlers", "org.apache.shindig.handlers");
		bind(DataServiceServlet.class).in(Singleton.class);
		serve("/rest/*", "/gadgets/api/rest/*", "/social/rest/*").with(
				DataServiceServlet.class, params);
		bind(JsonRpcServlet.class).in(Singleton.class);
		serve("/rpc", "/gadgets/api/rpc/*", "/social/rpc/*").with(
				JsonRpcServlet.class, params);
		bind(JsServlet.class).in(Singleton.class);
		serve("/gadgets/js/*").with(JsServlet.class);
		bind(SampleOAuthServlet.class).in(Singleton.class);
		serve("/oauth/*").with(SampleOAuthServlet.class);
		bind(OAuthCallbackServlet.class).in(Singleton.class);
		serve("/gadgets/oauthcallback").with(OAuthCallbackServlet.class);

		Multibinder<Initializer> initializers = Contapp
				.newInitializerBinder(binder());
		initializers.addBinding().to(ShindigOAuthInitializer.class);
	}

	/**
	 * Should return the port that the current server is running on. Useful for
	 * testing and working out of the box configs. Looks for a port in system
	 * properties "shindig.port" then "jetty.port", if not set uses fixed value
	 * of "8080"
	 * 
	 * @return an integer port number as a string.
	 */
	protected static String getServerPort() {
		return System.getProperty("shindig.port") != null ? System
				.getProperty("shindig.port") : (System
				.getProperty("jetty.port") != null ? System
				.getProperty("jetty.port") : "8080");
	}

	/**
	 * Should return the hostname that the current server is running on. Useful
	 * for testing and working out of the box configs. Looks for a hostname in
	 * system properties "shindig.host", if not set uses fixed value of
	 * "localhost"
	 * 
	 * @return a hostname
	 */
	protected static String getServerHostname() {
		return System.getProperty("shindig.host") != null ? System
				.getProperty("shindig.host") : (System
				.getProperty("jetty.host") != null ? System
				.getProperty("jetty.host") : "localhost");
	}

	private static class ShindigOAuthInitializer extends AbstractInitializer {

		@Inject
		private OAuth1Store resourceOAStore;

		@Inject
		private OAuthStore shindigOAStore;

		@Inject
		@Named("shindig.signing.global-callback-url")
		private String defaultCallbackUrl;

		@Override
		public void initialize(Request request) {
			log.info("Initializing Shindig OAuth");

			BasicOAuthStore basicOAuthStore = (BasicOAuthStore) shindigOAStore;

			final String OAUTH_CONFIG = "config/oauth.json";
			try {
				String oauthConfigString = ResourceLoader
						.getContent(OAUTH_CONFIG);
				basicOAuthStore.initFromConfigString(oauthConfigString);
			} catch (GadgetException e) {
				log.info("Error reading Shindig OAuth config", e);
			} catch (IOException e) {
				log.info("Error reading Shindig OAuth config", e);
			}

			basicOAuthStore.setDefaultCallbackUrl(defaultCallbackUrl);

			KeyPair keyPair = ((OAuthStoreImpl) resourceOAStore)
					.getLocalKeyPair();
			StringWriter w = new StringWriter();
			PEMWriter pemWriter = new PEMWriter(w);
			try {
				pemWriter.writeObject(keyPair.getPrivate());
				pemWriter.flush();
			} catch (IOException e) {
				log.error("Error serializing local private key", e);
			}
			String serverHostname = getServerHostname();
			BasicOAuthStoreConsumerKeyAndSecret defaultKey = new BasicOAuthStoreConsumerKeyAndSecret(
					serverHostname, w.toString(), KeyType.RSA_PRIVATE,
					serverHostname, null);

			basicOAuthStore.setDefaultKey(defaultKey);
		}

	}

	private static class RandomDefaultSecurityTokenCodec extends
			DefaultSecurityTokenCodec {

		private final SecurityTokenCodec codec;

		@Inject
		public RandomDefaultSecurityTokenCodec(ContainerConfig config,
				BlobCrypter crypter) {
			super(config);
			codec = new RandomBlobCrypterSecurityTokenCodec(config, crypter);
		}

		public SecurityToken createToken(Map<String, String> tokenParameters)
				throws SecurityTokenException {
			return codec.createToken(tokenParameters);
		}

		public String encodeToken(SecurityToken token)
				throws SecurityTokenException {
			if (token == null) {
				return null;
			}
			return codec.encodeToken(token);
		}

	}

	private static class RandomBlobCrypterSecurityTokenCodec extends
			BlobCrypterSecurityTokenCodec {

		public RandomBlobCrypterSecurityTokenCodec(ContainerConfig config,
				BlobCrypter crypter) {
			super(config);
			for (String container : config.getContainers()) {
				String keyFile = config.getString(container,
						SECURITY_TOKEN_KEY_FILE);
				if (keyFile == null) {
					crypters.put(container, crypter);
				}
			}
		}

	}

	private static class RandomBasicBlobCrypterProvider implements
			com.google.inject.Provider<BlobCrypter> {

		public BlobCrypter get() {
			byte[] random = new byte[32];
			new SecureRandom().nextBytes(random);
			String line = Base64.encodeBase64String(random);
			line = line.trim();
			byte[] keyBytes = CharsetUtil.getUtf8Bytes(line);
			BlobCrypter crypter = new BasicBlobCrypter(keyBytes);
			return crypter;
		}

	}

}
