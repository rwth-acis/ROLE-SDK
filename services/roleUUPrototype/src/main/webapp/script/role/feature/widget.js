define([ "com", "jquery", "../model/space", "../model/widget", "./activity", "rave" ], function(
		com, $, space, Widget, activityFeature, rave) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#" ],
	
	load : function() {
		var _currentActivity = null;
		com.on("http://purl.org/role/ui/Activity#", "select", function(currentActivity) {
			if (_currentActivity) {
				_currentActivity.disconnectWidgetCache();
			}
			_currentActivity = currentActivity;

			var tools, widgets, widgetUrls, widgetContexts;
			var tool, properties, activity, i, widget;
			
			com.clear("http://purl.org/role/ui/Widget#");
			
			tools = openapp.resource.context(space._context).sub(
					openapp.ns.role + "tool").list();
					
			widgets = [];
			widgetUrls = [];
			widgetContexts = [];
			for ( i = 0; i < tools.length; i++) {
				tool = tools[i];
				properties = openapp.resource.context(tool)
						.properties();

				activity = properties[openapp.ns.role + "activity"];
				if (((typeof activity === "undefined" || activity === openapp.ns.role
						+ "activity/Overview")
						&& (currentActivity.getUri() !== openapp.ns.role + "activity/Overview"
								&& currentActivity.getUri() !== space.getUri()))
						|| (typeof activity !== "undefined"
								&& activity !== currentActivity.getUri()
								&& activity !== openapp.ns.role + "activity/Overview"
								&& !(currentActivity.getUri() !== null
								&& currentActivity.getUri().substring(currentActivity.getUri().length - 6) === "/Share" && activity
								.substring(activity.length - 6) === "/Share"))) {
					continue;
				}
				
				widget = this._prepareWidget(properties, currentActivity);
				widgets.push(widget);
				widgetUrls.push(widget.widgetUrl);
				widgetContexts.push(tool);
			}
			this._getGadgetSpecs(widgetUrls, function(obj) {
				var i, widgetInstances = [];
				for (i = 0; i < widgets.length; i++) {
					widgets[i].metadata = obj.data.result[widgets[i].widgetUrl];
					widgetInstances.push(this._processWidget(widgets[i], obj, widgetContexts[i]));
				}
				
				//Set to activity and get new order.
				widgetInstances = currentActivity.setWidgets(widgetInstances);
				for (i=0;i<widgetInstances.length;i++) {
					com.add(widgetInstances[i]);
				}
				this._postprocessWidgets(widgetInstances);
			}.bind(this));
			com.trigger(currentActivity, "http://purl.org/role/ui/Activity#", "update");
		}.bind(this));
	},
	
	_processWidgetUrl : function(widgetUrl, currentActivity) {
		if (decodeURIComponent($.url(widgetUrl).param("openapp.ns.role")) === openapp.ns.role) {
			widgetUrl = widgetUrl + "&openapp.role.activity=" +
				encodeURIComponent(currentActivity.getUri());
		}
		return widgetUrl;
	},
	
	_prepareWidget : function(properties, currentActivity) {
		var preferences;
		preferences = gadgets.json.parse(properties[openapp.ns.widget + "preferences"] || "{}");
		return {
			type : 'OpenSocial',
			regionWidgetId : properties[openapp.ns.widget
					+ "moduleId"],
			widgetUrl : this._processWidgetUrl(properties[openapp.ns.role + "widget"], currentActivity),
			_widgetSource : properties["http://www.w3.org/2002/07/owl#sameAs"],
			securityToken : decodeURIComponent(properties[openapp.ns.widget
					+ "securityToken"]),
			metadata : {},
			userPrefs : preferences
		};
	},
	
	_processWidget : function(widget, specResults, widgetContext) {
		var hasPrefsToEdit, userPrefs, widgetInstance;
		widget.metadata = specResults.data.result[widget.widgetUrl];
		if (typeof widget.metadata !== "undefined") {
			hasPrefsToEdit = false;
			userPrefs = widget.metadata.userPrefs;
			if (typeof userPrefs !== "undefined") {
				$.each(userPrefs, function(userPrefName, userPref) {
					if (typeof userPref.dataType === "undefined"
							|| userPref.dataType !== "HIDDEN") {
						hasPrefsToEdit = true;
					}
				});
				widget.metadata.hasPrefsToEdit = hasPrefsToEdit;
			} else {
				widget.metadata = {};
			}
			widgetInstance = Object.create(Widget);
			widgetInstance._widget = widget;
			widgetInstance._context = widgetContext;
			widgetInstance._space = space;
			return widgetInstance;
		}
	},
	
	_postprocessWidgets : function(activity, widgetInstances) {
		this._fixIframes();
		window.setTimeout((function() {
			this._showWidgets(true);
		}).bind(this), 400);
	},
	
	_getGadgetSpecs : function(widgetUrls, callback) {
		var request = {
			method : "gadgets.metadata",
			id : "gadgets.metadata",
			params : {
				ids : widgetUrls,
				country : "default",
				language : "default",
				view : "home",
				container : "default",
				fields : [ "iframeUrl", "modulePrefs.*", "userPrefs.*",
						"iframeUrl", "views.preferredHeight",
						"views.preferredWidth", "expireTimeMs",
						"responseTimeMs" ],
				userId : "@viewer",
				groupId : "@self"
			}
		};

		var makeRequestParams = {
			"CONTENT_TYPE" : "JSON",
			"METHOD" : "POST",
			"POST_DATA" : gadgets.json.stringify(request)
		};

		gadgets.io.makeNonProxiedRequest("/rpc", callback,
				makeRequestParams, "application/javascript");
	},
	
	_showWidgets : function(isAddingWidget) {
		if (!isAddingWidget) {
			//$("html").css("overflow", "auto");
			animatingWidgets = false;
			$(".widget-wrapper").css("display", "none").css(
					"visibility", "visible");
		} else {
			var hasWidgetCanvas = $(".widget-wrapper-canvas").length>0 &&
									$(".widget-wrapper-canvas").attr('id').match(/\w+\-([\w\d]+)\-\w+/)[1] != "widgetStore";
			$(".widget-wrapper").each(
					function() {
						if ($(this).css("visibility") === "hidden") {
							$(this).css(
									"visibility", "visible");
						}
						if ( hasWidgetCanvas && !$(this).hasClass("widget-wrapper-canvas"))
							$(this).css("display", "none");
					});
		}
		if ($(".widget-wrapper-canvas").get(0) == null)    //ke li: if maximized widget does not exist, fade in  
			$(".widget-wrapper").fadeIn();
	},
	
	_fixIframes : function() {
		$(".widget-wrapper").find("iframe").attr("width", "100%");
		$(".widget-wrapper-canvas").find("iframe").attr("width", "100%");
		//$("html").css("overflow", "auto");
	},
	
	addWidget : function(gadgetUrl, message) {
		var defaultUrl = "http://";
		if (message) {
			defaultUrl = gadgetUrl;
			gadgetUrl = undefined;
		}
		gadgetUrl = gadgetUrl || prompt(message
			|| "Enter the URL of the OpenSocial gadget that is to be added.", defaultUrl);
		if (gadgetUrl !== null && gadgetUrl.trim().length > 0) {
			this._getGadgetSpecs([ gadgetUrl ], function(gadgetSpec) {
				var currentActivity = null, newTool;
				if (gadgetSpec.data.result[gadgetUrl].hasOwnProperty("error")) {
					this.addWidget(
							gadgetUrl,
							"The widget could not be added. Please make sure that the URL is correct."
									+ "\n\nDetailed error information:\n"
									+ gadgetSpec.data.result[gadgetUrl].error.message);
					return;
				}
				com.one("http://purl.org/role/ui/Activity#", "select", function(activity) {
					currentActivity = activity;
				});
				newTool = openapp.resource.context(space._context).sub(openapp.ns.role + "tool")
					.type(openapp.ns.role + "OpenSocialGadget")
					.seeAlso(gadgetUrl);
				if (currentActivity.getUri() !== openapp.ns.role + "activity/Overview"
						&& currentActivity.getUri() !== space.getUri()) {
					newTool.control(openapp.ns.role + "activity", currentActivity.getUri());
				}
				newTool.create(function(tool) {
					space.refresh(function() {
						var tools, i, widget;
						tools = openapp.resource.context(space._context).sub(
								openapp.ns.role + "tool").list();
						for (i = 0; i < tools.length; i++) {
							if (tools[i].uri === tool.uri) {
								widget = this._prepareWidget(openapp.resource.context(tools[i])
										.properties(), currentActivity);
								this._getGadgetSpecs([ widget.widgetUrl ], function(obj) {
									widget.metadata = obj.data.result[widget.widgetUrl];
									var wi = this._processWidget(widget, obj, tools[i]);
									currentActivity.addWidget(wi);
									com.add(wi);
									this._postprocessWidgets();
								}.bind(this));
								break;
							}
						}
					}.bind(this));
				}.bind(this));
			}.bind(this));
		}
	}
	
}; });