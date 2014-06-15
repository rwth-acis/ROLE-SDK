define(["../../model/space", "./reqdui"], function(space, reqDui){

var appMgr = {
	getUserAndSpaceId: function(callback){
		console.log("get current user and space id");
		reqDui.get("", null, callback);
	},
	
	
	getWidgetList: function (callback){
		console.log("refresh widgets");
		reqDui.get("refreshWidgets", null, callback);
	},
	
	/**
	 * Get the stored states of the widget 
	 * @param widgetId
	 * @param callback A function defined as "function(xmlhttpreq){}"
	 */
	getWidgetState: function(widgetUri, callback){
		console.log("reload widget state");
		var query = {
				"widgetUri": widgetUri,
				"space": space._uri
		};
		reqDui.get("widget", query, callback);
	},
	
	/**
	 * Get the stored states of the application that acts like a context of the union of widgets in the space
	 * @param callback A function defined as "function(xmlhttpreq){}"
	 */
	loadAppState: function(callback){
		console.log("reload application state");
		var query = {"space": space._uri};
		reqDui.get("appState", query, callback);
	},
	
	setAppState: function(appStates){
		console.log("set app state");
		var params = {
				"appStateJson": JSON.stringify(appStates),
				"space": space._uri
		};
		reqDui.put("setAppState", params, function(xmlhttp){});
	},
	
	/**
	 * Get the device on which the widget is displayed
	 * @param widgetId
	 * @param callback
	 */
	getWidgetCurrentLocation: function(widgetUri, callback){
//		console.log("get widget location: " + widgetUri);
		var query = {
				"widgetUri": widgetUri,
				"space": space._uri
		};
		reqDui.get("widgetLocation", query, callback);
	},
	
	/**
	 * Calls the server to initialize a widget migration
	 * @param widgetId
	 * @param targetDeviceName
	 */
	initMigrateWidget: function(widgetUri, targetDeviceName, callback){
		//	1 client req:	http req initMigration (widget targetdevice)
		//	  server resp:	fined source device, xmpp publish initMigration (source, target, widget)
		//	2 client req: 	if is source device, web messaging acquire widget states
		//	  widget resp:	web messaging widget states
		//	3 client req:	http req save widget states
		//	  server resp:	save widget states
		//	4 client req:	on save success, change widget location
		//	  server resp:	change widget location, xmpp publish: perform migration info:source+target+widget
		//	5 client resp:	if target device, ROLE client display add widget, load widget state, getCurrentWidgetsLocations()
		//	5 client resp:	if source device, ROLE client display remove widget, getCurrentWidgetsLocations()
		//	5 client resp:	if other devices, getCurrentWidgetsLocations();
		console.log("init migration: " + widgetUri + "to" + targetDeviceName);
		var params = {
				"widgetUri": widgetUri,
				"targetDevice": targetDeviceName,
				"space": space._uri
		};
		reqDui.put("initMigration", params, callback);
	},
	
	/**
	 * change the widget location, i.e. widget migrates
	 * @param widgetId
	 * @param targetDeviceName
	 * @param isDuiWidget The flag indicates whether the widget supports advanced mobile feature
	 */
	changeWidgetLocation: function(widgetUri, currentDeviceName, targetDeviceName, isDuiWidget, callback){
		console.log("change widget location: " + widgetUri + " to " + targetDeviceName);
		var params = {
				"widgetUri": widgetUri,
				"currentDevice": currentDeviceName,
				"targetDevice": targetDeviceName,
				"space": space._uri,
				"isDuiWidget": isDuiWidget
		};
		reqDui.put("changeWidgetLocation", params, callback);
	},
	
	setNewWidgetLocation: function(widgetUri, activityUri, currentDeviceName, callback){
		console.log("set new widget location: " + widgetUri + " to " + currentDeviceName);
//		if (currentDeviceName != null){
			params = {
					"widgetUri": widgetUri,
					"currentDevice": currentDeviceName,
					"activity": activityUri,
					"space": space._uri
			};
			reqDui.put("setNewWidgetLoc", params, callback);
//		}
	},
	
	/**
	 * set the widget states to the server
	 * @param widgetUri The widgetUri
	 * @param jsonWidgetState A JSON representation of all widget states {widgetStates: [{state1:value1},{state2:value2},{state3:value3}]}
	 * @param callback
	 */
	setWidgetState: function(widgetUri, jsonWidgetStates, callback){
		/*
		var widgetStatesJsonObj = intent.data.widgetStates;
		var widgetId = intent.data.widgetId;
		var targetDeviceName = intent.data.target;
		*/
		var params = {
				"widgetUri": widgetUri,
				"widgetStateJson": JSON.stringify(jsonWidgetStates),
				"space": space._uri
		};
		reqDui.put("saveWidgetState", params, callback);
	},
	
	onWidgetRemoved: function(widgetId, spaceUri){
		var params = {
				"widgetId": widgetId,
				"space": spaceUri
		};
		reqDui.put("widgetRemoved", params, function(obj){});
	}
};

return appMgr;

});
