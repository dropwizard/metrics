package com.codahale.metrics.stub;

import com.codahale.metrics.GlobalTimer;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

public class GlobalTimerStub extends GlobalTimer {
	
	public void start() {
	}
	
	public void stop() {
	}


    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public double getFifteenMinuteRate() {
        return 0.0;
    }

    @Override
    public double getFiveMinuteRate() {
        return 0.0;
    }

    @Override
    public double getMeanRate() {
        return 0.0;
    }

    @Override
    public double getOneMinuteRate() {
        return 0.0;
    }

    @Override
    public double getOneHourRate() {
        return 0.0;
    }

    @Override
    public double getThreeHourRate() {
        return 0.0;
    }    
    
    @Override
    public Snapshot getSnapshot() {
        return null;
    }

	@Override
	public Timer getTimer() {
		return null;
	}
}
