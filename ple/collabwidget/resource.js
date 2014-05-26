/*global openapp*/

/**
 * Allows retrieval of sets of resources that the user has right to see, either in the context of a space 
 * or in the context of a user (user-profile like data).
 * Every resource returned is exposed as a json structure, see the following example:
 * {
 *    id: "/resource/17",
 *    context: "/space/2",
 *    tags: ["tag1", "tag2"],
 *    owners: ["participant1", "participant2", "administrators"],
 *    readers: ["participants"],
 *    editors: ["participants"],
 *    subResourceOf: "/resource/5",
 *    uri: "", 
 *    metaData: {
 *    },
 *    format: "namespaced-properties",
 *    data: {
 *    }
 * }
 * 
 * Some explanation of the fields in the resource json-object:
 *      id            - the identifier of the resource.
 *      context       - must be the identifier of a user or space to which the resource belongs.
 *      tags          - tags of this resource
 *      owners        - an array of strings being either userIds or roles that have full rights to this resource. 
 *      readers       - an array of strings being either userIds or roles that have full read-rights to this resource.
 *      editors       - an array of strings being either userIds or roles that have full rights to the data of this resource.
 *      subResourceOf - a resourceId to which this resource is a sub-resource.
 *      uri           - a URI that is set automatically unless resources data is external, 
 *                      provides the subject (RDF-wise) of metadata (and of data if it is in the namespaced properties format).
 *      metadata      - either an inline object or a URI pointing to an external metadata source, 
 *                      useful for instance when referring to books in a library which have an public metadata catalog 
 *                      where individual records can be referred to.
 *      format        - a mimetype indicating the data-format used.
 *      data          - an object or string containing the bits and pieces in a manner decided by the format.
 *                      useful for for instance when referring to online resources (links).
 * 
 * @param {String} context - .
 * @param {Object} params object may contain the following filtering properties:
 *     contextId        - must be provided, either a userId or a spaceId.
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
openapp.resource.getResources = function(contextId, params, callback) {};

/**
 * Retrieves the data part of a resource with the given id.
 */
openapp.resource.getResourceData = function(id, callback) {};

/**
 * Adds a resource.
 * @param {Object} resource the initial resource object to create with context, tags, owners, readers, editors, subResourceOf, uri, 
 *                 metadata, format, and data. Note that the id should not be provided as it is created on the serverside.
 * @param {Function} callback is a function that will be called with the created resource (now with an id).
 */
openapp.resource.addResource = function(resource, callback) {};

/**
 * Updates a resource including its metadata and data. Although if the metadata or data property is not provided (or null) it will not be touched.
 * However, if the metadata or data property points to an empty object it will clear any preexisting metadata/data. 
 * Hence be careuful with empty objects.
 * 
 * @param {Object} resource the information to update.
 * @param {Function} callback is a function that will be called on a successful update.
 */
openapp.resource.updateResource = function(resource, callback) {};

/**
 * Removes an object and all its sub-resources, even if the current user is not the owner of them.
 * @param {String} id the identifier of the resource to remove.
 * @param {Function} callback is a function that will be called on a successful remove, the callback will be called with an 
 * array of identifiers of all resources that was removed as a result of this call.
 */
openapp.resource.removeResource = function(id, callback) {}; //Only owners can do this.

/**
 * The format property of the resource specifies how the data of the 
 * 
 * @param {String} id the identifier of the resource who's data we want to update
 * @param {Object} data a piece of data that should be updated, see discussion above of the different cases.
 * @param {Object} path a xpath or jsonpath for pointing to where the update is taking place in the structured data, not used for namespaced-properties or other formats.
 * @param {Function} callback is a function that will be called on a successful update. 
 */
openapp.resource.updateData = function(id, data, path, callback) {};

/**
 * Indicates which resources to watch updates for.
 * If a resource is watched, then updates will be recieved for all subresources as well.
 * Updates will be recieved in the callback function registered via the openapp.event.connect function.
 * 
 * @param {Array} an array of identifiers for resources to listen for updates on.
 */
openapp.resource.watchResources = function(params, callback) {};