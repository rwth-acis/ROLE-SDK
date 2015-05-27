package eu.role_project.service.resource;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import net.sf.ehcache.constructs.web.filter.SimpleCachingHeadersPageCachingFilter;

import org.cometd.server.CometDServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Application;
import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Guard;
import se.kth.csc.kmr.conserve.Initializer;
import se.kth.csc.kmr.conserve.Listener;
import se.kth.csc.kmr.conserve.Responder;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.core.Contapp;
import se.kth.csc.kmr.conserve.core.ContempModule;
import se.kth.csc.kmr.conserve.iface.jaxrs.PostRequestFilter;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.logic.AuthenticationResponder;
import se.kth.csc.kmr.conserve.logic.ConceptResponder;
import se.kth.csc.kmr.conserve.logic.ConfigurationResponder;
import se.kth.csc.kmr.conserve.logic.ContentResponder;
import se.kth.csc.kmr.conserve.logic.ContextResponder;
import se.kth.csc.kmr.conserve.logic.ControlResponder;
import se.kth.csc.kmr.conserve.logic.FeedResponder;
import se.kth.csc.kmr.conserve.logic.HTMLAppResponder;
import se.kth.csc.kmr.conserve.logic.IndexResponder;
import se.kth.csc.kmr.conserve.logic.MetaMetadataResponder;
import se.kth.csc.kmr.conserve.logic.MetadataResponder;
import se.kth.csc.kmr.conserve.logic.ReferenceResponder;
import se.kth.csc.kmr.conserve.logic.RepositoryResponder;
import se.kth.csc.kmr.conserve.logic.RepresentationMetaResponder;
import se.kth.csc.kmr.conserve.logic.RepresentationResponder;
import se.kth.csc.kmr.conserve.logic.ResourceResponder;
import se.kth.csc.kmr.conserve.logic.SecretResponder;
import se.kth.csc.kmr.conserve.security.oauth.OAuthListener;
import se.kth.csc.kmr.conserve.security.oauth.OAuthSessionPrincipal;
import se.kth.csc.kmr.conserve.security.oauth.OAuthStoreImpl;
import se.kth.csc.kmr.conserve.security.openid.OpenID;
import se.kth.csc.kmr.conserve.security.openid.OpenIDModule;
import se.kth.csc.kmr.conserve.security.oauth2.OAuth2Module;
import se.kth.csc.kmr.conserve.security.session.SessionListener;
import se.kth.csc.kmr.conserve.security.session.SessionLogin;
import se.kth.csc.kmr.conserve.util.Base64UUID;
import se.kth.csc.kmr.conserve.util.TemplateHTMLEscaper;
import se.kth.csc.kmr.conserve.util.TemplateManager;
import se.kth.csc.kmr.conserve.util.WebAppServlet;
import se.kth.csc.kmr.oauth.OAuth1Module;
import se.kth.csc.kmr.oauth.OAuth1Store;
import se.kth.csc.kmr.oauth.OAuthPrincipal;
import se.kth.csc.kmr.staticrs.Static;
import se.kth.csc.kmr.staticrs.StaticRS;
import se.kth.csc.kmr.staticrs.StaticRSInitializer;
import se.kth.csc.kmr.staticrs.StaticRSModule;

import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

public class ResourceModule extends ServletModule {

	private static Logger log = LoggerFactory.getLogger(ResourceModule.class);

