package com.kcserver.vermietung.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(SmtpMailProperties.class)
public class SmtpMailConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "kc.mail.smtp", name = "enabled", havingValue = "true")
    public JavaMailSender kcJavaMailSender(SmtpMailProperties config) {
        if (isBlank(config.getHost()) || isBlank(config.getFrom())) {
            throw new IllegalStateException("KC_SMTP_HOST und KC_SMTP_FROM müssen für den direkten Mailversand gesetzt sein.");
        }
        if (isBlank(config.getUsername()) != isBlank(config.getPassword())) {
            throw new IllegalStateException("KC_SMTP_USERNAME und KC_SMTP_PASSWORD müssen gemeinsam gesetzt sein.");
        }
        if (config.isSsl() && config.isStartTls()) {
            throw new IllegalStateException("KC_SMTP_SSL und KC_SMTP_STARTTLS dürfen nicht gleichzeitig aktiviert sein.");
        }
        if (config.getPort() < 1 || config.getPort() > 65535) {
            throw new IllegalStateException("KC_SMTP_PORT muss zwischen 1 und 65535 liegen.");
        }

        if (config.isTestMode()) config.requireTestRecipient();

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getHost().trim());
        sender.setPort(config.getPort());
        sender.setDefaultEncoding("UTF-8");
        if (!isBlank(config.getUsername())) {
            sender.setUsername(config.getUsername());
            sender.setPassword(config.getPassword());
        }

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", Boolean.toString(!isBlank(config.getUsername())));
        properties.put("mail.smtp.starttls.enable", Boolean.toString(config.isStartTls()));
        properties.put("mail.smtp.starttls.required", Boolean.toString(config.isStartTls()));
        properties.put("mail.smtp.ssl.enable", Boolean.toString(config.isSsl()));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        return sender;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
