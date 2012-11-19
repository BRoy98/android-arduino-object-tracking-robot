package com.robotProject.video.events.color;

import org.opencv.core.Rect;

public class ColorFrameProcessedEvent {
	public long time;
	public Rect noMove;
	public Rect objectPosition;
	public boolean tracking;
	
	public ColorFrameProcessedEvent(long time, Rect rect, Rect noMove, boolean tracking) {
		this.time = time;
		this.objectPosition = rect;
		this.noMove = noMove;
		this.tracking = tracking;
	}
}
