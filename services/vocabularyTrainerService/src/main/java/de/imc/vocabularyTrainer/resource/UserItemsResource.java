package de.imc.vocabularyTrainer.resource;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import de.imc.vocabularyTrainer.VItem;

public class UserItemsResource extends BaseResource {
	
	int listId;
	String userName;
	
	@Override  
	protected void doInit() throws ResourceException {  
			
		String listIdString = (String) getRequest().getAttributes().get("listId");
		listId = Integer.valueOf(listIdString);
		
		userName = (String) getRequest().getAttributes().get("userName");
		
		

	}	
	
	/**
	 * Handle GET requests: return the userlists for the given user id
	 */	
	@Get
	public Representation represent(){
		
		Representation result;
		JSONArray jsonArray = new JSONArray();
				
		int userId = super.getDBWrapper().getUserId(userName);
		
		if(userId == 0){
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			result =  generateErrorRepresentation("User does not exists","");
		}else{
		
			List<List<VItem>> itemLists = super.getDBWrapper().getUserItems(listId,userId);
			
			Iterator<List<VItem>> listIter = itemLists.iterator();
			while(listIter.hasNext()){
				List<VItem> tmpList = listIter.next();
				Iterator<VItem> itemIter = tmpList.iterator();
				JSONArray tmpJSONArray = new JSONArray();
				
				while(itemIter.hasNext()){
					VItem tmpItem = itemIter.next();
					tmpJSONArray.put(tmpItem.toJSON());
					
				}
				jsonArray.put(tmpJSONArray);
				
			}
			result = new JsonRepresentation(jsonArray);
		}
		return result;
		
	}	
}
