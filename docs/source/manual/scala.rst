.. _manual-scala:

#############
Scala Support
#############

The ``metrics-scala_2.9.1`` module provides the ``Instrumented`` trait for Scala 2.9.1 applications:

.. code-block:: scala

    class Example(db: Database) extends Instrumented {
      private val loading = metrics.timer("loading")

      def loadStuff(): Seq[Row] = loading.time {
        db.fetchRows()
      }
    }

It also provides Scala-specific wrappers for each metric type.
