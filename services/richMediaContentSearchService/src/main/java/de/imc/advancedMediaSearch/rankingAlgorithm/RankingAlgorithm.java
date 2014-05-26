package de.imc.advancedMediaSearch.rankingAlgorithm;

import de.imc.advancedMediaSearch.result.ResultSet;

import java.util.List;

public abstract class RankingAlgorithm {
    
	/**
	 * adds all results of the given result sets to the list until the given
	 * maximum results are eached
	 * @param resultSets the result sets to use
	 * @param queryString the query string to use for the search
	 * @param the maximum number of results of the result set returned 
	 */
    public abstract ResultSet computeRankedList(List<ResultSet> resultSets, String queryString, int maxResults);

	/**
	 * @param lastResults
	 * @param queryString
	 * @param maxQueryResults
	 */
	public abstract ResultSet computeRankedList(ResultSet lastResults, String queryString,
			int maxQueryResults);    
	
	public abstract String getIdentifier();
}
