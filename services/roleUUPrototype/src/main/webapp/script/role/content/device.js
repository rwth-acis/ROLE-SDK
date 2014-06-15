/**
 * add this feature using com.add() at /role/view/learningspace.js.
 * and this file along with the device.html template should be put into /role/panel
 */
define(["com","jquery", "./members", "handlebars!./devices", "handlebars!./device", "handlebars!./widgetli"], 
		function(com, $, members, devicesTemplate, dvTemplate, widgetLi){
	var duiManager = null;
	var timer = null;
	var devicePanel = {
		interfaces: ["http://purl.org/role/ui/Panel#"],
		that: this,
		
		getTitle: function(){
			return "Devices";
		},
		
		createUI: function(container){
			$(devicesTemplate()).appendTo(container);
		},
		
		load: function(duiMgr){
			duiManager = duiMgr;
			$("#sideEntry-deviceManager").click(function(){
				$("#content-deviceManager").toggle();
				if ($("#sideEntry-deviceManager").hasClass("sideEntrySel"))
					$("#sideEntry-deviceManager").removeClass("sideEntrySel");
				else
					$("#sideEntry-deviceManager").addClass("sideEntrySel");
				duiManager.refreshGUI();
			}.bind(this));
			$("#dui-device-panel-idleWidgets").sortable({
				connectWith: '.duiSortable',
				scroll: true,
				axis: 'y',
				receive: function(event, ui){
					$(ui.sender).sortable('cancel');
				},
				stop: this.stop
			});
			$("#panel-img-refresh").click(function(){
				duiManager.refreshConnectivities();
			});
			$("#panel-img-add").click(function(){
				var newDeviceName = prompt("Enter the name of the device that is to be added.", "");
				if (newDeviceName != null && newDeviceName != "")
					duiManager.newDevice(newDeviceName);
			});
			$("#panel-img-show").click(function(){
				$("#panel-img-show").hide();
				$("#panel-img-hide").show();
				$("#panel-dContainer").show();
			});
			$("#panel-img-hide").click(function(){
				$("#panel-img-hide").hide();
				$("#panel-img-show").show();
				$("#panel-dContainer").hide();
			});
		},
		
		createDevicePanel: function(deviceName){
			var container = $("#dui-device-panel").get(0);
			$(dvTemplate({"deviceName": deviceName})).appendTo(container);
			$("#dui-device-ul-"+deviceName).sortable({
				connectWith: '.duiSortable',
				scroll: true,
				stop: this.stop,
				axis: 'y'
			});
			$("#dui-device-panel-title-"+deviceName).click(function(){
				var dName = $(this).attr('id').substring("dui-device-panel-title-".length);
				if ($("#dui-device-profile-"+dName).is(":hidden")){
					$("#dui-device-profile-"+dName).toggle();
					var profile = duiManager.getDeviceProfile(dName);
					if (profile == null){
						$("#"+dName+"-ua").html("Not profiled");
						return;
					}
					if (profile.hasOwnProperty("userAgent")){
						$("#"+dName+"-ua").html("uncategoried: " + profile.userAgent);
					}
					else{
						$("#"+dName+"-ua").empty();
						if (profile.hasOwnProperty("os")){
							$("#"+dName+"-os").html("OS:  " + profile.os);
						}
						else
							$("#"+dName+"-os").empty();
						if (profile.hasOwnProperty("browser")){
							var text = "Browser:  " + profile.browser;
							if (profile.hasOwnProperty("version")&&profile.version!="unknown")
								text = text + " v"+profile.version;
							$("#"+dName+"-browser").html(text);
						}
						else
							$("#"+dName+"-browser").empty();
					}
				}
				else
					$("#dui-device-profile-"+dName).toggle();
			});
			$("#d-panel-del-"+deviceName).click(function(e){
				var dName = $(this).attr('id').substring("d-panel-del-".length);
				duiManager.removeDevice(dName);
				e.stopPropagation();
			});
			$("#d-panel-set-"+deviceName).click(function(e){
				var dName = $(this).attr('id').substring("d-panel-set-".length);
				duiManager.saveConfig(dName);
				e.stopPropagation();
			});
		},
		
		createWidgetLi: function(widgetId, title, deviceName){
			var container = $("#dui-device-ul-"+deviceName).get(0);
			$(widgetLi({"regionWidgetId": widgetId, "title": title})).appendTo(container);
		},
		
		createIdleWidget: function(widgetId, title, createdByOthers){
			var container = $("#dui-device-panel-idleWidgets").get(0);
			$(widgetLi({"regionWidgetId": widgetId, "title": title})).appendTo(container);
			if (createdByOthers){
				var popup = $("#popupinfo");
				popup.html("New idle widget incoming");
				if (popup.css('display') == 'none')
					popup.fadeIn("slow");
				if (timer && typeof timer["clearTimeout"] == "function")
					timer.clearTimeout();
				timer = setTimeout(function(){
					$("#popupinfo").html("");
					if ($("#popupinfo").css("display") != "none")
						$("#popupinfo").fadeOut("slow");
				}, 3000);
			}
		},
		
		stop: function(event, ui){
			if ($(this).attr('id') == ui.item.parent().attr('id'))
				return;
			var sourceDeviceName = $(this).attr('id');
			sourceDeviceName = sourceDeviceName.substring("dui-device-ul-".length);
			var targetDeviceName = ui.item.parent().attr('id');
			targetDeviceName = targetDeviceName.substring("dui-device-ul-".length);
			var widgetId = ui.item.attr('id');
			widgetId = widgetId.substring("dui-device-panel-widget-".length);
			duiManager.migrateWidget(widgetId, targetDeviceName, sourceDeviceName);
		},
		
		setDeviceOnline: function(deviceName){
			var el = $("#dui-device-panel-title-"+deviceName);
			if (!el.hasClass("duiTitleConn")){
				el.removeClass("duiTitleNotConn");
				el.addClass("duiTitleConn");
				var parent = $("#dui-device-panel-"+deviceName);
				if (!parent.hasClass("duiCurrentDevice"))
					parent.insertAfter("#dui-panel-remote-division");
				if ($("#sidebar").css('display') != 'none')
					parent.effect("bounce", function(){
						$.each($("div .ui-effects-wrapper"), function(i, ele){
							if (ele.innerHTML == '')
								ele.parentNode.removeChild(ele);
						});
					});
				else{
					var popup = $("#popupinfo");
					popup.html("Device '" + deviceName + "' is online");
					if (popup.css('display') == 'none')
						popup.fadeIn("slow");
					if (timer && typeof timer["clearTimeout"] == "function")
						timer.clearTimeout();
					timer = setTimeout(function(){
						$("#popupinfo").html("");
						if ($("#popupinfo").css("display") != "none")
							$("#popupinfo").fadeOut("slow");
					}, 3000);
				}
			}
		},
		
		setDeviceOffline: function(deviceName){
			var el = $("#dui-device-panel-title-"+deviceName);
			if (!el.hasClass("duiTitleNotConn")){
				el.removeClass("duiTitleConn");
				el.addClass("duiTitleNotConn");
				var parent = $("#dui-device-panel-"+deviceName);
				if ($("#sidebar").css('display') != 'none')
					parent.effect("bounce", function(){
						$.each($("div .ui-effects-wrapper"), function(i, ele){
							if (ele.innerHTML == '')
								ele.parentNode.removeChild(ele);
						});
					});
				else{
					var popup = $("#popupinfo");
					popup.html("Device '" + deviceName + "' is offline");
					if (popup.css('display') == 'none')
						popup.fadeIn("slow");
					if (timer && typeof timer["clearTimeout"] == "function")
						timer.clearTimeout();
					timer = setTimeout(function(){
						$("#popupinfo").html("");
						if ($("#popupinfo").css("display") != "none")
							$("#popupinfo").fadeOut("slow");
					}, 3000);
				}
			}
		},
		
		removeDevice: function(deviceName){
			var idlewidgetcontainer = $("#dui-device-panel-idleWidgets").get(0);
			$("#dui-device-ul-"+deviceName).children().appendTo(idlewidgetcontainer);
			$("#dui-device-panel-"+deviceName).remove();
		},
		
		removeWidget: function(widgetId){
			$("#dui-device-panel-widget-"+widgetId).remove();
		},
		
		moveWidgetLi: function(widgetId, targetDevice){
			var container = $("#dui-device-ul-"+targetDevice).get(0);
			$("#dui-device-panel-widget-"+widgetId).appendTo(container);
		},
		
		setWidgetLiText: function(widgetId, title){
			$("#dui-device-panel-widget-"+widgetId).empty();
			var li = document.getElementById("dui-device-panel-widget-"+widgetId);
			if (li != null)
				li.appendChild(document.createTextNode(title));
		},
		
		bumpToTop: function(deviceName){
			var top = $("#dui-device-panel-"+deviceName);
			top.addClass("duiCurrentDevice");
			top.insertBefore("#dui-panel-remote-division");
		},
		
		
		fallback: function(widgetId, sourceDevice){
			if (sourceDevice != null)
				this.moveWidgetLi(widgetId, sourceDevice);
			var popup = $("#popupinfo");
			popup.html("widget '" + duiManager.widgetIndex[widgetId].title + "' migration failed: source device timed out");
			if (popup.css('display') == 'none')
				popup.fadeIn("slow");
			if (timer && typeof timer["clearTimeout"] == "function")
				timer.clearTimeout();
			timer = setTimeout(function(){
				$("#popupinfo").html("");
				if ($("#popupinfo").css("display") != "none")
					$("#popupinfo").fadeOut("slow");
			}, 3000);
		}
		
	};
	
	return devicePanel;
});
