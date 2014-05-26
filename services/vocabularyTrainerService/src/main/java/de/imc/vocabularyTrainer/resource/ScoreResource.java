package de.imc.vocabularyTrainer.resource;


import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;


import de.imc.vocabularyTrainer.VTScore;

public class ScoreResource extends BaseResource{
	
	String userName;
	
	@Override  
	protected void doInit() throws ResourceException {  
		
		userName = (String) getRequest().getAttributes().get("userName");
				

	}	
	
	/**
	 * Handle GET requests: return the score for the given user id
	 */	
	@Get
	public Representation represent(){
		
		Representation result;
		JSONObject jsonObject;
				
		int userId = super.getDBWrapper().getUserId(userName);
		
		if(userId == 0){
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			result =  generateErrorRepresentation("User does not exists","");
		}else{
		
			VTScore score = super.getDBWrapper().fetchUserScore(userId);
			
			if(score == null){
				return  generateErrorRepresentation("There exists no Scores for user "+userName+" exists","");
			}
			
			jsonObject = score.toJSON();
			
			result = new JsonRepresentation(jsonObject);
		}
		return result;
		
	}
}
