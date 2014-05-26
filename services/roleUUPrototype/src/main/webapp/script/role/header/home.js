define([ "com", "jquery", "handlebars!./home" ], function(com, $, template) { return {

	interfaces : [ "http://purl.org/role/ui/Header#" ],
	
	position : "right",
	
	createUI : function(container) {
		$(template()).appendTo(container);
	}
	
}; });