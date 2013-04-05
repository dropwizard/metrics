/**
 *
 *     Copyright 2013 KU Leuven Research and Development - iMinds - Distrinet
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *     Administrative Contact: dnet-project-office@cs.kuleuven.be
 *     Technical Contact: bart.vanbrabant@cs.kuleuven.be
 */
package com.yammer.metrics;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;



import com.yammer.metrics.Metric;
import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.MetricRegistry.MetricBuilder;

public class DecayingRegistry extends MetricRegistry {
	
	
	
	public class DecayTask extends TimerTask {

		@Override
		public void run() {
			Set<String> ls = lastSeen;
			lastSeen = getSet();
			
			for (String metr : getNames()) {
				if(!ls.contains(metr))
					remove(metr);
			}
			//System.out.println(getNames().size());
		}

	}

	private Set<String> lastSeen = getSet();
	
	public DecayingRegistry(int interval, TimeUnit unit) {
		Timer t = new Timer("metrics-reaper");
		t.scheduleAtFixedRate(new DecayTask(), unit.toMillis(interval), unit.toMillis(interval));
	}

	protected Set<String> getSet() {
		return Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	}

	protected <T extends Metric> T getOrAdd(String name, MetricBuilder<T> builder) {
		lastSeen.add(name);
		return super.getOrAdd(name, builder);
	}

	
	

	

}
