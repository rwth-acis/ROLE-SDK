define([ "com", "jquery", "../model/space", "handlebars!./widget" ], function(com, $, space, widgetTemplate) { return {

	interfaces : [ "http://purl.org/role/ui/Header#" ],
	
	position : "center",
	
	createUI : function(container) {
		com.on("http://purl.org/role/ui/Widget#", "add", function(widget) {
			$(widgetTemplate({
				title: widget.getTitle(),
				regionWidgetId: widget.getRegionWidgetId()
			})).appendTo(container);
		});
		com.on("http://purl.org/role/ui/Widget#", "remove", function(widget) {
			$("#dashboardButton-" + widget.getRegionWidgetId()).remove();
		});
	}
	
}; });