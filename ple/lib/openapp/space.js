openapp.getSpace = function(spaceId, params, callback) {
	if (openapp.remote) {
		openapp.remoteCall("getSpace", [spaceId, params], callback);
	} else {
		openapp.dm.get(spaceId, params, callback);
	}
};

openapp.getSpaces = function(params, callback) {
	if (openapp.remote) {
		openapp.remoteCall("getSpaces", [params], callback);
	} else {
		openapp.dm.search("space", params, callback);
	}
};
openapp.watchSpaces = function(params, callback) {
	//TODO
};

openapp.addSpace = function(space, callback) {
	if (openapp.remote) {
		openapp.remoteCall("addSpace", [space], callback);
	} else {
		openapp.dm.create("space", space, callback);		
	}
};

openapp.removeSpace = function(spaceId, callback) {
	if (openapp.remote) {
		openapp.remoteCall("removeSpace", [spaceId], callback);
	} else {
		openapp.dm.remove(spaceId, callback);
	}
};

openapp.updateSpace = function(space, callback) {
	if (openapp.remote) {
		openapp.remoteCall("updateSpace", [space], callback);
	} else {
		openapp.dm.put(space, callback);
	}
};

openapp.getSpaceTools = function(spaceId, callback) {
	openapp.getResources({context: spaceId, tag: "tool"}, callback);
};
openapp.addSpaceTool = function(spaceId, tool, callback) {
	tool.tag = "tool";
	tool.context = spaceId;
	tool.format = "namespaced-propeties";
	openapp.addResource(tool, callback);
};