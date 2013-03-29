.. _man-healthchecks:

#############
Health Checks
#############

Metrics also provides you with a consistent, unified way of performing application health checks. A
health check is basically a small self-test which your application performs to verify that a
specific component or responsibility is performing correctly.

To create a health check, extend the ``HealthCheck`` class:

.. code-block:: java

    public class DatabaseHealthCheck extends HealthCheck {
        private final Database database;

        public DatabaseHealthCheck(Database database) {
            this.database = database;
        }

        @Override
        protected Result check() throws Exception {
            if (database.ping()) {
                return Result.healthy();
            }
            return Result.unhealthy("Can't ping database");
        }
    }

In this example, we've created a health check for a ``Database`` class on which our application
depends. Our fictitious ``Database`` class has a ``#ping()`` method, which executes a safe test
query (e.g., ``SELECT 1``). ``#ping()`` returns ``true`` if the query returns the expected result,
returns ``false`` if it returns something else, and throws an exception if things have gone
seriously wrong.

Our ``DatabaseHealthCheck``, then, takes a ``Database`` instance and in its ``#check()`` method,
attempts to ping the database. If it can, it returns a **healthy** result. If it can't, it returns
an **unhealthy** result.

.. note::

    Exceptions thrown inside a health check's ``#check()`` method are automatically caught and
    turned into unhealthy results with the full stack trace.

To register a health check, either use a ``HealthCheckRegistry`` instance:

.. code-block:: java

    registry.register("database", new DatabaseHealthCheck(database));

You can also run the set of registered health checks:

.. code-block:: java

    for (Entry<String, Result> entry : registry.runHealthChecks().entrySet()) {
        if (entry.getValue().isHealthy()) {
            System.out.println(entry.getKey() + ": OK");
        } else {
            System.out.println(entry.getKey() + ": FAIL");
        }
    }
