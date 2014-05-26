package de.imc.vocabularyTrainer;

import org.json.JSONException;
import org.json.JSONObject;

public class VItemContext {

	int contextId;
	String context;
	String source;
	
	public VItemContext(int contextId, String context, String source) {
		super();
		this.contextId = contextId;
		this.context = context;
		this.source = source;
	}
	
	public VItemContext(JSONObject contextJSON) {
		super();
		try {
			this.contextId = contextJSON.getInt("contextId");
			this.context = contextJSON.getString("context");
			this.source = contextJSON.getString("source");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	
	public JSONObject toJSON(){
		JSONObject contextJSON = new JSONObject();
		
		try {
			contextJSON.put("contextId", getContextId());
			contextJSON.put("context", getContext());
			contextJSON.put("source", getSource());
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return contextJSON;
	}	

	public int getContextId() {
		return contextId;
	}

	public void setContextId(int contextId) {
		this.contextId = contextId;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	
	
}
