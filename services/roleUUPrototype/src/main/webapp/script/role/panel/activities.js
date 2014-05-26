define([ "com", "jquery", "../model/space", "../model/activity", "../ui/ui", "handlebars!./activities",
         "handlebars!./activity", "../feature/activity", "../content/createactivity",
         "../model/bundle",
         "jqueryui/menu", "jqueryui/position", "jqueryui/dialog"], 
         function(com, $, space, Activity, ui, template,
         activityTemplate, activity, createactivity,
         bundle) { return {

	interfaces : [ "http://purl.org/role/ui/Panel#",
				   "http://purl.org/openapp/int/EventListener#"],
	
	getTitle : function() {
		return "Activities";
	},
	
	openAppEvent: function(envelope, message) {
		var sideEntry = $("#sideEntry-addSelectedBundle");
		if (envelope.event === "select"
				&& typeof envelope.uri !== "undefined"
				&& typeof message[openapp.ns.dcterms + "title"] !== "undefined"
				&& typeof message[openapp.ns.rdf+ "type"] !== "undefined"
				&& message[openapp.ns.rdf + "type"] === "http://purl.org/role/terms/Bundle") {
			sideEntry.find(".title").html("<strong>Create activity from bundle:</strong> "+message[openapp.ns.dcterms + "title"]);
			sideEntry.attr("data-openapp", JSON.stringify(envelope));
			sideEntry.attr("href", envelope.uri);
			sideEntry.show();
			sideEntry.stop(true, true);
			var origColor = sideEntry.css("color");
			sideEntry.css("background", "#ffff00").css("color", "#000000");
			sideEntry.animate({
				backgroundColor : "#ffffff",
				color : origColor
			}, 5000, "linear", function() {
				sideEntry.css("background", "")
						.css("color", "");
			});
		} else {
			sideEntry.hide();
		}
	},

	createUI : function(container) {
		var startActivity = null;
		$(container).parent().addClass("activitiesPanel");
		$(template()).appendTo(container);
		
		setTimeout(function() {
			$(document.body).on("click", "#sideEntry-addSelectedBundle", function(event) {
				event.preventDefault();
				var oa = $("#sideEntry-addSelectedBundle").attr("data-openapp");
				oa = JSON.parse(oa);
				bundle.loadBundle(oa.uri, function(b) {
					openapp.resource.context(space._context).sub(openapp.ns.role + "activity").create(function(context) {
						openapp.resource.context(context).metadata().graph()
							.literal(openapp.ns.dcterms + "title", b.getLabel())
							.literal(openapp.ns.dcterms + "description", b.getDescription()).put(function() {
							var activity = Object.create(Activity);
							activity._uri = context.uri;
							activity._title = b.getLabel();
							activity._context = context;
							activity._space = space;
							com.add(activity);
							$("#sideEntry-addSelectedBundle").hide();
							$("#sideEntry-addSelectedWidgets").hide(); //Ugly, but avoids clumsy inter-module communication.
							
							var widgets = b.getWidgets();
							var countdown = widgets.length;
							var addWidget = function(widget) {
								if (widget.source == null) {
									return;
								}
								openapp.resource.context(space._context).sub(openapp.ns.role + "tool")
										.type(openapp.ns.role + "OpenSocialGadget")
										.seeAlso(widget.source)
										.control(openapp.ns.role + "activity", context.uri)
										.create(function(tool) {
											openapp.resource.context(tool).metadata().graph()
												.literal(openapp.ns.dcterms + "title", widget.label)
												.literal(openapp.ns.dcterms + "description", widget.desc).put(function() {
													countdown--;
													if (countdown === 0) {
														space.refresh(function() {});
													}
												});
										});
							};
							
							for (var w=0;w<widgets.length;w++) {
								addWidget(widgets[w]);
							}
						});
					});
				});
			//	$(this).hide();
			//	widgetFeature.addWidget($(this).attr('href'));
			});			
		}, 1);
		
		var showMenu = (function(menuId, callback) {
			var firstInit = true;
			var menuButton = null;
			var underlay = null;
			var disableMenu = function(event) {
				event.preventDefault();
				$(menuId).menu("blur").hide();
				underlay.hide();
				menuButton !== null && menuButton.removeClass("onMenu");
				menuButton = null;
			};
			var selectObj = null;
			return function(obj, menuAnchor) {
				var menu = $(menuId);
				if (firstInit) {
					firstInit = false;
					underlay = $(menuId).after("<div class='ROLEMenuUnderlay'></div>").next();
					underlay.click(disableMenu);
					menu.menu({select: function(ev, ui) {
						disableMenu(ev);
						selectObj(ui);
					}});
				}
				menu.show();
				menu.position({of: menuAnchor, my: "left top", at: "left bottom"});
				selectObj = function(ui) {
					callback(obj, menuAnchor, ui.item.index(), ui);
				};
				underlay.show();
				menuAnchor.addClass("onMenu");
				menuButton !== null && menuButton.removeClass("onMenu");
				menuButton = menuAnchor;
			};
		})("#activityMenu", function(activity, activityButtonNode, idx, ui) {
			if (idx === 0) { //Rename
				$("#activity-name").val(activity.getTitle());
				$("#activity-desc").val(activity.getDescription());
				$( "#activity-edit-dialog" ).dialog({
					resizable: false,
					height:"auto",
					width: "400",
					modal: true,
					buttons: {
						"Save": function() {
							$( this ).dialog( "close" );
							var nt = $("#activity-name").val();
							var nd = $("#activity-desc").val();
							activity.setTitleAndDescription(nt, nd);
							activityButtonNode.parent().attr("title", nd);
							activityButtonNode.parent().find(".activity-title").html(nt);
						},
						"Cancel": function() {
							$( this ).dialog( "close" );
						}
					}
				});				
			} else if (idx === 1) { //Delete
				$("#activity_name_remove").html(activity.getTitle());
				$( "#activity-remove-dialog-confirm" ).dialog({
					resizable: false,
					height:"auto",
					width: "400",
					modal: true,
					buttons: {
						"Delete activity": function() {
							$( this ).dialog( "close" );
							var activityRow = activityButtonNode.parent();
							if (activityRow.is(".activitySel")) {
								com.trigger(startActivity, "http://purl.org/role/ui/Activity#", "select");
								$(container).find(".sideEntry:first-child").addClass("activitySel");
							}
							com.remove(activity);
							activity.remove();
							activityRow.remove();
						},
						"Cancel": function() {
							$( this ).dialog( "close" );
						}
					}
				});
			}
		});
		
		
		com.on("http://purl.org/role/ui/Activity#", "add", function(activity) {
			var isStartActivity = activity.getUri() === openapp.ns.role + "activity/Overview"
					|| activity.getUri() === space.getUri();
			var element = $(activityTemplate({
				title : activity.getTitle(),
				description: activity.getDescription(),
				uri : activity.getUri(),
				start: isStartActivity ? " noMenu" : ""
			}));
			element.find(".sideEntryMenuButton").click(function(event) {
				event.preventDefault();
				event.stopPropagation();
				showMenu(activity, element.find(".sideEntryMenuButton"));
			});
			
			if (isStartActivity) {
				$(element).addClass("activitySel");
				startActivity = activity;				
			}
			element.get(0)._component = activity;
			element.appendTo($(container).find("#activityEntries"));
		});
		$(container).on("click", "#sideEntry-newActivity", function(event) {
			event.preventDefault();
			ui.content(createactivity, "newActivity");
//			ui.browse("/activities?mode=embedded", "newActivity");
		});
		$(container).on("click", ".sideEntry", function(event) {
			event.preventDefault();
			if ($(this).is(".activitySel")) {
				// Already selected
				return;
			}
			$(container).find(".sideEntry").removeClass("activitySel");
			$(this).addClass("activitySel");
			com.trigger(this._component, "http://purl.org/role/ui/Activity#", "select");
		});
	}
	
}; });