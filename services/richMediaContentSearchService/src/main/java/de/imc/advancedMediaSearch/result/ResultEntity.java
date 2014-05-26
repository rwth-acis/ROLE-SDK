package de.imc.advancedMediaSearch.result;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.imc.advancedMediaSearch.restlet.application.AMSApplication;

/**
 * Result entities describe search result items
 */
public class ResultEntity implements Comparable<ResultEntity> {
	
	
	//-----------------------------------------------------
	//---------------------Stonehenge----------------------
	//general information
	protected long id;
	
    /** */
	protected String title;
    
    /** */
	protected String description;
    
    /** */
    private String url;
    
    private List<String> languages;

    /** */
    private String source;
    
    /** */
    private ResultThumbnail thumbnail;
    
    private ResultPreview preview;
    
    /** */
    private MediaType mediaType;
    
    private MimeType mimeType;
    
    private List<ResultCategory> categories;
    
    private List<ResultUser> authorslist;
    
    private ResultUser uploader;
    
    private Date published;
    
    private Date updated;
    
    
    /** */
    private List<ResultUser> authors;

    private List<ResultLicence> licences;
    
    private List<ResultTag> tags;
    
    private List<ResultRating> ratings;
    
    private List<ResultComment> comments;
    
    private int viewCount;
    
    private int length;
    
    private double score;
    
    //-----------------------------------------
    //------------------Xmas-------------------

    /** */

    
    /** */
    private String format;  

    /**
     */
    public ResultEntity() {
        super();
        this.title = "";
        this.authors = null;
        this.description = "";
        this.url = null;
        this.source = "";
        this.thumbnail = null;
        this.length = 0;
        this.published = null;
        this.updated = null;
        this.tags = null;
        this.format = "";
        this.mediaType = MediaType.UNKNOWN;
        this.comments = new ArrayList<ResultComment>();
        this.languages = null;
        this.score = 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ResultEntity resultEntity) {
    	return new RelevanceResultEntityComparator().compare(this, resultEntity);
    }

    public double getUserRating() {
    	if(ratings==null) {
    		return 0;
    	}
    	double rating = 0;
    	
    	for(ResultRating r : ratings) {
    		if(r.getSource().equals(AMSApplication.APPLICATIONID)) {
    			rating = r.getRating();
    		}
    	}
    	return rating;
    }
    
    public void addTag(ResultTag tag) {
    	if(tag==null) {
    		return;
    	}
    	if(tags==null) {
    		tags = new ArrayList<ResultTag>();
    	}
    	tags.add(tag);
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }



    /**
     * @return the thumbnail
     */
    public ResultThumbnail getThumbnail() {
        return thumbnail;
    }

