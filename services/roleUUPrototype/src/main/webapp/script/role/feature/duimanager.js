/**
 * add this feature using com.add() at role.js before role/feature/xmpp is com.add()ed
 * this file should be put into /role/content
 * 
 * 
 * Notes:
 * 
 * How to decide whether to stop the widget to be displayed: 
 * pseudo code: getAllWidget().contains(incomingwidget) && !currentDevice.displayedWidgets.contains(incomingWidget)
 * which means that the incoming widget is blocked to be displayed if it is already in existence and is not displayed on current device.
 * insert the decision code before the widget is GUIed, at /role/panel/widgets.js and /role/content/widgets.js
 * 
 * XMPP connection to the space pubsub node:
 * $(document).bind('xmpp-connected', setupXMPPfortheSpaceHere(){})
 * from the already configured xmpp/xmpp a strophe connection and the pubsubservice is available, get them using xmpp.connection and xmpp.pubsubservice
 * from the ../model/space the spaceId (e.g. kelitest) used for the pubsub node is available
 * as well as a boolean space._isCollaborative used for deciding if this spaceId is the right one
 * (only the collaborative space has xmpp feature and is worthy distributing UIs across devices)
 * use them to config the iwc.Proxy(or an overwritten iwc.Proxy) and iwcProxy.connect().
 * 
 * 
 */
