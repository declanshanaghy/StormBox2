package com.threefiftynice.android.sesw;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class SESWController extends Activity implements BluezIME {
	private static final String TAG = "*************************************************" + SESWController.class.getSimpleName();
	private static final boolean D = true;
	
	//A string used to ensure that apps do not interfere with each other
	private static final String SESSION_NAME = "SESWController";
	private static final String MAC = "00:1C:4D:04:60:19";

	private boolean bConnected;
	private JoystickView vJoystick;
	private JoystickController joystickController;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

	private static final float MOTOR_UI_RESOLUTION = 1.0f;
	private static final long JOYSTICK_UI_DELAY = 0;

    private final Handler mHandler = new Handler() {
		@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_WRITE:
            	if (D) {
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG, "TX: \"" + writeMessage + "\"");
            	}
                break;
            case MESSAGE_READ:
            	if (D) {
                	try {
                		byte[] readBuf = (byte[]) msg.obj;
        				String readMessage = new String(readBuf, 0, msg.arg1);
                        Log.d(TAG, "RX: \"" + readMessage + "\"");
                	}
                	catch ( Exception ex) {
                		Log.e(TAG, ex.getMessage(), ex);
                	}
            	}
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controller);
		
        registerReceiver(stateCallback, new IntentFilter(EVENT_REPORT_CONFIG));
        registerReceiver(stateCallback, new IntentFilter(EVENT_REPORTSTATE));
        registerReceiver(stateCallback, new IntentFilter(EVENT_CONNECTED));
        registerReceiver(stateCallback, new IntentFilter(EVENT_DISCONNECTED));
        registerReceiver(stateCallback, new IntentFilter(EVENT_ERROR));
        
        registerReceiver(statusMonitor, new IntentFilter(EVENT_DIRECTIONALCHANGE));
        registerReceiver(statusMonitor, new IntentFilter(EVENT_KEYPRESS));
		
        joystickController = new JoystickController();
		vJoystick = (JoystickView)findViewById(R.id.vJoystick);
		vJoystick.setMovementConstraint(JoystickView.CONSTRAIN_CIRCLE);
		vJoystick.setOnJostickMovedListener(joystickController);
		vJoystick.setYAxisInverted(false);
		vJoystick.setAutoReturnToCenter(true);
		vJoystick.setMovementRange(joystickController.getMovementRange());
		vJoystick.setMoveResolution(MOTOR_UI_RESOLUTION);
		vJoystick.setUserCoordinateSystem(JoystickView.COORDINATE_CARTESIAN);

        if ( !bConnected )
        	connect();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//disconnect();
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	unregisterReceiver(stateCallback);
    	unregisterReceiver(statusMonitor);
    	
    	if ( bConnected )
    		disconnect();
    }
    
	private void connect() {
        Intent serviceIntent = new Intent(REQUEST_CONNECT);
        serviceIntent.setClassName(BLUEZ_IME_PACKAGE, BLUEZ_IME_SERVICE);
        serviceIntent.putExtra(SESSION_ID, SESSION_NAME);
        serviceIntent.putExtra(REQUEST_CONNECT_ADDRESS, MAC);
        serviceIntent.putExtra(REQUEST_CONNECT_DRIVER, DRIVER_ZEEMOTE_JS1);
        startService(serviceIntent);
	}
	
	private void disconnect() {
		Intent serviceIntent = new Intent(REQUEST_DISCONNECT);
	    serviceIntent.setClassName(BLUEZ_IME_PACKAGE, BLUEZ_IME_SERVICE);
	    serviceIntent.putExtra(SESSION_ID, SESSION_NAME);
	    startService(serviceIntent);
	}
	
	private void reportUnmatched(String entry) {
		Toast.makeText(SESWController.this, entry, Toast.LENGTH_SHORT).show();
		Log.e(TAG, entry);
	}

    private BroadcastReceiver stateCallback = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == null)
				return;
			
			//Filter everything that is not related to this session
			if (!SESSION_NAME.equals(intent.getStringExtra(SESSION_ID)))
				return;
			
			if (intent.getAction().equals(EVENT_REPORT_CONFIG)) {
				Toast.makeText(SESWController.this, "Bluez-IME version " + intent.getIntExtra(EVENT_REPORT_CONFIG_VERSION, 0), Toast.LENGTH_SHORT).show();				
			} 
			else if (intent.getAction().equals(EVENT_REPORTSTATE)) {
				bConnected = intent.getBooleanExtra(EVENT_REPORTSTATE_CONNECTED, false);
				Toast.makeText(SESWController.this, "Connected=" + bConnected, Toast.LENGTH_SHORT).show();
				
				//After we connect, we rumble the device for a second if it is supported
				if (bConnected) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							Intent req = new Intent(REQUEST_FEATURECHANGE);
							req.putExtra(REQUEST_FEATURECHANGE_LEDID, 2);
							req.putExtra(REQUEST_FEATURECHANGE_RUMBLE, true);
							startService(req);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
							req.putExtra(REQUEST_FEATURECHANGE_LEDID, 1);
							req.putExtra(REQUEST_FEATURECHANGE_RUMBLE, false);
							startService(req);
						}
					});
				}
			} 
			else if (intent.getAction().equals(EVENT_CONNECTED)) {			
				bConnected = true;
				Toast.makeText(SESWController.this, "Connected=" + bConnected, Toast.LENGTH_SHORT).show();
			} 
			else if (intent.getAction().equals(EVENT_DISCONNECTED)) {
				bConnected = false;
				Toast.makeText(SESWController.this, "Connected=" + bConnected, Toast.LENGTH_SHORT).show();
			} 
			else if (intent.getAction().equals(EVENT_ERROR)) {
				Toast.makeText(SESWController.this, "Error: " + intent.getStringExtra(EVENT_ERROR_SHORT), Toast.LENGTH_SHORT).show();
				reportUnmatched("Error: " + intent.getStringExtra(EVENT_ERROR_FULL));
				bConnected = false;
			}
		}
	};
	
	private BroadcastReceiver statusMonitor = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == null)
				return;
			if (!SESSION_NAME.equals(intent.getStringExtra(SESSION_ID)))
				return;

			if (intent.getAction().equals(EVENT_DIRECTIONALCHANGE)) {
				int value = intent.getIntExtra(EVENT_DIRECTIONALCHANGE_VALUE, 0);
				int direction = intent.getIntExtra(EVENT_DIRECTIONALCHANGE_DIRECTION, 100);
				Log.d(TAG, "direction=" + direction + ", value=" + value);				
			} 
			else if (intent.getAction().equals(EVENT_KEYPRESS)) {
				int key = intent.getIntExtra(EVENT_KEYPRESS_KEY, 0);
				int action = intent.getIntExtra(EVENT_KEYPRESS_ACTION, 100);
				Log.d(TAG, "key=" + key + ", action=" + action);				
			}
		}
	};
	
    private class JoystickController implements JoystickMovedListener, Runnable {
		private float movementRange = 255f;
		private int left;
		private int right;
		
		public float getMovementRange() {
			return movementRange;
		}

		@Override
		public void OnMoved(int left, int right) {
			this.left = left;
			this.right = right;
			mHandler.removeCallbacks(this);
			mHandler.postDelayed(this, JOYSTICK_UI_DELAY);
		}
		
		public void run() {
			Log.d(TAG, String.format("drive(%d,%d)", left, right));
		}

		@Override
		public void OnReleased() {
			mHandler.removeCallbacks(this);
		}
		
		@Override
		public void OnReturnedToCenter() {
			mHandler.removeCallbacks(this);
		}
    }
}
