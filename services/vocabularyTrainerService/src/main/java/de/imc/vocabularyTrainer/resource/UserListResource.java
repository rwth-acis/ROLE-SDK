package de.imc.vocabularyTrainer.resource;

import org.restlet.resource.ResourceException;

public class UserListResource extends BaseResource{

	String userName;
	
	@Override  
	protected void doInit() throws ResourceException {  
			
		userName = (String) getRequest().getAttributes().get("userName");

	}	
	
}
