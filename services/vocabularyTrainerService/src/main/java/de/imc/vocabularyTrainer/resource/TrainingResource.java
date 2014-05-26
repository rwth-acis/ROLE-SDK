package de.imc.vocabularyTrainer.resource;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

public class TrainingResource extends BaseResource{
	
	String userName;
	int itemId;
	
	@Override  
	protected void doInit() throws ResourceException {  
			
		userName = (String) getRequest().getAttributes().get("userName");
		
		String itemIdString = (String) getRequest().getAttributes().get("itemId");
		itemId = Integer.valueOf(itemIdString);
	
	}
		
	/**
	 * Handle PUT requests: create a new item.
	 * @throws JSONException 
	 */
	@Put
	public Representation acceptItem(Representation entity){
		
		int userId = super.getDBWrapper().getUserId(userName);
				
		Representation result = null;

		if(userId == 0){
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			result =  generateErrorRepresentation("User does not exists","");
		}else{
			
			JsonRepresentation represent;
			try {
				represent = new JsonRepresentation(entity);
				JSONObject jsonobject = represent.toJsonObject();
				
				boolean answerCorrect = jsonobject.getBoolean("answerCorrect");
		        
				super.getDBWrapper().itemTrained(itemId, userId, answerCorrect);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result =  generateErrorRepresentation("IOException","");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result =  generateErrorRepresentation("JSONException","");
			}
	        
			result =  generateSuccessRepresentation("Item updated");
		}
		return result;
	}
}
