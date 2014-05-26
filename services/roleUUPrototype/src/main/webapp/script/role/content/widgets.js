define([ "com", "jquery", "handlebars!./widget", "../model/space", "rave", "./info"], function(
		com, $, widgetTemplate, space, rave, info) { return {

	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	createUI : function(container) {
		var _currentActivity = null;
		com.on("http://purl.org/role/ui/Activity#", "select", function(currentActivity) {
			_currentActivity = currentActivity;
		});

		rave.api.rpc.moveWidget = function(uiState) {
			var wid = rave.getObjectIdFromDomId(uiState.widget.id);
			_currentActivity.setWidgetPosition(wid, uiState.targetIndex);
			$(uiState.widget.parentElement).css("width", _currentActivity.getWidgetWidth(_currentActivity.getWidget(wid)));
		};
		com.on("http://purl.org/role/ui/Widget#", "add", function(widget) {
			if (!document.getElementById("widget-" + widget.getRegionWidgetId() + "-wrapper")) {
				var widgetUI = $(widgetTemplate({
					widgetdescription: widget.getDescription(),
					regionWidgetId: widget.getRegionWidgetId(),
					title: widget.getTitle()
				}));
				widgetUI.appendTo("#region-0-id");
				
				$(widgetUI).on("click", ".widget-toolbar-remove-btn", function() {
//					if ($(document.body).is(".not-authenticated")) {
//						alert("You need to sign in to perform this action.");
//						return;						
//					}
					if (space.isCollaborative() && !space.isOwner()  && !space.isMemberAllowedToAddTools()) {
						alert("Only owners of a space can add or remove widgets unless the owner has given explicit permission.");
						return;
					}
					if (confirm("Are you sure you want to remove this widget from your page?")) {
						_currentActivity.removeWidget(widget);
						openapp.resource.del(widget.getUri(), function() {
							space.refresh(function() {
								com.remove(widget);
							});
						});
					}
				});
				
				if (space.isOwner() || space.isMemberAllowedToAddTools()) {
					widgetUI.find(".widgetinfo-save-btn").show();
					widgetUI.find(".cannotEditWidgetinfo").hide();
					widgetUI.find(".widgetinfo-title").removeAttr("disabled");
					widgetUI.find(".widgetinfo-description").removeAttr("disabled");
				}

				widgetUI.on("click", ".widget-toolbar-info-btn", function() {
					widgetUI.find(".widgetinfo").toggle();
				});
				widgetUI.on("click", ".widgetinfo-cancel-btn", function() {
					widgetUI.find(".widgetinfo").hide();
				});

				widgetUI.on("click", ".widgetinfo-save-btn", function() {
					var t = widgetUI.find(".widgetinfo-title").val();
					var d = widgetUI.find(".widgetinfo-description").val();
					widget.setTitleAndDescription(t, d);
					$("#widget-"+widget.getRegionWidgetId()+"-title").html(t);
					$("#widget-"+widget.getRegionWidgetId()+"-toolbar").attr("title", d);
					$("#sideEntry-"+widget.getRegionWidgetId()).attr("title", d).find(".widgettitle").html(t);
					widgetUI.find(".widgetinfo").hide();
				});
				
				
				// Bazaar wishing functionality
				if($(widgetUI).find(".integratedWishingElement").bazaarWishing) {
					// integrated wishing libraries already loaded...
					$(widgetUI).find(".integratedWishingElement").bazaarWishing({softwareUrl : widget._widget.widgetUrl});
				}
				else {
					//require(["/s/script/jquery.plugin.bazaarWishing.nodeps.all.min.js"], function(bazaarWishingPlugin) {
					require(["//requirements-bazaar.org/JQueryBazaarPlugin/js/jquery.plugin.bazaarWishing.nodeps.all.min.js"], function(bazaarWishingPlugin) {
						$(widgetUI).find(".integratedWishingElement").bazaarWishing({softwareUrl : widget._widget.widgetUrl});
					});
				}
				
				$(widgetUI).on("click", ".widget-toolbar-wish-btn", function() {
					// wishing without making an screenshot
					$(widgetUI).find(".integratedWishingElement").bazaarWishing('toggle', false);
				});
				
				
				
				// Make the widget resizable horizontally, unless we're in the dashboard
				// where this currently causes issues
				if (!$(document.body).is(".user-profile")) {
					var widgetdiv = $("#widget-" + widget.getRegionWidgetId() + "-wrapper");
					var w = widget.getWidth();
					if (w != null) {
						widgetdiv.css("width", ""+w+"px");
					}
					widgetdiv.resizable({
						handles : "e",
						resize : function() {
							//$(this).css("overflow", "hidden");
						},
						start : function() {
							$(this).addClass("widget-wrapper-focus");
							$("#pageContent").find("iframe").css("visibility", "hidden");
						},
						stop : function() {
							var j = $(this);
							widget.setWidth(j.width());
							j.removeClass("widget-wrapper-focus");
							$("#pageContent").find("iframe").css("visibility", "visible");
						}
					});
				}
			}
			if (widget._widget.metadata.iframeUrl) {
				rave.initWidgets([ widget._widget ]);				
			}
			rave.initUI();
		});
		com.on("http://purl.org/role/ui/Widget#", "remove", function(widget) {
			$("#widget-" + widget.getRegionWidgetId() + "-wrapper").remove();
		});
	}
	
}; });