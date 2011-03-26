package com.quadcopter.webserver.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;

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
		BasicHttpEntityEnclosingRequest enclosingRequest = new BasicHttpEntityEnclosingRequest(request.getRequestLine());
		StringBuffer form = new StringBuffer();
		String failVal = "";
		String dataDecoded = null;
		String formData = null;
		try {
			serverConnection.receiveRequestEntity(enclosingRequest);
			InputStream input = enclosingRequest.getEntity().getContent();
			InputStreamReader reader = new InputStreamReader(input);
			
			while (reader.ready()) {
				form.append((char) reader.read());
			}
			
			formData = form.toString();
			if ("".equals(formData))
				System.out.println("test error");
			dataDecoded = URLDecoder.decode(formData);
			//data.substring(data.indexOf("data="));dataDecoded
			JSONObject data = new JSONObject(dataDecoded);
			
			if (data!=null&&handleRequest(data))
			{
				return getHTTPSuccessResponse("Success!");
			} else
			{
				return getHTTPFailureResponse("form data was null or there was an error handling request");
			}
			
		} catch (HttpException e) {
			failVal = e.toString();
		} catch (IOException e) {
			failVal = e.toString();
		} catch (JSONException e) {
			failVal = e.toString();
		}
		failVal += " formData=\"" + (formData!=null?formData:"null") + "\"";
		failVal += " dataDecoded=\"" + (dataDecoded!=null?dataDecoded:"null") + "\"";
		return getHTTPFailureResponse(failVal);
	}
	
	private boolean handleRequest(JSONObject request) throws JSONException
	{
		if (!request.isNull("Roll"))
		{
			BluetoothCommunication.sendMessageToDevice(mContext, 'R', String.valueOf(request.getInt("Roll")));
		}
		if (!request.isNull("Pitch"))
		{
			BluetoothCommunication.sendMessageToDevice(mContext, 'P', String.valueOf(request.getInt("Pitch")));
		}
		if (!request.isNull("Yaw"))
		{
			BluetoothCommunication.sendMessageToDevice(mContext, 'Y', String.valueOf(request.getInt("Yaw")));
		}
		if (!request.isNull("Throttle"))
		{
			BluetoothCommunication.sendMessageToDevice(mContext, 'T', String.valueOf(request.getInt("Throttle")));
		}
		if (!request.isNull("Arm"))
		{
			if (request.getInt("Arm")==1)
				BluetoothCommunication.sendMessageToDevice(mContext, 'A', "");
			else
				BluetoothCommunication.sendMessageToDevice(mContext, 'D', "");
		}
		if (!request.isNull("Callabrate"))
		{
			BluetoothCommunication.sendMessageToDevice(mContext, 'C', "");
		}
		if (!request.isNull("AcrobaticMode"))
		{
			BluetoothCommunication.sendMessageToDevice(mContext, 'M'
					, String.valueOf(request.getInt("AcrobaticMode")));
		}
		return true;
	}

}
