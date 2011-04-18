package com.quadcopter.webserver.servlets;

import java.net.URLDecoder;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
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
	public void runServlet(HttpRequest request, HttpResponse response) 
	{
		String failVal = "";
		String dataDecoded = null;
		String formData = null;
		try {
			
			formData = getHTTPPostBody(request);

			dataDecoded = URLDecoder.decode(formData);
			//data.substring(data.indexOf("data="));dataDecoded
			JSONObject data = new JSONObject(dataDecoded);
			
			if (data!=null&&handleRequest(data))
			{
				setHTTPSuccessResponse(response, "Success!");
				return;
			} else
			{
				setHTTPFailureResponse(response, "form data was null or there was an error handling request");
			}
		} catch (JSONException e) {
			failVal = e.toString();
		}
		failVal += " formData=\"" + (formData!=null?formData:"null") + "\"";
		failVal += " dataDecoded=\"" + (dataDecoded!=null?dataDecoded:"null") + "\"";
		failVal += " request.getRequestLine()=\"" + request.getRequestLine() + "\"";
		setHTTPFailureResponse(response, failVal);
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
