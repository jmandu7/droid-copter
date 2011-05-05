package com.quadcopter.webserver.servlets;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import android.location.Location;

import com.quadcopter.background.hardware.GPSRefresher;

public class GPSDataServlet extends Servlet{

	@Override
	public void runServlet(HttpRequest request, HttpResponse response) {
		Location loc = GPSRefresher.getCurrentLocation();
		response.addHeader("Content-Type", "application/json");
		if (loc!=null)
			setHTTPSuccessResponse(response, "{\"lat\":" + loc.getLatitude() + ", \"lng\":" + loc.getLongitude() + "}");
		else 
			setHTTPSuccessResponse(response, "{\"lat\":" + 0 + ", \"lng\":" + 0 + "}");
	}
}
