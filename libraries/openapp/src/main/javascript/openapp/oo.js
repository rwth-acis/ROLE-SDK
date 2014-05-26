goog.provide("openapp.oo");
goog.require("openapp.resource");
goog.require("openapp");

/** @namespace OpenApp Object Oriented API is a layer on top of the openapp.resource API. */
openapp.oo = {};

/**
 * @class Use this class for getting an object oriented access point to a resource.
 * @constructor
 * @memberOf openapp.oo
 * @param {Object} uri the uri to the resource
 * @param {Object} context optional context object from the openapp.resource API
 */
openapp.oo.Resource = function(uri, context /*optional context object*/, info /*optional shallow context object*/) {
	this.uri = uri;
	this.context = context;
	this.info = info;
};
var OARP = openapp.oo.Resource.prototype;

/**
 * @methodOf openapp.oo.Resource
 */
OARP.getURI = function() {
	return this.uri;
};

/**
 * @methodOf openapp.oo.Resource
 * @param {Function} f will be called in the context of this instance when the 
 * 	openapp.resource specific context object is loaded for this instance.
 * @private
 */
OARP._call = function(f) {
	var slf = this;
	if (this.context == null) {
		//Push callback functions into an array if the call is underway, call all the functions at once when the context is loaded (once).
		if (this._deferred == null) {
			this._deferred = [f];
			openapp.resource.get(this.uri, function(context) {
				slf.context = context;
				var defs = slf._deferred;
				delete slf._deferred;
				for (var ind=0;ind<defs.length;ind++) {
					defs[ind].call(slf);
				}
			});			
		} else {
			this._deferred.push(f);
		}
	} else {
		f.call(slf);
	}
};

/**
 * Refresh the resource if you suspect that it has been modified and local cache may be out of date.
 */
OARP.refresh = function(callback) {
	delete this.context;
	delete this.info;
	if (callback) {
		this._call(function() {
			callback();
		});
	}
};

/**
 * The parameters are sent in an object where the following properties can be provided: 
 *  relation - {String} an relation to a subresource, must be a URI
 *  type - {String} an optional type that restricts which subresources that are found, must be a URI
 *  followReference - {Boolean} if true and there is a reference relation to a new resource that resource is returned instead.
 *  onEach - {Function} a callback that will be called for each subresource found
 *  onAll - {Function} a callback that will be called with a list of all subresources found
 *  
 * @param {params} the parameters as a object.
 * @methodOf openapp.oo.Resource
 */
OARP.getSubResources = function(params) {
	var self = this;
	this._call(function() {
		if (params.type != null) {
			var sr = openapp.resource.context(this.context).sub(params.relation).type(params.type).list();				
		} else {
			var sr = openapp.resource.context(this.context).sub(params.relation).list();
		}
		var subResources = [];
		for (var i=0; i<sr.length;i++) {
			var subUri = sr[i].uri;
			if (params.followReference) {
				var objArr = this.context.data[subUri]["http://www.w3.org/2002/07/owl#sameAs"];
				if (objArr != null && objArr.length > 0) {
					subUri = objArr[0].value;
				}
			}
			var subRes = new openapp.oo.Resource(subUri, null, sr[i]);
			//Cache reference
			if (params.followReference == null) {
				subRes._referenceLoaded = true;
				var arr = this.context.data[subUri]["http://www.w3.org/2002/07/owl#sameAs"];
				if (arr != null && arr.length > 0) {
					subRes._reference = arr[0].value;
				}
			}
			if (params.onEach) {
				params.onEach(subRes);
			}
			if (params.onAll) {
				subResources.push(subRes);
			}
		}
		if (params.onAll) {
			params.onAll(subResources);
		}
	});
};

/**
 * If the 
 * @param {Function} callback will be called with a openapp.oo.Resource instance, 
 * 	either the current instance or if the current resource is a reference the referenced resource will be provided .
 * @methodOf openapp.oo.Resource
 */
OARP.followReference = function(callback) {
	if (this._referenceLoaded) {
		callback(this._reference != null ? new openapp.oo.Resource(this._reference):this);
		return;
	}
	this._call(function() {
		var objArr = this.context.data[this.uri]["http://www.w3.org/2002/07/owl#sameAs"];
		if (objArr != null && objArr.length > 0) {
			callback(new openapp.oo.Resource(objArr[0].value));
		} else {
			callback(this);
		}
	});
};

/**
 * Similar to followReference, but provides the uri instead of a openapp.oo.Resource instance.
 * 
 * @param {Function} callback with be called without parameters if the current resource is
 * 	not a reference, otherwise with the URI to the referenced resource. 
 * @methodOf openapp.oo.Resource
 */
OARP.getReference = function(callback) {
	if (this._referenceLoaded) {
		callback(this._reference);
		return;
	}
	this._call(function() {
		var sA = this.context.subject[openapp.ns.rdfs + "seeAlso"];
		if (sA != null && sA.length > 0) {
			callback(this.context.subject[openapp.ns.rdfs + "seeAlso"][0].value);
		} else {
			callback();
		}
	});
};

