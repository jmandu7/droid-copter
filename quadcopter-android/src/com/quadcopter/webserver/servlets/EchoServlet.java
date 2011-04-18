package com.quadcopter.webserver.servlets;

import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public class EchoServlet extends Servlet{

	@Override
	public void runServlet(HttpRequest request, HttpResponse response) 
	{	
		Header[] headers = request.getAllHeaders();
		String ret = request.getRequestLine().getUri() + "\n";
		for (int i=0;i<headers.length;i++)
		{	
			ret=headers[i].getName() + "=" + headers[i].getValue() + "\n";
		}	
		
		HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
		InputStream s;
		StringBuffer form = new StringBuffer();
		try {
			s = entity.getContent();		
			int b;
			while ((b=s.read())!=-1)
				form.append((char)b);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setHTTPSuccessResponse(response, ret + form.toString());
	}

}
