/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.target;

import de.imc.advancedMediaSearch.result.ResultSet;

/**
 * Target for searching in vimeo.com
 * @author julian.weber@im-c.de
 *
 */
public class VimeoTarget extends Target {

	private final String searchbaseurl = "http://vimeo.com/api/rest/v2?format=json&method=vimeo.videos.search&full_response=1";
	private final String queryparameter = "&query=";
	private final String resultsizeparameter = "&per_page=";
	private final String consumerkey = "799076bcc6ae1a33ee193a6124b8e272";
	private final String consumersecret = "d7e06c0cf52fcdb8";
	
	
	
	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.target.Target#searchByTags(java.lang.String)
	 */
	@Override
	public ResultSet searchByTags(String tagQuery, QueryArguments args) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.target.Target#searchByFullTextQuery(java.lang.String)
	 */
	@Override
	public ResultSet searchByFullTextQuery(String searchTermQuery, QueryArguments args) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.target.Target#searchByAuthor(java.lang.String)
	 */
	@Override
	public ResultSet searchByAuthor(String authorQuery, QueryArguments args) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.target.Target#getId()
	 */
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

}
