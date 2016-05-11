package com.devmel.apps.reprapcontrol.view;

import com.devmel.apps.reprapcontrol.MainActivity;
import com.devmel.apps.reprapcontrol.R;
import com.devmel.apps.reprapcontrol.tools.GCodeControl;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SdcardFragment extends Fragment {
	MainActivity mActivity = null;	//Controller
	private TextView statusDisplay;
	private Button btSDInit;
	private Button btPause;
	private Button btStart;
	private Button btTime;
	private ListView listViewFiles;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity != null && activity instanceof MainActivity) {
			mActivity = (MainActivity) activity;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_sdcard, container, false);
		statusDisplay = (TextView) rootView.findViewById(R.id.statusDisplay);
		btSDInit = (Button) rootView.findViewById(R.id.btSDInit);
		btPause = (Button) rootView.findViewById(R.id.btPause);
		btStart = (Button) rootView.findViewById(R.id.btStart);
		btTime = (Button) rootView.findViewById(R.id.btTime);
		listViewFiles = (ListView) rootView.findViewById(R.id.listViewFiles);

		btSDInit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null){
					if(!mActivity.gcodeControl.command("M22\nM21\nM20"))
						mActivity.machineBusyMsg();
				}
			}
		});
		btPause.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null){
					if(!mActivity.gcodeControl.command("M25"))
						mActivity.machineBusyMsg();
				}
			}
		});
		btStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null){
					if(!mActivity.gcodeControl.command("M24"))
						mActivity.machineBusyMsg();
				}
			}
		});
		btTime.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(mActivity != null){
					if(!mActivity.gcodeControl.command("M31"))
						mActivity.machineBusyMsg();
				}
			}
		});
		listViewFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String itemName = (String) parent.getItemAtPosition(position);
				if(mActivity != null && itemName != null){
					if(!mActivity.gcodeControl.command("M23 "+itemName.toLowerCase()))
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
	public void setUserVisibleHint(boolean visible) {
		super.setUserVisibleHint(visible);
		refresh();
	}

	public void refresh(){
		testConnectivity();
		if(mActivity != null){
			String sdStatus = getString(R.string.notSDPrinting);
			DeviceListAdapter list = new DeviceListAdapter(mActivity.getApplicationContext(), R.layout.files_list_item, mActivity.sharedData.sdFiles.toArray(new String[mActivity.sharedData.sdFiles.size()]));
			list.setSelected(mActivity.sharedData.getSDFileSelected());
			if(listViewFiles != null) {
				listViewFiles.setAdapter(list);
				justifyListViewHeightBasedOnChildren(listViewFiles);
			}
			if(mActivity.sharedData.getSDStatusTotal()>0){
				sdStatus = mActivity.sharedData.getSDStatusPosition() + "/" + mActivity.sharedData.getSDStatusTotal();
				double percent = mActivity.sharedData.getSDStatusPosition() * 100.0;
				percent /= mActivity.sharedData.getSDStatusTotal();
				sdStatus += " (" + String.format("%.2f" , percent)+ "%) ";
			}
			if(statusDisplay != null)
				statusDisplay.setText(sdStatus);
		}
	}

	private void setConnected(boolean connected){
		if (btPause != null)
			btPause.setEnabled(connected);
		if (btSDInit != null)
			btSDInit.setEnabled(connected);
		if (btTime != null)
			btTime.setEnabled(connected);
		if (listViewFiles != null)
			listViewFiles.setEnabled(connected);
	}
	private void setConnectedStatus(boolean connected){
		if (btStart != null)
			btStart.setEnabled(connected);
	}

	private void testConnectivity(){
		if (mActivity != null) {
			setConnected(mActivity.gcodeControl.isConnected());
			if(mActivity.sharedData.getSDStatusTotal() > 0){
				setConnectedStatus(mActivity.gcodeControl.isConnected());
			}else{
				setConnectedStatus(false);
			}
		} else {
			setConnected(false);
			setConnectedStatus(false);
		}
	}

	private static void justifyListViewHeightBasedOnChildren(ListView listView) {
		int totalHeight = 0;
		int count = 0;
		ListAdapter adapter = listView.getAdapter();
		if (adapter != null) {
			ViewGroup vg = listView;
			for (int i = 0; i < adapter.getCount(); i++) {
				View listItem = adapter.getView(i, null, vg);
				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
			}
			count = adapter.getCount();
		}
		ViewGroup.LayoutParams par = listView.getLayoutParams();
		par.height = totalHeight + (listView.getDividerHeight() * (count - 1));
		listView.setLayoutParams(par);
		listView.requestLayout();
	}
}
