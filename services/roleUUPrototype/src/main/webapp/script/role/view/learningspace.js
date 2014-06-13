define([ "com", "./space", "../header/home", "../header/signin", "../panel/members", "../panel/device",
         "../panel/chat", "../feature/chat", "../feature/dashboardinit", "../model/space", 
         "../header/sidepanelheader", "detectmobile" ], function(
		com, spaceView, homeHeader, signInHeader, membersPanel, devicePanel, chatPanel, chatFeature,
		dashboardInitFeature, space, sidePanelHeader, detectMobile) { return {

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
		spaceView.activate(state);
		$(".header-center").hide();
		//com.add(homeHeader);
		sidePanelHeader.createUI(detectMobile.isMobile());
		com.add(signInHeader);
		com.add(membersPanel);
		com.add(devicePanel);	//added by Ke Li: device panel
		com.add(chatPanel);
		com.add(chatFeature);
		com.add(dashboardInitFeature);
		com.on("http://purl.org/role/ui/Space#", "update", function(space) {
			this._title = space.getTitle();
			com.trigger(this, "http://purl.org/role/ui/View#", "update");
		}.bind(this));
	},
	
	getTitle : function() {
		return this._title;
	}
	
}; });