openapp=typeof openapp!=="undefined"?openapp:{};

/**
 * Retrieves a single resource from its id.
 * 
 * Every resource returned is exposed as a json structure, below is an example:
 * {
 *    "id": "/resource/17",
 *    "context": "/space/2",
 *    "modified": "2010-10-10",
 *    "modifiedBy": "/user/1",
 *    "tags": ["tag1", "tag2"],
 *    "owners": ["participant1", "participant2", "administrators"],
 *    "readers": ["participants"],
 *    "editors": ["participants"],
 *    "subResourceOf": "/resource/5",
 *    "about": "", 
 *    "metaData": {},
 *    "format": "namespaced-properties",
 *    "data": true
 * } 
 * 
 * Some explanation of the fields in the resource json-object:
 *      id            - the identifier of the resource.
 *      context       - must be the identifier of a user or space to which the resource belongs.
 *      modified      - date of last modification
 *      modifiedBy    - who did the last modification of the resource (or its data).
 *      tags          - tags of this resource
 *      owners        - an array of strings being either userIds or roles that have full rights to this resource. 
 *      readers       - an array of strings being either userIds or roles that have full read-rights to this resource.
 *      editors       - an array of strings being either userIds or roles that have full rights to the data of this resource.
 *      subResourceOf - a resourceId to which this resource is a sub-resource.
 *      about         - a URI that is set automatically to id + "/data" unless resources data is external, 
 *                      provides the subject (RDF-wise) of metadata (and of data if it is in the namespaced properties format).
 *      metadata      - either an inline object or a URI pointing to an external metadata source, 
 *                      useful for instance when referring to books in a library which have an public metadata catalog 
 *                      where individual records can be referred to.
 *      format        - a mimetype indicating the data-format used.
 *      data          - a boolean, true indicates that there is data available, false means that there is no digital representation 
 *      				yet or there wont be any since the resource corresponds to a non-digital resource such as a physical object
 *      				or idea. The data property is undefined if and only if the resource is a link 
 *      				(the about property points to a Url outside of the space).
 * 
 * @param {String} resId the identifier of the resource to fetch.
 * @param {Object} params various parameters, e.g. byPassCahce = true.
 * @param {Function} callback will be called with an object representing the resource according to the format indicated above.
 */
openapp.getResource = function(resId, params, callback) {
	if (openapp.remote) {
		openapp.remoteCall("getResource", [resId, params], callback);
	} else {
		openapp.dm.get(resId, params, callback);		
	}
};


/**
 * Allows retrieval of sets of resources that the user has right to see, either in the context of a space 
 * or in the context of a user (user-profile like data).
 * 
 * @param {Object} params object may contain the following filtering properties:
 *     context          - must be provided, either a userId or a spaceId.
 *     tag              - optional, restricts the results to include resources with the given tag
 *                        (does not apply to sub-resources of hierarchies where the topmost resource matches the tag
 *                        if the subResourceDepth property is provided)
 *     subResourceDepth - indicates how many levels of sub-resources that should be fetched for hierarchical resources,
 *                        (note that resources that have no sub-resources are considered to be roots of empty hierarchies)
 *                        0         == all sub-resources are fetched
 *                        1         == only the root of resource hierarchies are fetched 
 *                        n         == n levels of sub-resources are fetched for matched top-level resources
 *                        undefined ==  resources are fetched independent of their position in resource-hierarchies.
 *    includeData       - specify if potential inline data should be included or not, true by default.
 * @param {Function} callback is a function that will be called with the array of the matching resources with the properties indicated above.
 */
openapp.getResources = function(params, callback) {
	if (openapp.remote) {
		openapp.remoteCall("getResources", [params], callback);
	} else {
		openapp.dm.search("resource", params, callback);		
	}
};

