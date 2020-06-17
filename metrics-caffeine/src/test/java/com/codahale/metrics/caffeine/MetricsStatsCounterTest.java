/*
 * Copyright 2016 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codahale.metrics.caffeine;

import static org.junit.Assert.assertEquals;

import com.codahale.metrics.MetricRegistry;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.junit.Test;

/**
 * An example of exporting stats to Dropwizard Metrics (http://metrics.dropwizard.io).
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class MetricsStatsCounterTest {

  @Test
  public void metrics() {
    // Use a registry that is exported using a Reporter (via console, JMX, Graphite, etc)
    MetricRegistry registry = new MetricRegistry();

    // Create the cache with a dedicated, uniquely named stats counter
    LoadingCache<Integer, Integer> cache = Caffeine.newBuilder()
        .recordStats(() -> new MetricsStatsCounter(registry, "example"))
        .build(key -> key);

    // Perform application work
    for (int i = 0; i < 4; i++) {
      cache.get(1);
    }

    // Statistics can be queried and reported on
    assertEquals(cache.stats().hitCount(), 3L);
    assertEquals(registry.counter("example.hits").getCount(), 3L);
  }
}
