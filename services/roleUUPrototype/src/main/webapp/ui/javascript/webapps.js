define(["jquery"], function($) {
	$(document).bind("plespace", function(event) {
		updateWebApps(event.spaceResource);
		updateMemberOfSpace(event.spaceResource, event.memberOfSpace);
	});
	var updateMemberOfSpace = function(spaceResource, memberOfSpace) {
		if (memberOfSpace) {
			$("#addWebAppNode").css("display", "").click(function() {
					addWebApp(spaceResource);
				});
		} else {
			$("#addWebAppNode").css("display", "none");
		}
	};
	
	var addWebApp = function(spaceResource) {
		var dialogNode = $("#dialogNode").html("");
		var formNode = $("<div></div>").appendTo(dialogNode);
		$("<label>Label:</label>").appendTo(formNode);
		var labelInput = $("<input type='text'></input>").appendTo(formNode);
		$("<label>Web address:</label>").appendTo(formNode);
		var addrInput = $("<input type='text'></input>").appendTo(formNode);
		var create = function() {
			var metadata = {};
			metadata[openapp.ns.dcterms + "title"] = labelInput.val();
			spaceResource.create({
				relation: openapp.ns.role + "tool", 
				type: openapp.ns.role + "WebApp",
				metadata: metadata,
				referenceTo: addrInput.val(),
				callback: function() {
					spaceResource.refresh();
					updateWebApps(spaceResource);
				}
			});
			dialogNode.dialog("close");
		};

		dialogNode.dialog({
			"autoOpen": true,
			"buttons": {
				"Cancel": function() {
					dialogNode.dialog("close");
				},
				"Add app" : function() {
					create();
				}
			}
		});
	};
	
	/**
	 * Updates the list of widgets in the controlpanel.
	 * @param {Object} tools
	 */
	var updateWebApps = function(spaceResource) {
		var webAppNode   = $("#webAppNode").html(""), 
			widgetsNode = $("#widgetsNode");
		widgetsNode.children(".webapp").remove();

		//Find subresources via the member relation and follow the references to the real user objects.
		spaceResource.getSubResources({
			relation: openapp.ns.role + "tool",
			type: openapp.ns.role + "WebApp",
			onEach: function(subResource) {
				subResource.getMetadata(null, function(metadata) {
					subResource.getReference(function(referenceURI) { 
						var label = metadata[openapp.ns.dcterms + "title"];
						var shortenedLabel = label.length > 14 ? label.substr(0,12)+"…" : label;
						var controlNode = $("<div class='sideEntry'></div>");
						var toolWrapper = $(
						"<div class='toolWrapper webapp' style='height: 600px'>"
							+"<div class='padder'><div class='tool'>"
								+"<div class='title'>"+label+"</div>"
								+"<div class='iframecontainer'><iframe style='width: 100%; height: 100%;border: 0px;' src='"+referenceURI+"'></iframe></div>"
							+"</div></div>"
						+"</div>");

						var toolNode = toolWrapper.find(".tool");
						toolNode.find(".title").click(function() {
							toolWrapper.toggleClass("maximized");
							widgetsNode.toggleClass("maximize");
						});
						toolNode.mouseover(function() {controlNode.addClass("sideEntrySel");});
						toolNode.mouseout(function() {controlNode.removeClass("sideEntrySel");});
						toolWrapper.appendTo(widgetsNode);
						
						controlNode.mouseover(function() {toolNode.addClass("toolFocus");})
									.mouseout(function() {toolNode.removeClass("toolFocus");})
									.append($("<input type='checkbox' checked='checked'></div>").change(function() {toolNode.toggle();}))
									.append("<span></span>")
									.append($("<span class='removeWebapp' title='remove app'>&nbsp;-</span>").click(function() {
										if (confirm("Remove app "+label+"?")) {
											subResource.del(function() {
												spaceResource.refresh();
												updateWebApps(spaceResource);
											});
										}
									}))
									.append("<span title='"+label+"'>"+shortenedLabel+"</span>")
									.appendTo(webAppNode);
					});
				});
		}});
	};
});