define(["com", "jquery", "../model/user", "../model/space", "domReady", "./widget", "xmpp/xmpp", "xmpp/duixmppportal", 
        "./duiproxies/appmgrproxy", "./duiproxies/devicemgrproxy", "./duiproxies/deviceinfo",
        "../panel/device", "./relay"], 
		function(com, $, user, space, domReady, widgetFeature, xmpp, duiXmpp, 
				appMgr, deviceMgr, deviceInfo, 
				devicePanel, relay){
	duiManager = {
	
		interfaces: ["http://purl.org/role/ui/Feature#"],
		
		isUserLoaded: false,
		pubsubNode: null,
		duiIwcProxy: null,
		currentDeviceName: null,
		currentUserId: null,
		devices: [],
		
		widgets: [],
		widgetsWhiteList: [],
		migratingWidgets: [],
		migratingWidgetStates: {}, //This maps widgetid => state
		duiWidgets: [],
		widgetComponents:[],
		widgetIndex: {},
		
		//global variables and counts for sync
		logOffCount: 0,
		timer: {},
		unSavedWidgets:[],
		dconnCounter: 0,
		isSwitching: false,
		doRefresh: true,
		hasSentNewLogin: false,
		refreshInterval: 16000,
		lastRefresh: null,
		globalRefreshTimer: null,
		currentAppState: null,
		isChecking: false,
		pendingMigration: null,
		migrationTimers: {},
		
//		---------------------------------------------------------------
//		role framework related--------------------------------------- begin
//		---------------------------------------------------------------
		
		refreshGUI: function(){
			console.log("todo: refresh ui");
			if (this.doRefresh){
				this.refreshConnectivities();
				this.doRefresh = false;
				setTimeout(function(){this.doRefresh = true;}.bind(this), 10000);
			}
		},
		
		createCookie: function(name,value,days) {
			var expires = "";
			if (days) {
				var date = new Date();
				date.setTime(date.getTime()+(days*24*60*60*1000));
				expires = "; expires="+date.toGMTString();
			}
			document.cookie = name+"="+value+expires+"; path=/";
		},

		readCookie: function(name) {
			var nameEQ = name + "=";
			var ca = document.cookie.split(';');
			for(var i=0;i < ca.length;i++) {
				var c = ca[i];
				while (c.charAt(0)==' ') c = c.substring(1,c.length);
				if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
			}
			return null;
		},

		eraseCookie: function(name) {
			this.createCookie(name,"",-1);
		},
		
		/**
		 * get user id, device list and previous device based on current state of user model
		 */
		loadUserProfileForRegiDevice: function(){
//			if (user._context.uri.indexOf("cTrvjuLrGC") == -1){
			var userProperties = openapp.resource.context(user._context).properties();
			userjid = userProperties["http://xmlns.com/foaf/0.1/jabberID"].substring("xmpp:".length);
			this.currentUserId = userjid.split("@")[0];
			this.devices = [];
			var dl = openapp.resource.context(user._context).sub(openapp.ns.role + "device").list();
			for (var k=0; k < dl.length; k++)
				this.devices.push(dl[k].uri.substring(dl[k].uri.lastIndexOf("/")+1));
			if (this.readCookie("duiUser") == user._uri.substring(user._uri.lastIndexOf("/")+1))
				this.currentDeviceName = this.readCookie("device");
			this.isUserLoaded = true;
			//blablabla...
//			}
		},
		
		/**
		 * connection watch dog
		 * @param widgetLocation
		 */
		updateConnectivities: function(widgetLocation){
			var that = this;
			if (this.lastRefresh != null && (((new Date()).getTime() - this.lastRefresh) < 15000)){
				
				this.globalRefreshTimer = setTimeout(function(){that.updateConnectivities();}, this.refreshInterval+Math.random()*5000);
				return;
			}
			if (widgetLocation)
				this.refreshConnectivities(widgetLocation);
			else
				this.refreshConnectivities();
			this.globalRefreshTimer = setTimeout(function(){that.updateConnectivities();}, this.refreshInterval+Math.random()*5000);
		},
		
		/**
		 * register the dui xmpp dispatcher to xmpp
		 * @param pubsubservice
		 * @param userpubsubnode
		 * @param connection
		 */
		connectUserXMPP: function(pubsubservice, userpubsubnode, connection){
			if (this.duiIwcProxy == null){
				this.duiIwcProxy = new duiXmpp();
				this.duiIwcProxy.onError = function(msg) {
					console.log("PROXY iwcProxy.onError() " + msg);
				};
				
			}
			this.duiIwcProxy.setPubSubNode(pubsubservice, userpubsubnode); //TODO Changed order as setXmppClient adds a handler with pubsubservice
			this.duiIwcProxy.setXmppClient(connection);
			this.duiIwcProxy.connect(this.duiLiwcDispatcher.bind(this), this.duiXmppDispatcher.bind(this), function(){
				if (this.currentDeviceName != null && this.currentDeviceName != ""){
					deviceMgr.notifyNewDeviceLoad(this.currentDeviceName, function(xmlhttpreq){
						if (xmlhttpreq.status == 404){
							this.eraseCookie("device");
							this.eraseCookie("duiUser");
							//if (xmpp.connection != null && xmpp.isConnected)
								//xmpp.connection.disconnect();
							alert("The device name is not found, switching to anonymous login...");
							location.reload();
						}
					}.bind(this));
					this.hasSentNewLogin = true;
				}
				this.updateConnectivities(true);
				var intent = {
					"action": "DUI_REG_CLIENT",
					"categories": ["DUI"],
					"component": "",
					"data":"",
					"dataType":"",
					"extras":{}
				};
				this.duiIwcProxy.publish(intent);
				if ($("#progressdiv").css("display") != "none"){
			        $("#progressdiv").fadeOut("slow", function(){$("#progressdiv span").text("");});
				}
			}.bind(this));
//			var self = this;
//			setTimeout(function(){self.refreshConnectivities();}, 3000);
//			this.duiIwcProxy.setPubSubNode(pubsubservice, xmpp.pubsubnode);
			this.duiIwcProxy.setSpacePubSubNode(xmpp.pubsubnode);
			//this.lastRefresh = (new Date()).getTime();
		},
		
		/**
		 * Get the device configuration for current space
		 * @param deviceName the device name
		 * @param context the space context
		 * @returns the json object of device configuration
		 */
		getDeviceConfig: function(deviceName, context){
			if (context.data.hasOwnProperty(user._uri)){
			    var dcList = context.data[user._uri][openapp.ns.role+"deviceConfig"];
			    if (dcList != null)
			    for (var p=0; p<dcList.length; p++){
			        var dcUri = dcList[p].value;
			        var dName = dcUri.substring(dcUri.lastIndexOf("/")+1);
			        if (deviceName == dName)
			            if (context.data.hasOwnProperty(dcUri))
			                return context.data[dcUri];
			    }
			}
			return null;
		},
		
		/**
		 * The the id of the widget in the space 
		 * @param context the space context
		 * @param widgetUri widget uri
		 * @returns
		 */
		getWidgetId: function(context, widgetUri){
			if (context.data.hasOwnProperty(widgetUri)){
				var widget = context.data[widgetUri];
				var moduleId = widget[openapp.ns.widget+"moduleId"];
				return moduleId[0].value;
			}
			else
				return null;
		},
		
		/**
		 * look for idle widgets (widget that will not be displayed on any device)
		 * @returns {Array}
		 */
		lookForIdleWidgets: function(){
			var wList = [];
			var res = [];
			for (var k=0; k < this.devices.length; k++){
				var dName = this.devices[k];
				var dCfg = this.getDeviceConfig(dName, space._context);
				if (dCfg != null && dCfg.hasOwnProperty(openapp.ns.role+"displayWidget")){
					var list = dCfg[openapp.ns.role+"displayWidget"];
					for(var p = 0; p < list.length; p++){
						var widgetUri = list[p].value;
						var moduleId = this.getWidgetId(space._context, widgetUri);
						if (moduleId != null)
							wList.push(moduleId);
					}
				}
				
			}
			for (var q = 0; q < this.widgets.length; q++)
				if (wList.indexOf(this.widgets[q]) == -1)
					res.push(this.widgets[q]);
			return res;
		},
		
		/**
		 * shortly after this dui manager feature is added to the framework, this function is called in role.js.<br/>
		 * This function is an initialization. 
		 * The current user, space, widget list, device list and the list of widgets to be displayed are all initialized here.
		 */
		load: function(){
			com.on("http://purl.org/role/ui/Space#", "join", function(space){
				if (space._isCollaborative)
					location.reload();
			});
			
			com.on("http://purl.org/role/ui/Space#", "leave", function(space){
				if (space._isCollaborative)
					location.reload();
			});
			/**
			 * The time to initialize these attributes is at when the space is loaded. 
			 * The parameter of the call back function in com.on("...space#", "load", function(space){}) is the space that is loaded 
			 * and the data structure is in ../model/space.
			 */
			com.on("http://purl.org/role/ui/Space#", "load", function(space){
				if (space._isCollaborative && user._context.uri.indexOf("cTrvjuLrGC") == -1){
					if ($("#progressdiv").css("display") == "none"){
						$("#progressdiv span").text("loading...");
				        $("#progressdiv").fadeIn("fast");
					}
					this.widgets = [];
					
					/*
					 * how to get the list:
					 * @see /role/feature/widget#load()
					*/
					var tools = openapp.resource.context(space._context).sub(openapp.ns.role+"tool").list();
					for (var i = 0; i < tools.length; i ++){
						var tool = tools[i];
						var properties = openapp.resource.context(tool).properties();
						var widgetId = properties[openapp.ns.widget+"moduleId"];
						var widgetUri = tool.uri;
						var widgetTitle = properties[openapp.ns.dcterms+"title"];
						if (typeof widgetId != "undefined"){
							this.widgets.push(widgetId);
							var widgetInfo = {"uri": widgetUri, "title": widgetTitle};
							this.widgetIndex[widgetId]=widgetInfo;
						}
					}
					//get the cookie, 
					if (this.readCookie("duiUser") == user._uri.substring(user._uri.lastIndexOf("/")+1))
						this.currentDeviceName = this.readCookie("device");
					//user id
					var userProperties = openapp.resource.context(user._context).properties();
					userjid = userProperties["http://xmlns.com/foaf/0.1/jabberID"].substring("xmpp:".length);
					this.currentUserId = userjid.split("@")[0];
					//device list
					this.devices = [];
					
					devicePanel.load(this);
					
					var dl = openapp.resource.context(user._context).sub(openapp.ns.role + "device").list();
					var bigWList = [];
					for (var k=0; k < dl.length; k++){
						var dName = dl[k].uri.substring(dl[k].uri.lastIndexOf("/")+1);
						this.devices.push(dName);
						devicePanel.createDevicePanel(dName);
						var dCfg = this.getDeviceConfig(dName, space._context);
						var wList = [];
						if (dCfg != null && dCfg.hasOwnProperty(openapp.ns.role+"displayWidget")){
							var list = dCfg[openapp.ns.role+"displayWidget"];
							for(var p = 0; p < list.length; p++){
								var widgetUri = list[p].value;
								var moduleId = this.getWidgetId(space._context, widgetUri);
								if (moduleId != null){
									wList.push(moduleId);
									bigWList.push(moduleId);
								}
							}
						}
						for (var q = 0; q < wList.length; q++)
							if (space._isMember)
								devicePanel.createWidgetLi(wList[q], this.widgetIndex[wList[q]].title, dName);
					}
					for (var t=0; t<this.widgets.length; t++){
						if (space._isMember && bigWList.indexOf(this.widgets[t])==-1)
							devicePanel.createIdleWidget(this.widgets[t], this.widgetIndex[this.widgets[t]].title, false);
					}
					this.widgetsWhiteList = [];
					if (this.currentDeviceName !=null && this.currentDeviceName != "" && space._isMember){
						var dConfig = this.getDeviceConfig(this.currentDeviceName, space._context);
						if (dConfig != null && dConfig.hasOwnProperty(openapp.ns.role+"displayWidget")){
							var dwList = dConfig[openapp.ns.role+"displayWidget"];
							for(var p = 0; p < dwList.length; p++){
								var widgetUri = dwList[p].value;
								var moduleId = this.getWidgetId(space._context, widgetUri);
								if (moduleId != null)
									this.widgetsWhiteList.push(moduleId);
							}
						}
					}
					else
						for(var k = 0; k < this.widgets.length; k++)
							this.widgetsWhiteList.push(this.widgets[k]);
//					else{
//						if (this.widgets.length > 2){
//							this.widgetsWhiteList.push(this.widgets[0]);
//							this.widgetsWhiteList.push(this.widgets[1]);
//						}else if (this.widgets.length > 0)
//							this.widgetsWhiteList.push(this.widgets[0]);
//					}

					//the device related GUI
					
					if (this.currentDeviceName != null && this.currentDeviceName != ""){
						devicePanel.bumpToTop(this.currentDeviceName);
					}
					for (var j = 0; j < this.devices.length; j++)
						this.guiCreateNewDeviceInfo(this.devices[j]);
					for (j = 0; j < this.widgets.length; j++)
						this.guiCreateWidgetInfo(this.widgets[j]);
					this.getCurrentDeviceInfo();
//					this.refreshConnectivities();
					this.reloadAppState();
					var that = this;
					$("#signOut-a").click(function(){
						if (that.currentDeviceName != null && that.currentDeviceName != "")
							deviceMgr.notifyDeviceOut(that.currentDeviceName);
					});
				}
			}.bind(this));
			

			/**
			 * The DUI manager must listen to activities of widgets, especially when a widget is added.
			 * There are several cases that the function com.add(widget) will be called and the callbacks on this "widget#" "add" event will be triggered.
			 * <ul>
			 * 	<li>
			 * 		On local device initialization, all widgets are created thus com.add(widget) will be called.<br/>
			 *     	But before that, the list of these widgets are already set in the DUI manager by callback in the com.on("space", "load" callback).<br/>
			 *     	Thus in this case, there can't be any unrecorded widget id in (Array)this.widgets when com.add(widget) is called.
			 * 	</li>
			 * 	<li>
			 * 		On switching the activity, the widget components the widget list of the space does not change in this case.<br/>
			 * 		Thus in this case, there can't be any unrecorded widget id in (Array)this.widgets when com.add(widget) is called.
			 * 	</li>
			 * 	<li>
			 * 		On user himself adding new widget from the ROLE GUI, the widget component will be created and added to the ROLE framework directly.
			 * 		Thus in this case, it is sure that in (Array)this.widgets the new widget id is not recorded.
			 * 	</li>
			 * 	<li>
			 * 		On getting informed that other device has added new widget, the message for the information is delivered by XMPP either by another DUI manager or the DUI service on the service.<br/>
			 * 		This XMPP event is surely to be received by the local DUI manager at the first hand and the new widget id is added to (Array)this.widgets before any com.add(widget) is called.<br/>
			 * 		Thus in this case, there can't be any unrecorded widget id in (Array)this.widgets when com.add(widget) is called.
			 * 	</li>
			 * </ul>
			 * Concluded from the cases above:<br/>
			 * Only if the user manually add new widget to the space, 
			 * will not (Array)this.widgets have the widget id before hande.
			 * Thus, in the callback function, it checks whether the widget id is in the list.
			 * If not, it means that user has added a brand new widget to the space locally!!
			 */
			com.on("http://purl.org/role/ui/Widget#", "add", function(widget){
				if (space._isCollaborative&&user._context.uri.indexOf("cTrvjuLrGC") == -1) {
					var recorded = false;
					//for some bugs I didn't find out why, some times this callback is triggered over and over again
					//since this is the client side and this check does not cost much time, i check it
					for (var i = 0; i < this.widgetComponents.length; i++){
						if (this.widgetComponents[i]._component_id == widget._component_id){
							recorded = true;
							break;
						}
					}
					if (!recorded)
						this.widgetComponents.push(widget);
					
					if (this.widgets.indexOf(widget.getRegionWidgetId()) == -1){ //brand new widget appears locally!
						//add it to the list
						var widgetId = widget.getRegionWidgetId();
						this.widgets.push(widgetId);
						var widgetInfo = {"uri": widget.getUri(), "title": widget.getTitle()};
						this.widgetIndex[widgetId] =widgetInfo;
						//add it to the white list
						this.widgetsWhiteList.push(widgetId);
						var currentActivity = {};
						com.one("http://purl.org/role/ui/Activity#", "select", function(activity) {
							currentActivity = activity;
						});
						var uri = currentActivity.getUri();
						appMgr.setNewWidgetLocation(widget.getUri(), uri, this.currentDeviceName, function(xmlhttpreq){
							space.refresh(function(){});
						});
						if (space._isMember && this.currentDeviceName != null && this.currentDeviceName != "")
							devicePanel.createWidgetLi(widgetId, widget.getTitle(), this.currentDeviceName);
						//create the GUI for this widget in DUI manager control panel
						this.guiCreateWidgetInfo(widgetId);
						//update the GUI
						this.guiSetWidgetLocationInfo(widgetId, this.currentDeviceName);
						this.guiSetCurrentDisplayedWidgets(this.widgetsWhiteList);
						var x = document.getElementById("dui-selectDevice").selectedIndex;
						if (x != -1){
							var selectedDevice = document.getElementById("dui-selectDevice").options[x].value;
							if (selectedDevice == this.currentDeviceName)
								this.loadSelectedDeviceConfig();
						}
					}
					else{
						this.widgetIndex[widget.getRegionWidgetId()]={"uri": widget.getUri(), "title": widget.getTitle()};
					}
					devicePanel.setWidgetLiText(widget.getRegionWidgetId(), widget.getTitle());
					this.guiUpdateWidgetTitle(widget.getRegionWidgetId(), widget.getTitle());
				}
				console.log("todo on widget add");
			}.bind(this));
		},
	
		/**
		 * Delete the widget from the device but not from the space
		 * @param widgetId
		 */
		roleRemoveWidgetFromDevice: function(widgetId){
			$("#widget-" + widgetId + "-wrapper").remove();
			$("#sideEntry-" + widgetId).remove();
			console.log("remove widget displayed on role platform");
		},
		
		/**
		 * Add the widget to the device and it is not a brand new widget to the space
		 * @param widgetId
		 */
		roleAddWidgetToDevice: function(widgetId){
			for (var i = 0; i < this.widgetComponents.length; i++){
				if (this.widgetComponents[i]._widget.regionWidgetId == widgetId){
					com.trigger(this.widgetComponents[i], "http://purl.org/role/ui/Widget#", "add");
					widgetFeature._postprocessWidgets();
					/*
					var currentCanvas = $('.widget-wrapper-canvas').attr('id');
					if (currentCanvas != null){
						setTimeout($("widget-"+widgetId+"-wrapper").css("visibility", "hidden"));
					}
					*/
				}
			}
			console.log("display a widget on role platform if it should be displayed in current activity");
		},
		
		/**
		 * this function is called when user click on the close button on the widget div in the role framework
		 * @param widget
		 */
		onUserRemoveWidget: function(widget){
			var widgetId = widget.getRegionWidgetId();
			var i = this.widgets.indexOf(widgetId);
			if (i != -1){
				this.widgets.splice(i, 1);
				if (this.widgetIndex.hasOwnProperty(widgetId))
					delete this.widgetIndex[widgetId];
			}
			else
				alert("keli: impossible list!");
			
			i = this.widgetsWhiteList.indexOf(widgetId);
			if (i != -1)
				this.widgetsWhiteList.splice(i, 1);
			else
				alert("keli: impossible whitelist!");
			
			i = this.duiWidgets.indexOf(widgetId);
			if (i != -1)
				this.duiWidgets.splice(i, 1);
			
			i = this.migratingWidgets.indexOf(widgetId);
			if (i != -1)
				this.migratingWidgets.splice(i, 1);
			
			for (var i = 0; i < this.widgetComponents.length; i++){
				if (this.widgetComponents[i]._component_id == widget._component_id){
					this.widgetComponents.splice(i, 1);
					break;
				}
				
			}
			appMgr.onWidgetRemoved(widget._widget.regionWidgetId, space._uri);
			//remove all infos of the widget from the DUI control panel
			this.guiRemoveWidgetInfo(widgetId);
			this.getCurrentDeviceInfo();
			if (document.getElementById("dui-selectDevice").selectedIndex != -1)
				this.loadSelectedDeviceConfig();
		},
		
		/**
		 * Get all available device profiles
		 * @returns {Array}
		 */
		getAllDeviceProfiles: function(){
			var dList = openapp.resource.context(user._context).sub(openapp.ns.role+"device").list();
			var deviceUris = [];
			var i;
			for (i = 0; i < dList.length; i++){
			    deviceUris.push(dList[i].uri);
			}
			var deviceProfiles = [];
			for (i = 0; i < deviceUris.length; i++){
			    if (user._context.data.hasOwnProperty(deviceUris[i])){
			        var profile = user._context.data[deviceUris[i]];
			        if (!profile.hasOwnProperty(openapp.ns.role+"deviceProfile"))
			        	continue;
			        profile = profile[openapp.ns.role+"deviceProfile"][0].value;
			        profile = JSON.parse(profile);
			        profile = {
			        		"name": deviceUris[i].substring(deviceUris[i].lastIndexOf("/")+1),
			        		"profile": profile
			        };
			        deviceProfiles.push(profile);
			    }
			}
			return deviceProfiles;
		},
		
		/**
		 * get the device profile
		 * @param deviceName
		 * @returns null if no profile is available
		 */
		getDeviceProfile: function(deviceName){
			var dList = openapp.resource.context(user._context).sub(openapp.ns.role+"device").list();
			var deviceUri = null;
			var i;
			for (i = 0; i < dList.length; i++){
				deviceUri = dList[i].uri.substring(dList[i].uri.lastIndexOf("/")+1);
			    if (deviceUri==deviceName){
			    	deviceUri = dList[i].uri;
			    	break;
			    }
			}
			var profile = null;
		    if (deviceUri != null && user._context.data.hasOwnProperty(deviceUri)){
		        profile = user._context.data[deviceUri];
		        if (profile.hasOwnProperty(openapp.ns.role+"deviceProfile")){
			        profile = profile[openapp.ns.role+"deviceProfile"][0].value;
			        profile = JSON.parse(profile);
		        }else
		        	profile = null;
		    }
			return profile;
		},
		
		getDeviceAlias: function(deviceName){
			var dList = openapp.resource.context(user._context).sub(openapp.ns.role+"device").list();
			var deviceUri = null;
			var i;
			for (i = 0; i < dList.length; i++){
				deviceUri = dList[i].uri.substring(dList[i].uri.lastIndexOf("/")+1);
			    if (deviceUri==deviceName){
			    	deviceUri = dList[i].uri;
			    	break;
			    }
			}
			var alias = null;
		    if (deviceUri != null && user._context.data.hasOwnProperty(deviceUri)){
		        alias = user._context.data[deviceUri];
		        if (alias.hasOwnProperty(openapp.ns.dcterms+"name")){
			        alias = alias[openapp.ns.dcterms+"name"][0].value;
		        }else
		        	alias = null;
		    }
			return alias;
		},
		
		getDeviceUri: function(deviceName){
			var dList = openapp.resource.context(user._context).sub(openapp.ns.role+"device").list();
			var deviceUri = null;
			var i;
			for (i = 0; i < dList.length; i++){
				deviceUri = dList[i].uri.substring(dList[i].uri.lastIndexOf("/")+1);
			    if (deviceUri==deviceName){
			    	deviceUri = dList[i].uri;
			    	break;
			    }
			}
			return deviceUri;
		},
		
//		---------------------------------------------------------------
//		role framework related--------------------------------------- end
//		---------------------------------------------------------------		

		
//		---------------------------------------------------------------
//		DUI manager GUI--------------------------------------- begin
//		---------------------------------------------------------------	
		guiUpdateWidgetTitle: function(widgetId, title){
			var lis = document.getElementById("dui-currentDisplayedWidgets-list").getElementsByTagName("li");
			var i = 0;
			for (i = 0; i < lis.length; i++)
				if (lis[i].childNodes[0].nodeValue == widgetId){
					lis[i].childNodes[0].nodeValue = title;
					break;
				}
			var migDiv = document.getElementById("dui-migration-"+widgetId);
			if (migDiv != null)
				migDiv.getElementsByTagName("label")[0].innerHTML = title;
			var options = document.getElementById("dui-selectWidget").options;
			for (i = 0; i < options.length; i++)
				if (options[i].text == widgetId){
					options[i].value = widgetId;
					options[i].text = title;
					break;
				}
		},
		/**
		 * Create GUIs in the migration section for the widget
		 * @param widgetId The widget id
		 */
		guiCreateWidgetMigrationDiv: function(widgetId){
			var migrationDiv = document.getElementById("dui-migration-div");
			var widgetDiv = document.createElement("div");
			var widgetDivId = "dui-migration-"+widgetId;
			widgetDiv.setAttribute("id", widgetDivId);
			widgetDiv.setAttribute("class", "duiInfoDiv");
			var nameLabel = document.createElement("label");
			var currentLabel = document.createElement("label");
			var targetSelect = document.createElement("select");
			nameLabel.setAttribute("class", "widgetInfo");
			currentLabel.setAttribute("class", "widgetInfo");
			targetSelect.setAttribute("class", "widgetInfo");
			currentLabel.setAttribute("id", "dui-migration-"+widgetId+"-current");
			targetSelect.setAttribute("id", "dui-migration-"+widgetId+"-target");
			currentLabel.innerHTML = "unknown";
			nameLabel.innerHTML = widgetId;
			for (var j = 0; j < this.devices.length; j++){
				var newComboItem = document.createElement("option");
				newComboItem.value = this.devices[j];
				var newComboText = document.createTextNode(this.getDeviceAlias(this.devices[j]));
				newComboItem.appendChild(newComboText);
				targetSelect.appendChild(newComboItem);
			}
			widgetDiv.appendChild(nameLabel);
			widgetDiv.appendChild(currentLabel);
			widgetDiv.appendChild(targetSelect);
			migrationDiv.appendChild(widgetDiv);
			$("#"+widgetDivId).click(function(){
				if ($(this).hasClass("selected"))
					$("#dui-migration-div").children().removeClass("selected");
				else{
					$("#dui-migration-div").children().removeClass("selected");
					$(this).addClass("selected");
				}
			});
		},
		
		/**
		 * Remove GUIs from the migration section for the widget
		 * @param widgetId The widget id
		 */
		guiRemoveWidgetMigrationDiv: function(widgetId){
			var migrationWidgetDiv = document.getElementById("dui-migration-" + widgetId);
			if (migrationWidgetDiv != null)
				migrationWidgetDiv.parentNode.removeChild(migrationWidgetDiv);
		},
		
		/**
		 * Create an selction entry for the widget in the widget state section
		 * @param widgetId The widget id
		 */
		guiCreateWidgetSelection: function(widgetId){
			var selectElement = document.getElementById("dui-selectWidget");
			var item = document.createElement("option");
			var widgetTitle = this.widgetIndex[widgetId].title;
			if (widgetTitle == null){
				var text = document.createTextNode(widgetId);
				item.appendChild(text);
			}
			else{
				item.appendChild(document.createTextNode(widgetTitle));
				item.value = widgetId;
			}
			selectElement.appendChild(item);
		},
		
		/**
		 * Remove the selection entry for the widget in the widget state section
		 * @param widgetId The widget id
		 */
		guiRemoveWidgetSelection: function(widgetId){
			var selectWidgetComboBox = document.getElementById("dui-selectWidget");
			for (var i = 0; i < selectWidgetComboBox.options.length; i++){
				if (selectWidgetComboBox.options[i].text == widgetId 
						|| (selectWidgetComboBox.options[i].value != null && selectWidgetComboBox.options[i].value == widgetId)){
					if (selectWidgetComboBox.selectedIndex == i)
						this.guiResetWidgetStates();
					selectWidgetComboBox.remove(i);
					break;
				}
			}
		},
		
		/**
		 * Create related infos about the widget on the DUI manager control panel
		 * @param widgetId The widget regionWidgetId
		 */
		guiCreateWidgetInfo: function(widgetId){
			//update the DUI manager GUI for the new widget
			this.guiCreateWidgetSelection(widgetId);
			//create infos for the widget migration div
			this.guiCreateWidgetMigrationDiv(widgetId);
			//get the widget current location
		},
		
		/**
		 * Remove all related infos about the widget from the DUI manager control panel
		 * @param widgetId The widget regionWidgetId
		 */
		guiRemoveWidgetInfo: function(widgetId){
			//the widget state section
			this.guiRemoveWidgetSelection(widgetId);
			//the migration setion
			this.guiRemoveWidgetMigrationDiv(widgetId);

		},
		
		/**
		 * Set the current device name in the current device info div.
		 * The container of the device name is a label.
		 * @param name The device name which is also the resource part of the xmpp jid if it is not an anonymous xmpp connection
		 */
		guiSetCurrentDeviceName: function(name){
			document.getElementById("dui-currentDeviceName-label").innerHTML = this.getDeviceAlias(name);
		},
		
		/**
		 * Set the current to-be-displayed widgets on this device in the current device info div.
		 * The container of the widgets infos is a ul.
		 * @param widgetIds An array of widget ids.
		 */
		guiSetCurrentDisplayedWidgets: function(widgetIds){
			var ul = document.getElementById("dui-currentDisplayedWidgets-list");
			$(ul).empty();
			for (var i = 0; i < widgetIds.length; i++){
				var widgetId = widgetIds[i];
				var li = document.createElement("li");
				var widgetTitle = this.widgetIndex[widgetId].title;
				if (widgetTitle == null)
					li.appendChild(document.createTextNode(widgetId));
				else
					li.appendChild(document.createTextNode(widgetTitle));
				ul.appendChild(li);
			}
		},
		
		/**
		 * Create all related GUIs for the incoming new device on the DUI control panel.
		 * @param newDeviceName The new device name.
		 */
		guiCreateNewDeviceInfo: function(newDeviceName){
//			select combo box in device config
			var selectElement = document.getElementById("dui-selectDevice");
			var item = document.createElement("option");
			item.value = newDeviceName;
			var alias = this.getDeviceAlias(newDeviceName);
			var text = document.createTextNode(alias);
			item.appendChild(text);
			selectElement.appendChild(item);
//			select combo box for widget migration target device
			var widgetMigrationDivs = new Array();
			widgetMigrationDivs = document.getElementById("dui-migration-div").getElementsByTagName("div");
			var comboBoxId;
			var comboBox;
			var newComboItem;
			var newComboText;
			for (var i = 0; i < widgetMigrationDivs.length; i++){
				//console.log(widgetMigrationDivs[i]);
				comboBoxId = widgetMigrationDivs[i].id + "-target";
				//console.log(comboBoxId);
				comboBox = document.getElementById(comboBoxId);
				newComboItem = document.createElement("option");
				newComboItem.value = newDeviceName;
				newComboText = document.createTextNode(alias);
				newComboItem.appendChild(newComboText);
				comboBox.appendChild(newComboItem);
			}
//			connectivity info div
			if (document.getElementById("dui-conn-div-"+newDeviceName)){
				$("#dui-conn-div-"+newDeviceName).click(function(){
					$("#dui-deviceName-input").val(newDeviceName);
				});
				return;
			}
			var connDiv = document.createElement("div");
			connDiv.setAttribute("id", "dui-conn-div-"+newDeviceName);
			connDiv.setAttribute("class", "duiInfoDiv");
			var nameLabel = document.createElement("label");
			var connLabel = document.createElement("label");
			nameLabel.setAttribute("class", "coupleInfo");
			connLabel.setAttribute("class", "coupleInfo");
			connLabel.setAttribute("id", "dui-conn-info-"+newDeviceName);
			nameLabel.innerHTML = alias;
			connLabel.innerHTML = "unknown";
			connDiv.appendChild(nameLabel);
			connDiv.appendChild(connLabel);
			document.getElementById("dui-conn-div").appendChild(connDiv);
			$("#dui-conn-div-"+newDeviceName).click(function(){
				$("#dui-deviceName-input").val(newDeviceName);
			});
		},
		
		guiChangeDeviceAlias: function(deviceName, alias){
//			select combo box in device config
			var selectElement = document.getElementById("dui-selectDevice");
			var i;
			for (i = 0; i < selectElement.options.length; i++){
				if (selectElement.options[i].value == deviceName){
					selectElement.options[i].text = alias;
					break;
				}
			}
//			select combo box for widget migration target device
			var widgetMigrationDivs = new Array();
			widgetMigrationDivs = document.getElementById("dui-migration-div").getElementsByTagName("div");
			var comboBoxId;
			var comboBox;
			for (i = 0; i < widgetMigrationDivs.length; i++){
				//console.log(widgetMigrationDivs[i]);
				comboBoxId = widgetMigrationDivs[i].id + "-target";
				//console.log(comboBoxId);
				comboBox = document.getElementById(comboBoxId);
				for (var j = 0; j < comboBox.options.length; j++){
					if (comboBox.options[j].value == deviceName){
						comboBox.options[j].text = alias;
						break;
					}
				}
			}
//			connectivity info div
			$("#dui-conn-div-" + deviceName + " label:first").text(alias);
			
			devicePanel.changeDeviceAlias(deviceName, alias);
		},
		
		/**
		 * Remove all related GUIs of the removed device from the DUI control panel.
		 * @param removeDeviceName The device name.
		 */
		guiRemoveDeviceInfo: function(removeDeviceName){
			var selectDeviceComboBox = document.getElementById("dui-selectDevice");
			for (var i = 0; i < selectDeviceComboBox.options.length; i++){
				if (selectDeviceComboBox.options[i].value == removeDeviceName){
					if (selectDeviceComboBox.selectedIndex == i){
						var configStr = "<span>You can select a device and 'reload' device information</span>";
						this.guiSetDeviceConfigInfo(configStr);
					}
					selectDeviceComboBox.remove(i);
					break;
				}
			}
			//the select box for widget migration target devices
			var widgetMigrationDivs = new Array();
			widgetMigrationDivs = document.getElementById("dui-migration-div").getElementsByTagName("div");
			var comboBoxId;
			var comboBox;
			for (var i = 0; i < widgetMigrationDivs.length; i++){
				//console.log(widgetMigrationDivs[i]);
				comboBoxId = widgetMigrationDivs[i].id + "-target";
				comboBox = document.getElementById(comboBoxId);
				for (var t = 0; t < comboBox.options.length; t++){
					if (comboBox.options[t].value == removeDeviceName){
						comboBox.remove(t);
						break;
					}
				}
			}
			//connectivity info div
			var connDiv = document.getElementById("dui-conn-div-"+removeDeviceName);
			if (connDiv != null)
				connDiv.parentNode.removeChild(connDiv);
		},
		
		/**
		 * Reset the connectivities of devices presented in the DUI control panel to "unknown"
		 */
		guiResetDeviceConnectivityInfo: function(){
			var parentDiv = document.getElementById("dui-conn-div");
			var connDivs = parentDiv.getElementsByTagName("div");
			var i = connDivs.length;
			if (connDivs.length > 0)
				while (i--){
					var deviceName = connDivs[i].getElementsByTagName("label")[0].innerHTML;
					if (deviceName.indexOf("anonymous-") == 0)
						parentDiv.removeChild(connDivs[i]);
					else
						connDivs[i].getElementsByTagName("label")[1].innerHTML = "unknown";
				}
		},
		
		/**
		 * set all device as timed out
		 */
		guiSetDeviceTimeout: function(){
			var parentDiv = document.getElementById("dui-conn-div");
			var connDivs = parentDiv.getElementsByTagName("div");
			var i = connDivs.length;
			if (connDivs.length > 0)
				while (i--){
					if (connDivs[i].getElementsByTagName("label")[1].innerHTML == "unknown"){
						connDivs[i].getElementsByTagName("label")[1].innerHTML = "timed out";
						devicePanel.setDeviceOffline(connDivs[i].getElementsByTagName("label")[0].innerHTML);
					}
				}
		},
		
		/**
		 * set the device as timed out
		 * @param deviceName
		 */
		guiSetDeviceOffline: function(deviceName){
			var connLabel = document.getElementById("dui-conn-info-" + deviceName);
			devicePanel.setDeviceOffline(deviceName);
			if (connLabel)
				connLabel.innerHTML = "timed out";
			else{
				var connDiv = document.createElement("div");
				connDiv.setAttribute("id", "dui-conn-div-"+deviceName);
				connDiv.setAttribute("class", "duiInfoDiv");
				var nameLabel = document.createElement("label");
				connLabel = document.createElement("label");
				nameLabel.setAttribute("class", "coupleInfo");
				connLabel.setAttribute("class", "coupleInfo");
				connLabel.setAttribute("id", "dui-conn-info-"+deviceName);
				nameLabel.innerHTML = this.getDeviceAlias(deviceName);
				connLabel.innerHTML = "time out";
				connDiv.appendChild(nameLabel);
				connDiv.appendChild(connLabel);
				document.getElementById("dui-conn-div").appendChild(connDiv);
			}
		},
		
		/**
		 * The GUI of the device connectivity info in the DUI control panel
		 * @param deviceName The device name
		 * @param connInfoStr The string representation for connectivity
		 */
		guiSetDeviceConnectivityInfo: function(deviceName, connInfoStr){
			var connLabel = document.getElementById("dui-conn-info-" + deviceName);
			if (deviceName.indexOf("anonymous-") != 0)
				devicePanel.setDeviceOnline(deviceName);
			if (connLabel)
				connLabel.innerHTML = connInfoStr;
			else{
				var connDiv = document.createElement("div");
				connDiv.setAttribute("id", "dui-conn-div-"+deviceName);
				connDiv.setAttribute("class", "duiInfoDiv");
				var nameLabel = document.createElement("label");
				connLabel = document.createElement("label");
				nameLabel.setAttribute("class", "coupleInfo");
				connLabel.setAttribute("class", "coupleInfo");
				connLabel.setAttribute("id", "dui-conn-info-"+deviceName);
				nameLabel.innerHTML = this.getDeviceAlias(deviceName);
				connLabel.innerHTML = connInfoStr;
				connDiv.appendChild(nameLabel);
				connDiv.appendChild(connLabel);
				document.getElementById("dui-conn-div").appendChild(connDiv);
			}
		},
		
		/**
		 * Display the device config in the device config section
		 * @param configStr The string representation of the device config
		 */
		guiSetDeviceConfigInfo: function(configStr){
			var dvcinfo = null;
			try{
				dvcinfo = JSON.parse(configStr);
				$("#dui-accordion-meta").children().show();
				$("#dui-accordion-profile").children().show();
				$("#dui-accordion-name").text(dvcinfo['deviceName']);
				$("#dui-accordion-owner").text(dvcinfo['owner']);
				$("#dui-accordion-cs").text(dvcinfo['currentSpace']);
				if (dvcinfo.hasOwnProperty("description")){
					$("#dui-accordion-noinfo").hide();
					$("#dui-accordion-dvcinfo").show();
					var dpf = dvcinfo["description"];
					if (dpf.hasOwnProperty("userAgent")){
						$("#dui-accordion-os").hide();
						$("#dui-accordion-browser").hide();
						$("#dui-accordion-ua").show();
						$("#dui-accordion-ua-value").text(dpf.userAgent);
					}
					else{
						$("#dui-accordion-os").show();
						$("#dui-accordion-browser").show();
						$("#dui-accordion-ua").hide();
						$("#dui-accordion-os-value").text(dpf.os);
						var text = dpf.browser;
						if (text != "unknown" && dpf.version != "unknown")
							text = text + " v"+ dpf.version;
						$("#dui-accordion-browser-value").text(text);
					}
				}
				else{
					$("#dui-accordion-noinfo").show();
					$("#dui-accordion-dvcinfo").hide();
				}
				var wList = dvcinfo['widgets'];
				$("#dui-accordion-widgets").empty();
				var container = document.getElementById("dui-accordion-widgets");
				for (var i = 0; i < wList.length; i++){
					var a = document.createElement("a");
					a.setAttribute("href", wList[i]);
					a.appendChild(document.createTextNode(wList[i]));
					var div = document.createElement("div");
					div.appendChild(a);
					container.appendChild(div);
				}
			}catch(e){
				console.log(e);
			}
		},
		
		/**
		 * Clear the container which displays all widget states in the DUI control panel
		 */
		guiResetWidgetStates: function(){
			$("#dui-widgetState-div").empty();
		},
		
		/**
		 * Display one widget state and its value in the DUI control panel
		 * @param stateName The state name
		 * @param stateValue The value
		 */
		guiDisplayWidgetState: function(stateName, stateValue){
			var stateDiv = document.getElementById("dui-widgetState-" + stateName);
			if (stateDiv == null){
				var nameLabel = document.createElement("label");
				var valueLabel = document.createElement("label");
				nameLabel.innerHTML = stateName;
				nameLabel.setAttribute("class", "coupleInfo");
				valueLabel.innerHTML = stateValue;
				valueLabel.setAttribute("id", "dui-widgetState-value-"+stateName);
				valueLabel.setAttribute("class", "coupleInfo");
				
				stateDiv = document.createElement("div");
				stateDiv.setAttribute("id", "dui-widgetState-" + stateName);
				stateDiv.appendChild(nameLabel);
				stateDiv.appendChild(valueLabel);
				document.getElementById("dui-widgetState-div").appendChild(stateDiv);
			}
			else{
				stateDiv.getElementsByTagName("label")[0].innerHTML = stateName;
				stateDiv.getElementsByTagName("label")[1].innerHTML = stateValue;
			}
		},
		
		/**
		 * Clear the container which displays all app states in the DUI control panel
		 */
		guiResetAppStates: function(){
			$("#dui-appStates").empty();
		},
		
		/**
		 * Display one app state and its value in the DUI control panel
		 * @param stateName The state name
		 * @param stateValue The value
		 */
		guiDisplayAppState: function(stateName, stateValue){
			var stateDiv = document.getElementById("dui-appState-"+stateName);
			if (stateDiv == null){
				var nameLabel = document.createElement("label");
				var valueLabel = document.createElement("label");
				nameLabel.innerHTML = stateName;
				nameLabel.setAttribute("class", "coupleInfo");
				valueLabel.innerHTML = stateValue;
				valueLabel.setAttribute("id", "dui-appState-value-" + stateName);
				valueLabel.setAttribute("class", "coupleInfo");
				
				stateDiv = document.createElement("div");
				stateDiv.setAttribute("id", "dui-appState-"+stateName);
				stateDiv.appendChild(nameLabel);
				stateDiv.appendChild(valueLabel);
				document.getElementById("dui-appStates").appendChild(stateDiv);
			}
			else{
				stateDiv.getElementsByTagName("label")[0].innerHTML = stateName;
				stateDiv.getElementsByTagName("label")[1].innerHTML = stateValue;
			}
		},
		
		/**
		 * Set the widget location in the DUI control panel
		 * @param widgetId The widget regionWidgetId
		 * @param deviceName The device in which the widget lies currently
		 */
		guiSetWidgetLocationInfo: function(widgetId, deviceName){
			var currentLabel = document.getElementById("dui-migration-"+widgetId+"-current");
			if (currentLabel == null){
				this.guiCreateWidgetMigrationDiv(widgetId);
				currentLabel = document.getElementById("dui-migration-"+widgetId+"-current");
			}
			currentLabel.innerHTML = (deviceName == null || deviceName == "")?"not displayed": this.getDeviceAlias(deviceName);
		},
		
		
		
//		---------------------------------------------------------------
//		DUI manager GUI--------------------------------------- end
//		---------------------------------------------------------------	


		
//		---------------------------------------------------------------
//		request from dui GUI--------------------------------------- begin
//		---------------------------------------------------------------		
		
		changeDeviceAlias: function(deviceName){
			var name = this.getDeviceAlias(deviceName);
			var newName = prompt("Please enter the new name of the device", name);
			if (newName != null && newName != "" && newName != name)
				deviceMgr.changeDeviceAlias(deviceName, newName, function(xmlhttp){});
		},
		
		getCurrentDeviceInfo: function (){
			if (this.currentDeviceName != null && this.currentDeviceName != ""){
				var requestTimer = setTimeout(function(){alert("you may be disconnected from the server");}, 10000);
				//ask the device mgr for infos
				deviceMgr.getCurrentDeviceInfo(this.currentDeviceName, function(xmlhttp) {
					//console.log("response current device info" + xmlhttp.readyState + ", " + xmlhttp.status);
					if (xmlhttp.status == 200) {
						clearTimeout(requestTimer);
						var dwList = new Array();
						console.log(xmlhttp.responseText);
						dwList = xmlhttp.responseText.split(",");
						if (dwList.length == 1 && dwList[0]== "")
							dwList = [];
						//set the data
						this.createCookie("device", this.currentDeviceName, 30);
						this.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 10);
						this.guiSetCurrentDeviceName(this.currentDeviceName);
						this.widgetsWhiteList = dwList;
						this.guiSetCurrentDisplayedWidgets(this.widgetsWhiteList);
					}
					else if (xmlhttp.status == 404){
						alert("This device has been removed by user previously.");
						this.eraseCookie("device");
						this.eraseCookie("duiUser");
//						xmpp.connection.disconnect();
						location.reload();
					}
				}.bind(this));
			}
			else{
				//anonymous resource part and the whitelist is as default
				this.guiSetCurrentDeviceName("anonymous");
				this.guiSetCurrentDisplayedWidgets(this.widgetsWhiteList);
			}
		},
	
		reconnect: function(){
			location.reload();
		},
		
		newDevice: function(deviceName){
			var newDeviceName = deviceName || document.getElementById("dui-deviceName-input").value;
			/*
			if (newDeviceName != null && newDeviceName != "" && newDeviceName.indexOf(" ") !== -1){
				alert("Currently does not support 'space' in device names. Please use a single word instead.");
				return;
			}*/
			if (newDeviceName == null || newDeviceName == ""){
				alert("The device name can not be empty!");
				return;
			}
			deviceMgr.addDevice(newDeviceName, function(xmlhttp){
				if (xmlhttp.status == 202){
					alert("device " + newDeviceName + " exists.");
					return;
				}
				/*
				if (xmlhttp.status == 201)
					if (this.devices.indexOf(newDeviceName) == -1){
						this.devices.push(newDeviceName);
						this.guiCreateNewDeviceInfo(newDeviceName);
						devicePanel.createDevicePanel(newDeviceName);
					}
				*/
			}.bind(this));
		},
	
		loadSelectedDeviceConfig: function(){
			console.log("load selected device config");
			var x = document.getElementById("dui-selectDevice").selectedIndex;
			if (x == -1){
				//alert("nothing is selected");
				return;
			}
			var deviceName = document.getElementById("dui-selectDevice").options[x].value;
			deviceMgr.getDeviceInfos(deviceName, function(xmlhttp){
				if (xmlhttp.status == 200){
					//on response
					var configStr = xmlhttp.responseText;
					this.guiSetDeviceConfigInfo(configStr);
				}
			}.bind(this));
		},
		
		removeDevice: function(dName){
			var that = this;
			var removeDeviceName = dName || document.getElementById("dui-deviceName-input").value;
			if (this.currentDeviceName != null && this.currentDeviceName == removeDeviceName){
				if (!confirm("Are you sure you want to remove your current device?"))
					return;
			}
			else
				if (!confirm("Are you sure you want to remove your device? It might be active."))
					return;
			deviceMgr.removeDevice(removeDeviceName, function(xmlhttp){
				if (xmlhttp.status == 404){
					alert("no such device " + removeDeviceName + " is found");
					return;
				}
				if (xmlhttp.status == 200){
					if (this.currentDeviceName != null && removeDeviceName == this.currentDeviceName){
						//if this is the device that is removed, ask for DUI widget to save the state and after 15 sec log off
						this.eraseCookie("device");
						this.eraseCookie("duiUser");
						this.logOffCount = this.duiWidgets.length;
						//if there are dui widgets, save them
						if (this.logOffCount > 0){
							intent = {
									"action": "DUI_LOG_OFF",
									"component": "",
									"categories": ["DUI"],
									"data": "",
									"dataType":"",
									"extras": {}
							};
							this.duiIwcProxy.publish(intent);
							this.timer = setTimeout(function(){
								if (that.currentDeviceName != null && that.currentDeviceName != "")
									deviceMgr.notifyDeviceOut(that.currentDeviceName);
								location = location.protocol + "//" + location.host + "/:authentication?return=%2F&action=signout";
							}, 15000);
						}
						//if there is no dui widget, sign out
						else{
							if (that.currentDeviceName != null && that.currentDeviceName != "")
								deviceMgr.notifyDeviceOut(that.currentDeviceName);
							location = location.protocol + "//" + location.host + "/:authentication?return=%2F&action=signout";
						}
					}
					else{
						//the list
						var x = this.devices.indexOf(removeDeviceName);
						if (x != -1){
							this.devices.splice(x, 1);
							//remove the GUIs from the DUI control panel
							this.guiRemoveDeviceInfo(removeDeviceName);
						}
						space.refresh(function(){});
					}
				}
			}.bind(this));
		},
		
		
		refreshConnectivities: function(widgetLocation){
			var that = this;
			this.guiResetDeviceConnectivityInfo();
			this.dconnCounter = 0;
			var requestTimer = setTimeout(function(){
				if (that.globalRefreshTimer != null)
					clearTimeout(that.globalRefreshTimer);
				alert("you may be disconnected from the server");
			}, 10000);
			this.isChecking = true;
			setTimeout(function(){
				that.isChecking = false;
				that.guiSetDeviceTimeout();
				if (widgetLocation)
					that.getWidgetsCurrentLocations();
				if (that.pendingMigration != null){
					that.migrateWidget(that.pendingMigration.widgetId, that.pendingMigration.targetDevice, that.pendingMigration.sourceDevice);
					that.pendingMigration = null;
				}
				if ($("#progressdiv").css("display") != "none"){
			        $("#progressdiv").fadeOut("slow", function(){$("#progressdiv span").text("");});
				}
			}, 4000);
			deviceMgr.checkConnectivities(function(xmlhttp){
				if (xmlhttp.status == 200){
					clearTimeout(requestTimer);
				}
			}.bind(this));
		},
		
		saveConfig: function(dName){
			console.log("save config");
			var deviceName = null;
			if (typeof dName == "undefined"){
				var x = document.getElementById("dui-selectDevice").selectedIndex;
				if (x == -1){
					alert("nothing is selected");
					return;
				}
				deviceName = document.getElementById("dui-selectDevice").options[x].value;
			}
			else
				deviceName = dName;
			deviceMgr.setDeviceProfile(deviceName);
		},
		toDevice: null,
		/**
		 * need further thinking after other things are integrated to the framework.
		 */
		switchToSelectedDevice: function(){
			var x = document.getElementById("dui-selectDevice").selectedIndex;
			if (x == -1){
				alert("nothing is selected");
				return;
			}
			var deviceName = document.getElementById("dui-selectDevice").options[x].value;
			if (deviceName != this.currentDeviceName){
				deviceMgr.trySwitchDevice(deviceName, this.currentDeviceName, space._uri,function(xmlhttp){
					if (xmlhttp.readyState == 4 && xmlhttp.status == 202){
						//need confirm coz switch will cause another device loses it configurations
						var cf = confirm("device " + deviceName + " may be active in another space " + xmlhttp.responseText + "."
												+ "\r\n Press'OK' to continue or 'Cancel' to abort.");
						if (cf == true)
							this.forceSwitchDevice(deviceName);
					}
					if (xmlhttp.readyState == 4 && xmlhttp.status == 200){
						//switch
						//if there are dui widgets, ask to save the state
						if (this.duiWidgets.length > 0){
							var duiintent = {};
							this.toDevice = deviceName;
							this.unSavedWidgets = [];
							for (var j = 0; j < this.duiWidgets.length; j++){
								this.unSavedWidgets.push(this.duiWidgets[j]);
							}
							for (var i = 0; i < this.duiWidgets.length; i++){
								duiintent = {
									"action": "DUI_GET_WS",
									"component": "duiclient-"+this.duiWidgets[i],
									"categories": ["DUI"],
									"data": "",
									"dataType":"",
									"extras": {"widgetId": this.duiWidgets[i]}
								};
								this.duiIwcProxy.publish(duiintent);
								setTimeout(function(){
									this.eraseCookie("device");
									this.eraseCookie("duiUser");
//									xmpp.connection.disconnect();
									this.createCookie("device", deviceName, 30);
									this.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 30);
									location.reload();
								}, 15000);
							}
						}
						//if there are on dui widget, then nothing is to be saved and just switch
						else{
							this.eraseCookie("device");
							this.eraseCookie("duiUser");
//							xmpp.connection.disconnect();
							this.createCookie("device", deviceName, 30);
							this.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 30);
							location.reload();
						}
					}
				}.bind(this));
			}
		},
		
		forceSwitchDevice: function(deviceName){
			if (this.currentDeviceName == null || this.currentDeviceName == "")
				this.isSwitching = true;
			deviceMgr.confirmSwitchDevice(deviceName, this.currentDeviceName, space._uri);
		},
		
		reloadWidgetState: function(){
			var x = document.getElementById("dui-selectWidget").selectedIndex;
			if (x == -1){
				//alert("nothing is selected");
				return;
			}
			var val = document.getElementById("dui-selectWidget").options[x].value;
			var widgetId = val==null?document.getElementById("dui-selectWidget").options[x].text:val;
			var widgetUri = this.widgetIndex[widgetId].uri;
			appMgr.getWidgetState(widgetUri, function(xmlhttp){
				if (xmlhttp.status == 200){
					//refresh the widget state div
					var widgetStates = {};
					if (xmlhttp.responseText != null && xmlhttp.responseText != "")
						widgetStates = JSON.parse(xmlhttp.responseText);
					//clear the widget state info div
					this.guiResetWidgetStates();
					var self = this;
					$.each(widgetStates, function (stateName, stateValue){
						//display this widget state in DUI control panel
						self.guiDisplayWidgetState(stateName, stateValue);
					});
				}
			}.bind(this));
		},
		
		reloadAppState: function(){
			appMgr.loadAppState(function(xmlhttp){
				if (xmlhttp.status == 200){
					var appStates = {};
					if (xmlhttp.responseText != null && xmlhttp.responseText != "")
						try{
							appStates = JSON.parse(xmlhttp.responseText);
						}catch(err){
							console.log("error on parsing json app state");
							//reset the app state to empty
							appMgr.setAppState(JSON.stringify(appStates));
						}finally{
							this.currentAppState = appStates;
							this.guiResetAppStates();
							var self = this;
							$.each(appStates, function(stateName, stateValue){
								self.guiDisplayAppState(stateName, stateValue);
							});
						}
				}
			}.bind(this));
		},
		
		getWidgetsCurrentLocations: function(){
			var migrationDiv = document.getElementById("dui-migration-div");
			var widgetDivs = migrationDiv.getElementsByTagName("div");
		//	console.log(widgetDivs.length);
		//	console.log(widgetDivs);
			for (var i = 0; i < widgetDivs.length; i++){
				var widgetId = widgetDivs[i].id;
				widgetId = widgetId.substring("dui-migration-".length);
				this.getWidgetCurrentLocation(widgetId);
			}
		},
		
		getWidgetCurrentLocation: function(widgetId){
//			console.log("get widget location: " + widgetId);
			var widgetUri = this.widgetIndex[widgetId].uri;
			appMgr.getWidgetCurrentLocation(widgetUri, function(xmlhttp){
				if (xmlhttp.status == 200){
					var devices = xmlhttp.responseText.split(",");
					var deviceName = devices[0];
					this.guiSetWidgetLocationInfo(widgetId, deviceName);
					var li = document.getElementById("dui-device-panel-widget-"+widgetId);
					if (li == null){
						var title = this.widgetIndex[widgetId].title == null? widgetId: this.widgetIndex[widgetId].title;
						devicePanel.createWidgetLi(widgetId, title, deviceName);
					}
				}
			}.bind(this));
		},
		initMigrateWidgetRelay: function(widgetId, targetDeviceName, sourceDevice){
		  console.log("jab:init called");
     var params = {
                  "widgetId": widgetId,
                  "source":sourceDevice,
                  "target": targetDeviceName,
                  "isDui": false,
                  "user": this.currentUserId,
                  "spaceUri": space._uri
                };
                migIntent = $build("intent", {xmlns: "http://dbis.rwth-aachen.de/~hocken/da/xsd/Intent"})
                  .c("component",{}).up()
                  .c("sender",{}).t('jab').up()
                  .c("action",{}).t('DUI_INIT_MIG').up()
                 .c("data",{mime:""}).up()
                 .c("categories",{})
                  .c("category").t("DUI").up()
                 .up()
                 .c("extras").t(JSON.stringify(params));        
      try {
        relay.relayConnection.pubsub.publishItem(relay.pubsubservice, relay.userpubsubnode, migIntent.tree(), function(ev){console.log("Publish: %o",ev);return true;});
      } catch(e){
        console.log("Relay:DUI_INIT_MIG. %o",e);
      }
      //Testing only://xmpp.connection.pubsub.publishItem(xmpp.pubsubservice, xmpp.userpubsubnode, migIntent.tree(), function(ev){console.log("Publish: %o",ev);return true;});
		},
		migrateWidget: function(widgetId, targetDeviceName, sourceDevice){
			var widgetUri = this.widgetIndex[widgetId].uri;
			if (this.isChecking){
				this.pendingMigration = {
					"widgetId": widgetId,
					"targetDevice": targetDeviceName,
					"sourceDevice": sourceDevice
				};
				if ($("#progressdiv").css("display") == "none"){
					$("#progressdiv span").text("checking device connection...");
			        $("#progressdiv").fadeIn("fast");
				}
				return;
			}
			if ($("#progressdiv").css("display") != "none"){
		        $("#progressdiv").fadeOut("slow", function(){$("#progressdiv span").text("");});
			}
			if(relay.isConnected) {
			  this.initMigrateWidgetRelay(widgetId, targetDeviceName, sourceDevice);
			}
			appMgr.initMigrateWidget(widgetUri, targetDeviceName, function(xmlhttp){
				if (xmlhttp.status == 403){
					console.log("you are not a member of the space");
					var container = $("#dui-device-panel-idleWidgets");
					if (sourceDevice != "")
						container = $("#dui-device-ul-"+sourceDevice);
					if (container.attr('id') != $("#dui-device-panel-widget-"+widgetId).parent().attr('id'))
						devicePanel.fallback(widgetId, sourceDevice, "please join the space first");
				}
				if (xmlhttp.status == 404){
					reason = xmlhttp.responseText;
					if (reason.indexOf("device")!=-1){
						var container = $("#dui-device-panel-idleWidgets");
						if (sourceDevice != "")
							container = $("#dui-device-ul-"+sourceDevice);
						if (container.attr('id') != $("#dui-device-panel-widget-"+widgetId).parent().attr('id'))
							devicePanel.fallback(widgetId, sourceDevice, "target device does not exist");
						this.guiRemoveDeviceInfo(targetDeviceName);
						console.log("device has already been removed");
					}
					if (reason.indexOf("widget")!=-1){
						this.guiRemoveWidgetInfo(widgetId);
						console.log("widget no longer exists");
					}
				}
			}.bind(this));
			
			var that = this;
			$("#dui-device-panel-widget-"+widgetId).addClass("migrating");
			var timer = setTimeout(function(){
				if (that.widgets.indexOf(widgetId) == -1)
					return;
				var dl = openapp.resource.context(user._context).sub(openapp.ns.role + "device").list();
				var dName = null;
				var doBreak = false;
				for (var k=0; k < dl.length; k++){
					dName = dl[k].uri.substring(dl[k].uri.lastIndexOf("/")+1);
					var dCfg = that.getDeviceConfig(dName, space._context);
					if (dCfg != null && dCfg.hasOwnProperty(openapp.ns.role+"displayWidget")){
						var list = dCfg[openapp.ns.role+"displayWidget"];
						for(var p = 0; p < list.length; p++){
							var widgetUri = list[p].value;
							var moduleId = that.getWidgetId(space._context, widgetUri);
							if (moduleId == widgetId){
								doBreak = true;
								break;
							}
						}
					}
					if (doBreak)
						break;
					else
						dName = "";
				}
				if (dName != null && dName != targetDeviceName){
					var container = $("#dui-device-panel-idleWidgets");
					if (sourceDevice != "")
						container = $("#dui-device-ul-"+sourceDevice);
					if (container.attr('id') != $("#dui-device-panel-widget-"+widgetId).parent().attr('id'))
						devicePanel.fallback(widgetId, sourceDevice, "source device timed out");
				}
				$("#dui-device-panel-widget-"+widgetId).removeClass("migrating");
				if (that.migrationTimers[widgetId])
					delete that.migrationTimers[widgetId];
			}, 7000);
			this.migrationTimers[widgetId] = timer;
		},
		
		migrateSelected: function(){
			var that = this;
			var migDivs = $("#dui-migration-div").children();
			$.each(migDivs, function(i, migDiv){
				var id = migDiv.id;
				if ($("#"+id).hasClass("selected")){
					widgetId = id.substring("dui-migration-".length);
					var selectElement = migDiv.getElementsByTagName("select")[0];
					var x = selectElement.selectedIndex;
					var targetDeviceName = selectElement.options[x].value;
					var currentLocation = migDiv.getElementsByTagName("label")[1].innerHTML;
					if (currentLocation != targetDeviceName){
						that.migrateWidget(widgetId, targetDeviceName, null);
					}
					$("#"+id).removeClass("selected");
				}
			});
		},

