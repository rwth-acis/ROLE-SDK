package de.imc.vocabularyTrainer;

import org.json.JSONException;
import org.json.JSONObject;

public class VIList {

	
	//{"listId":number,"listName":String,"sourceLanguage":String,
	//"targetLanguage":String,"numberOfItems":number}
	
	int listId;
	String listName;
	String sourceLanguage;
	String targetLanguage;
	int numberOfItems;
	
	public VIList(int listId, String listName, String sourceLanguage,
			String targetLanguage, int numberOfItems) {
		super();
		this.listId = listId;
		this.listName = listName;
		this.sourceLanguage = sourceLanguage;
		this.targetLanguage = targetLanguage;
		this.numberOfItems = numberOfItems;
	}
	
	public VIList(JSONObject listJSON) {
		super();
		try {
			this.listId = listJSON.getInt("listId");
			this.listName = listJSON.getString("listName");
			this.sourceLanguage = listJSON.getString("sourceLanguage");
			this.targetLanguage = listJSON.getString("targetLanguage");
			this.numberOfItems = listJSON.getInt("numberOfItems");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	
	public JSONObject toJSON(){
		JSONObject listJSON = new JSONObject();
		
		try {
			listJSON.put("listId", listId);
			listJSON.put("listName", listName);
			listJSON.put("sourceLanguage", sourceLanguage);
			listJSON.put("targetLanguage", targetLanguage);
			listJSON.put("numberOfItems", numberOfItems);
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return listJSON;
	}		
	
	public int getListId() {
		return listId;
	}
	public void setListId(int listId) {
		this.listId = listId;
	}
	public String getListName() {
		return listName;
	}
	public void setListName(String listName) {
		this.listName = listName;
	}
	public String getSourceLanguage() {
		return sourceLanguage;
	}
	public void setSourceLanguage(String sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}
	public String getTargetLanguage() {
		return targetLanguage;
	}
	public void setTargetLanguage(String targetLanguage) {
		this.targetLanguage = targetLanguage;
	}
	public int getNumberOfItems() {
		return numberOfItems;
	}
	public void setNumberOfItems(int numberOfItems) {
		this.numberOfItems = numberOfItems;
	}

	
}
