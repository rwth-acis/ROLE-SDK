define([ "com", "jquery", "../ui/ui",
         "handlebars!./store", '../../config' ], function(
        		 com, $, ui, template, config) { return {

	interfaces : [ "http://purl.org/role/ui/Panel#",
	               "http://purl.org/role/ui/Feature#"],
	
	getTitle : function() {
		return "Store";
	},

	createUI : function(container) {
		$(container).parent().addClass("noHeader");
		$(template()).appendTo(container);
		com.on("http://purl.org/role/ui/Space#", "load", function() {
		});
	},
	
	load : function() {
		com.on("http://purl.org/role/ui/DOMReady#", "domReady", function() {
			$(document.body).on("click", "#sideEntry-widgetStore", function(event) {
				event.preventDefault();
				//Ugly, but avoids clumsy inter-module communication.
				$("#sideEntry-addSelectedBundle").hide();
				$("#sideEntry-addSelectedWidgets").hide(); 
				$("#sideEntry-addSelectedWidget").hide(); 
				ui.browse(config.widgetstore, 'widgetStore');
			});
		}.bind(this));
	}
}; });