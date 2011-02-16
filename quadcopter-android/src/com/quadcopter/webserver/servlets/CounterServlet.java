package com.quadcopter.webserver.servlets;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultHttpServerConnection;

public class CounterServlet extends Servlet{
	int count = 0;
	
	@Override
	public void setupServlet() {
		count = 10;
	}

	@Override
	public HttpResponse runServlet(DefaultHttpServerConnection serverConnection, HttpRequest request) 
	{
		count++;
		return getHTTPSuccessResponse("" + count);
	}

}
