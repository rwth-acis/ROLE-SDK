/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.result;

import java.util.ArrayList;
import java.util.List;

/**
 * @author julian.weber@im-c.de
 *
 */
public enum MimeType {
	text, image, video, audio, application, multipart, message, model, example;
	
	/**
	 * creates a MimeType Object from a string,
	 * return null if conversion fails
	 * @param s the string to convert
	 * @return a MimeType object or null on error
	 */
	public static MimeType getTypeFromString(String s) {
		if(s==null) {
			return null;
		}
		if(s.equals(text.toString())) {
			return text;
		} else if(s.equals(image.toString())) {
			return image;
		} else if(s.equals(video.toString())) {
			return video;
		} else if(s.equals(audio.toString())) {
			return audio;
		} else if(s.equals(application.toString())) {
			return application;
		} else if(s.equals(multipart.toString())) {
			return multipart;
		} else if(s.equals(message.toString())) {
			return message;
		} else if(s.equals(model.toString())) {
			return model;
		} else if(s.equals(example.toString())) {
			return example;
		} else {
			return null;
		}
	}
	
	public static List<MimeType> getTypesFromString(String tString) {
    	if(tString == null) {
    		return null;
    	}
    	
    	String[] splitted = tString.split(",");
    	if(splitted.length<1) {
    		return null;
    	}
    	
    	ArrayList<MimeType> list = new ArrayList<MimeType>();
    	for(String s : splitted) {
    		if(s!=null) {
    			MimeType actualType = getTypeFromString(s);
    			if(s!=null) {
    				list.add(actualType);
    			}
    		}
    	}
    	
    	if(list.size()>0) {
    		return list;
    	} else {
    		return null;
    	}
    }
}
