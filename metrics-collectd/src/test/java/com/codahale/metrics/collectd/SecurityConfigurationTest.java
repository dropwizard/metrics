package com.codahale.metrics.collectd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SecurityConfigurationTest {

    private byte[] username;
    private byte[] password;
    private SecurityLevel securityLevel;

    @BeforeEach
    public void setUp() {
        username = "admin".getBytes();
        password = "password".getBytes();
        securityLevel = SecurityLevel.SIGN;
    }

    /**
     * Test the constructor and getters of the SecurityConfiguration class.
     */
    @Test
    public void testConstructorAndGetters() {
        // When
        SecurityConfiguration config = new SecurityConfiguration(username, password, securityLevel);

        // Then
        assertArrayEquals(username, config.getUsername(), "Username matched");
        assertArrayEquals(password, config.getPassword(), "Password matched");
        assertEquals(securityLevel, config.getSecurityLevel(), "Security level matched");
    }

    /**
     * Test the none() factory method of SecurityConfiguration to ensure it creates
     * a configuration with no security settings.
     */
    @Test
    public void testNoneSecurityConfiguration() {
        // When
        SecurityConfiguration config = SecurityConfiguration.none();

        // Then
        assertNull(config.getUsername(), "Username is null");
        assertNull(config.getPassword(), "Password is null");
        assertEquals(SecurityLevel.NONE, config.getSecurityLevel(), "Security level is NONE");
    }
}
