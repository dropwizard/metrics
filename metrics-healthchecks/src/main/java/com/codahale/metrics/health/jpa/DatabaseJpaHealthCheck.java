package com.codahale.metrics.health.jpa;

import com.codahale.metrics.health.HealthCheck;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * A health check which returns healthy if database is up or not.
 */
public abstract class DatabaseJpaHealthCheck extends HealthCheck {

    private final EntityManagerFactory entityManagerFactory;

    /**
     * Creates a new health check with the given entity manager factory.
     *
     * @param entityManagerFactory database entity manager factory
     */
    public DatabaseJpaHealthCheck(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * The SQL query that will be used to validate the database.
     * This query MUST be an SQL SELECT statement that returns at least one row.
     *
     * @return the sql query to test database.
     */
    public abstract String checkQuery();

    /**
     * The expected result from the validation query to verify if the result
     * from the query is the same as expected.
     *
     * @return expected result from the validation query.
     */
    public abstract Object checkResult();

    /**
     * Message when the database is down.
     *
     * @return message when the database is down.
     */
    protected String unhealthyMessage() {
        return "Database is down.";
    }

    @Override
    protected Result check() throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Object value = entityManager.createNativeQuery(checkQuery()).getSingleResult();
            if (value != null && value.equals(checkResult())) {
                return HealthCheck.Result.healthy();
            }
            return HealthCheck.Result.unhealthy(unhealthyMessage());
        } catch (Exception exc) {
            return HealthCheck.Result.unhealthy(unhealthyMessage());
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

}
