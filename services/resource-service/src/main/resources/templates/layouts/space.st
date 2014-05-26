space(param, static, bundle, literal, include) ::= <<
<!doctype html>
<html>
	<head>
		<title>Learning Space: $param.space.title$</title>
		<link rel="shortcut icon" href="$static.logo.icon$" type="image/x-icon"/>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta http-equiv="X-UA-Compatible" content="IE=9"/>
		<style>
			html { margin: 0px; padding: 0px; }
			body { background: #fff; color: #000; background: url($static.background.shade$); margin: 0px; padding: 0px; }
			.title {  }
			iframe { display: block; }

			body { margin: 0px; font-family: sans-serif; font-size: 13px; /*padding-left: 150px;*/ }
			#top { height: 40px; padding: 5px; }
			#logo { float: right; }
			.bar { margin-top: 50px; }
			.barTitle { font-size: 20px; font-weight: bold; font-style: italic; }
			.box { overflow: auto; }
			.tool, .toolFocus { width: 300px; float: left; border: 1px solid #ccc; margin: 5px; background: #fff; }
			.title { cursor: pointer; height: 20px; font-weight: bold; text-align: center; background: #eeffdd; border-top: 1px solid #ccc; border-bottom: 2px solid #ccc; margin-bottom: 1px; background: url($static.background.shade$); padding-left: 5px; padding-right: 5px; padding-top: 2px; padding-bottom: 0px; }
			.sideSection { position: absolute; top: 0x; left: 0px; width: 150px; }
			.hidden .tool { width: auto; }
			.hidden iframe { display: none; }

			.tool:hover, .toolCell:hover .tool, .toolFocus { padding: 0px; border: 1px solid #00cc55; }
			.tool:hover .title, .toolCell:hover .title, .toolFocus .title { border-top: 1px solid #00cc55; border-bottom: 2px solid #00cc55; background: url($static.background.highlight$); }

			#personal .tool { float: none; vertical-align: bottom; margin-bottom: 9px; }
			#personal .toolCell { height: 20px; bottom: 0px; display: table-cell; vertical-align: bottom; }
			#personal { /*width: 912px;*/ }
			.hidden { width: auto !important; }

			.hidden .title { border-left: 1px solid #ccc; border-right: 1px solid #ccc; border-bottom: 2px solid #ccc; }
			.hidden .tool { border-left: 0px; border-right: 0px; border-bottom: 0px; }

			.toolCell:hover .tool { width: 300px; border-left: 1px solid #00cc55; border-right: 1px solid #00cc55; border-bottom: 1px solid #00cc55; }
			.toolCell:hover .title { border-left: 0px; border-right: 0px; }
			.toolCell:hover iframe { display: block; }

			/*.sideSection { display: none !important; }*/

			.sideHeader { padding: 10px 10px 4px 10px; font-weight: bold; border-bottom: 1px solid #ccc; }
			.sideEntry, .sideEntrySel { padding: 4px 10px 3px 10px; cursor: pointer; border-bottom: 1px solid #ccc; background: url($static.background.shade$); }
			.sideEntry:hover, .sideEntrySel { background: url($static.background.highlight$); padding: 2px 10px 2px 10px; border-top: 2px solid #00cc55; border-bottom: 2px solid #00cc55;  }
			.sideEntrySel { font-weight: bold; }
			.sideItem { padding: 4px 10px 3px 10px; }
		</style>
		<script src="$static.openapp$"></script>
		<script>
			addPersonalTool = function() {
				var gadgetUrl = prompt("Currently you may only add OpenSocial gadgets, by entering the URL here.", "http://");
				if (gadgetUrl !== null) {
					openapp.resource.get("$param.user.uri$", function(user){
						openapp.resource.context(user).sub(openapp.ns.role + "tool").type(openapp.ns.role + "OpenSocialGadget").seeAlso(gadgetUrl).create(function(context){
							openapp.resource.context(context).metadata().graph().literal(openapp.ns.dcterms + "title", "Gadget").put(function() {
								alert("Added!");
								window.location.reload();
							});
						});
					});
				}
			};
			addSpaceTool = function() {
				var gadgetUrl = prompt("Currently you may only add OpenSocial gadgets, by entering the URL here.", "http://");
				if (gadgetUrl !== null) {
					openapp.resource.get("$param.context.uri$", function(space){
						openapp.resource.context(space).sub(openapp.ns.role + "tool").type(openapp.ns.role + "OpenSocialGadget").seeAlso(gadgetUrl).create(function(context){
							openapp.resource.context(context).metadata().graph().literal(openapp.ns.dcterms + "title", "Gadget").put(function() {
								alert("Added!");
								window.location.reload();
							});
						});
					});
				}
			};
			deleteTool = function(uri) {
				openapp.resource.del(uri, function(response){
					alert("Deleted!");
					window.location.reload();
				});
			};
	</script>
	</head>
	<body>
		<div class="sideSection"></div>
		<div id="top" style="background: url($static.background.highlight$);"><img id="logo" src="$static.logo.small$" width="100" height="40" alt="ROLE"/></div>
		<div class="section">
			<div class="sideSection"></div>
			<div style="float: right; padding-right: 15px;">
				$if(param.user.name)$
				<strong>$param.user.name$</strong> | <a href="$param.request.baseuri$o/session/logout?return=$param.request.uri$" style="color: #000;">Sign out</a>
				$else$
				<a href="$param.request.baseuri$o/session/login?return=$param.request.uri$" style="color: #000;">Sign in</a>
				$endif$
			</div>
			<div class="bar" style="margin-top: -32px; padding: 1px; padding-left: 10px;"><span class="barTitle">$param.space.title$</span> <a href="http://fusion.google.com/add?source=atgs&moduleurl=$param.context.uri$/role:widget"><img src="http://gmodules.com/ig/images/plus_google.gif" border="0" alt="Add to Google"></a><!-- <span style="font-size: 10px;">▼</span>--></div>
		</div>
		<div id="rowBox" class="section" style="position: relative; border-top: 2px solid #00cc55; border-bottom: 2px solid #00cc55; background: #fff;">
			<div class="sideSection"><div style="overflow: auto; height: $param.tools.height$px;">
				<div class="sideHeader">Widgets</div>$param.tool.src.keys:{tool|
					<div class="sideEntry" onmouseover="document.getElementById('tool_$tool$').className='toolFocus';" onmouseout="document.getElementById('tool_$tool$').className='tool';">$param.tool.title.(tool)$</div>
				}$$if(param.user.name)$<div class="sideItem" style="cursor: pointer;" onclick="addSpaceTool();"><em><span style="color: #00cc55; font-weight: bold; font-size: 16px;">+</span> Add widget</em></div>$endif$
			</div></div>
			<div class="box" style="margin-left: 150px; border-left: 2px solid #ccc;">
				<div style="width: $param.tools.width$px/*3120px*/;">
					<!--<div class="tool" style="width: 149px; position: absolute; left: -150px; height: 100%; margin: 0px; padding: 0px;">
						<div class="title">Controls</div>
						<div><ul><li>Language Browser</li><li>Translator</li><li>Vocabulary Trainer</li><li>RSS</li></ul></div>
					</div>-->
					$param.tool.src.keys:{tool|
					<div class="tool" id="tool_$tool$">
						<div class="title">$param.tool.title.(tool)$$if(param.user.name)$<span style="float: right; font-size: 10px; padding-top: 3px; cursor: pointer;" onclick="deleteTool('$param.tool.uri.(tool)$');">X</span>$endif$</div>
						<iframe src="$param.tool.src.(tool)$" style="width: 100%; height: $param.tool.height.(tool)$px; border: 0px;"></iframe>
					</div>
					}$
					<!--<div class="tool" style="width: 100px; cursor: pointer; position: relative; top: 200px; border: 0px;"
							  onclick="alert('TODO: Show the Widget Store.');">
						<div style="font-size: 100px; text-align: center; color: #00cc55; font-weight: bold;">+</div>
						<div style="font-style: italic; text-align: center; margin-top: -2em;">Add a new tool.</div>
					</div>-->
				</div>
			</div>
		</div>
		<div class="section" style="position: fixed; left: 0px; bottom: 0px; height: 40px; width: 100%; background: #fff url($static.background.highlight$); border-top: 2px solid #00cc55;">
		</div>
		<div class="section" style="position: fixed; left: 0px; bottom: 0px; width: 100%; z-index: 9999;">
			<!--<div class="sideSection" style="cursor: pointer; font-style: italic; text-align: right;">&#xA0;</div>-->
			<div id="dashboard" class="box" style="/*margin-left: 150px;*/">
				<div id="personal" class="hidden"><div style="display: table-row;">
					$if(param.user.name)$
					<div id="personalFirst" style="display: table-cell; font-style: italic; font-weight: bold; vertical-align: bottom; padding-bottom: 10px; padding-left: 20px;" onmousedown="document.getElementById('personal').className='hidden'; this.style.height='auto';">Personal:&#xA0;</div>
					$param.personal.src.keys:{tool|
					<div class="toolCell" onmouseover="if(document.getElementById('personal').className=='hidden') this.style.height='10000px';" onmouseout="this.style.height='auto';" onmousedown="if(event.target===this) { document.getElementById('personal').className='hidden'; this.style.height='auto'; document.getElementById('personalFirst').style.height='auto'; }"><div class="tool">
						<div class="title" onclick="document.getElementById('personal').className='';document.getElementById('personalFirst').style.height='10000px';/*document.getElementById('filler').style.height=this.parentElement.clientHeight+'px';*/">$param.personal.title.(tool)$</div>
						<iframe src="$param.personal.src.(tool)$" style="width: 100%; height: $param.personal.height.(tool)$px; border: 0px;"></iframe>
					</div></div>
					}$
					<div class="toolCell" style="display: table-cell; cursor: pointer; padding: 9px;" onclick="addPersonalTool();"><em><span style="color: #00cc55; font-weight: bold; font-size: 16px;">+</span> Add widget</em></div>
					$else$
					<div id="personalFirst" style="display: table-cell; font-style: italic; vertical-align: bottom; padding-bottom: 10px; padding-left: 20px;" onmousedown="document.getElementById('personal').className='hidden'; this.style.height='auto';"><a href="$param.request.baseuri$o/session/login?return=$param.request.uri$" style="color: #000;">Sign in</a> to see your personal tools here.</div>
					$endif$
				</div></div>
			</div>
		</div>
		<div style="width: 100%; height: 40px;"></div>
	</body>
</html>
