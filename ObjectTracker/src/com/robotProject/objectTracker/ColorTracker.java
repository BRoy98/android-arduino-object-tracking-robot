package com.robotProject.objectTracker;

import java.text.DecimalFormat;

import org.opencv.android.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.robotProject.robot.ObjectTrackingRobot;
import com.robotProject.robot.Robot;
import com.robotProject.objectTracker.R;
import com.robotProject.video.cameraPreview.*;
import com.robotProject.video.cameraPreview.*;
import com.robotProject.video.events.color.ColorFrameProcessedEvent;
import com.robotProject.video.events.template.*;
import com.robotProject.video.events.test.*;
import com.robotProject.video.listeners.color.IColorFrameProcessedListener;
import com.robotProject.video.listeners.template.*;
import com.robotProject.video.processors.*;
import com.robotProject.video.processors.color.ColorFrameProcessor;
import com.robotProject.video.processors.template.*;
import android.graphics.*;
import android.graphics.PorterDuff.Mode;
import android.graphics.Paint.*;
import android.graphics.Rect;
import android.hardware.*;
import android.hardware.Camera.*;
import android.os.*;
import android.app.*;
import android.util.*;
import android.view.*;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnTouchListener;
import android.widget.*;

@SuppressWarnings("unused")
/**
 * @author Mejdl Safran & Steve Haar
 * This activity will track an object based on the color selected by the user.
 * To use this activity be sure to make it the main activity in the manifest file.
 * (currently this is already set as the main activity)
 */
public class ColorTracker extends Activity implements ICameraPreviewCallback, IColorFrameProcessedListener, Callback, OnTouchListener{
	private static final String TAG = "CameraTest::MainActivity";
	
	private ColorFrameProcessor mFrameProcessor;
	private CameraPreview mPreview;
	private SurfaceView mCameraOverlay;
	private FrameLayout mFrameLayout;
	private ObjectTrackingRobot mRobot;
	
	private Button mTrackButton;
	private TextView mFpsIndicator;
	private long mTotalTime = 0;
	private int mFpsIteration = 0;
	private int mFpsStep = 10;
	private int mDrawIteration = 0;
	private int mDrawStep = 5;
	private boolean mTrackingStarted = false;
	
	private Paint mRectPaint = new Paint();
	private Paint mNoMoveRectPaint = new Paint();
	private DecimalFormat mDecimalFormat = new DecimalFormat("#.##");

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mRobot = new ObjectTrackingRobot(this);
        setContentView(R.layout.activity_color_tracker);
        
