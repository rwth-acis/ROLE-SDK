define([ "com", "jquery", "handlebars!./signin" ], function(com, $, template) { return {

	interfaces : [ "http://purl.org/role/ui/Header#" ],
	
	position : "right",
	
	createUI : function(container) {
		com.one("http://purl.org/role/ui/User#", "load", function(user) {
			$(template({
				authReturnUri: encodeURIComponent(window.location.pathname+window.location.search)
			})).appendTo(container);
		});
	}
}; });