package com.quadcopter.background.hardware;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

public class GPSRefresher 
{
	private static final String TAG = "GPSRefresher"; 
	
	private Context mContext = null;
	
	private LocationManager locationManager = null;
	
	//one min
	private static final int MIN_UPDATE_TIME = 1000 * 60 * 1;
	
	private HandlerThread refreshThread = null;
	
	private static Location currentLocation = null;
	
	// Define a listener that responds to location updates
	LocationListener locationListener = new LocationListener() 
	{	
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.i(TAG, "GPS status changed");
		}
		@Override
		public void onProviderEnabled(String provider) {
			Log.i(TAG, "GPS Provide enabled");
		}
		@Override
		public void onProviderDisabled(String provider) {
			Log.i(TAG, "GPS provider disabled");
		}
		@Override
		public void onLocationChanged(Location location) {
			Log.i(TAG, "GPS location changed");
		   // Called when a new location is found by the network location provider.
			if (location!=null)
				setCurrentLocation(location);
		}
	};
	
	public GPSRefresher(Context context)
	{
		this.mContext = context;
	}
	
	public void start()
	{
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		
		if (locationManager!=null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			locationManager.removeUpdates(locationListener);
		}
		setCurrentLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

//		refreshThread = new HandlerThread("GPSRefresher")
//		{
//			// Or use LocationManager.GPS_PROVIDER
//			@Override
//			public void run() {
//				GPSRefresher.this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_TIME, 0, GPSRefresher.this.locationListener);
//			}	
//		};
		 refreshThread = new HandlerThread("GPS Thread");
         refreshThread.start();
         new Handler(refreshThread.getLooper()).post(
             new Runnable() {
				 @Override
				 public void run() {
					 GPSRefresher.this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GPSRefresher.this.locationListener);
				 }
             }
         );
	}
	
	public void stop()
	{
		// Remove the listener you previously added
		if (locationManager!=null)
			locationManager.removeUpdates(locationListener);
		refreshThread.getLooper().quit();
	}

	public static synchronized void setCurrentLocation(Location currentLocation) {
		if (currentLocation!=null)
		{
			Log.i(TAG, "Latitude=" + currentLocation.getLatitude()
								+ ", Longitude=" + currentLocation.getLongitude());
			GPSRefresher.currentLocation = currentLocation;
		}
	}

	public static synchronized Location getCurrentLocation() {
		return currentLocation;
	}
}
