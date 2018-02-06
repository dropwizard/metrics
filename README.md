Metrics
======= 
[![Build Status](https://secure.travis-ci.org/dropwizard/metrics.png)](http://travis-ci.org/dropwizard/metrics)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.dropwizard.metrics/metrics-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.dropwizard.metrics/metrics-core/)
[![Javadoc](http://javadoc-badge.appspot.com/io.dropwizard.metrics/metrics-core.svg)](http://www.javadoc.io/doc/io.dropwizard.metrics/metrics-core)
[![Code Climate](https://codeclimate.com/github/dropwizard/metrics/badges/gpa.svg)](https://codeclimate.com/github/dropwizard/metrics)

*Capturing JVM- and application-level metrics. So you know what's going on.*

For more information, please see [the documentation](http://dropwizard.github.io/metrics/).

### Versions

#### Version 4.x.x ([Javadoc](https://www.javadoc.io/doc/io.dropwizard.metrics/metrics-core/4.0.1))

Version 4.x.x (the last release is 4.0.2) is a Java 8/9 compatible and the most fresh release of Metrics. The version targets Java 8 and removes a lot of internal cruft from 3.2.x (for instance, there's no dependency on the Unsafe API and custom `LongAdder` and `ThreadLocalRandom` implementations). It's mostly compatible with the 3.2 API and the update should be painless in Java 8 environments. If you have a 3rd party application which is dependent on an old version of Metrics, you can force a new version by adding `metrics-bom` to your Maven configuration. Check out the [release notes](https://github.com/dropwizard/metrics/releases/tag/v4.0.0) for 4.0.0.

Source code for 4.1.x is resided in the [4.1-development branch](https://github.com/dropwizard/metrics/tree/4.1-development).

Source code for 4.0.x is resided in the [4.0-maintenance branch](https://github.com/dropwizard/metrics/tree/4.0-maintenance).

#### Version 3.2.x ([Javadoc](https://www.javadoc.io/doc/io.dropwizard.metrics/metrics-core/3.2.6))

Version 3.2.x (the last release is 3.2.6) is a Java 6 compatible and the most stable release of Metrics. 3.2.* was actively developed in 2017 and has many new features compared to 3.1.x (check out the [release notes](https://github.com/dropwizard/metrics/releases/tag/v3.2.0)). Curently 3.2.x version is in maintenance mode. No future development is planned, but bugfixes are ported and you can expect maintenance releases.

Source code for 3.2.x is resided in the [3.2-maintenance branch](https://github.com/dropwizard/metrics/tree/3.2-maintenance).

#### Version 3.1.x ([Javadoc](https://www.javadoc.io/doc/io.dropwizard.metrics/metrics-core/3.1.5))

Version 3.1.x (the last release is 3.1.5) is a Java 6 compatible release of Metrics. It's the most common distribution of Metrics, albeit not actively developed and maintained. Version 3.1.2 was released in April, 2015 and since then there's been only a handful amount of bugfixes ported to this branch. If you're using Metrics 3.1.* , please strongly consider upgrading to 3.2.* or 4.0.* , because this version is practically EOL. No future releases of 3.1.* are expected.    

Source code for 3.1.x is resided in the [3.1-maintenance branch](https://github.com/dropwizard/metrics/tree/3.1-maintenance).

### Future development

New not-backward compatible features (for example, support for tags) will be implemented in a 5.x.x release. The release will have new Maven coordinates, a new package name and an backwards-incompatible API. 

Source code for 5.x.x is resided in the [5.0-development branch](https://github.com/dropwizard/metrics/tree/5.0-development).

License
-------

Copyright (c) 2010-2013 Coda Hale, Yammer.com, 2014-2017 Dropwizard Team

Published under Apache Software License 2.0, see LICENSE
