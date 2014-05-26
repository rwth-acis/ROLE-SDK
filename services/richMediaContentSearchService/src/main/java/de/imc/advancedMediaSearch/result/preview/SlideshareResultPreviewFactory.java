/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.result.preview;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.imc.advancedMediaSearch.result.ResultPreview;
import de.imc.advancedMediaSearch.target.SlideShareTarget;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class SlideshareResultPreviewFactory implements ResultPreviewFactory {

	/**
	 * creates a ResultPreview Object for the given slideshare entry data
	 * 
	 * @param repositoryId
	 *            is ommitted (Default = SlideShareTarget.ID)
	 * @param resultUrl
	 *            the embed code delivered from slideshare
	 * @param width
	 *            the desired height
	 * @param height
	 *            the desired height
	 */
	public ResultPreview createResultPreview(String repositoryId,
			String resultUrl, int width, int height) {
		repositoryId = SlideShareTarget.ID;
		String embedcode = resultUrl;
		
		//replace widths and heigths
		String replace1 = "style=\"width:477px\"";
		String replace2 = "width=\"477\"";
		String replace3 = "height=\"510\"";
		
		embedcode = embedcode.replace(replace1, "style=\"width:" + width + "px\"");
		embedcode = embedcode.replace(replace2, "width=\"" + width + "\"");
		embedcode = embedcode.replace(replace3, "height=\"" + height + "\"");
		
		ResultPreview prev = new ResultPreview(true, embedcode, null, repositoryId);
		return prev;
	}

}
