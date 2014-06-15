define([ "com", "jquery", "../ui/ui", "../feature/rave", "../model/space",
         "../panel/activities", "../panel/widgets",
         "../panel/recent", "../content/nowidgets", "../content/duimgrui", "../content/widgets",
         "../feature/activity", "../feature/widget" ], function(
        com, $, ui, raveFeature, space, activitiesPanel, widgetsPanel, recentPanel,
		noWidgetsContent, duiMgrContent, widgetsContent, activityFeature, widgetFeature) { return {

	interfaces : [ "http://purl.org/role/ui/View#" ],
	
	query : function(state, views) {
	},
	
	activate : function(state) {
		ui.setLayout("standard");
		com.add(raveFeature);
		com.add(space);
		com.add(activitiesPanel);
		com.add(duiMgrContent);		//added by Ke Li: DUI manager GUI
		com.add(widgetsPanel);
		//com.add(recentPanel);
		com.add(noWidgetsContent);
		com.add(widgetsContent);
		com.add(activityFeature);
		com.add(widgetFeature);
		space.load(state.uri);
		com.trigger(space, "http://purl.org/role/ui/Resource#", "select");
	}
	
}; });