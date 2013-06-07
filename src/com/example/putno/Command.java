package com.example.putno;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	
	Command(byte [] _cmd, OutputStream _os, InputStream _is, ReadWriteLock _lock){
		this.cmd = _cmd;
		this.os = _os;
		this.is = _is;
		this.lock = _lock;
		task =  new TimerTask() {

            @Override
            public void run() {
            	execute();
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

	        lock.writeLock().lock();
			try{ 
				send();
			}finally{
				lock.writeLock().unlock();
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			lock.readLock().lock();
			try{
				receive();
			}finally{
				lock.readLock().unlock();
			}
			return true;
		}
		
	}
	
	private void send () { 

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
	
	private void receive () { 
		try {
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
