define(["jquery"], function($) {
	$(document).bind("plespace", function(event) {
		updateWidgets(event.spaceResource.getURI(), event.widgets);
		updateMemberOfSpace(event.spaceResource, event.memberOfSpace);
	});
	var updateMemberOfSpace = function(spaceResource, memberOfSpace) {
		if (memberOfSpace) {
			var addWidget = function() {
				var gadgetUrl = prompt("Currently you may only add OpenSocial gadgets, by entering the URL here.", "http://");
				if (gadgetUrl !== null) {
					var metadata = {};metadata[openapp.ns.dcterms + "title"] = "Gadget";
					spaceResource.create({
						relation: openapp.ns.role + "tool", 
						type: openapp.ns.role + "OpenSocialGadget",
						metadata: metadata,
						referenceTo: gadgetUrl,
						callback: function() {
							alert("Added!");
							updateWidgetsFor(spaceResource.getURI());
						}
					});
				}
			};
			$("#addWidgetNode").css("display", "").click(addWidget);
		} else {
			$("#addWidgetNode").css("display", "none");
		}
	};
	
	var updateWidgetsFor = function(spaceURI) {
		$.getJSON(spaceURI+"/role:plespace",function(data) {
			updateWidgets(spaceURI, data.tools);
		});
	};

		/**
	 * Updates the list of widgets in the controlpanel.
	 * @param {Object} tools
	 */
	var updateWidgets = function(spaceURI, tools) {
		
		//Improve when updating so that already loaded tools does not refresh.
		var widgetsListNode   = $("#widgetsListNode").html(""), 
			widgetsNode = $("#widgetsNode");

		widgetsNode.children(".widget").remove();

		$.each(tools, function(toolId, tool) {
			var controlNode = $("<div class='sideEntry'></div>").appendTo(widgetsListNode);
			var toolWrapper = $("<div class='toolWrapper widget' style='height:"+(tool.height+30)+"px'>"
					+"<div class='padder'><div class='tool'>"
						+"<div class='title'>"+(tool.title)+"</div>"
						+"<div class='iframecontainer'><iframe style='width: 100%; height: 100%;border: 0px;' src='"+tool.src+"'></iframe></div>"
					+"</div></div>"
				+"</div>");
			var toolNode = toolWrapper.find(".tool");
			toolNode.mouseover(function() {controlNode.addClass("sideEntrySel");});
			toolNode.mouseout(function() {controlNode.removeClass("sideEntrySel");});
			toolWrapper.appendTo(widgetsNode);
						
			shortenedTitle = tool.title.length > 14 ? tool.title.substr(0,12)+"…" : tool.title;
			toolNode.find(".title").click(function() {
				toolWrapper.toggleClass("maximized");
				widgetsNode.toggleClass("maximize");
			});
			$("<div></div>").mouseover(function() {toolNode.addClass("toolFocus");})
						.mouseout(function() {toolNode.removeClass("toolFocus");})
						.append($("<input type='checkbox' checked='checked'></div>").change(function() {toolWrapper.toggle();}))
						.append($("<span class='removeWidget' title='remove widget'>&nbsp;-</span>").click(function() {
							if (confirm("Remove widget "+tool.title+"?")) {
								var res = new openapp.oo.Resource(tool.uri);
								res.del(function() {
									updateWidgetsFor(spaceURI);
								});
							}
						}))
						.append("<span title='"+tool.title+"'>"+shortenedTitle+"</span>")
						.appendTo(controlNode);
		});
	};
});