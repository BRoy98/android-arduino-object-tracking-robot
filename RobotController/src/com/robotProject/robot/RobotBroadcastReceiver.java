package com.robotProject.robot;

import android.content.Context;

import com.robotProject.adkutils.UsbBroadcastReceiver;

public class RobotBroadcastReceiver extends UsbBroadcastReceiver {
	
	public RobotBroadcastReceiver(Context context) {
		super(context);
	}
	
	public void sendMessage(RobotCommand command) {
		super.sendMessage(command.id);
	}
	
	public void sendMessage(RobotCommand command, int value) {
		super.sendMessage(command.id, value);
	}
	
	public void sendMessage(RobotCommand command, int value1, int value2) {
		super.sendMessage(command.id, value1, value2);
	}
}
