package com.robotProject.video.processors.color;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.hardware.Camera;
import android.util.Log;

import com.robotProject.video.events.color.ColorFrameProcessedEvent;
import com.robotProject.video.listeners.color.IColorFrameProcessedListener;
import com.robotProject.video.processors.Configuration;
import com.robotProject.video.processors.FrameProcessor;

/**
 * @author Mejdl Safran & Steve Haar
 * This class uses OpenCV's contours and color blob detection to process each frame
 * and identify an objectPosition which specifies where the object is located in the frame.
 * For each frame processed, an event will be sent to the FrameProcessedListener. The onTouch
 * method is called in the activity from an onTouch listener which allows the user to touch
 * the screen to begin tracking an object.
 */
public class ColorFrameProcessor extends FrameProcessor {
	private static final String TAG = "ColorPreviewProcessing";
	private long timeStamp = 0;
	private IColorFrameProcessedListener mListener;	
	private boolean mIsColorSelected = false;
	private Scalar mBlobColorRgba;
	private Scalar mBlobColorHsv;
	private ColorBlobDetector mDetector;
	private Mat mSpectrum;
	private static Size SPECTRUM_SIZE;
	private Mat mRgba, mYuv;
	static public Point TouchedPixel;
	
	public ColorFrameProcessor(IColorFrameProcessedListener listener) {
		this.mListener = listener;
	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		mYuv.put(0, 0, data);
	    Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
	    Rect objectPosition = null;
	    Rect noMove = null;
	    
	    if (mIsColorSelected)
        {            
        	mDetector.process(mRgba);
        	List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            if(contours.size() > 0) {
            	objectPosition = Imgproc.boundingRect(contours.get(0));
            	noMove = getNoMoveRect(objectPosition);
            }
        }
	    
		long newTimeStamp = System.currentTimeMillis();
		mCamera.addCallbackBuffer(data);
		mListener.onFrameProcessed(new ColorFrameProcessedEvent(newTimeStamp - timeStamp, objectPosition, noMove, mIsColorSelected));
		timeStamp = newTimeStamp;
	}
	
	private Rect getNoMoveRect(Rect rect)
	{
		int width = (int)Math.round(Configuration.NO_MOVE_RECT_RATIO * this.getFrameSize().width);
		int height = (int)Math.round(Configuration.NO_MOVE_RECT_RATIO * this.getFrameSize().height);
		int x = (this.getFrameSize().width / 2) - (width / 2);
		int y = (this.getFrameSize().height / 2) - (height / 2);
		
		width = Math.min(width, this.getFrameSize().width - x - 1);
		height = Math.min(height, this.getFrameSize().height - y - 1);
		x = Math.max(x, 0);
		y = Math.max(y, 0);
			
		return new Rect(x, y, width, height);
	}
	
	public boolean onTouch(int x, int y) {
		 Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
		 mIsColorSelected = true;     
        if ((x < 0) || (y < 0) || (x > this.mFrameSize.width) || (y > this.mFrameSize.height)) return false;
  
        TouchedPixel = new Point (x,y);
        Rect touchedRect = new Rect();
        
        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < this.getFrameSize().width) ? x + 4 - touchedRect.x : this.mFrameSize.width - touchedRect.x;
        touchedRect.height = (y+4 < this.getFrameSize().height) ? y + 4 - touchedRect.y : this.mFrameSize.height - touchedRect.y;
        	
        Mat touchedRegionRgba = mRgba.submat(touchedRect);
        
        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
        
        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
        {
        	mBlobColorHsv.val[i] /= pointCount;
        }
        
        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
        
        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] + 
    			", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");
   		
   		mDetector.setHsvColor(mBlobColorHsv);
   		
   		Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
   		
        mIsColorSelected = true;
        return false; // don't need subsequent touch events
	}
	
	private Scalar converScalarHsv2Rgba(Scalar hsvColor)
	{	
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
       
        return new Scalar(pointMatRgba.get(0, 0));
	}

	@Override
	public void ready() {
		// TODO Auto-generated method stub
		
		SPECTRUM_SIZE = new Size(200, 32);
		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
		mBlobColorHsv = new Scalar(255);
		mYuv = new Mat(this.getFrameSize().height + this.getFrameSize().height / 2, this.getFrameSize().width, CvType.CV_8UC1);     	
    	mRgba = new Mat();
    	mDetector = new ColorBlobDetector();
	}

}
