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
package se.kth.csc.kmr.conserve.security.openid;

import java.nio.charset.Charset;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.logic.RepresentationResponder;

public class OpenID extends RepresentationResponder {

	@Override
	public Object doGet(Request request) {
		UriBuilder uri = ((RequestImpl) request).getUriInfo()
				.getBaseUriBuilder().path("o/openid/server");
		StringBuffer xrds = new StringBuffer();
		xrds.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xrds.append("<xrds:XRDS xmlns:xrds=\"xri://$xrds\" xmlns=\"xri://$xrd*($v*2.0)\">\n");
		xrds.append(" <XRD>\n");
		xrds.append("  <Service priority=\"0\">\n");
		xrds.append("   <Type>http://specs.openid.net/auth/2.0/signon</Type>\n");
		xrds.append("   <Type>http://specs.openid.net/auth/2.0/server</Type>\n");
		// xrds.append("   <Type>http://openid.net/srv/ax/1.0</Type>\n");
		// xrds.append("   <Type>http://specs.openid.net/extensions/ui/1.0/mode/popup</Type>\n");
		// xrds.append("   <Type>http://specs.openid.net/extensions/ui/1.0/icon</Type>\n");
		// xrds.append("   <Type>http://specs.openid.net/extensions/pape/1.0</Type>\n");
		xrds.append("   <URI>" + uri.build().toString() + "</URI>\n");
		xrds.append("  </Service>\n");
		xrds.append(" </XRD>\n");
		xrds.append("</xrds:XRDS>\n");
		return Response.ok()
				.entity(xrds.toString().getBytes(Charset.forName("UTF-8")))
				.type("application/xrds+xml; charset=UTF-8")
				.header("Cache-Control", "no-store").build();
	}

}
