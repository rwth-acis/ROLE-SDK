/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.target;

import java.util.List;

import de.imc.advancedMediaSearch.exceptions.IllegalArgumentException;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class AggregatorFactory {

	/**
	 * creates an aggregator containing the target repositories with the given
	 * ids (domain-names) ommits unknown identifiers
	 * 
	 * @param targetids
	 *            a list of target id strings
	 * @return an aggregator containing the given target repositories
	 * @throws IllegalArgumentException
	 *             if the argument is null
	 */
	public Aggregator createAggregator(List<String> targetids)
			throws IllegalArgumentException {
		IllegalArgumentException.checkArgumentForNullValues(targetids);

		String[] array = (String[]) targetids.toArray();
		return createAggregator(array);
	}

	/**
	 * creates an aggregator containing the target repositories with the given
	 * ids (domain-names) ommits unknown identifiers
	 * 
	 * @param targetids
	 *            an arragy of target id strings
	 * @return an aggregator containing the given target repositories
	 * @throws IllegalArgumentException
	 *             if the argument is null
	 */
	public Aggregator createAggregator(String[] targetids)
			throws IllegalArgumentException {
		IllegalArgumentException.checkArgumentForNullValues(targetids);

		Aggregator a = new Aggregator();
		for (String s : targetids) {
			if (s != null) {
				// Youtube
				if (s.equals(YoutubeTarget.ID)) {
					YoutubeTarget t = new YoutubeTarget();
					a.addTarget(t);
				}
				// Slideshare
				else if (s.equals(SlideShareTarget.ID)) {
					SlideShareTarget t = new SlideShareTarget();
					a.addTarget(t);
				}
				// OpenScout
				else if (s.equals(OpenScoutRepositoryTarget.ID)) {
					OpenScoutRepositoryTarget t = new OpenScoutRepositoryTarget();
					a.addTarget(t);
				}
				//Wikipedia
				else if (s.equals(WikipediaTarget.ID)) {
					WikipediaTarget t = new WikipediaTarget();
					a.addTarget(t);
				} 
				//Globe
				else if(s.equals(GlobeTarget.ID)) {
					a.addTarget(new GlobeTarget());
				} 
				//mediaListService
				else if(s.equals(LearningListTarget.ID)) {
					a.addTarget(new LearningListTarget());
				}
				//NYTimes
				else if(s.equals(TimesTarget.ID)) {
					a.addTarget(new TimesTarget());
				}
				//Scribd
				else if(s.equals(ScribdTarget.ID)) {
					a.addTarget(new ScribdTarget());
				}
				//The Guardian
				else if(s.equals(GuardianTarget.ID)) {
					a.addTarget(new GuardianTarget());
				}
				//5min
				else if(s.equals(FiveMinTarget.ID)) {
					a.addTarget(new FiveMinTarget());
				}
				//icoper
				else if(s.equals(IcoperTarget.ID)) {
					a.addTarget(new IcoperTarget());
				}
                //OU-UK Podcasts
				else if(s.equals(OUUKPodcastTarget.ID)) {
					a.addTarget(new OUUKPodcastTarget());
				}
			}
		}

		return a;
	}

	/**
	 * creates an aggregator containing the target repositories in the specified
	 * comma separated list string with the given ids (domain-names) ommits
	 * unknown identifiers
	 * 
	 * @param targetids
	 *            a comma separated list of target-ids
	 * @return an aggregator containing the given target repositories
	 * @throws IllegalArgumentException
	 *             if the argument is null
	 */
	public Aggregator createAggregator(String targetids)
			throws IllegalArgumentException {
		IllegalArgumentException.checkArgumentForNullValues(targetids);
		String[] array = targetids.split(",");
		return createAggregator(array);
	}

	public Aggregator createAggregator(String targetids, int maxQueryResults,
			int timeout) throws IllegalArgumentException {
		Aggregator a = createAggregator(targetids);
		a.setMaxQueryResults(maxQueryResults);
		a.setTimeout(timeout);
		return a;
	}
	
	public Aggregator createAggregator(String[] targetids, int maxQueryResults, int timeout) throws IllegalArgumentException {
		Aggregator a = createAggregator(targetids);
		a.setMaxQueryResults(maxQueryResults);
		a.setTimeout(timeout);
		return a;
	}
	
	public Aggregator createAggregator(List<String> targetids, int maxQueryResults, int timeout) throws IllegalArgumentException {
		Aggregator a = createAggregator(targetids);
		a.setMaxQueryResults(maxQueryResults);
		a.setTimeout(timeout);
		return a;
	}

	/**
	 * returns an Aggregator filled with all available Target Repositories
	 * initialized with default values
	 * 
	 * @return an Aggregator with all available target repositories
	 */
	public Aggregator createAggregator() {
		Aggregator a = new Aggregator();
		a.addTarget(new YoutubeTarget());
		a.addTarget(new OpenScoutRepositoryTarget());
		a.addTarget(new SlideShareTarget());
		a.addTarget(new WikipediaTarget());
		a.addTarget(new GlobeTarget());
		a.addTarget(new LearningListTarget());
		a.addTarget(new TimesTarget());
		a.addTarget(new ScribdTarget());
		a.addTarget(new GuardianTarget());
		a.addTarget(new FiveMinTarget());
		a.addTarget(new IcoperTarget());
        a.addTarget(new OUUKPodcastTarget());
		a.setMaxQueryResults(Target.DEFAULT_MAXQUERYRESULTS);
		a.setTimeout(Target.DEFAULT_TIMEOUT);
		return a;
	}

	public Aggregator createAggregator(int maxQueryResults, int timeout) {
		Aggregator a = createAggregator();
		a.setMaxQueryResults(maxQueryResults);
		a.setTimeout(timeout);
		return a;
	}
}
