package com.quadcopter.webserver.servlets;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultHttpServerConnection;

import android.content.Context;

import com.quadcopter.background.hardware.BluetoothCommunication;

public class SendMessageServlet extends Servlet
{

	Context mContext = null;
	@Override
	public void setupServlet(Context context) {
		super.setupServlet(context);
		mContext = context;
	}

	@Override
	public HttpResponse runServlet(DefaultHttpServerConnection serverConnection, HttpRequest request) 
	{
		String msg = request.getRequestLine().getUri();
		msg = msg.contains("?")?msg.split("\\?")[1]:null;
		
		if (msg!=null)
		{
			BluetoothCommunication.sendMessageToDevice(mContext, msg.charAt(0), msg);
			return getHTTPSuccessResponse("Success!");
		} else
		{
			return getHTTPSuccessResponse("Failure!");
		}
	}

}
