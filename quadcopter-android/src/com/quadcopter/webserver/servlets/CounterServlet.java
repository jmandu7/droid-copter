package com.quadcopter.webserver.servlets;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import android.content.Context;

public class CounterServlet extends Servlet{
	int count = 0;
	
	@Override
	public void setupServlet(Context context) {
		super.setupServlet(context);
		count = 10;
	}

	@Override
	public void runServlet(HttpRequest request, HttpResponse response) 
	{
		count++;
		setHTTPSuccessResponse(response, "" + count);
	}

}
