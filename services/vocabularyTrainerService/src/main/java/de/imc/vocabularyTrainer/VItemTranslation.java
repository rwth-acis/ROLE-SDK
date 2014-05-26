package de.imc.vocabularyTrainer;

import org.json.JSONException;
import org.json.JSONObject;

public class VItemTranslation {
	
	int translationId;
	String translation;
	
	public VItemTranslation(int translationId, String translation) {
		super();
		this.translationId = translationId;
		this.translation = translation;
	}	
	
	public VItemTranslation(JSONObject translationJSON) {
		super();
		try {
			this.translationId = translationJSON.getInt("translationId");
			this.translation = translationJSON.getString("translation");
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
		
	public JSONObject toJSON(){
		JSONObject contextJSON = new JSONObject();
		
		try {
			
			contextJSON.put("translationId", getTranslationId());
			contextJSON.put("translation", getTranslation());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return contextJSON;
	}
	
	public int getTranslationId() {
		return translationId;
	}
	public void setTranslationId(int translationId) {
		this.translationId = translationId;
	}
	public String getTranslation() {
		return translation;
	}
	public void setTranslation(String translation) {
		this.translation = translation;
	}


}
