package se.kth.csc.kmr.conserve.logic;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.util.Base64UUID;
import se.kth.csc.kmr.conserve.util.RDFJSON;

public class HTMLAppResponder extends ResourceResponder {

	@Inject
	private SystemResponder systemResponder;

	@Override
	public boolean canGet(Request request) {
		return true;
	}

	@Override
	public Object doGet(final Request request) {
		StreamingOutput streamingOutput = new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(
						output, Charset.forName("UTF-8")));
				JsonFactory jsonFactory = new JsonFactory();
				JsonGenerator json = jsonFactory.createJsonGenerator(writer);
				json.useDefaultPrettyPrinter();

				writer.print("<!doctype html><html><head><title></title>");
				writer.print("<noscript><meta");
				writer.printf(" http-equiv=\"refresh\" content=\"0; url=%s\">",
						"/noscript");
				writer.print("</noscript></head>");
				writer.print("<body><script>(function(){");
				writer.print("var s=document.createElement(\"script\");");
				writer.printf("s.src=\"%s\";", "/s/script/require.js");
				writer.print("s.setAttribute(\"data-main\",window.location.search.match");
				writer.printf("(/(^\\?|&)debug=0(&|$)/)?\"%s\":\"%s\");",
						"/s/script/main-built.js", "/s/script/main.js");
				writer.print("document.body.appendChild(s);})();");
				// writer.print("window.addEventListener(\"load\",function(){");
				// writer.print("window.postMessage(JSON.stringify(");
				writer.print("var _openapp_event=");
				writer.flush();

				json.writeStartObject();
				json.writeFieldName("OpenApplicationEvent");
				json.writeStartObject();

				json.writeFieldName("event");
				json.writeString("statechange");
				json.writeFieldName("uri");
				json.writeString(store().in(request.getContext()).uri()
						.toString());
				json.writeFieldName("type");
				json.writeString("namespaced-properties");
				json.writeFieldName("message");

				json.writeStartObject();
				// Graph graph = new GraphImpl();
				// systemResponder.addTriples(graph, request,
				// request.getContext(), store().in(request.getContext())
				// .uriBuilder());
				// RDFJSON.graphToRdfJsonJackson(graph, json,
				// store().in(request.getContext()).uri().toString());

				Concept concept = request.getContext();

				String contextUri = store()
						.in(store.getConcept(concept.getContext())).uri()
						.toString();
				if (contextUri.startsWith("urn:uuid:")) {
					contextUri = ((RequestImpl) request).getUriInfo()
							.getBaseUriBuilder()
							.path(Base64UUID.encode(concept.getContext()))
							.build().toString();
				}
				json.writeFieldName("http://purl.org/openapp/context");
				json.writeString(contextUri);

				json.writeFieldName("http://purl.org/openapp/predicate");
				json.writeString((store.getConcept(concept.getPredicate())
						.getId()));

				json.writeEndObject();

				json.writeEndObject();
				json.writeEndObject();
				json.flush();

				// writer.print("),\"*\");},false);");
				writer.print(";");
				writer.print("</script></body></html>");
				writer.flush();
				output.close();
			}
		};
		return Response.ok().header("Content-Type", "text/html; charset=UTF-8")
				.header("Cache-Control", "no-store").entity(streamingOutput)
				.build();
	}
}
