package eu.role_project.service.resource;

import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.guice.GuiceResourceFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.GetRestful;

import com.google.inject.Binding;
import com.google.inject.Injector;

@Singleton
public class GuiceHttpServletDispatcher extends HttpServletDispatcher {

	private static final long serialVersionUID = 1L;

	@Inject
	private Injector injector;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		final ServletContext servletContext = servletConfig.getServletContext();
		final Registry registry = (Registry) servletContext
				.getAttribute(Registry.class.getName());
		final ResteasyProviderFactory providerFactory = (ResteasyProviderFactory) servletContext
				.getAttribute(ResteasyProviderFactory.class.getName());
		for (final Binding<?> binding : injector.getBindings().values()) {
			final Type type = binding.getKey().getTypeLiteral().getType();
			if (type instanceof Class) {
				final Class<?> beanClass = (Class<?>) type;
				if (GetRestful.isRootResource(beanClass)) {
					final ResourceFactory resourceFactory = new GuiceResourceFactory(
							binding.getProvider(), beanClass);
					// log.info("registering factory for {0}",
					// beanClass.getName());
					registry.addResourceFactory(resourceFactory);
				}
				if (beanClass.isAnnotationPresent(Provider.class)) {
					// log.info("registering provider instance for {0}",
					// beanClass.getName());
					providerFactory.registerProviderInstance(binding
							.getProvider().get());
				}
			}
		}

	}
}
