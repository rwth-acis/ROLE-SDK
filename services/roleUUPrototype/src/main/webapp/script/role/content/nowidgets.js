define([ "com", "jquery", "handlebars!./nowidgets", "../ui/ui" ], function(com, $, template, ui) { return {

	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	createUI : function(container) {
		var hasWidgets = false;
		container.hide();
		$(template({
			authReturnUri: encodeURIComponent(window.location.pathname)
		})).appendTo(container);
		$(container).on("click", ".widgetStoreLink", function(event) {
			event.preventDefault();
			ui.browse("http://embedded.role-widgetstore.eu", "widgetStore");
		});
		com.on("http://purl.org/role/ui/Activity#", "update", function() {
			hasWidgets = false;
			container.hide();
			window.setTimeout(function() {
				if (!hasWidgets) {
					container.fadeIn();
				}
			}, 100);
		});
		com.on("http://purl.org/role/ui/Widget#", "add", function() {
			hasWidgets = true;
			container.stop(true, false).hide();
		});
	}
	
}; });