//		---------------------------------------------------------------
//		request from dui GUI--------------------------------------- end
//		---------------------------------------------------------------				
		
//		---------------------------------------------------------------
//		response from dui xmpp and liwc related--------------------------------------- begin
//		---------------------------------------------------------------	
		
		/**
		 * The callback dispatcher for Liwc intents
		 */
		duiLiwcDispatcher: function(intent){
			if (intent.action == "DUI_CLIENT_OK"){
				this.onRcvDUIClientCreated(intent);
				return;
			}
			if (intent.action == "DUI_PUB_USER"){
				this.duiIwcProxy.publishIntent(intent);
				return;
			}
			if (intent.action == "DUI_REQ_WS"){
				this.updateWidgetState(intent.extras.widgetId, false);
			}
			if (intent.action == "DUI_WS_MIG"){
				this.onRcvWidgetStateForMig(intent);
				return
			}
			if (intent.action == "DUI_PRE_MIG_OK"){
				this.onRcvWidgetMigrationPrepared(intent);
				return;
			}
			if (intent.action == "DUI_SAVE_WS"){
				var widgetId = intent.extras.widgetId;
				var widgetUri = this.widgetIndex[widgetId].uri;
				var jsonWidgetStates = intent.extras["widgetStates"];
				try{
					JSON.stringify(jsonWidgetStates);
				}catch(err){
					console.log("error on parsing json widget state");
					return;
				}
				appMgr.setWidgetState(widgetUri, jsonWidgetStates, function(xmlhttp){
					if (xmlhttp.status == 404){
						console.log("widget " + widgetId + " not found");
						this.guiRemoveWidgetInfo(widgetId);
						for (var i = 0; i < this.widgetComponents.length; i++){
							if (this.widgetComponents[i]._widget.regionWidgetId == widgetId)
								com.remove(this.widgetComponents[i]);
						}
					}
				}.bind(this));
				return;
			}
			if (intent.action == "DUI_WS_LOGOFF"){
				this.onRcvLogOff(intent);
				return;
			}
			if (intent.action == "DUI_WS"){
				this.onRcvWidgetStateForSwitch(intent);
				return;
			}
			if (intent.action == "DUI_SET_AS"){
				this.onSetAppState(intent);
				return;
			}
			if (intent.action == "DUI_GET_AS"){
				if (this.currentAppState != null){
					var i = {
							"action": "DUI_AS",
							"categories": ["DUI"],
							"component": intent.sender,
							"data":"",
							"dataType":"",
							"extras":{"appStates": this.currentAppState}
					};
					this.duiIwcProxy.publish(i);
				}
				return;
			}
			console.log("invalid intent on DUI manager:");
			console.log(intent);
		},
		
		/**
		 * The callback dispatcher for incoming intents via xmpp 
		 * @param intent
		 */
		duiXmppDispatcher: function(intent){
			/**
			 * accepted intents are in two gourps: 	intent.extras.user undifined(broadcast) 
			 * 					 					|| intent.extras.user == this.currentUserId  
			 *
			 */
			if (typeof intent.extras.user == "undefined"){
				if (intent.action == "DUI_NEW_WIDGET"){
					this.onXMPPRcvNewWidget(intent);
					return;
				}
				if (intent.action == "DUI_PUB_USER"){
					if (intent.data != null && intent.dataType == "application/json")
						this.duiIwcProxy.publish(JSON.parse(intent.data));
					return;
				}
				if (intent.action == "DUI_REMOVE_WIDGET"){
					this.onXMPPRcvRemoveWidget(intent);
					return;
				}				
				if (intent.action == "DUI_APP_STATE_CHANGE"){
					this.onXMPPRcvAppStateChange(intent);
					return;
				}
			}
			else if(intent.extras.user == this.currentUserId){
				if (intent.action == "DUI_CONN_CHK"){
					this.onXMPPCheckConnectivity(intent);
					return;
				}				
				if (intent.action == "DUI_CONN_CONFIRM"){
					this.onXMPPRcvDeviceConnected(intent);
					return;
				}
				
				if (intent.action == "DUI_NOT_CONN"){
					this.onXMPPRcvDeviceNotConnected(intent);
					return;
				}
								
				if (intent.action == "DUI_INIT_MIG"){
					this.onXMPPInitMigration(intent); 
					return;
				}				
				if (intent.action == "DUI_PERFORM_MIG"){
					this.onXMPPPerformMigration(intent);
					return;
				}
				if (intent.action == "DUI_SWITCH_DEVICE"){
					this.onXMPPRcvPerformSwitchDevice(intent);
					return;
				}
				if (intent.action == "DUI_DVC_LOAD"){
					this.onXMPPRcvDvcLoad(intent);
					return;
				}
				if (intent.action == "DUI_CHANGE_ALIAS"){
					this.onXMPPRcvChangeAlias(intent);
					return;
				}
				if (intent.action == "DUI_NEW_DEVICE"){
					this.onXMPPRcvNewDevice(intent);
					return;
				}				
				if (intent.action == "DUI_REMOVE_DEVICE"){
					this.onXMPPRcvRemoveDevice(intent);
					return;
				}				
				if (intent.action == "DUI_NEW_PROFILE"){
					this.onXMPPRcvNewProfile(intent);
					return;
				}
			}
			
			console.log("invalid intent on DUI manager:");
			console.log(intent);
		},
		
		onXMPPRcvChangeAlias: function(intent){
			var deviceName = intent.extras.device;
			var alias = intent.extras.alias;
			if (deviceName == this.currentDeviceName){
				$("#dui-currentDeviceName-label").text(alias);
			}
			this.guiChangeDeviceAlias(deviceName, alias);
			var userUri = "http://" + document.location.host + "/user";
			openapp.resource.get(userUri, function(context){
				user._context = context;
				user._uri = context.uri;
			});
		},
		
		updateWidgetState: function(widgetId, isForMigration){
			var widgetUri = this.widgetIndex[widgetId].uri;
			appMgr.getWidgetState(widgetUri, function(xmlhttp){
				if (xmlhttp.status == 200){
					var widgetStates = {};
					if (xmlhttp.responseText != null && xmlhttp.responseText != "")
						widgetStates = JSON.parse(xmlhttp.responseText);
					var intent = {
							"action": "DUI_UPDATE_STATE",
							"categories": ["DUI"],
							"component": "duiclient-"+widgetId,
							"data":"",
							"dataType":"",
							"extras":{"widgetId": widgetId}
					};
					if (typeof isForMigration != "undefined")
						intent.extras["isForMigration"] = isForMigration;
					else intent.extras["isForMigration"] = false;
					intent.extras["widgetStates"] = widgetStates;
					if (this.currentAppState != null)
						intent.extras["appStates"] = this.currentAppState;
					
					//decide whether to update the widget state GUIs in the DUI control panel
					var x = document.getElementById("dui-selectWidget").selectedIndex;
					var doUpdate = false;
					if (x != -1){
						var val = document.getElementById("dui-selectWidget").options[x].value;
						var selectedWidget = val==null?document.getElementById("dui-selectWidget").options[x].text:val;
						if (selectedWidget == widgetId){
							//clear the widget state div for display new states if the selected widget is this one
							this.guiResetWidgetStates();
							//and mark as "update needed"
							doUpdate = true;
						}
					}
					if (doUpdate){
						this.guiResetWidgetStates();
						var self = this;
						$.each(widgetStates, function(stateName, stateValue){
							self.guiDisplayWidgetState(stateName, stateValue);
						}.bind(this));
					}
					//asks the widget to update its state
					this.duiIwcProxy.publish(intent);
				}
			}.bind(this));
		},
		
		onXMPPRcvNewProfile: function(intent){
			var userUri = "http://" + document.location.host + "/user";
			var deviceName = intent.extras.deviceName;
			openapp.resource.get(userUri, function(context){
				user._context = context;
				user._uri = context.uri;
				devicePanel.popupInfo("The system information is set to device " + deviceName + ".");
			});
		},
		
		onXMPPRcvDvcLoad: function(intent){
//			var that = this;
			if (intent.extras.device == this.currentDeviceName && !this.hasSentNewLogin){
				this.eraseCookie("device");
				this.eraseCookie("duiUser");
				this.logOffCount = this.duiWidgets.length;
				//if there are dui widget, save the state first
				if (this.logOffCount > 0){
					intent = {
							"action": "DUI_LOG_OFF",
							"component": "",
							"categories": ["DUI"],
							"data": "",
							"dataType":"",
							"extras": {}
					};
					this.duiIwcProxy.publish(intent);
					this.timer = setTimeout(function(){
//						if (that.currentDeviceName != null && that.currentDeviceName != "")
//							deviceMgr.notifyDeviceOut(that.currentDeviceName);
						location = location.protocol + "//" + location.host + "/:authentication?return=%2F&action=signout";
					}, 15000);
				}
				else{
//					if (that.currentDeviceName != null && that.currentDeviceName != "")
//						deviceMgr.notifyDeviceOut(that.currentDeviceName);
					location = location.protocol + "//" + location.host + "/:authentication?return=%2F&action=signout";
				}
			}
			else{
				this.guiSetDeviceConnectivityInfo(intent.extras.device, "connected at space " + intent.extras.space);
				if (intent.extras.device == this.currentDeviceName && this.hasSentNewLogin)
					this.hasSentNewLogin = false;
			}
		},
		
		onXMPPInitMigration: function(intent){
			if (intent.extras.spaceUri != space._uri)
				return;
			var source = intent.extras.source;
			var target = intent.extras.target;
			var widgetId = intent.extras.widgetId;
			if (source == this.currentDeviceName){
				//2 client req: 	if is source device where the widget lies, acquire widget states
				if (this.duiWidgets.indexOf(widgetId) != -1){
					intent = {
						"action": "DUI_GET_WS",
						"component": "duiclient-"+widgetId,
						"categories": ["DUI"],
						"data": "",
						"dataType":"",
						"extras": {"widgetId": widgetId, "target": target} //has target implies that it is for migration
					};
					this.duiIwcProxy.publish(intent);
				}else{
					var widgetUri = this.widgetIndex[widgetId].uri;
					appMgr.changeWidgetLocation(widgetUri, this.currentDeviceName, target, false, function(xmlhttp){
						if (xmlhttp.status == 404){
							this.guiRemoveWidgetInfo(widgetId);
							console.log("widget does not exist any longer");
						}
					}.bind(this));
				}
			}
		},
		migrateWidgetRelay: function(widgetId, targetDeviceName, sourceDevice, jsonState){
		  console.log("jab:perform");
      var params = {
        "widgetId": widgetId,
        "source":sourceDevice,
        "target": targetDeviceName,
        "isDui": true,
        "user": this.currentUserId,
        "spaceUri": space._uri,
        "state": jsonState
      };
      migIntent = $build("intent", {xmlns: "http://dbis.rwth-aachen.de/~hocken/da/xsd/Intent"})
                  .c("component",{}).up()
                  .c("sender",{}).t('jab').up() //TODO Not true
                  .c("action",{}).t('DUI_PERFORM_MIG').up()
                 .c("data",{mime:""}).up()
                 .c("categories",{})
                  .c("category").t("DUI").up()
                 .up()
                 .c("extras").t(JSON.stringify(params));                
     try{
      relay.relayConnection.pubsub.publishItem(relay.pubsubservice, relay.userpubsubnode, migIntent.tree(), function(ev){console.log("Publish: %o",ev);return true;});
     } catch (e){
      console.log("Relay error: DUI_PERFORM_MIG. %o",e);
     }
     //Testing only://xmpp.connection.pubsub.publishItem(xmpp.pubsubservice, xmpp.userpubsubnode, migIntent.tree(), function(ev){console.log("Publish: %o",ev);return true;});
		},
		onRcvWidgetStateForMig: function(intent){
			var widgetId = intent.extras.widgetId;
			var targetDeviceName = intent.extras.target;
			var widgetUri = this.widgetIndex[intent.extras.widgetId].uri;
			var jsonWidgetStates = intent.extras["widgetStates"];
			try{
				JSON.stringify(jsonWidgetStates);
			}catch(err){
				console.log("error on parsing json widget state");
				return;
			}
			if(relay.isConnected) {
			  this.migrateWidgetRelay(widgetId, targetDeviceName, this.currentDeviceName, jsonWidgetStates); //TODO Leave logic at server
			}
			//appMgr.changeWidgetLocation(widgetUri, this.currentDeviceName, targetDeviceName, true, function(xmlhttp){}.bind(this));
			appMgr.setWidgetState(widgetUri, jsonWidgetStates, function(xmlhttp){
				var result = xmlhttp.status;
				if(result==200){
					var widgetUri = this.widgetIndex[widgetId].uri;
					appMgr.changeWidgetLocation(widgetUri, this.currentDeviceName, targetDeviceName, true, function(xmlhttp){
						if (xmlhttp.status == 404){
							this.guiRemoveWidgetInfo(widgetId);
							console.log("widget does not exist any longer");
						}
					}.bind(this));
				}
				else if (result == 404){
					console.log("widget" + widgetId + "does not exist");
					this.guiRemoveWidgetInfo(widgetId);
					for (var i = 0; i < this.widgetComponents.length; i++){
						if (this.widgetComponents[i]._widget.regionWidgetId == widgetId)
							com.remove(this.widgetComponents[i]);
					}
				}
			}.bind(this));
			
			//4 client req:	on save success, change widget location
		},
		
		onXMPPPerformMigration: function(intent){
			//anonymous does not care migration
			if (intent.extras.spaceUri != space._uri)
				return;
			var source = intent.extras.source;
			var target = intent.extras.target;
			var widgetId = intent.extras.widgetId;
			space.refresh(function(){
				devicePanel.moveWidgetLi(widgetId, target);
			});
			if (this.currentDeviceName == null || this.currentDeviceName == ""){
				$("#dui-device-panel-widget-"+widgetId).removeClass("migrating");
				if (this.migrationTimers[widgetId]){
					clearTimeout(this.migrationTimers[widgetId]);
					delete this.migrationTimers[widgetId];
				}
				return;
			}
			//idle widget or widget is currently displayed nowhere or this is the widget location
			if (this.currentDeviceName == source || source == ""){
				if (this.duiWidgets.indexOf(widgetId) != -1){				
					intent = {
						"action": "DUI_PRE_MIG",
						"component": "duiclient-"+widgetId,
						"categories": ["DUI"],
						"data": "",
						"dataType":"",
						"extras": {"widgetId": widgetId}
					};
					this.duiIwcProxy.publish(intent);
			//		the widget will prepare the migration and then publish an OK msg, the agent will do onRcvWidgetMigrationPrepared()!!!!}
				}else{
					this.roleRemoveWidgetFromDevice(widgetId);
					//remove it from the white list
					var index = this.widgetsWhiteList.indexOf(widgetId);
					if (index != -1){
						this.widgetsWhiteList.splice(index, 1);
						//update the DUI control panel since the current device has made some changes
						this.guiSetCurrentDisplayedWidgets(this.widgetsWhiteList);
						this.guiSetWidgetLocationInfo(widgetId, target);
						var li = document.getElementById("dui-device-panel-widget-"+widgetId);
						if (li == null){
							var title = this.widgetIndex[widgetId].title == null? widgetId: this.widgetIndex[widgetId].title;
							devicePanel.createWidgetLi(widgetId, title, target);
						}
					}
					var x = document.getElementById("dui-selectDevice").selectedIndex;
					if (x != -1){
						var selectedDevice = document.getElementById("dui-selectDevice").options[x].value;
						if (selectedDevice == this.currentDeviceName)
							this.loadSelectedDeviceConfig();
					}
				}
			}
			if (this.currentDeviceName == target && this.widgetsWhiteList.indexOf(widgetId) == -1){
				//if this widget does for sure support DUI
				if (intent.extras.isDui == true){
					/**
					 * solution: 
					 * create an array in the dui mgr e.g. migratingWidgets = new Array()
					 * add the widgetId to the array when dui mgr calls the role framework to create the widget GUI
					 * the DUIClient on the widget sends an intent to inform the completence of the widget creation(i.e. add an intent sending in the DUIClient constructor)
					 * on receiving such an intent, check the if there is such a widget recorded in this.migratingWidgets(every widget instanitiation will send this intent, thus a check is needed)
					 * if not, it means that the creation is a normal widget creation by use case "add widget" or use case "init role platform"
					 * if yes, it means that the creation is a migration creation. Thus call this.updateWidgetState(widgetId, true)
					 */
					this.migratingWidgets.push(widgetId);
					if(intent.extras.state != undefined) {
					  this.migratingWidgetStates[widgetId]=intent.extras.state;
					}
					
				}
				//push it into the white list and don't have to update the device config from the server since this is the only change
				this.widgetsWhiteList.push(widgetId);
				//must first push to the white list then add
				this.roleAddWidgetToDevice(widgetId);
				//update the DUI control panel since the current device has made some changes
				this.guiSetCurrentDisplayedWidgets(this.widgetsWhiteList);
				this.getWidgetCurrentLocation(widgetId);
				var x = document.getElementById("dui-selectDevice").selectedIndex;
				if (x != -1){
					var selectedDevice = document.getElementById("dui-selectDevice").options[x].value;
					if (selectedDevice == this.currentDeviceName)
						this.loadSelectedDeviceConfig();
				}
			}
			
			$("#dui-device-panel-widget-"+widgetId).removeClass("migrating");
			if (this.migrationTimers[widgetId]){
				clearTimeout(this.migrationTimers[widgetId]);
				delete this.migrationTimers[widgetId];
			}
		},
		
		/**
		 * The intent is received and this function is called when a DUIClient on a widget is created.
		 * Thus check if this widget is a migrating one, if yes, update the widget state for migration
		 * if not, perform a normal state update
		 * @param intent
		 */
		onRcvDUIClientCreated: function(intent){
			var widgetId = intent.extras.widgetId;
			var i = this.migratingWidgets.indexOf(widgetId);
			var f = true;
			if (i != -1){
			  if(this.migratingWidgetStates[widgetId]!=undefined){
			    var intent = {
							"action": "DUI_UPDATE_STATE",
							"categories": ["DUI"],
							"component": "duiclient-"+widgetId,
							"data":"",
							"dataType":"",
							"extras":{"widgetId": widgetId, "isForMigration":true, "widgetStates": this.migratingWidgetStates[widgetId]}
					};
					if (this.currentAppState != null)
						intent.extras["appStates"] = this.currentAppState;
					this.duiIwcProxy.publish(intent);
					delete this.migratingWidgetStates[widgetId];
			  }else {
				  //this.updateWidgetState(widgetId, true); //TODO?
				}
				this.migratingWidgets.splice(i, 1);
				f = false;
			}
			i = this.duiWidgets.indexOf(widgetId);
			//record that this widget supportes DUI
			if (i == -1){
				this.duiWidgets.push(widgetId);
			}
			if (f)
					this.updateWidgetState(widgetId, false);
		},
		
		/**
		 * The intent is received and this function is called when a DUIClient informs that the widget is ready to be removed.
		 * Thus remove the widget from the framework and then update the widget location on the DUI manager GUI.
		 * @param intent
		 */
		onRcvWidgetMigrationPrepared: function(intent){
			var widgetId = intent.extras.widgetId;
			this.roleRemoveWidgetFromDevice(widgetId);
			//remove it from the white list
			this.widgetsWhiteList.splice(this.widgetsWhiteList.indexOf(widgetId), 1);
			//update the DUI control panel since the current device has made some changes
			this.guiSetCurrentDisplayedWidgets(this.widgetsWhiteList);
			this.getWidgetCurrentLocation(widgetId);
			var x = document.getElementById("dui-selectDevice").selectedIndex;
			if (x != -1){
				var selectedDevice = document.getElementById("dui-selectDevice").options[x].value;
				if (selectedDevice == this.currentDeviceName)
					this.loadSelectedDeviceConfig();
			}
		},
		
		onXMPPCheckConnectivity: function(intent){
			this.lastRefresh = (new Date()).getTime();
			this.guiResetDeviceConnectivityInfo();
			this.dconnCounter = 0;
			var that = this;
			if (!this.isChecking){
				this.isChecking = true;
				setTimeout(function(){
					that.guiSetDeviceTimeout();
					that.isChecking = false;
					if (that.pendingMigration != null){
						that.migrateWidget(that.pendingMigration.widgetId, that.pendingMigration.targetDevice, that.pendingMigration.sourceDevice);
						that.pendingMigration = null;
					}
				}, 4000);
			}
			this.responseConnectProbe();
		},
		
		responseConnectProbe: function(){
			console.log("confirm connectivity");
			var atSpace = space._uri;
			deviceMgr.confirmConnected(this.currentDeviceName, atSpace);
		},
		
		onXMPPRcvDeviceConnected: function(intent){
			var deviceName = intent.extras.device;
			var isAnonymous = intent.extras.isAnonymous;
			var location = intent.extras.location;
			var connInfoStr = "connected at space " + location;
			if (isAnonymous){
				deviceName = "anonymous-"+this.dconnCounter;
				this.dconnCounter++;
			}
			this.guiSetDeviceConnectivityInfo(deviceName, connInfoStr);
			var parentDiv = document.getElementById("dui-conn-div");
			var connDivs = parentDiv.getElementsByTagName("div");
			var checkFinished = true;
			for (var i = 0; i < connDivs.length; i++){
				var deviceName = connDivs[i].getElementsByTagName("label")[0].innerHTML;
				var conn = connDivs[i].getElementsByTagName("label")[1].innerHTML;
				if (deviceName.indexOf("anonymous-") == -1 && conn == "unknown"){
					checkFinished = false;
					break;
				}
			}
			if (checkFinished){
				this.isChecking = false;
				if (this.pendingMigration != null){
					this.migrateWidget(this.pendingMigration.widgetId, this.pendingMigration.targetDevice, this.pendingMigration.sourceDevice);
					this.pendingMigration = null;
				}
			}
		},
		
		
		onXMPPRcvDeviceNotConnected: function(intent){
			var deviceName = intent.extras.device;
			this.guiSetDeviceOffline(deviceName);
		},
		
		
		onXMPPRcvAppStateChange: function(intent){
			var o = null;
			if (typeof intent.extras.oldStates != "undefined")
				o = intent.extras.oldStates;
			var n = intent.extras.newStates;
			try{
				this.currentAppState = JSON.parse(n);
				this.guiResetAppStates();
				var self = this;
				$.each(this.currentAppState, function(stateName, stateValue){
					self.guiDisplayAppState(stateName, stateValue);
				});
				intent = {
					"action": "DUI_APP_CHANGE",
					"categories": ["DUI"],
					"component": "",
					"data": "",
					"dataType": "",
					"extras": {"oldStates": o==null?null:JSON.parse(o), "newStates": this.currentAppState},
				};
				// sample array of old/newStates {"statename1":value1, "statename2":value2,"statename3":value3}
				this.duiIwcProxy.publish(intent);
			}catch(err){
				console.log("error on parsing json app state");
				return;
			}
		},
		
		onXMPPRcvNewDevice: function(intent){
			var that = this;
			var newDeviceName = intent.extras.newDevice;
			
			var userUri = "http://" + document.location.host + "/user";
			openapp.resource.get(userUri, function(context){
				user._context = context;
				user._uri = context.uri;
				if (that.devices.indexOf(newDeviceName) == -1){
					that.devices.push(newDeviceName);
					that.guiCreateNewDeviceInfo(newDeviceName);
					devicePanel.createDevicePanel(newDeviceName);
				}
				space.refresh(function(){});
			});
//			this.refreshConnectivities();
		},
		
		createWidgetComponent: function(widgetId, activityUri){
				var tools, tool, context, properties, widget, widgetUrl, preferences;

				context = space._context;
				tools = openapp.resource.context(context).sub(
						openapp.ns.role + "tool").list();
				for (var i = 0; i < tools.length; i++){
					tool = tools[i];
					properties = openapp.resource.context(tool).properties();
					if (widgetId != properties[openapp.ns.widget+"moduleId"])
						continue;
	
					//else record the widget first
					this.widgets.push(widgetId);
					var widgetInfo = {"uri": tool.uri, "title": properties[openapp.ns.dcterms+"title"]};
					this.widgetIndex[widgetId]=widgetInfo;
					if (this.currentDeviceName == null || this.currentDeviceName == "")
						this.widgetsWhiteList.push(widgetId);
					
					preferences = gadgets.json.parse(properties[openapp.ns.widget + "preferences"] || "{}");
					
					widgetUrl = properties[openapp.ns.role + "widget"];
					if (decodeURIComponent($.url(widgetUrl).param("openapp.ns.role")) === openapp.ns.role) {
						widgetUrl = widgetUrl + "&openapp.role.activity=" + encodeURIComponent(activityUri);
					}
					
					widget = {
						type : 'OpenSocial',
						regionWidgetId : properties[openapp.ns.widget
								+ "moduleId"],
						widgetUrl : widgetUrl,
						_widgetSource : properties["http://www.w3.org/2002/07/owl#sameAs"],
						securityToken : decodeURIComponent(properties[openapp.ns.widget + "securityToken"]),
						metadata : {},
						userPrefs : preferences
					};
					
					widgetFeature._getGadgetSpecs([widget.widgetUrl], function(obj) {
						widget.metadata = obj.data.result[widget.widgetUrl];
						com.add(widgetFeature._processWidget(widget, obj, tool));
						widgetFeature._postprocessWidgets();
					});
					//the widget component will be added by widgetFeature._processWidget(), break the loop and finish it.
							
					this.guiCreateWidgetInfo(widgetId);
					this.getWidgetCurrentLocation(widgetId);
					var idleWidgets = this.lookForIdleWidgets();
					for (var j=0; j<idleWidgets.length;j++)
						if (space._isMember && widgetId == idleWidgets[j])
							devicePanel.createIdleWidget(idleWidgets[j], this.widgetIndex[idleWidgets[j]].title, true);
					var x = document.getElementById("dui-selectDevice").selectedIndex;
					if (x != -1)
						this.loadSelectedDeviceConfig();
					break;	
				}
		},
		
		onXMPPRcvNewWidget: function(intent){
			//create the widget component and add it to role client env. see role/feature/widget
			var widgetId = intent.extras.widgetId;
			var activityUri = intent.extras.activityUri;
			
			//if the new widget is already recorded, i.e. this is the device who creates the new widget, then do nothing
//			/*
			if (this.widgets.indexOf(widgetId) != -1)
				return;
			
			//and then create the widget component and add it to role client env. see role/feature/widget
//			/*
			space.refresh(function(){
				$(document).trigger('duiprepwidget', {"widgetId": widgetId, "activityUri": activityUri});
			});
			//create the DUI manager GUI for this new widget

//			*/
		},
		
		onRcvLogOff: function(intent){
			var widgetUri = this.widgetIndex[intent.extras.widgetId].uri;
			var jsonWidgetStates = intent.extras["widgetStates"];
			try{
				JSON.stringify(jsonWidgetStates);
				appMgr.setWidgetState(widgetUri, jsonWidgetStates, function(xmlhttp){});
			}catch(err){
				console.log("error on parsing json widget state");
				
			}finally{
				this.logOffCount--;
				if (this.logOffCount == 0){
//					if (this.currentDeviceName != null && this.currentDeviceName != "")
//						deviceMgr.notifyDeviceOut(this.currentDeviceName);
					location = location.protocol + "//" + location.host + "/:authentication?return=%2F&action=signout";
				}
			}
			
		},
		
		onXMPPRcvRemoveDevice: function(intent){
		//	@see removeDevice the http onreadystatechange but notice that the device is removed if this is the device sending the remove httpreq
			
			var removeDeviceName = intent.extras.device;
			var that = this;
			devicePanel.removeDevice(removeDeviceName);
			if (this.currentDeviceName != null && this.currentDeviceName == removeDeviceName){
				//if this is the device that is removed, ask for DUI widget to save the state and after 15 sec log off
				this.eraseCookie("device");
				this.eraseCookie("duiUser");
				this.logOffCount = this.duiWidgets.length;
				//if there are dui widgets, save them
				if (this.logOffCount > 0){
					intent = {
							"action": "DUI_LOG_OFF",
							"component": "",
							"categories": ["DUI"],
							"data": "",
							"dataType":"",
							"extras": {}
					};
					this.duiIwcProxy.publish(intent);
					this.timer = setTimeout(function(){
						if (that.currentDeviceName != null && that.currentDeviceName != "")
							deviceMgr.notifyDeviceOut(that.currentDeviceName);
						location = location.protocol + "//" + location.host + "/:authentication?return=%2F&action=signout";
					}, 15000);
				}
				//if there is no dui widget, sign out
				else{
					if (that.currentDeviceName != null && that.currentDeviceName != "")
						deviceMgr.notifyDeviceOut(that.currentDeviceName);
					location = location.protocol + "//" + location.host + "/:authentication?return=%2F&action=signout";
				}
			}
			else{
				var x = this.devices.indexOf(removeDeviceName) == -1;
				if (x == -1)
					return;
			// 	update device select and info div, update widget target select
			//	the list
				this.devices.splice(x, 1);
			//	the remove device info from the DUI control panel
				this.guiRemoveDeviceInfo(removeDeviceName);
				var userUri = "http://" + document.location.host + "/user";
				openapp.resource.get(userUri, function(context){
					user._context = context;
					user._uri = context.uri;
					space.refresh(function(){});
				});
			}
		},
			
		onXMPPRcvRemoveWidget: function(intent){
			var widgetId = intent.extras.widgetId;
			devicePanel.removeWidget(widgetId);
			//if the widget id exists in the list, means that this device is not the source device on which the widget is deleted
			var i = this.widgets.indexOf(widgetId);
			if ( i != -1){
				//remove it from the list
				this.widgets.splice(i, 1);
				if (this.widgetIndex.hasOwnProperty(widgetId))
					delete this.widgetIndex[widgetId];
				//remove it from all sub lists in case of any sync problem by rogue operations from users
				i = this.widgetsWhiteList.indexOf(widgetId);
				if ( i != -1){
					this.widgetsWhiteList.splice(i, 1);
//					this.roleRemoveWidgetFromDevice(widgetId);
				}
				i = this.migratingWidgets.indexOf(widgetId);
				if ( i != -1)
					this.migratingWidgets.splice(i, 1);
				i = this.duiWidgets.indexOf(widgetId);
				if ( i != -1)
					this.duiWidgets.splice(i, 1);
				//remove the ROLE widget component pointing to this widget, COMBINDED moves with com.on remove widget in load function
				for (i = 0; i < this.widgetComponents.length;)
					if (this.widgetComponents[i]._widget.regionWidgetId == widgetId){
						com.remove(this.widgetComponents[i]);
						this.widgetComponents.splice(i, 1);
					}
					else
						i++;
				//remove all infos of the widget from the DUI control panel
				this.guiRemoveWidgetInfo(widgetId);
				if (document.getElementById("dui-selectDevice").selectedIndex != -1)
					this.loadSelectedDeviceConfig();
				space.refresh(function(){});
			}
			
		},
		
		onRcvWidgetStateForSwitch: function(intent){
			var widgetUri = this.widgetIndex[intent.extras.widgetId].uri;
			var jsonWidgetStates = intent.extras["widgetStates"];
			try{
				JSON.stringify(jsonWidgetStates);
				appMgr.setWidgetState(widgetUri, jsonWidgetStates, function(xmlhttp){});
			}catch(err){
				console.log("error on parsing json widget state");
			}finally{
				var widgetId = intent.extras.widgetId;
				this.unSavedWidgets.splice(this.unSavedWidgets.indexOf(widgetId), 1);
				if (this.unSavedWidgets.length == 0){
					this.eraseCookie("device");
					this.eraseCookie("duiUser");
//					xmpp.connection.disconnect();
					if (this.toDevice){
						this.createCookie("device", this.toDevice, 30);
						this.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 30);
					}
					location.reload();
				}
			}
			
			
		},
		
		onXMPPRcvPerformSwitchDevice: function(intent){
			//this is the device that requires the switch
			var that = this;
			var from = intent.extras.from;
			var isAnonyms = this.currentDeviceName == null || this.currentDeviceName == "";
			if ((from == null && isAnonyms && this.isSwitching)
					||(from != null && this.currentDeviceName != null && this.currentDeviceName == from)){
				//if there are dui widget, ask to save the state
				if (this.duiWidgets.length > 0){
					var duiintent = {};
					this.unSavedWidgets = [];
					this.toDevice = intent.extras.to;
					for (var j = 0; j < this.duiWidgets.length; j++){
						this.unSavedWidgets.push(this.duiWidgets[j]);
					}
					for (var i = 0; i < this.duiWidgets.length; i++){
						duiintent = {
							"action": "DUI_GET_WS",
							"component": "duiclient-"+this.duiWidgets[i],
							"categories": ["DUI"],
							"data": "",
							"dataType":"",
							"extras": {"widgetId": this.duiWidgets[i]} //has target implies that it is for migration
						};
						this.duiIwcProxy.publish(duiintent);
						setTimeout(function(){
							this.eraseCookie("device");
							this.eraseCookie("duiUser");
//							xmpp.connection.disconnect();
							this.createCookie("device", intent.extras.to, 30);
							this.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 30);
							location.reload();
						}, 15000);
					}
				}
				//if there is no dui widget, nothing is need to be saved and just switch
				else{
					this.eraseCookie("device");
					this.eraseCookie("duiUser");
//					xmpp.connection.disconnect();
					this.createCookie("device", intent.extras.to, 30);
					this.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 30);
					location.reload();
				}
			}
			//this is the device that is a tragedy to be forced switched
			if (intent.extras.to == this.currentDeviceName){
				this.eraseCookie("device");
				this.eraseCookie("duiUser");
				this.logOffCount = this.duiWidgets.length;
				//if there are dui widget, save the state first
				if (this.logOffCount > 0){
					intent = {
							"action": "DUI_LOG_OFF",
							"component": "",
							"categories": ["DUI"],
							"data": "",
							"dataType":"",
							"extras": {}
					};
					this.duiIwcProxy.publish(intent);
					this.timer = setTimeout(function(){
//						if (that.currentDeviceName != null && that.currentDeviceName != "")
//							deviceMgr.notifyDeviceOut(that.currentDeviceName);
						location = location.protocol + "//" + location.host + "/:authentication?return=%2F&action=signout";
					}, 15000);
				}
				//if there is no dui widget, just sign out
				else{
//					if (that.currentDeviceName != null && that.currentDeviceName != "")
//						deviceMgr.notifyDeviceOut(that.currentDeviceName);
					location = location.protocol + "//" + location.host + "/:authentication?return=%2F&action=signout";
				}
			}
				
		},
		
		onSetAppState: function(intent){
			appMgr.setAppState(intent.extras.states);
		}
	
		
