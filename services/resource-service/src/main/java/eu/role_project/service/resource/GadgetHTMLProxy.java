package eu.role_project.service.resource;

import java.util.regex.Matcher;

public class GadgetHTMLProxy {

	public String replace(String src, String replace, String replacement) {
		return src.replaceAll(Matcher.quoteReplacement(replace), replacement);
	}

}
