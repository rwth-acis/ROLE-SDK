define(function() {	
	var componentMap = {}, interfaceMap = {}, idCounter = 1, listenerMap = {}, triggerMap = {};
	return {
		
		add: function(component, interfaces) {
			var id, components, i, length, iface;
			interfaces = interfaces || component.interfaces;
			length = interfaces.length;
			if (component.hasOwnProperty("_component_id") &&
					!componentMap.hasOwnProperty(component._component_id)) {
				id = component._component_id;
				for (i = 0; i < length; i++) {
					componentMap[id].push(interfaces[i]);
				}
			} else {
				id = idCounter++;
				component._component_id = id;
				componentMap[id] = interfaces;
			}
			for (i = 0; i < length; i++) {
				iface = interfaces[i];
				if (interfaceMap.hasOwnProperty(iface)) {
					components = interfaceMap[iface];
				} else {
					components = [];
					interfaceMap[iface] = components;
				}
				components.push(component);
				this.trigger(component, iface, { type: "add" });
			}
			if (typeof console !== "undefined") {
				//console.log("Added component ");
				//console.log(component);
			}
		},
		
		remove: function(component) {
			var interfaces, iface, components, i, j;
			if (!component.hasOwnProperty("_component_id") ||
					!componentMap.hasOwnProperty(component._component_id)) {
				return;
			}
			interfaces = componentMap[component._component_id];
			delete componentMap[component._component_id];
			for (i = 0; i < interfaces.length; i++) {
				iface = interfaces[i];
				components = interfaceMap[iface];
				for (j = 0; j < components.length;) {
					if (components[j] === component) {
						components.splice(j, 1);
					} else {
						j++;
					}
				}
				this.trigger(component, iface, { type: "remove" });
			}
			if (typeof console !== "undefined") {
				//console.log("Removed component");
				//console.log(component);
			}
		},
		
		clear: function(iface) {
			var components, length, i, component;
			if (!interfaceMap.hasOwnProperty(iface)) {
				return;
			}
			components = interfaceMap[iface].slice(0);
			length = components.length;
			for (i = 0; i < length; i++) {
				component = components[i];
				this.remove(component);
			}
		},
		
		invoke: function(iface, method, args) {
			var components, length, i, component;
			if (typeof console !== "undefined") {
				console.log("Invoking " + iface + method);
			}
			if (!interfaceMap.hasOwnProperty(iface)) {
				return;
			}
			components = interfaceMap[iface];
			length = components.length;
			for (i = 0; i < length; i++) {
				component = components[i];
				if (typeof component[method] === "function") {
					component[method].apply(component, Array.prototype.slice.call(arguments, 2));					
				}
			}
		},
		
		on: function(iface, event, callback) {
			var eventMap, callbacks, components, length, i;
			if (listenerMap.hasOwnProperty(event)) {
				eventMap = listenerMap[event];
			} else {
				eventMap = {};
				listenerMap[event] = eventMap;
			}
			if (eventMap.hasOwnProperty(iface)) {
				callbacks = eventMap[iface];
			} else {
				callbacks = [];
				eventMap[iface] = callbacks;
			}
			callbacks.push(callback);
			if (event === "add" || event === "update") {
				if (interfaceMap.hasOwnProperty(iface)) {
					components = interfaceMap[iface];
					length = components.length;
					for (i = 0; i < length; i++) {
						callback(components[i], { type: event });
					}
				}
			} else {
				if (triggerMap.hasOwnProperty(event)) {
					eventMap = triggerMap[event];
					if (eventMap.hasOwnProperty(iface)) {
						components = eventMap[iface];
						for (i = 0; i < components.length; i++) {
							callback(components[i], { type: event });
						}
					}
				}
			}
		},
		
		one: function(iface, event, callback) {
			var handler = null, self = this;
			handler = function(component, event) {
				self.off(iface, event.type, handler);
				callback(component, event);
			};
			this.on(iface, event, handler);
		},
		
		off: function(iface, event, callback) {
			var eventMap, callbacks, i;
			if (listenerMap.hasOwnProperty(event)) {
				eventMap = listenerMap[event];
			} else {
				return;
			}
			if (eventMap.hasOwnProperty(iface)) {
				callbacks = eventMap[iface];
			} else {
				return;
			}
			for (i = 0; i < callbacks.length;) {
				if (callbacks[i] === callback) {
					callbacks[i] = function() {};
//MP fix: cannot change the array since it is iterated over from within trigger, 
//alternatively do a timeout or use an object instead of array using some index of the callbacks
//					callbacks.splice(i, 1);
				} else {
					i++;
				}
			}
		},
		
		trigger : function(component, iface, event) {
			var eventMap, callbacks, components;
			if (typeof event === "object" && typeof event.length !== "undefined") {
				for (var i = 0; i < event.length; i++) {
					this.trigger(component, iface, event[i]);
				}
			} else {
				if (typeof event === "string") {
					event = { type: event };
				}
				if (triggerMap.hasOwnProperty(event.type)) {
					eventMap = triggerMap[event.type];
				} else {
					eventMap = {};
					triggerMap[event.type] = eventMap;
				}
				if (event.type.substring(0, 1) === "!") {
					if (eventMap.hasOwnProperty(iface)) {
						components = eventMap[iface];
						for (var k = 0; k < components.length;) {
							if (components[k] === component) {
								components.splice(k, 1);
							} else {
								k++;
							}
						}
					}
				} else {
					if (event.type !== "add" && event.type !== "remove"
							&& event.type !== "update") {
						if (eventMap.hasOwnProperty(iface)) {
							components = eventMap[iface];
						} else {
							components = [];
							eventMap[iface] = components;
						}
						components.splice(0);
						components.push(component);
					}
					if (listenerMap.hasOwnProperty(event.type)) {
						eventMap = listenerMap[event.type];
						if (eventMap.hasOwnProperty(iface)) {
							callbacks = eventMap[iface];
							for (var j = 0; j < callbacks.length; j++) {
								callbacks[j](component, event);
							}
						}
					}
				}
			}
		}
		
	};
});