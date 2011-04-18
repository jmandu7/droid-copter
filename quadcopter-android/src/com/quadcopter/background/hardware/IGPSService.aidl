//This interface is used for all communication between
//the service and the activity that started the service

package com.quadcopter.background.hardware;

interface IGPSService {
/* GPS Communication*/
	//this function starts updating gps
	void startGPSRefresh();
	
	//this function stops refreshing gps
	void stopGPSRefresh();
	
	//get the most recently updated location
	//if startGPSRefresh() has never been
	//called then we will return a null
	//it may take a moment after calling 
	//startGPSRefresh() before it will
	//stop returning null 
	Location getGPSLocation();
}