/**
 * Retrieves the data part of a resource with the given id.
 * Note that if the openapp.getResources where called with the includeData flag, it appropriate to use 
 * the synchronous mode of this function since the data will be in the client side cache in most situations.
 * NOTE: only request data for resources where the data property is true, indicated data being available. 
 * 
 * If the resource format was "namespaced-properties" the data can look like:
 *  {
 * 	 	"http://www.w3.org/1999/02/22-rdf-syntax-ns#type": "http://example.com/Competence",
 * 		"http://example.com/competenceDefinition": "http://example.com/competencyDefinitions/cd1",
 * 		"http://example.com/skill": "http://example.com/skillLevels/sl1"
 * 	}
 * @param {String} resId is the identifier for the resource, the URI to the data in the REST api will be generated by appending "/data".
 * @param {Function} callback that will be called with the data in the format determined by the resources format property.
 */
openapp.getResourceData = function(resId, callback) {
	if (openapp.remote) {
		openapp.remoteCall("getResourceData", [resId], callback);
	} else {
		openapp.dm.get(openapp._getdataUrl(resId), null, callback);		
	}
};

/**
 * Adds a resource to a space or user.
 * @param {Object} resource the initial resource object, it must indicate wich space or user the resource belongs to, 
 *                 that is the context property is required. The properties id, modified, modifiedBy, and data will be ignored.
 * 				   It is highly encouraged to provide metadata. The rest of the properties (tags, owners, readers, 
 * 				   editors, subResourceOf, about, format) is optional, they can be provided in a later phase. 
 * @param {Function} callback is a function that will be called with the created resource (now with an id).
 */
openapp.addResource = function(resource, callback) {
	if (openapp.remote) {
		openapp.remoteCall("addResource", [resource], callback);
	} else {
		openapp.dm.create("resource", resource, callback);		
	}
};

/**
 * Updates a resource, changes to the properties id, context, modified, modifiedBy, and data will be ignored 
 * as they are managed by the system.
 * 
 * @param {Object} resource the information to update.
 * @param {Function} callback is a function that will be called with the updated resource on a successful update.
 */
openapp.updateResource = function(resource, callback) {
	if (openapp.remote) {
		openapp.remoteCall("updateResource", [resource], callback);
	} else {
		openapp.dm.put(resource, callback);
	}
};

/**
 * Removes an resource and all resources marked as sub-resources, even if the current user is not the owner of them.
 * @param {String} resId the identifier of the resource to remove.
 * @param {Function} callback is a function that will be called on a successful remove, the callback will be called with an 
 * array of identifiers of all resources that was removed as a result of this call.
 */
openapp.removeResource = function(resId, callback) {
	if (openapp.remote) {
		openapp.remoteCall("removeResource", [resId], callback);
	} else {
		openapp.dm.remove(resId, callback);
	}
};

/**
 * Updates data for a resource according to the format indicated by the format property of the resource.
 * If the format is namespaced-properites the data will be a flat object like:
 *  {
 * 	 	"http://www.w3.org/1999/02/22-rdf-syntax-ns#type": "http://example.com/Competence",
 * 		"http://example.com/competenceDefinition": "http://example.com/competencyDefinitions/cd1",
 * 		"http://example.com/skill": ""
 * 	}
 * All of the properties with non emtry values will be added or updated to contain the new value.
 * A property with a value of an empty string will have the special meaning of removing that property, 
 * properties not mentioned will be left as they are.
 * For the namespaced-properties the path parameter have no meaning.
 * 
 * If the format is application/json then the data will be a object that may contain other objects and arrays,
 * although loops are not allowed. The path parameter may provide a json-path in the existing data object and
 * will replace the data at that point, if there is no data at that point it will be added.  
 * 
 * @param {String} resId the identifier of the resource who's data we want to update
 * @param {Object} data a piece of data that should be updated, see discussion above of the different cases.
 * @param {Object} path a xpath or jsonpath for pointing to where the update is taking place in the structured data, not used for namespaced-properties.
 * @param {Function} callback is a function that will be called on a successful update. 
 */
openapp.updateData = function(resId, data, path, callback) {
	if (openapp.remote) {
		openapp.remoteCall("updateData", [resId, data, path], callback);
	} else {
		if (path) {
			openapp.dm.post(openapp._getdataUrl(resId), data, {path: path}, callback);		
		} else {
			openapp.dm.put(openapp._getdataUrl(resId), data, null, callback);		
		}
	}
};
openapp.watchResources = function(params, callback) {
	//TODO
};

openapp._getdataUrl = function(resId) {
	return resId+"/data";
};