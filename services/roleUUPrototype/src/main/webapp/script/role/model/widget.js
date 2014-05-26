define([ "com", "jquery" ], function(com, $) {
	
	return {

	interfaces : [ "http://purl.org/role/ui/Widget#" ],
	
	_widget : {},
	
	_context : null,

	_space : null,
	
	_activity: null,
	
	getPrefsTitle : function() {
		return typeof this._widget.metadata.modulePrefs !== "undefined"
			? this._widget.metadata.modulePrefs.title : "Widget";
	},

	getTitle : function() {
		if (this._context !== null && this._context.data !== "" &&
			this._context.data[this._context.uri]["http://purl.org/dc/terms/title"] != null) {
			return this._context.data[this._context.uri]["http://purl.org/dc/terms/title"][0].value;
		}
		return this.getPrefsTitle();
	},
	
	getDescription : function() {
		if (this._context !== null && this._context.data !== "" && 
			this._context.data[this._context.uri]["http://purl.org/dc/terms/description"] != null) {
			return this._context.data[this._context.uri]["http://purl.org/dc/terms/description"][0].value;
		}
		return null;
	},

	setTitleAndDescription : function(ntitle, ndesc) {
		var self = this;
		var res = new openapp.oo.Resource(this._context.uri, this._context);
		res.getMetadata(null, function(md) {
			if (ntitle != null && ntitle != "") {
				md[openapp.ns.dcterms + "title"] = ntitle;				
			} else {
				md[openapp.ns.dcterms + "title"] = "No title";
			}
			if (ndesc != null && ndesc != "") {
				md[openapp.ns.dcterms + "description"] = ndesc;				
			} else {
				delete md[openapp.ns.dcterms + "description"];
			}
			res.setMetadata(md, null, function() {
				self._context = res.context;
			});
		});
	},
	
	getRegionWidgetId : function() {
		return this._widget.regionWidgetId;
	},
	
	getWidth: function() {
		return this._activity.getWidgetWidth(this);
	},
	
	setWidth: function(w) {
		this._activity.setWidgetWidth(this, w);
	},
/*	getWidth: function() {
		//Fetch from tool resource.
		var properties = openapp.resource.context(this._context).properties();
		var ow = properties[openapp.ns.role+"width"];
		if (this._space.isOwner()  && ow != null) {
			return ow;
		}

		//Fetch from cookie.
		var cw = getCookie(this.getRegionWidgetId());
		if (cw != null) {
			var o = JSON.parse(cw);
			return o.w;
		}
		
		return ow;
	},
	
	setWidth: function(width) {
		if (this._space.isOwner()) {
			var ooRes = new openapp.oo.Resource(this._context.uri);
			ooRes.getMetadata(null, function(md) {
				md[openapp.ns.role+"width"] = ""+width;
				ooRes.setMetadata(md, null); //TODO error handling?
			});
		} else {
			var c = getCookie(this.getRegionWidgetId());
			var obj = {};
			if (c != null) {
				obj = JSON.parse(c);
			}
			obj.w = ""+width;
			setCookie(this.getRegionWidgetId(), JSON.stringify(obj));
		}
	},
	
	getOrder: function() {
		//Fetch from tool resource.
		var properties = openapp.resource.context(this._context).properties();
		var ow = properties[openapp.ns.role+"order"];
		if (this._space.isOwner()  && ow != null) {
			return ow;
		}

		//Fetch from cookie.
		var cw = getCookie(this.getRegionWidgetId());
		if (cw != null) {
			var o = JSON.parse(cw);
			return o.o;
		}
		
		return ow;
	},
	
	setOrder: function(order) {
		if (this._space.isOwner()) {
			var ooRes = new openapp.oo.Resource(this._context.uri);
			ooRes.getMetadata(null, function(md) {
				md[openapp.ns.role+"order"] = ""+order;
				ooRes.setMetadata(md, null); //TODO error handling?
			});
		} else {
			var c = getCookie(this.getRegionWidgetId());
			var obj = {};
			if (c != null) {
				obj = JSON.parse(c);
			}
			obj.o = ""+order;
			setCookie(this.getRegionWidgetId(), JSON.stringify(obj));
		}
	},

	clearLayout: function() {
		if (this._space.isOwner()) {
			var ooRes = new openapp.oo.Resource(this._context.uri);
			ooRes.getMetadata(null, function(md) {
				delete md[openapp.ns.role+"width"];
				delete md[openapp.ns.role+"order"];
				ooRes.setMetadata(md, null); //TODO error handling?
			});
		} else {
			setCookie(this.getRegionWidgetId(), "", -1);
		}
		
	},*/

	getUri : function() {
		return this._context.uri;
	}
	
}; });