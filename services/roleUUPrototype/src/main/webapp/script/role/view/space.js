define([ "com", "jquery", "../ui/ui", "../feature/rave", "../model/space",
         "../panel/activities", "../panel/widgets", "../panel/store",
         "../panel/recent", "../content/nowidgets", "../content/widgets",
         "../feature/activity", "../feature/widget" ], function(
        com, $, ui, raveFeature, space, activitiesPanel, widgetsPanel, storePanel, recentPanel,
		noWidgetsContent, widgetsContent, activityFeature, widgetFeature) { return {

	interfaces : [ "http://purl.org/role/ui/View#" ],
	
	query : function(state, views) {
	},
	
	activate : function(state) {
		ui.setLayout("standard");
		var embedded = state.params.embedded === "" || state.params.embedded === "true";
		ui.setIsEmbedded(embedded);
		com.add(raveFeature);
		com.add(space);
		com.add(activitiesPanel);
		com.add(widgetsPanel);
		com.add(storePanel);
		//com.add(recentPanel);
		com.add(noWidgetsContent);
		com.add(widgetsContent);
		com.add(activityFeature);
		com.add(widgetFeature);
		space.load(state.uri);
		com.trigger(space, "http://purl.org/role/ui/Resource#", "select");
	}
	
}; });