/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.properties;

import java.util.Properties;

/**
 * Central access point for all application default values read from the
 * advancedmediasearch.properties file
 * designed as a singleton class
 * 
 * @author julian.weber@im-c.de
 * 
 */
public class AMSPropertyManager {
	private PropertyManager manager;
	private static AMSPropertyManager instance;
	private static final String DEFAULT_PROPERTIES_FILENAME = "advancedmediasearch.properties";

	private AMSPropertyManager() {
		manager = new PropertyManager(DEFAULT_PROPERTIES_FILENAME);
		instance = this;
	}

	public static AMSPropertyManager getInstance() {
		if (instance == null) {
			instance = new AMSPropertyManager();
		}
		return instance;
	}

	public Properties getProperties() {
		return manager.getProperties();
	}
	
	/**
	 * returns the string value of the property with the given name, if present,
	 * returns null if no such property exists, or if no value is defined
	 * @param propertyName the name of the property to get the value for
	 * @return
	 */
	private String getValue(String propertyName) {
		if(this.manager.getProperties()!= null) {
			return this.manager.getProperties().getProperty(propertyName);
		} else {
			return null;
		}
	} 
	
	public String getStringValue(String propertyName, String defaultValue) {
		String val = getValue(propertyName);
		if(val==null) {
			return defaultValue;
		} else {
			return val;
		}
	}
	
	public int getIntValue(String propertyName, int defaultValue) {
		String val = getValue(propertyName);
		if(val==null) {
			return defaultValue;
		} else {
			try {
				int i = Integer.parseInt(val);
				return i;
			} catch(NumberFormatException e) {
				return defaultValue;
			}
		}
	}

	/**
	 * @param string
	 * @param defaultMultithreading
	 * @return
	 */
	public boolean getBooleanValue(String propertyName, boolean defaultValue) {
		String val = getValue(propertyName);
		if(val==null) {
			return defaultValue;
		} else {
			if(val.toLowerCase().equals("true")) {
				return true;
			} else if(val.toLowerCase().equals("false")) {
				return false;
			} else {
				return defaultValue;
			}
		}
	}

}
