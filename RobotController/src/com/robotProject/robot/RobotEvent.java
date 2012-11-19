package com.robotProject.robot;

import com.robotProject.adkutils.UsbEvent;

public class RobotEvent extends UsbEvent {
	private static final long serialVersionUID = 1L;

	public RobotEvent(UsbEvent e) {
		super(e.getSource(), e.getValue());
	}
	
}
