package eu.role_project.service.dui.iface;

public interface DUIService {
	
	/**
	 * Informs all client devices that a new device has been created
	 * @param deviceName the name of the device
	 * @param uid the user id
	 * @return
	 */
	public boolean onNewDevice(String deviceName, String deviceAlias, String uid);
	
	/**
	 * Informs all client devices that a device has been removed
	 * @param userId the user id
	 * @param deviceName the name of the device
	 * @return
	 */
	public boolean onRemoveDevice(String userId, String deviceName);
	
	/**
	 * Informs all client devices that a global connection check is initiated
	 * @param userId the user id of the user whose devices are checked
	 */
	public void onCheckDevices(String userId);
	
	/**
	 * Informs all client devices that a device has finished the connection check and has been marked as 'connected'
	 * @param userId the user id
	 * @param deviceName the device name
	 * @param location the space in which the device is active
	 * @param isAnonymous true if this connection confirmation is from an anonymous device
	 */
	public void onDeviceConfirm(String userId, String deviceName, String location, boolean isAnonymous);
	
	/**
	 * Informs all client devices that a new widget has been created
	 * @param widgetId the widget id
	 * @param widgetUri the widget uri
	 * @param activityUri the uri of the activity to which the widget belongs
	 * @param spaceId the space id
	 */
	public void onNewWidget(String widgetId, String widgetUri, String activityUri, String spaceId);
	
	/**
	 * Informs all client devices that the location of a widget has been modified
	 * @param widgetId the widget id
	 * @param userId the user id
	 * @param sourceDevice the previous device to which the widget belongs
	 * @param targetDevice the target device to which the widget belongs
	 * @param isDuiWidget true if the widget is recognized as DUI supported widget
	 * @param spaceId the space id
	 */
	public void onSetWidgetLocation(String widgetId, String userId, String sourceDevice, String targetDevice, boolean isDuiWidget, String spaceId);
	public void onRemoveWidgetFromSpace(String widgetId, String spaceId);
	
	/**
	 * Informs all client devices that the application state is changed
	 * @param oldStates the string representation of the old application state
	 * @param newStates the string representation of the new application state
	 * @param spaceId the space id
	 */
	public void onAppStateChange(String oldStates, String newStates, String spaceId);
	
	/**
	 * Informs the devices involved in the device switch that the switch is done on the server side and request the client device to perform relative reactions
	 * @param uid the user id
	 * @param fromDeviceName the name of the device which is set as disconnected
	 * @param toDeviceName the name of the device which is set as connected to the space
	 */
	public void onSwitchDevice(String uid, String fromDeviceName,
			String toDeviceName);
	
	/**
	 * Requests the involved devices to initialize the widget migration on the client side
	 * @param uid the user id
	 * @param sourceDevice the source device from which the widget migrates
	 * @param targetDevice the target device to which the widget migrates
	 * @param widgetId the widget id
	 * @param spaceUri the uri of the space
	 */
	public void onInitMigration(String uid, String sourceDevice,
			String targetDevice, String widgetId, String spaceUri);
	
	/**
	 * informs all client devices that a device profile has been stored in certain device
	 * @param uid the user id
	 * @param deviceName the device name
	 */
	public void onSetDeviceProfile(String uid, String deviceName);
	
	/**
	 * Informs all client devices that a device is online
	 * @param deviceName the device name
	 * @param spaceId the space id
	 * @param uid the user id
	 */
	public void onNewDeviceLoaded(String deviceName, String spaceId, String uid);
	
	/**
	 * Informs all client devices that a device is offline
	 * @param deviceName the device name
	 * @param uid the user id
	 */
	public void onDeviceOff(String deviceName, String uid);

	public void onChangeDeviceAlias(String deviceName, String alias, String uid);
}
