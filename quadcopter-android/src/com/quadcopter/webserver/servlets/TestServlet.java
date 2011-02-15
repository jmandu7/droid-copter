package com.quadcopter.webserver.servlets;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultHttpServerConnection;

public class TestServlet extends Servlet 
{
	@Override
	public HttpResponse runServlet(DefaultHttpServerConnection serverConnection, HttpRequest request) {
		String body = "This is a test servlet - uri = \"" + request.getRequestLine().getUri() + "\"";
		
		return getHTTPSuccessResponse(body);
	}
}
