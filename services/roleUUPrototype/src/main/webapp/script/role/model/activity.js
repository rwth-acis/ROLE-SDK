define([ "com", "jquery" ], function(com, $) { 
	
	
	
	var setCookie = function(c_name,value,exdays) {
		var exdate=new Date();
		exdate.setDate(exdate.getDate() + exdays);
		var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
		document.cookie=c_name + "=" + c_value;
	};

	var getCookie = function(c_name) {
		var i,x,y,ARRcookies=document.cookie.split(";");
		for (i=0;i<ARRcookies.length;i++) {
			x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
			y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
			x=x.replace(/^\s+|\s+$/g,"");
			if (x==c_name) {
				return unescape(y);
    		}
    	}
	};
		

	return {

	interfaces : [ "http://purl.org/role/ui/Activity#" ],
	
	_title : null,
	
	_uri : null,
	
	_context: null,

	_widgets: null,
	
	_space: null,
	
	_orderByUser: false,
	
	getUri : function() {
		return this._uri;
	},
	
	getTitle : function() {
		return this._title;
	},
	setTitle : function(title) {
		var self = this;
		var res = new openapp.oo.Resource(this._uri, this._context);
		res.getMetadata(null, function(md) {
			md[openapp.ns.dcterms + "title"] = title;
			res.setMetadata(md, null, function() {
				self._context = res.context;
				self._title = title;
			});
		})
	},
	
	getDescription : function() {
		if (!this._description)	{
			if (this._context !== null && this._context.data !== "" &&
				this._context.data[this._uri]["http://purl.org/dc/terms/description"] != null) {
				this._description = this._context.data[this._uri]["http://purl.org/dc/terms/description"][0].value;
			}
		}
		return this._description;
	},
	
	setTitleAndDescription : function(ntitle, ndesc) {
		var self = this;
		var res = new openapp.oo.Resource(this._uri, this._context);
		res.getMetadata(null, function(md) {
			if (ntitle != null && ntitle != "") {
				md[openapp.ns.dcterms + "title"] = ntitle;
				self._title = ntitle;
			} else {
				md[openapp.ns.dcterms + "title"] = "No title";
				self._title = "No title";
			}
			if (ndesc != null && ndesc != "") {
				md[openapp.ns.dcterms + "description"] = ndesc;
				self._description = ndesc;
			} else {
				delete md[openapp.ns.dcterms + "description"];
				delete self._description;
			}
			res.setMetadata(md, null, function() {});
		});
	},
	
	remove : function() {
		var self = this;
		openapp.resource.del(this._uri, function() {
			var c = getCookie("layouts");
			var obj = {};
			if (c != null) {
				obj = JSON.parse(c);
			}
			delete obj[self._getActivityId()];
			setCookie("layouts", JSON.stringify(obj));
		});
	},

	getWidgets: function() {
		return this._widgets;
	},
	setWidgets: function(widgets) {
		this._owidgets = widgets;
		var widget, counter = 0;
		this._updateLayoutCache();
		for (var i=0;i<this._owidgets.length;i++) {
			widget = this._owidgets[i];
			widget._activity = this;
			widget.__order = this.getWidgetOrder(widget, true);
			widget.__width = this.getWidgetWidth(widget, true);
			if (widget.__order != null && widget.__order - counter > 0) {
				counter = widget.__order;
			}
		}

		for (var i=0;i<this._owidgets.length;i++) {
			widget = this._owidgets[i];
			if (widget.__order == null) {
				counter = counter + 1;
				widget.__order = counter;
			}
		}
		
		this._widgets = this._owidgets.slice(0);
		this._widgets.sort(function(w1, w2) {
			return w1.__order - w2.__order;
		});
		return this._widgets;
	},

	
	addWidget: function(widget) {
		if (this._widgets.length !== 0) {
			widget.__order = this._widgets[this._widgets.length-1].__order + 1;
		} else {
			widget.__order = 1;
		}
		widget._activity = this;
		this._owidgets.push(widget);
		this._widgets.push(widget);
	},
	
	removeWidget: function(widget) {
		for (var i=0;i<this._widgets.length;i++) {
			if (this._widgets[i] === widget) {
				this._widgets.splice(i, 1);
				break;
			}
		}
		for (i=0;i<this._owidgets.length;i++) {
			if (this._owidgets[i] === widget) {
				this._owidgets.splice(i, 1);
				break;
			}
		}
	},
	
	getWidget: function(widgetId) {
		for (var i=0;i<this._owidgets.length;i++) {
			if (widgetId === this._owidgets[i].getRegionWidgetId()) {
				return this._owidgets[i];
			}
		}
	},

	disconnectWidgetCache: function() {
		this._widgets = this._owidgets = null;
		this._aLayout = this._cLayout = null;
	},
	
	setWidgetPosition: function(widgetId, position) {
		var currentPosition, lastOrder, widget;
		this._orderByUser =  true;
		for (var i=0;i<this._widgets.length;i++) {
			if (widgetId === this._widgets[i].getRegionWidgetId()) {
				currentPosition = i;
				widget = this._widgets[i];
				break
			}
		}
		
		if (currentPosition === position) {
			return;
		} else if (currentPosition < position) {
			if (position == this._widgets.length-1) {
				widget.__order = this._widgets[position].__order + 1;
			} else {
				widget.__order = (this._widgets[position].__order + this._widgets[position+1].__order)/2;				
			}
		} else {
			if (position == 0) {
				widget.__order = this._widgets[0].__order - 1;
			} else {
				widget.__order = (this._widgets[position].__order + this._widgets[position-1].__order)/2;				
			}
		}
		this._saveActivityLayout();
		this.setWidgets(this._widgets); //Reorder.
	},
	
	getWidgetWidth: function(widget, ignoreCacheUpdate) {
		ignoreCacheUpdate !== true && this._updateLayoutCache();
		var wid = widget.getRegionWidgetId();
		
		var aa = (this._alayout[wid] || {}).w;
		var ca = (this._clayout[wid] || {}).w;
		var ww;
		if (widget._widget.metadata.modulePrefs) {
			ww = widget._widget.metadata.modulePrefs.width;
		};
		ww = ww != null && ww > 0 ? ww : null;
		if (this._space.isOwner()) {
			return aa || ca || ww;
		} else {
			return ca || aa || ww;
		}
	},
	
	getWidgetHeight: function(widget, ignoreCacheUpdate) {
		ignoreCacheUpdate !== true && this._updateLayoutCache();
		var wid = widget.getRegionWidgetId();
		
		var aa = (this._alayout[wid] || {}).h;
		var ca = (this._clayout[wid] || {}).h;
		var ww;
		if (widget._widget.metadata.modulePrefs) {
			ww = widget._widget.metadata.modulePrefs.height;
		};
		ww = ww != null && ww > 0 ? ww : null;
		if (this._space.isOwner()) {
			return aa || ca || ww;
		} else {
			return ca || aa || ww;
		}
	},

	getWidgetOrder: function(widget, ignoreCacheUpdate) {
		ignoreCacheUpdate !== true && this._updateLayoutCache();
		var wid = widget.getRegionWidgetId();
		
		var aa = (this._alayout[wid] || {}).o;
		var ca = (this._clayout[wid] || {}).o;
		if (this._space.isOwner()) {
			if (aa != null) {
				this._orderByUser =  true;
			}
			return aa || ca;
		} else {
			if (ca != null) {
				this._orderByUser =  true;
			}
			return ca || aa;
		}
	},
	
	setWidgetWidth: function(widget, width) {
		widget.__width = width;
		this._saveActivityLayout();
	},
		
	setWidgetHeight: function(widget, height) {
		widget.__height = height;
		this._saveActivityLayout();
	},
	
	_updateLayoutCache: function() {
		//From activity resource
		var properties = openapp.resource.context(this._context).properties();
		this._alayout = JSON.parse(properties[openapp.ns.role+"layout"] || "{}");
		
		//From cookie
		this._clayout = JSON.parse(getCookie("layouts") || "{}")[this._getActivityId()] || {};
	},
	
	_saveActivityLayout: function() {
		var layout = {}, wid, widget;
		for (var i=0;i<this._widgets.length;i++) {
			widget = this._widgets[i];
			wid = widget.getRegionWidgetId();
			if (this._orderByUser || widget.__width != null) {
				layout[wid] = {};
				if (this._orderByUser) {
					layout[wid].o = ""+widget.__order;
				}
				if (widget.__width != null) {
					layout[wid].w = ""+widget.__width;
				}
			}
		}
		
		if (this._space.isOwner()) {
			var layoutstr = JSON.stringify(layout);
			this._context.subject[openapp.ns.role+"layout"] = layoutstr;
			var ooRes = new openapp.oo.Resource(this._context.uri);
			ooRes.getMetadata(null, function(md) {
				if (layout != null) {
					md[openapp.ns.role+"layout"] = layoutstr;
				} else {
					delete md[openapp.ns.role+"layout"];
				}
				ooRes.setMetadata(md, null); //TODO error handling?
			});
		} else {
			var c = getCookie("layouts");
			var obj = {};
			if (c != null) {
				obj = JSON.parse(c);
			}
			obj[this._getActivityId()] = layout;
			setCookie("layouts", JSON.stringify(obj));
		}
	},
	
	_getActivityId: function() {
		if (!this._id) {
			this._id = this._context.uri.substr(this._context.uri.lastIndexOf('/'));
		}
		return this._id;
	}
}; });