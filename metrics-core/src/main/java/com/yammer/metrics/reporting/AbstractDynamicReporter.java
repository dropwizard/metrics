/*
 * Copyright (c) 2012. Netflix, Inc.
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
 */

package com.yammer.metrics.reporting;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.MetricsRegistryListener;

import java.util.Set;

/**
 * User: gorzell
 * Date: 3/23/12
 * Time: 12:23 PM
 */
public abstract class AbstractDynamicReporter extends AbstractReporter implements MetricsRegistryListener {


    protected AbstractDynamicReporter(MetricsRegistry registry){
        super(registry);
    }
    
    protected AbstractDynamicReporter(Set<MetricsRegistry> registries){
        super(registries);
    }

    /**
     * Start the reporter by adding it as a listener on the registry
     */
    public void start() {
        for (MetricsRegistry metricsRegistry : metricsRegistries) {
            metricsRegistry.addListener(this);
        }
    }

    /**
     * Stops the reporter and closes any internal resources.
     */
    public void shutdown() {
        for (MetricsRegistry metricsRegistry : metricsRegistries) {
            metricsRegistry.removeListener(this);
        }
    }
}
