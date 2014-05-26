define([ "com", "jquery", "../model/space", "../model/bundle", "../feature/widget", "../ui/ui",
         "handlebars!./widgets", "handlebars!./widget" ], function(
        		 com, $, space,bundle, widgetFeature, ui, template, widgetTemplate) { return {

	interfaces : [ "http://purl.org/role/ui/Panel#",
	               "http://purl.org/role/ui/Feature#",
	               "http://purl.org/openapp/int/EventListener#"],
	
	getTitle : function() {
		return "Widgets";
	},

	createUI : function(container) {
		$(container).parent().addClass("activitiesPanel").addClass("activitiesPanelLast");
		$(template()).appendTo(container);
		com.on("http://purl.org/role/ui/Space#", "load", function() {
		});
		com.on("http://purl.org/role/ui/Widget#", "add", function(widget) {
			var element = $(widgetTemplate({
				title: widget.getTitle(),
				description: widget.getDescription(),
				regionWidgetId: widget.getRegionWidgetId()
			}));
			element.appendTo($(container).find("#widgetEntries"));
			element.slideDown();
		});
		com.on("http://purl.org/role/ui/Widget#", "remove", function(widget) {
			$("#sideEntry-" + widget.getRegionWidgetId()).remove();
		});
	},
	
	openAppEvent: function(envelope, message) {
		if (envelope.event === "select"
				&& typeof envelope.uri !== "undefined"
				&& typeof message[openapp.ns.dcterms + "title"] !== "undefined"
				&& typeof message[openapp.ns.rdf+ "type"] !== "undefined") {
			var sideEntry;
			if (message[openapp.ns.rdf + "type"] === "http://purl.org/role/terms/OpenSocialGadget") {
				$("#sideEntry-addSelectedWidgets").hide();
				sideEntry = $("#sideEntry-addSelectedWidget");
				sideEntry.attr("href", message["http://purl.org/dc/terms/source"] || envelope.uri); //Compatability with old event style.
				sideEntry.find(".title").html("<strong>Add widget:</strong> "+ message[openapp.ns.dcterms + "title"]);
			} else if(message[openapp.ns.rdf + "type"] === "http://purl.org/role/terms/Bundle") {
				$("#sideEntry-addSelectedWidget").hide();
				sideEntry = $("#sideEntry-addSelectedWidgets");
				sideEntry.attr("href", envelope.uri); //No backwards compatability needed since this is new functionality.
				sideEntry.find(".title").html("<strong>Add all widgets from bundle:</strong> "+message[openapp.ns.dcterms + "title"]);
			} else {
				return;
			}
			sideEntry.attr("data-openapp", JSON.stringify(envelope));
			sideEntry.show();
			var origColor = sideEntry.css("color");
			sideEntry.css("background", "#ffff00").css("color", "#000000");
			sideEntry.stop(true, true);
			sideEntry.animate({
				backgroundColor : "#ffffff",
				color : origColor
			}, 5000, "linear", function() {
				sideEntry.css("background", "")
						.css("color", "");
			});
		} else {
			$("#sideEntry-addSelectedWidgets").hide();
			$("#sideEntry-addSelectedWidget").hide();
		}
	},
	openAppReceipt: function(envelope, message) {
		var widgetId = envelope.source.frameElement.parentElement.parentElement.id.match(/\d+/)[0];
		var sideEntry = $("#sideEntry-" + widgetId);
		if (sideEntry.size() > 0) {
			sideEntry.stop(true, true);
			var origColor = sideEntry.css("borderBottomColor");
			sideEntry
					.css("border-top", "1px solid #ffff00")
					.css("border-bottom", "1px solid #ffff00")
					.css("background", "url(/s/images/wavebg5.png) #ffff00");
			sideEntry.animate({
				borderTopColor : origColor,
				borderLeftColor : origColor,
				borderBottomColor : origColor,
				borderRightColor : origColor
			}, 5000, "linear", function() {
				sideEntry.css("padding", "")
						.css("border", "").css(
								"border-color",
								"").css(
								"background",
								"");
			});
		}
	},

	load : function() {
		com.on("http://purl.org/role/ui/DOMReady#", "domReady", function() {
			$(document.body).on("click", "#sideEntry-addWidgetURL", function(event) {
				event.preventDefault();
				widgetFeature.addWidget();
			});
			$(document.body).on("click", "#sideEntry-addSelectedWidget", function(event) {
				event.preventDefault();
				$(this).hide();
				widgetFeature.addWidget($(this).attr('href'));
			});
			$(document.body).on("click", "#sideEntry-addSelectedWidgets", function(event) {
				event.preventDefault();
				var oa = $("#sideEntry-addSelectedWidgets").attr("data-openapp");
				oa = JSON.parse(oa);
				bundle.loadBundle(oa.uri, function(b) {
					widgetFeature.addWidgets(b.getWidgets());
				});
				$(this).hide();
				$("#sideEntry-addSelectedBundle").hide(); //Ugly, but avoid clumsy intercommunication between modules.
			});
			$(document.body).on("click", ".sideEntryTool", function(event) {
				event.preventDefault();
				$(this).stop(true, true);
				ui.canvas(this.id.match(/\d+/)[0], false);
				window.setTimeout(function(){
					widgetFeature._fixIframes();
				}, 100);
			});
		
			$(document.body).on("dblclick", ".sideEntryTool", function() {
				ui.canvas(this.id.match(/\d+/)[0], true);
				window.setTimeout(function(){
					widgetFeature._fixIframes();
				}, 100);
			});
			
			$(document.body).on("dblclick", ".widget-title-bar", function() {
				ui.canvas(this.id.match(/\d+/)[0], true);
				window.setTimeout(function(){
					widgetFeature._fixIframes();
				}, 100);
			});
			
			$(document.body).on("click", ".widget-toolbar-max-btn", function() {
				ui.canvas(this.id.match(/\d+/)[0], true);
				window.setTimeout(function(){
					widgetFeature._fixIframes();
				}, 100);
			});
		}.bind(this));
	}
	
}; });