package eu.role_project.service.space;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.logic.ResourceResponder;

public class SpaceService extends ResourceResponder {

	private static Logger log = LoggerFactory.getLogger(SpaceService.class);

	public static final UUID ID = UUID
			.fromString("4cbde6b5-f7ea-4436-9a90-c14e5440d2ad");

	@Override
	public void initialize(Request request) {
		// Initialize the "spaces" context
		log.info("Initializing /spaces");
		store().in(app.getRootUuid(request)).sub(ROLETerms.spaceService)
				.acquire(ID, "spaces");
	}

	@Override
	public Resolution resolve(Request request) {
		Resolution resolution = super.resolve(request);
		if (Resolution.StandardType.CONTEXT.equals(resolution.getType())
				&& resolution.getContext() == null) {
			// Ensure we have only one not-resolved segment after the space
			// service (e.g., /spaces/newspace and not /spaces/newspace/more)
			Concept spaceService = request.getContext();
			int spaceServiceCount = 0;
			for (Resolution res : request.getResolutionPath()) {
				if (spaceService.equals(res.getContext())) {
					spaceServiceCount++;
				}
			}
			if (spaceServiceCount == 1) {
				return new Resolution(Resolution.StandardType.CONTEXT,
						spaceService);
			}
		}
		return resolution;
	}

}
