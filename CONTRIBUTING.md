Contributing/Development
===
Metrics is always looking for people to contribute to the project. We welcome your
feedback and want to listen and discuss your ideas and issues.

There are many different ways to help contribute to the Metrics project.

* Helping others by participating in the [Metrics User Google Group](https://groups.google.com/forum/#!forum/metrics-user)
* Improving or enhancing our [documentation](http://dropwizard.github.io/metrics/)
* Fixing open issues listed in the [issue tracker](https://github.com/dropwizard/metrics/issues?state=open)
* Adding new features to the Metrics codebase

Important
===
While third party integrations are essential to this project, currently we cannot accept pull requests for new third party
integrations, as it has become burdensome to maintain modules which weren't written by the core developers. Please create
your own repository, publish to maven, and open a pull request to add a link to `third-party.rst`.

Guidelines
===
When submitting a pull request, please make sure to fork the repository and create a
separate branch for your feature or fix for an issue.

All contributions are welcome to be submitted for review for inclusion, but before
they will be accepted, we ask that you follow these simple guidelines:

Code style
---
When submitting code, please make every effort to follow existing conventions and
style in order to keep the code as readable as possible. We realize that the style
used in Metrics might be different that what is used in your projects, but in the end
it makes it easier to merge changes and maintain in the future.

Testing
---
We kindly ask that all new features and fixes for an issue should include any unit tests.
Even if it is small improvement, adding a unit test will help to ensure no regressions or the
issue is not re-introduced. If you need help with writing a test for your feature, please
don't be shy and ask!

Documentation
---
Up-to-date documentation makes all our lives easier. If you are adding a new feature,
enhancing an existing feature, or fixing an issue, please add or modify the documentation
(including changelog and release notes) as needed and include it with your pull request.

New Features
===
If you would like to implement a new feature, please raise an issue before sending a
pull request so the feature can be discussed. **We appreciate the effort and want
to avoid a situation where a contribution requires extensive rework on either side,
it sits in the queue for a long time, or cannot be accepted at all.**

Committers
===
The list of people with committer access is kept in the developer section of the pom.xml located in the parent directory.
