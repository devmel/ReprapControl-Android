package com.devmel.apps.reprapcontrol.tools;


import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.devmel.communication.IUart;
import com.devmel.devices.SimpleIP;
import com.devmel.devices.SimpleIPError;

public class GCodeControl {
	private final Vector<Command> commands = new Vector<Command>();
	public int timeoutms = 15000;
	private GCodeControl.Listener listener;
	private int bufferSize = 64;
	private Manager manager = null;
	private long busyUntil = 0;
	
	
	public boolean connect(final IUart device, boolean resetState){
		if(isConnected()){
			disconnect();
			if(manager!=null && manager.device == device){
				while(isConnected() == true){
					try {Thread.sleep(100);} catch (InterruptedException e) {}
				}
			}
		}
		if(device != null){
			manager = new Manager(device, resetState);
			return true;
		}
		return false;
	}
	
	public void disconnect(){
		manager.close();
	}

	public IUart getDevice(){
		if(isConnected()){
			return manager.device;
		}
		return null;
	}
	
	public boolean isConnected(){
		if(manager!=null){
			return manager.isAlive();
		}
		return false;
	}
	
	public void resetBuffer(){
		if(isConnected()){
			synchronized (commands) {
				Iterator<Command> i = commands.iterator();
				while (i.hasNext()) {
					Command cmd = i.next();
					if (cmd.sent < 0)
						i.remove();
				}
			}
		}else{
			commands.clear();
		}
	}

	public boolean isEmpty(){
		return (commands.size() != 0) ? false : true;
	}
	public boolean isBusy(){
		if(getCommandSize() > bufferSize * 2 || busyUntil > System.currentTimeMillis()) {
			System.err.println("isBusy now");
			return true;
		}
		return false;
	}
	
	public void setBufferSize(int size){
		this.bufferSize = size;
	}

	public void commandBusy(final int timeout){
		commands.clear();
/*		resetBuffer();
		Iterator<Command> i = commands.iterator();
		while (i.hasNext()) {
			Command cmd = i.next();
			if(cmd.sent > 0)
				cmd.sent = System.currentTimeMillis();
		}
*/		busyUntil = System.currentTimeMillis() + timeout;
	}
	public boolean command(final String cmd){
		boolean ret = false;
		if(isBusy() == false && cmd != null){
            String lines[] = cmd.split("[\\r\\n]+");
            for(String line:lines)
    			ret = commands.add(new Command(line));
			synchronized(commands){
				commands.notify();
			}
		}
		return ret;
	}
	
	public void setListener(GCodeControl.Listener listener){
		this.listener=listener;
	}

	public interface Listener{
		public void onConnect();
		public void onDisconnect();
        public void onOpen(boolean open);
        public void onDeviceException(IOException exception);
		public void onResend(final int line);
		public void onError(final String error);
		public void onEcho(final String echo);
		public void onMessage(final String command, final String msg);
	}

	private int getCommandSize(){
		int usage = 0;
		for (int i = 0; i < commands.size(); i++){
			Command cmd = commands.get(i);
			usage += cmd.command.length();
		}
		return usage;
	}
	
	private String commandsToString(Command[] cmds){
		StringBuffer ret = new StringBuffer();
		if(cmds!=null){
			for (int i = 0; i < cmds.length; i++){
				ret.append(cmds[i].command);
                ret.append("\n");
			}
		}
		return ret.toString();
	}

	private void commandsSent(Command[] cmds){
		if(cmds!=null){
			for (int i = 0; i < cmds.length; i++){
				cmds[i].sent = System.currentTimeMillis();
			}
		}
	}
	
	private void clearCommandsSent(){
		Iterator<Command> i = commands.iterator();
		while (i.hasNext()) {
			Command cmd = i.next();
			if(cmd.sent > 0)
				i.remove();
		}
	}


	private Command[] getNextCommands(int bufferSize){
		Vector<Command> next = new Vector<Command>();
		int usage = getBufferUsage();
		for (int i = 0; i < commands.size(); i++){
			Command cmd = commands.get(i);
			if(cmd.sent == -1){
				usage += cmd.command.length();
				if(usage < bufferSize){
					next.add(cmd);
				}else{
					break;
				}
			}
		}
		Command[] ret = new Command[next.size()];
		next.toArray(ret);
		return ret;
	}

	private int getBufferUsage(){
		int usage = 0;
		synchronized(commands) {
			for (int i = 0; i < commands.size(); i++) {
				Command cmd = commands.get(i);
				if (cmd.sent > 0) {
					usage += cmd.command.length();
				} else {
					break;
				}
			}
		}
		return usage;
	}
	
	private class Command{
		final String command;
		long sent = -1;
		
		Command(String command){
            this.command=command;
		}
	}

	
	private class Manager extends Thread{
		private final IUart device;
		private int nextErrors = 0;
		private boolean close = false;
		private boolean start;
		private int bypassOk = 0;
		