/**
 * Retrieves the metadata for the resource in the format specified. The format alternatives are:
 * 	graph - corresponds to the rdf/json format, see http://docs.api.talis.com/platform-api/output-types/rdf-json
 *  properties - simplified flat property value pairs represented as a simple object.
 *  
 * @param {String} format, either "graph" or "properties", "properties" is the default.
 * @param {Function} callback will be called with metadata according to the format.
 * @methodOf openapp.oo.Resource
 */
OARP.getMetadata = function(format, callback) {
	this._call(function() {
		openapp.resource.context(this.context).metadata().get(function(content) {
			switch(format || "properties") {
				case "properties":
					callback(openapp.resource.context(content).properties());
					break;
				case "graph":
					callback(openapp.resource.content(content).graph());
					break;
				case "rdfjson":
					callback(openapp.resource.content(content).json());
			}
		});
	});
};

OARP.getInfo = function(callback) {
	if (callback) {
		if (this.context || this.info) {
			callback(openapp.resource.context(this.context || this.info).properties());
		} else {
			this._call(function() {
				callback(openapp.resource.context(this.context || this.info).properties());
			});
		}		
	} else if (this.context || this.info) {
		return openapp.resource.context(this.context || this.info).properties();
	}
};

OARP.getRepresentation = function(format, callback) {
	this._call(function() {
		openapp.resource.context(this.context).representation().get(function(content) {
			switch(format || "text/html") {
				case "properties":
					callback(openapp.resource.context(content).properties());
					break;
				case "graph":
					callback(openapp.resource.content(content).graph());
					break;
				case "rdfjson":
					callback(openapp.resource.content(content).json());
					break;
				case "text/html":
					callback(openapp.resource.content(content).string());
					break;
			}
		});
	});	
};

/**
 * Stores metadata for a resource.
 * 
 * @param {Object} metadata as an object according to the format.
 * @param {String} format the options are properties, rdfjson, or graph.
 * @param {Function} callback
 * @methodOf openapp.oo.Resource
 */
OARP.setMetadata = function(metadata, format, callback) {
	var rdfjson = {};
	switch (format || "properties") {
		case "properties":
			var props = {};
			for (var key in metadata) {
				props[key] = [{"type": "literal", "value": metadata[key]}];
			}
			rdfjson[this.context.uri] = props;
			break;
		case "rdfjson":
			rdfjson = metadata;
			break;
		case "graph":
			graph.put(callback);
			return;
	}
	openapp.resource.put(this.context.uri, callback,
		{ "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate": openapp.ns.rest + "metadata" }, 
		JSON.stringify(rdfjson));
};

/**
 * Stores a representation for a resource.
 * 
 * @param {Object | String} representation if it is a object it is serialized into a string first via JSON.stringify 
 * @param {String} mediaType for example application/json, text/html, application/pdf etc. 
 * @param {Function} callback
 * @methodOf openapp.oo.Resource
 */
OARP.setRepresentation = function(representation, mediaType, callback) {
	this._call(function() {
		var inter = openapp.resource.context(this.context).representation().mediaType(mediaType);
		if (typeof representation === "string") {
			inter.string(representation).put(callback);
		} else {
			inter.json(representation).put(callback);
		}
	});
};

/**
 * params may contain:
 *   relation - {String} a relation to use to the new subresource, must be a valid URI.
 *   type - {String} an optional type of the new subresource, must be a valid URI.
 *   referenceTo - {String} an optional resource the subresource will reference, must be a valid URI.
 * 	 metadata - {Object} optional metadata of the subresource.
 *   format - {String} the format of the metadata, either "properties", "graph" or "rdfjson", default is "properties".
 *   representation - {String|Object} an optional representation for the resource, may be a string or an object.
 *   medieType - {String} the medietype of the representation, defaults to "application/json"
 *   callback - {Function} a callback that will be called with the created subresource.
 *   
 * @param {Object} params
 * @methodOf openapp.oo.Resource
 */	
OARP.create = function(params) {
	this._call(function() {
		var inter = openapp.resource.context(this.context).sub(params.relation || openapp.ns.role + "data");
		if (params.referenceTo != null) {
			inter = inter.seeAlso(params.referenceTo);
		}
		if (params.type != null) {
			inter = inter.type(params.type);
		}
		inter.create(function(context) {
			var resource = new openapp.oo.Resource(context.uri, context);
			if (params.metadata) { //If metadata provided
				resource.setMetadata(params.metadata, params.format, function(content) {
					if (params.representation) { //If metadata and representation
						resource.setRepresentation(params.representation, params.medieType || "application/json", function(content) {
							params.callback(resource);
						});
					} else { //Only metadata
						params.callback(resource);
					}
				});					
			} else if (params.representation) { //Only representation 
					resource.setRepresentation(params.representation, params.medieType || "application/json", function(content) {
						params.callback(resource);
					});					
			} else { //Neither metadata or representation
				params.callback(resource);
			}
		});
	});
};
OARP.del = function(callback) {
		openapp.resource.del(this.uri, callback);
};