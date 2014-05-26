require(
		//configure object, tells where the jqueryui plugins are located.
		{ paths: { jqueryui: '/libs/jqueryui/jqueryui'}},
		
		//Make sure all UI components are listed as dependencies
		["jquery", "/s/openapp", "participants", "dashboard", "resources", "widgets", "webapps"],
		
		//requirejs callback that will be called after all the dependencies have been loaded. 
		function($) {

	//Bootstrap when DOM is ready.
	$(document).ready(function() {

		//Load the space, cannot use the openapp resource API since the securitytoken for the widgets 
		//need to be included, hence below the special request spaceid/role:plespace is used.
		var spaceURL = window.location.href;
		$.getJSON(spaceURL+"/role:plespace",function(data) {
			updateSpaceInfo(data);
			updateSignedInInfo(data);
			fixLayoutControls();

			//Now the space is created as a openapp.oo.Resource to simplify interaction.
			var spaceResource = new openapp.oo.Resource(data.spaceURI);
			
			//First we load all the members of the space to determine wether the user is a member or not.
			spaceResource.getSubResources({
				relation: openapp.ns.foaf + "member", 
				followReference: true, 
				onAll: function(subResources) {
					var memberOfSpace = false;
					for (var i=0;i<subResources.length;i++) {
						if (subResources[i].getURI() === data.userURI) {
							memberOfSpace = true;
						}
					}
					if (memberOfSpace) {
						$("body").removeClass("notOwner notParticipant");
					}
					//Trigger event to other parts of the UI.
					//Any UI should listen in for plespace events...
					//see for instance the widgets.js file how it is done.
					var event = jQuery.Event("plespace");
					event.spaceResource = spaceResource;
					event.userURI = data.userURI;
					event.userName = data.userName;
					event.memberOfSpace = memberOfSpace;
					event.dashboardWidgets = data.ptools;
					event.widgets = data.tools;
					$(document).trigger(event);
				}});			
		});
	});
	
	/** 
	 * Updates information about the current space such as title and links.
	 */
	var updateSpaceInfo = function(data) {
		if (data.spaceTitle != null) {
			$("#spaceTitleNode").html(data.spaceTitle).attr("href", data.spaceURI);
			document.title = data.spaceTitle;
		}
		var loc = window.location.href;
		if (data.spaceURI != null && loc.indexOf("://127.0.0.1") == -1 && loc.indexOf("://localhost") == -1) {
			$("#addToGoogleLinkNode").css("display", "").attr("href", "http://fusion.google.com/add?source=atgs&moduleurl="+encodeURIComponent(data.spaceURI+"/role:widget"));
		}
	};

	/**
	 * Updates information about who is currently signed in, including links to sign in/sign out.
	 * @param {Object} data
	 */
	var updateSignedInInfo = function(data) {
		if (data.userName != null && data.userName!= "") {
			$("body").removeClass("notLoggedIn");
			$("#dashboardSignInNode").css("display", "none");
			$("#signedInNode").css("display", "");
			$("#signInNode").html("<strong>"+data.userName+"</strong> | <a style='color: black;' href='/o/session/logout?return="+encodeURIComponent(window.location)+"'>Sign out</a>");
		} else {
			$("body").addClass("notLoggedIn");
			$("#signInNode").html("<strong><a style='color: black;' href='/o/session/login?return="+encodeURIComponent(window.location)+"'>Sign in</a></strong>");
			$("#dashboardSignInLinkNode").attr("href", "/o/session/login?return="+encodeURIComponent(window.location));

		}
	};
	
	/**
	 * Bind the layout controls to actual changes by adding and substracting css classes on the widgetsNode node.
	 */
	var fixLayoutControls = function() {
		$("#layoutNode input").change(
			function(event) {
				var val = $(event.target).val();
				switch ($(event.target).val()) {
					case "flowing":
						$("#widgetsNode").removeClass("oneColumn twoColumns threeColumns");
						break;
					case "oneColumn":
						$("#widgetsNode").removeClass("twoColumns threeColumns").addClass("oneColumn");
						break;
					case "twoColumns":
						$("#widgetsNode").removeClass("oneColumn threeColumns").addClass("twoColumns");
						break;
					case "threeColumns":
						$("#widgetsNode").removeClass("oneColumn twoColumns").addClass("threeColumns");
						break;
				}
			});
	};	
});