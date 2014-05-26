/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.result.preview;

import de.imc.advancedMediaSearch.result.ResultPreview;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class EmbedlyResultPreviewFactory implements ResultPreviewFactory {

	private static final String DEFAULTEMBEDLYREQUESTFORMAT = "json";
	private static final String DEFAULT_SOURCE = "embed.ly";

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.imc.advancedMediaSearch.result.preview.ResultPreviewFactory#
	 * createResultPreview(java.lang.String, java.lang.String, int, int)
	 */
	public ResultPreview createResultPreview(String repositoryId,
			String resultUrl, int width, int height) {

		String previewurl = "http://api.embed.ly/1/oembed?" + "url=" + resultUrl
				+ "&maxwidth=" + width + "&maxheight=" + height + "&format="
				+ DEFAULTEMBEDLYREQUESTFORMAT;
		
		ResultPreview prev = new ResultPreview();
		prev.setAvailable(true);
		prev.setGenerationUrl(previewurl);
		prev.setSource(DEFAULT_SOURCE);
		return prev;
	}

}
