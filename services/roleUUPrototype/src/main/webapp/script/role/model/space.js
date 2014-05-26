define([ "com", "jquery", "./widget", "./activity", "../ui/ui", "../model/user" ], function(
		com, $, widgetComponent, activityComponent, ui, user) { return {

	interfaces : [ "http://purl.org/role/ui/Space#" ],
	
	_uri : null,
	
	_context : null,
	
	_isCollaborative : false,
	
	_isPersonal : false,
	
	_isMember : false,
	
	_isOwner : false,
	
	_memberUri : null,
	
	_ownerUri : null,
	
	_firstInit : true,
	
	load : function(uri) {
		this._uri = uri;
		openapp.resource.get(this._uri, function(context) {
			this.initialize(context);
		}.bind(this));
	},
	
	refresh : function(callback) {
		openapp.resource.get(this._uri, function(context) {
			this._context = context;
			callback();
		}.bind(this));
	},
	
	initialize : function(context) {
		this._context = context;
		com.one("http://purl.org/role/ui/User#", "load", this.initializeWithUser.bind(this));
	},
	
	initializeWithUser : function(user) {
		this._isCollaborative = this._context.link.rdf.hasOwnProperty(openapp.ns.role
				+ "tool")
				&& this._context.link.rdf.hasOwnProperty(openapp.ns.foaf
						+ "member");
		this._isPersonal = this._context.link.rdf.hasOwnProperty(openapp.ns.role
				+ "tool")
				&& !this._context.link.rdf.hasOwnProperty(openapp.ns.foaf
						+ "member");
		if (this._isCollaborative) {
			var members = openapp.resource.context(
					this._context).sub(
					openapp.ns.foaf + "member").list();
			this._isMember = false;
			for ( var i = 0; i < members.length; i++) {
				var memberProperties = openapp.resource
						.context(members[i]).properties();
				if (user._uri === memberProperties["http://www.w3.org/2002/07/owl#sameAs"]) {
					this._memberUri = members[i].uri;
					this._isMember = true;
				}
			}
			
			var owners = openapp.resource.context(
					this._context).sub(
					openapp.ns.openapp + "owner").list();
			this._isOwner = false;
			for ( i = 0; i < owners.length; i++) {
				var ownerProperties = openapp.resource
						.context(owners[i]).properties();
				if (user._uri === ownerProperties["http://www.w3.org/2002/07/owl#sameAs"]) {
					this.ownerUri = owners[i].uri;
					this._isOwner = true;
				}
			}
		} else {
			this._isMember = false;
			this._isOwner = true;
		}
		
		if (this._firstInit) {
			com.trigger(this, "http://purl.org/role/ui/Space#", [ "load", "update",
		        this._isMember ? "join" : "!join",
				this._isMember ? "!leave" : "leave" ]);
			this._firstInit = false;
		} else {
			com.trigger(this, "http://purl.org/role/ui/Space#", [ "update",
  		        this._isMember ? "join" : "!join",
  				this._isMember ? "!leave" : "leave" ]);			
		}
	},
	
	getUri : function() {
		return this._uri;
	},
	
	getTitle : function() {
		if (this._context !== null && this._context.data !== "" &&
			this._context.data[this._uri]["http://purl.org/dc/terms/title"] != null) {
			return this._context.data[this._uri]["http://purl.org/dc/terms/title"][0].value;
		}
		return null;
	},

	setTitle : function(ntitle) {
		var self = this;
		var res = new openapp.oo.Resource(this._uri, this._context);
		res.getMetadata(null, function(md) {
			md[openapp.ns.dcterms + "title"] = ntitle;
			res.setMetadata(md, null, function() {
				self._context = res.context;
			});
		});
	},
	
	isMemberAllowedToAddTools: function() {
		if (this._context !== null && this._context.data !== "" &&
			this._context.data[this._uri]["http://purl.org/role/terms/memberAllowedToAddTools"] != null) {
			return true;
		}
		return false;	
	},
	
	getDescription : function() {
		if (this._context !== null && this._context.data !== "" &&
			this._context.data[this._uri]["http://purl.org/dc/terms/description"] != null) {
			return this._context.data[this._uri]["http://purl.org/dc/terms/description"][0].value;
		}
		return null;
	},

	setTitleAndDescription : function(ntitle, ndesc, nallowedToAddTools) {
		var self = this;
		var res = new openapp.oo.Resource(this._uri, this._context);
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
			if (nallowedToAddTools != null && nallowedToAddTools !== false) {
				md["http://purl.org/role/terms/memberAllowedToAddTools"] = "true";
			} else {
				delete md["http://purl.org/role/terms/memberAllowedToAddTools"];
			}
			res.setMetadata(md, null, function() {
				self._context = res.context;
			});
		});
	},
	
	getSubtitle : function() {
		return this._isPersonal ? "Personal Space" : (this._mode === "profile" ? "Profile" : "Learning Space");
	},
	
	isCollaborative : function() {
		return this._isCollaborative;
	},
	
	isPersonal : function() {
		return this._isPersonal;
	},
	
	isMember : function() {
		return this._isMember;
	},
	
	isOwner : function() {
		return this._isOwner;
	},
	
	join : function() {
		if (this._isMember) {
			return;
		}
		openapp.resource.context(this._context).sub(openapp.ns.foaf + "member")
		.type(openapp.ns.foaf + "Person").seeAlso(user._uri)
		.create(function(member) {
			this._isMember = true;
			this.load(this._uri);
		}.bind(this));
	},
	
	leave : function() {
		if (!this._isMember) {
			return;
		}
		openapp.resource.del(this._memberUri, function() {
			this._isMember = false;
			this.load(this._uri);
		}.bind(this));
	}
	
}; });