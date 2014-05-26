// JavaScript Document

		var selectedElement = null;
		var outcomeIds = new Array();
		var widgetIds = new Array();
		var peerIds = new Array();
		
		var allOutcomes = new Array();
		var allOutcomesNames = new Array();
		
		var allWidgets	= new Array();
		var widgetIds	= new Array();
		
		var allPeers	= new Array();
		var peerIds		= new Array();
		
		var tools;
		var peers = 0;
		
		var outcomes = 0;
		var widgets = 0;
		var users = 0;
		
		var absPath = "http://augur.wu.ac.at/patternshare/";
		
		var sessionId		= "";
		var sessionName		= "";
		
		var patternTitle	= "";
		var topUrl			= "";
		
		var userName		= "";
		var spaceName		= "";
		
		var goals			= 0;
		var outsideOfUU		= false;
		
		var widgetInputBoxes = 0;
		var outcomeInputBoxes = 0;
		
		var gSpacePeers = new Array();
		var gSpaceTools = new Array();
		

		// ***********************************************
		// *                                             *
		// * login(usr, pwd)                             *
		// *                                             *
		// ***********************************************
			
		function login(usr, pwd)
		{
			var parameter =
			{
				username: usr,
				password: pwd
			};
		
			var params											= {};
			postdata											= gadgets.io.encodeValues(parameter);
			params[gadgets.io.RequestParameters.METHOD]			= gadgets.io.MethodType.POST;
			params[gadgets.io.RequestParameters.POST_DATA]		= postdata;

			try
			{
				gadgets.io.makeRequest("http://augur.wu-wien.ac.at/pleshare2/pleshare_api/user/login", loginFinished, params);
			}
			catch(e)
			{
				alert(e);
			}
		}
		
		
		// ***********************************************
		// *                                             *
		// * loginFinished(obj)                          *
		// *                                             *
		// ***********************************************
			
		function loginFinished(obj)
		{
			if(obj.rc == 200)
			{
				try
				{
					var restResult	= JSON.parse(obj.text);

					userName		= restResult.user.name;
					sessionId		= restResult.sessid;
					sessionName		= restResult.session_name;
					
					if(!outsideOfUU)
					{
						$("#show_loginname").html('<span style="float:right;">'+spaceName+' &gt; PLEShare</span>');
					}
					else
					{
						$("#show_loginname").html('<span style="float:right;"> &gt; PLEShare</span>');
					}
				}
				catch(e)
				{
					$("#show_loginname").html('<span style="float:right;">Error logging in</span>');
					alert(e);
				}
			}
			else
			{
				$("#show_loginname").html('<span style="float:right;">Space: <span style="font-weight:bold;">'+spaceName+'</span> Unknown user');
			}
		}
		
		// ***********************************************
		// *                                             *
		// * addWidget()                                 *
		// *                                             *
		// ***********************************************
		
		function addWidget(urlToWidget)
		{
			var envelope = 
			{
				type: "select", 
				uri: urlToWidget, 
				message:
				{ 
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type": "http://purl.org/role/terms/OpenSocialGadget", 
					"http://purl.org/dc/terms/title": "MyActivities"
				}
			};
			
			try
			{
				gadgets.openapp.publish(envelope);
			}
			catch(e)
			{
				alert(e);
			}
		
		}

		// ***********************************************
		// *                                             *
		// * getWidgetTitle()                            *
		// *                                             *
		// ***********************************************
		
		function getWidgetTitle(uri)
		{
			var params											= {};
			params[gadgets.io.RequestParameters.METHOD]			= gadgets.io.MethodType.GET;
			params[gadgets.io.RequestParameters.CONTENT_TYPE]	= gadgets.io.ContentType.DOM;
			params[gadgets.io.RequestParameters.HEADERS]		= 
			{
				'Accept' : 'application/json',
				'Content-Type': 'application/x-www-form-urlencoded ; charset=UTF-8',
				'Cookie': sessionName + '=' + sessionId + ';domain=augur.wu-wien.ac.at'
			};
			
			try
			{
				var rand1 = Math.random();
				var rand2 = Math.random();
				gadgets.io.makeRequest(uri+"?"+rand1+rand2, getWidgetTitleFinished, params);
			}
			catch(e)
			{
				alert(e);
			}
			
		}

		// ***********************************************
		// *                                             *
		// * getWidgetTitleFinished()                    *
		// *                                             *
		// ***********************************************

		function getWidgetTitleFinished(obj)
		{
			if(obj.rc == 200)
			{
				var widget			= obj.data;
				var widgetTitle		= "";
				var modulePrefs		= widget.getElementsByTagName("ModulePrefs");
				var uri				= $("#widgetsinput").attr("value");

				if(modulePrefs)
				{
					if(modulePrefs[0].getAttributeNode("title"))
					{
						widgetTitle = modulePrefs[0].getAttributeNode("title").nodeValue;
					}
					else
					{
						widgetTitle = "Not specified.";
					}
				}
				
				var dummyStruct = new Array();
				
				dummyStruct["name"] = widgetTitle;
				dummyStruct["uri"] = uri;
				
				gSpaceTools[gSpaceTools.length] = dummyStruct;

				var alt		= "widget";
				var src 	= absPath + "images/widget.png";
						
				widgetIds[widgetIds.length] = "#widget"+widgetIds.length;
				allWidgets[allWidgets.length] = '{"type":"'+ alt + '", "uri":"'+ uri +'"}';
					
				$("#mywidgets-menu").append('<div id="widget'+(widgetIds.length-1)+'" class="outcome-menu-item"><div></div><img src="'+src+'" alt="'+alt+':'+uri+'" style="float:left; margin-left:10px;" /><div class="outcome-menu-item-text">'+widgetTitle+'</div></div>');
				$("#widgets_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/check.png" alt="ok"/>');
				$("#widgets_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>If you want to add more widgets to your activity, enter its URI and press the add-button. To delete a widget, hover over its name and click the red cross.');
				$("#widgets_content_panel_info").attr("class", "info");
				++widgets;


				$('#widget'+(widgetIds.length-1)).hover
				(
					function()
					{
						$(this).addClass('outcome-menu-item-hover');
						$(this).find("div:nth-child(1)").html('<img class="delete-menu-item" src="'+absPath+'images/delete.png" alt="delete item"/>');
						$(this).find("img:nth-child(1)").click
						( 
							function()
							{
								$(this).parent("div").parent("div").remove();
								var identifier = $(this).parent("div").parent("div").attr("id");
								var id = identifier.replace(/widget/g, "");
										
								delete gSpaceTools[id];
								--widgets;

								if(widgets == 0)
								{
									$("#widgets_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/error.png" alt="error"/>');
									$("#widgets_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>Please add widgets to this activity before sharing it.<br><br>To add widgets to your activity, add them to your Personal Learning Environment and press the reload-button in the upper right corner.  To delete a widget, hover over its name and click the red cross.');
									$("#widgets_content_panel_info").attr("class", "info-error");
								}
							}
						)
					},
					function()
					{
						$(this).removeClass('outcome-menu-item-hover');
						$(this).find("div:nth-child(1)").html('');
					}
				);
			}
			else
			{
				if(obj.rc == 404)
				{
					alert("The given URL is incorrect. No widget could be found.");
				}
				else if(obj.rc == 400)
				{
					alert('URL does not seem to be valid. Has to start with \"http://\" or \"https://\".');
				}
				else
				{
					alert("Error "+obj.rc+" occured.");
				}
			}
		}


		// ***********************************************
		// *                                             *
		// * openActivity()                              *
		// *                                             *
		// ***********************************************
		
		
		function openActivity(nodeId)
		{
			var params											= {};
			params[gadgets.io.RequestParameters.METHOD]			= gadgets.io.MethodType.TEXT;
			params[gadgets.io.RequestParameters.CONTENT_TYPE]	= gadgets.io.ContentType.JSON;
			params[gadgets.io.RequestParameters.HEADERS]		= 
			{
				'Accept' : 'application/json',
				'Content-Type': 'application/x-www-form-urlencoded ; charset=UTF-8',
				'Cookie': sessionName + '=' + sessionId + ';domain=augur.wu-wien.ac.at'
			};

			try
			{
				var rand1 = Math.random();
				var rand2 = Math.random();
				gadgets.io.makeRequest("http://augur.wu-wien.ac.at/pleshare2/pleshare_api/node/"+nodeId+"/?parameters[type]=json&rand="+rand1+rand2, openActivityFinished, params);
			}
			catch(e)
			{
				alert(e);
			}
			
		}
		
		// ***********************************************
		// *                                             *
		// * openActivityFinished()                      *
		// *                                             *
		// ***********************************************

		function openActivityFinished(obj)
		{
			if(obj.rc == 200)
			{
				var objData		= obj.data;
				var bodyData	= objData.body;
				var bData		= JSON.parse(bodyData);
				
				var toolData	= bData.tools;

				for(var i = 0; i < toolData.length; ++i)
				{
					if(typeof(toolData[i].uri) != "undefined")
					{
						addWidget(toolData[i].uri);
						alert("Added Widget: "+toolData[i].name);
					}
				}
			}
		}


		// ***********************************************
		// *                                             *
		// * publish()                                   *
		// *                                             *
		// ***********************************************
			
		function publish(patternTitle, patternContent)
		{
			var parameter =
			{
				type:	"json",
				title:	patternTitle,
				body:	patternContent
			};
		
			var params											= {};
			postdata											= gadgets.io.encodeValues(parameter);
			params[gadgets.io.RequestParameters.METHOD]			= gadgets.io.MethodType.POST;
			params[gadgets.io.RequestParameters.POST_DATA]		= postdata;
			
			params[gadgets.io.RequestParameters.HEADERS] = 
			{
				'Content-Type': 'application/x-www-form-urlencoded ; charset=UTF-8',
				'Accept': 'text/xml',
				'Cookie': sessionName + '=' + sessionId + ';domain=augur.wu-wien.ac.at'
			};

			try
			{
				gadgets.io.makeRequest("http://augur.wu-wien.ac.at/pleshare2/pleshare_api/node", publishFinished, params);
			}
			catch(e)
			{
				alert(e);
			}
		}

		// ***********************************************
		// *                                             *
		// * publishFinished(obj)                        *
		// *                                             *
		// ***********************************************
		
		function publishFinished(obj)
		{
			if(obj.rc == 200)
			{
				var myObject = obj.data;
				alert(obj.text);
			}
		}


		// ***********************************************
		// *                                             *
		// * searchPatterns()                            *
		// *                                             *
		// ***********************************************
			
		function searchPatterns(searchterm)
		{
			var params											= {};
			params[gadgets.io.RequestParameters.METHOD]			= gadgets.io.MethodType.TEXT;
			params[gadgets.io.RequestParameters.CONTENT_TYPE]	= gadgets.io.ContentType.JSON;
			params[gadgets.io.RequestParameters.HEADERS]		= 
			{
				'Accept' : 'application/json',
				'Content-Type': 'application/x-www-form-urlencoded ; charset=UTF-8',
				'Cookie': sessionName + '=' + sessionId + ';domain=augur.wu-wien.ac.at'
			};

			$("#topic_retrieval_content_panel_toggle").html('<div class="result">Searching...</div>');					

			try
			{
				var rand1 = Math.random();
				var rand2 = Math.random();
				gadgets.io.makeRequest("http://augur.wu-wien.ac.at/pleshare2/pleshare_api/node?parameters[type]=json&parameters[uid]=7&parameters[query]="+searchterm+"&rand="+rand1+rand2, searchPatternsFinished, params);
			}
			catch(e)
			{
				alert(e);
			}
		}


		// ***********************************************
		// *                                             *
		// * searchPatternsFinished(obj)             *
		// *                                             *
		// ***********************************************
		
		function searchPatternsFinished(obj)
		{
			if(obj.rc == 200)
			{
				var restResult	= JSON.parse(obj.text);
				var htmlResult	= "";

				var i = 0;
				for(i = 0; i < restResult.length; ++i)
				{
					htmlResult = htmlResult + '<div class="resultNid">'+restResult[i].nid+'</div><div class="resultTitle">'+restResult[i].title+'</div>';					
				}
				
				if(i == 0)
				{
					$("#topic_retrieval_content_panel_toggle").html('<div class="noresult">No results found.</div>');					
				}
				else
				{
					$("#topic_retrieval_content_panel_toggle").html('<div class="result">'+htmlResult+'</div>');
				}
			}
			else
			{
			}
		}

		// ***********************************************
		// *                                             *
		// * retrieveMyPatterns()                        *
		// *                                             *
		// ***********************************************
			
		function retrieveMyPatterns()
		{
			var params											= {};
			params[gadgets.io.RequestParameters.METHOD]			= gadgets.io.MethodType.TEXT;
			params[gadgets.io.RequestParameters.CONTENT_TYPE]	= gadgets.io.ContentType.JSON;
			params[gadgets.io.RequestParameters.HEADERS]		= 
			{
				'Accept' : 'application/json',
				'Content-Type': 'application/x-www-form-urlencoded ; charset=UTF-8',
				'Cookie': sessionName + '=' + sessionId + ';domain=augur.wu-wien.ac.at'
			};

			try
			{
				var rand1 = Math.random();
				var rand2 = Math.random();
				gadgets.io.makeRequest("http://augur.wu-wien.ac.at/pleshare2/pleshare_api/node?parameters[type]=json&parameters[uid]=7&rand="+rand1+rand2, retrieveMyPatternsFinished, params);
			}
			catch(e)
			{
				alert(e);
			}
		}

		// ***********************************************
		// *                                             *
		// * retrieveStats()                             *
		// *                                             *
		// ***********************************************
			
		function retrieveStats()
		{
			var params											= {};
			params[gadgets.io.RequestParameters.METHOD]			= gadgets.io.MethodType.TEXT;
			params[gadgets.io.RequestParameters.CONTENT_TYPE]	= gadgets.io.ContentType.JSON;
			params[gadgets.io.RequestParameters.HEADERS]		= 
			{
				'Accept' : 'application/json',
				'Content-Type': 'application/x-www-form-urlencoded ; charset=UTF-8',
				'Cookie': sessionName + '=' + sessionId + ';domain=augur.wu-wien.ac.at'
			};

			try
			{
				var rand1 = Math.random();
				var rand2 = Math.random();
				gadgets.io.makeRequest("http://augur.wu-wien.ac.at/pleshare2/pleshare_api/node?parameters[type]=json&parameters[filter]=mostactiveusers&rand="+rand1+rand2, retrieveStatsFinished, params);
			}
			catch(e)
			{
				alert(e);
			}
		}

		// ***********************************************
		// *                                             *
		// * retrieveStatsFinished(obj)                  *
		// *                                             *
		// ***********************************************
		
		function retrieveStatsFinished(obj)
		{
			if(obj.rc == 200)
			{
				var restResult	= JSON.parse(obj.text);
				var htmlResult	= "";
				var i = 0;
				
				var clearNames	= new Array();
				var stats		= new Array();
				var colors		= new Array('#058DC7', '#50B432', '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4');
				var color		= 0;
				for (v in restResult)
				{
					var x = restResult[v];
					i = 1;
					
					for (y in x)
					{
						if(i % 2 == 0)
						{
							clearNames.push(x[y]);
						}
						if(i % 3 == 0)
						{
							var o = new Object();
							o['y'] = x[y];
							o['color'] = colors[color];
							stats.push(o);
						}
						++i;
					}
					++color;
					if(color == 10){color = 0;}
				}
				//var result = restResult.split(",");
				
				for(var j = 0; j < clearNames.length; ++j)
				{
					if(j % 2 == 0)
					{
						htmlResult = htmlResult + '<div style="" ><div style="width:200px; float:left; background-color:#eeeeee;">' + clearNames[j] + '</div><div style="width:50px; float:left; background-color:#eeeeee;">' + stats[j] + '</div></div>';
					}
					else
					{
						htmlResult = htmlResult + '<div><div style="width:200px; float:left; background-color:#cccccc;">' + clearNames[j] + '</div><div style="width:50px; float:left; background-color:#cccccc;">' + stats[j] + '</div></div>';
					}
					//htmlResult = htmlResult + "<div><span>"+restResult[i].nid+"</span><span>"+restResult[i].title+"</span><span>"+restResult[i].uri+"</span></div>";
				}

				$('#stats').html(htmlResult);
				
					var chart;
			
					chart = new Highcharts.Chart({
					chart: {
						renderTo: 'container',
						defaultSeriesType: 'column',
						margin: [ 10, 10, 60, 20]
						
					},
					title: {
						text: 'Top 10 peers working together.'
					},
					xAxis: {
						categories: clearNames,
						labels: {
							rotation: -30,
							align: 'right',
							style: {
								 font: 'normal 9px Verdana, sans-serif'
							}
						}
					},
					yAxis: {
						min: 0,
						title: {
							text: 'No. of shared activities'
						}
					},
					legend: {
						enabled: false
					},
					tooltip: {
						formatter: function() {
							return '<b>'+ this.x +'</b><br/>'+
								 'shared activities: '+ Highcharts.numberFormat(this.y, 0) +
								 '';
						}
					},
				        series: [{
						name: 'shared activities',
						data: stats,
						dataLabels: {
							enabled: true,
							rotation: -90,
							color: '#FFFFFF',
							align: 'right',
							x: -3,
							y: 10,
							formatter: function() {
								return this.y;
							},
							style: {
								font: 'normal 9px Verdana, sans-serif'
							}
						}			
					}]
				});
	
				
				//var t = chart["series"];
				//t[0].color = colors;
				

			}
			else
			{
				alert(obj.rc);
			}
		}



		// ***********************************************
		// *                                             *
		// * retrieveMyPatternsFinished(obj)             *
		// *                                             *
		// ***********************************************
		
		function retrieveMyPatternsFinished(obj)
		{
			if(obj.rc == 200)
			{
				var restResult	= JSON.parse(obj.text);
				var htmlResult	= "";
				
				for(var i = 0; i < restResult.length; ++i)
				{
					
					//htmlResult = htmlResult + '<div class="outcome-menu-item"><div></div><img src="'+absPath+'images/widget.png" alt="" style="float:left; margin-left:10px;" /><div class="resultNid">'+restResult[i].nid+'</div><div class="resultTitle">'+restResult[i].title+'</div></div><div style="clear: both;"></div>';
					
					//alert("NodeId: "+restResult[i].nid);
					
					$("#my_practices_content_panel_toggle1").append('<div id="myAct'+i+'" class="outcome-menu-item"><div></div><img src="'+absPath+'images/widget.png" alt="" style="float:left; margin-left:10px;"/><div style="display:none">'+restResult[i].nid+'</div><div class="outcome-menu-item-text">'+restResult[i].title+'</div></div><div style="clear: both;"></div>');
								
					$('#myAct'+i).hover
					(
							function()
							{
								var parent = $(this);
								$(this).find("div:nth-child(1)").html('<img class="delete-menu-item" src="'+absPath+'images/adds.png" alt="add activity"/>');
								$(this).addClass('outcome-menu-item-hover');
								
								$(this).find("img:nth-child(1)").click
								( 
									function()
									{
										var nodeId	= parent.find("div:eq(1)").html();
										openActivity(nodeId);
									}
								)
							},
							function()
							{
								$(this).removeClass('outcome-menu-item-hover');
								$(this).find("div:nth-child(1)").html('');
							}
					);
				}
			}
			else
			{
			}
		}
		
		// ***********************************************
		// *                                             *
		// * addInputBox(to)                             *
		// *                                             *
		// ***********************************************

		function addInputBox(to)
		{
			if(to == "widgets")
			{
				$("#mywidgets-menu").html('<span><input id="widgetsinput" type="text" name="" value="" /></span>');
				$("#widgets").show();
				++widgetInputBoxes;
			}
			
			if(to == "peers")
			{		
				$("#mypeers-menu").html('<div><div style="width:70px;">Name: </div><div><input id="peersname-input" type="text" name="" value="" /></div><div style="width:70px;">Unique Id.:</div><div><input id="peersuid-input" type="text" name="" value="" /></div>');
				
				$("#peers").show();
				++outcomeInputBoxes;
			}
		}
		
		// ***********************************************
		// *                                             *
		// * sendHttpRequest()                           *
		// *                                             *
		// ***********************************************

		function makeDOMRequest(url)
		{
			var params = {};
			params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.DOM;
			gadgets.io.makeRequest(url, response, params);
		}

	
		// ***********************************************
		// *                                             *
		// * showResults()                               *
		// *                                             *
		// ***********************************************

		function showResults(obj)
		{
			if(obj.rc == 200)
			{
				var myObject = obj.data;
				var root = myObject["http://127.0.0.1:8073/spaces/test"];
				var tools = root["http://www.role-project.eu/rdf/tool"];
				
				for (var i = 0; i < tools.length; ++i)
				{
					//alert([i].value);
				}
			}
		}

		// ***********************************************
		// *                                             *
		// * JQuery $(document).ready                    *
		// *                                             *
		// ***********************************************

		$(document).ready
		(function(){
				  
		makeJSONRequest("rest:info");		
		login("roletest", "42role42");
		
				
		// ***********************************************
		// *                                             *
		// * makeJSONRequest()                           *
		// *                                             *
		// ***********************************************

		function makeJSONRequest(restCall)
		{
			try
			{
				topUrl = top.location.href;
			}
			catch(e)
			{
			}
			
			var params = {};
			params[gadgets.io.RequestParameters.CONTENT_TYPE]	= gadgets.io.ContentType.JSON;
			params[gadgets.io.RequestParameters.HEADERS]		= 
			{
				'Accept' : 'application/json'
			};
			
			var rand1 = Math.random();
			var rand2 = Math.random();
			
			var restUrl = topUrl + '/' + restCall + '?rand='+rand1+rand2;
			
			try
			{
				gadgets.io.makeRequest(restUrl, updateShareDisplay, params);
			}
			catch(e)
			{
				alert(e);
			}
		};


		// ***********************************************
		// *                                             *
		// * updateBrowseDisplay()                       *
		// *                                             *
		// ***********************************************

		function updateBrowseDisplay(obj)
		{
			
		}



		// ***********************************************
		// *                                             *
		// * updateShareDisplay()                        *
		// *                                             *
		// ***********************************************

		function updateShareDisplay(obj)
		{
			var topUrl = "";
			
			try
			{
				topUrl = top.location.href;
			}
			catch(e)
			{
			}
			
			if(obj.rc == 200)
			{
				var idMember			= "http://xmlns.com/foaf/0.1/member";
				var idTool				= "http://purl.org/role/terms/tool";
				var idTitle				= "http://purl.org/dc/terms/title";
				var idWidget			= "http://purl.org/role/terms/widget";
				var owlSameAs			= "http://www.w3.org/2002/07/owl#sameAs";
				var idSpace				= topUrl;
				
				var sT					= "";
				var spaceMembers		= new Array();
				var spaceTools			= new Array();

				var spaceMemberNames	= new Array();
				var spaceToolNames		= new Array();

				var myObject			= obj.data;
				var spaceOverview		= myObject[topUrl];
				
				var i					= 0;

				//$("#rest-call-result").html(obj.text);

				// --------------------------

				sT			= spaceOverview[idTitle];
				spaceName	= sT[0].value;
			
				spaceMembers 	= spaceOverview[idMember];
				spaceTools		= spaceOverview[idTool];

				var userNameRef1, userNameRef2, userNameRef3, userNameRef4 = "";
				var toolNameRef1, toolNameRef2, toolNameRef3, toolNameRef4 = "";

				// Save member-uris and member-names in array: spaceMemberNames[]

				for(i = 0; i < spaceMembers.length; ++i)
				{
					var memberInfo		= new Array();
					userNameRef1		= myObject[spaceMembers[i].value];
					userNameRef2		= userNameRef1[owlSameAs];
					userNameRef3		= myObject[userNameRef2[0].value];
					userNameRef4		= userNameRef3[idTitle];
					
					memberInfo["uri"]	= spaceMembers[i].value;
					memberInfo["name"]	= userNameRef4[0].value;
					
					spaceMemberNames[i]	= memberInfo;
					gSpacePeers[i] = memberInfo;
				}
				
				// Save tool-uris and tool-names in array: spaceToolNames[]
				
				if(spaceTools)
				{
				
					for(i=0; i < spaceTools.length; ++i)
					{
						var	toolInfo		= new Array();
						toolNameRef1		= myObject[spaceTools[i].value];
						toolNameRef2		= toolNameRef1[idTitle];
						toolNameRef3		= toolNameRef1[idWidget];

						toolInfo["uri"]		= toolNameRef3[0].value;
						
						if(toolNameRef2)
						{
							toolInfo["name"]	= toolNameRef2[0].value;
						}
						else
						{
							toolInfo["name"]	= "Unknown title."
						}
					
						spaceToolNames[i]	= toolInfo;	
						gSpaceTools[i]		= toolInfo;
						//alert(spaceToolNames[i].uri + " " + spaceToolNames[i].name);
					}
				}
				else
				{
					$("#widgets_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>Please add widgets to this activity before sharing it. To add more widgets to this activity, please add them to your Personal Learning Environment and press the reload-button in the upper right corner.');
					$("#widgets_content_panel_info").attr('class', 'info-error');
				}
				
				peers = 0;
				widgets = 0;
				
				
				/***********************************************************/
				/* Add Widgets to Display                                  */
				/***********************************************************/

				if(spaceToolNames.length > 0)
				{
					$("#widgets_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/check.png" alt="ok"/><span style="font-weight:bold;"></span>');
					
					$("#widgets_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>To add more widgets to this activity, please add them to your Personal Learning Environment and press the reload-button in the upper right corner.  To delete a widget, hover over its name and click the red cross.');
					$("#widgets_content_panel_info").attr('class', 'info');
					
				}
				else
				{
					$("#widgets_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;"  src="' + absPath + 'images/error.png" alt="ok" style="vertical-align:top; float:right; margin-right:10px;"/>');
					
					$("#widgets_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>Please add widgets to this activity before sharing it. To add more widgets to this activity, please add them to your Personal Learning Environment and press the reload-button in the upper right corner.');
					$("#widgets_content_panel_info").attr('class', 'info-error');

				}
				
				var Str	= "";
				$("#mywidgets-menu").html('');
				widgetIds	= new Array();
				widgets		= 0;
				
				for (var i = 0; i < spaceToolNames.length; ++i)
				{
					//Str = Str + '<img src="'+absPath+'images/widget.png" alt="widget" style="float:left; margin-left:10px;" /><div class="outcome-menu-item-text">'+spaceToolNames[i].name+'</div><div style="clear: both;"></div>';
					
					var src		= absPath + "images/widget.png";
					var title	= spaceToolNames[i].name;
					
					widgetIds[widgetIds.length] = "#widget"+widgetIds.length;
					$("#mywidgets-menu").append('<div id="widget'+(widgetIds.length-1)+'" class="outcome-menu-item"><div></div><img src="'+src+'" alt="" style="float:left; margin-left:10px;" /><div class="outcome-menu-item-text">'+title+'</div></div>');
					
					$('#widget'+(widgetIds.length-1)).hover(
							function()
							{
								$(this).addClass('outcome-menu-item-hover');
								$(this).find("div:nth-child(1)").html('<img class="delete-menu-item" src="'+absPath+'images/delete.png" alt="delete item"/>');
								$(this).find("img:nth-child(1)").click
								( 
									function()
									{
										$(this).parent("div").parent("div").remove();
										var identifier = $(this).parent("div").parent("div").attr("id");
										var id = identifier.replace(/widget/g, "");
										
										delete gSpaceTools[id];
										--widgets;

										if(widgets == 0)
										{
											$("#widgets_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/error.png" alt="error"/>');
											
											$("#widgets_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>Please add widgets to this activity before sharing it.<br><br>To add widgets to your activity, add them to your Personal Learning Environment and press the reload-button in the upper right corner.  To delete a widget, hover over its name and click the red cross.');
											$("#widgets_content_panel_info").attr("class", "info-error");
										}
									}
								);
								
							}, 
							function()
							{
								$(this).removeClass('outcome-menu-item-hover');
								$(this).find("div:nth-child(1)").html('');
							});
						++widgets;
				}
				
				$('#widgets').show();
				
				/***********************************************************/
				/* Add Peers to Display                                    */
				/***********************************************************/
				
				if((spaceMemberNames.length > 1) && (spaceMemberNames.length != 0))
				{
					$("#peers_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/check.png" alt="check"/>');
				}
				else
				{
					$("#peers_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/check.png" alt="check"/>');
				}
				
				var Str	= "";
				
				$("#mypeers-menu").html('');
				peerIds		= new Array();
				peers		= 0;
				
				for (var i = 0; i < spaceMemberNames.length; ++i)
				{
					//Str = Str + '<img src="'+absPath+'images/widget.png" alt="widget" style="float:left; margin-left:10px;" /><div class="outcome-menu-item-text">'+spaceToolNames[i].name+'</div><div style="clear: both;"></div>';
					
					var src		= absPath + "images/peer.png";
					var title	= spaceMemberNames[i].name;
					
					peerIds[peerIds.length] = "#peer"+peerIds.length;
					$("#mypeers-menu").append('<div id="peer'+(peerIds.length-1)+'" class="outcome-menu-item"><div></div><img src="'+src+'" alt="" style="float:left; margin-left:10px;" /><div class="outcome-menu-item-text">'+title+'</div></div>');
					
					$('#peer'+(peerIds.length-1)).hover(
							function()
							{
								$(this).addClass('outcome-menu-item-hover');
								$(this).find("div:nth-child(1)").html('<img class="delete-menu-item" src="'+absPath+'images/delete.png" alt="delete item"/>');
								$(this).find("img:nth-child(1)").click
								( 
									function()
									{
										$(this).parent("div").parent("div").remove();
										var identifier = $(this).parent("div").parent("div").attr("id");
										var id = identifier.replace(/peer/g, "");
										
										delete gSpacePeers[id];
										--peers;
										//alert(peers);

										if(peers == 0)
										{
											$("#peers_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/info.png" alt="error"/>');
											$("peers_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>To add peers to this activity, please add them to your Personal Learning Environment and press the reload-button in the upper right corner.');
										}
									}
								);
								
							}, 
							function()
							{
								$(this).removeClass('outcome-menu-item-hover');
								$(this).find("div:nth-child(1)").html('');
							});
						++peers;
				}
				$('#peers').show();
			}
			else
			{
				//alert("Error: "+obj.rc);
				outsideOfUU = true;
				$("#reload-button").hide();
				$("#show_loginname").html('<span style="float:right;"><span style="font-weight:bold;">Logged in as:</span> '+userName);
				
				$("#peers_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>To add a peer, enter her/his name (e.g. John Doe) AND a unique identifier (e.g. email-adress, facebook-profile,...) in the boxes above and click the add-button.');
				$("#widgets_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>To add a widget, enter its URI in the box above and click the add-button.');
				
				$('#widgets-button').show();
				$('#peers-button').show();
				
				addInputBox("widgets");
				addInputBox("peers");
			}
		};
	
		$(function(){
				  $( "#privacy-button" ).button({icons: {primary:"ui-icon-locked",secondary: "ui-icon-triangle-1-s"}}).click(function()
					{ 
						$("#dropdown-menu").fadeToggle(); 
					}),
				  
				$(function() {
				<!-- $( "#radio" ).buttonset(); -->
				}),
				
				
				$('#show_peers').click(function(){
					<!-- $('#outcomes_content_panel_toggle').slideToggle('slow', function() { }); -->
					$("#help_content_panel_info").hide();
					
					$('#peers_content_panel_toggle').slideToggle('slow', function() 
					{
						if ($(this).is(':visible'))
						{
							$("#peers_arrow").attr("src", absPath + "images/arrow_up.png");
						}
						else
						{
							$("#peers_arrow").attr("src", absPath + "images/arrow_down.png");
						}
					});
				}),
				
				$('#show_privacy').click(function(){

				$("#help_content_panel_info").hide();
					<!-- $('#outcomes_content_panel_toggle').slideToggle('slow', function() { }); -->
					$('#privacy_content_panel_toggle').slideToggle('slow', function() 
					{
						if ($(this).is(':visible'))
						{
							$("#privacy_arrow").attr("src", absPath + "images/arrow_up.png");
						}
						else
						{
							$("#privacy_arrow").attr("src", absPath + "images/arrow_down.png");
						}
					});
				}),
				
				$('#show_widgets').click(function(){
				$("#help_content_panel_info").hide();
					<!-- $('#outcomes_content_panel_toggle').slideToggle('slow', function() { }); -->
					$('#widgets_content_panel_toggle').slideToggle('slow', function() 
					{
						if ($(this).is(':visible'))
						{
							$("#widgets_arrow").attr("src", absPath + "images/arrow_up.png");
						}
						else
						{
							$("#widgets_arrow").attr("src", absPath + "images/arrow_down.png");
						}
					});
				}),

				
				$('#show_general').click(function(){
				$("#help_content_panel_info").hide();
					<!-- $('#outcomes_content_panel_toggle').slideToggle('slow', function() { }); -->
					$('#general_content_panel_toggle').slideToggle('slow', function() 
					{
						if ($(this).is(':visible'))
						{
							$("#general_arrow").attr("src", absPath + "images/arrow_up.png");
						}
						else
						{
							$("#general_arrow").attr("src", absPath + "images/arrow_down.png");
						}
					});
				}),

				$('#show_outcomes').click(function(){
					<!-- $('#outcomes_content_panel_toggle').slideToggle('slow', function() { }); -->
				$("#help_content_panel_info").hide();
					$('#outcomes_content_panel_toggle').slideToggle('slow', function()
					{ 
						if ($(this).is(':visible'))
						{
							$("#outcomes_arrow").attr("src", absPath + "images/arrow_up.png");
						}
						else
						{
							$("#outcomes_arrow").attr("src", absPath + "images/arrow_down.png");
						}
					});
				}),
				
				$('#show_my_practices').click(function(){
					<!-- $('#outcomes_content_panel_toggle').slideToggle('slow', function() { }); -->
				$("#help_content_panel_info").hide();
					$('#my_practices_content_panel_toggle').slideToggle('slow', function() 
					{
						if ($(this).is(':visible'))
						{
							$("#my_practices_arrow").attr("src", absPath + "images/arrow_up.png");
						}
						else
						{
							$("#my_practices_arrow").attr("src", absPath + "images/arrow_down.png");
						}
					});
				}),
				
				$('#show_topic_retrieval').click(function(){
					<!-- $('#outcomes_content_panel_toggle').slideToggle('slow', function() { }); -->
				$("#help_content_panel_info").hide();
					$('#topic_retrieval_content_panel_toggle').slideToggle('slow', function() 
					{
						if ($(this).is(':visible'))
						{
							$("#topic_retrieval_arrow").attr("src", absPath + "images/arrow_up.png");
						}
						else
						{
							$("#topic_retrieval_arrow").attr("src", absPath + "images/arrow_down.png");
						}
					});
				}),
				
				  $('#dropdown-menu #dropdown-menu-item1, #dropdown-menu-item2, #dropdown-menu-item3').hover(
					function()
					{
						$(this).addClass('dropdown-menu-item-hover');
					}, 
					function()
					{
						$(this).removeClass('dropdown-menu-item-hover');
					}),
					
				$('#dropdown-menu-item1').click(function(){
					$("#dropdown-menu").fadeToggle();
					$( "#privacy-button").button('option', 'label', 'everyone');
					$('#privacy_content_panel_toggle').slideToggle('slow', function() {});
					$("#privacy_arrow").attr("src", absPath + "images/arrow_down.png");
					$("#privacy_content_panel_status").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="'+absPath+'images/check.png" alt="options"/>');
					$("#privacy_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>You are sharing this pattern with everyone. To change the privacy of your pattern, please click the button above.');
					$("#privacy_content_panel_info").attr("class", "info-ok");
				}),
				
				$('#dropdown-menu-item2').click(function(){
					$("#dropdown-menu").fadeToggle();
					$( "#privacy-button").button('option', 'label', 'my friends');
					
					$('#privacy_content_panel_toggle').slideToggle('slow', function() {});
					$("#privacy_arrow").attr("src", absPath + "images/arrow_down.png");
					$("#privacy_content_panel_status").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="'+absPath+'images/check.png" alt="options"/>');
					
					$("#privacy_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>You are changing this pattern with your friends. To change the privacy of your pattern, please click the button above.');
					$("#privacy_content_panel_info").attr("class", "info-ok");					
				}),
				
				$('#dropdown-menu-item3').click(function(){
					$("#dropdown-menu").fadeToggle();
					$( "#privacy-button").button('option', 'label', 'only me');
					
					$('#privacy_content_panel_toggle').slideToggle('slow', function() {});
					$("#privacy_arrow").attr("src", absPath + "images/arrow_down.png");
					$("#privacy_content_panel_status").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="'+absPath+'images/info.png" alt="options"/>');
					
					$("#privacy_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>This pattern is only visible to you. To change the privacy of your pattern, please click the button above.');
					$("#privacy_content_panel_info").attr("class", "info");
				}),
				
				 $(function()
				 {
					$("#radioset").buttonset();
 				 }),
				 

				  $( "#dologin-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", secondary: "ui-icon-check" */}}).click(function() 
				{
					var usr = $("#login-username").attr("value");
					var pwd = $("#login-password").attr("value");
					
					login(usr,pwd);
					
					if ($("#login-panel").is(':visible'))
					{
						$('#login-panel').fadeToggle('slow', function() { });
						$('#share-panel').fadeToggle('slow', function() { });
					}
				}),
				 
			 
				 $( "#dontlogin-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", secondary: "ui-icon-check" */}}).click(function() 
				{
					//if ($("#browse-panel").is(':visible'))
					//{
					//	$('#browse-panel').fadeToggle('slow', function() { });
					//	$('#share-panel').fadeToggle('slow', function() { });
					//}
				}),

				 $( "#login-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", secondary: "ui-icon-check" */}}).click(function() 
				{
					if ($("#browse-panel").is(':visible'))
					{
						$('#browse-panel').fadeToggle('slow', function() { });
						$('#login-panel').fadeToggle('slow', function() { });
					}
					if ($("#share-panel").is(':visible'))
					{
						$('#share-panel').fadeToggle('slow', function() { });
						$('#login-panel').fadeToggle('slow', function() { });
					}
				}),


				 $( "#share-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", secondary: "ui-icon-check" */}}).click(function() 
				{
					if ($("#browse-panel").is(':visible'))
					{
						$('#browse-panel').fadeToggle('slow', function() { });
						$('#share-panel').fadeToggle('slow', function() { });
					}
					
					if ($("#login-panel").is(':visible'))
					{
						$('#login-panel').fadeToggle('slow', function() { });
						$('#share-panel').fadeToggle('slow', function() { });
					}
				}),

				 $( "#browse-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", secondary: "ui-icon-check" */}}).click(function() 
				{
					retrieveStats();
					
					addWidget("http://augur.wu.ac.at/plerecorder/plerecorder.xml");
					
					if ($("#share-panel").is(':visible'))
					{
						$('#browse-panel').fadeToggle('slow', function()
						{
							retrieveMyPatterns();
							updateBrowseDisplay();
						});
						$('#share-panel').fadeToggle('slow', function() { });
					}
					
					if ($("#login-panel").is(':visible'))
					{
						$('#browse-panel').fadeToggle('slow', function()
						{
							retrieveMyPatterns();
							updateBrowseDisplay();
						});
						$('#login-panel').fadeToggle('slow', function() { });
					}
				}),
	

				 $( "#search-button button:first" ).button({icons: {primary:"ui-icon-search"/*, secondary: "ui-icon-check" */}}).click(function() 
				{
					var searchword = $("#searchterm").attr("value");
					searchPatterns(searchword);
					
					if ($('#topic_retrieval_content_panel_toggle').is(':visible'))
					{
					}
					else
					{
						$('#topic_retrieval_content_panel_toggle').slideToggle('slow', function() {});
						
						if ($('#topic_retrieval_content_panel_toggle').is(':visible'))
						{
							$("#topic_retrieval_arrow").attr("src", absPath + "images/arrow_up.png");
						}
						else
						{
							$("#topic_retrieval_arrow").attr("src", absPath + "images/arrow_down.png");
						}
					}
				}),


				 // *************************************************************************************************************************************
				 // share your pattern
				 // *************************************************************************************************************************************								 
				$( "#shareit-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", */ secondary: "ui-icon-check"}}).click(function() 
				{
					// Aufbau des Objektes patternJSON
					var patternJSON = new Array();
				
					var peers		= "[";
					var tools		= "[";
					var outcomes	= "[";
					var toolsAdded	= false;
					
					for(var i = 0; i < gSpacePeers.length; ++i)
					{
						if(gSpacePeers[i])
						{
							peers = peers + '{"uri":"' + gSpacePeers[i].uri + '", "name":"' + gSpacePeers[i].name + '"},';
						}
					}
					peers = peers + "{}]";
					
					for(var i = 0; i < gSpaceTools.length; ++i)
					{
						if(gSpaceTools[i])
						{
							tools = tools + '{"uri":"' + gSpaceTools[i].uri + '", "name":"' + gSpaceTools[i].name + '"},';
							toolsAdded = true;
						}
					}
					tools = tools + "{}]";
					
					for(var i = 0; i < allOutcomes.length; ++i)
					{
						if(allOutcomes[i])
						{
							outcomes = outcomes + allOutcomes[i] + ',';
						}
					}
					
					outcomes = outcomes + "{}]";
					var patternString = '{"peers":' + (gSpacePeers ? peers : "{}") + ',"tools":' + (gSpaceTools ? tools : "{}") + ',"outcomes":' + (allOutcomes ? outcomes : "{}") + '}';
					
					//alert(patternString);
					
					if(allOutcomes.length > 0)
					{
						try
						{
							var patternJsonFlat = JSON.parse(patternString);
						}
						catch(e)
						{
							//alert(e);
						}
					
						if(toolsAdded)
						{
					
							if(patternTitle.length > 0) // PatternTitle given?
							{
								publish(patternTitle, patternString);
							}
							else	// No. Autogenerate one.
							{
								if(!outsideOfUU)
								{
									patternTitle = "autoID_" + allOutcomesNames[0];
								}
								else
								{
									patternTitle = "autoID_" + allOutcomesNames[0];
								}
								
								try
								{
									patternTitle.replace(/\s/g, "_");
								}
								catch(e)
								{
									alert(e);
								}
								publish(patternTitle, patternString);
							}
						}
						else
						{
							alert("Please add widgets to your activity before sharing it.");
						}
					}
					else
					{
						alert("Please specify a goal for your activity before sharing it.");
						
						if($('#outcomes_content_panel_toggle').is(':visible'))
						{
						}
						else
						{
							$('#outcomes_content_panel_toggle').slideToggle('slow', function()
							{ 
								if ($(this).is(':visible'))
								{
									$("#outcomes_arrow").attr("src", absPath + "images/arrow_up.png");
								}
								else
								{
									$("#outcomes_arrow").attr("src", absPath + "images/arrow_down.png");
								}
							});
						}
					}
				}),
				  
				  // share your pattern end
				 
				 
				 // reload space-items (peers, widgets, etc.)
				 
				 $( "#reload-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", secondary: "ui-icon-refresh" */}}).click(function() 
				{
					makeJSONRequest("rest:info");
				}),
				 
				 
				  $( "#general-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", */ secondary: "ui-icon-check"}}).click(function() { 
					
					var title = $("#pattern-title").attr("value");
					$("#pattern-title").attr("value", "");

					patternTitle = title;

						if(title.length > 2)
						{
							$('#general_content_panel_toggle').slideToggle('slow', function() {});
							$("#general_arrow").attr("src", absPath + "images/arrow_down.png");
							$("#general_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="'+absPath+'images/check.png" alt="options"/>');
							$("#general_content_panel_info").attr("class", "info-ok");
							$("#general_content_panel_info").html('The name of your pattern is: '+patternTitle);
						}
						else
						{
							alert("The name of your pattern is too short." + '\n' + "Please enter at least 3 characters.");
						}
						
					}),


				/*widgets BEGIN*********************************************/
				
				  $("#widgets-button button:first").button({icons: {/* primary:"ui-icon-circle-arrow-n", */ secondary: "ui-icon-plusthick"}}).click(function()
					{ 
						var uri		= $("#widgetsinput").attr("value");
						// !!!
						getWidgetTitle(uri);
						
							
							
					}),

				/*widgets END*********************************************/

				/*peers BEGIN*********************************************/
				
				  $( "#peers-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", */ secondary: "ui-icon-plusthick"}}).click(function()
					{ 
					//				peersname-input
					//				peersuid-input
					
						var name	= $("#peersname-input").attr("value");
						var uri		= $("#peersuid-input").attr("value");
						
						var alt		= "peer";
						var src		= absPath + "images/peer.png";
						
						peerIds[peerIds.length] = "#peer"+peerIds.length;
						allPeers[allPeers.length] = "{\"type\":\""+ alt + "\", \"value\":\""+ name +"\"}";
					
						$("#mypeers-menu").append('<div id="peer'+(peerIds.length-1)+'" class="outcome-menu-item"><div></div><img src="'+src+'" alt="'+alt+':'+name+'" style="float:left; margin-left:10px;" /><div class="outcome-menu-item-text">'+name+'</div></div>');
							++users;
						
						
						var dummyPeer		= new Array();
						dummyPeer["name"]	= name;
						dummyPeer["uri"]	= uri;
						
						gSpacePeers[gSpacePeers.length] = dummyPeer;
						
						
						$("#peersname-input").attr("value", "");
						$("#peersuid-input").attr("value", "");
						
						$("#peers_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/check.png" alt="ok"/>');
						
						$('#peer'+(peerIds.length-1)).hover(
							function()
							{
								$(this).addClass('outcome-menu-item-hover');
								$(this).find("div:nth-child(1)").html('<img class="delete-menu-item" src="'+absPath+'images/delete.png" alt="delete item"/>');
								$(this).find("img:nth-child(1)").click
								( 
									function()
									{
										$(this).parent("div").parent("div").remove();
										var identifier = $(this).parent("div").parent("div").attr("id");
										var id = identifier.replace(/peer/g, "");
										
										delete gSpacePeers[id];
										--users;
										//alert(peers);

										if(users == 0)
										{
											$("#peers_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/info.png" alt="error"/>');
											$("peers_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>To add a peer, enter her/his name (e.g. John Doe) AND a unique identifier (e.g. email-adress, facebook-profile,...) in the boxes above and click the add-button.');
										}
									}
								)
							},
							function()
							{
								$(this).removeClass('outcome-menu-item-hover');
								$(this).find("div:nth-child(1)").html('');
							}
						);
							
							
					}),

				/*peers END*********************************************/


				  $( "#peers-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", */ secondary: "ui-icon-plusthick"}}).click(function()
					{					
					
					}),



				  $( "#outcome-button button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", */ secondary: "ui-icon-plusthick"}}).click(function() { 
						
						var title = $("#outcome-title").attr("value");
						$("#outcome-title").attr("value", "");
						
						if(title.length > 2)
						{
							outcomeIds[outcomeIds.length] = "#outcome"+outcomeIds.length;
						
							if($("#radio1").attr("checked"))
							{
								var src = absPath + "images/doc.png";
								var alt = "document";
							}
							else
							{
								var src = absPath + "images/goal.png";
								var alt = "goal";
								++goals;
							}
							
							allOutcomes[allOutcomes.length] = '{"type":"'+ alt + '", "value":"'+ title +'"}';
							allOutcomesNames[allOutcomesNames.length] = title;
							
							$("#myoutcomes-menu").append('<div id="outcome'+(outcomeIds.length-1)+'" class="outcome-menu-item"><div></div><img src="'+src+'" alt="'+alt+':'+title+'" style="float:left; margin-left:10px;" /><div class="outcome-menu-item-text">'+title+'</div></div>');
							++outcomes;
							
							if((outcomes > 0) && (goals > 0))
							{
								$("#outcomes_content_panel_status").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/check.png" alt="check"/>');
								$("#outcomes_content_panel_info").attr("class", "info-ok");																   
								$("#outcomes_content_panel_info").html("You have added one goal. Please feel free to add more documents and goals.");
							}
							
							if(outcomes == 1){$('#outcomes').show();}
							
							$('#outcome'+(outcomeIds.length-1)).hover(
							function()
							{
								$(this).addClass('outcome-menu-item-hover');
								$(this).find("div:nth-child(1)").html('<img class="delete-menu-item" src="'+absPath+'images/delete.png" alt="delete item"/>');
								$(this).find("img:nth-child(1)").click
								( 
									function()
									{
										// Detect wether it's a goal or a document that is deleted
										
										var content = $(this).parent("div").parent("div").html();
										var suchstring = /(goal.png)/g;
										var suchergebnis = suchstring.test( content );
										if (suchergebnis){ --goals; }
										
										if(goals == 0)
										{
											$("#outcomes_content_panel_info").attr("class", "info");																   
											$("#outcomes_content_panel_info").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="http://augur.wu.ac.at/patternshare/images/help.png" alt="help"/>Please enter at least one goal, your pattern is aiming at.');
											$("#outcomes_content_panel_status").html('<img style="vertical-align:top; float:left; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/error.png" alt="error"/>');
										}
										
										$(this).parent("div").parent("div").remove();
										
										var identifier = $(this).parent("div").parent("div").attr("id");
										var id = identifier.replace(/outcome/g, "");
										delete allOutcomes[id];
										delete allOutcomesNames[id];

										--outcomes;
										
										if((outcomes > 0) && (goals > 0))
										{
											$("#outcomes_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/check.png" alt="check"/>');
										}
										
										if(outcomes == 0)
										{
											$('#outcomes').hide();
											$("#outcomes_content_panel_status").html('<img style="vertical-align:top; margin-left:10px; margin-right:10px;" src="' + absPath + 'images/error.png" alt="error"/>');
										}
									}
								);
								
							}, 
							function()
							{
								$(this).removeClass('outcome-menu-item-hover');
								$(this).find("div:nth-child(1)").html('');
							});

							$( "#successfully_added").fadeIn('slow');
							$( "#successfully_added").fadeOut('slow');
							
						}
						else
						{
							alert("The name of your outcome is too short." + '\n' + "Please enter at least 3 characters.");
						}
						
				  }),
				  
  				  $( "#outcome-button2 button:first" ).button({icons: {/* primary:"ui-icon-circle-arrow-n", */ secondary: "ui-icon-minusthick"}}).click(function()
				  {
						$(selectedElement).remove();
				  }),

				  $('myoutcomes-menu div').hover(
					function()
					{
						$(this).addClass('outcome-menu-item-hover');
					}, 
					function()
					{
						$(this).removeClass('outcome-menu-item-hover');
					}),

				  /* design tab BEGIN */

				  $('#myfriends-menu .dropdown-menu-item').hover(
					function()
					{
						$(this).addClass('dropdown-menu-item-hover');
					}, 
					function()
					{
						$(this).removeClass('dropdown-menu-item-hover');
					});
					
					$('.dropdown-menu-item, .outcome-menu-item').hover(
					function()
					{
						$(this).addClass('dropdown-menu-item-hover');
					}, 
					function()
					{
						$(this).removeClass('dropdown-menu-item-hover');
					}),

					$('.dropdown-menu-item').draggable
					({
						containment: '#dnd',
						drag: function (event, ui)
						{
							$( this ).css({ opacity: 0.5 });
							$( this ).addClass('drag-highlight');
						}
					}),
					
					$("#myoutcomes-menu div").click(function()
					{
						var i;

						for(i = 0; i < outcomeIds.length; ++i)
						{
							$('#'+outcomeIds[i]).removeClass('drag-highlight');
						}
					
						$( this ).addClass('drag-highlight');
						selectedElement = $(this);
					}),
		
					$( "#myfriends-menu" ).droppable
					({
						drop: function( event, ui )
						{
							$('#myfriends-menu').append('<div class="dropdown-menu-item">'+ui.draggable[0].innerHTML+'</div>');
							$(ui.draggable[0]).remove();
			
							$('.dropdown-menu-item').hover(function()
							{
								$(this).addClass('dropdown-menu-item-hover');
							}, 
								function()
							{
								$(this).removeClass('dropdown-menu-item-hover');
							}),

							$('#hidetip').fadeOut('slow'),
							
							$('.dropdown-menu-item').draggable
							({
								containment: '#dnd',
								drag: function (event, ui)
								{
									$( this ).css({ opacity: 0.5 });
									$( this ).addClass('drag-highlight');
								}
							})
						}
					}),  
					
					$( "#myaddedfriends-menu" ).droppable
					({
						drop: function( event, ui )
						{
							$('#myaddedfriends-menu').append('<div class="dropdown-menu-item">'+ui.draggable[0].innerHTML+'</div>');
							$(ui.draggable[0]).remove();
							
							$('.dropdown-menu-item').hover(function()
							{
								$(this).addClass('dropdown-menu-item-hover');
							}, 
								function()
							{
								$(this).removeClass('dropdown-menu-item-hover');
							}),
							
							$('#hidetip').fadeOut('slow'),
							
							$('.dropdown-menu-item').draggable
							({
								containment: '#dnd',
								drag: function (event, ui)
								{
									$( this ).css({ opacity: 0.5 });
									$( this ).addClass('drag-highlight');
								}
							})
						}
					});	  
					/* design tab END */
					
			}); /* end $function */
		}); /* end (document).ready */
