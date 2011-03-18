package com.quadcopter.webserver.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.quadcopter.background.hardware.BluetoothCommunication;

public class ControlReceiverServlet extends Servlet
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
			JSONObject data = new JSONObject(form.toString());
			
			if (data!=null)
			{
				BluetoothCommunication.sendMessageToDevice(mContext, 'A', 
						data.getInt("Roll")+data.getInt("Pitch")+","+","+data.getInt("Yaw"));
				
				return getHTTPSuccessResponse("Success!");
			} else
			{
				return getHTTPSuccessResponse("Failure!");
			}
			
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getHTTPSuccessResponse("Failure!");
	}

}
