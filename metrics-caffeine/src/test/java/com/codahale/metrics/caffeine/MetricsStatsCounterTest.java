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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.codahale.metrics.MetricRegistry;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.junit.Before;
import org.junit.Test;

/**
 * An example of exporting stats to Dropwizard Metrics (http://metrics.dropwizard.io).
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @author John Karp
 */
public final class MetricsStatsCounterTest {

  private static final String PREFIX = "foo";

  private MetricsStatsCounter stats;
  private MetricRegistry registry;

  @Before
  public void setUp() {
    registry = new MetricRegistry();
    stats = new MetricsStatsCounter(registry, PREFIX);
  }

  @Test
  public void basicUsage() {
    LoadingCache<Integer, Integer> cache = Caffeine.newBuilder()
        .recordStats(() -> new MetricsStatsCounter(registry, PREFIX))
        .build(key -> key);

    // Perform application work
    for (int i = 0; i < 4; i++) {
      cache.get(1);
    }

    assertEquals(3L, cache.stats().hitCount());
    assertEquals(3L, registry.counter(PREFIX + ".hits").getCount());
  }

  @Test
  public void hit() {
    stats.recordHits(2);
    assertThat(registry.counter(PREFIX + ".hits").getCount()).isEqualTo(2);
  }

  @Test
  public void miss() {
    stats.recordMisses(2);
    assertThat(registry.counter(PREFIX + ".misses").getCount()).isEqualTo(2);
  }

  @Test
  public void loadSuccess() {
    stats.recordLoadSuccess(256);
    assertThat(registry.timer(PREFIX + ".loads-success").getCount()).isEqualTo(1);
  }

  @Test
  public void loadFailure() {
    stats.recordLoadFailure(256);
    assertThat(registry.timer(PREFIX + ".loads-failure").getCount()).isEqualTo(1);
  }

  @Test
  public void eviction() {
    stats.recordEviction();
    assertThat(registry.histogram(PREFIX + ".evictions").getCount()).isEqualTo(1);
    assertThat(registry.counter(PREFIX + ".evictions-weight").getCount()).isEqualTo(1);
  }

  @Test
  public void evictionWithWeight() {
    stats.recordEviction(3);
    assertThat(registry.histogram(PREFIX + ".evictions").getCount()).isEqualTo(1);
    assertThat(registry.counter(PREFIX + ".evictions-weight").getCount()).isEqualTo(3);
  }

  @Test
  public void evictionWithCause() {
    // With JUnit 5, this would be better done with @ParameterizedTest + @EnumSource
    for (RemovalCause cause : RemovalCause.values()) {
      stats.recordEviction(3, cause);
      assertThat(registry.histogram(PREFIX + ".evictions." + cause.name()).getCount()).isEqualTo(1);
    }
  }
}
