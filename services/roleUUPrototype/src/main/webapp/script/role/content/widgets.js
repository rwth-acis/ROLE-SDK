define([ "com", "jquery", "handlebars!./widget", "../model/space", "../feature/duimanager", "detectmobile" ], function(
		com, $, widgetTemplate, space, duiManager, detectMobile) { return {

	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	createUI : function(container) {
		com.on("http://purl.org/role/ui/Widget#", "add", function(widget) {
			//added by Ke Li: when the widget is in the widget list but not in the to-be-displayed list, do not render it.
			if (space.isMember() && duiManager.widgets.indexOf(widget.getRegionWidgetId()) != -1 
					&& duiManager.widgetsWhiteList.indexOf(widget.getRegionWidgetId()) == -1)
				return;
			if (!document.getElementById("widget-" + widget.getRegionWidgetId() + "-wrapper")) {
				var widgetUI = $(widgetTemplate({
					regionWidgetId: widget.getRegionWidgetId(),
					title: widget.getTitle()
				}));
				widgetUI.appendTo("#region-0-id");
				
				$(widgetUI).on("click", ".widget-toolbar-remove-btn", function() {
//					if ($(document.body).is(".not-authenticated")) {
//						alert("You need to sign in to perform this action.");
//						return;						
//					}
					if (space.isCollaborative() && !space.isOwner()) {
						alert("Currently only the owners of a space can add and remove widgets.");
						return;
					}
					if (confirm("Are you sure you want to remove this widget from your page?")) {
						openapp.resource.del(widget.getUri(), function() {
							space.refresh(function() {
								com.remove(widget);
								duiManager.onUserRemoveWidget(widget); //added by Ke Li: watch on the only source that can really delete the widget
							});
						});
					}
				});
				$(widgetUI).on("click", ".notmaximize img", function(){
					var widgetId = widget.getRegionWidgetId();
					if (widgetId != null) {
						$('#widget-' + widgetId + '-wrapper').removeClass("widget-wrapper-canvas");
						$("#sideEntry-" + widgetId).removeClass("sideEntrySel");
						$(".widget-wrapper").css("display", "");
						(function(id) {
							window.setTimeout(function() {
								var wdgt = rave.getWidgetById(id);
								if (typeof wdgt !== "undefined") {
									wdgt.minimize();
									$(".widget-wrapper").find("iframe").attr("width", "100%");
									$(".widget-wrapper-canvas").find("iframe").attr("width", "100%");						
								}
								$('#widget-' + id + '-wrapper').removeClass("widget-wrapper-canvas");
								$('#widget-' + id + '-wrapper').css({"width": "", "height": ""});
								$("#sideEntry-" + id).removeClass("sideEntrySel");
							}, 1);
						})(widgetId);
					}
				});
				// Make the widget resizable horizontally, unless we're in the dashboard
				// where this currently causes issues
				if (!$(document.body).is(".user-profile")) {
					if (!detectMobile.isMobile())
						$("#widget-" + widget.getRegionWidgetId() + "-wrapper").resizable({
							handles : "e",
							resize : function() {
								//$(this).css("overflow", "hidden");
							},
							start : function() {
								$(this).addClass("widget-wrapper-focus");
								$("#pageContent").find("iframe").css("visibility", "hidden");
							},
							stop : function() {
								$(this).removeClass("widget-wrapper-focus");
								$("#pageContent").find("iframe").css("visibility", "");
							}
						});
				}
			}		
			rave.initWidgets([ widget._widget ]);
			rave.initUI(detectMobile.isMobile(), detectMobile.isTouch());
		});
		com.on("http://purl.org/role/ui/Widget#", "remove", function(widget) {
			$("#widget-" + widget.getRegionWidgetId() + "-wrapper").remove();
		});
	}
	
}; });