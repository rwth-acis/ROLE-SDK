/*
 * #%L
 * Conserve Concept Server
 * %%
 * Copyright (C) 2010 - 2011 KMR
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.kth.csc.kmr.conserve.util;

import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.stringtemplate.v4.AttributeRenderer;

public class TemplateHTMLEscaper implements AttributeRenderer {

	@Override
	public String toString(Object arg0, String arg1, Locale arg2) {
		return StringEscapeUtils.escapeHtml(arg0.toString());
	}

}
