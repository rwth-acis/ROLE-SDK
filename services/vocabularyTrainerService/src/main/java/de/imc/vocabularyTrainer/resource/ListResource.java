package de.imc.vocabularyTrainer.resource;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import de.imc.vocabularyTrainer.VIList;

/**
 * Resource that manages a list of items.
 * 
 */
public class ListResource extends BaseResource {

	
	int listId;
	String userName;
	
	@Override  
	protected void doInit() throws ResourceException {  
			
		String listIdString = (String) getRequest().getAttributes().get("listId");
		if(listIdString != null){
			listId = Integer.valueOf(listIdString);
		}
		
		userName = (String) getRequest().getAttributes().get("userName");
	
	}	
	
	/**
	 * Handle POST requests: creates a new list.
	 * @throws JSONException 
	 */
	@Post
	public Representation acceptItem(Representation entity){
		
		if(userName == null){
			return generateErrorRepresentation("No username was given","");
		}
		
		int userId = super.getDBWrapper().getUserId(userName);
		
		if(userId == 0){
			return generateErrorRepresentation("There exists no user for this name","");
		}
		
		
		Representation result = null;
		
		JsonRepresentation represent;
		try {
			represent = new JsonRepresentation(entity);
			JSONObject jsonobject = represent.toJsonObject();
			VIList list = new VIList(jsonobject);
	        
			super.getDBWrapper().createList(list, userId);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result =  generateErrorRepresentation("IOException","");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result =  generateErrorRepresentation("JSONException","");
		}
        
		result =  generateSuccessRepresentation("List created");
	
		return result;
	}
	
	
	/** 
      * Handle DELETE requests: deletes a list
      */  
     @Delete  
     public Representation removeItem() {  
    	 Representation result = null;
    	 
    	 super.getDBWrapper().deleteList(listId);
    	 
    	 result =  generateSuccessRepresentation("List deleted");
    	 
    	 return result;
     } 
}
