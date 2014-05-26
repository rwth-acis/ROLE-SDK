package de.imc.vocabularyTrainer.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class LoginRessource extends BaseResource {

	/**
	 * Handle GET requests: return the user with the given user id
	 */	
	@Get
	public Representation represent(){
		
		Representation returnMsg = 
			generateSuccessRepresentation("Login succesful"); 
		
		return returnMsg;
		
	}	
	
}
