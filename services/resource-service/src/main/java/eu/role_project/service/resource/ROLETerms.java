package eu.role_project.service.resource;

import java.util.UUID;

import se.kth.csc.kmr.conserve.Property;
import se.kth.csc.kmr.conserve.Type;
import se.kth.csc.kmr.conserve.core.Terms;

public final class ROLETerms extends Terms {

	public static final ROLETerms INSTANCE = new ROLETerms();

	@Type
	public static final UUID Space = INSTANCE
			.uuid("http://purl.org/role/terms/Space");

	@Property(domain = { "http://purl.org/role/terms/SpaceService",
			"http://purl.org/role/terms/Person" }, range = "http://purl.org/role/terms/Space")
	public static final UUID space = INSTANCE
			.uuid("http://purl.org/role/terms/space");

	@Property(domain = { "http://purl.org/role/terms/UserService",
			"http://purl.org/role/terms/Space" }, range = "http://purl.org/role/terms/Person")
	public static final UUID member = INSTANCE
			.uuid("http://xmlns.com/foaf/0.1/member");

	@Property(domain = { "http://purl.org/role/terms/Space",
			"http://purl.org/role/terms/Person",
			"http://purl.org/role/terms/Activity",
			"http://purl.org/role/terms/Data" }, range = "http://purl.org/role/terms/Data")
	public static final UUID data = INSTANCE
			.uuid("http://purl.org/role/terms/data");

	@Property(domain = { "http://purl.org/role/terms/Space",
			"http://purl.org/role/terms/Person" }, range = "http://purl.org/role/terms/Tool")
	public static final UUID tool = INSTANCE
			.uuid("http://purl.org/role/terms/tool");

	@Type
	public static final UUID Tool = INSTANCE
			.uuid("http://purl.org/role/terms/Tool");

	@Type
	public static final UUID OpenSocialGadget = INSTANCE
			.uuid("http://purl.org/role/terms/OpenSocialGadget");

	@Property(domain = "http://purl.org/role/terms/Space")
	public static final UUID widget = INSTANCE
			.uuid("http://purl.org/role/terms/widget");

	@Property
	public static final UUID profile = INSTANCE
			.uuid("http://purl.org/role/terms/profile");

	@Type
	public static final UUID Person = INSTANCE
			.uuid("http://purl.org/role/terms/Person");

	@Property(range = "http://purl.org/role/terms/SpaceService")
	public static final UUID spaceService = INSTANCE
			.uuid("http://purl.org/role/terms/spaceService");

	@Property(range = "http://purl.org/role/terms/UserService")
	public static final UUID userService = INSTANCE
			.uuid("http://purl.org/role/terms/userService");

	@Property(range = "http://purl.org/role/terms/ActivityService")
	public static final UUID activityService = INSTANCE
			.uuid("http://purl.org/role/terms/activityService");

	@Type
	public static final UUID SpaceService = INSTANCE
			.uuid("http://purl.org/role/terms/SpaceService");

	@Type
	public static final UUID UserService = INSTANCE
			.uuid("http://purl.org/role/terms/UserService");

	@Type
	public static final UUID ActivityService = INSTANCE
			.uuid("http://purl.org/role/terms/ActivityService");

	@Property
	public static final UUID ple = INSTANCE
			.uuid("http://purl.org/role/terms/ple");

	@Type
	public static final UUID Data = INSTANCE
			.uuid("http://purl.org/role/terms/Data");

	@Property
	public static final UUID preferences = INSTANCE
			.uuid("http://purl.org/role/widget/preferences");

	@Property
	public static final UUID rave = INSTANCE
			.uuid("http://purl.org/role/terms/rave");

	@Property(domain = { "http://purl.org/role/terms/Space",
			"http://purl.org/role/terms/Person" }, range = "http://purl.org/role/terms/Activity")
	public static final UUID activity = INSTANCE
			.uuid("http://purl.org/role/terms/activity");

	@Type
	public static final UUID Activity = INSTANCE
			.uuid("http://purl.org/role/terms/Activity");

	@Property
	public static final UUID spaceSystemData = INSTANCE
			.uuid("http://purl.org/role/terms/spaceSystemData");

	public static final UUID externalPassword = INSTANCE
			.uuid("http://purl.org/role/terms/externalPassword");;

	public ROLETerms() {
		super(INSTANCE);
	}

}