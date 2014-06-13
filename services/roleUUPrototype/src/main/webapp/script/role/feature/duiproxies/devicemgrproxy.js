define(["../../model/space", "./reqdui", "./deviceinfo", "detectmobile"], 
		function(space, reqDui, deviceInfo, detectMobile){

var deviceMgr = {
	
	getDeviceList: function(callback){
		console.log("refresh devices");
		reqDui.get("refreshDevices", "", callback);
	},
	
	/**
	 * Get the current device info and typically a list of displayed widgets on current space
	 * @param currentDeviceName
	 * @param callback A function defined as "function(xmlhttpreq){}"
	 */
	getCurrentDeviceInfo: function (currentDeviceName, callback){
		console.log("get current device info");
		var query = {
				"deviceName": currentDeviceName,
				"space": space._uri
		};
		reqDui.get("device", query, callback);
	},
	
	/**
	 * Create a device concept for current user
	 * @param newDeviceName
	 * @param callback A function defined as "function(xmlhttpreq){}"
	 */
	addDevice: function(newDeviceName, callback, isChecked){
		console.log("new device");
		var params = {
				"deviceName": newDeviceName,
		};
		
		if(isChecked){
			var os = deviceInfo.OS;
			var browser = deviceInfo.browser;
			var version = deviceInfo.version;
			var isMobile = detectMobile.isMobile();
			if (os == "unknown" || browser == "unknown"){
				var userAgent = {"userAgent": navigator.userAgent};
				userAgent["isMobile"] = isMobile;
				params["deviceInfo"] = JSON.stringify(userAgent);
			}
			else{
				var dInfo = {
						"os": os, 
						"browser": browser, 
						"version": version,
						"isMobile": isMobile
				};
				params["deviceInfo"] = JSON.stringify(dInfo);
			}
		}
		reqDui.post("newDevice", params, callback);
	},
	
	/**
	 * Set the current device info to the target device
	 * @param deviceName the name of the target device
	 */
	setDeviceProfile: function(deviceName){
		console.log("device profile");
		if (confirm("Do you want to record the current operating system and browser information in device " + deviceName + "? " +
				"Please make sure the device fits the information to be recorded.")){
			var params = {"deviceName": deviceName};
			var os = deviceInfo.OS;
			var browser = deviceInfo.browser;
			var version = deviceInfo.version;
			var isMobile = detectMobile.isMobile();
			if (os == "unknown" || browser == "unknown"){
				var userAgent = {"userAgent": navigator.userAgent, "isMobile": isMobile};
				params["deviceInfo"] = JSON.stringify(userAgent);
			}
			else{
				var dInfo = {"os": os, "browser": browser, "version": version, "isMobile": isMobile};
				params["deviceInfo"] = JSON.stringify(dInfo);
			}
			reqDui.put("deviceProfile", params, function(xmlhttp){});
		}
	},
	
	/**
	 * Get infos of the selected device. Typically the space on which the device is working on and the display widgets
	 * @param deviceName
	 * @param callback A function defined as "function(xmlhttpreq){}"
	 */
	getDeviceInfos: function(deviceName, callback){
		console.log("load selected device config");
		var query = {"deviceName": deviceName};
		reqDui.get("deviceInfo", query, callback);
	},
	
	/**
	 * remove a device concept from the current user
	 * @param removeDeviceName
	 * @param callback A function defined as "function(xmlhttpreq){}"
	 */
	removeDevice: function(removeDeviceName, callback){
		console.log("remove device");
		var query = {"deviceName": removeDeviceName};
		reqDui.del("removeDevice", query, callback);
	},
	
	/**
	 * Initialize a connectivity check
	 * @param callback The server responses OK once it received the req
	 */
	checkConnectivities: function(callback){
		console.log("refresh connectivities");
		reqDui.get("connectivity", "", callback);
	},
	
	/**
	 * Informs the server that the current device is connected
	 * @param atSpace
	 */
	confirmConnected: function(currentDeviceName, atSpace){
//		console.log("confirm connectivity");
		var isAnonymous = false;
		if (currentDeviceName == null || currentDeviceName == ""){
			currentDeviceName = "";
			isAnonymous = true;
		}
		var params = {
				"deviceName": currentDeviceName,
				"isAnonymous": isAnonymous,
				"location": atSpace
		};
		reqDui.put("connected", params, function(xmlhttp){});
	},
	
	/**
	 * Initialize switch the device
	 * @param deviceName The name of the device concept that is to be loaded
	 * @param currentDeviceName
	 * @param callback
	 */
	trySwitchDevice: function(deviceName, currentDeviceName, spaceUri, callback){
		console.log("switch device");
		var params = {
				"fromDevice": currentDeviceName,
				"toDevice": deviceName,
				"space": spaceUri
		};
		reqDui.put("switch", params, callback);
	},
	
	/**
	 * Force to switch to another device even if the target device is currently active somewhere
	 * @param deviceName
	 * @param currentDeviceName
	 */
	confirmSwitchDevice: function(deviceName, currentDeviceName, spaceUri){
		console.log("confirm swtich device");
		var params = {
				"fromDevice": currentDeviceName,
				"toDevice": deviceName,
				"space": spaceUri
		};
		reqDui.put("forceSwitch", params, function(xmlhttpreq){});
	},
	
	/**
	 * notify a new device load to kick another device with the same device name offline
	 * @param deviceName
	 */
	notifyNewDeviceLoad: function(deviceName, callback){
		console.log("new device load");
		params = {
				"deviceName": deviceName,
				"space": space._uri
		};
		reqDui.put("newLoad", params, callback);
	},
	
	notifyDeviceOut: function(deviceName){
		console.log("device sign out");
		params = {
				"deviceName": deviceName,
		};
		reqDui.put("signOut", params, function(xmlhttpreq){});
	},
	
	changeDeviceAlias: function(deviceId, newName, callback){
		console.log("change device alias");
		params = {
				"deviceName": deviceId,
				"newName": newName
		};
		reqDui.put("changeDeviceAlias", params, callback);
	}
};
return deviceMgr;
});