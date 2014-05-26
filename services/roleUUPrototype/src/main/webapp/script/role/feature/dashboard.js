//var _role_dashboard_manualinit = true;
var _role_userprofile_href = "http://" + document.location.host + "/user/:app?mode=dashboard";

define(["jquery", "../ui/ui"], function($, ui) {
	
	var toggleSurface = function(widgetId, dashboardButton) {
		if ($(dashboardButton).hasClass("dashboard-button-focus")) {
			removeSurface();
			return;
		} else if ($(".dashboard-button-focus").size() > 0) {
			$(".dashboard-button-focus").removeClass(
					"dashboard-button-focus");
		}

		$("html").bind("mousedown", removeSurface);

		$("html").css("background", "none");
		$("body").css("background", "none");
		$("#pageContent").css("background", "none");
		$("#header").css("bottom", "0px");
		$(".sideSection").hide();
		$(".widget-wrapper").each(function() {
			if ($(this).hasClass("widget-wrapper-focus")) {
				$(this).removeClass("widget-wrapper-focus");
				$(this).fadeOut(200);
			} else {
				$(this).hide();
			}
		});

		// Disable dragging (reordering) of widgets temporarily
		$(".region").sortable("disable");
		
		$(dashboardButton).addClass("dashboard-button-focus");
		$("#widget-" + widgetId + "-wrapper").css("position", "fixed")
				.css("bottom", "17px").css("left",
						($(dashboardButton).offset().left - 7) + "px")
				.addClass("widget-wrapper-focus").slideDown(200);

		dashboardToggleSurface();
	};

	var removeSurface = function(event, instant) {
		if (typeof event !== "undefined"
				&& $(event.target).hasClass("dashboard-button")) {
			return;
		}

		$("html").unbind("mousedown", removeSurface);
		$(".region").sortable("enable");

		$(".dashboard-button").removeClass("dashboard-button-focus");
		$(".widget-wrapper-focus").fadeOut(
				(typeof instant !== "undefined" && instant) ? 0 : 200,
				function() {
					dashboardRemoveSurface();
					window.setTimeout(function() {
						$("html").css("background", "");
						$("body").css("background", "");
						$("#pageContent").css("background", "");
						$("#header").css("bottom", "");
						$(".sideSection").show();

						$(".widget-wrapper").css("position", "").css(
								"bottom", "").css("left", "")
								.removeClass("widget-wrapper-focus")
								.show();
					}, 1);
				});
	};

	var dashboardToggle = function() {
		window.parent.postMessage(JSON.stringify({
			ROLEDashboardCall : {
				name : "dashboardToggle"
			}
		}), "*");
	};

	var dashboardToggleSurface = function() {
		window.parent.postMessage(JSON.stringify({
			ROLEDashboardCall : {
				name : "dashboardToggleSurface"
			}
		}), "*");
	};

	var dashboardRemoveSurface = function() {
		window.parent.postMessage(JSON.stringify({
			ROLEDashboardCall : {
				name : "dashboardRemoveSurface"
			}
		}), "*");
	};
	
	var init = function() {
		$(document.body).on("click", ".dashboard-expand", function() {
			if (!$('.sideSection').is(':visible')) {
				$('.widget-wrapper').stop(true, true);
				removeSurface(undefined, true);
				window.setTimeout(function() {
					dashboardToggle();
				}.bind(this), 1);
			} else {
				dashboardToggle();
			}
		});
		
		$(document.body).on("click", ".dashboard-collapse", function() {
			dashboardToggle();
		});
		
		$(document.body).on("click", ".dashboard-button", function() {
			$(this).stop(true, true);
			toggleSurface(this.id.match(/\d+/)[0], this);
		});
		
		$(window).resize(function() {
			if ($(".dashboard-button-focus").size() > 0
					&& $(window).height() === 25) {
				removeSurface(undefined, true);
			}
			var isExpanded = $(window).height() > 25
					&& $(".sideSection").is(":visible");
			$(document.body).toggleClass("dashboard-expanded",
					isExpanded);
			$(document.body).toggleClass("dashboard-collapsed",
					!isExpanded);
			$(document.body).css("overflow", isExpanded ? "auto" : "hidden");
			if (!isExpanded) {
				ui.canvas();
			}
		});
		$(document.body).addClass("dashboard-collapsed");
		$(document.body).css("overflow", "hidden");
		
		$(document.body).on("dblclick", "#header", function(event) {
			if ($(event.target).hasClass("dashboard-button")) {
				return;
			}
			if ($(".dashboard-button-focus").size() > 0) {
				removeSurface(undefined, true);
			}
			dashboardToggle();
		});
	};

	return {
		toggleSurface : toggleSurface,
		removeSurface : removeSurface,
		dashboardToggle : dashboardToggle,
		dashboardToggleSurface : dashboardToggleSurface,
		dashboardRemoveSurface : dashboardRemoveSurface,
		init : init
	};

});