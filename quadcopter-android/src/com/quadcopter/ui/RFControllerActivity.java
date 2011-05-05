package com.quadcopter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
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
	private static final int THROTTLE_STEP_SIZE = 10;
	
	Button arm;
	Button disarm;
	Button acro;
	Button stable;
	Button kill;
	Button reset;
	Button callibrate;
	
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
	
	Button setTrim;
	
	Button back;
	
	int trimPitch=0;
	int trimRoll=0;
	int trimYaw=0;
	
	int oldTrimPitch=0;
	int oldTrimRoll=0;
	int oldTrimYaw=0;
	
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
        callibrate = (Button)findViewById(R.id.btnCallibrate);
        
        arm.setOnClickListener(this);
        disarm.setOnClickListener(this);
        stable.setOnClickListener(this);
        acro.setOnClickListener(this);
        kill.setOnClickListener(this);
        reset.setOnClickListener(this);
        callibrate.setOnClickListener(this);
        
        
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
        
        setTrim = (Button)findViewById(R.id.setTrim);
        setTrim.setOnClickListener(this);
        
        back = (Button)findViewById(R.id.btnBack);
        back.setOnClickListener(this);
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
//			oldTrimPitch = trimPitch;
//			oldTrimYaw = trimYaw;
//			oldTrimRoll = trimRoll;
//			roll.setProgress(500);
//			pitch.setProgress(500);
//			yaw.setProgress(500);
		} else if (v==acro)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'M', "1");
			trimPitch = oldTrimPitch;
			trimYaw = oldTrimYaw;
			trimRoll = oldTrimRoll;
			roll.setProgress(500+trimRoll);
			pitch.setProgress(500+trimPitch);
			yaw.setProgress(500+trimYaw);
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
			roll.setProgress(500+trimRoll);
			pitch.setProgress(500+trimPitch);
			yaw.setProgress(500+trimYaw);
		} else if (v==incRoll)
		{
			roll.setProgress(roll.getProgress()+stepSize*4);
		} else if (v==decRoll)
		{
			roll.setProgress(roll.getProgress()-stepSize*4);
		} else if (v==incPitch)
		{
			pitch.setProgress(pitch.getProgress()+stepSize*4);
		} else if (v==decPitch)
		{
			pitch.setProgress(pitch.getProgress()-stepSize*4);
		} else if (v==incYaw)
		{
			yaw.setProgress(yaw.getProgress()+stepSize*4);
		} else if (v==decYaw)
		{
			yaw.setProgress(yaw.getProgress()-stepSize*4);	
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
		} else if (v==setTrim)
		{
			trimPitch = pitch.getProgress() - 500;
			trimRoll = roll.getProgress() - 500;
			trimYaw = yaw.getProgress() - 500;
		} else if (v==back)
		{
			startActivity(new Intent(this, QuadCopterActivity.class));
		} else if (v==callibrate)
		{
			BluetoothCommunication.sendMessageToDevice(this, 'C', "");
		}
	}

	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
	{
		int startVal = 1375;
		if (seekBar == roll)
		{
			progress = progress/4;	
			BluetoothCommunication.sendMessageToDevice(this, 'R', String.valueOf(progress+startVal));
			lblRoll.setText(progress+startVal+"");
		} else if (seekBar == pitch)
		{
			progress = progress/4;	
			BluetoothCommunication.sendMessageToDevice(this, 'P', String.valueOf(progress+startVal));
			lblPitch.setText(progress+startVal+"");
		} else if (seekBar == yaw)
		{
			progress = progress/4;
			BluetoothCommunication.sendMessageToDevice(this, 'Y', String.valueOf(progress+startVal));
			lblYaw.setText(progress+startVal+"");
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
		if (seekBar == roll)
		{
			seekBar.setProgress(500+trimRoll);
		} else if (seekBar == pitch)
		{
			seekBar.setProgress(500+trimPitch);
		} else if (seekBar == yaw)
		{
			seekBar.setProgress(500+trimYaw);
		}
	}

	boolean pp;
	boolean pm;
	boolean rp;
	boolean rm;
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_E&&pp)
		{
			pp = false;
			pitch.setProgress(pitch.getProgress()-stepSize*4);
		} else if (keyCode == KeyEvent.KEYCODE_X&&pm)
		{
			pm = false;
			pitch.setProgress(pitch.getProgress()+stepSize*4);
		} else if (keyCode == KeyEvent.KEYCODE_S&&rm)
		{
			rm = false;
			roll.setProgress(roll.getProgress()+stepSize*4);
		} else if (keyCode == KeyEvent.KEYCODE_F&&rp)
		{
			rp = false;
			roll.setProgress(roll.getProgress()-stepSize*4);
		} else
		{
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_P)
		{
			throttle.setProgress(throttle.getProgress()+THROTTLE_STEP_SIZE);
		} else if (keyCode == KeyEvent.KEYCODE_PERIOD)
		{
			throttle.setProgress(throttle.getProgress()-THROTTLE_STEP_SIZE);
		} else if (keyCode == KeyEvent.KEYCODE_E&&!pp)
		{
			pp = true;
			pitch.setProgress(pitch.getProgress()+stepSize*4);
		} else if (keyCode == KeyEvent.KEYCODE_X&&!pm)
		{
			pm = true;
			pitch.setProgress(pitch.getProgress()-stepSize*4);
		} else if (keyCode == KeyEvent.KEYCODE_S&&!rm)
		{
			rm = true;
			roll.setProgress(roll.getProgress()-stepSize*4);
		} else if (keyCode == KeyEvent.KEYCODE_F&&!rp)
		{
			rp = true;
			roll.setProgress(roll.getProgress()+stepSize*4);
		} else
		{
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}
}
