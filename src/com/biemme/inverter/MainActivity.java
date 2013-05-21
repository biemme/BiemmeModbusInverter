package com.biemme.inverter;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

//import com.biemme.iodemo.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnSeekBarChangeListener{
	private TextView tx1,current,freq,voltage;
	private Button bt1;
	private Button btW, btnStopRef, testRead;
	private long fid = 0;
	private Timer tR;
	private Timer tW;
	private SeekBar sb;
	private ProgressBar pb;
	private ImageView bulb,back,forward, input1, input2;
	PowerManager pm;
	PowerManager.WakeLock wl;
	
	private Button btnAcc1, btnAcc2, btnSpe1, btnSpe2;
	private CheckBox chkBulb1;
	
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		wl.release();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		btnAcc1 = (Button) findViewById(R.id.btnAccensione1);
		btnAcc2 = (Button) findViewById(R.id.btnAccensione2);
		btnSpe1 = (Button) findViewById(R.id.btnSpegnimento1);
		btnSpe2 = (Button) findViewById(R.id.btnSpegnimento2);
		chkBulb1 = (CheckBox) findViewById(R.id.chkBulb);
		chkBulb1.setOnClickListener(this);
		
		btnAcc1.setOnClickListener(this);
		btnAcc2.setOnClickListener(this);
		btnSpe1.setOnClickListener(this);
		btnSpe2.setOnClickListener(this);
		
		sb = (SeekBar)findViewById(R.id.seekBar1);
		sb.setOnSeekBarChangeListener(this);
		
		pb =(ProgressBar)findViewById(R.id.progressBar1);
		
		tx1 = (TextView) findViewById(R.id.textView1);
		current = (TextView) findViewById(R.id.txtCurr);
		freq = (TextView) findViewById(R.id.txtFreq);
		voltage = (TextView) findViewById(R.id.txtVolt);
		
		
		bt1 = (Button)findViewById(R.id.button1);
		btW = (Button)findViewById(R.id.btnWrite1);
		btnStopRef = (Button)findViewById(R.id.btnStopRefresh);
		testRead = (Button)findViewById(R.id.testRead);
		testRead.setOnClickListener(this);
		bt1.setOnClickListener(this);
		btW.setOnClickListener(this);
		btnStopRef.setOnClickListener(this);
		
		bulb = (ImageView) findViewById(R.id.imageView2);
		back = (ImageView) findViewById(R.id.imageView5);
		forward = (ImageView) findViewById(R.id.imageView6);
		input1 = (ImageView)findViewById(R.id.imgInput1);
		input2 = (ImageView)findViewById(R.id.imgInput2);
		
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
		wl.acquire();
		
		back.setVisibility(View.INVISIBLE);
		forward.setVisibility(View.INVISIBLE);
	
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (fid == 0){
			
            fid = ModbusLib.openCom();
            Log.d("uart=",String.valueOf(fid));
        }
		if (tR!=null){
			tR.cancel();
			tR.notify();
			tR=null;
		}
		DelayedRead(1,4,4,0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.button1){
			if (tR!=null){
				tR.cancel();
				tR.notify();
				tR=null;
			}
			DelayedRead(1,4,1,0);
			
		}else if (v.getId()==R.id.btnStopRefresh){
			if (tR!=null){
				tR.cancel();
				tR.purge();
				tR=null;
			}
		}else if (v.getId()==R.id.btnAccensione1){
			if (tW!=null){
				tW.cancel();
				tW.purge();
				tW=null;
			}
			forward.setVisibility(View.VISIBLE);
			DelayedWrite(1,26,8192,0);
		}
		else if (v.getId()==R.id.btnSpegnimento1){
			if (tW!=null){
				tW.cancel();
				tW.purge();
				tW=null;
			}
			
			DelayedWrite(1,1,8192,0);
		}
		else if (v.getId()==R.id.btnAccensione2){
			if (tW!=null){
				tW.cancel();
				tW.purge();
				tW=null;
			}
			
			DelayedWrite(1,34,8192,0);
		}
		else if (v.getId()==R.id.btnSpegnimento2){
			if (tW!=null){
				tW.cancel();
				tW.purge();
				tW=null;
			}
			DelayedWrite(1,0,2,0);
		}else if (v.getId()==R.id.chkBulb){
			if (tW!=null){
				tW.cancel();
				tW.purge();
				tW=null;
			}
			CheckBox chkref = (CheckBox)v;
			if (chkref.isChecked()==true){
				bulb.setVisibility(View.VISIBLE);
				DelayedWrite(1,64,8192,0);
				
			}else{
				DelayedWrite(1,0,8192,0);
				bulb.setVisibility(View.INVISIBLE);
			}
			
		}
	}
	
	private void DelayedRead(final int device_id, final int starting_address, final int registers, final long delay){
		if (tR!=null){
			tR.cancel();
			tR.purge();
		}
		TimerTask tt=new TimerTask() {
			
			public void run() {
	            runOnUiThread(new Runnable() {
	                public void run() {
	                	//execute background modbus writes
	                	new ReadRegisters().execute(device_id, starting_address,registers);
	                }
	            });
	        }
		};
		
		tR=new Timer();
		
		tR.scheduleAtFixedRate(tt, delay, 1800);
		
	}
	
	
	private void DelayedWrite(final int address, final int value, final int starting_address, final long delay){
		if (tW!=null){
			tW.cancel();
			tW.purge();
		}
		TimerTask tt=new TimerTask() {
			
			public void run() {
	            runOnUiThread(new Runnable() {
	                public void run() {
	                	//execute background modbus writes
	                	new WriteRegisters().execute(address, value, starting_address);
	                }
	            });
	        }
		};
		tW=new Timer();
		
		tW.schedule(tt, delay);
		
	}
	
	private class ReadRegisters extends AsyncTask<Integer, Void, int[]>{
		private int[] holdingRegs,holdingRegs1;
		@Override
		protected void onPostExecute(int[] result) {
			if (result!=null){
				
				DecimalFormat df = new DecimalFormat("#.0");
				freq.setText(String.valueOf(df.format((double)result[0]/10.0)));
				current.setText(String.valueOf(df.format((double)result[2]/100.0)));
	            voltage.setText(String.valueOf(df.format((double)result[3]/10.0)));
				
	            int val_1 = (result[1])/10 ;
				pb.setProgress(val_1); 

				// the fourth item is a 16 bit register, we use a bitmask to test whether a single bit is set or not 
				// please see Allen-bradley inverter manual
				
				int bitmask = 0x1000;
		        int val = result[4];
		        Log.d("bit test", String.valueOf(val)+","+String.valueOf(val & bitmask));		        
		        if ((val & bitmask) == bitmask){
		        	input1.setImageResource(R.drawable.input_on);
		        }else{
		        	input1.setImageResource(R.drawable.input_off);
		        }
		        
		        bitmask = 0x2000;
		        
		        if ((val & bitmask) == bitmask){
		        	input2.setImageResource(R.drawable.input_on);
		        }else{
		        	input2.setImageResource(R.drawable.input_off);
		        }
		        
		        bitmask = 0x8;
		        
		        if ((val & bitmask) == bitmask){
		        	
		        	back.setVisibility(View.INVISIBLE);
					forward.setVisibility(View.VISIBLE);
		        }else{
		        	
		        	back.setVisibility(View.VISIBLE);
					forward.setVisibility(View.INVISIBLE);
		        }
		        
		        bitmask = 0x2;
		        
		        if ((val & bitmask) != bitmask){
		        	back.setVisibility(View.INVISIBLE);
					forward.setVisibility(View.INVISIBLE);
		        }
		        
			}
		}
		
		@Override
		protected int[] doInBackground(Integer... params) {
			
			try{
					
					int node_to_write 		= params[0];
					int starting_address 	= 1;		//it should be: int no_of_registers = params[1];
					int no_of_registers 	= 4;		//it should be: int no_of_registers = params[2];
					
					int offset 				= no_of_registers;
					long bytes_received1 	= 0;
					long bytes_received2 	= 0;
					
					//registers that stores the read values
					holdingRegs = new int[5];	
					holdingRegs1 = new int[1];
					
					int retries = 0, max_retries = 0;
					
					do{						
						String s;
						
						if (bytes_received1 <= 7){
							
							bytes_received1 = ModbusLib.ReadHoldingRegisters((int)fid,node_to_write,starting_address,no_of_registers, holdingRegs);
							Log.d("br1",String.valueOf(bytes_received1));
							if (bytes_received1 >= 7){
								s="("+String.valueOf(bytes_received1) + ")";
		    					for (int i=0; i<no_of_registers; i++){
		    						s+=String.valueOf(holdingRegs[i]);
		    						s+=",";
		    					}
		    					Log.d("modbus3#1", s);
							}
						}
						
						//specific to Alley-bradey power inverter 
						
						starting_address = 8448;
						no_of_registers = 1;
						
						if (bytes_received2 <= 7){
							
							bytes_received2 = ModbusLib.ReadHoldingRegisters((int)fid,node_to_write,starting_address,no_of_registers, holdingRegs1);
							Log.d("br1",String.valueOf(bytes_received2));
							if (bytes_received2 >= 7){
								s="("+String.valueOf(bytes_received2) + ")";
		    					for (int i=0; i<no_of_registers; i++){
		    						s+=String.valueOf(holdingRegs1[i]);
		    						s+=",";
		    					}
		    					Log.d("modbus3#1", s);
							}
						}
						
						if (bytes_received1 >= 7 && bytes_received2 >=7){
							holdingRegs[offset]=holdingRegs1[0];
							return holdingRegs; 	
						}
						retries++;
						
					}while (retries<max_retries);
					return null;
					
    		}catch(Throwable t){
    			Log.d("modbusERR", t.toString());
    		}
			return null;
		}

	}
	
	private class WriteRegisters extends AsyncTask<Integer, Void, int[]>{
		private int[] holdingRegs;
		@Override
		protected void onPostExecute(int[] result) {
			if (result!=null){
					
				//this part can access the graphical objects of the activity
			}
		}
		
		
		@Override
		protected int[] doInBackground(Integer... params) {
			
			try{
				
					int no_of_registers 	= 1; 
					int node_to_write 		= params[0];
					int starting_address 	= params[2];
					
					holdingRegs 			= new int[10];
					holdingRegs[0]			= params[1];
					
					long bytes_received1=0;
					int retries = 0, max_retries = 5;
					
					do{						
						
						String s;
						if (bytes_received1 <= 0){
							bytes_received1 = ModbusLib.WriteMultipleRegisters((int)fid, node_to_write, starting_address, no_of_registers, holdingRegs);
							if (bytes_received1 >= 7){
								
								s="("+String.valueOf(bytes_received1) + ")";
		    					for (int i=0; i<bytes_received1; i++){
		    						s+=String.valueOf(holdingRegs[i]);
		    						s+=",";
		    					}
		    					Log.d("modbus16#1", s);
							}
						}
						

						retries++;
						
						if (bytes_received1 >= 7){
							return holdingRegs; 
						}
					}while (retries<max_retries);
					
					
					return null;
    		}catch(Throwable t){
    			Log.d("modbusERR", t.toString());
    		}
			return null;
		}	
	}

	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
	
		
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
		if (seekBar.getId()==R.id.seekBar1){
			// when a change in the seekbar is detected, writes the value to the inverter
			Log.d("seekbar", "val");
			int mProgressStatus = 10;
            
            pb.setProgress(mProgressStatus); 
			DelayedWrite(1,seekBar.getProgress(),8193,0);
			
		}
	}

}
