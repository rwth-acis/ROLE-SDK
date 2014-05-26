define(["com", "jquery", "role/ui/ui", "role/model/user", "role/header/title",
        "role/feature/authentication", "role/feature/xmpp", "role/feature/iwc",
        "role/feature/dashboard", "role/feature/viewer", "role/feature/title",
        "role/feature/favicon", "role/view/home", "role/view/newspace",
        "role/view/learningspace", "role/view/personalspace"
        /*"less!space.less",*/ ],
function(com, $, ui, user, titleHeader,
		authenticationFeature, xmppFeature, iwcFeature, dashboardFeature, viewerFeature,
		titleFeature, favIconFeature, homeView, newSpaceView, learningSpaceView,
		personalSpaceView) {
	
	com.add(ui);
	com.add(user);
	com.add(titleHeader);
	com.add(authenticationFeature);
	com.add(xmppFeature);
	com.add(viewerFeature);
	com.add(titleFeature);
	com.add(favIconFeature);
	com.add(homeView);
	com.add(newSpaceView);
	com.add(learningSpaceView);
	com.add(personalSpaceView);
//	com.add(activitiesView);
	
//	com.add(iwcFeature);
//	com.add(dashboardFeature);
	
	com.on("http://purl.org/role/ui/Feature#", "add", function(feature) {
		//console.log("Added feature");
		//console.log(feature);
		feature.load();		
	});
	
	// As gadgets.openapp.connect supports only one event handler, it is necessary
	// that the container performs only one gadgets.openapp.connect, and that
	// all others listen to the jQuery events. Otherwise, only the last caller of
	// gadgets.openapp.connect will be able to receive event messages.
	gadgets.openapp.connect(function(envelope, message) {
		var event = $.Event("openapp");
		event.envelope = envelope;
		event.message = message;
		$(document).trigger(event);
	});
	
});