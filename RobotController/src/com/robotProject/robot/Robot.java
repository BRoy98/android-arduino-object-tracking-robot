package com.robotProject.robot;

import org.opencv.core.Rect;

import android.content.Context;

/**
 * @author Mejdl Safran & Steve Haar
 * Facilitates controlling a robot built with an Arduino microcontroller.
 * RobotListeners can be added to listen for the sonar signals from the robot.
 */
public class Robot {
	protected Context context;
	protected RobotBroadcastReceiver robotReceiver;
	protected int CameraVertPosition = 90;
	protected int CameraHorPosition = 90;
	protected int MaxCameraVertPosition = 130;
	protected int MaxCameraHorPosition = 180;
	protected int MinCameraVertPosition = 50;
	protected int MinCameraHorPosition = 0;
	protected Rect NoMoveRect;
	
	public Robot(Context context) {
		this.context = context;
		robotReceiver = new RobotBroadcastReceiver(this.context);
		robotReceiver.open();
	}
	
	public void setNoMoveRect(Rect rect) {
		this.NoMoveRect = rect;
	}
	
    public void resume() {
        robotReceiver.resume();
    }
    
    public void pause() {
        robotReceiver.suspend();
    }
    
    public void destroy() {
        robotReceiver.close();
    }
    
    public void addEventListener(RobotListener listener) {
    	robotReceiver.addUsbEventListener(listener);
    }
    
    public void removeEventListener(RobotListener listener) {
    	robotReceiver.removeUsbEventListener(listener);
    }
    
    public int getCameraVertPosition() {
    	return CameraVertPosition;
    }
    
    public int getCameraHorPosition() {
    	return CameraHorPosition;
    }
    
    public int getMaxCameraHorPosition() {
    	return MaxCameraHorPosition;
    }
    
    public int getMaxCameraVertPosition() {
    	return MaxCameraVertPosition;
    }
    
    public int getMinCameraHorPosition() {
    	return MinCameraHorPosition;
    }
    
    public int getMinCameraVertPosition() {
    	return MinCameraVertPosition;
    }
    
    public void MoveForward(int speed) {
    	if (speed >= 0 && speed <= 100)
    	{
    		robotReceiver.sendMessage(RobotCommand.CmdMoveForward, speed);
    	}
    }
    
    public void MoveForward(int speed, int centimeters) {
    	if (speed >= 0 && speed <= 100 && centimeters >= 0)
    	{
    		robotReceiver.sendMessage(RobotCommand.CmdMoveForwardDistance, speed, centimeters);
    	}
    }
    
    public void MoveBackward(int speed) {
    	if (speed >= 0 && speed <= 100)
    	{
    		robotReceiver.sendMessage(RobotCommand.CmdMoveBackward, speed);
    	}
    }
    
    public void MoveBackward(int speed, int centimeters) {
    	if (speed >= 0 && speed <= 100 && centimeters >= 0)
    	{
    		robotReceiver.sendMessage(RobotCommand.CmdMoveBackwardDistance, speed, centimeters);
    	}
    }    
    
    public void SpinLeft(int speed) {
    	if (speed >= 0 && speed <= 100)
    	{
    		robotReceiver.sendMessage(RobotCommand.CmdSpinLeft, speed);
    	}
    }
    
    public void SpinLeft(int speed, int degrees) {
    	if (speed >= 0 && speed <= 100 && degrees >= 0 && degrees <= 180)
    	{
    		robotReceiver.sendMessage(RobotCommand.CmdSpinLeftDistance, speed, degrees);
    	}
    }
    
    public void SpinRight(int speed) {
    	if (speed >= 0 && speed <= 100)
    	{
    		robotReceiver.sendMessage(RobotCommand.CmdSpinRight, speed);
    	}
    }
    
    public void SpinRight(int speed, int degrees) {
    	if (speed >= 0 && speed <= 100 && degrees >= 0 && degrees <= 180)
    	{
    		robotReceiver.sendMessage(RobotCommand.CmdSpinRightDistance, speed, degrees);
    	}
    }
    
    public void Stop() {
    	robotReceiver.sendMessage(RobotCommand.CmdStop);
    }
    
    public void MoveCameraVert(int degrees) {
    	if (degrees >= 0 && degrees <= 180)
    	{
	    	robotReceiver.sendMessage(RobotCommand.CmdMoveCameraVert, degrees);
	    	CameraVertPosition = degrees;
    	}
    }
    
    public void MoveCameraHor(int degrees) {
    	if (degrees >= 0 && degrees <= 180)
    	{
	    	robotReceiver.sendMessage(RobotCommand.CmdMoveCameraHor, degrees);
	    	CameraHorPosition = degrees;
    	}
    }
}
