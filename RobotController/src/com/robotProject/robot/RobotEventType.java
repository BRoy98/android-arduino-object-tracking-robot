package com.robotProject.robot;

public enum RobotEventType {
	EvtFrontSonar(0),
	EvtRearSonar(1),
	EvtSonarStop(2),
	EvtDistanceStop(3);
	
	public final byte id;

	RobotEventType(int id) {
		this.id = (byte)id;
	}
}