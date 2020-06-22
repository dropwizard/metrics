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

import com.codahale.metrics.Counter;
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
 */
public final class MetricsStatsCounter implements StatsCounter {
  private final Counter hitCount;
  private final Counter missCount;
  private final Counter loadSuccessCount;
  private final Counter loadFailureCount;
  private final Timer totalLoadTime;
  private final Counter evictionCount;
  private final Counter evictionWeight;
  private final EnumMap<RemovalCause, Counter> evictionCountWithCause;
  private final EnumMap<RemovalCause, Counter> evictionWeightWithCause;

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
    totalLoadTime = registry.timer(MetricRegistry.name(metricsPrefix, "loads"));
    loadSuccessCount = registry.counter(MetricRegistry.name(metricsPrefix, "loads-success"));
    loadFailureCount = registry.counter(MetricRegistry.name(metricsPrefix, "loads-failure"));
    evictionCount = registry.counter(MetricRegistry.name(metricsPrefix, "evictions"));
    evictionWeight = registry.counter(MetricRegistry.name(metricsPrefix, "evictions-weight"));

    evictionCountWithCause = new EnumMap<>(RemovalCause.class);
    evictionWeightWithCause = new EnumMap<>(RemovalCause.class);
    for (RemovalCause cause : RemovalCause.values()) {
      evictionCountWithCause.put(
          cause,
          registry.counter(MetricRegistry.name(metricsPrefix, "evictions", cause.name())));
      evictionWeightWithCause.put(
          cause,
          registry.counter(MetricRegistry.name(metricsPrefix, "evictions-weight", cause.name())));
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
    loadSuccessCount.inc();
    totalLoadTime.update(loadTime, TimeUnit.NANOSECONDS);
  }

  @Override
  public void recordLoadFailure(long loadTime) {
    loadFailureCount.inc();
    totalLoadTime.update(loadTime, TimeUnit.NANOSECONDS);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void recordEviction() {
    // This method is scheduled for removal in version 3.0 in favor of recordEviction(weight)
    recordEviction(1);
  }

  @Override
  public void recordEviction(int weight) {
    evictionCount.inc();
    evictionWeight.inc(weight);
  }

  @Override
  public void recordEviction(@NonNegative int weight, RemovalCause cause) {
    evictionCountWithCause.get(cause).inc();
    evictionWeightWithCause.get(cause).inc(weight);
    recordEviction(weight);
  }

  @Override
  public CacheStats snapshot() {
    return new CacheStats(
        hitCount.getCount(),
        missCount.getCount(),
        loadSuccessCount.getCount(),
        loadFailureCount.getCount(),
        totalLoadTime.getCount(),
        evictionCount.getCount(),
        evictionWeight.getCount());
  }

  @Override
  public String toString() {
    return snapshot().toString();
  }
}
