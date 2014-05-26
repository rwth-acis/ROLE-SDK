/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL
 */
package de.imc.advancedMediaSearch.target;

import com.hp.hpl.jena.query.*;
import de.imc.advancedMediaSearch.helpers.ConversionHelper;
import de.imc.advancedMediaSearch.http.ApacheHttpClient;
import de.imc.advancedMediaSearch.http.RESTHttpClient;
import de.imc.advancedMediaSearch.properties.AMSPropertyManager;
import de.imc.advancedMediaSearch.restlet.resource.SearchResource;
import de.imc.advancedMediaSearch.result.*;
import de.imc.advancedMediaSearch.result.ResultSet;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

/**
 * @author sten.govaerts@cs.kuleuven.be
 */
public class OUUKPodcastTarget extends Target
{
    //variables and constants
    public static final String ID = "podcast.open.ac.uk";

	private static final String SPARQL_ENDPOINT = "http://data.open.ac.uk/query";

    private static final String SPARQL_QUERY = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "select * where {\n" +
                    "?p <http://purl.org/dc/terms/description> ?desc .\n" +
                    "?p <http://purl.org/dc/terms/title> ?title .\n" +
                    "OPTIONAL{?p <http://www.w3.org/TR/2010/WD-mediaont-10-20100608/copyright> ?copyright} .\n" +
                    "OPTIONAL{?p <http://digitalbazaar.com/media/download> ?medialink} .\n" +
                    "OPTIONAL{?p <http://digitalbazaar.com/media/depiction> ?image} .\n" +
                    "OPTIONAL{?p <http://digitalbazaar.com/media/duration> ?duration} .\n" +
                    "OPTIONAL{?p <http://purl.org/dc/terms/published> ?published} .\n" +
                    "OPTIONAL{?p <http://www.w3.org/TR/2010/WD-mediaont-10-20100608/format> ?format} .\n" +
                    "OPTIONAL{?p <http://www.w3.org/TR/2010/WD-mediaont-10-20100608/language> ?lang }.\n" +
                    "OPTIONAL{?p <http://www.w3.org/TR/2010/WD-mediaont-10-20100608/genre> ?genre .\n" +
                    "?genre rdfs:label ?genrelabel}\n" +
                    "OPTIONAL{?p <http://www.w3.org/TR/2010/WD-mediaont-10-20100608/publisher> ?publisher .\n" +
                    "?publisher rdfs:label ?publisherlabel}\n" +
                    "{?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.open.ac.uk/podcast/ontology/VideoPodcast> }\n" +
                    "FILTER(regex(str(?title), \"%s\", \"i\" ) || regex(str(?desc), \"%s\", \"i\" ))}\n" +
                    "LIMIT %d";

	private String apiurl = AMSPropertyManager.getInstance().getStringValue(
			"de.imc.advancedMediaSearch.baseurls.ouukpodcast", SPARQL_ENDPOINT);

	private static Logger logger = Logger.getLogger(FiveMinTarget.class);

    public OUUKPodcastTarget() {
		super();
		initializeMetaData();
	}

	public void initializeMetaData() {
		name = "OU Podcasts";
		url = "http://podcast.open.ac.uk/";
		mediaTypeIconUrl = "";
		description = "Learn at any time with the Open University audio and video podcasts.";
		String[] mTypes = { MediaType.VIDEO.toString(), MediaType.AUDIO.toString() };
		mediaTypes = mTypes;
		iconUrl = AMSPropertyManager
				.getInstance()
				.getStringValue("de.imc.advancedMediaSearch.iconurls.ouukpodcast",
						"http://role-demo.de:8080/richMediaContentSearchResources/icons/oupodcast.ico");
	}

	private com.hp.hpl.jena.query.ResultSet executeQuery(String queryStr) {
        Query query = QueryFactory.create(queryStr);

		try {
            logger.info("querying " + apiurl + " with: \n" + queryStr);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(apiurl, query);
            return qexec.execSelect();
		} catch (Exception e) {
			logger.debug(e.getMessage());
			return null;
        }
	}

