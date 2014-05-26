define([ "com", "jquery", "../model/space", "handlebars!./title", "../content/info" ], function(com, $, space, template, infoContent) { return {

	interfaces : [ "http://purl.org/role/ui/Header#" ],
	
	position : "title",
	
	createUI : function(container) {
		com.on("http://purl.org/role/ui/Space#", "load", function(space) {
			var node = $(template({
				uri : space.getUri(),
				title : space.getTitle(),
				subtitle : space.getSubtitle(), 
				description: space.getDescription() || ""
			})).appendTo(container);
			
			node.click(function() {
				infoContent.toggleVisible();
			});
		});
	}
	
}; });