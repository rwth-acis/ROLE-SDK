widget(param, static, bundle, literal, include) ::= <<
<?xml version="1.0" encoding="UTF-8"?>
<Module>
  <ModulePrefs title="Learning Space: $param.space.title$" author_email="erikis@kth.se" description="A Learning Space as a Gadget." height="500">
    <Require feature="opensocial-0.9" />
    <OAuth>
      <Service name="openapp">
		<Request url="$param.base.uri$o/oauth/initiate?scope=user_resource%20space_resource" method="GET" />
		<Authorization url="$param.base.uri$o/oauth/authorize" />
		<Access url="$param.base.uri$o/oauth/token" method="GET" />
      </Service>
    </OAuth>
  </ModulePrefs>
  <Content type="html" view="default,home,profile"><![CDATA[

<div id="space">

		<div id="top" style="background: url($static.background.highlight$);"><img id="logo" src="$static.logo.small$" width="100" height="40" alt="ROLE"/></div>
		<div class="section">
			<div class="sideSection"></div>
			<div style="float: right; padding-right: 15px;"><strong><!--Space--></strong></div>
			<div class="bar" style="margin-top: -27px; padding: 1px; padding-left: 10px;"><span class="barTitle">&nbsp;</span> <span style="font-size: 10px;"><!--▼--></span></div>
		</div>
		<div class="sideSection"><div style="overflow: auto;">
			<div class="sideHeader">Tools</div><div id="tools"></div><div class="sideItem" style="cursor: pointer;" onclick="addTool()"><em><span style="color: #00cc55; font-weight: bold; font-size: 16px;">+</span> Add tool</em></div><div class="sideHeader">Participants</div><div id="participants"></div><!--<em><span style="color: #00cc55; font-weight: bold; font-size: 16px;">+</span> Add participant</em></div>--><div class="sideHeader">Resources</div><div id="resources"></div></div>
		</div>
		<div class="section" style="position: relative; border-top: 2px solid #00cc55; border-bottom: 0px solid #00cc55; background: #150pxfff;"> 
		  </div>
	
</div>

<script type="text/javascript" src="http://www.google.com/jsapi?key=ABQIAAAArYowhPTyKQn7tayyo_1sixT2yXp_ZAY8_ufC3CFXhHIE1NvwkxS_b5dYAqSj_axhktZy0hCXbg1lLA"></script>
<script type="text/javascript" src="$static.openapp$"></script>

<script type="text/javascript">

google.load("jquery", "1.4.2");

var isOwner = false;
var moduleId = 0;

init = function() {
	\$("#tools").attr("id", openapp.ns.role + "tool");
	\$("#participants").attr("id", openapp.ns.foaf + "member");
	\$("#resources").attr("id", openapp.ns.role + "data");
	openapp.resource.get("$param.context.uri$", function(context) {
		space = context;
		
		openapp.resource.context(context).metadata().get(function(content){
			var metadata = openapp.resource.context(content).properties();
			\$(".barTitle").text(metadata[openapp.ns.dcterms + "title"]);
		});

		var rels = openapp.resource.context(context).properties();
		for (rel in rels) { if (rels.hasOwnProperty(rel)) {
			var section = document.getElementById(rel);
			if (typeof section === "undefined" || section === null) {
				continue;
			}
			var resources = openapp.resource.context(context).sub(rel).list();
			for (var i = 0; i < resources.length; i++) {
				var resource = resources[i];
				var control = openapp.resource.context(resource).properties();
				var seeAlso = control[openapp.ns.rdfs + "seeAlso"];
				var text = typeof seeAlso !== "undefined" ? seeAlso : resource.uri;
				\$("<div/>").attr("id", resource.uri).addClass("sideEntry").text(text).appendTo(\$(section));
				var closure=function(resource){
					openapp.resource.context(resource).followSeeAlso().metadata().get(function(content){
						var metadata = openapp.resource.context(content).properties();
						\$(document.getElementById(resource.uri)).text(metadata[openapp.ns.dcterms + "title"]);
					});
				};closure(resource);
			}		
		}}

		var exampleData = openapp.resource.context(context).sub(openapp.ns.role + "data").type("http://example.com/rdf/ExampleData").list();
		//alert("Number of example data found: " + exampleData.length);

		if (exampleData.length < 3) {
			openapp.resource.context(context).sub(openapp.ns.role + "data").type("http://example.com/rdf/ExampleData").create(function(context){
				openapp.resource.context(context).metadata().graph().literal(openapp.ns.dcterms + "title", "My Example Data #" + (exampleData.length + 1)).put(function(content) {});
				openapp.resource.context(context).representation().mediaType("text/html").string("<html><head><title>Example Data</title></head><body><h1>Hello World!</h1></body></html>").put(function(content) {
				
					/*openapp.resource.context(context).representation().get(function(content){
						alert(content.uri + "\n" + content.contentUri + "\n\n" + openapp.resource.context(content).string());
					});*/
					
				});
			});
		}
	});
};

addTool = function() {
	var gadgetUrl = prompt("Currently you may only add OpenSocial gadgets, by entering the URL here.", "http://");
	if (gadgetUrl !== null) {
		openapp.resource.context(space).sub(openapp.ns.role + "tool").type(openapp.ns.role + "OpenSocialGadget").seeAlso(gadgetUrl).create(function(context){
			openapp.resource.context(context).metadata().graph().literal(openapp.ns.dcterms + "title", "Gadget").put(function() {
				alert("Added!");
			});
		});
	}
};

</script>

<style>
@CHARSET "UTF-8";

html { margin: 0px; padding: 0px; }
body { background: #fff; color: #000; margin: 0px; padding: 0px; }
.title {  }
iframe { display: block; }

body { margin: 0px; font-family: sans-serif; font-size: 13px; /*padding-left: 150px;*/ }
#top { height: 40px; padding: 5px; }
#logo { float: right; }
.bar { margin-top: 50px; }
.barTitle { font-size: 20px; font-weight: bold; font-style: italic; }
.box { overflow: auto; }
.gadget { width: 300px; float: left; border: 1px solid #ccc; margin: 5px; background: #fff; }
.title { cursor: pointer; height: 20px; font-weight: bold; text-align: center; background: #eeffdd; border-top: 1px solid #ccc; border-bottom: 2px solid #ccc; margin-bottom: 1px; background: url($static.background.shade$); padding-left: 5px; padding-right: 5px; padding-top: 2px; padding-bottom: 0px; }
.sideSection { position: absolute; top: 0x; left: 0px; width: 100%; }
.hidden .gadget { width: auto; }
.hidden iframe { display: none; }

.gadget:hover, .gadgetCell:hover .gadget { padding: 0px; border: 1px solid #00cc55; }
.gadget:hover .title, .gadgetCell:hover .title { border-top: 1px solid #00cc55; border-bottom: 2px solid #00cc55; background: url($static.background.highlight$); }

#personal .gadget { float: none; vertical-align: bottom; margin-bottom: 9px; }
#personal .gadgetCell { height: 20px; bottom: 0px; display: table-cell; vertical-align: bottom; }
#personal { /*width: 912px;*/ }
.hidden { width: auto !important; }

.hidden .title { border-left: 1px solid #ccc; border-right: 1px solid #ccc; border-bottom: 2px solid #ccc; }
.hidden .gadget { border-left: 0px; border-right: 0px; border-bottom: 0px; }

.gadgetCell:hover .gadget { width: 300px; border-left: 1px solid #00cc55; border-right: 1px solid #00cc55; border-bottom: 1px solid #00cc55; }
.gadgetCell:hover .title { border-left: 0px; border-right: 0px; }
.gadgetCell:hover iframe { display: block; }

/*.sideSection { display: none !important; }*/

.sideHeader { padding: 10px 10px 4px 10px; font-weight: bold; border-bottom: 1px solid #ccc; }
.sideEntry { padding: 4px 10px 3px 10px; cursor: pointer; border-bottom: 1px solid #ccc; background: url($static.background.shade$); }
.sideEntry:hover { background: url($static.background.highlight$); padding: 2px 10px 2px 10px; border-top: 2px solid #00cc55; border-bottom: 2px solid #00cc55;  }
.sideItem { padding: 4px 10px 3px 10px; }

</style>

<script type="text/javascript">
gadgets.util.registerOnLoadHandler(init);
</script>

  ]]></Content>
  <Content type="url" view="canvas" href="$param.context.uri$/role:ple"></Content>
</Module>
>>