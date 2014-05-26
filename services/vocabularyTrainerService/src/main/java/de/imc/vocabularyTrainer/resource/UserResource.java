package de.imc.vocabularyTrainer.resource;

import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import de.imc.vocabularyTrainer.VIList;
import de.imc.vocabularyTrainer.VTUser;



public class UserResource extends BaseResource {
	
	String userName;
	
	@Override  
	protected void doInit() throws ResourceException {  
			
		userName = (String) getRequest().getAttributes().get("userName");

	}
		
	/**
	 * Handle POST requests: create a new item.
	 */
	@Post
	public Representation acceptItem(Representation entity) {
		
		// Parse the given representation and retrieve pairs of
		// "name=value" tokens.
		Form form = new Form(entity);
		String userName = form.getFirstValue("userName");
		String password = form.getFirstValue("password");
		String email = form.getFirstValue("email");
		
		String firstListname = form.getFirstValue("firstListname");
		String firstSourceLanguage = form.getFirstValue("firstSourceLanguage");
		String firstTargetLanguage = form.getFirstValue("firstTargetLanguage");
		
		if(password == null){
			password ="";
		}
		
		if(email == null){
			email ="";
		}
		
		// Register the new user if one is not already registered.
		int userId = super.getDBWrapper().getUserId(userName);
		
		if(userId != 0){
			//setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return  generateErrorRepresentation("User "+userName+" already exists","");
		}else{
			
			//create the first list for the user
			VIList firstList = new VIList(0, firstListname, firstSourceLanguage,
					firstTargetLanguage, 0);
			
			// Set the response's status and entity			
			VTUser user = new VTUser(0,userName,email);
			
			super.getDBWrapper().createUser(user, password, firstList);
			//setStatus(Status.SUCCESS_CREATED);
			
			return  generateSuccessRepresentation("User created");
		}
		/*
		try {
			System.out.println(result.getText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
		*/
	}
	
	/**
	 * Handle GET requests: return the user with the given user id
	 */	
	@Get
	public Representation represent(){
		
		if(userName == null){
			return generateErrorRepresentation("No username was given","");
		}
		
		Representation result;
		
		if(super.getDBWrapper() == null){
			return generateErrorRepresentation("Database connection was not initialized correctly","");
		}
		
		int userId = super.getDBWrapper().getUserId(userName);
		
		VTUser user = super.getDBWrapper().fetchUser(userId);
		
		if(user == null){
			return generateErrorRepresentation("There exists no user for this name","");
		}
			result = new JsonRepresentation(user.toJSON());
		
		return result;
		
	}
	
 
}
