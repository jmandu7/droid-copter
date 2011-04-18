package com.quadcopter.background;

import java.io.IOException;
import java.net.Socket;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.quadcopter.R;
import com.quadcopter.background.hardware.BluetoothCommunication;
import com.quadcopter.background.webserver.CookiesDatabaseOpenHelper;
import com.quadcopter.background.webserver.WebServerThread;
import com.quadcopter.ui.QuadCopterActivity;

public class BackgroundService extends Service 
{

	static final String PREFS_NAME = "QuadCopterWebServerPrefs";

	static final String PREFS_ALLOW_UPLOADS = "ALLOW_UPLOADS";
	public static String PREFS_REQUIRE_LOGIN = "REQUIRE_LOGIN";
	public static String PREFS_PASSWORD = "PASSWORD";

	private static final int DEFAULT_PORT = 8080;
	
	private static final String BLUETOOTH_DEVICE_ADDRESS = "00:06:66:42:22:32";

	private static final String TAG = "BackgroundService";

	private WebServerThread mWebServerThread = null;
	
	private BluetoothCommunication bluetoothComm = null;
	
	private final IBackgroundService.Stub mBinder = new IBackgroundService.Stub() {
		@Override
		public int getPort() {
			int port = 0;
			if(mWebServerThread != null){
				port = mWebServerThread.getPort();
			}
			return port;
		}

		@Override
		public void setWebCamByteArray(byte[] img) throws RemoteException {
			if(mWebServerThread != null){
				mWebServerThread.setWebCamImg(img);
			}
		}

		@Override
		public void start() throws RemoteException {
			if (bluetoothComm!=null)
				bluetoothComm.start();
			
			if(mWebServerThread == null){
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
				int port = settings.getInt("port", DEFAULT_PORT);
				try {
					//TODO add cookies back in
//					mWebServerThread = new WebServerThread(BackgroundService.this, settings,
//							new CookiesDatabaseOpenHelper(BackgroundService.this).getWritableDatabase(), port);
					mWebServerThread = new WebServerThread(BackgroundService.this, settings,
							null, port);
					//This command ensures that this thread will be closed 
					//if the the service using this thread crashes.
					mWebServerThread.setDaemon(true);
					mWebServerThread.start();
				} catch (IOException e) {
					Log.e(TAG, "Problem creating server socket " + e.toString());
				}
				showNotification();
			}
		}

		@Override
		public void stop() throws RemoteException {
			if (bluetoothComm!=null)
				bluetoothComm.stop();
			
			if(mWebServerThread != null){
				int port = getPort();
				Thread tmp = mWebServerThread;
				mWebServerThread = null;
				tmp.interrupt();
				
				//The purpose of this command is to close the socket that is
				//in the server thread. The socket stays open until a connection
				//is made to it. So . . . this simply make a connection and then
				//the socket (and hence the thread) can close
				try {
					Socket stopServerSock = new Socket((String) null, port);
					stopServerSock.close();
				} catch (Exception e) {
					Log.e(TAG, "Problem connection to socket: " + e.toString());
				}
				hideNotification();
			}
		}

		@Override
		public boolean isRunning() throws RemoteException {
			return mWebServerThread!=null;
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		if (IBackgroundService.class.getName().equals(intent.getAction())) {
			return mBinder;
		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		bluetoothComm = new BluetoothCommunication(this, BluetoothAdapter.getDefaultAdapter()
												,BLUETOOTH_DEVICE_ADDRESS);	
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (bluetoothComm!=null)
			bluetoothComm.stop();
		
		stopForeground(true);
	}
	
	//This function has the dual effect of showing a notification that says the WebService
	//is running and it keeps the service from being closed.
	private void showNotification()
	{
		 // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon,
        											 "QuadCopter - Web Server Running", 
        											 System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                					      new Intent(this, QuadCopterActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, 
        								"QuadCopter - Web Server",
        								"The web server is running", 
        								contentIntent);
        
		startForeground(R.string.app_name, notification);
	}
	
	//hide the notifcation
	private void hideNotification()
	{
		stopForeground(true);
	}
}
