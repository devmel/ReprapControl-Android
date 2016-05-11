package com.devmel.apps.reprapcontrol;

import com.devmel.apps.reprapcontrol.view.DeviceListAdapter;
import com.devmel.communication.android.UartBluetooth;
import com.devmel.communication.android.UartUsbOTG;
import com.devmel.storage.Node;
import com.devmel.storage.SimpleIPConfig;
import com.devmel.storage.android.UserPrefs;
import com.devmel.tools.android.USBOTGSystem;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PortSelect extends Activity {
	RadioGroup deviceSelect;
	ListView linkbusDevices;
	Button linkbusAdd;
	ListView usbDevices;
	Button usbEnable;
	ListView bluetoothDevices;
	Button bluetoothEnable;
	UserPrefs userPrefs;
	
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_port_select);
		deviceSelect = (RadioGroup) findViewById(R.id.deviceSelect);
		linkbusDevices = (ListView) findViewById(R.id.linkbusDevices);
		linkbusAdd = (Button) findViewById(R.id.linkbusAdd);
		usbDevices = (ListView) findViewById(R.id.usbDevices);
		usbEnable = (Button) findViewById(R.id.usbEnable);
		bluetoothDevices = (ListView) findViewById(R.id.bluetoothDevices);
		bluetoothEnable = (Button) findViewById(R.id.bluetoothEnable);
		
		linkbusAdd.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				addLinkbus();
  			}
		});
		usbEnable.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
	           	//Enable usb
				try{
					USBOTGSystem.enable();
            	    Toast.makeText(getApplicationContext(), R.string.usb_otg_enable, Toast.LENGTH_LONG).show();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		bluetoothEnable.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
	           	//Enable bluetooth
				try{
	            	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	            	if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
	            	    mBluetoothAdapter.enable();
	            	    Thread.sleep(1000);
	            	    Toast.makeText(getApplicationContext(), R.string.bt_enable, Toast.LENGTH_LONG).show();
	            	}
	            	hideAllList();
            	    listSelected("BT");
            	    colorSelected();
				}catch(Exception e){
					e.printStackTrace();
				}
  			}
		});
		
		linkbusDevices.setOnItemClickListener(new OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
	            String itemName = (String) parent.getItemAtPosition(position);
	            save("LB", itemName);
            	hideAllList();
        	    listSelected("LB");
	            colorSelected();
		    }
		});
		
		usbDevices.setOnItemClickListener(new OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
	            String itemName = (String) parent.getItemAtPosition(position);
	            save("USB", itemName);
	            colorSelected();
		    }
		});
		
		bluetoothDevices.setOnItemClickListener(new OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
	            String itemName = (String) parent.getItemAtPosition(position);
	            save("BT", itemName);
	            colorSelected();
		    }
		});

		
		deviceSelect.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup selected, int checkedId) {
				hideAllList();
				if(checkedId == R.id.linkbusChecked){
					listSelected("LB");
				}
				if(checkedId == R.id.usbChecked){
					listSelected("USB");
				}
				if(checkedId == R.id.bluetoothChecked){
					listSelected("BT");
				}
	            colorSelected();
			}
		});
		
	}
	
	@Override
	protected void onResume(){
	    super.onResume();
		initPreferences();
		//Initialize
		String type = userPrefs.getString("selectedType");
		if(type!=null){
			if(type.equals("LB")){
				RadioButton bt = (RadioButton) findViewById(R.id.linkbusChecked);
				bt.setChecked(true);
			}
			else if(type.equals("USB")){
				RadioButton bt = (RadioButton) findViewById(R.id.usbChecked);
				bt.setChecked(true);
			}
			else if(type.equals("BT")){
				RadioButton bt = (RadioButton) findViewById(R.id.bluetoothChecked);
				bt.setChecked(true);
			}
		}
	}
	
	@Override
	protected void onPause(){
	    super.onPause();
	    deviceSelect.clearCheck();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	
	private void initPreferences(){
		if(userPrefs==null){
			userPrefs = new UserPrefs(getSharedPreferences(MainActivity.sharedPreferencesName, Context.MODE_PRIVATE));
		}
	}
	
	private void addLinkbus(){
        Intent intent = new Intent(this, LinkbusAdd.class);
		startActivityForResult(intent, 0);
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Request = 0  //Add Linkbus
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String profilName = data.getStringExtra("profilName");
				Node devices = new Node(this.userPrefs, "Linkbus");
				String[] ipDeviceList = devices.getChildNames();
				if(ipDeviceList!=null){
					for(int i=0; i<ipDeviceList.length;i++){
						SimpleIPConfig dev = SimpleIPConfig.createFromNode(devices, ipDeviceList[i]);
						if(dev!=null && dev.getName().equals(profilName)){
							save("LB", dev.getName()+" - "+dev.getIpAsText());
							break;
						}
					}
				}
			}
		}
	}


	public void removeDeviceClickHandler(View v) {
		Object get = v.getTag();
		if(get!=null){
			String name = get.toString();
			initPreferences();
			Node devices = new Node(this.userPrefs, "Linkbus");
			String[] ipDeviceList = devices.getChildNames();
			if(ipDeviceList!=null){
				String shortName = null;
				for(int i=0; i<ipDeviceList.length;i++){
					SimpleIPConfig dev = SimpleIPConfig.createFromNode(devices, ipDeviceList[i]);
					if(dev!=null){
						if(name.equals(dev.getName()+" - "+dev.getIpAsText())){
							shortName = dev.getName();
							break;
						}
					}
				}
				if(shortName!=null){
					devices.removeChild(shortName);
	            	hideAllList();
	        	    listSelected("LB");
	        	    colorSelected();
				}
			}
		}
	}
	
	private void hideAllList(){
		linkbusDevices.setAdapter(null);
		justifyListViewHeightBasedOnChildren(linkbusDevices);
		linkbusAdd.setVisibility(View.GONE);
		usbDevices.setAdapter(null);
		justifyListViewHeightBasedOnChildren(usbDevices);
		usbEnable.setVisibility(View.GONE);
		bluetoothDevices.setAdapter(null);
		justifyListViewHeightBasedOnChildren(bluetoothDevices);
		bluetoothEnable.setVisibility(View.GONE);
	}
	
	private void listSelected(String type){
		initPreferences();
    	if(type.equals("LB")){
			Node devices = new Node(this.userPrefs, "Linkbus");
			String[] ipDeviceList = devices.getChildNames();
			if(ipDeviceList!=null){
				for(int i=0; i<ipDeviceList.length;i++){
					SimpleIPConfig dev = SimpleIPConfig.createFromNode(devices, ipDeviceList[i]);
					if(dev!=null){
						ipDeviceList[i] = dev.getName()+" - "+dev.getIpAsText();
					}
				}
				DeviceListAdapter list = new DeviceListAdapter(this, android.R.layout.simple_list_item_1, ipDeviceList);
				list.setDelete(true);
				linkbusDevices.setAdapter(list);
			}else{
				linkbusDevices.setAdapter(null);
			}
			justifyListViewHeightBasedOnChildren(linkbusDevices);
			linkbusAdd.setVisibility(View.VISIBLE);
    	}else if(type.equals("USB")){
			//Test if usb exist
			boolean isUSB = false;
			try {
				Class.forName("android.hardware.usb.UsbManager");
				isUSB = true;
			} catch( ClassNotFoundException e ) {
			}
			if(isUSB) {
				String[] listDevices = UartUsbOTG.list(getBaseContext());
				if (listDevices != null) {
					DeviceListAdapter list = new DeviceListAdapter(this, android.R.layout.simple_list_item_1, listDevices);
					usbDevices.setAdapter(list);
				} else {
					usbDevices.setAdapter(null);
				}
				justifyListViewHeightBasedOnChildren(usbDevices);
				if (USBOTGSystem.isEnabled() == false) {
					usbEnable.setVisibility(View.VISIBLE);
				}
			}else{
				usbEnable.setText(R.string.usb_not_found);
				usbEnable.setEnabled(false);
				usbEnable.setVisibility(View.VISIBLE);
			}
    	}else if(type.equals("BT")){
			String[] listDevices = UartBluetooth.list();
			if(listDevices!=null){
				DeviceListAdapter list = new DeviceListAdapter(this, android.R.layout.simple_list_item_1, listDevices);
				bluetoothDevices.setAdapter(list);
			}else{
				bluetoothDevices.setAdapter(null);
			}
			justifyListViewHeightBasedOnChildren(bluetoothDevices);
			//Check is bluetooth enabled
        	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        	if (mBluetoothAdapter == null) {
    			bluetoothEnable.setText(R.string.bt_not_found);
    			bluetoothEnable.setEnabled(false);
    			bluetoothEnable.setVisibility(View.VISIBLE);
        	}else if (!mBluetoothAdapter.isEnabled()) {
    			bluetoothEnable.setVisibility(View.VISIBLE);
        	} 
    	}
	}

	
	private void colorSelected(){
		initPreferences();
		String type = userPrefs.getString("selectedType");
		String name = userPrefs.getString("selectedName");
    	ListAdapter adapter = null;
    	try{
	    	if(type.equals("LB")){
		    	adapter = linkbusDevices.getAdapter();
	    	}else if(type.equals("USB")){
		    	adapter = usbDevices.getAdapter();
	    	}else if(type.equals("BT")){
		    	adapter = bluetoothDevices.getAdapter();
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	if(adapter!=null && adapter instanceof DeviceListAdapter){
    		DeviceListAdapter a = (DeviceListAdapter)adapter;
    		a.setSelected(name);
    		a.notifyDataSetChanged();
    	}
	}
	
	private void save(String type, String name){
		initPreferences();
        userPrefs.saveString("selectedType", type);
        userPrefs.saveString("selectedName", name);
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
