/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.result.preview;

import de.imc.advancedMediaSearch.result.ResultPreview;

/**
 * Interface for ResultPreview Factory classes
 * @author julian.weber@im-c.de
 *
 */
public interface ResultPreviewFactory {
	
	/**
	 * creates a result preview
	 * @param repositoryId the repository's id string
	 * @param resultUrl the url of the ResultItem to preview
	 * @return a ResultPreview of the given resultUrl
	 */
	public ResultPreview createResultPreview(String repositoryId, String resultUrl, int width, int height);
}
