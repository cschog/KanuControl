package com.kcserver.vermietung.config;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kc.mail.smtp")
public class SmtpMailProperties {
    private boolean enabled;
    private boolean testMode;
    private String testRecipient;
    private String host;
    private int port = 587;
    private String username;
    private String password;
    private String from;
    private boolean startTls = true;
    private boolean ssl;

    public boolean isTestMode() { return testMode; }
    public void setTestMode(boolean value) { testMode = value; }
    public String getTestRecipient() { return testRecipient; }
    public void setTestRecipient(String value) { testRecipient = value; }

    /** Require exactly one mailbox; never fall back to the real recipient. */
    public String requireTestRecipient() {
        try {
            if (testRecipient == null || testRecipient.isBlank()
                    || testRecipient.contains("\r") || testRecipient.contains("\n")) {
                throw new AddressException("Missing or multiline address");
            }
            String value = testRecipient.trim();
            InternetAddress address = new InternetAddress(value, true);
            address.validate();
            if (address.isGroup() || !value.equals(address.getAddress())) {
                throw new AddressException("Expected one plain mailbox");
            }
            return value;
        } catch (AddressException ex) {
            throw new IllegalStateException("KC_SMTP_TEST_RECIPIENT muss bei aktiviertem KC_SMTP_TEST_MODE genau eine gültige E-Mail-Adresse enthalten.");
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public boolean isStartTls() { return startTls; }
    public void setStartTls(boolean startTls) { this.startTls = startTls; }
    public boolean isSsl() { return ssl; }
    public void setSsl(boolean ssl) { this.ssl = ssl; }
}
