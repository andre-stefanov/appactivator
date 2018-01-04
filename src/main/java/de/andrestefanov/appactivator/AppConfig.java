package de.andrestefanov.appactivator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource(value = "file:/run/secrets/appactivator.properties", encoding = "UTF-8")
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private final Environment environment;

    @Autowired
    public AppConfig(Environment environment) {
        this.environment = environment;

        logger.info("email.name=" + getSenderName());
        logger.info("email.address=" + getSenderAddress());
        logger.info("email.validator.suffix=" + getEmailValidatorSuffix());
        logger.info("fcm=" + getFcmProject());
        logger.info("debug.email=" + debugMailAddress());
        logger.info("email.subject=" + getEmailSubject());
        logger.info("email.server=" + getEmailServer());
        logger.info("email.port=" + getEmailPort());
        logger.info("email.username=" + getEmailUsername());
        logger.info("email.password=" + getEmailPassword());
    }

    String getSenderName() {
        return environment.getProperty("email.name");
    }

    String getSenderAddress() {
        return environment.getProperty("email.address");
    }

    String getEmailValidatorSuffix() {
        return environment.getProperty("email.validator.suffix");
    }

    String getFcmProject() {
        return environment.getProperty("fcm");
    }

    String debugMailAddress() {
        return environment.getProperty("debug.email");
    }

    String getEmailSubject() {
        return environment.getProperty("email.subject");
    }

    String getEmailServer() {
        return environment.getProperty("email.server");
    }

    String getEmailPort() {
        return environment.getProperty("email.port");
    }

    String getEmailUsername() {
        return environment.getProperty("email.username");
    }

    String getEmailPassword() {
        return environment.getProperty("email.password");
    }
}
