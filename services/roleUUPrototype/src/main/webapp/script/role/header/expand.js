define([ "com", "jquery", "handlebars!./expand" ], function(com, $, template) { return {

	interfaces : [ "http://purl.org/role/ui/Header#" ],
	
	position : "right",
	
	createUI : function(container) {
		$(template({
			
		})).appendTo(container);
	}
	
}; });