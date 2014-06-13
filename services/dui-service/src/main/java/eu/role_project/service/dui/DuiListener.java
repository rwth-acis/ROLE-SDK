package eu.role_project.service.dui;




import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.role_project.service.dui.iface.DUIService;
import eu.role_project.service.resource.ROLETerms;

import se.kth.csc.kmr.conserve.Concept;
import se.kth.csc.kmr.conserve.Content;
import se.kth.csc.kmr.conserve.Control;
import se.kth.csc.kmr.conserve.Request;
import se.kth.csc.kmr.conserve.core.AbstractListener;
import se.kth.csc.kmr.conserve.core.ConserveTerms;
import se.kth.csc.kmr.conserve.iface.jaxrs.RequestImpl;
import se.kth.csc.kmr.conserve.security.SecurityInfo;

public class DuiListener extends AbstractListener {
	private static Logger log = LoggerFactory.getLogger(DuiListener.class);
	
	@Inject
	DUIService duiService;
	
	@Inject
	private SecurityInfo securityInfo;
	
	private JSONParser parser = new JSONParser();
	
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
	 * Get the member concept of the user in a space
	 * @param user the user
	 * @param space the space
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
	 * Get the device configuration which contains the widget list for the device
	 * @param member the member concept decided by the user and the space
	 * @param deviceName the name of the device owned by the user
	 * @return the device configuration concept
	 */
	private Concept getDeviceConfig(Concept member, String deviceName){
		return store().in(member).sub(ROLETerms.dc).get(deviceName);
	}
	
	/**
	 * Get the widget list which contains the widget that is displayed at the client browser
	 * @param deviceConfig the device configuration concept for the device
	 * @return the list of widget Uris (the widget uri identifies the widget in the server)
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
	 * Called when a POST request incoming for ROLETerms.tool. The widget is a ROLETerms.tool
	 */
	@Override
	public Object doPost(Request request, byte[] data) {
		Concept tool = request.getCreated();
		if (ROLETerms.tool.equals(tool.getPredicate())){
			boolean isOpenSocialGadget = null != store.getControls(
					tool.getUuid(), ConserveTerms.type,
					ROLETerms.OpenSocialGadget);
			if (isOpenSocialGadget){
				log.info("An Opensocial gadget is posted");
				Concept space = store().get(tool.getContext());
				HttpHeaders httpHeaders = ((RequestImpl) request).getRequestHeaders();
				Map<String, Cookie> cookies = httpHeaders.getCookies();
				if (cookies.containsKey("device")) {
					Cookie deviceCookie = cookies.get("device");
					String deviceName = deviceCookie.getValue();
					Concept user = getUser();
					Concept member = getMemberForUser(user, space);
					Concept deviceConfig = getDeviceConfig(member, deviceName);
					List<String> dwList = getDisplayWidgets(deviceConfig);
					String widgetUri = store().in(tool).uri().toString();
					dwList.add(widgetUri);
					String dwStr = JSONValue.toJSONString(dwList);
					log.info(dwStr);
					store().in(deviceConfig).as(ROLETerms.aw).type("application/json").string(dwStr);
				}
			}
		}
		return null;
	}

}
