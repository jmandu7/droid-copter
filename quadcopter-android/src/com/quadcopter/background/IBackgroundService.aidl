//This interface is used for all communication between
//the service and the activity that started the service

package com.quadcopter.background;

interface IBackgroundService {
/* General BackgroundService functions*/
	//start and stop stuff from happening
	void start();
	void stop();

/* WebService functions*/
	//returns the the port number 
	//that the server is running on
 	int getPort();

	//Send the byte array to the server. This image is
	//in the jpeg format and can be viewed when a user
	//goes to http://ip.address/webcam.jpg
	void setWebCamByteArray(in byte[] img);
	
	//is the server running
	boolean isRunning();
}