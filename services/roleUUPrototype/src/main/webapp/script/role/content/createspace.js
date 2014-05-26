define([ "com", "jquery", "handlebars!./createspace" ], function(com, $, template) { return {

	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	createUI : function(container) {
		var match, id;
		match = window.location.href.match(/\/spaces\/(.*)/);
		id = (match !== null && match.length) > 1 ? match[1] : "";
		if (id.indexOf("?") !== -1) {
			id = id.substring(0, id.indexOf("?"));
		}
		if (id.indexOf("#") !== -1) {
			id = id.substring(0, id.indexOf("#"));
		}
		$(template({
			id : decodeURIComponent(id),
			authReturnUri: encodeURIComponent(window.location.pathname)
		})).appendTo(container);
	}
	
}; });