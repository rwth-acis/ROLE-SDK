define([ "com", "jquery", "handlebars!./loginwarning" ], function(com, $, template) { return {

	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	createUI : function(container) {
		com.on("http://purl.org/role/ui/Space#", "load", function(space) {
			setTimeout(function() {
				var node = $(template({})).appendTo(container);
				node.find(".role_content_close").click(function() {container.hide();});
			}, 2000);
		});
	}
}; });