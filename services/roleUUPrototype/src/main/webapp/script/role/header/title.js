define([ "com", "jquery", "../model/space", "handlebars!./title" ], function(com, $, space, template) { return {

	interfaces : [ "http://purl.org/role/ui/Header#" ],
	
	position : "title",
	
	createUI : function(container) {
		com.on("http://purl.org/role/ui/Space#", "load", function(space) {
			$(template({
				uri : space.getUri(),
				title : space.getTitle(),
				subtitle : space.getSubtitle()
			})).appendTo(container);
		});
	}
	
}; });