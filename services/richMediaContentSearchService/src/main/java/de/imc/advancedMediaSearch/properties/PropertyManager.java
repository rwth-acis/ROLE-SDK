/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author julian.weber@im-c.de
 *
 */
public class PropertyManager {
	
	private Properties properties;
	private static Logger logger = Logger.getLogger(PropertyManager.class);
	
	public PropertyManager() {
		properties = null;
	}
	
	public PropertyManager(String filename) {
		loadPropertyFile(filename);
	}
	
	public void loadPropertyFile(String filename) {
		
		// Get the inputStream  
        InputStream inputStream = this.getClass().getClassLoader()  
                .getResourceAsStream(filename); 
		properties = new Properties();
		
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			logger.error("An error occured while loading the properties file: " + e.getMessage());
		}
	}

	public Properties getProperties() {
		return properties;
	}
	
	@Override
	public String toString() {
		if(properties==null) {
			return "Properties:no property file loaded";
		} else {
			return properties.toString();
		}
	}
	
}
