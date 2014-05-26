package eu.role_project.service.user;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Guard;
import se.kth.csc.kmr.conserve.Initializer;
import se.kth.csc.kmr.conserve.Responder;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.core.Contapp;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

import eu.role_project.service.resource.ROLETerms;

public class UserModule extends AbstractModule {

	private static Logger log = LoggerFactory.getLogger(UserModule.class);

	@Override
	protected void configure() {
		log.info("Configuring User module");
		MapBinder<UUID, Responder> responders = Contapp
				.newResponderBinder(binder());
		MapBinder<UUID, Guard> guards = Contapp.newGuardBinder(binder());
		Multibinder<Initializer> initializers = Contapp
				.newInitializerBinder(binder());

		initializers.addBinding().to(UserService.class);

		responders.addBinding(ROLETerms.userService).to(UserService.class);
		responders.addBinding(ROLETerms.member).to(Member.class);
		responders.addBinding(ConserveTerms.owner).to(Member.class);
		// responders.addBinding(ROLETerms.profile).to(UserProfile.class);

		guards.addBinding(UserService.ID).to(UserGuard.class);

	}

}