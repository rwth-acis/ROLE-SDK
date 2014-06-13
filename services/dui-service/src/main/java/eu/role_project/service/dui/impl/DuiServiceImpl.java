package eu.role_project.service.dui.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import org.json.simple.JSONValue;
import eu.role_project.service.dui.iface.DUIService;
import eu.role_project.service.dui.iface.XmppProxy;
import eu.role_project.service.realtime.iwc.Intent;

public class DuiServiceImpl implements DUIService {
	
	public static final String NAME = "duiservice";
//	private JSONParser parser = new JSONParser();
	
	@Inject
	XmppProxy xmppProxy;

	public boolean onNewDevice(String deviceName, String deviceAlias, String uid) {
		String categories[] = {"DUI"};
		Map<String, Object> map=new LinkedHashMap<String, Object>();
		map.put("user", uid);
		map.put("newDevice", deviceName);
		map.put("deviceAlias", deviceAlias);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_NEW_DEVICE", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, uid);
		return true;
	}

	public boolean onRemoveDevice(String userId, String deviceName) {
		String categories[] = {"DUI"};
		Map<String, String> map=new LinkedHashMap<String, String>();
		map.put("user", userId);
		map.put("device", deviceName);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_REMOVE_DEVICE", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, userId);
		return true;
	}

	public void onRemoveWidgetFromSpace(String widgetId, String spaceId) {
		String categories[] = {"DUI"};
		Map<String, String> map=new LinkedHashMap<String, String>();
		map.put("widgetId", widgetId);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_REMOVE_WIDGET", "", "", categories, null, extras);
		xmppProxy.publishToSpace(intent, spaceId);
	}

	public void onAppStateChange(String oldStates, String newStates, String spaceId) {
		String categories[] = {"DUI"};
		Map<String, String> map=new LinkedHashMap<String, String>();
		if (oldStates != null)
			map.put("oldStates", oldStates);
		map.put("newStates", newStates);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_APP_STATE_CHANGE", "", "", categories, null, extras);
		xmppProxy.publishToSpace(intent, spaceId);
	}

	public void onNewWidget(String widgetId, String widgetUri, String activityUri, String spaceId) {
		String categories[] = {"DUI"};
		Map<String, String> map=new LinkedHashMap<String, String>();
		map.put("widgetId", widgetId);
		map.put("activityUri", activityUri);
		map.put("widgetUri", widgetUri);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_NEW_WIDGET", "", "", categories, null, extras);
		xmppProxy.publishToSpace(intent, spaceId);
	}

	public void onCheckDevices(String userId) {
		String categories[] = {"DUI"};
		Map<String, String> map=new LinkedHashMap<String, String>();
		map.put("user", userId);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_CONN_CHK", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, userId);
	}

	public void onDeviceConfirm(String userId, String deviceName, String location,
			boolean isAnonymous) {
		String categories[] = {"DUI"};
		Map<String, Object> map=new LinkedHashMap<String, Object>();
		map.put("user", userId);
		map.put("device", deviceName);
		map.put("location", location);
		map.put("isAnonymous", isAnonymous);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_CONN_CONFIRM", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, userId);
	}

	public void onSwitchDevice(String uid, String fromDeviceName,
			String toDeviceName) {
		String categories[] = {"DUI"};
		Map<String, Object> map=new LinkedHashMap<String, Object>();
		map.put("user", uid);
		map.put("from", fromDeviceName);
		map.put("to", toDeviceName);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_SWITCH_DEVICE", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, uid);
	}

	public void onSetWidgetLocation(String widgetId, String userId, String sourceDevice,
			String targetDevice, boolean isDuiWidget, String spaceUri) {
		String categories[] = {"DUI"};
		Map<String, Object> map=new LinkedHashMap<String, Object>();
		map.put("user", userId);
		map.put("source", sourceDevice);
		map.put("target", targetDevice);
		map.put("widgetId", widgetId);
		map.put("isDui", isDuiWidget);
		map.put("spaceUri", spaceUri);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_PERFORM_MIG", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, userId);
	}

	public void onInitMigration(String uid, String sourceDevice,
			String targetDevice, String widgetId, String spaceUri) {
		String categories[] = {"DUI"};
		Map<String, Object> map=new LinkedHashMap<String, Object>();
		map.put("user", uid);
		map.put("source", sourceDevice);
		map.put("target", targetDevice);
		map.put("widgetId", widgetId);
		map.put("spaceUri", spaceUri);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_INIT_MIG", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, uid);
	}

	public void onSetDeviceProfile(String uid, String deviceName) {
		String categories[] = {"DUI"};
		Map<String, Object> map=new LinkedHashMap<String, Object>();
		map.put("user", uid);
		map.put("deviceName", deviceName);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_NEW_PROFILE", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, uid);
	}

	public void onNewDeviceLoaded(String deviceName, String spaceId, String uid) {
		String categories[] = {"DUI"};
		Map<String, Object> map=new LinkedHashMap<String, Object>();
		map.put("user", uid);
		map.put("device", deviceName);
		map.put("space", spaceId);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_DVC_LOAD", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, uid);
		
	}

	public void onDeviceOff(String deviceName, String uid) {
		String categories[] = {"DUI"};
		Map<String, Object> map=new LinkedHashMap<String, Object>();
		map.put("user", uid);
		map.put("device", deviceName);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_NOT_CONN", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, uid);
	}

	public void onChangeDeviceAlias(String deviceName, String alias,
			String uid) {
		// TODO Auto-generated method stub
		String categories[] = {"DUI"};
		Map<String, Object> map=new LinkedHashMap<String, Object>();
		map.put("user", uid);
		map.put("device", deviceName);
		map.put("alias", alias);
		String extras = JSONValue.toJSONString(map);
		Intent intent = new Intent("", NAME, "DUI_CHANGE_ALIAS", "", "", categories, null, extras);
		xmppProxy.publishToUser(intent, uid);
	}

}
