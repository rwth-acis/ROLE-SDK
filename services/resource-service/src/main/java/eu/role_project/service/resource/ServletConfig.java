package eu.role_project.service.resource;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContextEvent;

import org.cometd.bayeux.server.BayeuxServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

public class ServletConfig extends GuiceServletContextListener {

	private static Logger log = LoggerFactory.getLogger(ServletConfig.class);

	private static String[] moduleNames = {
			"eu.role_project.service.resource.ResourceModule",
			"eu.role_project.service.space.ShindigModule",
			"eu.role_project.service.user.UserModule",
			"eu.role_project.service.space.SpaceModule",
			"eu.role_project.service.realtime.RealtimeModule",
			"eu.role_project.service.resource.RESTEasyModule" };

	private static String contextPath = null;

	private static BayeuxServer bayeux = null;

	@Override
	protected Injector getInjector() {
		log.info("Loading modules");
		List<Module> modules = new LinkedList<Module>();
		for (String name : moduleNames) {
			try {
				Object module = Class.forName(name, false,
						this.getClass().getClassLoader()).newInstance();
				if (module instanceof Module) {
					modules.add((Module) module);
					log.info(name + " module instantiated");
				} else {
					log.warn(name + " is not a module");
				}
			} catch (ClassNotFoundException e) {
				log.info(name + " module not available");
			} catch (InstantiationException e) {
				log.warn(name + " module could not be instantiated", e);
			} catch (IllegalAccessException e) {
				log.warn(name + " module could not be instantiated", e);
			}
		}
		return Guice.createInjector(modules);
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		contextPath = servletContextEvent.getServletContext().getContextPath();
		bayeux = (BayeuxServer) servletContextEvent.getServletContext()
				.getAttribute(BayeuxServer.ATTRIBUTE);
		super.contextInitialized(servletContextEvent);
	}

	public static String getContextPath() {
		return contextPath;
	}

	public static BayeuxServer getBayeux() {
		return bayeux;
	}

}
