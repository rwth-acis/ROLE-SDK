package de.imc.vocabularyTrainer.resource;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

import de.imc.vocabularyTrainer.application.VTApplication;
import de.imc.vocabularyTrainer.database.DatabaseWrapper;

/**
 * Base resource class that supports common behaviours or attributes shared by
 * all resources.
 * 
 */

public abstract class BaseResource extends ServerResource  {


	/**
     * Returns the connection managed by this application.
     * 
     * @return the connection managed by this application.
     */
    protected DataSource getConnection() {
        return ((VTApplication) getApplication()).getConnection();
    }
    
    protected DatabaseWrapper getDBWrapper() {
        return ((VTApplication) getApplication()).getDBWrapper();
    }
    
    /** 
     * Generate an JSON representation of an error response. 
     *  
     * @param errorMessage 
     *            the error message. 
     * @param errorCode 
     *            the error code. 
     */  
    protected Representation generateErrorRepresentation(String errorMessage,  
            String errorCode) {  
   	JsonRepresentation result = null;  

   	JSONObject errorMsg = new JSONObject();
   	
   	try {
   		errorMsg.put("errorCode", errorCode);
			errorMsg.put("errorMessage", errorMessage);
   	} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in generateErrorRepresentation "+e);
		}
   	  	
   	result = new JsonRepresentation(errorMsg);  
   	
   	return result;  
    } 
   
   /** 
    * Generate an JSON representation of an error response. 
    *  
    * @param errorMessage 
    *            the error message. 
    * @param errorCode 
    *            the error code. 
    */  
    protected Representation generateSuccessRepresentation(String successMessage) {  
  	JsonRepresentation result = null;  

  	JSONObject successMsg = new JSONObject();
  	
  	try {
  		successMsg.put("successMessage", successMessage);
  	} catch (JSONException e) {
			// TODO Auto-generated catch block
  			System.out.println("Error in generateSuccessRepresentation "+e);
		}
  	  	
  	result = new JsonRepresentation(successMsg);  
  	
  	return result;  
   }    
    
}
