define([ "com", "../ui/ui", "./space", "../header/windowControls", "../header/embed", "../content/embed", "../content/loginwarning", "../content/info",
		"../header/home", "../header/signin", "../panel/members",
         "../panel/chat", "../feature/chat", "../feature/dashboardinit", "../model/space" ], function(
		com, ui, spaceView, windowControlsHeader, embedHeader, embedContent, loginwarningContent, infoContent, homeHeader, signInHeader, membersPanel, chatPanel, chatFeature,
		dashboardInitFeature, space) { return {

	interfaces : [ "http://purl.org/role/ui/View#" ],
	
	_title : null,
	
	query : function(state, views) {
		if (state.predicate === openapp.ns.role + "space") {
			views.push({
				component : this,
				score : 1
			});
		}
	},
	
	activate : function(state) {
		com.add(embedContent);
		com.add(loginwarningContent);
		com.add(infoContent);
		spaceView.activate(state);
		$(".header-center").hide();
		com.add(windowControlsHeader);
		if (!ui.embedded) {
			com.add(homeHeader);
		}
		com.add(embedHeader);
		com.add(signInHeader);
		com.add(membersPanel);
		com.add(chatPanel);
		com.add(chatFeature);
		var dashboard = state.params.dashboard !== "false";
		if (dashboard) {
			com.add(dashboardInitFeature);
		}
		ui.setHasDashboard(dashboard);
		com.on("http://purl.org/role/ui/Space#", "update", function(space) {
			this._title = space.getTitle();
			com.trigger(this, "http://purl.org/role/ui/View#", "update");
		}.bind(this));
	},
	
	getTitle : function() {
		return this._title;
	}
	
}; });