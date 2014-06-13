/**
 * add this content using com.add() at /role/view/space.js
 * and this file along with duimgrui.html template file should be put into /role/content
 */
define(["com", "jquery", "../feature/duimanager", "handlebars!./duimgrui"], function(com, $, duiManager, duimgruiTemplate){
	return {
		interfaces: [ "http://purl.org/role/ui/Content#" ],
		
		duiMgrUI: null,
		
		createUI: function(container){
			if (this.duiMgrUI == null){
				this.duiMgrUI = $(duimgruiTemplate());
				this.duiMgrUI.appendTo(container);
				
				//clear template sample codes
				$(this.duiMgrUI).find("#dui-currentDeviceName-label").empty();
				$(this.duiMgrUI).find("#dui-conn-div").empty();
				$(this.duiMgrUI).find("#dui-migration-div").empty();
				$(this.duiMgrUI).find("#dui-widgetState-div").empty();
				$(this.duiMgrUI).find("#dui-appStates").empty();
				//the prototype page is in wibaWebAppDemo#index.html
				$(this.duiMgrUI).on("click", "#dui-currentDevice-btn", function(){duiManager.getCurrentDeviceInfo();});
				$(this.duiMgrUI).on("click", "#dui-reconnect-btn", function(){duiManager.reconnect();});
				$(this.duiMgrUI).on("click", "#dui-newDevice-btn", function(){duiManager.newDevice();});
				$(this.duiMgrUI).on("click", "#dui-removeDevice-btn", function(){duiManager.removeDevice();});
				$(this.duiMgrUI).on("click", "#dui-refreshConn-btn", function(){duiManager.refreshConnectivities();});
				$(this.duiMgrUI).find("#dui-selectDevice").change(function(){duiManager.loadSelectedDeviceConfig();});
				$(this.duiMgrUI).on("click", "#dui-loadConfig-btn", function(){duiManager.loadSelectedDeviceConfig();});
				$(this.duiMgrUI).on("click", "#dui-saveConfig-btn", function(){duiManager.saveConfig();});
				$(this.duiMgrUI).on("click", "#dui-switch-btn", function(){duiManager.switchToSelectedDevice();});
				$(this.duiMgrUI).on("click", "#dui-widgetLocation-btn", function(){duiManager.getWidgetsCurrentLocations();});
				$(this.duiMgrUI).find("#dui-selectWidget").change(function(){duiManager.reloadWidgetState();});
				$(this.duiMgrUI).on("click", "#dui-loadWidgetState-btn", function(){duiManager.reloadWidgetState();});
				$(this.duiMgrUI).on("click", "#dui-loadAppState-btn", function(){duiManager.reloadAppState();});
				$(this.duiMgrUI).on("click", "#dui-migSel-btn", function(){duiManager.migrateSelected();});
				$(this.duiMgrUI).find("#dui-accordion").accordion({
					clearStyle: true,
					collapsible: true,
					active: false
				});
			}
		}
	};
});