package de.imc.advancedMediaSearch.restlet.resource;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;

import org.restlet.data.CharacterSet;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import de.imc.advancedMediaSearch.rankingAlgorithm.LuceneRanking;
import de.imc.advancedMediaSearch.rankingAlgorithm.NoRanking;
import de.imc.advancedMediaSearch.rankingAlgorithm.RankingAlgorithm;
import de.imc.advancedMediaSearch.representation.search.ROMEFeedRepresentationGenerator;
import de.imc.advancedMediaSearch.result.ResultSet;

import de.imc.advancedMediaSearch.target.Aggregator;
import de.imc.advancedMediaSearch.target.OpenScoutRepositoryTarget;
import de.imc.advancedMediaSearch.target.SlideShareTarget;
import de.imc.advancedMediaSearch.target.Target;
import de.imc.advancedMediaSearch.target.YoutubeTarget;


import org.apache.log4j.Logger;

/** */
public class FeedResource extends BaseServerResource {

    private static Logger logger = Logger.getLogger(FeedResource.class);
    
    private static final int DEFAULT_MAX_RESULTS = 20;
    
    private static final int DEFAULT_TIMEOUT = 10;
    
    //private static final String DEFAULT_REPOSITORIES = "ALL";
    
    private static final boolean DEFAULT_RANKING_ACTIVATED = false;
    
    String query;
    String searchType;
    
    int maxResults;
    
    int timeout;
    
    List<Target> repositoryList;
    
    boolean rankingActivated;

    @Override
    protected void doInit() throws ResourceException {

    	//get parameters

        //get searchtype
        searchType = (String) getRequest().getAttributes().get("searchType");
        
        logger.debug("SearchType: "+searchType);
        
        //get query
        query = getRequest().getResourceRef().
        	getQueryAsForm().getFirstValue("query");
        
        logger.debug("Query: "+query);
        
        //get maxResults
        String maxResultsString = getRequest().getResourceRef().
            getQueryAsForm().getFirstValue("maxResults");
        
        if(maxResultsString == null){
        	maxResults = DEFAULT_MAX_RESULTS;
        }else{
            try{
            	maxResults = Integer.parseInt(maxResultsString);
            }catch(Exception e){
            	maxResults = DEFAULT_MAX_RESULTS;
            }
        }
        logger.debug("Set max results to: "+maxResults);
        
        //get timeout
        String timeoutString = getRequest().getResourceRef().
        	getQueryAsForm().getFirstValue("timeout");
        
        if(timeoutString == null){
        	timeout = DEFAULT_TIMEOUT;
        }else{
            try{
            	timeout = Integer.parseInt(timeoutString);
            }catch(Exception e){
            	timeout = DEFAULT_TIMEOUT;
            }
        }
        logger.debug("Set timeout to: "+timeout);
        
        //get repositories
        String repositoryString = getRequest().getResourceRef().
        	getQueryAsForm().getFirstValue("repositories");
        
        repositoryList = new ArrayList<Target>();
        
        if(repositoryString == null || repositoryString.equalsIgnoreCase("") 
        		||repositoryString.equalsIgnoreCase("All")){
        	repositoryList.add(new YoutubeTarget(timeout, timeout));
    		repositoryList.add(new SlideShareTarget(timeout, timeout));
    		repositoryList.add(new OpenScoutRepositoryTarget(timeout, timeout));
    		logger.debug("Added all repositories");
    		
        }else{
        
        	String[] repositoryStringArray = repositoryString.split(",");
        	
	        for(int i = 0;i<repositoryStringArray.length;i++){
	        	if(repositoryStringArray[i].equalsIgnoreCase("youtube")){
	        		repositoryList.add(new YoutubeTarget(timeout, maxResults));
	        		logger.debug("Added Youtube repository");
	        	}else if(repositoryStringArray[i].equalsIgnoreCase("slideshare")){
	        		repositoryList.add(new SlideShareTarget(timeout, maxResults));
	        		logger.debug("Added Slideshare repository");
	        	}else if(repositoryStringArray[i].equalsIgnoreCase("openscout")){
	        		repositoryList.add(new OpenScoutRepositoryTarget(timeout, maxResults));
	        		logger.debug("Added Openscout repository");
	        	}
	        }
        }
        
        //get rankingActivated
        String rankingActivatedString = getRequest().getResourceRef().
        	getQueryAsForm().getFirstValue("rankingActivated"); 
        
        if(rankingActivatedString==null){
        	rankingActivated = DEFAULT_RANKING_ACTIVATED;
        }else{
            if(rankingActivatedString.equals("0")){
            	rankingActivated = false;
            }else if(rankingActivatedString.equals("1")){
            	rankingActivated = true;
            }else{
            	rankingActivated = DEFAULT_RANKING_ACTIVATED;
            }
        }
        
        logger.debug("Ranking is activated: "+rankingActivated);
        
    }

    @Get
    public Representation represent() {

        Representation result;
        
        //create feed generator
        ROMEFeedRepresentationGenerator atomGen = new ROMEFeedRepresentationGenerator("atom_1.0", "AdvancedMediaSearch", "http://www.im-c.de/AdvancedMediaSearch",
                "daniel.dahrendorf@im-c.de");

        //choose ranking algorithm
        RankingAlgorithm rankingalgorithm;
        
        if(rankingActivated){
        	rankingalgorithm = new LuceneRanking();
        }else{
        	rankingalgorithm = new NoRanking();
        }

        //add repositories
        Target target = new Aggregator(timeout, maxResults, rankingalgorithm);
        
        Iterator<Target> targetIter = repositoryList.iterator();
        
        while(targetIter.hasNext()){
        	
        	Target tmpTarget = targetIter.next();
        	
        	logger.debug("Added repository to aggregator : "+ tmpTarget.getName());
        	
        	((Aggregator)target).addTarget(tmpTarget);
        }

        
        
        ResultSet resultSet = new ResultSet();
        
        if(searchType.equals("tag")){
            
        	logger.debug("Start tag query for: "+query+"");
            resultSet = target.searchByTags(query, null);
            
        }else if(searchType.equals("author")){
        	
        	logger.debug("Start author query for: "+query+"");
            resultSet = target.searchByAuthor(query, null);
            
        }else if(searchType.equals("fullTextQuery")){        
        	
        	logger.debug("Start fullTextQuery for: "+query+"");
            resultSet = target.searchByFullTextQuery(query, null);
                       
        }else{
        	
        	logger.debug("No querying for: "+query+"");
        	
            //TODO: generate error feed
            return atomGen.generateRepresentation(null, this, getPreferredVariant(getVariants()));
            
        }
		
        result = atomGen.generateRepresentation(resultSet, this, getPreferredVariant(getVariants()));

        result.setCharacterSet(CharacterSet.UTF_8);
        
        return result;
    }
}
