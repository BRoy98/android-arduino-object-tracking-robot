package com.robotProject.robot;

import org.opencv.core.Rect;

import android.content.Context;
import android.hardware.Camera.Size;
import android.util.Log;

/**
 * @author Mejdl Safran & Steve Haar
 * When this class is used the robot will track an object by spinning and driving forward and backward.
 * TrackObject should be called repeatedly while processing frames in order for the robot to know
 * where the object is. If the object position passed to TrackObject is null, the robot will assume the
 * object is lost and will begin a seek and find routine in an effort to relocate the object.
 */
public class ObjectTrackingRobot extends Robot {
	private TrackingRobotState robotState = TrackingRobotState.NoMove;
	private Rect objectPosition;
	private Rect lastKnownPosition;
	public Size frameSize;
	public Rect originalRectangle;
	private int lostSpeed = 30;
	private double maxRatio = 1.25;
	private double minRatio = 0.75;
	private int frontSonar;
	private int rearSonar;
	private final int sonarThreshold = 50;
	private final int robotSpeed = 35;
	
	public ObjectTrackingRobot(Context context) {
		super(context);
		this.addEventListener(frontSonarListener);
		this.addEventListener(rearSonarListener);
	}
	
	private final RobotListener frontSonarListener = new RobotListener(RobotEventType.EvtFrontSonar){
    	@Override
    	public void robotSignalReceived(RobotEvent event){
    		frontSonar = event.getValue();
		}
    };
    
    private final RobotListener rearSonarListener = new RobotListener(RobotEventType.EvtRearSonar){
    	@Override
    	public void robotSignalReceived(RobotEvent event){
    		rearSonar = event.getValue();
		}
    };
	
	public void  TrackObject(Rect objectPosition)
    {
		Log.i("TAG", this.robotState.toString());
		this.objectPosition = objectPosition;
		if (this.objectPosition == null)
		{
			this.robotState = TrackingRobotState.Lost;
		}
		else
		{
			this.lastKnownPosition = this.objectPosition;
		}
		
		switch(robotState)
		{
			case NoMove:
				noMove();
				break;
			case MoveCameraUpDown:
				moveCameraUpDown();
				break;
			case DriveLeftRight:
				driveLeftRight();
				break;
			case DriveForwardBackwards:
				driveForwardBackward();
				break;
			case Lost:
				lost();
				break;
		}
    }
	
	private void noMove()
	{
		this.robotState = TrackingRobotState.NoMove;
		this.Stop();
		boolean moveTop = objectPosition.y < this.NoMoveRect.y;
		boolean moveRight = objectPosition.x + objectPosition.width > this.NoMoveRect.x + this.NoMoveRect.width;
		boolean moveBottom = objectPosition.y + objectPosition.height > this.NoMoveRect.y + this.NoMoveRect.height;
		boolean moveLeft = objectPosition.x < this.NoMoveRect.x;
		double ratio = (double)objectPosition.width / this.originalRectangle.width;
		
		if (moveTop || moveBottom)
		{
			moveCameraUpDown();
		}
		else if (moveLeft || moveRight)
		{
			driveLeftRight();
		}
		else if (ratio < minRatio || ratio > maxRatio)
		{
			driveForwardBackward();
		}
	}
	
	private void lost()
	{
		this.robotState = TrackingRobotState.Lost;
		if (this.objectPosition == null)
		{
			int objectCenter = this.lastKnownPosition.x + (this.lastKnownPosition.width / 2);
			int frameCenter = (int)Math.round(this.frameSize.width) / 2;
			
			if (objectCenter < frameCenter)
			{
				this.SpinLeft(lostSpeed);
			}
			else
			{
				this.SpinRight(lostSpeed);
			}
		}
		else
		{
			noMove();
		}
	}
	
	private void moveCameraUpDown()
	{
		this.robotState = TrackingRobotState.MoveCameraUpDown;
		boolean moveTop = objectPosition.y < this.NoMoveRect.y;
		boolean moveBottom = objectPosition.y + objectPosition.height > this.NoMoveRect.y + this.NoMoveRect.height;
		
		if(moveTop)
    	{
    		if (this.getCameraVertPosition() < this.getMaxCameraVertPosition())
    		{
    			this.MoveCameraVert(this.getCameraVertPosition() + 1);
    		}
    	}
		else if (moveBottom)
		{
			if (this.getCameraVertPosition() > this.getMinCameraVertPosition())
    		{
    			this.MoveCameraVert(this.getCameraVertPosition() - 1);
    		}
		}
		else
		{
			noMove();
		}
	}

	private void driveLeftRight()
	{
		this.robotState = TrackingRobotState.DriveLeftRight;
		boolean moveRight = objectPosition.x + objectPosition.width > this.NoMoveRect.x + this.NoMoveRect.width;
		boolean moveLeft = objectPosition.x < this.NoMoveRect.x;
		
		if (moveLeft)
		{
			this.SpinLeft(robotSpeed);
		}
		else if (moveRight)
		{
			this.SpinRight(robotSpeed);
		}
		else
		{
			noMove();
		}
	}

	private void driveForwardBackward()
	{
		this.robotState = TrackingRobotState.DriveForwardBackwards;
		double ratio = (double)objectPosition.width / this.originalRectangle.width;
		
		if (ratio < minRatio)
		{
			if (this.frontSonar > sonarThreshold)
			{
				this.MoveForward(robotSpeed);
			}
			else
			{
				this.robotState = TrackingRobotState.NoMove;
			}
		}
		else if (ratio > maxRatio)
		{
			if (this.rearSonar > sonarThreshold)
			{
				this.MoveBackward(robotSpeed);
			}
			else
			{
				this.robotState = TrackingRobotState.NoMove;
			}
		}
		else
		{
			noMove();
		}
	}
}
