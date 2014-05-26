/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.target;

import java.util.List;

import de.imc.advancedMediaSearch.restlet.resource.SearchResource;
import de.imc.advancedMediaSearch.result.MediaType;

/**
 * @author julian.weber@im-c.de
 *
 */
public class QueryArguments {
	private List<MediaType> mediaTypes;
	private boolean preview;
	private int previewHeigth;
	private int previewWidth;
	private String language;
	
	public QueryArguments() {
		this.preview = true;
		this.previewHeigth = SearchResource.DEFAULT_PREVIEW_HEIGHT;
		this.previewWidth = SearchResource.DEFAULT_PREVIEW_WIDTH;
		this.mediaTypes = null;
		this.language = null;
	}
	
	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}

	public int getPreviewHeigth() {
		return previewHeigth;
	}

	public void setPreviewHeigth(int previewHeigth) {
		this.previewHeigth = previewHeigth;
	}

	public int getPreviewWidth() {
		return previewWidth;
	}

	public void setPreviewWidth(int previewWidth) {
		this.previewWidth = previewWidth;
	}

	public void setMediaTypes(List<MediaType> mediaTypes) {
		this.mediaTypes = mediaTypes;
	}

	public List<MediaType> getMediaTypes() {
		return mediaTypes;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	
	/**
	 * returns whether another language than the default language is set
	 * @return
	 */
	public boolean isLanguageSet() {
		if(this.getLanguage()!=null && !this.getLanguage().equals(SearchResource.DEFAULT_LANGUAGE)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	
	
	
	
	
}
