package com.yammer.metrics.ehcache.tests;

// TODO: 3/10/13 <coda> -- figure out how to coordinate on registry names

//public class ConfigInstrumentedEhcacheTest {
//
//    private static final CacheManager MANAGER = CacheManager.create();
//
//    private Ehcache cache;
//
//    @Before
//    public void setUp() throws Exception {
//        cache = MANAGER.getEhcache("test-config");
//        if (cache == null) fail("Cache is not set correctly");
//    }
//
//    @Test
//    public void measuresGets() throws Exception {
//        cache.get("woo");
//
//        final Timer gets = Metrics.defaultRegistry().newTimer(Cache.class,
//                                                              "get",
//                                                              "test-config",
//                                                              TimeUnit.MILLISECONDS,
//                                                              TimeUnit.SECONDS);
//
//        assertThat(gets.count(), is(1L));
//
//    }
//
//    @Test
//    public void measuresPuts() throws Exception {
//
//        cache.put(new Element("woo", "whee"));
//
//        final Timer puts = Metrics.defaultRegistry().newTimer(Cache.class,
//                                                              "put",
//                                                              "test-config",
//                                                              TimeUnit.MILLISECONDS,
//                                                              TimeUnit.SECONDS);
//
//        assertThat(puts.count(), is(1L));
//
//    }
//
//}
