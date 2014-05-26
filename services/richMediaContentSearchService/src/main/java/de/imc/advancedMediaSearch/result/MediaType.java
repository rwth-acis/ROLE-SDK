package de.imc.advancedMediaSearch.result;

import java.util.ArrayList;
import java.util.List;

public enum MediaType {
	VIDEO, PRESENTATION, IMAGE, TEXT, AUDIO, UNKNOWN, APPLICATION, LIST;

	/**
	 * creates a MediaType Object from a string, return null if conversion fails
	 * 
	 * @param s
	 *            the string to convert
	 * @return a MediaType object or null on error
	 */
	public static MediaType getTypeFromString(String tString) {
		if (tString == null) {
			return null;
		}
		tString = tString.toLowerCase();
		
		if (tString.equals(VIDEO.toString().toLowerCase())) {
			return VIDEO;
		} else if (tString.equals(PRESENTATION.toString().toLowerCase())) {
			return PRESENTATION;
		} else if (tString.equals(IMAGE.toString().toLowerCase())) {
			return IMAGE;
		} else if (tString.equals(TEXT.toString().toLowerCase())) {
			return TEXT;
		} else if (tString.equals(AUDIO.toString().toLowerCase())) {
			return AUDIO;
		} else if (tString.equals(APPLICATION.toString().toLowerCase())) {
			return APPLICATION;
		} else if (tString.equals(UNKNOWN.toString().toLowerCase())) {
			return UNKNOWN;
		} else if (tString.equals(LIST.toString().toLowerCase())) {
			return LIST; 
		}
		else {
			return null;
		}
	}

	/**
	 * returns a list of media types corresponding to the given comma separated string
	 * ,returns null on errors
	 * @param tString a comma separated string of mediatypes
	 * @return a list of MediaType objects or null on errors
	 */
	public static List<MediaType> getTypesFromString(String tString) {
		if (tString == null) {
			return null;
		}
		String[] splitted = tString.split(",");
		if (splitted.length < 1) {
			return null;
		}
		ArrayList<MediaType> list = new ArrayList<MediaType>();
		for (String s : splitted) {
			if (s != null) {
				MediaType actualType = getTypeFromString(s);
				if (s != null) {
					list.add(actualType);
				}
			}
		}
		if (list.size() > 0) {
			return list;
		} else {
			return null;
		}
	}

	public static MediaType getTypeFromMimeTypeString(String mimeTypeString) {
		MediaType t = MediaType.UNKNOWN;

		if (mimeTypeString == null || mimeTypeString.equals("")) {
			return t;
		}

		String[] formatArray = mimeTypeString.split("/");

		String mediaTypeString = formatArray[0];

		if (mediaTypeString.equalsIgnoreCase("image")) {
			t = MediaType.IMAGE;
		} else if (mediaTypeString.equalsIgnoreCase("audio")) {
			t = MediaType.AUDIO;
		} else if (mediaTypeString.equalsIgnoreCase("text")) {
			t = MediaType.TEXT;
		} else if (mediaTypeString.equalsIgnoreCase("video")) {
			t = MediaType.VIDEO;
		} else if (mediaTypeString.equalsIgnoreCase("application")) {

			if (formatArray[1].equalsIgnoreCase("vnd.ms-powerpoint")
					|| formatArray[1].equalsIgnoreCase("mspowerpoint")) {
				t = MediaType.PRESENTATION;
			} else if (formatArray[1].equalsIgnoreCase("pdf")) {
				t = MediaType.TEXT;
			} else if (formatArray[1].equalsIgnoreCase("vnd.ms-word")
					|| formatArray[1].equalsIgnoreCase("msword")) {
				t = MediaType.TEXT;
			} else if (formatArray[1].equalsIgnoreCase("x-shockwave-flash")) {
				t = MediaType.PRESENTATION;
			}

		} else if (mediaTypeString.equalsIgnoreCase("flv-application")) {

			if (formatArray[1].equalsIgnoreCase("octet-stream")) {
				t = MediaType.VIDEO;
			}

		} else if (mediaTypeString.equalsIgnoreCase("octet")) {
			t = MediaType.PRESENTATION;
		}

		return t;
	}
}
