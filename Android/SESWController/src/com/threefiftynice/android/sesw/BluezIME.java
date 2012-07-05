package com.threefiftynice.android.sesw;

public interface BluezIME {
    
	//These constants are copied from the BluezService
	String SESSION_ID = "com.hexad.bluezime.sessionid";
	
	String EVENT_KEYPRESS = "com.hexad.bluezime.keypress";
	String EVENT_KEYPRESS_KEY = "key";
	String EVENT_KEYPRESS_ACTION = "action";

	String EVENT_DIRECTIONALCHANGE = "com.hexad.bluezime.directionalchange";
	String EVENT_DIRECTIONALCHANGE_DIRECTION = "direction";
	String EVENT_DIRECTIONALCHANGE_VALUE = "value";

	String EVENT_CONNECTED = "com.hexad.bluezime.connected";
	String EVENT_CONNECTED_ADDRESS = "address";

	String EVENT_DISCONNECTED = "com.hexad.bluezime.disconnected";
	String EVENT_DISCONNECTED_ADDRESS = "address";

	String EVENT_ERROR = "com.hexad.bluezime.error";
	String EVENT_ERROR_SHORT = "message";
	String EVENT_ERROR_FULL = "stacktrace";
	
	String REQUEST_STATE = "com.hexad.bluezime.getstate";

	String REQUEST_CONNECT = "com.hexad.bluezime.connect";
	String REQUEST_CONNECT_ADDRESS = "address";
	String REQUEST_CONNECT_DRIVER = "driver";
	
	String REQUEST_DISCONNECT = "com.hexad.bluezime.disconnect";
	
	String EVENT_REPORTSTATE = "com.hexad.bluezime.currentstate";
	String EVENT_REPORTSTATE_CONNECTED = "connected";
	String EVENT_REPORTSTATE_DEVICENAME = "devicename";
	String EVENT_REPORTSTATE_DISPLAYNAME = "displayname";
	String EVENT_REPORTSTATE_DRIVERNAME = "drivername";
	
	String REQUEST_FEATURECHANGE = "com.hexad.bluezime.featurechange";
	String REQUEST_FEATURECHANGE_RUMBLE = "rumble"; //Boolean, true=on, false=off
	String REQUEST_FEATURECHANGE_LEDID = "ledid"; //Integer, LED to use 1-4 for Wiimote
	String REQUEST_FEATURECHANGE_ACCELEROMETER = "accelerometer"; //Boolean, true=on, false=off
	
	String REQUEST_CONFIG = "com.hexad.bluezime.getconfig";
	
	String EVENT_REPORT_CONFIG = "com.hexad.bluezime.config";
	String EVENT_REPORT_CONFIG_VERSION = "version";
	String EVENT_REPORT_CONFIG_DRIVER_NAMES = "drivernames";
	String EVENT_REPORT_CONFIG_DRIVER_DISPLAYNAMES = "driverdisplaynames";
	
	String BLUEZ_IME_PACKAGE = "com.hexad.bluezime";
	String BLUEZ_IME_SERVICE = "com.hexad.bluezime.BluezService";
	
	//These are from API level 9
	int KEYCODE_BUTTON_A = 0x60;
	int KEYCODE_BUTTON_B = 0x61;
	int KEYCODE_BUTTON_C = 0x62;
	int KEYCODE_BUTTON_X = 0x63;
	int KEYCODE_BUTTON_Y = 0x64;
	int KEYCODE_BUTTON_Z = 0x65;

	String DRIVER_ZEEMOTE_JS1 = "zeemote"; 
	String DRIVER_BGP100_CHAINPUS = "bgp100";
	String DRIVER_PHONEJOY = "phonejoy";
	String DRIVER_ICONTROL_PAD = "icontrolpad";
	String DRIVER_WIITMOTE = "wiimote";
	String DRIVER_DATA_DUMP = "dump";

}
