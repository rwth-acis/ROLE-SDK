package de.imc.vocabularyTrainer;

import org.json.JSONException;
import org.json.JSONObject;

public class VItemImage {
	
	int imageId;
	String imageURL;
	
	public VItemImage(int imageId, String imageURL) {
		super();
		this.imageId = imageId;
		this.imageURL = imageURL;
	}
	
	public VItemImage(JSONObject imageJSON) {
		super();
		try {
			this.imageId = imageJSON.getInt("imageId");
			this.imageURL = imageJSON.getString("imageURL");
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
		
	public JSONObject toJSON(){
		JSONObject contextJSON = new JSONObject();
		
		try {
			
			contextJSON.put("imageId", getImageId());
			contextJSON.put("imageURL", getImageURL());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return contextJSON;
	}	
	
	public int getImageId() {
		return imageId;
	}
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}


}
