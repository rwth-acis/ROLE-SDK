goog.provide("openapp.io");
goog.require("openapp")

/** @namespace OpenApp IO functionality. */
openapp.io = {};

/**
 * Creates an XMLHttpRequest object, in a manner compatible with the
 * current browser environment.
 *  
 * @returns {XMLHttpRequest} The created XMLHttpRequest object.
 * @throws {Object} If XMLHttpRequest is not supported in this environment.
 * @memberOf openapp.io
 */
openapp.io.createXMLHttpRequest = function () {
	if (typeof XMLHttpRequest !== "undefined") {
		return new XMLHttpRequest();
	} else if (typeof ActiveXObject !== "undefined") {
		return new ActiveXObject("Microsoft.XMLHTTP");
	} else {
		throw {
			name: "XMLHttpRequestError",
			message: "XMLHttpRequest not supported"
		};
	}
};

/**
 * Wraps the gadgets.io.makeRequest function, providing browser UI for the OAuth authorization flow.
 * 
 * @memberOf openapp.io
 */
openapp.io.makeRequest = function(url, callback, opt_params) {
	gadgets.io.makeRequest(url, function (response) {
		var popup, oauthPersonalize, oauthPersonalizeButton, oauthPersonalizeDenyButton,
		  oauthPersonalizeDone, oauthPersonalizeDoneButton, oauthPersonalizeComplete,
		  oauthPersonalizeMessage, oauthPersonalizeHideButton, text;
		if (document.getElementById("oauthPersonalize") === null) {
			oauthPersonalize = document.createElement("div");
			oauthPersonalizeButton = document.createElement("input");
			oauthPersonalizeDenyButton = document.createElement("input");
			oauthPersonalizeDone = document.createElement("div");
			oauthPersonalizeDoneButton = document.createElement("input");
			oauthPersonalizeComplete = document.createElement("div");
			oauthPersonalizeMessage = document.createElement("span");
			oauthPersonalizeHideButton = document.createElement("input");
			oauthPersonalize.id = "oauthPersonalize";
			oauthPersonalizeButton.id = "oauthPersonalizeButton";
			oauthPersonalizeDenyButton.id = "oauthPersonalizeDenyButton";
			oauthPersonalizeDone.id = "oauthPersonalizeDone";
			oauthPersonalizeDoneButton.id = "oauthPersonalizeDoneButton";
			oauthPersonalizeComplete.id = "oauthPersonalizeComplete";
			oauthPersonalizeMessage.id = "oauthPersonalizeMessage";
			oauthPersonalizeHideButton.id = "oauthPersonalizeHideButton";
			oauthPersonalizeButton.id = "oauthPersonalizeButton";
			oauthPersonalize.style.display = "none";
			oauthPersonalizeDone.style.display = "none";
			oauthPersonalizeComplete.style.display = "none";
			oauthPersonalizeButton.type = "button";
			oauthPersonalizeDenyButton.type = "button";
			oauthPersonalizeDoneButton.type = "button";
			oauthPersonalizeHideButton.type = "button";

			oauthPersonalizeButton.value = "Continue";
			oauthPersonalizeDenyButton.value = "Ignore";
			oauthPersonalizeDoneButton.value = "Done";
			oauthPersonalizeHideButton.value = "Hide";
			oauthPersonalize.appendChild(document.createTextNode("In order to provide the full functionality of this tool, access to your personal data is being requested."));
			oauthPersonalizeDone.appendChild(document.createTextNode("If you have provided authorization and are still reading this, click the Done button."));
			
			var openappDialog = document.getElementById("openappDialog");
			if (openappDialog == null) {
				openappDialog = document.createElement("div");
				if (document.body.firstChild != null) {
					document.body.insertBefore(openappDialog, document.body.firstChild);
				} else {
					document.body.appendChild(openappDialog);
				}
			}
			openappDialog.appendChild(oauthPersonalize);
			openappDialog.appendChild(oauthPersonalizeDone);
			openappDialog.appendChild(oauthPersonalizeComplete);
			
			oauthPersonalize.appendChild(oauthPersonalizeButton);
			oauthPersonalize.appendChild(oauthPersonalizeDenyButton);
			oauthPersonalizeDone.appendChild(oauthPersonalizeDoneButton);
			oauthPersonalizeComplete.appendChild(oauthPersonalizeMessage);
			oauthPersonalizeComplete.appendChild(oauthPersonalizeHideButton);
			oauthPersonalizeDenyButton.onclick = (function(){
				oauthPersonalize.style.display = "none";
			});
			oauthPersonalizeHideButton.onclick = (function(){
				oauthPersonalizeComplete.style.display = "none";
			});
		}
		if (response.oauthApprovalUrl) {
			popup = (function(options) {
			  // Code from http://gadget-doc-examples.googlecode.com/svn/trunk/opensocial-gadgets/popup.js
			  // Used under Apache License, Version 2.0.

			  /*if (!("destination" in options)) {
			    throw "Must specify options.destination";
			  }
			  if (!("windowOptions" in options)) {
			    throw "Must specify options.windowOptions";
			  }
			  if (!("onOpen" in options)) {
			    throw "Must specify options.onOpen";
			  }
			  if (!("onClose" in options)) {
			    throw "Must specify options.onClose";
			  }*/
			  var destination = options.destination;
			  var windowOptions = options.windowOptions;
			  var onOpen = options.onOpen;
			  var onClose = options.onClose;

			  // created window
			  var win = null;
			  // setInterval timer
			  var timer = null;

			  // Called when we recieve an indication the user has approved access, either
			  // because they closed the popup window or clicked an "I've approved" button.
			  function handleApproval() {
			    if (timer) {
			      window.clearInterval(timer);
			      timer = null;
			    }
			    if (win) {
			      win.close();
			      win = null;
			    }
			    onClose();
			    return false;
			  }

			  // Called at intervals to check whether the window has closed.  If it has,
			  // we act as if the user had clicked the "I've approved" link.
			  function checkClosed() {
			    if ((!win) || win.closed) {
			      win = null;
			      handleApproval();
			    }
			  }

			  /**
			   * @return an onclick handler for the "open the approval window" link
			   * @private
			   */
			  function createOpenerOnClick() {
			    return function() {
			      // If a popup blocker blocks the window, we do nothing.  The user will
			      // need to approve the popup, then click again to open the window.
			      // Note that because we don't call window.open until the user has clicked
			      // something the popup blockers *should* let us through.
			      win = window.open(destination, "_blank", windowOptions);
			      if (win) {
				// Poll every 100ms to check if the window has been closed
				timer = window.setInterval(checkClosed, 100);
				onOpen();
			      }
			      return false;
			    };
			  }

			  /**
			   * @return an onclick handler for the "I've approved" link.  This may not
			   * ever be called.  If we successfully detect that the window was closed,
			   * this link is unnecessary.
			   * @private
			   */
			  function createApprovedOnClick() {
			    return handleApproval;
			  }

			  return {
			    createOpenerOnClick: createOpenerOnClick,
			    createApprovedOnClick: createApprovedOnClick
			  };
			})({
				destination: response.oauthApprovalUrl,
				windowOptions: "width=450,height=500",
				onOpen: function() { 
					document.getElementById("oauthPersonalize").style.display = "none";
					document.getElementById("oauthPersonalizeDone").style.display = "block";
				},
				onClose: function() {
					document.getElementById("oauthPersonalizeDone").style.display = "none";
					document.getElementById("oauthPersonalizeComplete").style.display = "block";
					openapp.io.makeRequest(url, callback, opt_params);
				}
			});
			document.getElementById("oauthPersonalizeButton").onclick = popup.createOpenerOnClick();
			document.getElementById("oauthPersonalizeDoneButton").onclick = popup.createApprovedOnClick();
			text = "Please wait.";
			if (document.all) {
				document.getElementById("oauthPersonalizeMessage").innerText = text;
			} else {
				document.getElementById("oauthPersonalizeMessage").textContent = text;
			}
			document.getElementById("oauthPersonalize").style.display = "block";
		} else if (response.oauthError) {
			text = "The authorization was not completed successfully. (" + response.oauthError + ")";
			if (document.all) {
				document.getElementById("oauthPersonalizeMessage").innerText = text;
			} else {
				document.getElementById("oauthPersonalizeMessage").textContent = text;
			}
			document.getElementById("oauthPersonalizeComplete").style.display = "block";
		} else {
			text = "You have now granted authorization. To revoke authorization, go to your Privacy settings.";
			if (document.all) {
				document.getElementById("oauthPersonalizeMessage").innerText = text;
			} else {
				document.getElementById("oauthPersonalizeMessage").textContent = text;
			}
			callback(response);
		}
	}, opt_params);
};

