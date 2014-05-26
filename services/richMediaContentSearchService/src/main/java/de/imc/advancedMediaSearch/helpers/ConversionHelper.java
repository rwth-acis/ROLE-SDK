/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * Class defining methods for Object conversions
 * 
 * @author julian.weber@im-c.de
 * 
 */
public class ConversionHelper {
	
	private static Logger logger = Logger.getLogger(ConversionHelper.class); 
	
	/**
	 * converts a string to an int if the given String is not convertible ->
	 * return defaultValue
	 * 
	 * @param intString
	 *            the string to convert
	 * @param defaultValue
	 *            the default value to return on errors
	 * @return the String as an int, or the defaultValue, if the string isn't
	 *         convertible
	 */
	public static int convertToInt(String intString, int defaultValue) {
		int ret = 0;
		try {
			ret = Integer.parseInt(intString);
		} catch (NumberFormatException e) {
			ret = defaultValue;
		}
		return ret;
	}

	/**
	 * converts a string to an int if the given String is not convertible ->
	 * return defaultValue
	 * @param intString
	 * @param defaultValue
	 * @param negative negative values allowed?
	 * @return
	 */
	public static int convertToInt(String intString, int defaultValue,
			boolean negative) {
		int ret = convertToInt(intString, defaultValue);
		if (ret < 0) {
			ret = defaultValue;
		}
		return ret;
	}

	/**
	 * converts a string to an int if the given String is not convertible ->
	 * return defaultValue
	 * @param intString
	 * @param defaultValue
	 * @param negative negative values allowed
	 * @param maxValue maximum value
	 * @return
	 */
	public static int convertToInt(String intString, int defaultValue,
			boolean negative, int maxValue) {
		int ret = convertToInt(intString, defaultValue, negative);
		if (ret > maxValue) {
			ret = defaultValue;
		}
		return ret;
	}
	
	/**
	 * converts a given String containing an integer value to a boolean value,
	 * e.g.: 0 -> false, !=0 -> true
	 * @param intString
	 * @param defaultValue
	 * @return
	 */
	public static boolean convertToBoolean(String intString, boolean defaultValue) {
		int i = convertToInt(intString, 1);
		if(i==0) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * converts a given comma separated String into a list of Strings
	 * @param inputString
	 * @return
	 */
	public static List<String> convertToList(String inputString) {
		ArrayList<String> list = new ArrayList<String>();
		if(inputString==null || inputString.equals("")) {
			return list;
		}
		
		String[] strings = inputString.split(",");
		for(String s : strings) {
			list.add(s);
		}
		return list;
	}
	
	/**
	 * converts a given String to a Date object using the specified format,
	 * returns the 0 date when errors occur
	 * @param inputString The string to parse
	 * @param format the format to use for parsing
	 * @return a date representation of the given input string, or a 0 date on errors
	 */
	public static Date convertStringToDate(String inputString, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		Date d;
		try {
			d = sdf.parse(inputString);
		} catch (ParseException e) {
			logger.error("Couldn't parse the date string: " + inputString + " withFormat: " + format);
			return new Date(0);
		}
		return d;
	}
}
