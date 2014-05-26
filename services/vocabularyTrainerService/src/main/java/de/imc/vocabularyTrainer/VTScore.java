package de.imc.vocabularyTrainer;

import java.math.BigDecimal;
import java.text.NumberFormat;

import org.json.JSONException;
import org.json.JSONObject;

public class VTScore {

	int userId;
	int correctAnswers;
	int wrongAnswers;
	double successRate;
	NumberFormat numberformat;
	
	public VTScore(int userId, int correctAnswers, int wrongAnswers,
			double successRate) {
		super();
		this.userId = userId;
		this.correctAnswers = correctAnswers;
		this.wrongAnswers = wrongAnswers;
		this.successRate = successRate;
		numberformat = NumberFormat.getCurrencyInstance();
		numberformat.setMaximumFractionDigits(2);
	}

	public VTScore(JSONObject jsonScore) {
		super();
		try {
			this.userId = jsonScore.getInt("userId");
			this.correctAnswers = jsonScore.getInt("correctAnswers");
			this.wrongAnswers = jsonScore.getInt("wrongAnswers");
			this.successRate = jsonScore.getDouble("successRate");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}	

	public JSONObject toJSON() {
		JSONObject userJSON = new JSONObject();
		
		try {
			userJSON.put("userId", getUserId());
			userJSON.put("correctAnswers", getCorrectAnswers());
			userJSON.put("wrongAnswers", getWrongAnswers());
			userJSON.put("successRate", getSuccessRate());
		
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

	public double getSuccessRate() {
	    int decimalPlace = 2;
	    BigDecimal bd = new BigDecimal(successRate);
	    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_UP);
	    double returnValue = bd.doubleValue();
	    
		return returnValue;
	}

	public void setSuccessRate(double successRate) {
		this.successRate = successRate;
	}
	
	
	
}
