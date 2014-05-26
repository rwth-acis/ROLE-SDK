package de.imc.advancedMediaSearch.rankingAlgorithm;

import java.util.Iterator;
import java.util.List;

import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;

/**
 * @author dahrendorf
 * 
 */
public class NoRanking extends RankingAlgorithm {

	public static final String IDENTIFIER = "none";
	/**
	 * returns an unranked ResultSet with the given maximum size and the results
	 * entries of the given list of resultsets
	 */
	@Override
	public ResultSet computeRankedList(List<ResultSet> resultSets,
			String queryString, int maxResults) {

		// create a new resultSet
		ResultSet aggregatedResultSet = new ResultSet();

		
		
		// iterate through the sets
		Iterator<ResultSet> setIter = resultSets.iterator();

		while (setIter.hasNext() && aggregatedResultSet.size() < maxResults) {
			ResultSet targetResultSet = setIter.next();
			
			//add all source repository urls to the new resultset
			if(targetResultSet.getSourceRepositories()!=null) {
				for(String s : targetResultSet.getSourceRepositories()) {
						aggregatedResultSet.addSourceRepository(s);
				}
			}
			aggregatedResultSet.addSourceRepository(targetResultSet.getSourceRepositories().get(0));
			
			Iterator<ResultEntity> entityIter = targetResultSet.iterator();

			while (entityIter.hasNext()
					&& aggregatedResultSet.size() < maxResults) {
				aggregatedResultSet.add(entityIter.next());
			}
		}
		return aggregatedResultSet;
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.rankingAlgorithm.RankingAlgorithm#computeRankedList(de.imc.advancedMediaSearch.result.ResultSet, java.lang.String, int)
	 */
	@Override
	public ResultSet computeRankedList(ResultSet lastResults, String queryString,
			int maxQueryResults) {
		return lastResults;
		
	}

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.rankingAlgorithm.RankingAlgorithm#getName()
	 */
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

}
