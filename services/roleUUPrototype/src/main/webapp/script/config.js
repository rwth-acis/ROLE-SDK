define({
	// Integration paramenters for an XMPP server accompanying the SDK installation
	// ============================================================================
	// Most parameters depend on the configuration of the XMPP server. 
	// Documentation on XMPP server configuration for use with the SDK is available at
	//
	//    [1] http://role-project.sourceforge.net/wiki/index.php/ROLE_SDK_XMPP_Server
	//
	
	// XMPP over WebSocket Connection
	// ------------------------------
	// configure, if clients should be enabled to connect via XMPP over WebSocket, if available in browser. 
	// If 'usewebsocket' is set to 'true' a connection manager capable of XMPP over WebSocket should be in place. 
	// Consult [1] for more information.
	"usewebsocket": false,
	"xmppwsport": 5280,
	"xmppwspath": "",
	
	// XMPP over BOSH Connection
	// -------------------------
	// standard connection technique for XMPP in Web applications. Most off-the-shelf XMPP servers have built-in support
	// for XMPP over BOSH with the below presets as standard default values.
	"xmppboshport": 5280,
	"xmppboshpath": "http-bind",
	"xmpppubsubservice": "pubsub",
	
	// --------------------------------------------------------------------------
	
	// Integration parameters for ROLE Widget Store
	// ============================================
	"widgetstore": "http://embeddedv2.role-widgetstore.eu/",
	"widgetstorecreatebundle": "http://role-widgetstore.eu/prepopulate/object"
	// Be careful that you do not add a comma after the last config value
	// (this would cause an error in IE)
	
});