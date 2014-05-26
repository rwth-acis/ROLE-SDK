define([ "com", "jquery", "../model/space", "handlebars!./chat" ], function(com, $, space, template) { return {

	interfaces : [ "http://purl.org/role/ui/Panel#" ],
	
	getTitle : function() {
		return "Chat";
	},

	createUI : function(container) {
		$(container).parent().addClass("when-member");
		$(template()).appendTo(container);
		com.on("http://purl.org/role/ui/Space#", "load", function() {
		});
	}
	
}; });