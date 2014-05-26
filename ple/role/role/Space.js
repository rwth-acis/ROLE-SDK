dojo.provide("role.Space");

dojo.declare("role.Space", null, {
	baseUrl: "http://"+ window.location.host,
	base: document.location.protocol + "//" + document.location.host,
	/*
	 * An array containing pairs of moduleIds and urls to gadget specifications, e.g.:
	 * [{"moduleId": 1, url: "http://example.com/gadget.xml"}, ...]
	 */
	gadgets: [],
	constructor: function(params) {
		this.gadgetIds = [];
		this.metadata = {};
		dojo.mixin(this, params);
	},
	renderGadgets: function() {
		var xhrArgs = {
			url: this.base+"/social/rest/appdata/"+this.spaceId+"/@self/@all",
			handleAs: "json-comment-optional",
			headers: {"accept": "application/json",
						"Content-type": "application/json"},
			load: dojo.hitch(this, function(data) {
				if (data.entry && data.entry[this.spaceId]) {
				this.gadgets = dojo.fromJson(data.entry[this.spaceId]["gadgets"]);
					if (dojo.isArray(this.gadgets)) {
						this.getMetadata(dojo.hitch(this, function() {
							dojo.forEach(this.gadgets, this.renderGadget, this);
							this.afterRenderGadgets();
						}));
					}
				}
			})
		};
		dojo.xhrGet(xhrArgs);
    },
	afterRenderGadgets: function() {		
	},
	getMetadata: function(callback) {
		var base = document.location.protocol + "//" + document.location.host;
		var request = {
			context: {
				country: "default",
				language: "default",
				view: "home",
				container: "default"
			},
      		gadgets: this.gadgets
    	};
				
		var xhrArg = {
//			url: base+"/gadgets/metadata%3Fst%3D0%3A0%3A0%3A0%3A0%3A0%3A0",
			url: base+"/gadgets/metadata?st="+this.spaceId+":"+shindig.container.layoutManager.getUser(),
			handleAs: "json-comment-optional",
			headers: {"Accept": "application/json",
						"Content-type": "application/json; charset=UTF-8"},
			load: dojo.hitch(this, function(data) {
				this.cacheMetadata(data.gadgets);
				callback();
			}),
			postData: dojo.toJson(request)};

		dojo.rawXhrPost(xhrArg);
	},
	cacheMetadata: function(metadata) {
		dojo.forEach(metadata, function(gmd) {
			this.metadata[gmd.url] = gmd;
		},this);
	},
	getSecureToken: function(gadgetObj) {
		var url = gadgetObj.url;
		var mid = gadgetObj.moduleId;
//		var st = this.spaceId+":"+gadgets.container.layoutManager.getUser()+":"+mid+":default:"+encodeURIComponent(url)+":"+mid+":1";
		var st = this.spaceId+":"+shindig.container.layoutManager.getUser()+":"+mid+":default:url:"+mid+":1";
		return st;
	},
	renderGadget: function(gadgetObj) {
		var url = gadgetObj.url;
		var g = shindig.container.createGadget({
            specUrl: url,
			secureToken: this.getSecureToken(gadgetObj),
			userPrefs: this.metadata[url].userPrefs
        });
		
		var subClass = false ? shindig.OAAIfrGadget : shindig.IfrGadget;
		for (var name in subClass) if (subClass.hasOwnProperty(name)) {
			g[name] = subClass[name];
		}

		g.title = this.metadata[url].title;
		g.spaceId = this.id;
		if (this.metadata[url] && this.metadata[url].height) {
//			dojo.style(ifr, "height", this.metadata[url].height)
			g.height = this.metadata[url].height;
		}
        shindig.container.addGadget(g);
        shindig.container.renderGadget(g);
        var ifr = dojo.byId("remote_iframe_"+g.id);
        ifr.src = g.getIframeUrl();
		this.gadgetIds.push(g.id);
	},
	getGadgetChrome: function(chromeId, gadget) {
		var node = dojo.byId(chromeId);
		if (node) {
			return node;
		}
		return this.createGadgetChrome(chromeId, gadget);
	},
	createGadgetChrome: function(chromeId) {
		//override me
	},
	getMaximizeNode: function() {
		//Override me
	},
	getGadgets: function() {
		return dojo.map(this.gadgetIds, shindig.container.getGadget, shindig.container);
	},
	getParticipants: function() {
		return dojo.isArray(this.participants) ? this.participants : [];
	},
	getFascilitators: function() {
		return dojo.isArray(this.fascilitators) ? this.fascilitators: [];
	}
});