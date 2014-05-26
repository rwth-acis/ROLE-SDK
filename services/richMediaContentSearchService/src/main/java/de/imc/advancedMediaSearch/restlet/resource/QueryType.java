/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.restlet.resource;

/**
 * Enumeration of the available query types for requests
 * @author julian.weber@im-c.de
 *
 */
public enum QueryType {
	fullTextQuery, authorQuery, tagQuery, categoryQuery;
	
	public static QueryType getQueryTypeFromString(String id) {
		if(id==null) {
			return null;
		}
		if(id.equals(fullTextQuery.toString())) {
			return QueryType.fullTextQuery;
		} else if(id.equals(authorQuery.toString())) {
			return QueryType.authorQuery;
		} else if(id.equals(tagQuery.toString())) {
			return QueryType.tagQuery;
		} else if(id.equals(categoryQuery.toString())) {
			return QueryType.categoryQuery;
		} else {
			return null;
		}
	};
}
