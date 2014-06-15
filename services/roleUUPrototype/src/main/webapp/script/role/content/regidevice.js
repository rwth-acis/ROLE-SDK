define([ "com", "jquery", "config", "xmpp/xmpp", "handlebars!./regidevice", 
         "../feature/duimanager", "../feature/duiproxies/devicemgrproxy", "xmpp/duixmppportal", "../feature/duiproxies/deviceinfo"], 
		function(com, $, config, xmpp, template, duiManager, deviceMgr, duiXmpp, deviceInfo) { 
var user0 = null;
var pendingNewDevice = [];
var regiDevice = {

	interfaces : [ "http://purl.org/role/ui/Content#", "http://purl.org/role/ui/Feature#" ],
	regDevice: null,
	duiIwcProxy: null,
	user: null,
	
	load : function() {
		com.on("http://purl.org/role/ui/User#", "load", function(user){
			if (user._context.uri.indexOf("cTrvjuLrGC") == -1){
				if ($("#progressdiv").css("display") == "none"){
					$("#progressdiv span").text("loading...");
			        $("#progressdiv").fadeIn("fast");
				}
				//dialog for creating new device on ROLE home page
				$("#new-device-form").dialog({
					autoOpen: false,
					modal: true,
//					height: 250,
//					width: 300,
					buttons:{
						"Create" : function(){
							var newDeviceName = $("#new-device-name").val();
							var isChecked = $("#create-for-current-device").is(':checked');
							var valid = true;
							if (newDeviceName == null || newDeviceName == ""){
								$(".new-device-form-tip").text("Device name can not be empty");
								$(".new-device-form-tip").css("background-color", "yellow");
								valid = false;
							}
							/*
							if (newDeviceName.indexOf(" ") !== -1){
								$(".new-device-form-tip").text("No space in device name please");
								$(".new-device-form-tip").css("background-color", "yellow");
								valid=false;
							}*/
							if (valid){
								$(".new-device-form-tip").text("");
								$(".new-deivce-form-tip").css("background-color", "");
								pendingNewDevice.push(newDeviceName);
								deviceMgr.addDevice(newDeviceName, function(xmlhttp){
									if (xmlhttp.status == 202){
										pendingNewDevice.splice(pendingNewDevice.indexOf(newDeviceName), 1);
										alert("device " + newDeviceName + " exists.");
										return;
									}else if (xmlhttp.status != 201){
										pendingNewDevice.splice(pendingNewDevice.indexOf(newDeviceName), 1);
										return;
									}
									if (xmlhttp.status == 201){
											var idx = pendingNewDevice.indexOf(newDeviceName);
											pendingNewDevice.splice(idx, 1);
											var deviceId = xmlhttp.responseText;
											duiManager.devices.push(deviceId);
											var li = document.createElement('li');
											li.appendChild(document.createTextNode(newDeviceName));
											li.setAttribute("id", "dvc-"+deviceId);
											li.setAttribute("class", "regili");
											document.getElementById("deviceList").appendChild(li);
											$("#"+li.id).click(function(){
												var id = $(this).attr('id');
												$("#deviceName").text(newDeviceName);
												duiManager.createCookie("device", id.substring("dvc-".length), 10);
												duiManager.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 10);
												
												$(".regidevice-wrapper").hide("slow", function(){$(".regidevice-wrapper").empty();});
												$(".regili").removeClass("regilisel");
												$(this).addClass("regilisel");
											});
											if (isChecked){
												duiManager.eraseCookie("duiUser");
												duiManager.eraseCookie("device");
											}
											if (duiManager.readCookie("duiUser") != user._uri.substring(user._uri.lastIndexOf("/")+1) || 
													duiManager.readCookie("device") == null){
												$("#deviceName").text(newDeviceName);
												duiManager.createCookie("device", deviceId, 10);
												duiManager.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 10);
												$(".regili").removeClass("regilisel");
												$(li).addClass("regilisel");
											}
											var userUri = "http://" + document.location.host + "/user";
											openapp.resource.get(userUri, function(context){
												user._context = context;
												user._uri = context.uri;
												$("#recoDevice").click();
											});
									}
								}, isChecked);
								$(this).dialog("close");
							}
						},
						Cancel: function(){
							$(this).dialog("close");
						}
					}
				});
				user0 = user;
				console.log("TO MAKE IT MORE PROMINENT! LONG-rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr ------ " + duiManager.isUserLoaded);
				duiManager.loadUserProfileForRegiDevice();
				
//				create device list elements
				if (duiManager.devices.length > 0)
					for (var i = 0; i < duiManager.devices.length; i++){
						var li = document.createElement('li');
						li.appendChild(document.createTextNode(duiManager.getDeviceAlias(duiManager.devices[i])));
						li.setAttribute("id", "dvc-"+duiManager.devices[i]);
						li.setAttribute("class", "regili");
						document.getElementById("deviceList").appendChild(li);
						$("#"+li.id).click(function(){
							var id = $(this).attr('id');
							$("#deviceName").text(duiManager.getDeviceAlias(id.substring("dvc-".length)));
							duiManager.createCookie("device", id.substring("dvc-".length), 10);
							duiManager.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 10);
							
							$(".regidevice-wrapper").hide("slow", function(){$(".regidevice-wrapper").empty();});
							$(".regili").removeClass("regilisel");
							$(this).addClass("regilisel");
						});
					}
				if (duiManager.currentDeviceName == null){
					$("#duiSuggestion").html("please select a device below or create a new device.");
				}
				else{
					$("#deviceName").text(duiManager.getDeviceAlias(duiManager.currentDeviceName));
					$(".regili").removeClass("regilisel");
					$("#dvc-"+duiManager.currentDeviceName).addClass("regilisel");
				}
				$("#newDevice").click(function(){
					$("#new-device-form").dialog("open");
				});
				var that = this;
				$("#recoDevice").click(function(){that.recognizeDevice(user);});
				$("#refresh").click(function(){			
					var list = document.getElementById("deviceList").getElementsByTagName("li");
					for (var i = 0; i < list.length; i++)
						list[i].childNodes[0].nodeValue = duiManager.getDeviceAlias(list[i].id.substring("dvc-".length));
					deviceMgr.checkConnectivities(function(xmlhttp){});
				});
				$("#panel-img-refresh").click(function(){
					$("#refresh").click();
				});
				$("#panel-img-add").click(function(){
					$("#newDevice").click();
				});
				$("#panel-img-delete").click(function(){
					var rmvDeviceName = $(".regilisel").attr("id");
					if (rmvDeviceName != null && rmvDeviceName != ""){
						rmvDeviceName = rmvDeviceName.substring("dvc-".length);
						var li = document.getElementById("dvc-"+rmvDeviceName);
						if (li != null)
							li.parentNode.removeChild(li);
						$("#deviceName").text("");
						duiManager.eraseCookie("device");
						duiManager.eraseCookie("duiUser");
						deviceMgr.removeDevice(rmvDeviceName, function(xmlhttp){});
					}
						
				});
				$("#panel-img-edit").click(function(){
					var deviceId = $(".regilisel").attr('id').substring("dvc-".length);
					var newName = prompt("Please enter the new name of the device", $("#deviceName").text());
					if (newName != null && newName != "" && newName != $("#deviceName").text())
						deviceMgr.changeDeviceAlias(deviceId, newName, function(xmlhttp){});
				});
				this.connectXmpp(user);
			}
		}.bind(this));
	},
	
	/**
	 * check if the current physical device matches any device profile stored in virtual device
	 * @param user the user data model (../model/user)
	 */
	recognizeDevice: function(user){
		var os = deviceInfo.OS;
		var browser = deviceInfo.browser;
		var version = deviceInfo.version;
		var info = null;
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
		
		if (os == "unknown" || browser == "unknown"){
			var result = [];
			info = navigator.userAgent;
			for (i = 0; i < deviceProfiles.length; i++)
				if (deviceProfiles[i].profile.hasOwnProperty("userAgent")
						&& deviceProfiles[i].profile.userAgent == info)
					result.push[deviceProfiles[i].name];
			if (result.length == 0){
				$("#duiSuggestion").html("No existing device profile matches current device.<br/>" +
						"please select a device below or create a new one.");
				$("#duiSuggestion").css("background-color", "#FD9E78");
			}
			else if (result.length == 1){
				var alias = duiManager.getDeviceAlias(result[0]);
				$("#duiSuggestion").html("Recognized as device '" + alias + "'.<br/>" +
				"You may select the device '" + alias + "' below.");
				$("#duiSuggestion").css("background-color", "#76E976");
				$("#deviceName").text(alias);
				duiManager.createCookie("device", result[0], 10);
				duiManager.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 10);
				$(".regili").removeClass("regilisel");
				$("#dvc-"+result[0]).addClass("regilisel");
			}
			else {
				var t = "";
				for (i = 0; i < result.length; i++)
					t = t + "'" + duiManager.getDeviceAlias(result[i]) + "'" + ", ";
				t = t.substring(0, t.lastIndexOf(","));
				$("#duiSuggestion").html("Recognized as devices:<br/> " + t + ".<br/>" +
				"You may select one of them below.");
				$("#duiSuggestion").css("background-color", "yellow");
			}
		}
		else{
			var recog = [];
			var maybe = [];
			for (i = 0 ; i < deviceProfiles.length; i++){
				if (!deviceProfiles[i].profile.hasOwnProperty("userAgent")){
					var pf = deviceProfiles[i].profile;
					if (pf.os != os || pf.browser != browser)
						continue;
					else{
						if (version == "unknown")
							maybe.push(deviceProfiles[i].name);
						else if (version == pf.version)
							recog.push(deviceProfiles[i].name);
						else
							maybe.push(deviceProfiles[i].name);
					}
				}
			}
			if (recog.length > 0){
				if (recog.length == 1){
					var alias = duiManager.getDeviceAlias(recog[0]);
					$("#duiSuggestion").html("Recognized as device '" + alias + "'.<br/>" +
					"You may select the device '" + alias + "' below.");
					$("#duiSuggestion").css("background-color", "#76E976");
					$("#deviceName").text(alias);
					duiManager.createCookie("device", recog[0], 10);
					duiManager.createCookie("duiUser", user._uri.substring(user._uri.lastIndexOf("/")+1), 10);
					$(".regili").removeClass("regilisel");
					$("#dvc-"+recog[0]).addClass("regilisel");
				}
				else {
					var t = "";
					for (i = 0; i < recog.length; i++)
						t = t + "'" + duiManager.getDeviceAlias(recog[i]) + "'" + ", ";
					t = t.substring(0, t.lastIndexOf(","));
					$("#duiSuggestion").html("Recognized as devices:<br/> " + t + ".<br/>" +
					"You may select one of them below.");
					$("#duiSuggestion").css("background-color", "yellow");
				}
			}
			else if (maybe.length > 0){
				if (maybe.length == 1){
					var alias = duiManager.getDeviceAlias(maybe[0]);
					$("#duiSuggestion").html("Recognized as device '" + alias + "' but not precisely.<br/>" +
					"You may select the device '" + alias + "' below.");
					$("#duiSuggestion").css("background-color", "#yellow");
				}
				else {
					var t = "";
					for (i = 0; i < maybe.length; i++)
						t = t + "'" + duiManager.getDeviceAlias(maybe[i]) + "'" + ", ";
					t = t.substring(0, t.lastIndexOf(","));
					$("#duiSuggestion").html("Recognized as devices but not precisely:<br/> " + t + "<br/>" +
					"You may select one of them below.");
					$("#duiSuggestion").css("background-color", "yellow");
				}
			}
			else{
				$("#duiSuggestion").html("No existing device profile matches current device.<br/>" +
				"please select a device below or create a new one.");
				$("#duiSuggestion").css("background-color", "#FD9E78");
			}
		}
		
		if (duiManager.readCookie("device") != null &&
				duiManager.readCookie("duiUser") == user._uri.substring(user._uri.lastIndexOf("/")+1)){
			$(".regidevice-wrapper").hide("slow", function(){$(".regidevice-wrapper").empty();});
		}else{
			$(".regidevice-wrapper").show("slow");
		}
	},
	
	/**
	 * simple dui xmpp dispatcher
	 * @param intent the incoming intent
	 */
	duiXmppDispatcher: function(intent){
		if(intent.extras.user == duiManager.currentUserId){
			if (intent.action == "DUI_CONN_CONFIRM"){
				var deviceName = intent.extras.device;
				var isAnonymous = intent.extras.isAnonymous;
				var location = intent.extras.location;
				if (!isAnonymous){
					document.getElementById("dvc-"+deviceName).childNodes[0].nodeValue = deviceName + "(active at space " + location +")";
				}
				return;
			}				
			if (intent.action == "DUI_NEW_DEVICE"){
				var newDeviceName = intent.extras.newDevice;
				var alias = intent.extras.deviceAlias;
				if (duiManager.devices.indexOf(newDeviceName) == -1 && pendingNewDevice.indexOf(alias) == -1){
					duiManager.devices.push(newDeviceName);
					var li = document.createElement('li');
					li.appendChild(document.createTextNode(alias));
					li.setAttribute("id", "dvc-"+newDeviceName);
					li.setAttribute("class", "regili");
					document.getElementById("deviceList").appendChild(li);
					$("#"+li.id).click(function(){
						var id = $(this).attr('id');
						$("#deviceName").text(alias);
						duiManager.createCookie("device", id.substring("dvc-".length), 10);
						duiManager.createCookie("duiUser", user0._uri.substring(user0._uri.lastIndexOf("/")+1), 10);
						
						$(".regidevice-wrapper").hide("slow", function(){$(".regidevice-wrapper").empty();});
						$(".regili").removeClass("regilisel");
						$(this).addClass("regilisel");
					});
					var userUri = "http://" + document.location.host + "/user";
					openapp.resource.get(userUri, function(context){
						user0._context = context;
						user0._uri = context.uri;
						$("#recoDevice").click();
					});
				}
				return;
			}				
			if (intent.action == "DUI_REMOVE_DEVICE"){
				var removeDeviceName = intent.extras.device;
				
				if ($(".regilisel").attr('id') && $(".regilisel").attr('id').substring("dvc-".length) == removeDeviceName){
					$("#deviceName").text("");
					duiManager.eraseCookie("device");
					duiManager.eraseCookie("duiUser");
				}
				
				var li = document.getElementById("dvc-"+removeDeviceName);
				if (li != null)
					li.parentNode.removeChild(li);
				
				var idx = duiManager.devices.indexOf(removeDeviceName);
				if (idx != -1)
					duiManager.devices.splice(idx, 1);
				var userUri = "http://" + document.location.host + "/user";
				openapp.resource.get(userUri, function(context){
					user0._context = context;
					user0._uri = context.uri;
					$("#recoDevice").click();
				});
				return;
			}
			if (intent.action == "DUI_NEW_PROFILE"){
				var userUri = "http://" + document.location.host + "/user";
				openapp.resource.get(userUri, function(context){
					user0._context = context;
					user0._uri = context.uri;
				});
				return;
			}
			if (intent.action == "DUI_CHANGE_ALIAS"){
				var deviceName = intent.extras.device;
				var alias = intent.extras.alias;
				if ($(".regilisel").attr('id').substring("dvc-".length) == deviceName){
					$("#deviceName").text(alias);
					$(".regilisel").text(alias);
					duiManager.eraseCookie("device");
					duiManager.eraseCookie("duiUser");
					duiManager.createCookie("device", deviceName, 10);
					duiManager.createCookie("duiUser", user0._uri.substring(user0._uri.lastIndexOf("/")+1), 10);
				}
				else{
					$("#dvc-"+deviceName).text(alias);
				}
				var userUri = "http://" + document.location.host + "/user";
				openapp.resource.get(userUri, function(context){
					user0._context = context;
					user0._uri = context.uri;
				});
				return;
			}
		}
		console.log("invalid intent on simple DUI manager:");
		console.log(intent);
	},

	/**
	 * initialize xmpp feature in ROLE homepage
	 * @param user
	 */
	initSimpleDui: function(user){
		if (this.duiIwcProxy == null){
			this.duiIwcProxy = new duiXmpp();
			this.duiIwcProxy.onError = function(msg) {
				console.log("PROXY iwcProxy.onError() " + msg);
			};	
		}
		$("#recoDevice").click();
		this.duiIwcProxy.setXmppClient(xmpp.connection);
		this.duiIwcProxy.setPubSubNode(xmpp.pubsubservice, xmpp.userpubsubnode);
		this.duiIwcProxy.connect(function(intent){}, this.duiXmppDispatcher.bind(this), function(){
			if ($("#progressdiv").css("display") != "none"){
		        $("#progressdiv").fadeOut("slow", function(){$("#progressdiv span").text("");});
			}
		});
	},
	
	/**
	 * initiate an xmpp conncetion
	 * @param user the user model (../model/user)
	 */
	connectXmpp: function(user){
		var userProperties = openapp.resource.context(
				user._context).properties();
		
		// extract connection information from user properties
		var userjid = userProperties["http://xmlns.com/foaf/0.1/jabberID"].substring("xmpp:".length);
		var userpass = userProperties[openapp.ns.role + "externalPassword"];
		
		var usert = userjid.split("@");
		var host = usert[1];
		var boshurl;
		if (config.usewebsocket && WebSocket) {
			boshurl = "ws://" + host;
			if (config.xmppwsport != 80) {
				boshurl += ':' + config.xmppwsport;
			}
			if (config.xmppwspath !== "") {
				boshurl += "/" + config.xmppwspath;
			}
		} else {
			boshurl = "http://" + host;
			if (config.xmppboshport != 80) {
				boshurl += ':' + config.xmppboshport;
			}
			if (config.xmppboshpath !== "") {
				boshurl += "/" + config.xmppboshpath;
			}
		}
		var pubsubservice = config.xmpppubsubservice + "." + host;
		
		xmpp.host = host;
		xmpp.boshurl = boshurl;
		xmpp.pubsubservice = pubsubservice;
		xmpp.userpubsubnode = "dui-"+usert[0];
		/*
		if (typeof duiManager.currentDeviceName!="undefined"&&
				duiManager.currentDeviceName != null && duiManager.currentDeviceName != "")
			userjid = userjid + "/" + duiManager.currentDeviceName;
			*/
		console.log("XMPP User JID: " + userjid);
		console.log("XMPP User Password: " + userpass);
		console.log("XMPP Host: " + xmpp.host);
		console.log("XMPP Connection Manager URL: " + xmpp.boshurl);
		console.log("XMPP Pubsub Service: " + xmpp.pubsubservice);
		console.log("User's private XMPP Pubsub node: " + xmpp.userpubsubnode);
		if(typeof xmpp.host === 'undefined' || xmpp.host.length == 0){
			console.log("Connection to XMPP server disabled. Enable with specifying parameters xmpp.host, xmpp.port");
		} else {
			if (!xmpp.isConnected){
				xmpp.connection = new Strophe.Connection(xmpp.boshurl);
				xmpp.connection.connect(
						userjid, userpass, 
						function(status){
							console.log("XMPP Strophe Connection Status: " + status);
							if (status === Strophe.Status.CONNECTED) {
								console.log("XMPP Strophe connected");
								var presence = $pres({"show":"online"});
								xmpp.connection.send(presence.tree());
								xmpp.isConnected = true;
								this.initSimpleDui(user);
								
							} else if (status === Strophe.Status.DISCONNECTED) {
								console.log("XMPP Strophe disconnected");
								$(document).trigger('xmpp-disconnected');
								xmpp.isConnected == false;
							}
						}.bind(this));
			}
		}
		/*
		window.onbeforeunload = function() {
			if (typeof xmpp.connection !== "undefined" &&
					xmpp.connection !== null) {
				xmpp.connection.disconnect();
			}
		};*/
	},
	
	createUI : function(container) {
		this.regDevice = $(template({
		})).appendTo(container);
	}
	
}; 


return regiDevice;
});