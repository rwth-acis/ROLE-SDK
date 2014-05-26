/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.result.preview;

import de.imc.advancedMediaSearch.result.ResultPreview;
import de.imc.advancedMediaSearch.target.YoutubeTarget;

/**
 * @author julian.weber@im-c.de
 *
 */
public class YoutubeResultPreviewFactory implements ResultPreviewFactory {

	private static final String EMBEDDED_LANGUAGE_CODE = "en_US";
	
	/**
	 * creates a ResultPreview Object for the given youtube video entry data
	 * @param repositoryId is ommitted (Default = YoutubeTarget.ID)
	 * @param resultUrl the video id string
	 * @param width the desired height
	 * @param height the desired height
	 */
	public ResultPreview createResultPreview(String repositoryId,
			String resultUrl, int width, int height) {

		//use the youtube repository id
		repositoryId = YoutubeTarget.ID;
		
		//@author daniel.dahrendorfer@im-c.de
		String embeddedHTML = "<object width=\""
			+ width
			+ "\" height=\""
			+ height
			+ "\">"
			+ "<param name=\"movie\" value=\""
			+ "http://www.youtube.com/v/"
			+ resultUrl
			+ "&hl="
			+ EMBEDDED_LANGUAGE_CODE
			+ "&fs=1&color1=0x3a3a3a&color2=0x999999\"></param>"
			+ "<param name=\"allowFullScreen\" value=\"true\"></param>"
			+ "<param name=\"allowscriptaccess\" value=\"always\"></param>"
			+ "<embed src=\""
			+ "http://www.youtube.com/v/"
			+ resultUrl
			+ "&hl="
			+ EMBEDDED_LANGUAGE_CODE
			+ "&fs=1&color1=0x3a3a3a&color2=0x999999\" type=\"application/x-shockwave-flash\" allowscriptaccess=\"always\" allowfullscreen=\"true\" width=\""
			+ width + "\" height=\"" + height
			+ "\"></embed>" + "</object>";
		
		ResultPreview prev = new ResultPreview();
		prev.setAvailable(true);
		prev.setEmbeddableHtml(embeddedHTML);
		prev.setSource(YoutubeTarget.ID);
		return prev;
	}

}
