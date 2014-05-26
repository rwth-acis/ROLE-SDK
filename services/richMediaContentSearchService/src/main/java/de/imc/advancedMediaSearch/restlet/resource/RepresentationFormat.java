/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.restlet.resource;

/**
 * @author julian.weber@im-c.de
 *
 */
public enum RepresentationFormat {
	json, atom;
	
	public static RepresentationFormat getFormatfromString(String s) {
		if(s==null) {
			return null;
		}
		if(s.equals(RepresentationFormat.json.toString())) {
			return RepresentationFormat.json;
		} else if(s.equals(RepresentationFormat.atom.toString())) {
			return RepresentationFormat.atom;
		} else {
			return null;
		}
	};
}
