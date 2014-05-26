package eu.role_project.service.space;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.logic.ResourceResponder;

public class ActivityService extends ResourceResponder {

	private static Logger log = LoggerFactory.getLogger(ActivityService.class);

	public static final UUID ID = UUID
			.fromString("50acbd3e-3d91-4828-b224-8870f5c864db");

	@Override
	public void initialize(Request request) {
		// Initialize the "activities" context
		log.info("Initializing /activities");
		store().in(app.getRootUuid(request)).sub(ROLETerms.activityService)
				.acquire(ID, "activities");
	}

}
