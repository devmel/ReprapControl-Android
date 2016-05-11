package com.devmel.apps.reprapcontrol;

import java.net.URI;

import com.devmel.apps.reprapcontrol.tools.SpUrlParser;
import com.devmel.storage.Node;
import com.devmel.storage.SimpleIPConfig;
import com.devmel.storage.android.UserPrefs;
import com.devmel.tools.Hexadecimal;
import com.devmel.tools.IPAddress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LinkbusAdd extends Activity {
	private final String defaultName = "LinkBus_";
	private final static String defaultIP = "fe80::dcf6:e5ff:fe";
	private Button scan;
	private EditText profilName;
	private EditText lbLocalIP;
	private EditText lbPassword;
	private Button validate;
	UserPrefs userPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_linkbus_add);
		
		scan = (Button) findViewById(R.id.scan);
		profilName = (EditText) findViewById(R.id.profilName);
		lbLocalIP = (EditText) findViewById(R.id.lbLocalIP);
		lbPassword = (EditText) findViewById(R.id.lbPassword);
		validate = (Button) findViewById(R.id.validate);

		profilName.setText(defaultName);
		lbLocalIP.setText(defaultIP);


		scan.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				try {
				    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				    startActivityForResult(intent, 0);
				} catch (Exception e) {
//					e.printStackTrace();
				    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
				    Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
				    startActivity(marketIntent);
				}
			}
		});
		
		validate.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//Manage information
				String err = null;
				initPreferences();
				Node devices = new Node(userPrefs, "Linkbus");
				if (devices.isChildExist(profilName.getText().toString())) {
					err = getString(R.string.errorNameExists);
				} else {
					try {
						final String name = profilName.getText().toString();
						final String localIP = lbLocalIP.getText().toString();
						final String password = lbPassword.getText().toString();
						byte[] ip = IPAddress.toBytes(localIP);
						if(name==null || name.length()==0){
							err = getString(R.string.errorNameInvalid);
						}else if(ip==null){
							err = getString(R.string.errorIPInvalid);
						}else{
							SimpleIPConfig device = new SimpleIPConfig(name);
							device.setIp(ip);
                            if(password!=null && password.length() > 0) {
                                device.setPassword(password);
                            }
							device.setTimeout(8000);
							device.save(devices);
						}
					} catch (Exception e) {
						e.printStackTrace();
                        err = getString(R.string.errorUnknown);
					}
				}
				if (err!=null) {
            	    Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
				}else{
					Intent resultIntent = new Intent();
					if(profilName != null){
						resultIntent.putExtra("profilName", profilName.getText().toString());
					}
					setResult(Activity.RESULT_OK, resultIntent);
					finish();
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {           
	    super.onActivityResult(requestCode, resultCode, data);
	    //Request = 0  //QR Scan sp://
	    if (requestCode == 0) {
	    	boolean scanOk = false;
	        if (resultCode == RESULT_OK) {
				SpUrlParser deviceInfo = new SpUrlParser(data.getStringExtra("SCAN_RESULT"));
				if(deviceInfo!=null){
					if(profilName.getText()!=null && defaultName.equals(profilName.getText().toString())){
						String ip = Hexadecimal.fromBytes(deviceInfo.getIp());
						if(ip!=null && ip.length()>6)
							profilName.setText(defaultName+ip.substring(ip.length()-6));
					}
					lbLocalIP.setText(deviceInfo.getIpAsText());
					lbPassword.setText(deviceInfo.getPassword());
					scanOk=true;
				}
			    if(scanOk==true){
            	    Toast.makeText(getApplicationContext(), getString(R.string.okScan), Toast.LENGTH_LONG).show();
			    }else{
            	    Toast.makeText(getApplicationContext(), getString(R.string.errorScan), Toast.LENGTH_LONG).show();
			    }
	        }
	    }
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

}
