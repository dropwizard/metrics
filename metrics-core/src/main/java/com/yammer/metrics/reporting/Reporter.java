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
import java.util.Set;

public interface Reporter {

    /**
     * Stops the reporter and closes any internal resources.
     */
    public void shutdown();

    /**
     * Set of registries associated with this reporter
     *
     * @return registries
     */
    public Set<MetricsRegistry> getMetricsRegistries();

    /**
     * Name of this reporter.
     *
     * @return
     */
    public String getName();
}