		Manager(IUart device, boolean resetState){
			this.device=device;
			this.start = !resetState;
			clearCommandsSent();
			this.start();
		}
		
		void close(){
			close = true;
			this.interrupt();
		}
		
		@Override
		public void run() {
			if(device==null)
				return ;
			LineReader in = null;
			try{
				//Connect
				if(device.open()){
                    if(listener!=null)
                        listener.onOpen(true);
					in = new LineReader(device.getInputStream());
					in.reset();
					//Wait for start
					long startTime = System.currentTimeMillis();
					while(start == false && (startTime+timeoutms)>System.currentTimeMillis()){
						processInput(in);
					}
					in.reset();
					if(start == true){
						if(listener!=null)
							listener.onConnect();
						//Manage Input/Output
						OutputStream out = device.getOutputStream();
						int bSize = bufferSize;
						while(device.isOpen() && close == false){
							//Send command if buffer available
							Command[] cmds = getNextCommands(bSize);
							if(cmds!=null && cmds.length > 0){
								String toSend = commandsToString(cmds);
//								System.err.print(">>"+bSize+" "+toSend);
								try{
									out.write(toSend.getBytes());
									out.flush();
									commandsSent(cmds);
								}catch(IOException e){
									SimpleIPError code = SimpleIP.getError(e);
									if(code!=SimpleIPError.SYNCHRONIZATION){
										throw e;
									}
								}
							}
							//Wait for read
							boolean wait = !processInput(in);
							if(wait){
								synchronized (commands){
									try {commands.wait(555);} catch (InterruptedException e) {}
								}
							}
							//Command timeout
							Command cmd = null;
							try {cmd = commands.firstElement();}catch(Exception e){}
							//Stop on command timeout
							if(cmd != null){
								long maxTimeout = System.currentTimeMillis() - timeoutms;
								if(cmd.sent > 0 && cmd.sent < maxTimeout) {
									throw new IOException("Command Timeout " + cmd.command);
								}
							}

							//Read error management
							nextErrors += in.getLastErrors();
							if(nextErrors == 0 && bSize < bufferSize){
								bSize = bufferSize;
								try {Thread.sleep(timeoutms/4);} catch (InterruptedException e) {}
							}else if(nextErrors > 0){
//								System.err.println("NextError :"+nextErrors);
								//Wait and resynchro
								try {Thread.sleep(timeoutms/2);} catch (InterruptedException e) {}
								clearCommandsSent();
								in.reset();
								nextErrors = in.getLastErrors();
								nextErrors = 0;
								bSize = bufferSize/2;
							}
						}
					}
				}else{
                    if(listener!=null)
                        listener.onOpen(false);
                }
			}catch(IOException e){
				if(listener!=null)
					listener.onDeviceException(e);
			}catch(Throwable e){
			}finally{
				//Disconnect
				device.close();
				if(in!=null)
					in.close();
				if(listener!=null)
					listener.onDisconnect();
			}
		}
		
		private boolean processInput(LineReader in){
			String next = in.nextLine();
			if(next!=null){
//				System.err.println("<<"+next);
				if(next.equalsIgnoreCase("start")){
					start = true;
				}
				else if(next.startsWith("ok")){
					String line = next.substring(2);
                    Command cmd = null;
					if(bypassOk <= 0) {
						try {cmd = commands.remove(0);}catch(Exception e){}
						bypassOk = 0;
					}else
						bypassOk--;
                    if(line != null && line.length()>0){
                        String command = "";
                        if(cmd != null)
                            command = cmd.command;
                        if(listener!=null)
                            listener.onMessage(command, line);
                    }
				}
				else if(next.startsWith("rs") || next.startsWith("Resend:")){
					String line = next.substring(2);
					if(next.startsWith("Resend:")){
						line = next.substring(7);
					}
					int lineNumber = -1;
					try{lineNumber = Integer.parseInt(line);}catch(Exception e){}
					if(listener!=null && lineNumber >= 0)
						listener.onResend(lineNumber);
					bypassOk++;
				}
				else if(next.startsWith("!!") || next.startsWith("Error:")){
					String line = next.substring(2);
					if(next.startsWith("Error:")){
						line = next.substring(6);
					}
					if(listener!=null)
						listener.onError(line);
				}
				else if(next.equalsIgnoreCase("Done saving file.")){
                    Command cmd = null;
					try {cmd = commands.remove(0);}catch(Exception e){}
                    String command = "";
                    if(cmd != null)
                        command = cmd.command;
					if(listener!=null)
						listener.onMessage(command, next);
				}
				else if(next.startsWith("echo:")){
					if(listener!=null)
						listener.onEcho(next.substring(5));
				}
				else{
                    Command cmd = null;
					try {cmd = commands.firstElement();}catch(Exception e){}
                    String command = "";
                    if(cmd != null)
                        command = cmd.command;
					if(listener!=null)
						listener.onMessage(command, next);
				}
				return true;
			}
			return false;
		}

	}
}
