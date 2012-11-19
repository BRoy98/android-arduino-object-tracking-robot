package com.robotProject.video.cameraPreview;

import java.io.IOException;
import java.util.List;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.robotProject.video.processors.IFrameProcessor;
import com.robotProject.video.processors.template.TemplateFrameProcessor;

import android.content.*;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.*;
import android.hardware.Camera.*;
import android.util.*;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.TextView;

@SuppressWarnings("unused")
/**
 * @author Mejdl Safran & Steve Haar
 * This class opens the camera on the Android and draws a preview of the camera on the screen.
 * It will initialize the Android OpenCV library and calls cameraPreviewLoaded on the context
 * when the library and the camera are ready to be used. This class processes frames from the
 * camera preview and passes them to the IFrameProcessor for processing.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, LoaderCallbackInterface {
	private static final String TAG = "CameraTest::CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private IFrameProcessor mFrameProcessor;
    private ICameraPreviewCallback mContext;
    
    public CameraPreview(ICameraPreviewCallback context, IFrameProcessor frameProcessor) {
    	super((Context)context);
        mContext = context;
        mFrameProcessor = frameProcessor;
        if (!loadOpenCV()) {
        	Log.e("OpenCV could not be loaded!", TAG);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            Size previewSize = mCamera.getParameters().getPreviewSize();
            int previewFormat = mCamera.getParameters().getPreviewFormat();
            int bytesPerPixel = ImageFormat.getBitsPerPixel(previewFormat) / 8;
            int bufferSize = (int)(previewSize.width * previewSize.height * bytesPerPixel * 1.5); //TODO: Don't know why I need this 1.5
            
            for (int i = 0; i < 1; i++) {
            	mCamera.addCallbackBuffer(new byte[bufferSize]);
            };
            
            mCamera.setPreviewCallbackWithBuffer(mFrameProcessor);

        } catch (Exception e){
            Log.e(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    
    private boolean loadOpenCV() {
    	return OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, (Context)mContext, this);
    }
    
    /*
     * This method is called after the OpenCV library is initialized.
     */
    @SuppressWarnings("deprecation")
	public void onManagerConnected(int status) {
		if (openCamera()) {
			// Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	        // deprecated setting, but required on Android versions prior to 3.0
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        	mFrameProcessor.setCamera(mCamera);
        	try {
    			Thread.sleep(1000);
    		} catch (Exception e) { }
        	mContext.cameraPreviewLoaded();
		}
	}

	public void onPackageInstall(InstallCallbackInterface callback) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean openCamera() {
    	if (mCamera == null) {
			if (Camera.getNumberOfCameras() > 0) {
				try {
		    		mCamera = Camera.open(0);
				}
				catch (Exception e) {
					Log.e("Could not access camera", TAG);
					return false;
				}
			}
    	}
    	return true;
    }
    
    private void releaseResources() {
    	if (mCamera != null) {
    		mCamera.release();
    		mCamera = null;
    	}
    }
    
    public void resume() {
        openCamera();
        mFrameProcessor.resume();
    }
    
    public void pause() {
        releaseResources();
        mFrameProcessor.pause();
    }
   
    public void destroy() {
    	releaseResources();
    	mFrameProcessor.destroy();
    }
}