        mTrackButton = (Button)findViewById(R.id.trackButton);
        mFpsIndicator = (TextView)findViewById(R.id.timeIndicator);
        mFpsIndicator.setTextColor(Color.RED);
        mRectPaint.setARGB(255, 0, 255, 0);
        mRectPaint.setStyle(Style.STROKE);
        mNoMoveRectPaint.setARGB(255, 255, 0, 0);
        mNoMoveRectPaint.setStyle(Style.STROKE);
    }

    public boolean onTouch(View v, MotionEvent event)
	{
    	double ratio = (double)mFrameLayout.getWidth() / mFrameProcessor.getFrameSize().width;
    	int x = (int)event.getX();
    	int y = (int)event.getY();
    	int adjustedX = (int)Math.round(x / ratio);
    	int adjustedY = (int)Math.round(y / ratio);
    	boolean result = mFrameProcessor.onTouch(adjustedX, adjustedY);
    	mTrackingStarted = true;
    	return result;
	}
      
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void trackButton_Click(View arg) {
    	mTrackButton.setEnabled(false);
    	mFrameProcessor = new ColorFrameProcessor(this);
        mPreview = new CameraPreview(this, mFrameProcessor);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mPreview != null) {
        	mPreview.resume();
        }
        mRobot.resume();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mPreview != null) {
        	mPreview.pause();
        }
        mRobot.pause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
        	mPreview.destroy();
        }
        mRobot.destroy();
    }
    
    public void cameraPreviewLoaded() {
    	//This is called after OpenCV library has been initialized
    	mCameraOverlay = new SurfaceView(this);
        mCameraOverlay.getHolder().setFormat(PixelFormat.TRANSPARENT);
    	
        mFrameLayout = (FrameLayout)findViewById(R.id.camera_preview);
    	mFrameLayout.addView(mCameraOverlay);
    	mFrameLayout.addView(mPreview);
    	
    	mCameraOverlay.getHolder().addCallback(this);
    	mPreview.setOnTouchListener(this);
    }
    
    public void onFrameProcessed(ColorFrameProcessedEvent event) {
    	mDrawIteration++;
    	mFpsIteration++;
    	mTotalTime += event.time;
    	
		if (mFpsIteration == mFpsStep) {
    		double fps = (1000.0 * mFpsStep) / mTotalTime;
    		mFpsIndicator.setText(mDecimalFormat.format(fps));
    		mTotalTime = mFpsIteration = 0;
    	}
		
		if (mDrawIteration % mDrawStep == 0) {
			if (mTrackingStarted)
			{
				if (mRobot.originalRectangle == null)
				{
					mRobot.originalRectangle = event.objectPosition;
					mRobot.frameSize = mFrameProcessor.getFrameSize();
				}
				
				mRobot.setNoMoveRect(event.noMove);
				mRobot.TrackObject(event.objectPosition);
				
				if (event.objectPosition != null) {				
					Canvas canvas = mCameraOverlay.getHolder().lockCanvas();
					if (canvas != null) {
						canvas.drawColor(0, Mode.CLEAR);
						canvas.drawRect(convertRectangle(event.objectPosition), mRectPaint);
						canvas.drawRect(convertRectangle(event.noMove), mNoMoveRectPaint);
						Log.i("NOMOVE: " + event.noMove.x + ", " + event.noMove.y + ", " + event.noMove.width + ", " + event.noMove.height, TAG);
						mCameraOverlay.getHolder().unlockCanvasAndPost(canvas);
					}
				}
			}
			mDrawIteration = 0;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		double cameraAspectRatio = (double)mFrameProcessor.getFrameSize().width / mFrameProcessor.getFrameSize().height;
		double screenAspectRatio = (double)mFrameLayout.getWidth() / mFrameLayout.getHeight();
		Log.i("Camera: " + mFrameProcessor.getFrameSize().width + " x " + mFrameProcessor.getFrameSize().height, TAG);
		Log.i("Screen: " + mFrameLayout.getWidth() + " x " + mFrameLayout.getHeight(), TAG);
		if (cameraAspectRatio < screenAspectRatio) {
			int newWidth = (int)Math.floor(mFrameLayout.getHeight() * cameraAspectRatio);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(newWidth, mFrameLayout.getHeight());
			lp.leftMargin = (mFrameLayout.getWidth() - newWidth) / 2;
			mFrameLayout.setLayoutParams(lp);
		}
		else if (cameraAspectRatio > screenAspectRatio) {
			int newHeight = (int)Math.floor(mFrameLayout.getWidth() / cameraAspectRatio);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mFrameLayout.getWidth(), newHeight);
			lp.topMargin = (mFrameLayout.getHeight() - newHeight) / 2;
			mFrameLayout.setLayoutParams(lp);
		}
		mCameraOverlay.getHolder().setFixedSize(mFrameProcessor.getFrameSize().width, mFrameProcessor.getFrameSize().height);
		
		Canvas canvas = null;
		while (canvas == null) {
			canvas = mCameraOverlay.getHolder().lockCanvas();
			if (canvas != null) {
				canvas.drawColor(0, Mode.CLEAR);
				canvas.drawPoint(mFrameLayout.getWidth() / 2, mFrameLayout.getHeight() / 2, mNoMoveRectPaint);
				mCameraOverlay.getHolder().unlockCanvasAndPost(canvas);
			}
		}
		
		mCameraOverlay.invalidate();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}
	
	private Rect convertRectangle(org.opencv.core.Rect r) {
    	return new Rect(r.x, r.y, r.x + r.width, r.y + r.height);
    }
}
