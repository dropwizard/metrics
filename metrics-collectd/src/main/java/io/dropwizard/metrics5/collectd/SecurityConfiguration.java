package io.dropwizard.metrics5.collectd;

public class SecurityConfiguration {

    private final byte[] username;
    private final byte[] password;
    private final SecurityLevel securityLevel;

    public SecurityConfiguration(byte[] username, byte[] password, SecurityLevel securityLevel) {
        this.username = username;
        this.password = password;
        this.securityLevel = securityLevel;
    }

    public static SecurityConfiguration none() {
        return new SecurityConfiguration(null, null, SecurityLevel.NONE);
    }

    public byte[] getUsername() {
        return username;
    }

    public byte[] getPassword() {
        return password;
    }

    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }
}
