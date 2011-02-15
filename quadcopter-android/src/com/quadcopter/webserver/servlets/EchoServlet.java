package com.quadcopter.webserver.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

public class EchoServlet extends Servlet{

	@Override
	public HttpResponse runServlet(DefaultHttpServerConnection serverConnection, HttpRequest request) 
	{	
		Header[] headers = request.getAllHeaders();
		String ret = request.getRequestLine().getUri() + "\n";
		for (int i=0;i<headers.length;i++)
		{	
			ret=headers[i].getName() + "=" + headers[i].getValue() + "\n";
		}	
		
		BasicHttpEntityEnclosingRequest enclosingRequest = new BasicHttpEntityEnclosingRequest(
				request.getRequestLine());
		StringBuffer form = new StringBuffer();
		try {
			serverConnection.receiveRequestEntity(enclosingRequest);
			InputStream input = enclosingRequest.getEntity().getContent();
			InputStreamReader reader = new InputStreamReader(input);
			
			while (reader.ready()) {
				form.append((char) reader.read());
			}
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return getHTTPSuccessResponse(ret + form.toString());
	}

}
