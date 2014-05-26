package eu.role_project.service.shindig;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import net.oauth.OAuthConsumer;
import net.oauth.OAuthProblemException;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.Album;
import org.apache.shindig.social.opensocial.model.MediaItem;
import org.apache.shindig.social.opensocial.model.Message;
import org.apache.shindig.social.opensocial.model.MessageCollection;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.oauth.OAuthEntry;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.ActivityStreamService;
import org.apache.shindig.social.opensocial.spi.AlbumService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.MediaItemService;
import org.apache.shindig.social.opensocial.spi.MessageService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.util.Base64UUID;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

import eu.role_project.service.resource.ROLETerms;
import eu.role_project.service.space.SpaceService;

@Singleton
public class ConserveShindigService implements ActivityService, PersonService,
		AppDataService, MessageService, AlbumService, MediaItemService,
		ActivityStreamService, OAuthDataStore {

	private static Logger log = LoggerFactory
			.getLogger(ConserveShindigService.class);

	private static final String PERSON_NAMESPACE = "http://kmr.csc.kth.se/rdf/opensocial/person/";

	private final Map<String, Enum<?>> personVocabularyMap;

	@Inject
	private Injector injector;

	@Inject
	@Named("conserve.user.context")
	private UUID userContext;

	@Inject
	@Named("conserve.user.predicate")
	private UUID userPredicate;

	@Inject
	@Named("shindig.bean.converter.json")
	BeanConverter converter;

	public ConserveShindigService() {
		Map<String, Enum<?>> personVocabularyMap = new HashMap<String, Enum<?>>();
		personVocabularyMap.put("http://purl.org/dc/terms/title",
				Person.Field.DISPLAY_NAME);
		this.personVocabularyMap = Collections
				.unmodifiableMap(personVocabularyMap);
	}

	private Contemp conserve() {
		return injector.getInstance(Contemp.class);
	}

	private <T> T conceptToObject(Concept person, Set<String> fields,
			String fieldNamespace, Map<String, Enum<?>> vocabularyMap,
			Class<T> classT) {
		Content metadata = conserve().query().in(person)
				.as(ConserveTerms.metadata).require();
		log.info("Found metadata: " + metadata);
		URI uri = conserve().query().in(person).uri();
		Graph graph = conserve().query().as(metadata).graph();
		Object subject = graph.getValueFactory().createURI(uri.toString());
		log.info("Created graph: " + graph + " For: " + subject);
		JSONObject jsonObj = new JSONObject();
		try {
			for (Statement statement : graph) {
				log.info("Found statement: " + statement);
				if (statement.getSubject().equals(subject)) {
					String predicate = statement.getPredicate().stringValue();
					log.info("Found predicate: " + predicate);
					String field = null;
					if (predicate.startsWith(PERSON_NAMESPACE)) {
						field = predicate.substring(PERSON_NAMESPACE.length());
						log.info("Found unmapped field: " + field);
					} else if (personVocabularyMap.containsKey(predicate)) {
						field = personVocabularyMap.get(predicate).toString();
						log.info("Found mapped field: " + field);
					}
					if (field != null && fields.contains(field)) {
						log.info("This field is wanted, adding to object: "
								+ field);
						jsonObj.put(field, statement.getObject().stringValue());
					}
				}
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		log.info("Creating object: " + jsonObj.toString());
		T osObj = converter.convertToObject(jsonObj.toString(), classT);
		log.info("Instantiated object: " + osObj);
		return osObj;
	}

	public Future<MediaItem> getMediaItem(UserId userId, String appId,
			String albumId, String mediaItemId, Set<String> fields,
			SecurityToken token) throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"MediaItems are not supported");
	}

	public Future<RestfulCollection<MediaItem>> getMediaItems(UserId userId,
			String appId, String albumId, Set<String> mediaItemIds,
			Set<String> fields, CollectionOptions options, SecurityToken token)
			throws ProtocolException {
		return ImmediateFuture.newInstance(new RestfulCollection<MediaItem>(
				new ArrayList<MediaItem>()));
	}

	public Future<RestfulCollection<MediaItem>> getMediaItems(UserId userId,
			String appId, String albumId, Set<String> fields,
			CollectionOptions options, SecurityToken token)
			throws ProtocolException {
		return ImmediateFuture.newInstance(new RestfulCollection<MediaItem>(
				new ArrayList<MediaItem>()));
	}

	public Future<RestfulCollection<MediaItem>> getMediaItems(
			Set<UserId> userIds, GroupId groupId, String appId,
			Set<String> fields, CollectionOptions options, SecurityToken token)
			throws ProtocolException {
		return ImmediateFuture.newInstance(new RestfulCollection<MediaItem>(
				new ArrayList<MediaItem>()));
	}

	public Future<Void> deleteMediaItem(UserId userId, String appId,
			String albumId, String mediaItemId, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"MediaItems are not supported");
	}

	public Future<Void> createMediaItem(UserId userId, String appId,
			String albumId, MediaItem mediaItem, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"MediaItems are not supported");
	}

	public Future<Void> updateMediaItem(UserId userId, String appId,
			String albumId, String mediaItemId, MediaItem mediaItem,
			SecurityToken token) throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"MediaItems are not supported");
	}

	public Future<Album> getAlbum(UserId userId, String appId,
			Set<String> fields, String albumId, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Albums are not supported");
	}

	public Future<RestfulCollection<Album>> getAlbums(UserId userId,
			String appId, Set<String> fields, CollectionOptions options,
			Set<String> albumIds, SecurityToken token) throws ProtocolException {
		return ImmediateFuture.newInstance(new RestfulCollection<Album>(
				new ArrayList<Album>()));
	}

	public Future<RestfulCollection<Album>> getAlbums(Set<UserId> userIds,
			GroupId groupId, String appId, Set<String> fields,
			CollectionOptions options, SecurityToken token)
			throws ProtocolException {
		return ImmediateFuture.newInstance(new RestfulCollection<Album>(
				new ArrayList<Album>()));
	}

	public Future<Void> deleteAlbum(UserId userId, String appId,
			String albumId, SecurityToken token) throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Albums are not supported");
	}

	public Future<Void> createAlbum(UserId userId, String appId, Album album,
			SecurityToken token) throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Albums are not supported");
	}

	public Future<Void> updateAlbum(UserId userId, String appId, Album album,
			String albumId, SecurityToken token) throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Albums are not supported");
	}

	public Future<RestfulCollection<MessageCollection>> getMessageCollections(
			UserId userId, Set<String> fields, CollectionOptions options,
			SecurityToken token) throws ProtocolException {
		return ImmediateFuture
				.newInstance(new RestfulCollection<MessageCollection>(
						new ArrayList<MessageCollection>()));
	}

	public Future<MessageCollection> createMessageCollection(UserId userId,
			MessageCollection msgCollection, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Message collections are not supported");
	}

	public Future<Void> modifyMessageCollection(UserId userId,
			MessageCollection msgCollection, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Message collections are not supported");
	}

	public Future<Void> deleteMessageCollection(UserId userId,
			String msgCollId, SecurityToken token) throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Message collections are not supported");
	}

	public Future<RestfulCollection<Message>> getMessages(UserId userId,
			String msgCollId, Set<String> fields, List<String> msgIds,
			CollectionOptions options, SecurityToken token)
			throws ProtocolException {
		return ImmediateFuture.newInstance(new RestfulCollection<Message>(
				new ArrayList<Message>()));
	}

	public Future<Void> createMessage(UserId userId, String appId,
			String msgCollId, Message message, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Messages are not supported");
	}

	public Future<Void> deleteMessages(UserId userId, String msgCollId,
			List<String> ids, SecurityToken token) throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Messages are not supported");
	}

	public Future<Void> modifyMessage(UserId userId, String msgCollId,
			String messageId, Message message, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Messages are not supported");
	}

	// private Set<String> getIdSet(UserId user, GroupId group, SecurityToken
	// token) {
	// String userId = user.getUserId(token);
	// if (group == null) {
	// return ImmutableSortedSet.of(userId);
	// }
	// Set<String> returnVal = Sets.newLinkedHashSet();
	// switch (group.getType()) {
	// case all:
	// case friends:
	// case groupId:
	// // if (db.getJSONObject(FRIEND_LINK_TABLE).has(userId)) {
	// // JSONArray friends = db.getJSONObject(FRIEND_LINK_TABLE)
	// // .getJSONArray(userId);
	// // for (int i = 0; i < friends.length(); i++) {
	// // returnVal.add(friends.getString(i));
	// // }
	// // }
	// break;
	// case self:
	// returnVal.add(userId);
	// break;
	// }
	// return returnVal;
	// }
	//
	// private Set<String> getIdSet(Set<UserId> users, GroupId group,
	// SecurityToken token) {
	// Set<String> ids = Sets.newLinkedHashSet();
	// for (UserId user : users) {
	// ids.addAll(getIdSet(user, group, token));
	// }
	// return ids;
	// }

	private Concept getApplication(UserId userId, String appId,
			SecurityToken token) {
		UUID spaceUuid = null, toolUuid = null;
		try {
			JSONObject trustedJson = new JSONObject(token.getTrustedJson());
			log.info("Found trusted JSON: " + trustedJson);
			spaceUuid = Base64UUID.decode(trustedJson.get("space").toString());
			toolUuid = Base64UUID.decode(trustedJson.get("tool").toString());
		} catch (JSONException e) {
			log.error("JSON error", e);
		}
		Concept person = null;
		Concept application = null;
		switch (userId.getType()) {
		case owner:
			if (spaceUuid != null && toolUuid != null) {
				application = conserve().query().in(spaceUuid)
						.sub(ROLETerms.tool).require(toolUuid);
			} else if (token.getOwnerId() != null) {
				UUID ownerUuid = Base64UUID.decode(token.getOwnerId());
				person = conserve().query().in(userContext).sub(userPredicate)
						.require(ownerUuid);
			}
			break;
		case viewer:
		case me:
			if (token.getViewerId() != null) {
				if (token.getViewerId().equals(token.getOwnerId())
						&& spaceUuid != null && toolUuid != null) {
					application = conserve().query().in(spaceUuid)
							.sub(ROLETerms.tool).require(toolUuid);
				} else {
					UUID viewerUuid = Base64UUID.decode(token.getViewerId());
					person = conserve().query().in(userContext)
							.sub(userPredicate).require(viewerUuid);
				}
			}
			break;
		case userId:
			person = conserve().query().in(userContext).sub(userPredicate)
					.get(userId.getUserId());
			break;
		default:
			return null;
		}
		if (person != null && application == null) {
			log.info("Found person: " + person);
			log.info("Owner id: " + token.getOwnerId());
			application = conserve().query().in(person)
					.sub(ConserveTerms.annotates)
					.acquire(URI.create(normalizeAppId(appId)));
		}
		return application;
	}

	private String normalizeAppId(String appId) {
		int queryIndex = appId.indexOf('?');
		return queryIndex == -1 ? appId : appId.substring(0, queryIndex);
	}

	public Future<DataCollection> getPersonData(Set<UserId> userIds,
			GroupId groupId, String appId, Set<String> fields,
			SecurityToken token) throws ProtocolException {
		log.info("Getting person data for: " + userIds);
		Map<String, Map<String, String>> dataCollection = Maps.newHashMap();
		for (UserId userId : userIds) {
			Map<String, String> appData = Maps.newHashMap();
			Concept application = getApplication(userId, appId, token);
			if (application != null) {
				log.info("Found application: " + application);
				List<Concept> dataList = conserve().query().in(application)
						.sub(ROLETerms.data).list();
				for (Concept data : dataList) {
					log.info("Found data: " + data.getId());
					if (fields.isEmpty() || fields.contains(data.getId())) {
						Content jsonContent = conserve().query().in(data)
								.as(ConserveTerms.representation).get();
						if (jsonContent != null
								&& jsonContent.getType().equals(
										MediaType.APPLICATION_JSON)) {
							log.info("Found content: " + jsonContent.getType());
							String jsonString = conserve().query()
									.as(jsonContent).string();
							log.info("Retrieved data string: " + jsonString);
							appData.put(data.getId(), jsonString);
						}
					}
				}
			}
			dataCollection.put(userId.getUserId(token), appData);
		}
		return ImmediateFuture.newInstance(new DataCollection(dataCollection));
	}

	public Future<Void> deletePersonData(UserId userId, GroupId groupId,
			String appId, Set<String> fields, SecurityToken token)
			throws ProtocolException {
		Concept application = getApplication(userId, appId, token);
		if (application != null) {
			List<Concept> dataList = conserve().query().in(application)
					.sub(ROLETerms.data).list();
			for (Concept data : dataList) {
				Content jsonContent = conserve().query().in(data)
						.as(ConserveTerms.representation).get();
				if (jsonContent != null
						&& jsonContent.getType().equals(
								MediaType.APPLICATION_JSON)) {
					if (fields.contains(data.getId())) {
						conserve().deleteConcept(data);
					}
				}
			}
		}
		return ImmediateFuture.newInstance(null);
	}

	public Future<Void> updatePersonData(UserId userId, GroupId groupId,
			String appId, Set<String> fields, Map<String, String> values,
			SecurityToken token) throws ProtocolException {
		Concept application = getApplication(userId, appId, token);
		List<Concept> dataList = conserve().query().in(application)
				.sub(ROLETerms.data).list();
		log.info("Found data list: " + dataList.size());
		for (Concept data : dataList) {
			Content jsonContent = conserve().query().in(data)
					.as(ConserveTerms.representation).get();
			if (jsonContent != null
					&& jsonContent.getType().equals(MediaType.APPLICATION_JSON)) {
				if (fields.contains(data.getId())
						&& !values.containsKey(data.getId())) {
					log.info("Deleting app data: " + data.getId());
					conserve().deleteConcept(data);
				}
			}
		}
		for (Entry<String, String> value : values.entrySet()) {
			log.info("Storing new app data: " + value);
			Concept data = conserve().query().in(application)
					.sub(ROLETerms.data).acquire(value.getKey());
			conserve().query().in(data).as(ConserveTerms.representation)
					.type(MediaType.APPLICATION_JSON).string(value.getValue());
		}
		return ImmediateFuture.newInstance(null);
	}

	public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds,
			GroupId groupId, CollectionOptions collectionOptions,
			Set<String> fields, SecurityToken token) throws ProtocolException {
		return ImmediateFuture.newInstance(new RestfulCollection<Person>(
				new ArrayList<Person>()));
	}

	public Future<Person> getPerson(UserId userId, Set<String> fields,
			SecurityToken token) throws ProtocolException {
		String id = userId.getUserId(token);
		if (id == null) {
			return ImmediateFuture.newInstance(null);
		}
		Concept person = conserve().query().get(Base64UUID.decode(id));
		if (person == null) {
			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
					"Person '" + userId.getUserId(token) + "' not found");
		}
		Person personObj = conceptToObject(person, fields, PERSON_NAMESPACE,
				personVocabularyMap, Person.class);
		personObj.setId(Base64UUID.encode(person.getUuid()));
		return ImmediateFuture.newInstance(personObj);
	}

	public Future<RestfulCollection<Activity>> getActivities(
			Set<UserId> userIds, GroupId groupId, String appId,
			Set<String> fields, CollectionOptions options, SecurityToken token)
			throws ProtocolException {
		return ImmediateFuture.newInstance(new RestfulCollection<Activity>(
				new ArrayList<Activity>()));
	}

	public Future<RestfulCollection<Activity>> getActivities(UserId userId,
			GroupId groupId, String appId, Set<String> fields,
			CollectionOptions options, Set<String> activityIds,
			SecurityToken token) throws ProtocolException {
		return ImmediateFuture.newInstance(new RestfulCollection<Activity>(
				new ArrayList<Activity>()));
	}

	public Future<Activity> getActivity(UserId userId, GroupId groupId,
			String appId, Set<String> fields, String activityId,
			SecurityToken token) throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Activities are not supported");
	}

	public Future<Void> deleteActivities(UserId userId, GroupId groupId,
			String appId, Set<String> activityIds, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Activities are not supported");
	}

	public Future<Void> createActivity(UserId userId, GroupId groupId,
			String appId, Set<String> fields, Activity activity,
			SecurityToken token) throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Activities are not supported");
	}

	public Future<RestfulCollection<ActivityEntry>> getActivityEntries(
			Set<UserId> userIds, GroupId groupId, String appId,
			Set<String> fields, CollectionOptions options, SecurityToken token)
			throws ProtocolException {
		return ImmediateFuture
				.newInstance(new RestfulCollection<ActivityEntry>(
						new ArrayList<ActivityEntry>()));
	}

	public Future<RestfulCollection<ActivityEntry>> getActivityEntries(
			UserId userId, GroupId groupId, String appId, Set<String> fields,
			CollectionOptions options, Set<String> activityIds,
			SecurityToken token) throws ProtocolException {
		return ImmediateFuture
				.newInstance(new RestfulCollection<ActivityEntry>(
						new ArrayList<ActivityEntry>()));
	}

	public Future<ActivityEntry> getActivityEntry(UserId userId,
			GroupId groupId, String appId, Set<String> fields,
			String activityId, SecurityToken token) throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Activity entries are not supported");
	}

	public Future<Void> deleteActivityEntries(UserId userId, GroupId groupId,
			String appId, Set<String> activityIds, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Activity entries are not supported");
	}

	public Future<ActivityEntry> updateActivityEntry(UserId userId,
			GroupId groupId, String appId, Set<String> fields,
			ActivityEntry activity, String activityId, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Activity entries are not supported");
	}

	public Future<ActivityEntry> createActivityEntry(UserId userId,
			GroupId groupId, String appId, Set<String> fields,
			ActivityEntry activity, SecurityToken token)
			throws ProtocolException {
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Activity entries are not supported");
	}

	public OAuthEntry getEntry(String oauthToken) {
		// TODO Auto-generated method stub
		return null;
	}

	public SecurityToken getSecurityTokenForConsumerRequest(String consumerKey,
			String userId) throws OAuthProblemException {
		// TODO Auto-generated method stub
		return null;
	}

	public OAuthConsumer getConsumer(String consumerKey)
			throws OAuthProblemException {
		// TODO Auto-generated method stub
		return null;
	}

	public OAuthEntry generateRequestToken(String consumerKey,
			String oauthVersion, String signedCallbackUrl)
			throws OAuthProblemException {
		// TODO Auto-generated method stub
		return null;
	}

	public OAuthEntry convertToAccessToken(OAuthEntry entry)
			throws OAuthProblemException {
		// TODO Auto-generated method stub
		return null;
	}

	public void authorizeToken(OAuthEntry entry, String userId)
			throws OAuthProblemException {
		// TODO Auto-generated method stub

	}

	public void disableToken(OAuthEntry entry) {
		// TODO Auto-generated method stub

	}

	public void removeToken(OAuthEntry entry) {
		// TODO Auto-generated method stub

	}

}