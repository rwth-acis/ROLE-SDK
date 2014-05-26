package eu.role_project.service.shindig;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.model.Enum;
import org.apache.shindig.social.opensocial.model.Account;
import org.apache.shindig.social.opensocial.model.Address;
import org.apache.shindig.social.opensocial.model.BodyType;
import org.apache.shindig.social.opensocial.model.Drinker;
import org.apache.shindig.social.opensocial.model.ListField;
import org.apache.shindig.social.opensocial.model.LookingFor;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.NetworkPresence;
import org.apache.shindig.social.opensocial.model.Organization;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.model.Smoker;
import org.apache.shindig.social.opensocial.model.Url;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Contemp;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;

public class ConservePersonService implements PersonService {

	private static Logger log = LoggerFactory
			.getLogger(ConservePersonService.class);

	@Inject
	private Injector injector;

	@Inject
	@Named("conserve.user.context")
	private UUID userContext;

	@Inject
	@Named("conserve.user.predicate")
	private UUID userPredicate;

	private Contemp conserve() {
		return injector.getInstance(Contemp.class);
	}

	public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds,
			GroupId groupId, CollectionOptions collectionOptions,
			Set<String> fields, SecurityToken token) throws ProtocolException {
		return ImmediateFuture.newInstance(new RestfulCollection<Person>(
				new ArrayList<Person>()));
	}

	public Future<Person> getPerson(UserId id, Set<String> fields,
			SecurityToken token) throws ProtocolException {
		log.info("Getting person");
		log.info("User id: " + id.getUserId(token));
		Concept person = conserve().query().in(userContext).sub(userPredicate)
				.get(id.getUserId(token));
		Person personObj = filterFields(person, fields, Person.class);
		// Map<String, Object> appData = getPersonAppData(
		// person.getString(Person.Field.ID.toString()), fields);
		// personObj.setAppData(appData);
		personObj.setAppData(new HashMap<String, Object>());
		return ImmediateFuture.newInstance(personObj);
	}

	@SuppressWarnings("unchecked")
	public <T> T filterFields(Concept object, Set<String> fields, Class<T> clz) {
		Content metadata = conserve().query().in(object)
				.as(ConserveTerms.metadata).require();
		URI uri = conserve().query().in(object).uri();
		Graph graph = conserve().query().as(metadata).graph();
		Repository myRepository = new SailRepository(new MemoryStore());
		ValueFactory valueFactory = myRepository.getValueFactory();
		String title = null;
		try {
			myRepository.initialize();
			RepositoryConnection conn = myRepository.getConnection();
			conn.setNamespace("dcterms", "http://purl.org/dc/terms/");
			conn.add(graph);
			RepositoryConnection con = myRepository.getConnection();
			try {
				String queryString = "SELECT {title} FROM {x} dcterms:title {title}";
				TupleQuery tupleQuery = con.prepareTupleQuery(
						QueryLanguage.SERQL, queryString);
				tupleQuery.setBinding("x",
						valueFactory.createURI(uri.toString()));
				TupleQueryResult result = tupleQuery.evaluate();
				try {
					if (result.hasNext()) {
						title = result.next().getBinding("title").getValue()
								.stringValue();
					}
				} finally {
					result.close();
				}
			} finally {
				con.close();
			}
		} catch (OpenRDFException e) {
			// handle exception
		}
		final String finalTitle = title;
		log.info("Got person display name: " + finalTitle);

		Person person = new Person() {
			public String getDisplayName() {
				return finalTitle;
			}

			public void setDisplayName(String displayName) {
				// TODO Auto-generated method stub

			}

			public String getAboutMe() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setAboutMe(String aboutMe) {
				// TODO Auto-generated method stub

			}

			public List<Account> getAccounts() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setAccounts(List<Account> accounts) {
				// TODO Auto-generated method stub

			}

			public List<String> getActivities() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setActivities(List<String> activities) {
				// TODO Auto-generated method stub

			}

			public List<Address> getAddresses() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setAddresses(List<Address> addresses) {
				// TODO Auto-generated method stub

			}

			public Integer getAge() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setAge(Integer age) {
				// TODO Auto-generated method stub

			}

			public Map<String, ?> getAppData() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setAppData(Map<String, ?> appData) {
				// TODO Auto-generated method stub

			}

			public Date getBirthday() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setBirthday(Date birthday) {
				// TODO Auto-generated method stub

			}

			public BodyType getBodyType() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setBodyType(BodyType bodyType) {
				// TODO Auto-generated method stub

			}

			public List<String> getBooks() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setBooks(List<String> books) {
				// TODO Auto-generated method stub

			}

			public List<String> getCars() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setCars(List<String> cars) {
				// TODO Auto-generated method stub

			}

			public String getChildren() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setChildren(String children) {
				// TODO Auto-generated method stub

			}

			public Address getCurrentLocation() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setCurrentLocation(Address currentLocation) {
				// TODO Auto-generated method stub

			}

			public Enum<Drinker> getDrinker() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setDrinker(Enum<Drinker> newDrinker) {
				// TODO Auto-generated method stub

			}

			public List<ListField> getEmails() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setEmails(List<ListField> emails) {
				// TODO Auto-generated method stub

			}

			public String getEthnicity() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setEthnicity(String ethnicity) {
				// TODO Auto-generated method stub

			}

			public String getFashion() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setFashion(String fashion) {
				// TODO Auto-generated method stub

			}

			public List<String> getFood() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setFood(List<String> food) {
				// TODO Auto-generated method stub

			}

			public Gender getGender() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setGender(Gender newGender) {
				// TODO Auto-generated method stub

			}

			public String getHappiestWhen() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setHappiestWhen(String happiestWhen) {
				// TODO Auto-generated method stub

			}

			public Boolean getHasApp() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setHasApp(Boolean hasApp) {
				// TODO Auto-generated method stub

			}

			public List<String> getHeroes() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setHeroes(List<String> heroes) {
				// TODO Auto-generated method stub

			}

			public String getHumor() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setHumor(String humor) {
				// TODO Auto-generated method stub

			}

			public String getId() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setId(String id) {
				// TODO Auto-generated method stub

			}

			public List<ListField> getIms() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setIms(List<ListField> ims) {
				// TODO Auto-generated method stub

			}

			public List<String> getInterests() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setInterests(List<String> interests) {
				// TODO Auto-generated method stub

			}

			public String getJobInterests() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setJobInterests(String jobInterests) {
				// TODO Auto-generated method stub

			}

			public List<String> getLanguagesSpoken() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setLanguagesSpoken(List<String> languagesSpoken) {
				// TODO Auto-generated method stub

			}

			public Date getUpdated() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setUpdated(Date updated) {
				// TODO Auto-generated method stub

			}

			public String getLivingArrangement() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setLivingArrangement(String livingArrangement) {
				// TODO Auto-generated method stub

			}

			public List<Enum<LookingFor>> getLookingFor() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setLookingFor(List<Enum<LookingFor>> lookingFor) {
				// TODO Auto-generated method stub

			}

			public List<String> getMovies() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setMovies(List<String> movies) {
				// TODO Auto-generated method stub

			}

			public List<String> getMusic() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setMusic(List<String> music) {
				// TODO Auto-generated method stub

			}

			public Name getName() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setName(Name name) {
				// TODO Auto-generated method stub

			}

			public Enum<NetworkPresence> getNetworkPresence() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setNetworkPresence(Enum<NetworkPresence> networkPresence) {
				// TODO Auto-generated method stub

			}

			public String getNickname() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setNickname(String nickname) {
				// TODO Auto-generated method stub

			}

			public List<Organization> getOrganizations() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setOrganizations(List<Organization> organizations) {
				// TODO Auto-generated method stub

			}

			public String getPets() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setPets(String pets) {
				// TODO Auto-generated method stub

			}

			public List<ListField> getPhoneNumbers() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setPhoneNumbers(List<ListField> phoneNumbers) {
				// TODO Auto-generated method stub

			}

			public List<ListField> getPhotos() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setPhotos(List<ListField> photos) {
				// TODO Auto-generated method stub

			}

			public String getPoliticalViews() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setPoliticalViews(String politicalViews) {
				// TODO Auto-generated method stub

			}

			public String getPreferredUsername() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setPreferredUsername(String preferredString) {
				// TODO Auto-generated method stub

			}

			public Url getProfileSong() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setProfileSong(Url profileSong) {
				// TODO Auto-generated method stub

			}

			public Url getProfileVideo() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setProfileVideo(Url profileVideo) {
				// TODO Auto-generated method stub

			}

			public List<String> getQuotes() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setQuotes(List<String> quotes) {
				// TODO Auto-generated method stub

			}

			public String getRelationshipStatus() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setRelationshipStatus(String relationshipStatus) {
				// TODO Auto-generated method stub

			}

			public String getReligion() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setReligion(String religion) {
				// TODO Auto-generated method stub

			}

			public String getRomance() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setRomance(String romance) {
				// TODO Auto-generated method stub

			}

			public String getScaredOf() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setScaredOf(String scaredOf) {
				// TODO Auto-generated method stub

			}

			public String getSexualOrientation() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setSexualOrientation(String sexualOrientation) {
				// TODO Auto-generated method stub

			}

			public Enum<Smoker> getSmoker() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setSmoker(Enum<Smoker> newSmoker) {
				// TODO Auto-generated method stub

			}

			public List<String> getSports() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setSports(List<String> sports) {
				// TODO Auto-generated method stub

			}

			public String getStatus() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setStatus(String status) {
				// TODO Auto-generated method stub

			}

			public List<String> getTags() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setTags(List<String> tags) {
				// TODO Auto-generated method stub

			}

			public Long getUtcOffset() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setUtcOffset(Long utcOffset) {
				// TODO Auto-generated method stub

			}

			public List<String> getTurnOffs() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setTurnOffs(List<String> turnOffs) {
				// TODO Auto-generated method stub

			}

			public List<String> getTurnOns() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setTurnOns(List<String> turnOns) {
				// TODO Auto-generated method stub

			}

			public List<String> getTvShows() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setTvShows(List<String> tvShows) {
				// TODO Auto-generated method stub

			}

			public List<Url> getUrls() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setUrls(List<Url> urls) {
				// TODO Auto-generated method stub

			}

			public boolean getIsOwner() {
				// TODO Auto-generated method stub
				return false;
			}

			public void setIsOwner(boolean isOwner) {
				// TODO Auto-generated method stub

			}

			public boolean getIsViewer() {
				// TODO Auto-generated method stub
				return false;
			}

			public void setIsViewer(boolean isViewer) {
				// TODO Auto-generated method stub

			}

			public String getProfileUrl() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setProfileUrl(String profileUrl) {
				// TODO Auto-generated method stub

			}

			public String getThumbnailUrl() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setThumbnailUrl(String thumbnailUrl) {
				// TODO Auto-generated method stub

			}
		};
		return (T) person;
	}
}