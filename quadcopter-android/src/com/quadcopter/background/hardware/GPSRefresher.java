package com.quadcopter.background.hardware;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSRefresher 
{
	private Context mContext = null;
	
	private LocationManager locationManager = null;
	
	//one min
	private static final int MIN_UPDATE_TIME = 1000 * 60 * 1;
	
	private Location currentLocation = null;
	
	// Define a listener that responds to location updates
	LocationListener locationListener = new LocationListener() 
	{	
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		@Override
		public void onProviderEnabled(String provider) {}
		@Override
		public void onProviderDisabled(String provider) {}
		@Override
		public void onLocationChanged(Location location) {
		   // Called when a new location is found by the network location provider.
			setCurrentLocation(location);
		}
	};
	
	public GPSRefresher(Context context)
	{
		this.mContext = context;
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void start()
	{
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			locationManager.removeUpdates(locationListener);
		}
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_TIME, 0, locationListener);
	}
	
	public void stop()
	{
		// Remove the listener you previously added
		locationManager.removeUpdates(locationListener);
	}

	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}
}
