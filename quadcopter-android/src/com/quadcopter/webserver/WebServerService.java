//   Copyright 2009 Google Inc.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.quadcopter.webserver;

import java.io.IOException;
import java.net.Socket;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.quadcopter.R;
import com.quadcopter.ui.QuadCopterActivity;

public class WebServerService extends Service 
{

	static final String PREFS_NAME = "QuadCopterWebServerPrefs";

	static final String PREFS_ALLOW_UPLOADS = "ALLOW_UPLOADS";
	public static String PREFS_REQUIRE_LOGIN = "REQUIRE_LOGIN";
	public static String PREFS_PASSWORD = "PASSWORD";

	private static final int DEFAULT_PORT = 80;

	private static final String TAG = "WebServerService";

	private WebServerThread mWebServerThread;
	
	private final IWebServerService.Stub mBinder = new IWebServerService.Stub() {
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
			if(mWebServerThread == null){
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
				int port = settings.getInt("port", DEFAULT_PORT);
				try {
					mWebServerThread = new WebServerThread(WebServerService.this, settings,
							new CookiesDatabaseOpenHelper(WebServerService.this).getWritableDatabase(), port);
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
		if (IWebServerService.class.getName().equals(intent.getAction())) {
			return mBinder;
		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
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
