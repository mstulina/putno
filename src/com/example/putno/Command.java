package com.example.putno;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

public abstract class Command {
	protected final String ERROR_TAG = "ERROR";
	protected final String TEST_TAG = "TEST";
	private byte [] cmd = new byte[2];
	static byte[] request = new byte[8];//neka bude mista da ne bude tisno :Dlol
	long currValue = 0;	
	private OutputStream os = null;
	InputStream is = null;
	Timer timer = null;
	TimerTask task = null;
	private Lock lock = new ReentrantLock();

	
	Command(byte [] _cmd, OutputStream _os, InputStream _is, Lock _lock){
		this.cmd = _cmd;
		this.os = _os;
		this.is = _is;
		this.lock = _lock;
		task =  new TimerTask() {

            @Override
            public void run() {
            	execute();
            	Log.d(TEST_TAG, "Task swiched!");
            }
        };
		
		if (timer==null){
            timer = new Timer();
            timer.schedule(task, 0,200);
		}
		
	}
	
	public boolean execute (){
		if (os == null || is == null){
			stop();
			return false;
		}
		else{
			if (lock.tryLock()) {
				try {
					send();
//					try{ 
//						Thread.sleep(200);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					receive();
				} finally {
			        lock.unlock();
			        Log.d(TEST_TAG, "Unlocked");
				}
				
				return true;
			}else{
				Log.e(ERROR_TAG, "Lock is locked");
				return false;
			}
			
		}
		
	}
	
	private void send () { 
		synchronized (os) {
			try {
				os.write(cmd);
				os.flush();
				Log.d(TEST_TAG, "Command sent!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(ERROR_TAG, "IOException: "+e.toString());
				stop();
			}
		}
	}
	
	private void receive () { 
		synchronized (is) {
			try {
				Log.d(TEST_TAG, "Entered into receive method!");
				is.read(request);
				if (request[1] == cmd[1]){
					currValue= interpret();
					Log.d(TEST_TAG, "Data received real :"+(request[2]&0xff)+" and interpreted :"+currValue+"%");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(ERROR_TAG, "IOException: "+e.toString());
				stop();
			}
		}
	}
	
	public boolean getState(){
		if(is != null && os != null){
			return true;
		}else{
			return false;
		}
	}
	
	public abstract int interpret();
	
	public void stop(){
		if(task != null){
			task.cancel();
			timer.cancel();
			timer.purge();
		}
	}
	
}
