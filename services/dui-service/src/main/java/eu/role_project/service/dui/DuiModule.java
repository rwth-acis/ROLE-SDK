package eu.role_project.service.dui;

import java.util.UUID;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import se.kth.csc.kmr.conserve.Guard;
import se.kth.csc.kmr.conserve.Initializer;
import se.kth.csc.kmr.conserve.Listener;
import se.kth.csc.kmr.conserve.Responder;
import se.kth.csc.kmr.conserve.core.Contapp;


import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;

import eu.role_project.service.dui.iface.DUIService;
import eu.role_project.service.dui.iface.XmppProxy;
import eu.role_project.service.dui.impl.DuiServiceImpl;
import eu.role_project.service.dui.impl.DuiXmppProxy;
import eu.role_project.service.resource.ROLETerms;


public class DuiModule extends ServletModule {
	private static Logger log = LoggerFactory.getLogger(DuiModule.class);
	@Override
	protected void configureServlets() {
		log.info("KELI HERE!! DuiModule ----------------------------------configuring-----------");
		MapBinder<UUID, Guard> guards = Contapp.newGuardBinder(binder());
		MapBinder<UUID, Responder> responders = Contapp.newResponderBinder(binder());
		MapBinder<UUID, Listener> listeners = Contapp.newListenerBinder(binder());
		Multibinder<Initializer> initializers = Contapp.newInitializerBinder(binder());
		
		//add a listener to widget actions, typically the POST reqs
		listeners.addBinding(ROLETerms.tool).to(DuiListener.class);
		//bind the dui responder to operate on request with the url pattern({roothost}/dui) 
		responders.addBinding(ROLETerms.duiService).to(DuiResponder.class);
		//a guard before the dui responder
		guards.addBinding(DuiResponder.ID).to(DuiGuard.class);
		
		//init the xmpp publish feature from the real time service
		bind(XmppProxy.class).toInstance(new DuiXmppProxy());
		//init the dui service 
		bind(DUIService.class).toInstance(new DuiServiceImpl());

		
//		bind(DuiHttpFilter.class).in(Singleton.class);
//		filter("/demo").through(DuiHttpFilter.class);
//		bind(DuiHttpServlet.class).in(Singleton.class);
//		serve("/demo").with(DuiHttpServlet.class);
		
		//let dui responder initialize the context for the url pattern ({roothost}/dui)
		initializers.addBinding().to(DuiResponder.class);
	}

}
