package de.imc.vocabularyTrainer;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import de.imc.vocabularyTrainer.database.DatabaseWrapper;

public class UserVIList extends VIList{
	
	int userId;
	UserVIListBucket bucket1;
	UserVIListBucket bucket2;
	UserVIListBucket bucket3;
	UserVIListBucket bucket4;
	UserVIListBucket bucket5;
	
	private static Logger logger = Logger.getLogger(DatabaseWrapper.class);
	
	
	public UserVIList(int listId, String listName, String sourceLanguage,
			String targetLanguage, int numberOfItems,
			int userId,	
			UserVIListBucket bucket1, UserVIListBucket bucket2, UserVIListBucket bucket3,
			UserVIListBucket bucket4, UserVIListBucket bucket5) {
		super(listId, listName, sourceLanguage, targetLanguage, numberOfItems);
		
		this.userId = userId;
		this.bucket1 = bucket1;
		this.bucket2 = bucket2;
		this.bucket3 = bucket3;
		this.bucket4 = bucket4;
		this.bucket5 = bucket5;
	
	}

	public UserVIList(JSONObject listJSON) {
		super(listJSON);
		try {
		
			this.userId = listJSON.getInt("userId");
			this.bucket1 = new UserVIListBucket(listJSON.getJSONObject("bucket1"));
			this.bucket2 = new UserVIListBucket(listJSON.getJSONObject("bucket2"));
			this.bucket3 = new UserVIListBucket(listJSON.getJSONObject("bucket3"));
			this.bucket4 = new UserVIListBucket(listJSON.getJSONObject("bucket4"));
			this.bucket5 = new UserVIListBucket(listJSON.getJSONObject("bucket5"));
			
		} catch (JSONException e) {
			logger.error(e.toString());
		}		
	}

	public JSONObject toJSON(){
		JSONObject listJSON = super.toJSON();
		
		try {

			listJSON.put("userId", userId);
			listJSON.put("score", getScore());
			listJSON.put("progress", getProgress());
			listJSON.put("successRate", getSuccessRate());
			listJSON.put("correctAnswers", getCorrectAnswers());
			listJSON.put("wrongAnswers", getWrongAnswers());
			
			listJSON.put("bucket1", bucket1.toJSON());
			listJSON.put("bucket2", bucket2.toJSON());
			listJSON.put("bucket3", bucket3.toJSON());
			listJSON.put("bucket4", bucket4.toJSON());
			listJSON.put("bucket5", bucket5.toJSON());
		
		} catch (JSONException e) {
			logger.error(e.toString());
		}
		
		return listJSON;
	}		
	
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public double getScore() {
		double score = bucket1.getNumberOfItems()*0 
			+ bucket2.getNumberOfItems()*1 
			+ bucket3.getNumberOfItems()*2
			+ bucket4.getNumberOfItems()*3
			+ bucket5.getNumberOfItems()*4;
		
		double successRate = ((double)score)*getSuccessRate();
		
	    int decimalPlace = 2;
	    BigDecimal bd = new BigDecimal(successRate);
	    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_UP);
	    successRate = bd.doubleValue();
	    
		return successRate;
	}
	
	public int getNumberOfItems(){
		return bucket1.getNumberOfItems() 
		+ bucket2.getNumberOfItems() 
		+ bucket3.getNumberOfItems()
		+ bucket4.getNumberOfItems()
		+ bucket5.getNumberOfItems();
	}

	public double getProgress() {
		if(this.getNumberOfItems() == 0){
			return 0;
		}
		
		double process = ((double)bucket1.getNumberOfItems())*0
			+ ((double)bucket2.getNumberOfItems())*0.25 
			+ ((double)bucket3.getNumberOfItems())*0.5
			+ ((double)bucket4.getNumberOfItems())*0.75
			+ ((double)bucket5.getNumberOfItems())*1;
		
		process = process*(1/((double)this.getNumberOfItems()));
		
	    int decimalPlace = 2;
	    BigDecimal bd = new BigDecimal(process);
	    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_UP);
	    process = bd.doubleValue();
	    
		return process;
	}

	public double getSuccessRate() {
		double successRate = 
			((double)bucket1.getSuccessRate())*0 
			+ ((double)bucket2.getSuccessRate())*0.25 
			+ ((double)bucket3.getSuccessRate())*0.5
			+ ((double)bucket4.getSuccessRate())*0.75
			+ ((double)bucket5.getSuccessRate())*1.0;
		
		successRate =  successRate*(1.0/2.5);
		
	    int decimalPlace = 2;
	    BigDecimal bd = new BigDecimal(successRate);
	    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_UP);
	    successRate = bd.doubleValue();
	    
		return successRate;		
	}

	public int getCorrectAnswers() {
		return bucket1.getCorrectAnswers() 
			+ bucket2.getCorrectAnswers() 
			+ bucket3.getCorrectAnswers()
			+ bucket4.getCorrectAnswers()
			+ bucket5.getCorrectAnswers();
	}

	public int getWrongAnswers() {
		return bucket1.getWrongAnswers() 
			+ bucket2.getWrongAnswers() 
			+ bucket3.getWrongAnswers()
			+ bucket4.getWrongAnswers()
			+ bucket5.getWrongAnswers();
	}


}
