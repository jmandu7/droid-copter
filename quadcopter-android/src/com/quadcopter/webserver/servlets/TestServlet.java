package com.quadcopter.webserver.servlets;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public class TestServlet extends Servlet 
{
	@Override
	public void runServlet(HttpRequest request, HttpResponse response) 
	{
		String body = "This is a test servlet - uri = \"" + request.getRequestLine().getUri() + "\"";
		
		setHTTPSuccessResponse(response, body);
	}
}
