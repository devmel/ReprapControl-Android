package com.devmel.apps.reprapcontrol.view;

import com.devmel.apps.reprapcontrol.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class DeviceListAdapter extends ArrayAdapter<String> {
	private String selected = null;
	private boolean isDeletable = false;;
	private Context context;

    public DeviceListAdapter(Context context, int textViewResourceId , String[] list ) {
        super(context, textViewResourceId, list);
        this.context = context;
    }
    
    public void setSelected(String name){
    	selected = name;
    }
    
    public void setDelete(boolean deletable){
    	isDeletable = deletable;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view =  super.getView(position, convertView, parent);
        String name = super.getItem(position);
        if(name.equals(selected)){
        	if(isDeletable){
	           	LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	           	convertView = mInflater.inflate(R.layout.devices_list_item, parent, false);
	        	TextView nameView = ((TextView) convertView.findViewById(R.id.nameDevice));
	        	ImageButton deleteView = ((ImageButton) convertView.findViewById(R.id.deleteDevice));
	        	nameView.setText(name);
	        	deleteView.setTag(name);
	        	convertView.setBackgroundColor(Color.LTGRAY);
	        	return convertView;
        	}else{
        		view.setBackgroundColor(Color.LTGRAY);
        	}
        }else{
        	view.setBackgroundColor(Color.TRANSPARENT);
        }
        return view;
    }

}
