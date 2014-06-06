package eu.role_project.service.dui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;

import eu.role_project.service.dui.iface.DUIService;
import eu.role_project.service.resource.ROLETerms;
import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.Resolution;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.logic.ResourceResponder;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

public class DuiResponder extends ResourceResponder {
	private static Logger log = LoggerFactory.getLogger(DuiResponder.class);
	public static final UUID ID = UUID.fromString("7bb68751-0b6f-5c04-a2d2-1c92787d608d");
	
	@Inject
	private SecurityInfo securityInfo;
	@Inject
	private DUIService duiService;
	private StringBuilder sb = new StringBuilder();
	private JSONParser parser = new JSONParser();
	private final RandomBasedGenerator generator = Generators
			.randomBasedGenerator();
	@Inject
	@Named("conserve.session.context")
	private UUID sessionContext;
	
	/**
	 * As an initializer registered in DuiModule, it is called to create the dui context which binds to the "/dui" url
	 */
	@Override
	public void initialize(Request request) {
		// Initialize the "dui" context
		log.info("Initializing /dui");
		store().in(app.getRootUuid(request)).sub(ROLETerms.duiService)
				.acquire(ID, "dui");
	}

	@Override
	public Resolution resolve(Request request) {
		Resolution resolution = super.resolve(request);
		if (Resolution.StandardType.CONTEXT.equals(resolution.getType())
				&& resolution.getContext() == null) {
			Concept duiService = store().get(ID);
			return new Resolution(Resolution.StandardType.CONTEXT,
						duiService);
		}
		return resolution;
	}
	
	/**
	 * Get the user concept of the current user who sends the request
	 * @return the user concept
	 */
	private Concept getUser(){
		if (securityInfo.getAgent()!= null){
			Concept user = store().get(securityInfo.getAgent());
			return user;
		}
		return null;
	}
	
	/**
	 * Get the space concept
	 * @param spaceUri the uri of the space
	 * @return the space concept
	 */
	private Concept getSpace(String spaceUri){
		return store().resolve(spaceUri);
//		return store.getConcept(SpaceService.ID, spaceUri.substring(spaceUri.lastIndexOf("/")+1));
	}
	
	/**
	 * Get the device confiugration which contains the list of widgets to be displayed
	 * @param member the member concept of the user in the space
	 * @param deviceName the name of the device
	 * @return the device configuration concept
	 */
	private Concept getDeviceConfig(Concept member, String deviceName){
		return store().in(member).sub(ROLETerms.dc).get(deviceName);
	}
	
