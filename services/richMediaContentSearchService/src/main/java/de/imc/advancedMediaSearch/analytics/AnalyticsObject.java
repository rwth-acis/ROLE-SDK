/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.analytics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author julian.weber@im-c.de
 *
 */
public class AnalyticsObject {
	private String id;
	private String description;
	private String message;
	private List<Number> values;
	
	
	public AnalyticsObject(String id) {
		this.id = id;
		this.description = "";
		this.message = "";
		this.values = new ArrayList<Number>();
	}
	
	public void addValue(Number n) {
		if(n!=null) {
			this.values.add(n); 
		}
	}


	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}


	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @return the values
	 */
	public List<Number> getValues() {
		return values;
	}
	
	public Number getValue(int index) {
		try {
			return this.values.get(index); 
		} catch(IndexOutOfBoundsException indException) {
			return 0;
		}
		
	}
	
	
	
}