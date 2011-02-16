package com.quadcopter.webserver.servlets;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.message.BasicHttpResponse;

import android.content.Context;

public abstract class Servlet 
{
	//This is called when someone goes to the servlet page
	public abstract HttpResponse runServlet(DefaultHttpServerConnection serverConnection, HttpRequest request);
	
	//This function is the first time this class is loaded
	public void setupServlet(Context context){}
	
	//this can be used to build a response based on a string
	public static HttpResponse getHTTPSuccessResponse(String body)
	{
		HttpResponse response = new BasicHttpResponse(new HttpVersion(1, 1), 200,"OK");
		try {
			response.setEntity(new StringEntity(body));
		} catch (UnsupportedEncodingException e) {
			//This shouldn't ever happen for our code.
			//Assume that the string was bad in some 
			//way and return a null response
			response = null;
		}
		return response;
	}
}
