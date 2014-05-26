define(["jquery", "jqueryui/selectable", "jqueryui/dialog"], function($) {
	$(document).bind("plespace", function(event) {
		//event.spaceResource event.userURI
		updateParticipants(event.spaceResource);
		updateMemberOfSpace(event.spaceResource, event.memberOfSpace);
	});
	var updateMemberOfSpace = function(spaceResource, memberOfSpace) {
		if (memberOfSpace) {
			var preId = 0;
			var addParticipant = function() {
				var pre = "addP"+preId+"_";
				preId++;
				openapp.resource.get("/users", function(context) {
					var users = openapp.resource.context(context).sub(openapp.ns.foaf + "member").list();
					var dialogNode = $("#dialogNode").html("");
					var listNode = $("<ul class='selectable'></ul>").appendTo(dialogNode);
					$.each(users, function(index, user) {
						openapp.resource.context(user).followSeeAlso().metadata().get(function(content){
							var metadata = openapp.resource.context(content).properties();
							listNode.append("<li id='"+pre+index+"'class='ui-widget-content'>"+metadata[openapp.ns.dcterms + "title"]+"</li>");
						});
					});
					listNode.selectable();
					var addP = function() {
						listNode.find(".ui-selected").each(function(index, node) {
							var idx = $(node).attr("id");
							idx = parseInt(idx.substr(idx.indexOf("_")+1));
							spaceResource.create({
								relation: openapp.ns.foaf + "member", 
								type: openapp.ns.foaf + "Person",
								referenceTo: users[idx].uri,
								callback: function() {
									spaceResource.refresh();
									updateParticipants(spaceResource);
								}
							});
						});
						$("#dialogNode").dialog("close");
					};
					dialogNode.dialog({
						"autoOpen": true,
						"buttons": {
							"Cancel": function() {
								$("#dialogNode").dialog("close");
							},
							"Add as participants": function() {
								addP();
							}
						}
					});
				});
			};

			$("#addParticipantNode").css("display", "").click(addParticipant);
		} else {
			$("#addParticipantNode").css("display", "none");
		}
	};
		
	/**
	 * Updates the list of participants in the controlpanel.
	 * @param {Object} spaceURI
	 */
	var updateParticipants = function(spaceResource) {
		var participantsNode = $("#participantsNode").html("");
		
		//Find subresources via the member relation and follow the references to the real user objects.
		spaceResource.getSubResources({
			relation: openapp.ns.foaf + "member",
			onEach: function(subResource) {
				//Get the reference.
				subResource.followReference(function(reference) {
					//Get the metadata for each participant in the properties format
					reference.getMetadata("properties", function(metadata) {
						var name = metadata[openapp.ns.dcterms + "title"];
						var shortenedName = name.length > 17 ? name.substr(0,15)+"…" : name;
						$("<div class='sideEntry'></div>")
							.append($("<span class='removeParticipant' title='remove participant'>&nbsp;-</span>").click(function() {
								if (confirm("Remove participant "+name+"?")) {
									subResource.del(function() {
										spaceResource.refresh();
										updateParticipants(spaceResource);
									});
								}
							}))
							.append("<span title='"+name+"'>"+shortenedName+"</span>")
							.appendTo(participantsNode);
					});
				});
			}});
	};
});