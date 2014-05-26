/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.rankingAlgorithm;

import org.apache.log4j.Logger;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class RankingFactory {
	
	private static Logger logger = Logger.getLogger(RankingFactory.class);
	
	/**
	 * creates a Ranking algorithm with the given identifier
	 * creates NoRanking on invalid or null identifier values
	 * @param identifier
	 * @return a RankingAlgorithm with the given identifier or NoRanking on errors
	 */
	public RankingAlgorithm createRanking(String identifier) {
		if(identifier==null || identifier.equals("") || identifier.equals(NoRanking.IDENTIFIER)) {
			logger.debug("creating NoRanking for identifier: " + identifier);
			return new NoRanking();
		} else if(identifier.equals(LuceneRanking.IDENTIFIER)) {
			logger.debug("creating LuceneRanking for identifier: " + identifier);
			return new LuceneRanking();
		} else {
			logger.debug("creating NoRanking as a default case");
			return new NoRanking();
		}
		

	}
}
