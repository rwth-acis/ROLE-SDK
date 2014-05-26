goog.provide("openapp.param");
goog.require("openapp");

/** @namespace OpenApp parameters. */
openapp.param = {};

/**
 * @memberOf openapp.param
 * @private
 */
var parseQueryParams = function(uri) {
	var pairs, pair, i, params = {};
	if (uri.indexOf("?") < 0) {
		return {};
	}
	pairs = uri.substr(uri.indexOf("?") + 1).split("&");
	if (!(pairs.length == 1 && pairs[0] === "")) {
		for (i = 0; i < pairs.length; i++) {
			pair = pairs[i].split("=");
			if (pair.length == 2) {
				params[pair[0]] = window.unescape(pair[1]);
			}
		}
	}
	return params;
}

/**
 * @memberOf openapp.param
 * @private
 */
var parseOpenAppParams = function(queryParams) {
	var namespaces = {}, params = {}, key, segments;
	for (key in queryParams) {
		if (queryParams.hasOwnProperty(key)) {
			if (key.substring(0, 11) === "openapp.ns.") {
				namespaces[key.substr(11)] = queryParams[key];
			}
		}
	}
	for (key in queryParams) {
		if (queryParams.hasOwnProperty(key)) {
			segments = key.split(".");
			if (segments.length === 3 && segments[0] === "openapp"
					&& namespaces.hasOwnProperty(segments[1])) {
				params[namespaces[segments[1]] + segments[2]] = queryParams[key];
			}
		}
	}
	return params;
};

/**
 * @memberOf openapp.param
 * @private
 */
var _openAppParams = parseOpenAppParams(parseQueryParams(parseQueryParams(window.location.href).url
		|| ""));

/**
 * Returns a parameter value.
 *
 * @memberOf openapp.param
 */
openapp.param.get = function(uri) {
	return _openAppParams[uri];
};

/**
 * Returns the space URI.
 * Equivalent to: <pre>openapp.param.get("http://www.role-project.eu/rdf/space")</pre>
 *
 * @memberOf openapp.param
 */
openapp.param.space = function() {
	return openapp.param.get("http://purl.org/role/terms/space");
};

/**
 * Returns the user URI.
 * Equivalent to: <pre>openapp.param.get("http://www.role-project.eu/rdf/user")</pre>
 *
 * @memberOf openapp.param
 */
openapp.param.user = function() {
	return openapp.param.get("http://purl.org/role/terms/user");
};

