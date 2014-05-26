define([ "com", "jquery", "handlebars!./createactivity", "../model/activity", "../model/space", "../ui/ui" ], function(com, $, template, Activity, space, ui) { 
	return {

	interfaces : [ "http://purl.org/role/ui/Content#" ],
	
	createUI : function(container) {
		$(template()).appendTo(container);
		$(container).find("#cancel-create-activity").click(function(event) {
			ui.content();
		});
		var createActivity = function(event) {
			event.preventDefault();
			var name, message;
			name = $("#activityName").val().trim();
			if (name.length === 0) {
				alert("Please enter an activity name.");
				return;
			}
			openapp.resource.context(space._context).sub(openapp.ns.role + "activity").create(function(context) {
				openapp.resource.context(context).metadata().graph()
					.literal(openapp.ns.dcterms + "title", name).put(function() {
						var activity = Object.create(Activity);
						activity._uri = context.uri;
						activity._title = name;
						activity._context = context;
						activity._space = space;
						com.add(activity);
						ui.content();
					});
				});
/*			message = {};
			message[openapp.ns.rdf + "type"] = "http://purl.org/role/terms/Activity";
			message[openapp.ns.dcterms + "title"] = name;
			gadgets.openapp.publish({
				intent : "add",
				type : "namespaced-properties",
				message : message
			});*/
		};
	    $(container).find("#create-create-activity").click(createActivity);
	    $("#activityName").keypress(function(e) {
			if(e.which == 13) {
		    	createActivity(e);
			}
	    });		
	}
}; });