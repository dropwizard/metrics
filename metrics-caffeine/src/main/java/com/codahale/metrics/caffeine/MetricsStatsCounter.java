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

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A {@link StatsCounter} instrumented with Dropwizard Metrics.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @author John Karp
 */
public final class MetricsStatsCounter implements StatsCounter {
  private final Counter hitCount;
  private final Counter missCount;
  private final Timer loadSuccess;
  private final Timer loadFailure;
  private final Histogram evictions;
  private final Counter evictionWeight;
  private final EnumMap<RemovalCause, Histogram> evictionsWithCause;

  // for implementing snapshot()
  private final LongAdder totalLoadTime = new LongAdder();

  /**
   * Constructs an instance for use by a single cache.
   *
   * @param registry the registry of metric instances
   * @param metricsPrefix the prefix name for the metrics
   */
  public MetricsStatsCounter(MetricRegistry registry, String metricsPrefix) {
    requireNonNull(metricsPrefix);
    hitCount = registry.counter(MetricRegistry.name(metricsPrefix, "hits"));
    missCount = registry.counter(MetricRegistry.name(metricsPrefix, "misses"));
    loadSuccess = registry.timer(MetricRegistry.name(metricsPrefix, "loads-success"));
    loadFailure = registry.timer(MetricRegistry.name(metricsPrefix, "loads-failure"));
    evictions = registry.histogram(MetricRegistry.name(metricsPrefix, "evictions"));
    evictionWeight = registry.counter(MetricRegistry.name(metricsPrefix, "evictions-weight"));

    evictionsWithCause = new EnumMap<>(RemovalCause.class);
    for (RemovalCause cause : RemovalCause.values()) {
      evictionsWithCause.put(
          cause,
          registry.histogram(MetricRegistry.name(metricsPrefix, "evictions", cause.name())));
    }
  }

  @Override
  public void recordHits(int count) {
    hitCount.inc(count);
  }

  @Override
  public void recordMisses(int count) {
    missCount.inc(count);
  }

  @Override
  public void recordLoadSuccess(long loadTime) {
    loadSuccess.update(loadTime, TimeUnit.NANOSECONDS);
    totalLoadTime.add(loadTime);
  }

  @Override
  public void recordLoadFailure(long loadTime) {
    loadFailure.update(loadTime, TimeUnit.NANOSECONDS);
    totalLoadTime.add(loadTime);
  }

  // @Override -- Caffeine 2.x
  @Deprecated
  @SuppressWarnings("deprecation")
  public void recordEviction() {
    // This method is scheduled for removal in version 3.0 in favor of recordEviction(weight)
    recordEviction(1);
  }

  // @Override -- Caffeine 2.x
  @Deprecated
  @SuppressWarnings("deprecation")
  public void recordEviction(int weight) {
    evictions.update(weight);
    evictionWeight.inc(weight);
  }

  @Override
  public void recordEviction(@NonNegative int weight, RemovalCause cause) {
    evictionsWithCause.get(cause).update(weight);
    evictionWeight.inc(weight);
  }

  @Override
  public CacheStats snapshot() {
    return CacheStats.of(
        hitCount.getCount(),
        missCount.getCount(),
        loadSuccess.getCount(),
        loadFailure.getCount(),
        totalLoadTime.sum(),
        evictions.getCount(),
        evictionWeight.getCount());
  }

  @Override
  public String toString() {
    return snapshot().toString();
  }
}
