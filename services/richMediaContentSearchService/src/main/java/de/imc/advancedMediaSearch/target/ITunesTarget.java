



package de.imc.advancedMediaSearch.target;

import java.util.Date;

import org.apache.log4j.Logger;

import de.imc.advancedMediaSearch.result.ResultSet;



/** */
public class ITunesTarget extends Target {
	
	public static final String ID = "itunes.com";
	
	private static Logger logger = Logger.getLogger(ITunesTarget.class);
	
    public ITunesTarget(int maxDuration, int maxQueryResults) {
        super(maxDuration, maxQueryResults);
        // TODO Auto-generated constructor stub
    }

    @Override
    public ResultSet searchByAuthor(String author, QueryArguments args) {
    	
        return null;
    }

    @Override
    public ResultSet searchByFullTextQuery(String searchTerm, QueryArguments args) {
    	
    	//Testing .................................................
    	Date myDate = new Date();
    	
    	Date stopDate =  new Date(myDate.getTime() + 10000);
    	
    	while(myDate.before(stopDate)) {
    		try {
    			Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.debug(e.getMessage());
				e.printStackTrace();
			}
    		myDate = new Date();
    		logger.debug("itunes target iterating!");
    	}
        return null;
        // TODO Auto-generated method stub
        
    }

    @Override
    public ResultSet searchByTags(String tagQuery, QueryArguments args) {
        return null;
        // TODO Auto-generated method stub
        
    }

	/* (non-Javadoc)
	 * @see de.imc.advancedMediaSearch.target.Target#getId()
	 */
	@Override
	public String getId() {
		return ID;
	}


}
