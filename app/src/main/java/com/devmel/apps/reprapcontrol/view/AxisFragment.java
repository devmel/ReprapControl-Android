package com.devmel.apps.reprapcontrol.view;

import com.devmel.apps.reprapcontrol.MainActivity;
import com.devmel.apps.reprapcontrol.R;
import com.devmel.apps.reprapcontrol.tools.GCodeControl;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class AxisFragment extends Fragment {
	MainActivity mActivity = null;	//Controller
	private Button homeX;
	private TextView positionX;
	private Button homeY;
	private TextView positionY;
	private Button homeZ;
	private TextView positionZ;
	private Button homeAll;
	private Button stop;
	private Button fanOff;
	private Button fanOn;
	private TextView positionE;
	private Button xL;
	private Button xP;
	private Button yL;
	private Button yP;
	private Button zL;
	private Button zP;
	private Button eL;
	private Button eP;
	private SeekBar stepsSeekbar;
	private TextView stepsText;
	private TextView bedCurrent;
	private TextView bedSet;
	private SeekBar bedSeekbar;
	private TextView extCurrent;
	private TextView extSet;
	private SeekBar extSeekbar;
	private CheckBox ccBox;
	private TextView laserPower;
	private Button laserOff;
	private Button laserOn;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity != null && activity instanceof MainActivity) {
			mActivity = (MainActivity) activity;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_axis, container, false);
		homeX = (Button) rootView.findViewById(R.id.homeX);
		positionX = (TextView) rootView.findViewById(R.id.positionX);
		homeY = (Button) rootView.findViewById(R.id.homeY);
		positionY = (TextView) rootView.findViewById(R.id.positionY);
		homeZ = (Button) rootView.findViewById(R.id.homeZ);
		positionZ = (TextView) rootView.findViewById(R.id.positionZ);
		homeAll = (Button) rootView.findViewById(R.id.homeAll);
		stop = (Button) rootView.findViewById(R.id.stop);
		fanOff = (Button) rootView.findViewById(R.id.fanOff);
		fanOn = (Button) rootView.findViewById(R.id.fanOn);
		positionE = (TextView) rootView.findViewById(R.id.positionE);
		xL = (Button) rootView.findViewById(R.id.xL);
		xP = (Button) rootView.findViewById(R.id.xP);
		yL = (Button) rootView.findViewById(R.id.yL);
		yP = (Button) rootView.findViewById(R.id.yP);
		zL = (Button) rootView.findViewById(R.id.zL);
		zP = (Button) rootView.findViewById(R.id.zP);
		eL = (Button) rootView.findViewById(R.id.eL);
		eP = (Button) rootView.findViewById(R.id.eP);
		stepsSeekbar = (SeekBar) rootView.findViewById(R.id.stepsSeekbar);
		stepsText = (TextView) rootView.findViewById(R.id.stepsText);
		bedCurrent = (TextView) rootView.findViewById(R.id.bedCurrent);
		bedSet = (TextView) rootView.findViewById(R.id.bedSet);
		bedSeekbar = (SeekBar) rootView.findViewById(R.id.bedSeekbar);
		extCurrent = (TextView) rootView.findViewById(R.id.extCurrent);
		extSet = (TextView) rootView.findViewById(R.id.extSet);
		extSeekbar = (SeekBar) rootView.findViewById(R.id.extSeekbar);
		ccBox = (CheckBox) rootView.findViewById(R.id.ccBox);
		laserPower = (TextView) rootView.findViewById(R.id.laserPower);
		laserOff = (Button) rootView.findViewById(R.id.laserOff);
		laserOn = (Button) rootView.findViewById(R.id.laserOn);

		stepsSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(mActivity != null){
					mActivity.sharedData.setStepsBarValue(progress);
					if(stepsText != null)
						stepsText.setText(mActivity.sharedData.getSteps() + " mm");
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		bedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(bedSet != null)
					bedSet.setText(String.format(Locale.US, "%05.1f" , progress / 10.0));
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(mActivity != null){
					if(!mActivity.gcodeControl.command("M140 S"+String.format(Locale.US, "%.1f" , ((float)seekBar.getProgress()) / 10.0)))
						mActivity.machineBusyMsg();
				}
			}
		});
		extSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(extSet != null)
					extSet.setText(String.format(Locale.US, "%05.1f" , progress / 10.0));
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(mActivity != null){
					if(!mActivity.gcodeControl.command("M104 S"+String.format(Locale.US, "%.1f" , ((float)seekBar.getProgress()) / 10.0)))
						mActivity.machineBusyMsg();
				}
			}
		});

		stop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null){
					mActivity.gcodeControl.resetBuffer();
					if(!mActivity.gcodeControl.command("M84"))
						mActivity.machineBusyMsg();
				}
			}
		});
		fanOff.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("M107"))
						mActivity.machineBusyMsg();
			}
		});
		fanOn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("M106"))
						mActivity.machineBusyMsg();
			}
		});
		homeAll.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G28"))
						mActivity.machineBusyMsg();
			}
		});
		homeX.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G28 X"))
						mActivity.machineBusyMsg();
			}
		});
		homeY.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G28 Y"))
						mActivity.machineBusyMsg();
			}
		});
		homeZ.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G28 Z"))
						mActivity.machineBusyMsg();
			}
		});
		xL.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G91\nG0 X-"+mActivity.sharedData.getSteps()+"\nG90"))
						mActivity.machineBusyMsg();
			}
		});
		xP.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G91\nG0 X"+mActivity.sharedData.getSteps()+"\nG90"))
						mActivity.machineBusyMsg();
			}
		});
		yL.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G91\nG0 Y-"+mActivity.sharedData.getSteps()+"\nG90"))
						mActivity.machineBusyMsg();
			}
		});
		yP.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G91\nG0 Y"+mActivity.sharedData.getSteps()+"\nG90"))
						mActivity.machineBusyMsg();
			}
		});
		zL.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G91\nG0 Z-"+mActivity.sharedData.getSteps()+"\nG90"))
						mActivity.machineBusyMsg();
			}
		});
		zP.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G91\nG0 Z"+mActivity.sharedData.getSteps()+"\nG90"))
						mActivity.machineBusyMsg();
			}
		});
		eL.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G91\nG0 E-"+mActivity.sharedData.getSteps()+"\nG90"))
						mActivity.machineBusyMsg();
			}
		});
		eP.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					if(!mActivity.gcodeControl.command("G91\nG0 E"+mActivity.sharedData.getSteps()+"\nG90"))
						mActivity.machineBusyMsg();
			}
		});
		laserPower.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					mActivity.sharedData.setLaserpower(Integer.parseInt(s.toString()));
				}catch(Exception e){}
			}
		});

		laserOff.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null){
					if(!mActivity.gcodeControl.command("M5"))
						mActivity.machineBusyMsg();
				}
			}
		});
		laserOn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String on = "M3";
				if(ccBox != null  && ccBox.isChecked())
					on = "M4";
				if(mActivity != null){
					on += " S"+mActivity.sharedData.getLaserpower();
					if(!mActivity.gcodeControl.command(on))
						mActivity.machineBusyMsg();
				}
			}
		});
		return rootView;
	}

	@Override
	public void onResume(){
		super.onResume();
		refresh();
	}

	@Override
	public void setUserVisibleHint(boolean visible){
		super.setUserVisibleHint(visible);
		refresh();
	}

	public void refresh(){
		testConnectivity();
		if(mActivity != null) {
			if (positionX != null)
				positionX.setText(String.format(Locale.US, "%.2f", mActivity.sharedData.getPositionX()));
			if (positionY != null)
				positionY.setText(String.format(Locale.US, "%.2f", mActivity.sharedData.getPositionY()));
			if (positionZ != null)
				positionZ.setText(String.format(Locale.US, "%.2f", mActivity.sharedData.getPositionZ()));
			if (positionE != null)
				positionE.setText(String.format(Locale.US, "%.2f", mActivity.sharedData.getPositionE()));
			if(stepsText != null)
				stepsText.setText(mActivity.sharedData.getSteps() + " mm");
			if(stepsSeekbar != null && stepsSeekbar.isFocused() == false)
				stepsSeekbar.setProgress(mActivity.sharedData.getStepsBarValue());
			if(bedCurrent != null)
				bedCurrent.setText(String.format(Locale.US, "%05.1f" , mActivity.sharedData.getTemperatureBed()[0]));
			if(bedSet != null)
				bedSet.setText(String.format(Locale.US, "%05.1f" , mActivity.sharedData.getTemperatureBed()[1]));
			if(extCurrent != null)
				extCurrent.setText(String.format(Locale.US, "%05.1f" , mActivity.sharedData.getTemperatureExtruder()[0]));
			if(extSet != null)
				extSet.setText(String.format(Locale.US, "%05.1f" , mActivity.sharedData.getTemperatureExtruder()[1]));
			if(laserPower != null && laserPower.isFocused()==false)
				laserPower.setText(mActivity.sharedData.getLaserpower()+"");
		}
	}

	private void setConnected(boolean connected){
		if (stop != null)
			stop.setEnabled(connected);
		if (fanOff != null)
			fanOff.setEnabled(connected);
		if (fanOn != null)
			fanOn.setEnabled(connected);
		if (homeAll != null)
			homeAll.setEnabled(connected);
		if (homeX != null)
			homeX.setEnabled(connected);
		if (homeY != null)
			homeY.setEnabled(connected);
		if (homeZ != null)
			homeZ.setEnabled(connected);
		if (xL != null)
			xL.setEnabled(connected);
		if (xP != null)
			xP.setEnabled(connected);
		if (yL != null)
			yL.setEnabled(connected);
		if (yP != null)
			yP.setEnabled(connected);
		if (zL != null)
			zL.setEnabled(connected);
		if (zP != null)
			zP.setEnabled(connected);
		if (eL != null)
			eL.setEnabled(connected);
		if (eP != null)
			eP.setEnabled(connected);
		if (bedSeekbar != null)
			bedSeekbar.setEnabled(connected);
		if (extSeekbar != null)
			extSeekbar.setEnabled(connected);
		if (laserOff != null)
			laserOff.setEnabled(connected);
		if (laserOn != null)
			laserOn.setEnabled(connected);
	}

	private void testConnectivity(){
		if (mActivity != null) {
			setConnected(mActivity.gcodeControl.isConnected());
		} else {
			setConnected(false);
		}
	}
}
