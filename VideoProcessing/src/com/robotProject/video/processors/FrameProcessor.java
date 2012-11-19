package com.robotProject.video.processors;

import android.hardware.Camera;

/**
 * @author Mejdl Safran & Steve Haar
 * This class is to be used with the CameraPreview. The CameraPreview will pass frames
 * from the video preview into the onPreviewFrame method.
 */
public abstract class FrameProcessor implements IFrameProcessor {
	protected android.hardware.Camera.Size mFrameSize;
	protected Camera mCamera;
	
	public void setCamera(Camera camera) {
		this.mFrameSize = camera.getParameters().getPreviewSize();
		this.mCamera = camera;
		ready();
	}
	
	public android.hardware.Camera.Size getFrameSize() {
		return mFrameSize;
	}
	
	public abstract void onPreviewFrame(byte[] data, Camera camera);
	public abstract void ready();
	
	public void resume() { }
	public void pause() { }
	public void destroy() { }
}
