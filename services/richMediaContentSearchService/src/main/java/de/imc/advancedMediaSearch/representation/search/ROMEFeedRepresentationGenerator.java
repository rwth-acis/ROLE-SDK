package de.imc.advancedMediaSearch.representation.search;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.restlet.ext.rome.RomeConverter;
import org.restlet.ext.rome.SyndFeedRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl;

import de.imc.advancedMediaSearch.representation.search.rome.AMSModule;
import de.imc.advancedMediaSearch.representation.search.rome.AMSModuleImpl;
import de.imc.advancedMediaSearch.representation.search.rome.AtomNSModule;
import de.imc.advancedMediaSearch.representation.search.rome.AtomNSModuleImpl;
import de.imc.advancedMediaSearch.representation.search.rome.FeedModuleImpl;
import de.imc.advancedMediaSearch.representation.search.rome.IFeedModule;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultTag;
import de.imc.advancedMediaSearch.result.ResultUser;

public class ROMEFeedRepresentationGenerator extends
		SearchRepresentationGenerator {

	private static Logger logger = Logger
			.getLogger(ROMEFeedRepresentationGenerator.class);
	// private static String NO_DESCRIPTION_AVAILABLE =
	// "No description available";

	public String feedType;
	public String title;
	public String link;
	public String description;

	public ROMEFeedRepresentationGenerator(String feedType, String title,
			String link, String description) {
		super();
		this.feedType = feedType;
		this.title = title;
		this.link = link;
		this.description = description;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Representation generateRepresentation(ResultSet resultSet,
			ServerResource resource, Variant variant) {

		logger.debug("Starting to generate feed");

		SyndFeedRepresentation result = new SyndFeedRepresentation();

		SyndFeed feed = new SyndFeedImpl();

		feed.setFeedType(feedType);
		feed.setTitle(title);

		// to be w3c compliant
		AtomNSModule atomModule = new AtomNSModuleImpl();
		atomModule.setLink(link);
		feed.getModules().add(atomModule);

		feed.setDescription(description);
		feed.setUri(link);
		feed.setPublishedDate(new Date());

		if (resultSet == null) {
			logger.error("Resultset is null");

			RomeConverter converter = new RomeConverter();
			try {
				result = (SyndFeedRepresentation) converter.toRepresentation(
						feed, variant, resource);
			} catch (IOException e) {
				logger.error("Cannot create feed");
			}

			return result;
		}

		try {

			// add custom feed items
			IFeedModule feedModule = new FeedModuleImpl();
			if (resultSet.getSourceRepositories() != null) {
				feedModule.setSourceRepositories(resultSet
						.getSourceRepositories());
			}
			
			feedModule.setItemsPerPage(resultSet.getItemsPerPage());
			feedModule.setSearchQuery(resultSet.getSearchQuery());
			feedModule.setSearchUrl(resultSet.getSearchUrl());
			feedModule.setSearchTime(resultSet.getCalculationTime());
			feedModule.setStartIndex(resultSet.getStartIndex());
			
			feed.getModules().add(feedModule);
			
			Iterator<ResultEntity> resultIter = resultSet.iterator();

			while (resultIter.hasNext()) {

				ResultEntity tmpEntity = resultIter.next();

				// create Entry
				SyndEntry entry = new SyndEntryImpl();

				// set defaults
				entry.setTitle(tmpEntity.getTitle());

				if (tmpEntity.getAuthors() != null) {
					for (ResultUser author : tmpEntity.getAuthors()) {
						SyndPerson tmpPerson = new SyndPersonImpl();
						tmpPerson.setName(author.getName());
						entry.getAuthors().add(tmpPerson);
					}
				}

				if (tmpEntity.getUrl() != null) {
					entry.setLink(tmpEntity.getUrl().toString());
				}

				if (tmpEntity.getPublished() != null) {
					entry.setPublishedDate(tmpEntity.getPublished());
				}

				if (tmpEntity.getUpdated() != null) {
					entry.setUpdatedDate(tmpEntity.getUpdated());
				}

				if (tmpEntity.getUrl() != null) {
					entry.setUri(tmpEntity.getUrl());
				}

				// set description
				SyndContent description = new SyndContentImpl();
				description.setType("text/html");

				String descriptionString = "";
				if (tmpEntity.getDescription() != null) {
					descriptionString = tmpEntity.getDescription();
				}
				// if(descriptionString.equals("")){
				// descriptionString=NO_DESCRIPTION_AVAILABLE;
				// }

				description.setValue(descriptionString);

				entry.setDescription(description);

				// set tags
				if (tmpEntity.getTags() != null) {
					Iterator<ResultTag> tagIter = tmpEntity.getTags()
							.iterator();

					while (tagIter.hasNext()) {
						SyndCategory tmpCategory = new SyndCategoryImpl();
						tmpCategory.setName(tagIter.next().getName());
						entry.getCategories().add(tmpCategory);
						// TODO: check why break is needed
						// break;
					}
				}

				// set custom fields
				AMSModule module = new AMSModuleImpl();
				module.setSource(tmpEntity.getSource());
				module.setLength(tmpEntity.getLength());
				module.setScore(tmpEntity.getScore());
				// module.setSourceRating(tmpEntity.getSourceRating());
				module.setThumbnail(tmpEntity.getThumbnail());
				// module.setUserRating(tmpEntity.getUserRating());
				if (tmpEntity.getPreview() != null) {
					if (tmpEntity.getPreview().getEmbeddableHtml() != null) {
						module.setEmbeddedHTML(tmpEntity.getPreview()
								.getEmbeddableHtml());
					}
				}

				module.setMediaType(tmpEntity.getMediaType());
				module.setMimeType(tmpEntity.getFormat());
				module.setNotes(tmpEntity.getComments());

				entry.getModules().add(module);

				feed.getEntries().add(entry);
			}

			RomeConverter converter = new RomeConverter();
			result = (SyndFeedRepresentation) converter.toRepresentation(feed,
					variant, resource);

		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return result;
	}

	public void addEntry(String title, String text, String category, Date date) {

	}

}
