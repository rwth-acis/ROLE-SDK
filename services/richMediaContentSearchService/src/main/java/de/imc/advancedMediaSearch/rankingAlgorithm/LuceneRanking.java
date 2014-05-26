package de.imc.advancedMediaSearch.rankingAlgorithm;

import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * @author dahrendorf
 */
public class LuceneRanking extends RankingAlgorithm {

	private static Logger logger = Logger.getLogger(LuceneRanking.class);

	private static final float TITLE_BOOST = 2.0F;
	private static final float DESCRIPTION_BOOST = 1.0F;
	private static final float TAGS_BOOST = 2.0F;
	private static final float AUTHORS_BOOST = 0.5F;

	public static final String IDENTIFIER = "lucene";

	/**
     * 
     */
	public ResultSet computeRankedList(List<ResultSet> resultSets,
			String queryString, int numberOfHits) {

		// compute number of results
		int numberOfResults = 0;
		for (ResultSet tmpResultSet : resultSets) {
			numberOfResults += tmpResultSet.size();
		}
		
		//initialize list of repository urls
		List<String> repositories = new ArrayList<String>();

		logger.debug("Rank " + numberOfResults + " results for: " + queryString
				+ "(" + numberOfHits + ")");

		// init result list and hashmap
		ResultSet resultList = new ResultSet();
		Map<String, ResultEntity> resultMap = new HashMap<String, ResultEntity>();

		// init the directory and the writer for ranking
		RAMDirectory ramDirectory = new RAMDirectory();
		IndexWriter indexWriter;

		// init the analyzer
		// TODO: check for languages -> PerFieldAnalyzerWrapper
		// each language gets its own Field!
		// Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
		Analyzer analyzer = new SnowballAnalyzer(Version.LUCENE_30, "English",
				StopAnalyzer.ENGLISH_STOP_WORDS_SET);

		try {
			indexWriter = new IndexWriter(ramDirectory, analyzer, true,
					IndexWriter.MaxFieldLength.UNLIMITED);

			// go through each result list
			for (ResultSet tmpResultSet : resultSets) {

				logger.debug("Add Resultset to documents");
				
				//collect source repository url information inside repositories list
				if(tmpResultSet.getSourceRepositories()!=null) {
					for(String s : tmpResultSet.getSourceRepositories()) {
						if(s!=null && !s.equals("") && !repositories.contains(s)) {
							repositories.add(s);
						}
					}
				}

				// add each entity
				Iterator<ResultEntity> iter = tmpResultSet.iterator();
				while (iter.hasNext()) {
					ResultEntity tmpResultEntity = iter.next();

					if (tmpResultEntity.getUrl() != null) {

						logger.debug("Add ResultEntity: "
								+ tmpResultEntity.getTitle() + " to document");

						// add to hashmap to find the results by uri
						resultMap
								.put(tmpResultEntity.getUrl(), tmpResultEntity);

						// create new doc
						Document doc = new Document();

						// add title
						Field titleField = new Field("title",
								tmpResultEntity.getTitle(), Field.Store.YES,
								Field.Index.ANALYZED);
						titleField.setBoost(TITLE_BOOST);
						doc.add(titleField);

						// add description
						Field descField = new Field("description",
								tmpResultEntity.getDescription(),
								Field.Store.YES, Field.Index.ANALYZED);
						descField.setBoost(DESCRIPTION_BOOST);
						doc.add(descField);

						// add tags
						Field tagField = new Field("tags",
								tmpResultEntity.getTagsString(),
								Field.Store.YES, Field.Index.ANALYZED);
						tagField.setBoost(TAGS_BOOST);
						doc.add(tagField);

						// add authors
						Field authorField = new Field("authors",
								tmpResultEntity.getAuthorsString(),
								Field.Store.YES, Field.Index.ANALYZED);
						authorField.setBoost(AUTHORS_BOOST);
						doc.add(authorField);

						// add url
						Field urlField = new Field("url",
								tmpResultEntity.getUrl(), Field.Store.YES,
								Field.Index.NOT_ANALYZED);
						doc.add(urlField);

						// boost document if there are some recommendations
						Float boost = 1.0F;
						boost += ((float) tmpResultEntity.getUserRating()) / 10.0F;
						doc.setBoost(boost);

						// add document to the index writer
						indexWriter.addDocument(doc);
					}
				}
			}

			logger.debug("Number of docs:indexwriter: " + indexWriter.numDocs());

			// optimize and close the writer
			indexWriter.optimize();
			indexWriter.close();

			logger.debug("Number of docs:indexwriter: " + indexWriter.numDocs()
					+ ". After optimization  and close");

			// prepare search

			IndexSearcher indexSearcher = new IndexSearcher(ramDirectory, true);

			// create query
			String searchFields[] = { "title", "description", "tags", "authors" };
			BooleanClause.Occur[] flags = { BooleanClause.Occur.SHOULD,
					BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD,
					BooleanClause.Occur.SHOULD };

			Query query = MultiFieldQueryParser.parse(Version.LUCENE_30,
					queryString, searchFields, flags, analyzer);

			logger.debug("Computed Query: " + query.toString());

			// do ranking
			TopDocs result = indexSearcher.search(query, numberOfHits);

			logger.debug("After indexing: " + result.totalHits);

			ScoreDoc[] hits = result.scoreDocs;

			logger.debug("Ranked " + hits.length + " items");

			// add score to documents and create resultList
			for (int i = 0; i < hits.length; i++) {

				Document tmpDoc = indexSearcher.doc(hits[i].doc);

				ResultEntity tmpResultEntity = resultMap.get(tmpDoc.get("url"));
				tmpResultEntity.setScore((double) hits[i].score);

				resultList.add(tmpResultEntity);

				logger.debug("Added rank RE " + i + ": "
						+ tmpResultEntity.getTitle() + "["
						+ tmpResultEntity.getScore() + "]");
			}

			ramDirectory.close();

		} catch (CorruptIndexException e) {
			logger.error("Error: " + e.getMessage(), e);
		} catch (LockObtainFailedException e) {
			logger.error("Error: " + e.getMessage(), e);
		} catch (IOException e) {
			logger.error("Error: " + e.getMessage(), e);
		} catch (ParseException e) {
			logger.error("Error: " + e.getMessage(), e);
		}
		
		//add source repository urls to resultlist
		resultList.setSourceRepositories(repositories);
		
		return resultList;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.imc.advancedMediaSearch.rankingAlgorithm.RankingAlgorithm#
	 * computeRankedList(de.imc.advancedMediaSearch.result.ResultSet,
	 * java.lang.String, int)
	 */
	@Override
	public ResultSet computeRankedList(ResultSet lastResults,
			String queryString, int maxQueryResults) {
		List<ResultSet> list = new ArrayList<ResultSet>(1);
		list.add(lastResults);
		return computeRankedList(list, queryString, maxQueryResults);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.imc.advancedMediaSearch.rankingAlgorithm.RankingAlgorithm#getName()
	 */
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}
}
