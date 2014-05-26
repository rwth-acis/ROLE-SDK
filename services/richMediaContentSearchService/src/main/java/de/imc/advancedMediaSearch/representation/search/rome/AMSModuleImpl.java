package de.imc.advancedMediaSearch.representation.search.rome;

import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultComment;
import de.imc.advancedMediaSearch.result.ResultThumbnail;

import com.sun.syndication.feed.module.ModuleImpl;

import java.util.List;

public class AMSModuleImpl extends ModuleImpl implements AMSModule {

    private String entrySource;
    private Double score;
    private ResultThumbnail thumbnail;
    private Integer length;
    private Double sourceRating;
    private Integer userRating;
    private String embeddedHTML;
    private String mimeType;
    private MediaType mediaType;
    private List<ResultComment> notes;

    /**
	 * 
	 */
    private static final long serialVersionUID = -1679488507351113849L;

    public AMSModuleImpl() {
        super(AMSModule.class, AMSModule.URI);
    }

    public void copyFrom(Object obj) {
        AMSModule sm = (AMSModule) obj;

        setSource(sm.getSource());
        setScore(sm.getScore());
        setThumbnail(sm.getThumbnail());
        setLength(sm.getLength());
        setSourceRating(sm.getSourceRating());
        setUserRating(sm.getUserRating());
        setEmbeddedHTML(sm.getEmbeddedHTML());
        setMimeType(sm.getMimeType());
        setMediaType(sm.getMediaType());
        setNotes(sm.getNotes());
    }

    public Class<AMSModule> getInterface() {
        return AMSModule.class;
    }

    /**
     * @return the entrySource
     */
    public String getSource() {
        return entrySource;
    }

    /**
     * @param entrySource the entrySource to set
     */
    public void setSource(String entrySource) {
        this.entrySource = entrySource;
    }

    /**
     * @return the score
     */
    public Double getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(Double score) {
        this.score = score;
    }

    /**
     * @return the length
     */
    public Integer getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * @return the sourceRating
     */
    public Double getSourceRating() {
        return sourceRating;
    }

    /**
     * @param sourceRating the sourceRating to set
     */
    public void setSourceRating(Double sourceRating) {
        this.sourceRating = sourceRating;
    }

    /**
     * @return the userRating
     */
    public Integer getUserRating() {
        return userRating;
    }

    /**
     * @param userRating the userRating to set
     */
    public void setUserRating(Integer userRating) {
        this.userRating = userRating;
    }

    /**
     * @return the entrySource
     */
    public String getEntrySource() {
        return entrySource;
    }

    /**
     * @param entrySource the entrySource to set
     */
    public void setEntrySource(String entrySource) {
        this.entrySource = entrySource;
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
     * @return the embeddedHTML
     */
    public String getEmbeddedHTML() {
        return embeddedHTML;
    }

    /**
     * @param embeddedHTML the embeddedHTML to set
     */
    public void setEmbeddedHTML(String embeddedHTML) {
        this.embeddedHTML = embeddedHTML;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the mediaType
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * @param mediaType the mediaType to set
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * @return the notes
     */
    public List<ResultComment> getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(List<ResultComment> notes) {
        this.notes = notes;
    }

    @Override
    public Object clone() {
      //TODO FIX CLONE
        
        AMSModule sm = new AMSModuleImpl();

        sm.setSource(this.getSource());
        sm.setScore(this.getScore());
        sm.setThumbnail(this.getThumbnail());
        sm.setLength(this.getLength());
        sm.setSourceRating(this.getSourceRating());
        sm.setUserRating(this.getUserRating());
        sm.setEmbeddedHTML(this.getEmbeddedHTML());
        sm.setMimeType(this.getMimeType());
        sm.setMediaType(this.getMediaType());
        
        
        sm.setNotes(this.getNotes());
        
        return sm;
    }
    
}