	@SuppressWarnings("unused")
	private boolean isMobile(Concept device){
		Content dProfile = store().in(device).as(ROLETerms.dp).get();
		if (dProfile != null){
			String content = store().as(dProfile).string();
			try {
				JSONObject obj = (JSONObject)parser.parse(content);
				boolean isMobile = (Boolean)obj.get("isMobile");
				return isMobile;
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Get the member concept
	 * @param user the user concept
	 * @param space the space concept
	 * @return the member concept
	 */
	private Concept getMemberForUser(Concept user, Concept space){
		List<Concept> concepts = store.getConcepts(space.getUuid());
		for (Concept cpt: concepts)
			if (ROLETerms.member.equals(cpt.getPredicate())){
				List<Control> references = store().in(cpt).get(ConserveTerms.reference);
				if (references != null && references.size() > 0 
						&& references.get(0).getObject().equals(user.getUuid()))
					return cpt;
			}
		return null;
	}
	
	/**
	 * Get the widget list to be displayed on the device
	 * @param deviceConfig 
	 * @return the list of widget Uris
	 */
	private List<String> getDisplayWidgets(Concept deviceConfig){
		Content aw = store().in(deviceConfig).as(ROLETerms.aw).get();
		if (aw != null){
			String widgetsListJson = store().as(aw).string();
			Object obj;
			List<String> dwList = new LinkedList<String>();
			try {
				obj = parser.parse(widgetsListJson);
				JSONArray widgets = (JSONArray)obj;
				for (Object wUri: widgets){
					String widgetUri = (String)wUri;
					Concept widget = store().resolve(widgetUri);
					if (widget != null && ROLETerms.tool.equals(widget.getPredicate())){
						boolean isOpenSocialGadget = null != store.getControls(
								widget.getUuid(), ConserveTerms.type,
								ROLETerms.OpenSocialGadget);
						if (isOpenSocialGadget)
							dwList.add(widgetUri);
					}
				}
				return dwList;
			} catch (ParseException e) {
				e.printStackTrace();
				return dwList;
			}
		}
		else{
			LinkedList<String> awl = new LinkedList<String>();
			return awl;
		}
	}
	
	/**
	 * Get the device on which the widget is displayed
	 * @param widget
	 * @param user
	 * @param member
	 * @return the list of device ids
	 */
	private List<String> getWidgetLocation(Concept widget, Concept user, Concept member){
		List<String> devices = new ArrayList<String>();
		if (member == null){
			List<Concept> dvs = store().in(user).sub(ROLETerms.device).list();
			for (Concept device: dvs)
				if (device.getPredicate().equals(ROLETerms.device))
					devices.add(device.getId());
		}
		else{
			List<Concept> dcl = store().in(member).sub(ROLETerms.dc).list();
			for (Concept dcfg: dcl){
				log.info(dcfg.getUuid().toString());
				UUID dUuid = store().in(dcfg).get(ConserveTerms.reference).get(0).getObject();
				Concept device = store.getConcept(user.getUuid(), dUuid);
				if (device == null){
					store.deleteConcept(dcfg);
					continue;
				}
				//for each cfg, look for the widget
				Content aw = store().in(dcfg).as(ROLETerms.aw).get();
				if (aw != null){
					String widgetsListJson = store().as(aw).string();
					Object obj;
					try {
						obj = parser.parse(widgetsListJson);
						JSONArray widgets = (JSONArray)obj;
						for (Object wUri: widgets){
							String widgetUri = (String)wUri;
							if (store().in(widget).uri().toString().equals(widgetUri)){
								devices.add(device.getId());
								break;
							}
						}
					} catch (ParseException e) {
						e.printStackTrace();
//						LinkedList<String> awl = new LinkedList<String>();
//						String awStr = JSONValue.toJSONString(awl);
//						store().in(dcfg).as(ROLETerms.aw).type("application/json").string(awStr);
						return devices;
					}
				}
//				else{
//					LinkedList<String> awl = new LinkedList<String>();
//					String awStr = JSONValue.toJSONString(awl);
//					store().in(dcfg).as(ROLETerms.aw).type("application/json").string(awStr);
//				}
			}
		}
		return devices;
	}
	
	@SuppressWarnings("unused")
	private void attachDeviceCookieToSession(Request request, String deviceName){
			HttpHeaders httpHeaders = ((RequestImpl) request).getRequestHeaders();
			Map<String, Cookie> cookies = httpHeaders.getCookies();
			if (cookies.containsKey("conserve_session")) {
				Cookie sessionCookie = cookies.get("conserve_session");
				String sessionCookieValue = sessionCookie.getValue();
				Concept session = store.getConcept(sessionContext, sessionCookieValue);
				if (session != null){
					store().in(session).as(ROLETerms.pd).type("text/plain").string(deviceName);
					String uri = store().in(getUser()).uri().toString();
					store().in(session).as(ROLETerms.pu).type("text/plain").string(uri.substring(uri.lastIndexOf("/")+1));
				}
			}
	}
	
	@SuppressWarnings("unused")
	private void detachDeviceCookieFromSession(Request request, String deviceName){
		HttpHeaders httpHeaders = ((RequestImpl) request).getRequestHeaders();
		Map<String, Cookie> cookies = httpHeaders.getCookies();
		if (cookies.containsKey("conserve_session")) {
			Cookie sessionCookie = cookies.get("conserve_session");
			String sessionCookieValue = sessionCookie.getValue();
			Concept session = store.getConcept(sessionContext, sessionCookieValue);
			if (session != null){
				Content preDv = store().in(session).as(ROLETerms.pd).get();
				Content preUsr = store().in(session).as(ROLETerms.pu).get();
				if (preDv != null && preUsr != null)
					store().as(preDv).string("");
					store().as(preUsr).string("");
			}
		}
	}
	
	/**
	 * Set widget location (on which device is the widget displayed)
	 * @param member the member concept where all device configurations are stored
	 * @param widgetUri the widget uri
	 * @param sourceDeviceName the previous widget location
	 * @param targetDevice the target widget location
	 */
	private void setWidgetLocation(Concept member, String widgetUri, String sourceDeviceName, Concept targetDevice){
		List<String> dwList, dwList1;
		if (sourceDeviceName != null){
			Concept srcDcfg = store().in(member).sub(ROLETerms.dc).get(sourceDeviceName);
			dwList = getDisplayWidgets(srcDcfg);
			dwList.remove(widgetUri);
			store().in(srcDcfg).as(ROLETerms.aw).type("application/json").string(JSONValue.toJSONString(dwList));
		}
		List<Concept> tarDcfgList = store().in(member).sub(ROLETerms.dc).list();
		for (Concept tarDcfg: tarDcfgList){
			if (!store().in(tarDcfg).get(ConserveTerms.reference).get(0).getObject().equals(targetDevice.getUuid()))
				continue;
			log.info(tarDcfg.getUuid().toString());
			dwList1 = getDisplayWidgets(tarDcfg);
			if (!dwList1.contains(widgetUri)){
				dwList1.add(widgetUri);
				Content c = store().in(tarDcfg).as(ROLETerms.aw).get();
				if (c == null)
					store().in(tarDcfg).as(ROLETerms.aw).type("application/json").string(JSONValue.toJSONString(dwList1));
				else{
	//				String s = store().as(c).string();
					store().as(c).string(JSONValue.toJSONString(dwList1));
				}
			}
		}
	}
	
	@Override
	public Object doGet(Request request) {
		String action = ((RequestImpl)request).getHttpServletRequest().getHeader("action");
		log.info("GET received.....action is " + action);
		
		if (action.equals("device")){ //get current device info -------------#space+user
			String spaceUri = ((RequestImpl)request).getLinkRelations().get("space");
			Concept space = getSpace(spaceUri);
			Concept user = getUser();
			String currentDeviceName = ((RequestImpl)request).getLinkRelations().get("deviceName");
			Concept device = store.getConcept(user.getUuid(), ROLETerms.device, currentDeviceName);
			if (device == null){
				//detachDeviceCookieFromSession(request, currentDeviceName);
				return Response.status(Response.Status.NOT_FOUND).build();
			}
//			store().in(device).as(ROLETerms.cs).type("text/plain").string(spaceUri);
//			Concept device = store().in(user).sub(ROLETerms.device).get(currentDeviceName);
			Concept member = getMemberForUser(user, space);
			if (member == null){
				List<Concept> cpts = store.getConcepts(space.getUuid());
				for (Concept cpt: cpts){
					if (ROLETerms.tool.equals(cpt.getPredicate())){
						boolean isOpenSocialGadget = null != store.getControls(
								cpt.getUuid(), ConserveTerms.type,
								ROLETerms.OpenSocialGadget);
						if (isOpenSocialGadget){
							UUID wid = cpt.getUuid();
							String widgetId = new Long(Math.abs(wid.getMostSignificantBits()
									^ wid.getLeastSignificantBits())).toString();
							sb.append(widgetId + ",");
						}
					}
				}
				if (sb.length()>0)
					sb.deleteCharAt(sb.length()-1);
				return Response.ok().type(MediaType.TEXT_PLAIN).entity(sb.toString()).build();
			}
			Concept dConfig = getDeviceConfig(member, currentDeviceName);
			if (dConfig != null){
				sb.delete(0, sb.length());
				List<String> widgetUris = getDisplayWidgets(dConfig);
				for (String widgetUri: widgetUris){
					UUID wid = store().resolve(widgetUri).getUuid();
					String widgetId = new Long(Math.abs(wid.getMostSignificantBits()
							^ wid.getLeastSignificantBits())).toString();
					sb.append(widgetId + ",");
				}
				if (sb.length() > 0)
					sb.deleteCharAt(sb.length()-1);
				return Response.ok().type(MediaType.TEXT_PLAIN).entity(sb.toString()).build();
			}
			else
				return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		
		if (action.equals("deviceInfo")){ //get selected device info---------#user
			Concept user = getUser();
			String deviceName = ((RequestImpl)request).getLinkRelations().get("deviceName");
			Concept device = store.getConcept(user.getUuid(), ROLETerms.device, deviceName);
			if (device == null)
				return Response.status(Response.Status.GONE).build();
			Map<String, Object> resp = new LinkedHashMap<String, Object>();
			resp.put("deviceName", deviceName);
			resp.put("owner", user.getId());
			Content c = store().in(device).as(ROLETerms.dp).get();
			if (c != null){
				JSONObject obj = new JSONObject();
				try {
					obj = (JSONObject) parser.parse(store().as(c).string());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				resp.put("description", obj);
			}
			Content currentSpace = store().in(device).as(ROLETerms.cs).get();
			if (currentSpace == null || store().as(currentSpace).string().equals("")){
				store().in(device).as(ROLETerms.cs).type("text/plain").string("");
				resp.put("currentSpace", "not active");
				resp.put("widgets", new LinkedList<String>());
			}
			else{
				Concept space = store().resolve(store().as(currentSpace).string());
				if (space != null){
					resp.put("currentSpace", space.getId());
					Concept member = getMemberForUser(user, space);
					if (member == null){
						LinkedList<String> wRef = new LinkedList<String>();
						List<Concept> cpts = store.getConcepts(space.getUuid());
						for (Concept cpt: cpts){
							if (ROLETerms.tool.equals(cpt.getPredicate())){
								boolean isOpenSocialGadget = null != store.getControls(
										cpt.getUuid(), ConserveTerms.type,
										ROLETerms.OpenSocialGadget);
								if (isOpenSocialGadget){
									wRef.add(store().in(cpt).get(ConserveTerms.reference).get(0).getUri());
								}
							}
						}
						resp.put("widgets", wRef);
					}
					else{
						Concept dConfig = getDeviceConfig(member, device.getId());
						if (dConfig != null){
							LinkedList<String> wRef = new LinkedList<String>();
							List<String> dwList = getDisplayWidgets(dConfig);
							for (String widgetUri: dwList){
								Concept widget = store().resolve(widgetUri);
								wRef.add(store().in(widget).get(ConserveTerms.reference).get(0).getUri());
							}
							resp.put("widgets", wRef);
						}
					}
				}
				else{
					store().in(device).as(ROLETerms.cs).type("text/plain").string("");
					resp.put("currentSpace", "not active");
				}
			}
			return Response.ok().type(MediaType.APPLICATION_JSON).entity(JSONValue.toJSONString(resp)).build();
		}
		
		if (action.equals("connectivity")){//init connectivity-------------#user
			Concept user = getUser();
			List<Concept> dList = store.getConcepts(user.getUuid());
			for (Concept device: dList)
				if (device.getPredicate().equals(ROLETerms.device))
					store().in(device).as(ROLETerms.cs).type("text/plain").string("");
			duiService.onCheckDevices(user.getUuid().toString());
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		if (action.equals("widget")){
//			Concept space = getSpace(((RequestImpl)request).getLinkRelations().get("space"));
			String widgetUri = ((RequestImpl)request).getLinkRelations().get("widgetUri");
			Concept widget = store().resolve(widgetUri);
			if (widget == null)
				return Response.status(Response.Status.GONE).build();
			boolean isOpenSocialGadget = null != store.getControls(
					widget.getUuid(), ConserveTerms.type,
					ROLETerms.OpenSocialGadget);
			if (!widget.getPredicate().equals(ROLETerms.tool)||!isOpenSocialGadget)
				return Response.status(Response.Status.CONFLICT).
						type(MediaType.TEXT_PLAIN).entity("no such widget found").build();
			Content ws = store().in(widget).as(ROLETerms.ws).get();
			if (ws != null){
				String stateStr = store().as(ws).string();
				return Response.ok().type(MediaType.APPLICATION_JSON).entity(stateStr).build();
			}
			return Response.ok().header("Cache-Control", "no-store").build();	
		}
		
		if (action.equals("appState")){//get app state ----------------------#space
			Concept space = getSpace(((RequestImpl)request).getLinkRelations().get("space"));
			Content appStates = store().in(space).as(ROLETerms.as).get();
			if (appStates != null){
				String stateStr = store().as(appStates).string();
				return Response.ok().type(MediaType.APPLICATION_JSON).entity(stateStr).build();
			}
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		if (action.equals("widgetLocation")){//get widget location ---------#space+user
			String spaceUri = ((RequestImpl)request).getLinkRelations().get("space");
			Concept space = getSpace(spaceUri);
			Concept user = getUser();
			String widgetUri = ((RequestImpl)request).getLinkRelations().get("widgetUri");
			Concept widget = store().resolve(widgetUri);
			if (widget == null)
				return Response.status(Response.Status.GONE).build();
			Concept member = getMemberForUser(user, space);
			List<String> devices = getWidgetLocation(widget, user, member);
			sb.delete(0, sb.length());
			for (String d: devices){
				Concept device = store.getConcept(user.getUuid(), ROLETerms.device, d);
				Content currentSpace = store().in(device).as(ROLETerms.cs).get();
				if (currentSpace != null && store().as(currentSpace).string().equals(spaceUri))
					sb.append(d+",");
			}
			if (sb.length()>0)
				sb.deleteCharAt(sb.length()-1);
			return Response.ok().type(MediaType.TEXT_PLAIN).entity(sb.toString()).build();
		}
		
		//here is the end of GET
		return Response.status(Response.Status.BAD_REQUEST).build();
	}


	@Override
	public Object doPut(Request request, byte[] data) {
		String action = ((RequestImpl)request).getHttpServletRequest().getHeader("action");
		log.info("PUT received.....action is " + action);
		
		if (action.equals("connected")){//confirm connected--------------#user
			Concept user = getUser();
			String deviceName = ((RequestImpl)request).getLinkRelations().get("deviceName");
			//Originally check from which space and publish the connectivity back to that space
			String isA = ((RequestImpl)request).getLinkRelations().get("isAnonymous");
			boolean isAnonymous = Boolean.valueOf(isA);
			Concept atSpace = getSpace(((RequestImpl)request).getLinkRelations().get("location"));
			if (!isAnonymous){
				Concept device = store().in(user).sub(ROLETerms.device).get(deviceName);
				store().in(device).as(ROLETerms.cs).type("text/plain").string(((RequestImpl)request).getLinkRelations().get("location"));
			}
			duiService.onDeviceConfirm(user.getUuid().toString(), deviceName, atSpace.getId(), isAnonymous);
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		if (action.equals("switch")){ //init/try switch device --------------------#user
			Concept user = getUser();
			String spaceUri = ((RequestImpl)request).getLinkRelations().get("space");
			String fromDeviceName = ((RequestImpl)request).getLinkRelations().get("fromDevice");
			String toDeviceName = ((RequestImpl)request).getLinkRelations().get("toDevice");
			Concept toDevice = store().in(user).sub(ROLETerms.device).get(toDeviceName);
			Concept fromDevice = null;
			if (fromDeviceName != null)
				fromDevice = store().in(user).sub(ROLETerms.device).get(fromDeviceName);
			Content currentSpace = store().in(toDevice).as(ROLETerms.cs).get();
			//toDevice is currently active
			if (currentSpace != null && !store().as(currentSpace).string().equals("")){
				String s = store().resolve(store().as(currentSpace).string()).getId();
				return Response.status(Response.Status.ACCEPTED).type(MediaType.TEXT_PLAIN).entity(s).build();
			}
			else{
				if (fromDevice != null)
					store().in(fromDevice).as(ROLETerms.cs).type("text/plain").string("");
				store().in(toDevice).as(ROLETerms.cs).type("text/plain").string(spaceUri);
				//attachDeviceCookieToSession(request, toDeviceName);
				return Response.ok().header("Cache-Control", "no-store").build();
			}
		}
		
		if (action.equals("forceSwitch")){ //force switch ------------------------#user
			Concept user = getUser();
			//the space on which the toDevice will be active after switch
			String spaceUri = ((RequestImpl)request).getLinkRelations().get("space");
			String fromDeviceName = ((RequestImpl)request).getLinkRelations().get("fromDevice");
			String toDeviceName = ((RequestImpl)request).getLinkRelations().get("toDevice");
			Concept toDevice = store().in(user).sub(ROLETerms.device).get(toDeviceName);
			Concept fromDevice = null;
			if (fromDeviceName != null)
				fromDevice = store().in(user).sub(ROLETerms.device).get(fromDeviceName);
			//have to remove the current space of the toDevice
			store().in(toDevice).as(ROLETerms.cs).type("text/plain").string(spaceUri);
			if (fromDevice != null)
				store().in(fromDevice).as(ROLETerms.cs).type("type/plain").string("");

			//attach the device info to the session and after reload the device can get the info from cookies
			//attachDeviceCookieToSession(request, toDeviceName);
			duiService.onSwitchDevice(user.getUuid().toString(), fromDeviceName, toDeviceName);
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		
		if (action.equals("initMigration")){//init widget migration ---------------#space+user
			String spaceUri = ((RequestImpl)request).getLinkRelations().get("space");
			Concept space = getSpace(spaceUri);
			Concept user = getUser();
			String widgetUri = ((RequestImpl)request).getLinkRelations().get("widgetUri");
			String targetDeviceName = ((RequestImpl)request).getLinkRelations().get("targetDevice");
			Concept targetDevice = store().in(user).sub(ROLETerms.device).get(targetDeviceName);
			Concept widget = store().resolve(widgetUri);
			if (targetDevice == null || widget == null)
				return Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_PLAIN)
						.entity((targetDevice==null?("device'"+targetDeviceName+"'"):"")+(widget==null?"widget":"")).build();
			//find the source device i.e. the current location of the widget
			Concept member = getMemberForUser(user, space);
//			List<String> devices = new ArrayList<String>();
//			getWidgetLocation(widget, user, member);
			List<Concept> dcl;
			//---------------------
					
			if (member == null){
				return Response.status(Response.Status.FORBIDDEN).build();
			}
			else{
				dcl = store().in(member).sub(ROLETerms.dc).list();
				for (Concept dcfg: dcl){
					log.info(dcfg.getUuid().toString());
					UUID dUuid = store().in(dcfg).get(ConserveTerms.reference).get(0).getObject();
					Concept device = store.getConcept(user.getUuid(), dUuid);
					if (device == null){
						store.deleteConcept(dcfg);
						continue;
					}
					//for each cfg, look for the widget
					Content aw = store().in(dcfg).as(ROLETerms.aw).get();
					if (aw != null){
						String widgetsListJson = store().as(aw).string();
						Object obj;
						boolean contains = false;
						try {
							obj = parser.parse(widgetsListJson);
							JSONArray widgets = (JSONArray)obj;
							for (Object wUri: widgets){
								String wdgtUri = (String)wUri;
								if (store().in(widget).uri().toString().equals(wdgtUri)){
									contains = true;
									break;
								}
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
						if (contains){
							Content currentSpace = store().in(device).as(ROLETerms.cs).get();
							if (currentSpace != null){
								if (store().as(currentSpace).string().equals(spaceUri)){
									String widgetId = new Long(Math.abs(widget.getUuid().getMostSignificantBits()
											^ widget.getUuid().getLeastSignificantBits())).toString();
									duiService.onInitMigration(user.getUuid().toString(), device.getId(), targetDeviceName, widgetId, spaceUri);
								}
								else{
									List<String> dwList, dwList1;
									dwList = getDisplayWidgets(dcfg);
									dwList.remove(widgetUri);
									store().in(dcfg).as(ROLETerms.aw).type("application/json").string(JSONValue.toJSONString(dwList));
									for (Concept tarDcfg: dcl){
										if (!store().in(tarDcfg).get(ConserveTerms.reference).get(0).getObject().equals(targetDevice.getUuid()))
											continue;
										log.info(tarDcfg.getUuid().toString());
										dwList1 = getDisplayWidgets(tarDcfg);
										if (!dwList1.contains(widgetUri)){
											dwList1.add(widgetUri);
											Content c = store().in(tarDcfg).as(ROLETerms.aw).get();
											if (c == null)
												store().in(tarDcfg).as(ROLETerms.aw).type("application/json").string(JSONValue.toJSONString(dwList1));
											else{
	//											String s = store().as(c).string();
												store().as(c).string(JSONValue.toJSONString(dwList1));
											}
										}
										break;
									}
									String widgetId = new Long(Math.abs(widget.getUuid().getMostSignificantBits()
											^ widget.getUuid().getLeastSignificantBits())).toString();
									duiService.onSetWidgetLocation(widgetId, user.getUuid().toString(), "", targetDeviceName, false, spaceUri);
								}
							}
							else{
								List<String> dwList, dwList1;
								dwList = getDisplayWidgets(dcfg);
								dwList.remove(widgetUri);
								store().in(dcfg).as(ROLETerms.aw).type("application/json").string(JSONValue.toJSONString(dwList));
								for (Concept tarDcfg: dcl){
									if (!store().in(tarDcfg).get(ConserveTerms.reference).get(0).getObject().equals(targetDevice.getUuid()))
										continue;
									log.info(tarDcfg.getUuid().toString());
									dwList1 = getDisplayWidgets(tarDcfg);
									if (!dwList1.contains(widgetUri)){
										dwList1.add(widgetUri);
										Content c = store().in(tarDcfg).as(ROLETerms.aw).get();
										if (c == null)
											store().in(tarDcfg).as(ROLETerms.aw).type("application/json").string(JSONValue.toJSONString(dwList1));
										else{
	//										String s = store().as(c).string();
											store().as(c).string(JSONValue.toJSONString(dwList1));
										}
									}
									break;
								}
								String widgetId = new Long(Math.abs(widget.getUuid().getMostSignificantBits()
										^ widget.getUuid().getLeastSignificantBits())).toString();
								duiService.onSetWidgetLocation(widgetId, user.getUuid().toString(), "", targetDeviceName, false, spaceUri);
							}
							return Response.ok().header("Cache-Control", "no-store").build();
						}
					}
				}
				for (Concept tarDcfg: dcl){
					if (!store().in(tarDcfg).get(ConserveTerms.reference).get(0).getObject().equals(targetDevice.getUuid()))
						continue;
					log.info(tarDcfg.getUuid().toString());
					List<String> dwList1 = getDisplayWidgets(tarDcfg);
					if (!dwList1.contains(widgetUri)){
						dwList1.add(widgetUri);
						Content c = store().in(tarDcfg).as(ROLETerms.aw).get();
						if (c == null)
							store().in(tarDcfg).as(ROLETerms.aw).type("application/json").string(JSONValue.toJSONString(dwList1));
						else{
	//						String s = store().as(c).string();
							store().as(c).string(JSONValue.toJSONString(dwList1));
						}
					}
					break;
				}
				String widgetId = new Long(Math.abs(widget.getUuid().getMostSignificantBits()
						^ widget.getUuid().getLeastSignificantBits())).toString();
				duiService.onSetWidgetLocation(widgetId, user.getUuid().toString(), "", targetDeviceName, false, spaceUri);
				return Response.ok().header("Cache-Control", "no-store").build();
			}
					
					
					
			//----------------------		
			/*		
			if (devices.size() > 0){
				//no need to migration if the widget is already there: for future multi widget at multi location
				if (devices.contains(targetDeviceName))
					return Response.status(Response.Status.ACCEPTED).build();
				String sourceDeviceName = devices.get(0);
				Concept sourceDevice = store.getConcept(user.getUuid(), ROLETerms.device, sourceDeviceName);
				Content currentSpace = store().in(sourceDevice).as(ROLETerms.cs).get();
				if (currentSpace != null){
					if (store().as(currentSpace).string().equals(spaceUri)){
						String widgetId = new Long(Math.abs(widget.getUuid().getMostSignificantBits()
								^ widget.getUuid().getLeastSignificantBits())).toString();
						duiService.onInitMigration(user.getUuid().toString(), sourceDeviceName, targetDeviceName, widgetId);
					}
					else{
						if (member != null)
							setWidgetLocation(member, widgetUri, sourceDeviceName, targetDevice);
						String widgetId = new Long(Math.abs(widget.getUuid().getMostSignificantBits()
								^ widget.getUuid().getLeastSignificantBits())).toString();
						duiService.onSetWidgetLocation(widgetId, user.getUuid().toString(), "", targetDeviceName, false);
					}
				}
				else{
					if (member != null)
						setWidgetLocation(member, widgetUri, sourceDeviceName, targetDevice);
					String widgetId = new Long(Math.abs(widget.getUuid().getMostSignificantBits()
							^ widget.getUuid().getLeastSignificantBits())).toString();
					duiService.onSetWidgetLocation(widgetId, user.getUuid().toString(), "", targetDeviceName, false);
				}
			}
			else{
				if (member != null)
					setWidgetLocation(member, widgetUri, null, targetDevice);
				String widgetId = new Long(Math.abs(widget.getUuid().getMostSignificantBits()
						^ widget.getUuid().getLeastSignificantBits())).toString();
				duiService.onSetWidgetLocation(widgetId, user.getUuid().toString(), "", targetDeviceName, false);
			}
			return Response.ok().header("Cache-Control", "no-store").build();*/
		}
		
		if (action.equals("changeWidgetLocation")){//change widget location--------#space+user
			String spaceUri = ((RequestImpl)request).getLinkRelations().get("space");
			Concept space = getSpace(spaceUri);
			Concept user = getUser();
			String widgetUri = ((RequestImpl)request).getLinkRelations().get("widgetUri");
			String currentDeviceName = ((RequestImpl)request).getLinkRelations().get("currentDevice");
			String targetDeviceName = ((RequestImpl)request).getLinkRelations().get("targetDevice");
			Concept targetDevice = store().in(user).sub(ROLETerms.device).get(targetDeviceName);
			String isDuiWidget = ((RequestImpl)request).getLinkRelations().get("isDuiWidget");
			boolean isDui = Boolean.valueOf(isDuiWidget);
			Concept widget = store().resolve(widgetUri);
			if (widget == null)
				return Response.status(Response.Status.NOT_FOUND).build();
			Concept member = getMemberForUser(user, space);
			if (member != null)
				setWidgetLocation(member, widgetUri, currentDeviceName, targetDevice);
			String widgetId = new Long(Math.abs(widget.getUuid().getMostSignificantBits()
					^ widget.getUuid().getLeastSignificantBits())).toString();
			duiService.onSetWidgetLocation(widgetId, user.getUuid().toString(), currentDeviceName, targetDeviceName, isDui, spaceUri);
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		
		if (action.equals("saveWidgetState")){//save the widget state --------------#space
//			Concept space = getSpace(((RequestImpl)request).getLinkRelations().get("space"));
			String widgetUri = ((RequestImpl)request).getLinkRelations().get("widgetUri");
			String widgetStateJson = ((RequestImpl)request).getLinkRelations().get("widgetStateJson");
			Concept widget = store().resolve(widgetUri);
			if (widget == null)
				return Response.status(Response.Status.NOT_FOUND).build();
			//simple overwrite
			//TODO distinguish migration state and normal state
			store().in(widget).as(ROLETerms.ws).type("application/json").string(widgetStateJson);
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		if (action.equals("setNewWidgetLoc")){
//			Concept user = getUser();
			Concept space = getSpace(((RequestImpl)request).getLinkRelations().get("space"));
			String widgetUri = ((RequestImpl)request).getLinkRelations().get("widgetUri");
			String activityUri = ((RequestImpl)request).getLinkRelations().get("activity");
			Concept widget = store().resolve(widgetUri);
//			String deviceName = ((RequestImpl)request).getLinkRelations().get("currentDevice");
//			Concept targetDevice = store().in(user).sub(ROLETerms.device).get(deviceName);
//			if (targetDevice != null){
//				Concept member = getMemberForUser(user, space);
//				setWidgetLocation(member, widgetUri, null, targetDevice);
//			}
			String widgetId = new Long(Math.abs(widget.getUuid().getMostSignificantBits()
					^ widget.getUuid().getLeastSignificantBits())).toString();
			duiService.onNewWidget(widgetId, widgetUri, activityUri, space.getId());
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		if (action.equals("widgetRemoved")){
			String widgetId = ((RequestImpl)request).getLinkRelations().get("widgetId");
			String spaceId = getSpace(((RequestImpl)request).getLinkRelations().get("space")).getId();
			duiService.onRemoveWidgetFromSpace(widgetId, spaceId);
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		if (action.equals("setAppState")){
			Concept space = getSpace(((RequestImpl)request).getLinkRelations().get("space"));
			String appStateJson = ((RequestImpl)request).getLinkRelations().get("appStateJson");
			String oldState = store().in(space).as(ROLETerms.as).type("application/json").string();
			if (!oldState.equals(appStateJson)){
				store().in(space).as(ROLETerms.as).type("application/json").string(appStateJson);
				duiService.onAppStateChange(oldState.equals("")?null:oldState, appStateJson, space.getId());
			}
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		if (action.equals("deviceProfile")){
			Concept user = getUser();
			String deviceName = ((RequestImpl)request).getLinkRelations().get("deviceName");
			String deviceInfo = ((RequestImpl)request).getLinkRelations().get("deviceInfo");
			if (deviceName != null && deviceInfo != null){
				Concept device = store().in(user).sub(ROLETerms.device).get(deviceName);
				if (device != null){
					Content dProfile = store().in(device).as(ROLETerms.dp).get();
					if (dProfile == null){
						store().in(device).as(ROLETerms.dp).type("application/json").string(deviceInfo);
					}
					else if (!deviceInfo.equals(store().as(dProfile).string())){
						store().as(dProfile).string(deviceInfo);
					}
					duiService.onSetDeviceProfile(user.getUuid().toString(), deviceName);
					return Response.ok().header("Cache-Control", "no-store").build();
				}
			}
		}
		
		if (action.equals("newLoad")){
			String deviceName = ((RequestImpl)request).getLinkRelations().get("deviceName");
			String spaceUri = ((RequestImpl)request).getLinkRelations().get("space");
			Concept device = store().in(getUser()).sub(ROLETerms.device).get(deviceName);
			if (device == null){
				//detachDeviceCookieFromSession(request, deviceName);
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			//store().in(device).as(ROLETerms.cs).type("text/plain").string(spaceUri);
			duiService.onNewDeviceLoaded(deviceName, getSpace(spaceUri).getId(), getUser().getUuid().toString());
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		if (action.equals("signOut")){
			String deviceName = ((RequestImpl)request).getLinkRelations().get("deviceName");
			Concept device = store().in(getUser()).sub(ROLETerms.device).get(deviceName);
			if (device == null){
				//detachDeviceCookieFromSession(request, deviceName);
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			store().in(device).as(ROLETerms.cs).type("text/plain").string("");
			duiService.onDeviceOff(deviceName, getUser().getUuid().toString());
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		if (action.equals("changeDeviceAlias")){
			String deviceName = ((RequestImpl)request).getLinkRelations().get("deviceName");
			Concept device = store().in(getUser()).sub(ROLETerms.device).get(deviceName);
			if (device == null){
				//detachDeviceCookieFromSession(request, deviceName);
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			String newName = ((RequestImpl)request).getLinkRelations().get("newName");
			store().in(device).as(ConserveTerms.metadata).type("application/json")
			.string("{\"\": { \"http://purl.org/dc/terms/name\": [{ \"value\": \""+ newName +"\", \"type\": \"literal\" }]}}");
			duiService.onChangeDeviceAlias(device.getId(), newName, getUser().getUuid().toString());
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		//the end of PUT
		return Response.status(Response.Status.BAD_REQUEST).build();
	}

	@Override
	public Object doPost(Request request, byte[] data) {
		String action = ((RequestImpl)request).getHttpServletRequest().getHeader("action");
		log.info("POST received.....action is " + action);
		
		if (action.equals("newDevice")){ //new a device------------------#user
			Concept user = getUser();
			String newDeviceName = ((RequestImpl)request).getLinkRelations().get("deviceName");
			String deviceInfo = ((RequestImpl)request).getLinkRelations().get("deviceInfo");
//			if (store().in(user).sub(ROLETerms.device).get(newDeviceName) != null)
//				return Response.status(Response.Status.ACCEPTED).build();
//			else{
				Concept device = store().in(user).sub(ROLETerms.device).acquire(generator.generate());
				store().in(device).as(ROLETerms.cs).type("text/plain").string("");
				store().in(device).as(ConserveTerms.metadata).type("application/json")
				.string("{\"\": { \"http://purl.org/dc/terms/name\": [{ \"value\": \""+ newDeviceName +"\", \"type\": \"literal\" }]}}");
				if (deviceInfo != null)
					store().in(device).as(ROLETerms.dp).type("application/json").string(deviceInfo);
				duiService.onNewDevice(device.getId(), newDeviceName, user.getUuid().toString());
				return Response.status(Response.Status.CREATED).type(MediaType.TEXT_PLAIN_TYPE).entity(device.getId()).build();
//			}
		}
		
		//the end of POST
		return Response.ok().header("Cache-Control", "no-store").build();
	}

	@Override
	public Object doDelete(Request request) {
		String action = ((RequestImpl)request).getHttpServletRequest().getHeader("action");
		log.info("DELETE received.....action is " + action);
		
		if (action.equals("removeDevice")){ //delete a device----------#user
			Concept user = getUser();
			String removeDeviceName = ((RequestImpl)request).getLinkRelations().get("deviceName");
			Concept device = store().in(user).sub(ROLETerms.device).get(removeDeviceName);
			if (device == null)
				return Response.status(Response.Status.NOT_FOUND).build();
			else
				store.deleteConcept(device);
			duiService.onRemoveDevice(user.getUuid().toString(), removeDeviceName);
			return Response.ok().header("Cache-Control", "no-store").build();
		}
		
		//the end of DELETE
		return Response.ok().header("Cache-Control", "no-store").build();
	}
	
	
}
