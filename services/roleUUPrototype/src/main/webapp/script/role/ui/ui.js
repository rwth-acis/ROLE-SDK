define([ "com", "jquery", "handlebars!./ui", "handlebars!./panel", "handlebars!./header",
         "handlebars!./content", "../content/browser" ], function(
		com, $, uiTemplate, panelTemplate, headerTemplate, contentTemplate, browser) { return {

	interfaces : [ "http://purl.org/role/ui/Feature#", "http://purl.org/role/ui/UI#" ],
	autoHide: null, 
	embedded: false,
	dashboard: false,
	
	load : function() {
		com.on("http://purl.org/role/ui/DOMReady#", "domReady", function() {
			$(uiTemplate()).appendTo(document.body);
			com.on("http://purl.org/role/ui/Panel#", "add", this.addPanel);
			com.on("http://purl.org/role/ui/Panel#", "remove", this.removePanel);
			com.on("http://purl.org/role/ui/Header#", "add", this.addHeader);
			com.on("http://purl.org/role/ui/Header#", "remove", this.removeHeader);
			com.on("http://purl.org/role/ui/Content#", "add", this.addContent);
			com.on("http://purl.org/role/ui/Content#", "remove", this.removeContent);

			var sidebar = $("#sidebar");
			var sidebarInner = $("#sidebar .sideSectionInner");
			var ui = this;
			sidebar.hover(
				function() {
					if (!ui.autoHide) {return;}
					sidebar.stop().animate({'left': '0px'}, 200);
					sidebarInner.stop().animate({'left': '0px'}, 200);
				},
				function() {
					if (!ui.autoHide) {return;}
					sidebar.stop().animate({'left': '-210px'}, 200);
					sidebarInner.stop().animate({'left': '-210px'}, 200);
				});
		}.bind(this));
	},
	
	setLayout : function(layout) {
		if (layout !== "empty") {
			$("#header").show();
			$("#sidebar").show();
		}
	},
	setIsEmbedded: function(embedded) {
		this.embedded = embedded;
		if (this.embedded) {
			this.autoHideSidePanel(true, 3000, 1500);
		} else {
			this.autoHideSidePanel(false);				
		}
	},
	
	setHasDashboard: function(hasDashboard) {
		this.dashboard = hasDashboard;
	},
	
	autoHideSidePanel: function(autoHide, initialAnimationDelay, initialAnimationTime) {
		if (this.autoHide === autoHide) {
			return;
		}
		this.autoHide = autoHide;
		var sidebar = $("#sidebar");
		var sidebarInner = $("#sidebar .sideSectionInner");
		if (this.autoHide) {
			//Shift pageContent to the left when there is more space
			$("#pageContent").css("margin-left", "15px");
			$("#content-wrapper>.widget").css("left", "15px");
			//Hide the sidepanel, perhaps after an initial delay.
			setTimeout(function() {
				sidebar.stop().animate({'left': '-210px'}, initialAnimationTime || 200);
				sidebarInner.stop().animate({'left': '-210px'}, initialAnimationTime || 200);				
			}, initialAnimationDelay || 1);
		} else {
			//Shift pageContent to the right to make room for the fixed sidePanel
			$("#pageContent").css("margin-left", "222px");
			$("#content-wrapper>.widget").css("left", "222px");
			//Show the sidepanel, parhaps after an initial delay
			setTimeout(function() {
				sidebar.stop().animate({'left': '0px'}, initialAnimationTime || 200);
				sidebarInner.stop().animate({'left': '0px'}, initialAnimationTime || 200);
			}, initialAnimationDelay || 1);
		}
		com.trigger(this, "http://purl.org/role/ui/UI#", "autoHideSidePanel");
	},

	addPanel : function(panel) {
		var panelUI = $(panelTemplate({
			title : panel.getTitle()
		}));
		panel.createUI(panelUI.find(".sideContent").get(0));
		panelUI.appendTo("#sidePanels");
	},

	removePanel : function(panel) {
	},
	
	addHeader : function(header) {
		var headerUI = $(headerTemplate({}));
		header.createUI(headerUI.find(".headerContent").get(0));
		headerUI.appendTo($("#header").find(".header-" + header.position));
	},

	removeHeader : function(header) {
	},
	
	addContent : function(content) {
		var contentUI = $(contentTemplate({}));
		content.createUI(contentUI);
		contentUI.appendTo("#pageContentHeader");
	},

	removeContent : function(content) {
	},
	
	_browserId : null,
	
	browse : function(url, id) {
		if (url && url !== browser.getUrl()) {
			this.content(browser, id);
			browser.setUrl(url);
		} else {
			browser.setUrl();
			this.content();
		}
	},
	
	content: function(component, id) {
		var container = $('#content-wrapper').html("");
		if (this._contentId) {
			$("#sideEntry-" + this._contentId).removeClass("sideEntrySel");
		}
		if (this._contentId != null && this._contentId === id) {
			delete this._contentId;
			return;
		}

		if (component) {
			component.createUI(container);
			if (id) {
				this._contentId = id;
				$("#sideEntry-" + this._contentId).addClass("sideEntrySel");
			}
			container.addClass('widget-wrapper-canvas').fadeIn();
		} else {
			container.fadeOut(0, function() {
				container.removeClass('widget-wrapper-canvas');
			});
		}
	},
	
	canvas : function(newCanvas, dblClick) {
		var currentCanvas = $('.widget-wrapper-canvas').attr('id');
		if (typeof currentCanvas !== "undefined") {
			currentCanvas = currentCanvas.match(/\w+\-([\w\d]+)\-\w+/)[1];
			if (currentCanvas === "widgetStore") {
				this.browse();
			} else {
				$('#widget-' + currentCanvas + '-wrapper').removeClass("widget-wrapper-canvas");
				$("#sideEntry-" + currentCanvas).removeClass("sideEntrySel");
			}
			$(".widget-wrapper").css("visibility", "visible");
			(function(currentCanvas) {
				window.setTimeout(function() {
					var widget = rave.getWidgetById(currentCanvas);
					if (typeof widget !== "undefined") {
						widget.minimize();
						$(".widget-wrapper").find("iframe").attr("width", "100%");
						$(".widget-wrapper-canvas").find("iframe").attr("width", "100%");						
					}
					$('#widget-' + currentCanvas + '-wrapper').removeClass("widget-wrapper-canvas");
					$("#sideEntry-" + currentCanvas).removeClass("sideEntrySel");
				}, 1);
			})(currentCanvas);
		}
		if (typeof newCanvas !== "undefined"
				&& ((typeof currentCanvas === "undefined" && dblClick) || (typeof currentCanvas !== "undefined"
						&& currentCanvas !== newCanvas && currentCanvas !== "widgetStore"))) {
			$(".widget-wrapper").css("visibility", "hidden");
			$('#widget-' + newCanvas + '-wrapper').addClass("widget-wrapper-canvas");
			$("#sideEntry-" + newCanvas).addClass("sideEntrySel");
			this._browserId = newCanvas;
			(function(newCanvas) {
				window.setTimeout(function() {
					rave.getWidgetById(newCanvas).maximize();
				}, 1);
			})(newCanvas);
		}
	}
	
}; });
