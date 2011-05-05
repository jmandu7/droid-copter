package com.quadcopter.ui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quadcopter.R;
import com.quadcopter.background.IBackgroundService;

public class QuadCopterActivity extends Activity implements OnClickListener, PreviewCallback{
	public static String TAG = "QuadCopterActivity";
	
	private IBackgroundService mBackgroundService;
	
//	CameraPreview mCamPreview;
	
    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 1;
    
	boolean startServerOnBind = false;
	
    private ServiceConnection mWebServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
			mBackgroundService = null;
			((TextView)findViewById(R.id.rf_controller)).setEnabled(false);
			//mCamPreview.mWebService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBackgroundService = IBackgroundService.Stub.asInterface(service);
			try {
				//if the web server needs to be started then start it
				if (mBackgroundService.isRunning())
				{
					((TextView)findViewById(R.id.rf_controller)).setEnabled(true);
					((Button)findViewById(R.id.start_server)).setText("Stop");
					//show ip address - works for LAN
			        TextView lblIpAddress = (TextView)findViewById(R.id.ip_address);
			        Context c = QuadCopterActivity.this.getApplicationContext(); 
			        String ip = getIPAddress(c);
			        
			        lblIpAddress.setText(ip+":"+mBackgroundService.getPort());
				}else if (startServerOnBind)
				{
					((TextView)findViewById(R.id.rf_controller)).setEnabled(true);
					mBackgroundService.start();
			        //show ip address - works for LAN
			        TextView lblIpAddress = (TextView)findViewById(R.id.ip_address);
			        Context c = QuadCopterActivity.this.getApplicationContext(); 
			        String ip = getIPAddress(c);
			        
			        lblIpAddress.setText(ip+":"+mBackgroundService.getPort());
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//                WindowManager.LayoutParams.FLAG_FULLSCREEN );
        
        setContentView(R.layout.main);
        
        ((CameraPreviewSurface)findViewById(R.id.camera_preview_surface)).setPreviewCallback(this);
        
        Button startServer = ((Button)findViewById(R.id.start_server));
        startServer.setOnClickListener(this);
        startServer.setClickable(true);
        
		//Startup the WebServerService 
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(IBackgroundService.class.getName());
        startService(serviceIntent);
//        bindService(serviceIntent, mWebServiceConnection, Context.BIND_AUTO_CREATE);
        
        TextView lblIpAddress = (TextView)findViewById(R.id.ip_address);
        lblIpAddress.setText("unavaliable");
        
        
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
//        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//        }
        
        TextView control = ((TextView)findViewById(R.id.rf_controller));
        control.setEnabled(false);
        control.setClickable(true);
        control.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(QuadCopterActivity.this, RFControllerActivity.class));
			}
		});
    }
	
    public void onClick(View v) {
		Button btn = (Button) v;
		if (btn.getText().equals("Start"))
		{
			if (mBackgroundService==null)
			{
				//Startup the WebServerService 
				startServerOnBind = true;
		        Intent serviceIntent = new Intent();
		        serviceIntent.setAction(IBackgroundService.class.getName());
		        bindService(serviceIntent, mWebServiceConnection, Context.BIND_AUTO_CREATE);
			} else
			{
				try {
					mBackgroundService.start();
					
			        //show ip address - works for LAN
			        TextView lblIpAddress = (TextView)findViewById(R.id.ip_address);
			        Context c = QuadCopterActivity.this.getApplicationContext(); 
			        String ip = getIPAddress(c);
			        
			        lblIpAddress.setText(ip+":"+mBackgroundService.getPort());
			        
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			btn.setText("Stop");
		} else
		{
			if (mBackgroundService!=null)
			{
				try {
					mBackgroundService.stop();
					
			        TextView lblIpAddress = (TextView)findViewById(R.id.ip_address);
			        lblIpAddress.setText("unavaliable");
			        
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			btn.setText("Start");
		}
	}
    
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session

            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Unable to enable bluetooth", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (mBackgroundService!=null)
		{
			try {
				Camera.Parameters params = camera.getParameters();
				
				//The image is in the NV21 image format and
				//must be converted to JPEG.
				YuvImage converter = new YuvImage(data, 
						ImageFormat.NV21,
						params.getPreviewSize().width,
						params.getPreviewSize().height,null);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				converter.compressToJpeg(new Rect(0, 0, converter.getWidth(),
						converter.getHeight()), 
						80, baos);
				
				//Pass the JPEG image to the WebServer
				mBackgroundService.setWebCamByteArray(baos.toByteArray());
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static String getIPAddress(Context context) 
	{
		WifiManager wifiManager = (WifiManager) context
		.getSystemService(Context.WIFI_SERVICE);
		android.net.wifi.WifiInfo info = wifiManager.getConnectionInfo();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		byte[] intByte;
		try 
		{
			dos.writeInt(info.getIpAddress());
			dos.flush();
			intByte = bos.toByteArray();
		} catch (IOException e) {
			Log.e(TAG, "Problem converting IP address");
			return "unknown";
		}

		// Reverse int bytes...
		byte[] addressBytes = new byte[intByte.length];
		for (int i = 0; i < intByte.length; i++) 
		{
			addressBytes[i] = intByte[(intByte.length - 1) - i];
		}

		InetAddress address = null;
		try 
		{
			address = InetAddress.getByAddress(addressBytes);
		} catch (UnknownHostException e) 
		{
			Log.e(TAG, "Problem determing IP address");
			return "unknown";
		}
		return address.getHostAddress();
	}
}