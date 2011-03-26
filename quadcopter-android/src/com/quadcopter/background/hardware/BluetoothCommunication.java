package com.quadcopter.background.hardware;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothCommunication {
	public static final String TAG = "BluetoothCommunication";
	private final BluetoothAdapter mAdapter;
	//this is used to terminate a string
	private static final char ACK_FLAG = 19;
	//first char sent by arduino
	public static final char ARDUINO_MSG_FLAG = 18;
	
	private ConnectThread mConnectThread = null;
	private ConnectedThread mConnectedThread = null;
	
	public static final String BROADCAST_BLUETOOTH_RECIEVCED_MESSAGE = "com.quadcopter.background.bluetooth.MESSAGE";
	public static final String BROADCAST_BLUETOOTH_STATE_CHANGED = "com.quadcopter.background.bluetooth.STATE.CHANGED";
	private static final String BROADCAST_BLUETOOTH_SEND_MESSAGE = "com.quadcopter.background.bluetooth.SEND";
	public static final String EXTRA_CHAR_FLAG = "charFlag";
	public static final String EXTRA_STRING_DATA_TO_SEND = "stringData";
	public static final String EXTRA_BLUETOOTH_STATE = "stateMsg";
	
	//uuid for use with the serial device
    private static final UUID SD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    //the connection state
    private int state = 0;
    
    public static final int STATE_NOT_CONNECTED = 0;
    public static final int STATE_CONNECTION_FAILED = 1;
    public static final int STATE_CONNECTION_LOST = 2;
    public static final int STATE_CONNECTED = 3;
    private String mAddress = null;
    private Context mContext = null;
	
	public BluetoothCommunication(Context context, BluetoothAdapter bluetoothAdapter,String address)
	{
		mContext = context;
		mAddress = address;
		mAdapter = bluetoothAdapter;
	}
	
	public synchronized void start()
	{
		BluetoothDevice device = mAdapter.getRemoteDevice(mAddress);
		mConnectThread = new ConnectThread(device);
		mConnectThread.setDaemon(true);
		mConnectThread.start();
		//register the broadcast reciever
		IntentFilter filter = new IntentFilter(BROADCAST_BLUETOOTH_SEND_MESSAGE);
		mContext.registerReceiver(mBroadcastReciever, filter);
	}
	
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        mContext.unregisterReceiver(mBroadcastReciever);
    }
    
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public synchronized void write(char flag, byte[] out) {
    	if (mConnectedThread!=null)
    		mConnectedThread.write(flag, out);
    }
    
	public void setState(int state) 
	{
		this.state = state;
		Intent broadcast = new Intent(BROADCAST_BLUETOOTH_RECIEVCED_MESSAGE);
		broadcast.putExtra(EXTRA_BLUETOOTH_STATE, state);
		mContext.sendBroadcast(broadcast);
	}

	public int getState() {
		return state;
	}
	
	public static void sendMessageToDevice(Context context, char flag, String msg)
	{
		Intent broadcast = new Intent(BROADCAST_BLUETOOTH_SEND_MESSAGE);
		broadcast.putExtra(EXTRA_CHAR_FLAG, flag);
		broadcast.putExtra(EXTRA_STRING_DATA_TO_SEND, msg);
		context.sendBroadcast(broadcast);
	}
	
	BroadcastReceiver mBroadcastReciever = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_BLUETOOTH_SEND_MESSAGE))
			{
				char flag = intent.getCharExtra(EXTRA_CHAR_FLAG, ' ');
				String data = intent.getStringExtra(EXTRA_STRING_DATA_TO_SEND);
				if (flag != ' ' && data!=null)
				{
					BluetoothCommunication.this.write(flag, data.getBytes());
				}
			}
		}
	};
	
	/**
	 * This function is used to broadcast the message received from the 
	 * @param msg
	 */
	private void broadcastMessage(String msg)
	{
		Intent broadcast = new Intent(BROADCAST_BLUETOOTH_RECIEVCED_MESSAGE);
		broadcast.putExtra(EXTRA_STRING_DATA_TO_SEND, msg);
		mContext.sendBroadcast(broadcast);
	}
	
	 /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        setState(STATE_CONNECTED);
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.setDaemon(true);
        mConnectedThread.start();
    }

	/**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(SD_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                BluetoothCommunication.this.setState(STATE_CONNECTION_FAILED);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (BluetoothCommunication.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) 
        {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try 
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) 
            {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            final char ACK_FLAG = 19;
            String msg;
            StringBuffer fullMsg = new StringBuffer();
            char c;
            // Keep listening to the InputStream while connected
            while (true) 
            {
                try 
                {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    msg = new String(buffer, 0, (bytes != -1) ? bytes : 0 );
                    
        			for (int i=0;i<msg.length();i++)
        			{
        				c = msg.charAt(i);
        				if (c == ACK_FLAG)
        				{
        					if ("IsAlive".equals(fullMsg.toString()))
        					{
        						this.write('I', "".getBytes());
        					} else
        					{
	        					// message complete send the data
	        					broadcastMessage(fullMsg.toString());
        					}
//    	                    mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, fullMsg.length(), -1, fullMsg.toString().getBytes())
//    	                            .sendToTarget();
        					fullMsg = new StringBuffer();
        				} else if (c != ARDUINO_MSG_FLAG)
        				{
        					fullMsg.append(c);
        				}
        			}
                } catch (IOException e) 
                {
                    Log.e(TAG, "disconnected", e);
                    BluetoothCommunication.this.setState(STATE_CONNECTION_LOST);
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(char flag, byte[] buffer) 
        {
            try 
            {
            	mmOutStream.write(flag);
                mmOutStream.write(buffer);
                mmOutStream.write(ACK_FLAG);

                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() 
        {
            try 
            {
                mmSocket.close();
            } catch (IOException e) 
            {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
