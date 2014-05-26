/**
 * @author weber
 * 24.11.2010
 */
package de.imc.advancedMediaSearch.result;

/**
 * @author weber
 *
 */
public class ResultPreview {
	private boolean available;
	private String embeddableHtml;
	private String generationUrl;
	private String source;
	
	public ResultPreview() {
		available = false;
		embeddableHtml = null;
		generationUrl = null;
		setSource(null);
	}
	
	public ResultPreview(boolean available, String embeddableHtml, String generationUrl, String source) {
		this.available = available;
		this.embeddableHtml = embeddableHtml;
		this.generationUrl = generationUrl;
		this.source = source;
	}

	/**
	 * @param embeddableHtml the embeddableHtml to set
	 */
	public void setEmbeddableHtml(String embeddableHtml) {
		this.embeddableHtml = embeddableHtml;
	}

	/**
	 * @return the embeddableHtml
	 */
	public String getEmbeddableHtml() {
		return embeddableHtml;
	}

	/**
	 * @param generationUrl the generationUrl to set
	 */
	public void setGenerationUrl(String generationUrl) {
		this.generationUrl = generationUrl;
	}

	/**
	 * @return the generationUrl
	 */
	public String getGenerationUrl() {
		return generationUrl;
	}

	/**
	 * @param available the available to set
	 */
	public void setAvailable(boolean available) {
		this.available = available;
	}

	/**
	 * @return the available
	 */
	public boolean isAvailable() {
		return available;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	
	
}
