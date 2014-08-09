package com.codahale.metrics.health.jpa;

import com.codahale.metrics.health.HealthCheck;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseJpaHealthCheckTest {

    private EntityManagerFactory emf;
    private EntityManager entityManager;
    private Query query;
    private DatabaseJpaHealthCheck healthCheck;

    @Before
    public void setup() {
        emf = mock(EntityManagerFactory.class);
        entityManager = mock(EntityManager.class);
        query = mock(Query.class);
        healthCheck = new DatabaseJpaHealthCheck(emf) {
            @Override
            public String checkQuery() {
                return "SELECT \'SUCCESS\' FROM DUAL";
            }

            @Override
            public Object checkResult() {
                return "SUCCESS";
            }
        };
    }

    @Test
    public void isHealthyIfDatabaseIsUp() {
        when(emf.createEntityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn("SUCCESS");

        assertThat(healthCheck.execute().isHealthy())
                .isTrue();
    }

    @Test
    public void isUnhealthyIfDatabaseIsDown() {
        when(emf.createEntityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getSingleResult()).thenReturn("FAILURE");

        final HealthCheck.Result result = healthCheck.execute();

        assertThat(result.isHealthy())
                .isFalse();

        assertThat(result.getMessage())
                .isEqualTo("Database is down.");
    }

    @Test
    public void isUnhealthyIfExceptionOnCheck() {
        when(emf.createEntityManager()).thenReturn(entityManager);
        when(entityManager.createNativeQuery(anyString())).thenThrow(IllegalArgumentException.class);
        when(query.getSingleResult()).thenReturn("SUCCESS");

        final HealthCheck.Result result = healthCheck.execute();

        assertThat(result.isHealthy())
                .isFalse();

        assertThat(result.getMessage())
                .isEqualTo("Database is down.");
    }

}
