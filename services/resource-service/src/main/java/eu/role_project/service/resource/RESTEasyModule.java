package eu.role_project.service.resource;

import com.google.inject.servlet.ServletModule;

public class RESTEasyModule extends ServletModule {

	@Override
	protected void configureServlets() {
		serveRegex("^((?!/s/).*)$").with(GuiceHttpServletDispatcher.class);
	}

}