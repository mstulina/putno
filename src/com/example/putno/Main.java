package com.example.putno;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.putno.gauge.GaugeView;

public class Main extends Activity {

	int size = 2;
	byte[] pidRq = new byte[size];
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	LinkedList<Byte> request = null;
	LinkedList<Command> cmdList = null;
	 
	Socket socket = null;
	FuelLevelCommand flc = null;
	EngineWaterTemperature ewt = null;
	SocketConnect sc = null;
	ToggleButton tbtn = null;
	Button btn = null;
	
	Timer timer = null;

	
	private GaugeView mGaugeView1 = null;
	private GaugeView mGaugeView2 = null;
   
	@Override
   public void onCreate(Bundle savedInstanceState) {
 
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      

      mGaugeView1 = (GaugeView) findViewById(R.id.gauge_view1);
      mGaugeView1.setTargetValue(0);
      mGaugeView2 = (GaugeView) findViewById(R.id.gauge_view2);
      mGaugeView2.setTargetValue(0);
      initList();
      
      init();
	}
	
	
      
   private void initList() {
	// TODO Auto-generated method stub
	request = new LinkedList<Byte>();
	request.add((byte)0);
}



private void init() {
	// TODO Auto-generated method stub
	   pidRq[0]=0x01;
	   pidRq[1]=0x2f;

	   tbtn = (ToggleButton) findViewById(R.id.toggleButton);
	   tbtn.setOnClickListener(new View.OnClickListener() {
		
	   @Override
	   public void onClick(View v) {
			// TODO Auto-generated method stub
			if (tbtn.isChecked()) {//to je onaj s kojim se pokrene konekcija
				sc = new SocketConnect();
				sc.execute();
	        } else {
	        	
	        	try{
	        		if(sc != null){
	        			sc.cancel(true);
	        		}
	        	}catch (Exception e) {
					// TODO: handle exception
	        		Log.e("SOCKETCONNECT", "IOException----ERRROR---sc.cancel "+e.toString());
				}
	        	if(flc !=null){
		     		   flc.stop();
		     		   Log.d("COMMAND", "Stopped on click");
		     	   	}
	        	if(ewt !=null){
		     		   ewt.stop();
		     		   Log.d("COMMAND", "Stopped on click");
		     	   	}
	        	if(socket != null){
		        	try {
						socket.close();
						Log.d("SOCKET", "Closed on click");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e("SOCKET", "IOException----ERRROR----socket.close()"+e.toString());
					}
	        	}
	        }
		}
	   });
	   btn = (Button) findViewById(R.id.button1);
	   
	   btn.setOnClickListener(new View.OnClickListener() {//s ovime se posalje poruka kao, znaci sa onim gore togglebutton nema problema? ne, ok
		
	   	@Override
	   	public void onClick(View v) {
			// TODO Auto-generated method stub
	   		if (socket != null){
				try {
					flc = new FuelLevelCommand(socket.getOutputStream(), socket.getInputStream(), lock);
					ewt = new EngineWaterTemperature(socket.getOutputStream(), socket.getInputStream(), lock);
					
					if (timer==null){
			            timer = new Timer();
			            timer.schedule( new TimerTask() {

			                @Override
			                public void run() {
			                	runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										
					                	mGaugeView1.setTargetValue(flc.interpret());
					                	mGaugeView2.setTargetValue(ewt.interpret());
									}
								});
			                }
			                }, 0,1000);
			        }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("SOCKET", "IOException new FuelLevelCommand(socket.getOutputStream(), socket.getInputStream()); "+e.toString());
				}
	   		}else{
	   			Toast.makeText(getApplicationContext(), "Please turn on server!", Toast.LENGTH_SHORT).show();
	   		}
	   		
		}
	});
	      
   }


   private class SocketConnect extends AsyncTask<Void, Void , Void> {


		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			
			try {
				socket = new Socket("10.0.2.2",4567);
				//For precaution
				Thread.sleep(100);
//				flc = new FuelLevelCommand(socket.getOutputStream(), socket.getInputStream());
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				Log.e("ERROR", "UnknownHostException: "+e1.toString());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				Log.e("ERROR", "IOException: "+e1.toString());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.e("ERROR", "InterruptedException: "+e.toString());			
			}
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			Log.d("TEST","Command object is set!");
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			try {
				if (socket != null)
					socket.close();
				   	Log.d("SOCKET", "Closed on post cancelled");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("SOCKET", "IOException"+e.toString());
			}
		}
		
	}
   
   
   @Override
   protected void onDestroy() {
   	// TODO Auto-generated method stub
   	   super.onDestroy();
   	   
   	   if(flc !=null){
   		   flc.stop();
   		   Log.d("COMMAND", "Stopped on destroy");
   	   }
   	if(ewt !=null){
		   ewt.stop();
		   Log.d("COMMAND", "Stopped on destroy");
	   }
   	   
   	   if(socket!=null){
   		   try {
   			   socket.close();
   			   Log.d("SOCKET", "Closed on destroy");
   			} catch (IOException e) {
   				// TODO Auto-generated catch block
   				Log.e("SOCKET", "IOException"+e.toString());
   			}
   	   }
   }
   
}

