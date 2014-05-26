define([ "com", "jquery", "../model/activity", "../model/space", "../ui/ui" ], function(com, $, Activity, space, ui) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#",
	               "http://purl.org/openapp/int/EventListener#" ],
	
	load : function() {
		com.on("http://purl.org/role/ui/Space#", "load", function(space) {
			var activities, activity, properties;
			
			activity = Object.create(Activity);
			activity._uri = space.getUri();
			activity._title = "Start";
			activity._context = space._context;
			activity._space = space;

			com.add(activity);
			com.trigger(activity, "http://purl.org/role/ui/Activity#", "select");
			
			activities = openapp.resource.context(space._context).sub(
					openapp.ns.role + "activity").list();
			for ( var i = 0; i < activities.length; i++) {
				properties = openapp.resource.context(activities[i]).properties();
				activity = Object.create(Activity);
				activity._uri = activities[i].uri;
				activity._context = activities[i];
				activity._space = space;
				activity._title = properties[openapp.ns.dcterms + "title"];
				com.add(activity);
			}
		});
	},
	
	openAppEvent : function(envelope, message) {
		var myOrigin, title, activity;
		if (typeof message !== "undefined"
				&& typeof message[openapp.ns.rdf + "type"] !== "undefined"
				&& message[openapp.ns.rdf + "type"] === openapp.ns.role + "Activity"
				&& typeof envelope.intent !== "undefined"
				&& envelope.intent === "add"
				&& typeof message[openapp.ns.dcterms + "title"] !== "undefined") {
			myOrigin = window.location.href.split("/");
			myOrigin = myOrigin[0] + "/" + myOrigin[1] + "/" + myOrigin[2];
			if (envelope.origin === myOrigin &&
					envelope.source === $("#widgetStoreFrame").get(0).contentWindow) {
								
				title = message[openapp.ns.dcterms + "title"];
				
				openapp.resource.context(space._context).sub(openapp.ns.role + "activity").create(function(context) {
					openapp.resource.context(context).metadata().graph()
					.literal(openapp.ns.dcterms + "title", title).put(function() {
						activity = Object.create(Activity);
						activity._uri = context.uri;
						activity._title = title;
						activity._context = context;
						activity._space = space;
						com.add(activity);
						ui.browse("about:blank");							
					});
				});
				
			}
		}
	}

}; });