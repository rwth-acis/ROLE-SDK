define(["jquery", "jqueryui/dialog"], function($) {
	$(document).bind("plespace", function(event) {
		//event.spaceResource event.userURI
		updateResources(event.spaceResource);
		updateMemberOfSpace(event.spaceResource, event.memberOfSpace);
	});
	var updateMemberOfSpace = function(spaceResource, memberOfSpace) {
		if (memberOfSpace) {
			$("#addResourceNode").css("display", "").click(function() {
				addResource(spaceResource);
			});
		} else {
			$("#addResourceNode").css("display", "none");
		}
	};
	
	/**
	 * Updates the list of resources in the controlpanel.
	 * @param {Object} spaceURI
	 */
	var updateResources = function(spaceResource) {
		var resourcesNode = $("#resourcesNode").html("");
	
		//Find subresources via the member relation and follow the references to the real user objects.
		spaceResource.getSubResources({
			relation: openapp.ns.role + "data",
			onEach: function(subResource) {
				
				//Get the metadata for each resource in the properties format
				subResource.getMetadata("properties", function(metadata) {
					var label = metadata[openapp.ns.dcterms + "title"];
					var shortenedLabel = label.length > 17 ? label.substr(0,15)+"…" : label;
					$("<div class='sideEntry'></div>").click(function(event) {
						subResource.getReference(function (refURI) {
							var md = {};
							md[openapp.ns.dcterms + "title"] = label;
							if (refURI != null) {
								md[openapp.ns.rdfs + "seeAlso"] = refURI;
							}
							gadgets.openapp.publish({
									"uri": refURI,
									"entry": subResource.getURI(), 
									"type": "namespaced-properties"
								},md);
						});
					})
					.append($("<span class='removeResource' title='remove resource'>&nbsp;-</span>").click(function(event) {
						if (confirm("Remove resource "+label+"?")) {
							subResource.del(function() {
								spaceResource.refresh();
								updateResources(spaceResource);
							});
						}
						event.stopPropagation();
					}))
					.append("<span title='"+label+"'>"+shortenedLabel+"</span>")
					.appendTo(resourcesNode);
				});
			}});
	};
	var addResource = function(spaceResource) {
		var dialogNode = $("#dialogNode").html("");
		var formNode = $("<div></div>").appendTo(dialogNode);
		$("<label>Label:</label>").appendTo(formNode);
		var labelInput = $("<input type='text'></input>").appendTo(formNode);
		$("<label>Web address:</label>").appendTo(formNode);
		var addrInput = $("<input type='text'></input>").appendTo(formNode);
		var create = function() {
			var metadata = {};
			metadata[openapp.ns.dcterms + "title"]= labelInput.val();
			var refURI = addrInput.val();
			spaceResource.create({
				relation: openapp.ns.role + "data", 
				type: "http://example.com/rdf/ExampleData",
				referenceTo:  refURI != "" ? refURI : null,
				metadata: metadata,
				format: "properties",
				representation: "<html><head><title>Example Data</title></head><body><h1>Hello World!</h1></body></html>",
				medieType: "text/html",
				callback: function() {
						spaceResource.refresh();
						updateResources(spaceResource);
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
				"Add resource" : function() {
					create();
				}
			}
		});
	};
});