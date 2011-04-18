package com.quadcopter.webserver.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;

import android.content.Context;

public abstract class Servlet 
{
	//This is called when someone goes to the servlet page
	public abstract void runServlet(HttpRequest request, HttpResponse response);
	
	//This function is the first time this class is loaded
	public void setupServlet(Context context){}
	
	//this can be used to build a response based on a string
	public static void setHTTPSuccessResponse(HttpResponse response, String body)
	{
		response.setStatusLine(new HttpVersion(1, 1), 200,"OK");
		//response.addHeader("Content-Type", "text/html");
		final String strBody = "".concat(body);
		if (body!=null)
		{
			EntityTemplate entity = new EntityTemplate(new ContentProducer() {
		        public void writeTo(final OutputStream outstream) throws IOException {
		            OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8"); 
		            writer.write(strBody);
		            writer.flush();
		        }
		        
		    });
			response.setEntity(entity);
		}
	}
	
	//this can be used to build a response based on a string
	public static void setHTTPFailureResponse(HttpResponse response, String body)
	{
		response.setStatusLine(new HttpVersion(1, 1), 500,"OK");
		try {
			if (body!=null)
				response.setEntity(new StringEntity(body));
		} catch (UnsupportedEncodingException e) {}
	}
	
	public static String getHTTPPostBody(HttpRequest request)
	{
		StringBuffer form = new StringBuffer();
		try {
			HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
			InputStream s;
			s = entity.getContent();		
			int b;
			while ((b=s.read())!=-1)
				form.append((char)b);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return form.toString();
	}
}
