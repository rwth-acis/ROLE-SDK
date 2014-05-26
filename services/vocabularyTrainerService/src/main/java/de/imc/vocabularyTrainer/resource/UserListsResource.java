package de.imc.vocabularyTrainer.resource;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import de.imc.vocabularyTrainer.UserVIList;

public class UserListsResource extends BaseResource{

	String userName;
	
	@Override  
	protected void doInit() throws ResourceException {  
			
		userName = (String) getRequest().getAttributes().get("userName");

	}	
	
	/**
	 * Handle GET requests: return the userlists for the given user id
	 */	
	@Get
	public Representation represent(){
		
		Representation result;
		JSONArray jsonArray = new JSONArray();
		
		if(userName == null){
			return generateErrorRepresentation("No username was given","");
		}
		
		int userId = super.getDBWrapper().getUserId(userName);
		
		if(userId == 0){
			return generateErrorRepresentation("There exists no user for this name","");
		}
		
		List<UserVIList> list = super.getDBWrapper().getUserLists(userId);
		
		Iterator<UserVIList> iter = list.iterator();
		
		while(iter.hasNext()){
			jsonArray.put(iter.next().toJSON());
		}
		
		result = new JsonRepresentation(jsonArray);
		
		return result;
		
	}	
	
	
}
