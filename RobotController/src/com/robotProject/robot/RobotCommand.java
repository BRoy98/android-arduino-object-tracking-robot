package com.robotProject.robot;

public enum RobotCommand {
	CmdMoveForward(0),
	CmdMoveForwardDistance(1),
	CmdMoveBackward(2),
	CmdMoveBackwardDistance(3),
	CmdSpinLeft(4),
	CmdSpinLeftDistance(5),
	CmdSpinRight(6),
	CmdSpinRightDistance(7),
	CmdStop(8),
	CmdMoveCameraVert(9),
	CmdMoveCameraHor(10);
	
	public final byte id;

	RobotCommand(int id) {
		this.id = (byte)id;
	}
}