//		---------------------------------------------------------------
//		response from dui xmpp and liwc related--------------------------------------- end
//		---------------------------------------------------------------			
		
	};
	
	domReady(function(){
		/*
		$(document).bind('space-xmpp-connected', function(){
			var pubsubservice = xmpp.pubsubservice;
			var connection = xmpp.connection;
			var pubsubnode = xmpp.pubsubnode;
			duiManager.connectSpaceXMPP(pubsubservice, pubsubnode, connection);
		});
		*/
		/*
		if (detectMobile.isMobile()){
			var meta = document.createElement("meta");
			var width = $(window).width();
			meta.content = "width="+width+"px; initial-scale=1.0; maximum-scale=1.0;";
			meta.name = "viewport";
			document.getElementsByTagName("head")[0].appendChild(meta);
		}
		*/
		$(document).bind('duiprepwidget', function(ev, data){
			duiManager.createWidgetComponent(data.widgetId, data.activityUri);
		});
		
		$(document).bind('user-xmpp-connected', function(){
			if (user._context.uri.indexOf("cTrvjuLrGC") == -1){
				var pubsubservice = xmpp.pubsubservice;
				var connection = xmpp.connection;
				var userpubsubnode = xmpp.userpubsubnode;
				duiManager.connectUserXMPP(pubsubservice, userpubsubnode, connection);
			}
		});
	});

	return duiManager;
});
