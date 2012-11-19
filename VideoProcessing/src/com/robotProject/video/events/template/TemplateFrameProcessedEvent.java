package com.robotProject.video.events.template;

import org.opencv.core.Rect;

public class TemplateFrameProcessedEvent {
	public long time;
	public Rect noMove;
	public Rect objectPosition;
	public boolean tracking;
	
	public TemplateFrameProcessedEvent(long time, Rect rect, Rect noMove, boolean tracking) {
		this.time = time;
		this.objectPosition = rect;
		this.noMove = noMove;
		this.tracking = tracking;
	}
}
