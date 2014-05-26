define([ "com", "jquery", "handlebars!./bookmarklet" ], function(com, $, template) { return {

	interfaces : [ "http://purl.org/role/ui/Header#" ],
	
	position : "right",
	
	createUI : function(container) {
		$(template({
			host: window.location.host
		})).appendTo(container);
	}
	
}; });