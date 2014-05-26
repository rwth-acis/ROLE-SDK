package de.imc.vocabularyTrainer;

import java.math.BigDecimal;

import org.json.JSONException;
import org.json.JSONObject;

public class UserVIListBucket {

	int numberOfItems;
	int correctAnswers;
	int wrongAnswers;
	
	
	public UserVIListBucket(int numberOfItems, int correctAnswers,
			int wrongAnswers) {
		super();
		this.numberOfItems = numberOfItems;
		this.correctAnswers = correctAnswers;
		this.wrongAnswers = wrongAnswers;
	}


	public UserVIListBucket(JSONObject bucketJSON) {
		super();
		try {
			this.numberOfItems = bucketJSON.getInt("numberOfItems");
			this.correctAnswers = bucketJSON.getInt("correctAnswers");
			this.wrongAnswers = bucketJSON.getInt("wrongAnswers");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	
	public JSONObject toJSON(){
		JSONObject bucketJSON = new JSONObject();
		
		try {
			bucketJSON.put("numberOfItems", numberOfItems);
			bucketJSON.put("successRate", getSuccessRate());
			bucketJSON.put("correctAnswers", correctAnswers);
			bucketJSON.put("wrongAnswers", wrongAnswers);
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bucketJSON;
	}


	public int getNumberOfItems() {
		return numberOfItems;
	}


	public void setNumberOfItems(int numberOfItems) {
		this.numberOfItems = numberOfItems;
	}


	public double getSuccessRate() {
		if(correctAnswers+wrongAnswers == 0){
			return  0;
		}else{
			double successRate = ((double)correctAnswers)/((double)(correctAnswers+wrongAnswers));
			
		    int decimalPlace = 2;
		    BigDecimal bd = new BigDecimal(successRate);
		    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_UP);
		    successRate = bd.doubleValue();
		    
			return successRate;
			
		}
	}

	public int getCorrectAnswers() {
		return correctAnswers;
	}


	public void setCorrectAnswers(int correctAnswers) {
		this.correctAnswers = correctAnswers;
	}


	public int getWrongAnswers() {
		return wrongAnswers;
	}


	public void setWrongAnswers(int wrongAnswers) {
		this.wrongAnswers = wrongAnswers;
	}
	
}
