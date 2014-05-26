/*global role*/

role.ple.getCurrentSpaceId = function() {}; //Only works from within widget inside a space or a "focused space mode" in a PLE.
role.ple.registerSpaceListener = function(params, callback) {};

role.ple.getCurrentUserId = function() {};
role.ple.getInterWidgetSharingLevel = function(callback) {};
role.ple.setInterWidgetSharingLevel = function(sharingLevel, callback) {};

role.ple.getSpaces = function(params, callback) {};
role.ple.addSpace = function(metadata, callback) {};
role.ple.removeSpace = function(spaceId, callback) {};