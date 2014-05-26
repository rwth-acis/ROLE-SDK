define([ "com", "jquery", "../model/space", "handlebars!./widget" ], function(com, $, space, widgetTemplate) { return {

	interfaces : [ "http://purl.org/role/ui/Header#",
				   "http://purl.org/openapp/int/EventListener#" ],
	
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
	},
	openAppEvent: function() {
	},
	openAppReceipt: function(envelope, message) {
		var widgetId = envelope.source.frameElement.parentElement.parentElement.id.match(/\d+/)[0];
		var dashboardButton = $("#dashboardButton-"+ widgetId);
		if (dashboardButton.size() > 0) {
			dashboardButton.stop(true, true);
			var origColor = dashboardButton
					.css("borderBottomColor");
			dashboardButton
					.css("border-color", "#ffff00")
					.css("background", "url(/s/images/wavebg4.png)");
			dashboardButton.animate({
				borderTopColor : origColor,
				borderLeftColor : origColor,
				borderBottomColor : origColor,
				borderRightColor : origColor
			}, 5000, "linear", function() {
				dashboardButton.css("border",
						"").css("border-color",
						"").css("background",
						"");
			});
		}
	}
}; });