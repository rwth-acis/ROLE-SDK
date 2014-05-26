define([ "com", "jquery", "../model/space", "../ui/ui", "handlebars!./activities",
         "handlebars!./activity", "../feature/activity", "jqueryui/menu", "jqueryui/position", "jqueryui/dialog"], function(com, $, space, ui, template,
         activityTemplate, activity) { return {

	interfaces : [ "http://purl.org/role/ui/Panel#" ],
	
	getTitle : function() {
		return "Activities";
	},

	createUI : function(container) {
		var startActivity = null;
		$(container).parent().addClass("activitiesPanel");
		$(template()).appendTo(container);
		
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
				$("#activity-rename").val(activity.getTitle());
				$( "#activity-rename-dialog" ).dialog({
					resizable: false,
					height:"auto",
					width: "400",
					modal: true,
					buttons: {
					"Rename activity": function() {
						$( this ).dialog( "close" );
						var nt = $("#activity-rename").val();
							activity.setTitle(nt);
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
			ui.browse("/activities?mode=embedded", "newActivity");
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