    /**
     * @param thumbnail the thumbnail to set
     */
    public void setThumbnail(ResultThumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the mediaType
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * @param video the mediaType to set
     */
    public void setMediaType(MediaType video) {
        this.mediaType = video;
    }

    /**
     * @return the comments
     */
    public List<ResultComment> getComments() {
        return comments;
    }

    /**
     * @param comments the comments to set
     */
    public void setComments(List<ResultComment> comments) {
        this.comments = comments;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the author
     */
    public List<ResultUser> getAuthors() {
        return authors;
    }

    /**
     * @param author the author to set
     */
    public void setAuthors(List<ResultUser> authors) {
        this.authors = authors;
    }
    
    /**
     * @return the tags
     */
    public String getAuthorsString() {
        
    	if(authors==null || authors.isEmpty()) {
    		return "";
    	}
    	
        Iterator<ResultUser> iter =  authors.iterator();
        
        String auhorsString = "";
        
        while(iter.hasNext()){
            auhorsString += iter.next().getUserId();
            
            if(iter.hasNext()){
                auhorsString += " "; 
            }
        }
        
        return auhorsString;
    }  
    
    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param languages the languages list to set
     */
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }
    
    public void addLanguage(String language) {
    	if(language == null) {
    		return;
    	}
    	
    	if(this.languages==null) {
    		this.languages = new ArrayList<String>();
    	}
    	this.languages.add(language);
    }

    /**
     * @return the language
     */
    public List<String> getLanguages() {
        return languages;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }


    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the published
     */
    public Date getPublished() {
        return published;
    }

    /**
     * @param published the published to set
     */
    public void setPublished(Date published) {
        this.published = published;
    }

    /**
     * @return the updated
     */
    public Date getUpdated() {
        return updated;
    }

    /**
     * @param updated the updated to set
     */
    public void setUpdated(Date updated) {
        this.updated = updated;
    }


    /**
     * @return the tags
     */
    public List<ResultTag> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(List<ResultTag> tags) {
        this.tags = tags;
    }

    /**
     * @return the tags
     */
    public String getTagsString() {
        
    	if(tags==null) {
    		return "";
    	}
    	
        Iterator<ResultTag> iter =  tags.iterator();
        
        String tagsString = "";
        
        while(iter.hasNext()){
        	ResultTag t = iter.next();

            tagsString += t.getName();
            
            if(iter.hasNext()){
                tagsString += " "; 
            }
        }
        
        return tagsString;
    }    
    
    
    public int getCommentCount() {
    	if(comments==null) {
    		return 0;
    	} else {
    		return comments.size(); 
    	}
    }

    /**
     * returns the overall rating of the ResultEntity
     * Algorithm = sum of all attached ratings / number of attached ratings
     * returns 0 if the result has no attached ratings
     * @return the overall rating of the ResultEntity
     */
    public double calculateOverallRating() {
    	if(ratings==null || ratings.size()==0) {
    		return 0;
    	}
    	double numRatings = ratings.size();
    	double sumRatings = 0;
    	for(ResultRating r : ratings) {
    		sumRatings += r.getRating();
    	}
    	return (sumRatings/numRatings);
    }
    /**
     * This method will try to set the MediaType if it is UNKNOWN.
     * It try to determine the media type from the source and the format
     * 
     * @param comments the notes to set
     */
    public void updateMediaType(){
    	if(getMediaType() == MediaType.UNKNOWN && !getFormat().equals("")){
    		setMediaType(MediaType.getTypeFromMimeTypeString(getFormat()));
    	}
    }

	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

	public MimeType getMimeType() {
		return mimeType;
	}

	public void setMimeType(MimeType mimeType) {
		this.mimeType = mimeType;
	}

	public List<ResultCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<ResultCategory> categories) {
		this.categories = categories;
	}

	public List<ResultUser> getAuthorslist() {
		return authorslist;
	}

	public void setAuthorslist(List<ResultUser> authorslist) {
		this.authorslist = authorslist;
	}

	public ResultUser getUploader() {
		return uploader;
	}

	public void setUploader(ResultUser uploader) {
		this.uploader = uploader;
	}

	public List<ResultLicence> getLicences() {
		return licences;
	}

	public void setLicences(List<ResultLicence> licences) {
		this.licences = licences;
	}

	public List<ResultRating> getRatings() {
		return ratings;
	}

	public void setRatings(List<ResultRating> ratings) {
		this.ratings = ratings;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setPreview(ResultPreview preview) {
		this.preview = preview;
	}

	public ResultPreview getPreview() {
		return preview;
	}

	/** adds an author to the result, 
	 * if the author is already present the function does nothing
	 * @param aut
	 */
	public void addAuthor(ResultUser aut) {
		if(aut==null) {
			return;
		}
		if(authors==null) {
			authors = new ArrayList<ResultUser>();
		}
		
		if(!authors.contains(aut)) {
			authors.add(aut); 
		}
	}

	/**
	 * @param rat
	 */
	public void addRating(ResultRating rat) {
		if(rat==null) {
			return;
		}
		if(ratings==null) {
			ratings = new ArrayList<ResultRating>();
		}
		ratings.add(rat);
		
	}
	
	public void addCategory(String categoryName, String source) {
		if(categoryName==null) {
			return;
		}
		if(this.getCategories()==null) {
			this.categories = new ArrayList<ResultCategory>();
		}
		this.categories.add(new ResultCategory(categoryName, source));
	}

}
