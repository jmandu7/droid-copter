package com.quadcopter.webserver.servlets;

import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;

public class TestServlet extends Servlet 
{
	@Override
	public HttpResponse runServlet(RequestLine requestLine) {
		String body = "This is a test servlet - uri = \"" + requestLine.getUri() + "\"";
		
		return getHTTPSuccessResponse(body);
	}
}
