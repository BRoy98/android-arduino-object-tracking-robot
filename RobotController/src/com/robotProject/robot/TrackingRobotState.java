package com.robotProject.robot;

public enum TrackingRobotState {
	NoMove(0),
	MoveCameraUpDown(1),
	DriveLeftRight(2),
	DriveForwardBackwards(3),
	Lost(4);
	
	public final int id;

	TrackingRobotState(int id) {
		this.id = id;
	}
}
