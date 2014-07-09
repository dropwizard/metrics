package com.codahale.metrics.concrete;

import com.codahale.metrics.GlobalTimer;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codahale.metrics.TimerContext;


/**
 * A timer metric which provide a single, one time, duration measurement
 */
public class GlobalTimerConcrete extends GlobalTimer {

	private final Timer mTimer;
	private TimerContext mContext;
	
	public GlobalTimerConcrete() {
		this(new TimerConcrete());
	}
	
	public GlobalTimerConcrete(Timer t) {
		mTimer = t;
		mContext = null;
	}
	
	public synchronized void start() {
		if (mContext != null) {
			// Disregard old measurement
			mContext = null;
		}
		
		// New measurement
		mContext = mTimer.time();
	}
	
	public synchronized void stop() {
		if (mContext != null) {
			mContext.stop();
			mContext = null;
		}
	}

	@Override
	public Snapshot getSnapshot() {
		return mTimer.getSnapshot();
	}

	@Override
	public long getCount() {
		return mTimer.getCount();
	}

	@Override
	public double getFifteenMinuteRate() {
		return mTimer.getFifteenMinuteRate();
	}

	@Override
	public double getFiveMinuteRate() {
		return mTimer.getFiveMinuteRate();
	}

	@Override
	public double getOneHourRate() {
		return mTimer.getOneHourRate();
	}

	@Override
	public double getThreeHourRate() {
		return getThreeHourRate();
	}

	@Override
	public double getMeanRate() {
		return mTimer.getMeanRate();
	}

	@Override
	public double getOneMinuteRate() {
		return mTimer.getOneMinuteRate();
	}

	@Override
	public Timer getTimer() {
		return mTimer;
	}

}
