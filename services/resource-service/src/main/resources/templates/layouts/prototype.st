prototype(param, static, bundle, literal, include) ::= <<
<!doctype html>
<html>
	<head>
		<title>$param.title$</title>
		<link rel="shortcut icon" href="$static.logo.icon$" type="image/x-icon"/>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta http-equiv="X-UA-Compatible" content="IE=9"/>
		<style>
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
			.sideEntry { padding: 4px 10px 3px 10px; cursor: pointer; border-bottom: 1px solid #ccc; background: url($static.background.shade$); }
			.sideEntry:hover { background: url($static.background.highlight$); padding: 2px 10px 2px 10px; border-top: 2px solid #00cc55; border-bottom: 2px solid #00cc55;  }
			.sideItem { padding: 4px 10px 3px 10px; }
			
			h1, h2 { margin-bottom: 0px; }
			ul { padding: 10px; }
			ul li { padding: 0em; margin-left: 20px; }
		</style>
		<script src="$static.openapp$"></script>
		<script>
			addTool = function() {
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
			<div class="bar" style="margin-top: -32px; padding: 1px; padding-left: 10px;"><span class="barTitle">$param.title$</span></div>
		</div>
		<div class="section" style="position: relative; border-top: 2px solid #00cc55; background: #fff;">
		</div>
		<div style="padding: 2em;">
			$include.body$
		</div>
		<div class="section" style="position: fixed; left: 0px; bottom: 0px; height: 40px; width: 100%; background: #fff url($static.background.highlight$); border-top: 2px solid #00cc55;">
		</div>
		<div class="section" style="position: fixed; left: 0px; bottom: 0px; width: 100%;">
			<!--<div class="sideSection" style="cursor: pointer; font-style: italic; text-align: right;">&#xA0;</div>-->
			<div class="box" style="/*margin-left: 150px;*/">
				<div id="personal" class="hidden"><div style="display: table-row;">
					$if(param.user.name)$
					<div id="personalFirst" style="display: table-cell; font-style: italic; font-weight: bold; vertical-align: bottom; padding-bottom: 10px; padding-left: 20px;" onmousedown="document.getElementById('personal').className='hidden'; this.style.height='auto';">Personal:&#xA0;</div>
					$param.personal.src.keys:{tool | 
					<div class="toolCell" onmouseover="if(document.getElementById('personal').className=='hidden') this.style.height='10000px';" onmouseout="this.style.height='auto';" onmousedown="if(event.target===this) \{ document.getElementById('personal').className='hidden'; this.style.height='auto'; document.getElementById('personalFirst').style.height='auto'; \}"><div class="tool">
						<div class="title" onclick="document.getElementById('personal').className='';document.getElementById('personalFirst').style.height='10000px';/*document.getElementById('filler').style.height=this.parentElement.clientHeight+'px';*/">$param.personal.title.(tool)$</div>
						<iframe src="$param.personal.src.(tool)$" style="width: 100%; height: $param.personal.height.(tool)$px; border: 0px;"></iframe>
					</div></div>
					}$
					<div class="toolCell" style="display: table-cell; cursor: pointer; padding: 9px;" onclick="addTool();"><em><span style="color: #00cc55; font-weight: bold; font-size: 16px;">+</span> Add tool</em></div>
					$else$
					<div id="personalFirst" style="display: table-cell; font-style: italic; vertical-align: bottom; padding-bottom: 10px; padding-left: 20px;" onmousedown="document.getElementById('personal').className='hidden'; this.style.height='auto';"><a href="$param.request.baseuri$o/session/login?return=$param.request.uri$" style="color: #000;">Sign in</a> to see your personal tools here.</div>
					$endif$
				</div></div>
			</div>
		</div>
		<div style="width: 100%; height: 40px;"></div>
	</body>
</html>
>>