	private ResultSet parseResult(com.hp.hpl.jena.query.ResultSet sparqlResults, String searchQuery) {
		ResultSet results = new ResultSet();
		results.addSourceRepository(ID);
		results.setSearchQuery(searchQuery);

		while(sparqlResults != null && sparqlResults.hasNext())
        {
            QuerySolution sparqlRes = sparqlResults.next();

			ResultEntity e = new ResultEntity();
			e.setSource(ID);

            //title
            if(sparqlRes.getLiteral("title") != null){
                e.setTitle(sparqlRes.getLiteral("title").getString());
            }
            //description
            if(sparqlRes.getLiteral("desc") != null){
                e.setDescription(sparqlRes.getLiteral("desc").getString());
            }
            //url
            if(sparqlRes.getResource("medialink") != null){
                e.setUrl(sparqlRes.getResource("medialink").getURI());
            }
            //thumbnail
            if(sparqlRes.getResource("image") != null)
            {
                String thumbnailLink = sparqlRes.getResource("image").getURI();
                try{
                    e.setThumbnail(new ResultThumbnail(300, 300, new URL(thumbnailLink)));
                } catch (MalformedURLException e1) {
                    logger.debug(e1.getMessage());
                }
            }
            //tags
            if(sparqlRes.getLiteral("genrelabel") != null)
            {
                String tag = sparqlRes.getLiteral("genrelabel").getString();
                //preprocess - remove @ postfix
                if(tag.indexOf("@") != -1)
                {
                    tag = tag.substring(0, tag.lastIndexOf("@"));
                }
                if(tag.length() > 0)
                {
                    ResultTag restag = new ResultTag(tag, ID);
				    e.addTag(restag);
                }
            }
            //published date
            if(sparqlRes.getLiteral("published") != null)
            {
                //date format: 2009-06-01T04:14:41+01:00
                Calendar publishedCal = DatatypeConverter.parseDateTime(sparqlRes.getLiteral("published").getString());
                if(publishedCal.getTime().getTime() != 0) {
                    e.setPublished(publishedCal.getTime());
                }
            }
            //format
            if(sparqlRes.getLiteral("format") != null)
            {
                String format = sparqlRes.getLiteral("format").getString();
                if(format.toLowerCase().indexOf("video") != -1){
                    e.setMediaType(MediaType.VIDEO);
                    e.setMimeType(MimeType.video);
                }
                else if(format.toLowerCase().indexOf("audio") != -1){
                    e.setMediaType(MediaType.AUDIO);
                    e.setMimeType(MimeType.audio);
                }
                else{
                    e.setMediaType(MediaType.UNKNOWN);
                }
            }
            //duration
            if(sparqlRes.getLiteral("duration") != null){
                String dur = sparqlRes.getLiteral("duration").getString();
                String[] durParts = dur.split(":");
                if(durParts.length > 0){
                    int sec = Integer.parseInt(durParts[durParts.length - 1]);
                    if(durParts.length > 1){
                        sec += (Integer.parseInt(durParts[durParts.length - 2]) * 60);
                    }
                    if(durParts.length > 2){
                        sec += (Integer.parseInt(durParts[durParts.length - 3]) * 60 * 60);
                    }
                    e.setLength(sec);
                }
            }
            //author/publisher
            if(sparqlRes.getLiteral("publisherlabel") != null)
            {
                LinkedList<ResultUser> authors = new LinkedList<ResultUser>();
                ResultUser user = new ResultUser();
                user.setName(sparqlRes.getLiteral("publisherlabel").getString());
                authors.add(user);
                e.setAuthors(authors);
                e.setUploader(user);
            }

            //TODO preview code...

			results.add(e);
		}
		results = filterResult(results);
		return results;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByTags(java.lang.String,
	 * de.imc.advancedMediaSearch.target.QueryArguments)
	 */
	@Override
	public ResultSet searchByTags(String tagQuery, QueryArguments args) {
		//currently: return full text results
		return searchByFullTextQuery(tagQuery, args);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByFullTextQuery(java.lang
	 * .String, de.imc.advancedMediaSearch.target.QueryArguments)
	 */
	@Override
	public ResultSet searchByFullTextQuery(String searchTermQuery,
			QueryArguments args) {

        String query = String.format(SPARQL_QUERY, searchTermQuery, searchTermQuery, getMaxQueryResults());
		return parseResult(executeQuery(query), searchTermQuery);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.imc.advancedMediaSearch.target.Target#searchByAuthor(java.lang.String,
	 * de.imc.advancedMediaSearch.target.QueryArguments)
	 */
	@Override
	public ResultSet searchByAuthor(String authorQuery, QueryArguments args) {
		//currently: return full text results
		return searchByFullTextQuery(authorQuery, args);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.imc.advancedMediaSearch.target.Target#getId()
	 */
	@Override
	public String getId() {
		return ID;
	}
}
