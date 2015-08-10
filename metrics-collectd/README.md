Metrics-collectd
=======

*A metrics plugin for reporting results using the collectd binary protocol over UDP*

For more information about metrics, please see [the metrics documentation](http://dropwizard.github.io/metrics/).
For more information about collectd binary protocol, please see [the collectd documentation](https://collectd.org/wiki/index.php/Binary_protocol).

Usage
-------

This plugin defines 3 custom collectd types. In order for your collectd server instance to recognize these types, you should add the following entries to your types.db or types.db.custom file:
``` sh
histogram		count:GAUGE:0:U, max:GAUGE:U:U, mean:GAUGE:U:U, min:GAUGE:U:U, stddev:GAUGE:0:U, p50:GAUGE:U:U, p75:GAUGE:U:U, p95:GAUGE:U:U, p98:GAUGE:U:U, p99:GAUGE:U:U, p999:GAUGE:U:U
metered		count:GAUGE:0:U, m1_rate:GAUGE:0:U, m5_rate:GAUGE:0:U, m15_rate:GAUGE:0:U, mean_rate:GAUGE:0:U
timer		max:GAUGE:U:U, mean:GAUGE:U:U, min:GAUGE:U:U, stddev:GAUGE:0:U, p50:GAUGE:U:U, p75:GAUGE:U:U, p95:GAUGE:U:U, p98:GAUGE:U:U, p99:GAUGE:U:U, p999:GAUGE:U:U
```

These entries are also included in the /src/main/resources/types.db.custom file in this project.

License
-------

Copyright (c) 2010-2014 Coda Hale, Yammer.com

Published under Apache Software License 2.0, see LICENSE
