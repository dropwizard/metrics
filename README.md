Metrics ![Build Status](https://api.travis-ci.org/codahale/metrics.png)
=======

*Capturing JVM- and application-level metrics. So you know what's going on.*

For more information, please see [the documentation](http://metrics.codahale.com).


License
-------

Copyright (c) 2010-2013 Coda Hale, Yammer.com

Published under Apache Software License 2.0, see LICENSE


Note to Self(s)
--------------
update project:
  * git pull --rebase

to bump the version:
  * mvn versions:set -DnewVersion=1.0.3-SNAPSHOT

ensure all the modules get it:
  * mvn  versions:update-child-modules

commit the junk
  * git commit -m "notes about the junk"

push the junk
  * git push origin

celebrate
  * yay.
