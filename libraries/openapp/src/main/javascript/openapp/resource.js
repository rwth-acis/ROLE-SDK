goog.provide("openapp.resource");
goog.require("openapp.ns");
goog.require("openapp.io");
goog.require("openapp");

/** @namespace OpenApp Remote Resource API. */
openapp.resource = {};

// Link header parser from http://bill.burkecentral.com/2009/10/15/parsing-link-headers-with-javascript-and-java/
/** @private */
var linkexp=/<[^>]*>\s*(\s*;\s*[^\(\)<>@,;:"\/\[\]\?={} \t]+=(([^\(\)<>@,;:"\/\[\]\?={} \t]+)|("[^"]*")))*(,|\$)/g, paramexp=/[^\(\)<>@,;:"\/\[\]\?={} \t]+=(([^\(\)<>@,;:"\/\[\]\?={} \t]+)|("[^"]*"))/g;
/** @private */
function unquote(value){
    if (value.charAt(0) === '"' && value.charAt(value.length - 1) === '"') {
        return value.substring(1, value.length - 1);
    }
    return value;
}
/** @private */
function parseLinkHeader(value){
   var matches = (value + ",").match(linkexp), rels = {}, titles = {}, rdf = {},
   		i, split, href, ps, link, s, j, p, paramsplit, nme,
   		resource, predicate, linkheader;
   for (i = 0; i < matches.length; i++) {
      split = matches[i].split('>');
      href = split[0].substring(1);
      ps = split[1];
      link = {};
      link.href = href;
      s = ps.match(paramexp);
      for (j = 0; j < s.length; j++) {
         p = s[j];
         paramsplit = p.split('=');
         nme = paramsplit[0];
         link[nme] = unquote(paramsplit[1]);
      }
      if (link.rel !== undefined && typeof link.anchor === "undefined") {
         rels[link.rel] = link;
      }
      if (link.title !== undefined && typeof link.anchor === "undefined") {
         titles[link.title] = link;
      }
      resource = rdf[link.anchor || ""] || {};
      predicate = resource[link.rel || "http://purl.org/dc/terms/relation"] || [];
      predicate.push({
    	  type: "uri",
    	  value: link.href
      });
      resource[link.rel || "http://purl.org/dc/terms/relation"] = predicate;
      rdf[link.anchor || ""] = resource;
   }
   linkheader = {};
   linkheader.rels = rels;
   linkheader.titles = titles;
   linkheader.rdf = rdf;
   return linkheader;
}

/**
 * Helper function for determining when a string is non-empty, that is type of string and not "".
 */
var isStringValue = function(v) {return v !== "" && typeof v === "string"};


/**
 * @private
 * @memberOf openapp.resource
 */
openapp.resource.makeRequest = function(method, uri, callback, link, data, mediaType) {
	var request = openapp.io.createXMLHttpRequest(), linkHeader = "", rel, relCount = 0;
	if (typeof link !== "undefined") {
		for (rel in link) { if (link.hasOwnProperty(rel)) {
			relCount++;
		}}
		if (relCount > 0) {
			var query = "";
			if (uri.indexOf("?") !== -1) {
				query = uri.substring(uri.indexOf("?"));
				uri = uri.substring(0, uri.length - query.length);
			}
			switch (uri.substring(uri.length - 1)) {
				case "/":
					uri += ":";
					break;
				case ":":
					break;
				default:
					uri += "/:";
			}
			for (rel in link) { if (link.hasOwnProperty(rel)) {
		//		if (linkHeader.length > 0) {
		//			linkHeader += ", ";
		//		}
				if (rel === openapp.ns.rdf+"predicate") {
					uri += ";predicate=" + encodeURIComponent(link[rel]);
		//			switch(link[rel]) {
		//				case "http://kmr.csc.kth.se/rdf/conserve/metadata":
		//					uri += "/:metadata";
		//					break;
		//				case "http://kmr.csc.kth.se/rdf/conserve/representation":
		//					uri += "/:representation";
		//					break;
		//				case "http://kmr.csc.kth.se/rdf/conserve/system":
		//					uri += "/:system";
		//					break;
		//				default:
		//					linkHeader += "<" + link[rel] + ">; rel=\"" + rel + "\"";
		//			}
				} else {
					uri += ";" + encodeURIComponent(rel) + "=" + encodeURIComponent(link[rel]);
		//			linkHeader += "<" + link[rel] + ">; rel=\"" + rel + "\"";
				}
			}}
			uri += query;
		}
	}
	request.open(method, uri, true);
	request.setRequestHeader('Accept', 'application/json');
	data = data || "";
	if (data.length > 0 || method === 'POST' || method === 'PUT') {
		request.setRequestHeader('Content-Type', typeof mediaType !==
		  'undefined' ? mediaType : 'application/json');
		//request.setRequestHeader('Content-Length', data.length);
	}
	if (linkHeader.length > 0) {
		request.setRequestHeader('Link', linkHeader);
	}
	callback = callback || function(){};
	request.onreadystatechange = function () {
		if (request.readyState === 4) {
			var context = {
				data: request.responseText,
				link: request.getResponseHeader("link") !== null ? parseLinkHeader(request.getResponseHeader("link")) : {}
			};
			if (isStringValue(request.getResponseHeader("location"))) {
				context.uri = request.getResponseHeader("location");
			} else if (isStringValue(request.getResponseHeader("content-base"))) {
				context.uri = request.getResponseHeader("content-base");
			} else if (context.link.hasOwnProperty("http://purl.org/dc/terms/subject")) {
				context.uri = context.link["http://purl.org/dc/terms/subject"].href;
			}
			if (isStringValue(request.getResponseHeader("content-location"))) {
				context.contentUri = request.getResponseHeader("content-location");
			}
			if (isStringValue(request.getResponseHeader("content-type"))
			  && request.getResponseHeader("content-type").split(";")[0] === "application/json") {
				context.data = JSON.parse(context.data);
			}
			if (typeof request.responseText !== "undefined") {
				if (context.data.hasOwnProperty("")) {
					context.subject = context.data[""];
				} else {
					context.subject = context.data[context.uri] || {};
				}
			} else {
				context.subject = {};
			}
			callback(context);
		}
	};
	request.send(data);
};
// If the gadgets.* API is available (OpenSocial), use it rather than XMLHttpRequest
if (typeof openapp_forceXhr === 'undefined' && typeof gadgets !== 'undefined' && typeof gadgets.io !== 'undefined' &&
		typeof gadgets.io.makeRequest !== 'undefined') {
	openapp.resource.makeRequest = function(method, uri, callback, link, data, mediaType) {
		var params = {}, linkHeader = "", rel, relCount = 0;
		if (typeof link !== "undefined") {
			for (rel in link) { if (link.hasOwnProperty(rel)) {
				relCount++;
			}}
			if (relCount > 0) {
				var query = "";
				if (uri.indexOf("?") !== -1) {
					query = uri.substring(uri.indexOf("?"));
					uri = uri.substring(0, uri.length - query.length);
				}
				switch (uri.substring(uri.length - 1)) {
					case "/":
						uri += ":";
						break;
					case ":":
						break;
					default:
						uri += "/:";
				}
				for (rel in link) { if (link.hasOwnProperty(rel)) {
	//				if (linkHeader.length > 0) {
	//					linkHeader += ", ";
	//				}
					if (rel === openapp.ns.rdf+"predicate") {
						uri += ";predicate=" + encodeURIComponent(link[rel]);
	//					switch(link[rel]) {
	//						case "http://kmr.csc.kth.se/rdf/conserve/metadata":
	//							uri += "/:metadata";
	//							break;
	//						case "http://kmr.csc.kth.se/rdf/conserve/representation":
	//							uri += "/:representation";
	//							break;
	//						case "http://kmr.csc.kth.se/rdf/conserve/system":
	//							uri += "/:system";
	//							break;
	//						default:
	//							linkHeader += "<" + link[rel] + ">; rel=\"" + rel + "\"";
	//					}
					} else {
						uri += ";" + encodeURIComponent(rel) + "=" + encodeURIComponent(link[rel]);
	//					linkHeader += "<" + link[rel] + ">; rel=\"" + rel + "\"";
					}
				}}
				uri += query;
			}
		}
		params[gadgets.io.RequestParameters.GET_FULL_HEADERS] = true;
		params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.TEXT;
		params[gadgets.io.RequestParameters.AUTHORIZATION] = gadgets.io.AuthorizationType.OAUTH;
		params[gadgets.io.RequestParameters.OAUTH_SERVICE_NAME] = "openapp";
		params[gadgets.io.RequestParameters.OAUTH_USE_TOKEN] = "always";
//		params[gadgets.io.RequestParameters.OAUTH_REQUEST_TOKEN] = "mytoken";
//		params[gadgets.io.RequestParameters.OAUTH_REQUEST_TOKEN_SECRET] = "mysecret";
		params[gadgets.io.RequestParameters.METHOD] = method;
		params[gadgets.io.RequestParameters.HEADERS] = params[gadgets.io.RequestParameters.HEADERS] || {};
		if (typeof data !== "undefined" && data !== null) {
			params[gadgets.io.RequestParameters.HEADERS]["Content-Type"] =
			  typeof mediaType !== "undefined" ? mediaType : "application/json";
			params[gadgets.io.RequestParameters.POST_DATA] = data;
		}
		params[gadgets.io.RequestParameters.HEADERS]["Accept"] = "application/json";
		if (linkHeader.length > 0) {
			params[gadgets.io.RequestParameters.HEADERS].link = linkHeader;
		}
		callback = callback || function(){};
		openapp.io.makeRequest(uri, function (response) {
			var context = {
				data: response.data,
				link: typeof response.headers.link !== "undefined" ? parseLinkHeader(response.headers.link[0]) : {}
			};
			if (response.headers.hasOwnProperty("location")) {
				context.uri = response.headers.location[0];
			} else if (response.headers.hasOwnProperty("content-base")) {
				context.uri = response.headers["content-base"][0];
			} else if (context.link.hasOwnProperty("http://purl.org/dc/terms/subject")) {
				context.uri = context.link["http://purl.org/dc/terms/subject"].href;
			}
			if (response.headers.hasOwnProperty("content-location")) {
				context.contentUri = response.headers["content-location"][0];
			}
			if (response.headers.hasOwnProperty("content-type")
			  && response.headers["content-type"][0].split(";")[0] === "application/json") {
				context.data = gadgets.json.parse(context.data);
			}
			if (typeof response.data !== "undefined") {
				if (context.data.hasOwnProperty("")) {
					context.subject = context.data[""];
				} else {
					context.subject = context.data[context.uri] || {};
				}
			} else {
				context.subject = {};
			}
			callback(context);
		}, params);
	};
}

/**
 * Performs a GET request on the given URI.
 *
 * @memberOf openapp.resource
 */
openapp.resource.get = function(uri, callback, link) {
	return openapp.resource.makeRequest("GET", uri, callback,
		link || { "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate": openapp.ns.conserve + "info" });
};

/**
 * Performs a POST request on the given URI.
 *
 * @memberOf openapp.resource
 */
openapp.resource.post = function(uri, callback, link, data, mediaType) {
	return openapp.resource.makeRequest("POST", uri, callback, link, data, mediaType);
};

/**
 * Performs a PUT request on the given URI.
 *
 * @memberOf openapp.resource
 */
openapp.resource.put = function(uri, callback, link, data, mediaType) {
	return openapp.resource.makeRequest("PUT", uri, callback, link, data, mediaType);
};

/**
 * Performs a DELETE request on the given URI.
 *
 * @memberOf openapp.resource
 */
openapp.resource.del = function(uri, callback, link) {
	return openapp.resource.makeRequest("DELETE", uri, callback, link);
};

/**
 * Wraps an object returned by openapp.resource.get/post/put/delete, providing
 *  methods that are performed on that object.
 * <pre>openapp.resource.context(context)&hellip;</pre>
 *
 * @param {object} context An object returned by openapp.resource.get/post/put/delete.
 * @returns {object} A new object that features all the methods of the
 *  open.resource.context namespace below.
 * @memberOf openapp.resource
 * @namespace OpenApp Resource API (context-related methods).
 */
openapp.resource.context = function(context) {
	return {
		/**
		 * Indicates that a method is to be performed on sub-contexts. Methods include
		 *  listing existing sub-contexts and creating a new sub-context.
		 * <pre>openapp.resource.context(context).sub(relation)&hellip;</pre>
		 *
		 * @memberOf openapp.resource.context
		 */
		sub: function(relation) {
			var link = {};
			return {
				/**
				 * Sets a control attribute.
				 * <pre>openapp.resource.context(context).sub(relation).control(key, value)&hellip;</pre>
				 *
				 * @memberOf openapp.resource.context
				 */
				control: function(key, value) {
					link[key] = value;
					return this;
				},
				/**
				 * Sets a type (as a control attribute).
				 * <pre>openapp.resource.context(context).sub(relation).type(value)&hellip;</pre>
				 *
				 * @memberOf openapp.resource.context
				 */
				type: function(value) {
					return this.control(openapp.ns.rdf + "type", value);
				},
				/**
				 * Sets a see-also (as a control attribute).
				 * <pre>openapp.resource.context(context).sub(relation).seeAlso(value)&hellip;</pre>
				 *
				 * @memberOf openapp.resource.context
				 */
				seeAlso: function(value) {
					return this.control(openapp.ns.rdfs + "seeAlso", value);
				},
				/**
				 * Lists the existing sub-contexts.
				 * <pre>openapp.resource.context(context).sub(relation).list()</pre>
				 *
				 * @memberOf openapp.resource.context
				 */
				list: function() {
					var result = [], subs = context.subject[relation], sub, subject, i, key, j, match;
					if (typeof subs === "undefined") {
						return result;
					}
					subfor: for (i = 0; i < subs.length; i++) {
						sub = subs[i];
						subject = context.data[sub.value];
						for (key in link) { if (link.hasOwnProperty(key)) {
							if (!subject.hasOwnProperty(key)) {
								continue subfor;
							}
							match = false;
							for (j = 0; j < subject[key].length; j++) {
								if (subject[key][j].value === link[key]) {
									match = true;
									break;
								}
							}
							if (!match) {
								continue subfor;
							}
						}}
						result.push({
							data: context.data,
							link: {},
							uri: sub.value,
							subject: subject
						});
					}
					return result;
				},
				/**
				 * Creates a new sub-context.
				 * <pre>openapp.resource.context(context).sub(relation).create()</pre>
				 *
				 * @memberOf openapp.resource.context
				 */
				create: function(callback) {
					if (!context.link.rdf.hasOwnProperty(relation)) {
						throw "The context does not support the requested relation: " + relation;
					}
					var postUri = context.uri; //context.link[relation].href;
					link[openapp.ns.rdf + "predicate"] = relation;
					openapp.resource.post(postUri, function(context){
						callback(context);
					}, link);
				}
			};
		},
		/**
		 * Indicates that a method is to be performed on the metadata content.
		 * <pre>openapp.resource.context(context).metadata()&hellip;</pre>
		 * Equivalent to: <pre>openapp.resource.context(context).content(openapp.ns.rest + "metadata")&hellip;</pre>
		 *
		 * @memberOf openapp.resource.context
		 */
		metadata: function() {
			return openapp.resource.context(context).content(openapp.ns.rest + "metadata");
		},
		/**
		 * Indicates that a method is to be performed on the representation content.
		 * <pre>openapp.resource.context(context).representation()&hellip;</pre>
		 * Equivalent to: <pre>openapp.resource.context(context).content(openapp.ns.rest + "representation")&hellip;</pre>
		 *
		 * @memberOf openapp.resource.context
		 */
		representation: function() {
			return openapp.resource.context(context).content(openapp.ns.rest + "representation");
		},
		/**
		 * Indicates that a method is to be performed on a content. A content could be, e.g., the metadata for the context.
		 * <pre>openapp.resource.context(context).content(topic)&hellip;</pre>
		 *
		 * @memberOf openapp.resource.context
		 */
		content: function(topic) {
			return {
				/**
				 * Gets the content (retrieves it from the server).
				 * <pre>openapp.resource.context(context).content(topic).get(callback)</pre>
				 *
				 * @memberOf openapp.resource.context
				 */
				get: function(callback) {
					openapp.resource.get(context.uri, function(content){
						callback(content);
					}, { "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate": topic });
				},
				/**
				 * Sets the media type of the content.
				 * <pre>openapp.resource.context(context).content(topic).mediaType(mediaType)&hellip;</pre>
				 *
				 * @memberOf openapp.resource.context
				 */
				mediaType: function(mediaType) {
					var type = mediaType, data = null;
					return {
					        /**
						 * Sets a string to be the content representation.
						 * <pre>openapp.resource.context(context).content(topic).mediaType(mediaType).string(string)&hellip;</pre>
						 *
						 * @memberOf openapp.resource.context
						 */
						string: function(string) {
							data = string;
							return this;
						},
					        /**
						 * Sets a JSON object to be the content representation.
						 * <pre>openapp.resource.context(context).content(topic).mediaType(mediaType).json(json)&hellip;</pre>
						 *
						 * @memberOf openapp.resource.context
						 */
						json: function(json) {
							data = JSON.stringify(json);
							return this;
						},
					        /**
						 * Puts the content (stores it on the server).
						 * <pre>openapp.resource.context(context).content(topic).mediaType(mediaType).put(callback)</pre>
						 *
						 * @memberOf openapp.resource.context
						 */
						put: function(callback) {
							openapp.resource.put(context.uri, function(content){
								if (typeof callback === 'function') {
									callback(content);
								}
							}, { "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate": topic }, data, type);							
						}
					};
				},
				/**
				 * Sets a string to be content representation with text/plain as the media type.
				 * <pre>openapp.resource.context(context).content(topic).string(string)&hellip;</pre>
				 * Equivalent to: <pre>openapp.resource.context(context).content(topic).mediaType("text/plain").string(string)</pre>
				 *
				 * @memberOf openapp.resource.context
				 */
				string: function(string) {
					return this.mediaType("text/plain").string(string);
				},
				/**
				 * Sets a JSON object to be content representation with application/json as the media type.
				 * <pre>openapp.resource.context(context).content(topic).json(json)&hellip;</pre>
				 * Equivalent to: <pre>openapp.resource.context(context).content(topic).mediaType("text/plain").string(string)</pre>
				 *
				 * @memberOf openapp.resource.context
				 */
				json: function(json) {
					return this.mediaType("application/json").json(json);
				},
				/**
				 * Sets a graph to be content representation.
				 * <pre>openapp.resource.context(context).content(topic).graph()&hellip;</pre>
				 *
				 * @memberOf openapp.resource.context
				 */
				graph: function() {
					var graph = {}, subj = "";
					return {
						/**
						 * Sets the subject of a triple in the graph (default subject: the context).
						 * <pre>openapp.resource.context(context).content(topic).graph().subject(subject)&hellip;</pre>
						 *
						 * @memberOf openapp.resource.context
						 */
						subject: function(subject) {
							subj = subject;
							return this;
						},
						/**
						 * Sets the predicate and object (a URI) of a triple in the graph.
						 * <pre>openapp.resource.context(context).content(topic).graph().resource(predicate, object)&hellip;</pre>
						 *
						 * @memberOf openapp.resource.context
						 */
						resource: function(predicate, object) {
							graph[subj] = graph[subj] || {};
							graph[subj][predicate] = graph[subj][predicate] || [];
							graph[subj][predicate].push({ value: object, type: "uri" });
							return this;
						},
						/**
						 * Sets the predicate and object (a literal) of a triple in the graph.
						 * <pre>openapp.resource.context(context).content(topic).graph().literal(predicate, literal, lang, datatype)&hellip;</pre>
						 *
						 * @memberOf openapp.resource.context
						 */
						literal: function(predicate, literal, lang, datatype) {
							graph[subj] = graph[subj] || {};
							graph[subj][predicate] = graph[subj][predicate] || [];
							graph[subj][predicate].push({ value: literal, type: "literal", lang: lang, datatype: datatype });
							return this;
						},
						/**
						 * Puts the content (stores it on the server).
						 * <pre>openapp.resource.context(context).content(topic).graph().put()</pre>
						 *
						 * @memberOf openapp.resource.context
						 */
						put: function(callback) {
							openapp.resource.put(context.uri, function(content){
								if (typeof callback === 'function') {
									callback(content);
								}
							}, { "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate": topic }, JSON.stringify(graph));
						}
					};
				}
			};
		},
		/**
		 * Returns the context's data as namespaced properties.
		 * <pre>openapp.resource.context(context).properties()</pre>
		 *
		 * @memberOf openapp.resource.context
		 * @private
		 */
		properties: function() {
			var result = {}, pred;
			for (pred in context.subject) { if (context.subject.hasOwnProperty(pred)) {
				result[pred] = context.subject[pred][0].value;
			}}
			return result;
		},
		/**
		 * Returns the context's data as a string.
		 * <pre>openapp.resource.context(context).string()</pre>
		 *
		 * @memberOf openapp.resource.context
		 * @private
		 */
		string: function() {
			if (typeof context.data === "string") {
				return context.data;
			} else {
				return gadgets.json.stringify(context.data);
			}
		},
		/**
		 * Returns the context's data as a JSON object.
		 * <pre>openapp.resource.context(context).json()</pre>
		 *
		 * @memberOf openapp.resource.context
		 * @private
		 */
		json: function() {
			if (typeof context.data === "string") {
				return null;
			} else {
				return context.data;
			}
		},
		/**
		 * If the context is a reference to another context, then return the referenced context.
		 * <pre>openapp.resource.context(context).followSeeAlso()&hellip;</pre>
		 *
		 * @memberOf openapp.resource.context
		 */
		followSeeAlso: function() {
			var seeAlso = context.subject[openapp.ns.rdfs + "seeAlso"], slashes = 0, totalSlashes, i;
			if (typeof seeAlso !== "undefined") {
				seeAlso = seeAlso[0].value;
				
				// Ugly reasoning about whether to follow the reference
				for (i = 0; i < seeAlso.length && i < context.uri.length && seeAlso.charAt(i) === context.uri.charAt(i); i++) {
					if (seeAlso.charAt(i) === "/") {
						slashes++;
					}
				}
				totalSlashes = slashes;
				for (; i < seeAlso.length; i++) {
					if (seeAlso.charAt(i) === "/") {
						totalSlashes++;
					}
				}
				if (slashes < 3 || totalSlashes > 4) {
					return this;
				}

				return openapp.resource.context({
					data: context.data,
					link: {},
					uri: seeAlso,
					subject: context.data[seeAlso]
				});
			} else {
				return this;
			}
		}
	};
};

/**
 * Wraps an object returned by openapp.resource.context.content.get, providing
 *  methods that are performed on that object.
 * <pre>openapp.resource.content(content)&hellip;</pre>
 *
 * @param {object} context An object returned by openapp.resource.context.content.get.
 * @returns {object} A new object that features all the methods of the
 *  open.resource.content namespace below.
 * @memberOf openapp.resource
 * @namespace OpenApp Remote Resource API (content-related methods).
 */
openapp.resource.content = function(content) {
	return {
		/**
		 * Returns the content's data as namespaced properties.
		 * <pre>openapp.resource.content(content).properties()</pre>
		 *
		 * @memberOf openapp.resource.content
		 */
		properties: function() {
			return openapp.resource.context(content).properties();
		},
		/**
		 * Returns the content's data as a string.
		 * <pre>openapp.resource.content(content).string()</pre>
		 *
		 * @memberOf openapp.resource.content
		 */
	        string: function() {
			return openapp.resource.context(content).string();
		},
		/**
		 * Returns the content's data as a JSON object.
		 * <pre>openapp.resource.content(context).json()</pre>
		 *
		 * @memberOf openapp.resource.content
		 */
	        json: function() {
			return openapp.resource.context(content).json();
		}
	}
};
