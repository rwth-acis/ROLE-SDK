define([ "com", "jquery", "../model/space", "../ui/ui", "handlebars!./activities",
         "handlebars!./activity", "../feature/activity" ], function(com, $, space, ui, template,
         activityTemplate, activity) { return {

	interfaces : [ "http://purl.org/role/ui/Panel#" ],
	
	getTitle : function() {
		return "Activities";
	},

	createUI : function(container) {
		$(container).parent().addClass("activitiesPanel");
		$(template()).appendTo(container);
		com.on("http://purl.org/role/ui/Activity#", "add", function(activity) {
			var element = $(activityTemplate({
				title : activity.getTitle(),
				uri : activity.getUri()
			}));
			if (activity.getUri() === openapp.ns.role + "activity/Overview"
					|| activity.getUri() === space.getUri()) {
				$(element).addClass("activitySel");
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