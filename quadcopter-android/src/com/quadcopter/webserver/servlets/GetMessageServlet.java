package com.quadcopter.webserver.servlets;

import java.util.LinkedList;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.quadcopter.background.hardware.BluetoothCommunication;

public class GetMessageServlet extends Servlet{

	LinkedList<String> msgQueue = new LinkedList<String>();
	
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public synchronized void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothCommunication.BROADCAST_BLUETOOTH_RECIEVCED_MESSAGE))
			{
				String msg = intent.getStringExtra(BluetoothCommunication.EXTRA_STRING_DATA_TO_SEND);
				if (msg!=null)
				{
					msgQueue.add(msg);
				}
			}
		}
	};
	
	@Override
	public void setupServlet(Context context) {
		super.setupServlet(context);
		IntentFilter filter = new IntentFilter(BluetoothCommunication.BROADCAST_BLUETOOTH_RECIEVCED_MESSAGE);
		context.registerReceiver(receiver, filter);
	}

	@Override
	public void runServlet(HttpRequest request, HttpResponse response) 
	{
		String ret="";
		while (!msgQueue.isEmpty())
			ret += msgQueue.poll() + "\n";
		setHTTPSuccessResponse(response, ret);
	}

}
