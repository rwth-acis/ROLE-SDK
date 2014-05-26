define([ "com", "jquery", "../model/space", "../ui/ui", "../content/embed", "handlebars!./embed" ], function(com, $, space, ui, embedContent, template) { return {

	interfaces : [ "http://purl.org/role/ui/Header#" ],
	
	position : "right",
	
	createUI : function(container) {
		com.on("http://purl.org/role/ui/Space#", "load", function(space) {
			$(template({
			})).appendTo(container).click(function() {
				embedContent.toggleVisible();
			});
		});
	}
}; });