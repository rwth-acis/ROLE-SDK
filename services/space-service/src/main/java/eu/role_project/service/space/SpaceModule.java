package eu.role_project.service.space;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Guard;
import se.kth.csc.kmr.conserve.Initializer;
import se.kth.csc.kmr.conserve.Responder;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.core.Contapp;
import se.kth.csc.kmr.conserve.logic.ResourceResponder;
import se.kth.csc.kmr.conserve.util.TemplateHTMLEscaper;
import se.kth.csc.kmr.conserve.util.TemplateManager;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import eu.role_project.service.resource.ROLETerms;
import eu.role_project.service.shindig.RaveResponder;

public class SpaceModule extends AbstractModule {

	private static Logger log = LoggerFactory.getLogger(SpaceModule.class);

	@Override
	protected void configure() {
		log.info("Configuring Space module");
		MapBinder<UUID, Responder> responders = Contapp
				.newResponderBinder(binder());
		MapBinder<UUID, Guard> guards = Contapp.newGuardBinder(binder());
		Multibinder<Initializer> initializers = Contapp
				.newInitializerBinder(binder());
		// MapBinder<UUID, Listener> listeners = Contapp
		// .newListenerBinder(binder());

		responders.addBinding(ROLETerms.spaceService).to(SpaceService.class);
		responders.addBinding(ROLETerms.activityService).to(
				ActivityService.class);
		responders.addBinding(ROLETerms.space).to(Space.class);
		responders.addBinding(ROLETerms.tool).to(Tool.class);

		// responders.addBinding(ROLETerms.ple).to(PLEResponder.class);
		responders.addBinding(ROLETerms.widget).to(Widget.class);
		responders.addBinding(ROLETerms.spaceSystemData).to(
				SpaceSystemResponder.class);
		responders.addBinding(ConserveTerms.system).to(
				SpaceSystemResponder.class);

		responders.addBinding(ROLETerms.rave).to(RaveResponder.class);
		responders.addBinding(RaveResponder.FakeID).to(RaveResponder.class);
		guards.addBinding(RaveResponder.FakeID).to(RaveResponder.class);

		// binder().bind(TemplateFactory.class)
		// .annotatedWith(Names.named("resource")).to(Home.class);
		// responders.addBinding(ConserveTerms.hasService).to(Home.class);

		responders.addBinding(ROLETerms.activity).to(ResourceResponder.class);

		guards.addBinding(SpaceService.ID).to(SpaceGuard.class);

		// listeners.addBinding(
		// UUID.fromString("237b34eb-b71b-4b69-a68b-c97249f759f6")).to(
		// UserListener.class); // UUID from user-service's
		// // UserService.java

		binder().bind(TemplateManager.class)
				.annotatedWith(Names.named("space"))
				.toProvider(new Provider<TemplateManager>() {
					@Inject
					@Named("staticrs")
					Map<String, String> staticrs;

					public TemplateManager get() {
						return new TemplateManager("templates").renderer(
								new TemplateHTMLEscaper()).map("static",
								staticrs);
					}
				}).in(Scopes.SINGLETON);

		initializers.addBinding().to(SpaceService.class);
		initializers.addBinding().to(TestInitializer.class);
	}

}