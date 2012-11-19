package com.robotProject.adkutils;

import java.io.*;
import java.util.*;
import android.hardware.usb.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;

/**
 * @author Mejdl Safran & Steve Haar
 * Facilitates two way communication between a USB device attached to the Android device.
 * This was written for and tested on an Arduino microcontroller.
 * Messages passed to the USB device should be a byte with an optional two ints.
 * Messages received from the USB device should be in the form of a single 32 bit integer.
 */
public abstract class UsbBroadcastReceiver extends BroadcastReceiver {
	private List<UsbListener> listeners = new ArrayList<UsbListener>();
	private boolean mPermissionRequestPending;
	private UsbAccessory mAccessory;	
	private ParcelFileDescriptor mFileDescriptor;
    private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;    
    private PendingIntent mPermissionIntent;
    private UsbManager mUsbManager;
    private Context mContext;
    private String TAG = this.getClass().getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	
    public UsbBroadcastReceiver(Context context) {
    	this.mContext = context;
    }
    
    public void open(){
    	mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(UsbBroadcastReceiver.ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(UsbBroadcastReceiver.ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        mContext.registerReceiver(this, filter);
    }
    
	@Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (UsbBroadcastReceiver.ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
            	UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    openAccessory(accessory);
                } else {
                    Log.d(TAG, "permission denied for accessory " + accessory);
                }
                mPermissionRequestPending = false;
            }
        } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
        	UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            if (accessory != null && accessory.equals(mAccessory)) {
                suspend();
            }
        }
    }
	
	public void suspend() {
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        }
        catch (IOException e) {
        }
        finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }
	
	public void resume(){
		if (mInputStream != null && mOutputStream != null) {
            return;
        }

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (this) {
                    if (!mPermissionRequestPending) {
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "mAccessory is null");
        }
	}
    
    public void close(){
    	mContext.unregisterReceiver(this);
   }
    
    public synchronized void addUsbEventListener(UsbListener listener)  {
    	listeners.add(listener);
    }
    
    public synchronized void removeUsbEventListener(UsbListener listener)   {
    	listeners.remove(listener);
    }
    
    private void openAccessory(UsbAccessory accessory) {
    	mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            Thread thread = new Thread(usbSignalReceived);
            thread.start();
            Log.d(TAG, "accessory opened");
        } else {
            Log.d(TAG, "accessory open fail");
        }
    }
    
    protected void sendMessage(byte command)
    {
		if (mOutputStream != null){
			try{
				byte[] buffer = new byte[1];
		    	buffer[0] = command;
				mOutputStream.write(buffer);
			}
			catch(Exception e) {
				
			}
		}
    }
    
    protected void sendMessage(byte command, int value)
    {
		if (mOutputStream != null){
			try{
				byte[] buffer = new byte[5];
				byte[] intAsBytes = getBytesFromInt(value);
		    	buffer[0] = command;
		    	buffer[1] = intAsBytes[0];
		    	buffer[2] = intAsBytes[1];
		    	buffer[3] = intAsBytes[2];
		    	buffer[4] = intAsBytes[3];
				mOutputStream.write(buffer);
			}
			catch(Exception e) {
				
			}
		}
    }
    
    protected void sendMessage(byte command, int value1, int value2)
    {
		if (mOutputStream != null){
			try{
				byte[] buffer = new byte[9];
				byte[] intAsBytes1 = getBytesFromInt(value1);
				byte[] intAsBytes2 = getBytesFromInt(value2);
		    	buffer[0] = command;
		    	buffer[1] = intAsBytes1[0];
		    	buffer[2] = intAsBytes1[1];
		    	buffer[3] = intAsBytes1[2];
		    	buffer[4] = intAsBytes1[3];
		    	buffer[5] = intAsBytes2[0];
		    	buffer[6] = intAsBytes2[1];
		    	buffer[7] = intAsBytes2[2];
		    	buffer[8] = intAsBytes2[3];
				mOutputStream.write(buffer);
			}
			catch(Exception e) {
				
			}
		}
    }
    
    private Runnable usbSignalReceived = new Runnable(){
    	public void run(){
    		byte[] buffer = new byte[5];
    		
    		while (true) {
    			try {
    				mInputStream.read(buffer, 0, 5);
    				byte signal = buffer[0];
    	    		
    	    		int value = getIntFromBytes(buffer[1], buffer[2], buffer[3], buffer[4]);
            		UsbEvent event = new UsbEvent(this, value);
                	for (UsbListener listener : listeners){
                		for (byte b : listener.getSignalsToListenFor()){
                			if (b == signal){
                				listener.usbSignalReceived(event);
                				break;
                			}
                		}
                	}
    			}
    			catch (IOException e) {
    				break;
    			}
    		}
    	}
    };

	private int getIntFromBytes(byte b1, byte b2, byte b3, byte b4) {
		int value = 0;
		value += b1;
		value <<= 8;
		value += b2;
		value <<= 8;
		value += b3;
		value <<= 8;
		value += b4;
		
		if (value < 0) {
			value += 256;
		}
		
		return value;
	}
	
	private byte[] getBytesFromInt(int i) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (i >> 24);
		bytes[1] = (byte) (i >> 16);
		bytes[2] = (byte) (i >> 8);
		bytes[3] = (byte) i;		
		return bytes;
	}
}