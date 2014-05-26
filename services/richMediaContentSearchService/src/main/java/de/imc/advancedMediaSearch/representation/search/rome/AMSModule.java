package de.imc.advancedMediaSearch.representation.search.rome;

import de.imc.advancedMediaSearch.result.MediaType;
import de.imc.advancedMediaSearch.result.ResultComment;
import de.imc.advancedMediaSearch.result.ResultThumbnail;

import com.sun.syndication.feed.module.Module;

import java.util.List;

public interface AMSModule extends Module {

	public static final String URI = "http://www.im-c.de/AdvancedMediaSearch";

	public String getSource();

	public void setSource(String source);

	public Double getScore();

	public void setScore(Double score);

	public ResultThumbnail getThumbnail();

	public void setThumbnail(ResultThumbnail thumbnail);

	public Integer getLength();

	public void setLength(Integer length);

	public Double getSourceRating();

	public void setSourceRating(Double sourceRating);

	public Integer getUserRating();

	public void setUserRating(Integer userRating);

	public String getEmbeddedHTML();

	public void setEmbeddedHTML(String embeddedHTML);

	public MediaType getMediaType();

	public void setMediaType(MediaType mediaType);

	public String getMimeType();

	public void setMimeType(String mimeType);

	public List<ResultComment> getNotes();

	public void setNotes(List<ResultComment> resultNotes);
}
