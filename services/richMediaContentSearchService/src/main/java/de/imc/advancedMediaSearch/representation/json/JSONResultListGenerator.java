/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.representation.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.imc.advancedMediaSearch.result.LearningList;
import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultCategory;
import de.imc.advancedMediaSearch.result.ResultComment;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultLicence;
import de.imc.advancedMediaSearch.result.ResultPreview;
import de.imc.advancedMediaSearch.result.ResultRating;
import de.imc.advancedMediaSearch.result.ResultSet;
import de.imc.advancedMediaSearch.result.ResultTag;
import de.imc.advancedMediaSearch.result.ResultThumbnail;
import de.imc.advancedMediaSearch.result.ResultUser;

/**
 * This class is used to create JSONObject from result lists
 * 
 * @author julian.weber@im-c.de
 * 
 */
public class JSONResultListGenerator {

	public static final String LEARNINGLISTITEMCLASSNAME = "llistitem";
	public static final String LEARNINGLISTTABLECLASSNAME = "llisttable";
	
	public JSONObject generateJSONResultList(ResultSet results) {
		if (results == null) {
			return null;
		}

		JSONObject o = new JSONObject();
		try {
			if (results.getItemsPerPage() != 0) {
				o.put("items-per-page", results.getItemsPerPage());
			}
			if (results.getNextPageUrl() != null) {
				o.put("next-page", results.getNextPageUrl());
			}
			if (results.getPreviousPageUrl() != null) {
				o.put("previous-page", results.getPreviousPageUrl());
			}
			if (results.getResults() != null) {
				if (results.size() > 0) {
					List<JSONObject> jsonItems = new ArrayList<JSONObject>();
					for (ResultEntity en : results.getResults()) {
						JSONObject actualObject = generateJSONResultItem(en);
						if (actualObject != null) {
							jsonItems.add(actualObject);
						}
					}
					o.put("items", jsonItems);
				}
			} else {
				o.put("total-results", results.size());
			}
			if (results.getSearchQuery() != null) {
				o.put("search-query", results.getSearchQuery());
			}
			if (results.getSearchUrl() != null) {
				o.put("search-url", results.getSearchUrl());
			}
			if (results.getSourceRepositories() != null) {
				if (results.getSourceRepositories().size() > 0) {
					o.put("source-repositories",
							results.getSourceRepositories());
				}
			}
			o.put("start-index", results.getStartIndex());
			o.put("search-time", results.getCalculationTime());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return o;
	}

	/**
	 * generates a JSONObject representation from the given ResultEntity, return
	 * null if the given argument is null
	 * 
	 * @param item
	 * @return
	 */
	public JSONObject generateJSONResultItem(ResultEntity item) {
		if (item == null) {
			return null;
		}

		JSONObject o = new JSONObject();
		try {
			if (item.getId() != 0) {
				o.put("id", item.getId());
			}
			if (item.getTitle() != null) {
				o.put("title", item.getTitle());
			}

			if (item.getDescription() != null) {
				o.put("description", item.getDescription());
			}
			if (item.getUrl() != null) {
				o.put("url", item.getUrl());
			}
			if (item.getLanguages() != null) {
				o.put("languages", item.getLanguages());
			}

			if (item.getSource() != null) {
				o.put("src", item.getSource());
			}
			if (item.getThumbnail() != null) {
				o.put("thumbnail",
						generateJSONResultThumbnail(item.getThumbnail()));
			}
			//generate preview representation for lists
			if(item.getMediaType()==MediaType.LIST) {
				o.put("preview", generateJSONLearningListPreview(item));
			} else {
				//generate preview representation for normal items
				if (item.getPreview() != null) {
					o.put("preview", generateJSONResultPreview(item.getPreview()));
				}
			}
			
			if (item.getMediaType() != null) {
				o.put("media-type", item.getMediaType().toString());
			}
			if (item.getMimeType() != null) {
				o.put("mime-type", item.getMimeType().toString());
			}
			if (item.getCategories() != null && item.getCategories().size() > 0) {
				List<JSONObject> categories = new ArrayList<JSONObject>();
				for (ResultCategory cat : item.getCategories()) {
					categories.add(generateJSONResultCategory(cat));
				}
				o.put("categories", categories);
			}
			if (item.getAuthors() != null && item.getAuthors().size() > 0) {
				List<JSONObject> authors = new ArrayList<JSONObject>();
				for (ResultUser aut : item.getAuthors()) {
					authors.add(generateJSONResultUser(aut));
				}
				o.put("authors", authors);
			}
			if (item.getUploader() != null) {
				o.put("uploader", generateJSONResultUser(item.getUploader()));
			}
			if (item.getPublished() != null) {
				o.put("date-published", item.getPublished());
			}
			if (item.getUpdated() != null) {
				o.put("date-updated", item.getUpdated());
			}
			if (item.getLength() != 0) {
				o.put("length", item.getLength());
			}
			if (item.getRatings() != null) {
				o.put("rating-count", item.getRatings().size());
			} else {
				o.put("rating-count", 0);
			}

			o.put("comment-count", item.getCommentCount());
			o.put("view-count", item.getViewCount());

			if (item.getComments() != null && item.getComments().size() > 0) {
				List<JSONObject> comments = new ArrayList<JSONObject>();
				for (ResultComment com : item.getComments()) {
					comments.add(generateJSONResultComment(com));
				}
				o.put("comments", comments);
			}

			if (item.getRatings() != null && item.getRatings().size() > 0) {
				List<JSONObject> ratings = new ArrayList<JSONObject>();
				for (ResultRating r : item.getRatings()) {
					ratings.add(generateJSONResultRating(r));
				}
				o.put("ratings", ratings);
			}

			if (item.getTags() != null && item.getTags().size() > 0) {
				List<JSONObject> tags = new ArrayList<JSONObject>();
				for (ResultTag t : item.getTags()) {
					tags.add(generateJSONResultTag(t));
				}
				o.put("tags", tags);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return o;
	}



	/**
	 * generates a JSONObjet representation from the given ResultThumbnail,
	 * returns null if the given argument is null
	 * 
	 * @param t
	 * @return
	 */
	public JSONObject generateJSONResultThumbnail(ResultThumbnail t) {
		if (t == null) {
			return null;
		}

		JSONObject o = new JSONObject();
		try {
			if (t.getUrl() != null) {
				o.put("url", t.getUrl());
			}
			if (t.getWidth() != 0) {
				o.put("width", t.getWidth());
			}
			if (t.getHeight() != 0) {
				o.put("height", t.getHeight());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * generates a JSONObject representation from the given ResultCategory,
	 * returns null if the given argument is null
	 * 
	 * @param c
	 * @return
	 */
	public JSONObject generateJSONResultCategory(ResultCategory c) {
		if (c == null) {
			return null;
		}
		JSONObject o = new JSONObject();
		try {
			if (c.getName() != null) {
				o.put("name", c.getName());
			}
			if (c.getSource() != null) {
				o.put("src", c.getSource());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * generates a JSONObjet representation from the given ResultComment,
	 * returns null if the given argument is null
	 * 
	 * @param c
	 * @return
	 */
	public JSONObject generateJSONResultComment(ResultComment c) {
		if (c == null) {
			return null;
		}

		JSONObject o = new JSONObject();
		try {
			if (c.getDate() != null) {
				o.put("date", c.getDate());
			}
			if (c.getSource() != null) {
				o.put("src", c.getSource());
			}
			if (c.getText() != null) {
				o.put("comment", c.getText());
			}
			if (c.getUserEmail() != null) {
				o.put("user-email", c.getUserEmail());
			}
			if (c.getUserId() != null) {
				o.put("user-id", c.getUserId());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * generates a JSONObjet representation from the given ResultLicence,
	 * returns null if the given argument is null
	 * 
	 * @param l
	 * @return
	 */
	public JSONObject generateJSONResultLicence(ResultLicence l) {
		if (l == null) {
			return null;
		}

		JSONObject o = new JSONObject();
		try {
			if (l.getName() != null) {
				o.put("name", l.getName());
			}
			if (l.getOwner() != null) {
				o.put("owner", l.getOwner());
			}
			if (l.getOwnerEmail() != null) {
				o.put("owner-email", l.getOwnerEmail());
			}
			if (l.getOwnerUrl() != null) {
				o.put("owner-url", l.getOwnerUrl());
			}
			if (l.getUrl() != null) {
				o.put("url", l.getUrl());
			}
			if (l.getUsage() != null) {
				if (l.getUsage().size() > 0) {
					o.put("usage", l.getUsage());
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * generates a JSONObjet representation from the given ResultPreview,
	 * returns null if the given argument is null
	 * 
	 * @param p
	 * @return
	 */
	public JSONObject generateJSONResultPreview(ResultPreview p) {
		if (p == null) {
			return null;
		}

		JSONObject o = new JSONObject();
		try {
			o.put("available", p.isAvailable());
			if (p.getEmbeddableHtml() != null) {
				o.put("embeddable-html", p.getEmbeddableHtml());
			}
			if (p.getGenerationUrl() != null) {
				o.put("generation-url", p.getGenerationUrl());
			}
			if (p.getSource() != null) {
				o.put("src", p.getSource());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	/** generates a JSONObject representation from the given Learning list item,
	 * returns null if the given argument is null or not a learning list
	 * @param item
	 * @return
	 */
	public JSONObject generateJSONLearningListPreview(ResultEntity item) {
		if(item.getClass()==LearningList.class) {
			LearningList l = (LearningList) item;
			if(l.getItems()!=null && l.getItems().size()>0) {
				String embeddedString = "<table class='" + LEARNINGLISTTABLECLASSNAME + "'>";
				//iterate through all list items
				for(ResultEntity e : l.getItems().getResults()) {
					embeddedString+= "<tr><td class='" + LEARNINGLISTITEMCLASSNAME +
					"'><a href='" + e.getUrl() + "'>" + e.getTitle() + "</a></td></tr>";
				}
				embeddedString+= "</table>";
				
				JSONObject o = new JSONObject();
				try {
					o.put("available", true);
					o.put("embeddable-html", embeddedString);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return o;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * generates a JSONObjet representation from the given ResultTag, returns
	 * null if the given argument is null
	 * 
	 * @param t
	 * @return
	 */
	public JSONObject generateJSONResultTag(ResultTag t) {
		if (t == null) {
			return null;
		}

		JSONObject o = new JSONObject();
		try {
			if (t.getName() != null) {
				o.put("name", t.getName());
			}
			if (t.getSource() != null) {
				o.put("src", t.getSource());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * generates a JSONObjet representation from the given ResultUser, returns
	 * null if the given argument is null
	 * 
	 * @param u
	 * @return
	 */
	public JSONObject generateJSONResultUser(ResultUser u) {
		if (u == null) {
			return null;
		}

		JSONObject o = new JSONObject();
		try {
			if (u.getName() != null) {
				o.put("name", u.getName());
			}
			if (u.getEmail() != null) {
				o.put("email", u.getEmail());
			}
			if (u.getProfileUrl() != null) {
				o.put("profile-url", u.getProfileUrl());
			}
			if (u.getUserId() != null) {
				o.put("user-id", u.getUserId());
			}
			if (u.getSource() != null) {
				o.put("src", u.getSource());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * generates a JSONObjet representation from the given ResultRating, returns
	 * null if the given argument is null
	 * 
	 * @param u
	 * @return
	 */
	public JSONObject generateJSONResultRating(ResultRating r) {
		if (r == null) {
			return null;
		}

		JSONObject o = new JSONObject();
		try {
			if (r.getRating() != 0) {
				o.put("rating", r.getRating());
			}
			if (r.getSource() != null) {
				o.put("src", r.getSource());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

}
