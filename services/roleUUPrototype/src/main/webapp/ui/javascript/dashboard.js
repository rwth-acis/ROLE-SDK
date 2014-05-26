define(["jquery"], function($) {
	$(document).bind("plespace", function(event) {
		//event.spaceResource event.userURI
		updateDashboardWidgets(event.dashboardWidgets);
		updateSignedIn(event.spaceResource, event.userName != null, event.userURI);
	});
	var updateSignedIn = function(spaceResource, signedIn, userURI) {
		if (signedIn) {
			var addPersonalWidget = function() {
				var gadgetUrl = prompt("Currently you may only add OpenSocial gadgets, by entering the URL here.", "http://");
				if (gadgetUrl !== null) {
					openapp.resource.get(userURI, function(user){
						openapp.resource.context(user).sub(openapp.ns.role + "tool").type(openapp.ns.role + "OpenSocialGadget").seeAlso(gadgetUrl).create(function(context){
							openapp.resource.context(context).metadata().graph().literal(openapp.ns.dcterms + "title", "Gadget").put(function() {
								alert("Added!");
								window.location.reload();
							});
						});
					});
				}
			};
			$("#addDashboardWidgetNode").css("display", "inline").click(addPersonalWidget);
		} else {
			$("#addDashboardWidgetNode").css("display", "none");
		}
	};
	
	/**
	 * Updates the list of widgets in the dashboard.
	 * @param {Object} tools
	 */
	var updateDashboardWidgets = function(tools) {
		//Improve when updating so that already loaded tools does not refresh.
		var dashboardWidgetsNode   = $("#dashboardWidgetsNode").html("");
		var zindex = 10;
		var focusToolNode = function(toolNode) {
			toolNode.css("z-index", zindex++);
		}
		$.each(tools, function(toolId, tool) {
			var controlNode = $("<div class='toolCell'></div>").mouseover(function() {focusToolNode(toolNode);})
						.click(function() {toolNode.toggle();})
						.append("<div class='title'>"+tool.title+"</div>")
						.appendTo(dashboardWidgetsNode);
			var toolNode = $("<div class='tool' style='display:none;height:"+(tool.height+20)+"px'></div>")
					.mouseover(function() {focusToolNode(toolNode);})
					.append("<div class='title'>"+tool.title+"</div>")
					.append("<div class='iframecontainer'><iframe style='width: 100%; height: 100%; border: 0px;' src='"+tool.src+"'></iframe></div>")
					.appendTo(controlNode);
		});
	};
});