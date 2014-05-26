roleterms = {};

//A simple template showing a title and an creator in the form of a foaf:Person with a firstname and lastname.
//The person being shown in a table format.
roleterms.widgetTemplate = {
	"id": "http://purl.org/role/rforms/Widget",
	"root":{
		"type":"group",
		"content":[
			{
				"type":"text", "nodetype":"LANGUAGE_LITERAL", "property":"http://purl.org/dc/terms/title",
				"cardinality": {"min": 1, "max": 1},
				"label":{"en":"Title"}, "description":{"en":"A short title of the Widget"}
			},
			{
				"type":"text", "nodetype":"LANGUAGE_LITERAL", "property":"http://purl.org/dc/terms/description",
				"cardinality": {"min": 0, "pref": 1, "max": 1}, "cls": ["rformsmultiline"],
				"label":{"en":"Description"}, "description":{"en":"A longer descriptive text of widget."}
			},
			{
				"type":"text", "nodetype":"URI", "property":"http://purl.org/dc/terms/source",
				"cardinality": {"min": 1, "max": 1},
				"label":{"en":"Widget source"}, "description":{"en":"Must be a url pointing to a digital representation of the widget, for example a OpenSocial Gadget XML file."}
			},
			{
				"type":"text", "nodetype":"URI", "property":"http://xmlns.com/foaf/0.1/img",
				"cardinality": {"min": 0, "max": 1},
				"label":{"en":"Screenshoot"}, "description":{"en":"Screenshoot of the widget, must be a url pointing to an image file."}
			},
			{
				"type":"text", "nodetype":"URI", "property":"http://xmlns.com/foaf/0.1/depiction",
				"cardinality": {"min": 0, "max": 1},
				"label":{"en":"Thumbnail"}, "description":{"en":"Thumbnail of the widget, must be a url pointing to an image file."}
			},
			{
				"type":"choice", "nodetype":"URI", "property":"http://purl.org/role/terms/hasCategory",
				"cardinality": {"min": 1},
				"label":{"en":"Tool category"}, "description":{"en":"Every tool, and hence widget, must belong to a Tool category."},
				"choices": [
							{"value": "PlanAndOrganize","label": {"en":"Plan &amp; Organize"},
							 "description": {"en":""}},
							{"value": "http://purl.org/role/instances/ToolCategory#SearchAndGetRecommendation", "label": {"en":"Search &amp; Get Recommendation"},
				             "description": {"en":""}},
						    {"value": "http://purl.org/role/instances/ToolCategory#CollaborateAndCommunicate","label": {"en":"Collaborate &amp; Communicate"},
							 "description": {"en":""}},
							{"value": "http://purl.org/role/instances/ToolCategory#ExploreAndView","label": {"en":"Explore &amp; View"},
				             "description": {"en":""}},
					        {"value": "http://purl.org/role/instances/ToolCategory#TrainAndTest","label": {"en":"Train &amp; Test"},
							 "description": {"en":""}},
							{"value": "http://purl.org/role/instances/ToolCategory#CreateAndManipulate","label": {"en":"Create &amp; Manipulate"},
					         "description": {"en":""}},
					        {"value": "http://purl.org/role/instances/ToolCategory#ReflectAndEvaluate","label": {"en":"Reflect &amp; Evaluate"},
					         "description": {"en":""}},
							{"value": "http://purl.org/role/instances/ToolCategory#Demo","label": {"en":"Demo Widgets"},
							 "description": {"en":""}},
							{"value": "http://purl.org/role/instances/ToolCategory#Develop","label": {"en":"Developer tools"},
							 "description": {"en":""}}
				]
	        },
			{
				"type":"text", "nodetype":"URI", "property":"http://purl.org/dc/terms/subject",
				"cardinality": {"min": 0},
				"label":{"en":"Topic"}, "description":{"en":"A topic that this widget works with, use dpbedia urls."}
			},
			{
				"type":"text", "nodetype":"URI", "property":"http://purl.org/role/terms/hasFunctionality",
				"cardinality": {"min": 0, "pref": 1},
				"label":{"en":"Functionality"}, "description":{"en":"A functionality that this tool has, e.g. Search or Visualisation."}
			},
			{
				"type":"group", "nodetype":"RESOURCE", "property":"http://xmlns.com/foaf/0.1/maker",
				"cardinality": {"min": 0, "pref": 1},
				"constraints":{"http://www.w3.org/TR/rdf-schema/type":"http://xmlns.com/foaf/0.1/Person"},
				"label":{"en":"Maker"},
				"content":[
					{
						"type":"text", "nodetype":"ONLY_LITERAL", "property":"http://xmlns.com/foaf/0.1/name",
						"cardinality": {"min": 1, "max": 1},
						"label":{"en":"Name"}
					},{
						"type":"text", "nodetype":"URI", "property":"http://xmlns.com/foaf/0.1/mbox",
						"cardinality": {"min": 0},
						"label":{"en":"Email"}
					}
				]
			}
		]
	}
};

//Simple rdfjson data, all triples are matched into the template.
roleterms.widgetRDF = {
	"http://www.role-widgetstore.eu/content/xmpp-multiuser-chat" : {
		"http://www.w3.org/TR/rdf-schema/type"	  : [ { "value" : "http://purl.org/role/terms/OpenSocialGadget", "type" : "uri"}],
        "http://purl.org/dc/terms/title"   : [ { "value" : "XMPP Multiuser Chat", "type" : "literal", "lang" : "en" } ] ,
        "http://purl.org/dc/terms/description"   : [ { "value" : "A chat tool that can be used for...", "type" : "literal", "lang" : "en" } ] ,
        "http://purl.org/dc/terms/source" : [ { "value" : "http://example.com/widget.xml", "type" : "uri" } ],
        "http://xmlns.com/foaf/0.1/maker" : [ { "value" : "_:maker", "type" : "bnode" } ],
        "http://xmlns.com/foaf/0.1/img" : [ { "value" : "http://example.com/image", "type" : "uri" } ],
        "http://xmlns.com/foaf/0.1/depiction" : [ { "value" : "http://example.com/thumb", "type" : "uri" } ],
        "http://purl.org/role/terms/hasCategory" : [ { "value" : "http://purl.org/role/instances/ToolCategory#CollaborateAndCommunicate", "type" : "uri" } ],
        "http://purl.org/role/terms/hasFunctionality" : [ { "value" : "http://purl.org/role/instances/Functionality#1", "type" : "uri" } ],
    } ,
    "_:maker" : {
		"http://www.w3.org/TR/rdf-schema/type"	  : [ { "value" : "http://xmlns.com/foaf/0.1/Person", "type" : "uri"}],
        "http://xmlns.com/foaf/0.1/name"     : [ { "value" : "Dominik Renzel", "type" : "literal" } ] ,
        "http://xmlns.com/foaf/0.1/mbox"       : [ { "value" : "dominik@example.com", "type" : "uri" } ]
    } ,
	"http://www.role-widgetstore.eu/content/dummy" : {
		"http://www.w3.org/TR/rdf-schema/type"	  : [ { "value" : "http://purl.org/role/terms/OpenSocialGadget", "type" : "uri"}],
    }
};