	@Override
	protected void configureServlets() {
		install(new StaticRSModule());
		install(new ContempModule());
		install(new OAuth1Module());
		install(new OpenIDModule());
		install(new OAuth2Module());

		log.info("Configuring Resource module");

		MapBinder<String, ServiceConfiguration> serviceInstances = MapBinder
				.newMapBinder(binder(), String.class,
						ServiceConfiguration.class);
		final UriBuilder baseUriBuilder = UriBuilder.fromUri("http://"
				+ getServerHostname()
				+ ((!"80".equals(getServerPort())) ? (":" + getServerPort())
						: "") + "/");
		serviceInstances.addBinding(
				baseUriBuilder.clone().path("spaces").build().toString())
				.toInstance(new ServiceConfiguration() {
					public String getRequestUrl(OAuthEndpointSetup setup,
							Map<String, String> attribs, Concept context,
							Concept user) {
						return baseUriBuilder
								.clone()
								.path("o/oauth/initiate")
								.queryParam("scope",
										Base64UUID.encode(context.getUuid()))
								.build().toString();
					}

					public String getAuthorizationUrl(OAuthEndpointSetup setup,
							Map<String, String> attribs, Concept context,
							Concept user) {
						UriBuilder urib = baseUriBuilder.clone().path(
								"o/oauth/authorize");
						ServiceConfiguration userService = setup
								.getConfig("http://www.role-project.eu/rdf/userService");
						if (userService != null) {
							urib.queryParam("openid",
									userService.getOpenID(setup, user));
						}
						return urib.build().toString();
					}

					public String getAccessUrl(OAuthEndpointSetup setup,
							Map<String, String> attribs, Concept context,
							Concept user) {
						return baseUriBuilder.clone().path("o/oauth/token")
								.build().toString();
					}

					public String getRequestMethod(OAuthEndpointSetup setup,
							Map<String, String> attribs, Concept context,
							Concept user) {
						return "GET";
					}

					public String getAccessMethod(OAuthEndpointSetup setup,
							Map<String, String> attribs, Concept context,
							Concept user) {
						return "GET";
					}

					public String getOpenID(OAuthEndpointSetup setup,
							Concept user) {
						return null;
					}
				});
		serviceInstances.addBinding(
				baseUriBuilder.clone().path("users").build().toString())
				.toInstance(new ServiceConfiguration() {
					public String getRequestUrl(OAuthEndpointSetup setup,
							Map<String, String> attribs, Concept context,
							Concept user) {
						return baseUriBuilder
								.clone()
								.path("o/oauth/initiate")
								.queryParam("scope",
										Base64UUID.encode(context.getUuid()))
								.build().toString();
					}

					public String getAuthorizationUrl(OAuthEndpointSetup setup,
							Map<String, String> attribs, Concept context,
							Concept user) {
						UriBuilder urib = baseUriBuilder.clone().path(
								"o/oauth/authorize");
						urib.queryParam("openid", getOpenID(setup, user));
						return urib.build().toString();
					}

					public String getAccessUrl(OAuthEndpointSetup setup,
							Map<String, String> attribs, Concept context,
							Concept user) {
						return baseUriBuilder.clone().path("o/oauth/token")
								.build().toString();
					}

					public String getRequestMethod(OAuthEndpointSetup setup,
							Map<String, String> attribs, Concept context,
							Concept user) {
						return "GET";
					}

					public String getAccessMethod(OAuthEndpointSetup setup,
							Map<String, String> attribs, Concept context,
							Concept user) {
						return "GET";
					}

					public String getOpenID(OAuthEndpointSetup setup,
							Concept user) {
						return baseUriBuilder.clone().path("o/openid").build()
								.toString();
					}
				});

		binder().bind(UUID.class)
				.annotatedWith(Names.named("conserve.user.context"))
				// UUID from user-service's UserService.java
				.toInstance(
						UUID.fromString("237b34eb-b71b-4b69-a68b-c97249f759f6"));
		binder().bind(UUID.class)
				.annotatedWith(Names.named("conserve.user.predicate"))
				.toInstance(ROLETerms.member);

		MapBinder<UUID, Responder> responders = Contapp
				.newResponderBinder(binder());
		MapBinder<UUID, Guard> guards = Contapp.newGuardBinder(binder());
		MapBinder<UUID, Listener> listeners = Contapp
				.newListenerBinder(binder());
		Multibinder<Initializer> initializers = Contapp
				.newInitializerBinder(binder());
		Multibinder<UUID> topics = Contapp.newTopicBinder(binder());
		MapBinder<String, String> namespaces = Contapp
				.newNamespaceBinder(binder());

		initializers.addBinding().to(ROLETerms.class);
		initializers.addBinding().to(TestDomainInitializer.class);

		MapBinder<String, Static> staticBinder = StaticRSModule
				.newBinder(binder());
		staticBinder.addBinding("openapp").toInstance(
				new Static("openapp.js", MediaType
						.valueOf("application/javascript")));
		staticBinder.addBinding("logo.small").toInstance(
				new Static("graphics/spidericon100x40.png", MediaType
						.valueOf("image/png")));
		staticBinder.addBinding("logo.icon").toInstance(
				new Static("graphics/role.ico", MediaType
						.valueOf("image/vnd.microsoft.icon")));
		staticBinder.addBinding("background.highlight").toInstance(
				new Static("graphics/wavebg.png", MediaType
						.valueOf("image/png")));
		staticBinder.addBinding("background.shade").toInstance(
				new Static("graphics/wavebg2.png", MediaType
						.valueOf("image/png")));
		staticBinder.addBinding("openid.provider.google").toInstance(
				new Static("graphics/google.png", MediaType
						.valueOf("image/png")));
		initializers.addBinding().to(StaticRSInitializer.class);
		binder().bind(StaticRS.class);

		binder().bind(Application.class).to(Contapp.class);

		namespaces.addBinding("").toInstance("http://purl.org/openapp/");
		namespaces.addBinding("openapp").toInstance("http://purl.org/openapp/");
		namespaces.addBinding("rest").toInstance("http://purl.org/openapp/");
		namespaces.addBinding("role").toInstance("http://purl.org/role/terms/");
		namespaces.addBinding("foaf").toInstance("http://xmlns.com/foaf/0.1/");
		namespaces.addBinding("owl").toInstance(
				"http://www.w3.org/2002/07/owl#");
		namespaces.addBinding("dc").toInstance("http://purl.org/dc/terms/");

		responders.addBinding(ConserveTerms.context).to(ContextResponder.class);
		responders.addBinding(ConserveTerms.control).to(ControlResponder.class);
		responders.addBinding(ConserveTerms.concept).to(ConceptResponder.class);
		// responders.addBinding(ConserveTerms.system).to(SystemResponder.class);
		responders.addBinding(ConserveTerms.content).to(ContentResponder.class);
		responders.addBinding(ConserveTerms.representation).to(
				RepresentationResponder.class);
		responders.addBinding(ConserveTerms.metametadata).to(
				MetaMetadataResponder.class);
		responders.addBinding(ConserveTerms.representationmetadata).to(
				RepresentationMetaResponder.class);
		responders.addBinding(ConserveTerms.metadata).to(
				MetadataResponder.class);
		responders.addBinding(ConserveTerms.secret).to(SecretResponder.class);
		responders.addBinding(ConserveTerms.reference).to(
				ReferenceResponder.class);
		responders.addBinding(ConserveTerms.openid).to(OpenID.class);
		responders.addBinding(ConserveTerms.index).to(IndexResponder.class);
		responders.addBinding(ConserveTerms.feed).to(FeedResponder.class);
		responders.addBinding(ConserveTerms.annotates).to(
				ResourceResponder.class);
		responders.addBinding(ROLETerms.data).to(ResourceResponder.class);
		responders.addBinding(ROLETerms.preferences)
				.to(ResourceResponder.class);
		// responders.addBinding(ConserveTerms.hasPart)
		// .to(ResourceResponder.class);
		responders.addBinding(ConserveTerms.root).to(RepositoryResponder.class);
		responders.addBinding(ConserveTerms.configuration).to(
				ConfigurationResponder.class);
		responders.addBinding(ConserveTerms.authentication).to(
				AuthenticationResponder.class);
		responders.addBinding(ConserveTerms.app).to(HTMLAppResponder.class);

		// responders.addBinding(ConserveTerms.root).to(ResourceResponder.class);
		responders.addBinding(ConserveTerms.hasService).to(Home.class);

		topics.addBinding().toInstance(ConserveTerms.system);
		topics.addBinding().toInstance(ConserveTerms.metadata);
		topics.addBinding().toInstance(ConserveTerms.representation);

		guards.addBinding(ConserveTerms.acl).to(SimpleACL.class);
		binder().bind(String.class).annotatedWith(Names.named("password-salt"))
				.toInstance(Base64UUID.encode(UUID.randomUUID()));

		initializers.addBinding().to(SessionListener.class);
		listeners.addBinding(ConserveTerms.root).to(SessionListener.class);
		listeners.addBinding(ConserveTerms.root).to(OAuthListener.class);
		binder().bind(OAuth1Store.class).to(OAuthStoreImpl.class);
		binder().bind(OAuthPrincipal.class).to(OAuthSessionPrincipal.class);

		binder().bind(SessionLogin.class);
		binder().bind(RequestImpl.class);

		binder().bind(TemplateManager.class)
				.annotatedWith(Names.named("resource"))
				.toProvider(new Provider<TemplateManager>() {
					@Inject
					@Named("staticrs")
					Map<String, String> staticrs;

					public TemplateManager get() {
						return new TemplateManager("templates")
								.renderer(new TemplateHTMLEscaper())
								.map("static", staticrs).newLayout("prototype")
								.include("body").build();
					}
				}).in(Scopes.SINGLETON);

		filter("/*").through(PostRequestFilter.class);
		Map<String, String> h2Params = new HashMap<String, String>();
		h2Params.put("webAllowOthers", "true");
		serve("/a/h2*").with(new org.h2.server.web.WebServlet(), h2Params);
		serve("/cometd/*").with(new CometDServlet());
		serve("/s/*").with(new WebAppServlet());
		Map<String, String> pageCacheParams = new HashMap<String, String>();
		pageCacheParams.put("suppressStackTrace", "false");
		pageCacheParams.put("cacheName",
				"SimpleCachingHeadersTimeoutPageCachingFilter");
		filter("/s/*").through(new SimpleCachingHeadersPageCachingFilter(),
				pageCacheParams);

		final String bootstrapRequestId = Base64UUID.encode(UUID.randomUUID());
		binder().bind(String.class)
				.annotatedWith(Names.named("bootstrap-request-id"))
				.toInstance(bootstrapRequestId);

		// Start a thread that will make a request to the server, as final
		// initialization is made during the first request
		log.info("Will bootstrap from path " + ServletConfig.getContextPath()
				+ "/" + bootstrapRequestId);
		new Thread() {
			@Override
			public void run() {
				try {
					URL bootstrap = new URL("http", getServerHostname(),
							Integer.valueOf(getServerPort()), "/"
									+ bootstrapRequestId);
					for (;;) {
						Thread.sleep(500);
						try {
							if (((HttpURLConnection) bootstrap.openConnection())
									.getResponseCode() != 503) {
								log.info("Initialization complete");
								return; // We're done!
							}
						} catch (IOException e) {
							// Try again
						}
					}
				} catch (NumberFormatException e) {
					log.error("Invalid server port", e);
				} catch (MalformedURLException e) {
					log.error("Invalid bootstrap URL", e);
				} catch (InterruptedException e) {
					log.error("Interrupted while waiting to bootstrap", e);
				}
			}
		}.start();
	}

	protected static String getServerPort() {
		return System.getProperty("shindig.port") != null ? System
				.getProperty("shindig.port") : (System
				.getProperty("jetty.port") != null ? System
				.getProperty("jetty.port") : "8080");
	}

	protected static String getServerHostname() {
		return System.getProperty("shindig.host") != null ? System
				.getProperty("shindig.host") : (System
				.getProperty("jetty.host") != null ? System
				.getProperty("jetty.host") : "localhost");
	}

}
