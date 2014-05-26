package de.imc.advancedMediaSearch.restlet.application;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import de.imc.advancedMediaSearch.restlet.resource.DocumentationServerResource;
import de.imc.advancedMediaSearch.restlet.resource.EntityDetailResource;
import de.imc.advancedMediaSearch.restlet.resource.FeedResource;
import de.imc.advancedMediaSearch.restlet.resource.MediaTypeResource;
import de.imc.advancedMediaSearch.restlet.resource.RepositoryResource;
import de.imc.advancedMediaSearch.restlet.resource.SearchResource;




public class AMSApplication extends Application{

	public static final String APPLICATIONID = "richMediaSearch.com";
	
    @Override
	public synchronized Restlet createRoot() {
        
    	// Create a router Restlet that defines routes.
        Router router = new Router(getContext());
  
        // Defines a route for the resource "search"
        router.attach("/search/{queryType}", SearchResource.class);   
        
        // Defines a route to the old search resource
        router.attach("/searchold/{searchType}", FeedResource.class);  
        
        //define entity route
        router.attach("/entity",EntityDetailResource.class); 
        
        // Defines a route for the resource "repositories"
        router.attach("/repositories", RepositoryResource.class);
        
        //attaching mediatype access point
        router.attach("/mediatypes", MediaTypeResource.class);
        
        //attaching documentation access point
        router.attach("/documentation", DocumentationServerResource.class);
        
        return router;
    }	
	
}
