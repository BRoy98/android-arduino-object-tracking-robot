package com.robotProject.video.processors;

import android.hardware.Camera;
import android.hardware.Camera.*;

public interface IFrameProcessor extends PreviewCallback {	
	public void onPreviewFrame(byte[] data, Camera camera);
	public void setCamera(Camera camera);
	public void resume();
	public void pause();
	public void destroy();
}
