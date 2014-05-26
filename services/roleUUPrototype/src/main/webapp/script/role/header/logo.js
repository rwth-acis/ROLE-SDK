define([ "com", "jquery", "handlebars!./logo" ], function(com, $, template) { return {

	interfaces : [ "http://purl.org/role/ui/Header#" ],
	
	position : "left",
	
	createUI : function(container) {
		$(template()).appendTo(container);
	}
	
}; });