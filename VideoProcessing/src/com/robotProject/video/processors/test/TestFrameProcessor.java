package com.robotProject.video.processors.test;

import com.robotProject.video.events.test.TestFrameProcessedEvent;
import com.robotProject.video.listeners.test.ITestFrameProcessedListener;
import com.robotProject.video.processors.FrameProcessor;

import android.graphics.Canvas;
import android.hardware.*;
import android.hardware.Camera.*;

@SuppressWarnings("unused")
public class TestFrameProcessor extends FrameProcessor {
	private long timeStamp = 0;
	private ITestFrameProcessedListener mListener;
	private Camera mCamera;
	
	public TestFrameProcessor(ITestFrameProcessedListener listener) {
		this.mListener = listener;
	}
	
	@Override
	public void ready() {
		
	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		long newTimeStamp = System.currentTimeMillis();
		mCamera.addCallbackBuffer(data);
		mListener.onFrameProcessed(new TestFrameProcessedEvent(newTimeStamp - timeStamp));
		timeStamp = newTimeStamp;
	}
}
