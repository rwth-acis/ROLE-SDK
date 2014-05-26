package de.imc.vocabularyTrainer;

import org.json.JSONException;
import org.json.JSONObject;

public class VTUser {

	int userId;
	String username;
	String email;
	
	public VTUser(int userId, String username, String email) {
		super();
		setUserId(userId);
		setUsername(username);
		setEmail(email);
	}

	public VTUser(JSONObject userJSON){
		try {
			setUserId(userJSON.getInt("username"));
			setUsername(userJSON.getString("username"));	
			setEmail(userJSON.getString("email"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public JSONObject toJSON(){
		JSONObject userJSON = new JSONObject();
		
		try {
			userJSON.put("userId", getUserId());
			userJSON.put("username", getUsername());
			userJSON.put("email", getEmail());
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return userJSON;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
