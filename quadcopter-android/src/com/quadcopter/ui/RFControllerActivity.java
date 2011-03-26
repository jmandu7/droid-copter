package com.quadcopter.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.quadcopter.R;
import com.quadcopter.background.hardware.BluetoothCommunication;

public class RFControllerActivity extends Activity implements OnClickListener, OnSeekBarChangeListener
{
	Button arm;
	Button disarm;
	Button acro;
	Button stable;
	Button kill;
	Button reset;
	
	Button incRoll;
	Button decRoll;
	Button incPitch;
	Button decPitch;
	Button incYaw;
	Button decYaw;
	Button incThrottle;
	Button decThrottle;
	
	Button incStep;
	Button decStep;
	TextView lblStepSize;
	int stepSize = 1;
	
	SeekBar roll;
	SeekBar pitch;
	SeekBar yaw;
	SeekBar throttle;
	
	TextView lblRoll;
	TextView lblPitch;
	TextView lblYaw;
	TextView lblThrottle;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);   
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.rfcontroller);
        
        lblRoll = (TextView)findViewById(R.id.rollval);
        lblPitch = (TextView)findViewById(R.id.pitchval);
        lblYaw = (TextView)findViewById(R.id.yawval);
        lblThrottle = (TextView)findViewById(R.id.throttleval);
        
        arm = (Button)findViewById(R.id.btnArm);
        disarm = (Button)findViewById(R.id.btnDisarm);
        stable = (Button)findViewById(R.id.btnStable);
        acro = (Button)findViewById(R.id.btnAcro);
        kill = (Button)findViewById(R.id.btnKill);
        reset = (Button)findViewById(R.id.btnReset);
        
        arm.setOnClickListener(this);
        disarm.setOnClickListener(this);
        stable.setOnClickListener(this);
        acro.setOnClickListener(this);
        kill.setOnClickListener(this);
        reset.setOnClickListener(this);
        
        
        roll = (SeekBar)findViewById(R.id.sbRoll);
        pitch = (SeekBar)findViewById(R.id.sbPitch);
        yaw = (SeekBar)findViewById(R.id.sbYaw);
        throttle = (SeekBar)findViewById(R.id.sbThrottle);
        
        roll.setOnSeekBarChangeListener(this);
        pitch.setOnSeekBarChangeListener(this);
        yaw.setOnSeekBarChangeListener(this);
        throttle.setOnSeekBarChangeListener(this);
        
        incRoll = (Button)findViewById(R.id.introll);
        incPitch = (Button)findViewById(R.id.intpitch);
        incYaw = (Button)findViewById(R.id.incyaw);
        incThrottle = (Button)findViewById(R.id.intthrottle);
        decRoll = (Button)findViewById(R.id.decroll);
        decPitch = (Button)findViewById(R.id.decpitch);
        decYaw = (Button)findViewById(R.id.decyaw);
        decThrottle = (Button)findViewById(R.id.decthrottle);
        
        incRoll.setOnClickListener(this);
        decRoll.setOnClickListener(this);
        incPitch.setOnClickListener(this);
        decPitch.setOnClickListener(this);
        incYaw.setOnClickListener(this);
        decYaw.setOnClickListener(this);
        incThrottle.setOnClickListener(this);
        decThrottle.setOnClickListener(this);
        
        incStep = (Button)findViewById(R.id.incStep);
        decStep = (Button)findViewById(R.id.decStep);
        incStep.setOnClickListener(this);
        decStep.setOnClickListener(this);
        lblStepSize = (TextView) findViewById(R.id.stepSize);
        
    }

	@Override
	public void onClick(View v) {
		if (v==arm)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'A', "");
		} else if (v==disarm)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'D', "");
		} else if (v==stable)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'M', "0");
		} else if (v==acro)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'M', "1");
		} else if (v==kill)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'R', "1500");
			BluetoothCommunication.sendMessageToDevice(this, 'P', "1500");
			BluetoothCommunication.sendMessageToDevice(this, 'Y', "1500");
			BluetoothCommunication.sendMessageToDevice(this, 'T', "1000");
			roll.setProgress(500);
			pitch.setProgress(500);
			yaw.setProgress(500);
			throttle.setProgress(0);
			BluetoothCommunication.sendMessageToDevice(this, 'D', "");
		} else if (v==reset)
		{
			roll.setProgress(500);
			pitch.setProgress(500);
			yaw.setProgress(500);
		} else if (v==incRoll)
		{
			roll.setProgress(roll.getProgress()+stepSize);
		} else if (v==decRoll)
		{
			roll.setProgress(roll.getProgress()-stepSize);
		} else if (v==incPitch)
		{
			pitch.setProgress(pitch.getProgress()+stepSize);
		} else if (v==decPitch)
		{
			pitch.setProgress(pitch.getProgress()-stepSize);
		} else if (v==incYaw)
		{
			yaw.setProgress(yaw.getProgress()+stepSize);
		} else if (v==decYaw)
		{
			yaw.setProgress(yaw.getProgress()-stepSize);	
		} else if (v==incThrottle)
		{
			throttle.setProgress(throttle.getProgress()+stepSize);
		} else if (v==decThrottle)
		{
			throttle.setProgress(throttle.getProgress()-stepSize);
		} else if (v==incStep)
		{
			stepSize++;
			lblStepSize.setText(stepSize+"");
		} else if (v==decStep)
		{
			stepSize--;
			lblStepSize.setText(stepSize+"");
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
	{
		if (seekBar == roll)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'R', String.valueOf(progress+1000));
			lblRoll.setText(progress+1000+"");
		} else if (seekBar == pitch)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'P', String.valueOf(progress+1000));
			lblPitch.setText(progress+1000+"");
		} else if (seekBar == yaw)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'Y', String.valueOf(progress+1000));
			lblYaw.setText(progress+1000+"");
		} else if (seekBar == throttle)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'T', String.valueOf(progress+1000));
			lblThrottle.setText(progress+1000+"");
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
