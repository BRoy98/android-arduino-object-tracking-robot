package com.robotProject.adkutils;

/**
 * @author Mejdl Safran & Steve Haar
 * 
 */
public abstract class UsbListener {
	private byte[] signalsToListenFor;
	public byte[] getSignalsToListenFor() {
		return signalsToListenFor;
	}
	
	protected void setSignalsToListenFor(byte[] signalsToListenFor) {
		this.signalsToListenFor = signalsToListenFor;
	}
	
	public abstract void usbSignalReceived(UsbEvent e);
	
	protected UsbListener() { }
	
	public UsbListener(byte[] signalsToListenFor){
		this.setSignalsToListenFor(signalsToListenFor);
	}

	public UsbListener(byte signalToListenFor){
		byte[] signals = new byte[1];
		signals[0] = signalToListenFor;
		this.setSignalsToListenFor(signals);
	}
}