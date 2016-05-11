package com.devmel.apps.reprapcontrol.tools;

import java.net.URI;

import com.devmel.tools.IPAddress;

public class SpUrlParser {
	private byte[] ip = null;
	private String password = null;
	
	public SpUrlParser(String text){
        URI lbUri = URI.create(text);
	    if(lbUri!=null){
	    	String scheme = lbUri.getScheme();
		    if(scheme!=null && scheme.equalsIgnoreCase("sp")){
		    	String lip = lbUri.getHost();
		    	if(lip!=null){
		    		lip = lip.replace("[", "");
		    		lip = lip.replace("]", "");
		    		ip = IPAddress.toBytes(lip);
		    		password = lbUri.getUserInfo();
		    	}
		    }
	    }
	    if(ip == null)
	    	throw new NullPointerException();
	}
	
	public byte[] getIp(){
		return ip.clone();
	}
	public String getIpAsText(){
		return IPAddress.fromBytes(ip);
	}
	public String getPassword(){
		return password;
	}

}
