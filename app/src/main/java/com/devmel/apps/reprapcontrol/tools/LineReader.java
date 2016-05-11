package com.devmel.apps.reprapcontrol.tools;

import com.devmel.devices.SimpleIP;
import com.devmel.devices.SimpleIPError;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class LineReader extends Thread{
	private final InputStream inStream;
	private boolean running = false;
	private Vector<String> buffer = new Vector<String>();
	private int error = 0;
	
	public LineReader(InputStream inStream){
		this.inStream = inStream;
		this.start();
	}
	
	public void reset(){
		try{
			inStream.reset();
		}catch(IOException e){
//			e.printStackTrace();	
		}
		buffer.clear();
	}
	
	public String nextLine(){
		if (buffer.size()>0){
			return buffer.remove(0);
		}
		return null;
	}
	
	public int getLastErrors(){
		int err = error;
		error = 0;
		return err;
	}
	
	public boolean isRunning(){
		return running;
	}
	
	public void close(){
		running = false;
		this.interrupt();
	}

	
	@Override
	public void run(){
		running = true;
		try {
			String inLine = "";
			int c = -1;
			while (running == true) {
				c = -1;
				// Search line
				try {
					c = inStream.read();
				} catch (IOException e) {
					if(e != null && !e.getMessage().contains("timeout")){
						error++;
					}
					Thread.sleep(10);
				}
				if (c != -1) {
					if (c == '\r' || c == '\n') {
						if (inLine.length()>0){
							buffer.add(inLine);
							inLine = "";
						}
					}else{
						inLine += new String(new byte[] { (byte) c });
					}
				}
			}
		} catch (Exception e) {
		//	 e.printStackTrace();
		}
		running = false;
	}
}
