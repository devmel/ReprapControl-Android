package com.devmel.apps.reprapcontrol.view;

import com.devmel.apps.reprapcontrol.MainActivity;
import com.devmel.apps.reprapcontrol.PortSelect;
import com.devmel.apps.reprapcontrol.R;
import com.devmel.apps.reprapcontrol.datas.SharedData;
import com.devmel.storage.android.UserPrefs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MachineFragment extends Fragment {
	MainActivity mActivity = null;	//Controller
	SharedData sharedData = new SharedData();

	private Button connectBt;
	private Button selectPortBt;
	private EditText baudRateValue;
	private RadioGroup dataBitsValue;
	private RadioGroup stopBitsValue;
	private RadioGroup parityValue;
	private TextView infosText;
	private Button swresetBt;
	private LinearLayout lbLayout;
	private CompoundButton vtgSwitch;
	private CompoundButton resetSwitch;

	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
		if(activity != null && activity instanceof MainActivity) {
			sharedData = ((MainActivity) activity).sharedData;
			mActivity = (MainActivity) activity;
		}
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_machine, container, false);
		connectBt = (Button) rootView.findViewById(R.id.connectBt);
		selectPortBt = (Button) rootView.findViewById(R.id.selectPortBt);
		baudRateValue = (EditText) rootView.findViewById(R.id.baudRateValue);
		dataBitsValue = (RadioGroup) rootView.findViewById(R.id.dataBitsValue);
		stopBitsValue = (RadioGroup) rootView.findViewById(R.id.stopBitsValue);
		parityValue = (RadioGroup) rootView.findViewById(R.id.parityValue);
		infosText = (TextView) rootView.findViewById(R.id.infosText);
		swresetBt = (Button) rootView.findViewById(R.id.swresetBt);
		lbLayout = (LinearLayout) rootView.findViewById(R.id.lbLayout);
		vtgSwitch = (CompoundButton) rootView.findViewById(R.id.vtgSwitch);
		resetSwitch = (CompoundButton) rootView.findViewById(R.id.resetSwitch);

		baudRateValue.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				sharedData.setBaudRate(getBaudRateView());
			}
		});

		dataBitsValue.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				sharedData.setDataBits(getDataBitsView());
			}
		});

		stopBitsValue.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				sharedData.setStopbits(getStopbitsView());
			}
		});

		parityValue.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				sharedData.setParity(getParityView());
			}
		});

		selectPortBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					mActivity.portSelect();
			}
		});
		connectBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					mActivity.connectClick();
			}
		});
		swresetBt.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null){
					mActivity.gcodeControl.resetBuffer();
					if(!mActivity.gcodeControl.command("M999")){
						mActivity.machineBusyMsg();
					}
				}
			}
		});

		vtgSwitch.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					mActivity.vtgToggle(vtgSwitch.isChecked());
			}
		});

		resetSwitch.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null)
					mActivity.resetToggle(resetSwitch.isChecked());
			}
		});

		return rootView;
	}
	
	@Override
	public void onResume(){
	    super.onResume();
		if (selectPortBt != null)
			selectPortBt.setText((sharedData.getPortName() != null) ? sharedData.getPortName() : getString(R.string.port_select));
		setParametersView();
		refresh();
	}

	public void refresh(){
		setInfos(sharedData.firmware);
		setLinkBusStatus(sharedData.linkbusStatus[0],sharedData.linkbusStatus[1],sharedData.linkbusStatus[2]);
		testConnectivity();
	}


	private void setInfos(final String infos){
		if(infos != null)
			infosText.setText(infos);
		else
			infosText.setText("");
	}
	private void setConnected(boolean connect){
		if(connect==true){
			baudRateValue.setEnabled(false);
			for (int i = 0; i < dataBitsValue.getChildCount(); i++) {
				dataBitsValue.getChildAt(i).setEnabled(false);
			}
			for (int i = 0; i < stopBitsValue.getChildCount(); i++) {
				stopBitsValue.getChildAt(i).setEnabled(false);
			}
			for (int i = 0; i < parityValue.getChildCount(); i++) {
				parityValue.getChildAt(i).setEnabled(false);
			}
		}else{
			baudRateValue.setEnabled(true);
			for (int i = 0; i < dataBitsValue.getChildCount(); i++) {
				dataBitsValue.getChildAt(i).setEnabled(true);
			}
			for (int i = 0; i < stopBitsValue.getChildCount(); i++) {
				stopBitsValue.getChildAt(i).setEnabled(true);
			}
			for (int i = 0; i < parityValue.getChildCount(); i++) {
				parityValue.getChildAt(i).setEnabled(true);
			}
		}
		if(swresetBt != null)
			swresetBt.setEnabled(connect);
	}
	private void setConnectButton(int state){
		if(state == 2){
			connectBt.setEnabled(false);
			connectBt.setText(getString(R.string.connecting));
		}else if(state == 1){
			connectBt.setEnabled(true);
			connectBt.setText(getString(R.string.disconnect));
		}else{
			connectBt.setEnabled(true);
			connectBt.setText(getString(R.string.connect));
		}
	}

	private void setLinkBusStatus(boolean isLinkbus, boolean vtg, boolean reset){
		if(lbLayout != null){
			if(isLinkbus){
				if(vtgSwitch != null){
					vtgSwitch.setChecked(vtg);
				}
				if(resetSwitch != null){
					resetSwitch.setChecked(reset);
				}
				lbLayout.setVisibility(View.VISIBLE);
			}
			else {
				lbLayout.setVisibility(View.GONE);
			}
		}
	}


	private int getBaudRateView(){
		int baudRate = sharedData.getBaudrate();
		try{baudRate = Integer.valueOf(baudRateValue.getText().toString());}catch(Exception e){}
		return baudRate;
	}
	private int getDataBitsView(){
		int dataBits = sharedData.getDatabits();
		switch (dataBitsValue.getCheckedRadioButtonId()) {
		case R.id.dataBits5:
			dataBits=5;
			break;
		case R.id.dataBits6:
			dataBits=6;
			break;
		case R.id.dataBits7:
			dataBits=7;
			break;
		case R.id.dataBits8:
			dataBits=8;
			break;
		}
		return dataBits;
	}
	private int getStopbitsView(){
		int stopBits = sharedData.getStopbits();
		switch (stopBitsValue.getCheckedRadioButtonId()) {
		case R.id.stopBits1:
			stopBits = 1;
			break;
		case R.id.stopBits2:
			stopBits = 2;
			break;
		}
		return stopBits;
	}
	private int getParityView(){
		int parity = sharedData.getParity();
		switch (parityValue.getCheckedRadioButtonId()) {
		case R.id.parityNone:
			parity=0;
			break;
		case R.id.parityOdd:
			parity=1;
			break;
		case R.id.parityEven:
			parity=2;
			break;
		}
		return parity;
	}

	private void setParametersView(){
		this.setBaudRateView(sharedData.getBaudrate());
		this.setDataBitsView(sharedData.getDatabits());
		this.setStopbitsView(sharedData.getStopbits());
		this.setParityView(sharedData.getParity());
	}

	private void setBaudRateView(int baudrate){
		if(baudrate<0){
			baudrate = 9600;
		}
		if(baudRateValue != null)
			baudRateValue.setText(""+baudrate);
	}
	private void setDataBitsView(int dataBits){
		int id = R.id.dataBits8;
		switch (dataBits) {
			case 5:
				id=R.id.dataBits5;
				break;
			case 6:
				id=R.id.dataBits6;
				break;
			case 7:
				id=R.id.dataBits7;
				break;
		}
		if(dataBitsValue != null)
			dataBitsValue.check(id);
	}
	private void setStopbitsView(int stopBits){
		int id = R.id.stopBits1;
		switch (stopBits) {
			case 2:
				id=R.id.stopBits2;
				break;
		}
		if(stopBitsValue != null)
			stopBitsValue.check(id);
	}
	private void setParityView(int parity){
		int id = R.id.parityNone;
		switch (parity) {
		case 1:
			id=R.id.parityOdd;
			break;
		case 2:
			id=R.id.parityEven;
			break;
		}
		if(parityValue != null)
			parityValue.check(id);
	}

	private void testConnectivity(){
		if (mActivity != null) {
			setConnected(mActivity.sharedData.connect);
			int btState = 0;
			if(mActivity.sharedData.connect)
				btState = 2;
			if(mActivity.gcodeControl.isConnected())
				btState = 1;
			setConnectButton(btState);
		} else {
			setConnected(false);
		}
	}

}
