package de.andrestefanov.appactivator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("file:/run/secrets/kjr-app-activator.properties")
public class AppConfig {

    private final Environment environment;

    @Autowired
    public AppConfig(Environment environment) {
        this.environment = environment;
    }

    public String getSenderName() {
        return environment.getProperty("email.name");
    }

    public String getSenderAddress() {
        return environment.getProperty("email.address");
    }

    public String getEmailValidatorSuffix() {
        return environment.getProperty("email.validator.suffix");
    }

    public String getFcmProject() {
        return environment.getProperty("fcm");
    }

    public String debugMailAdress() {
        return environment.getProperty("debug.email");
    }

    public String getEmailSubject() {
        return environment.getProperty("email.subject");
    }

    public String getEmailServer() {
        return environment.getProperty("email.server");
    }

    public String getEmailPort() {
        return environment.getProperty("email.port");
    }

    public String getEmailUsername() {
        return environment.getProperty("email.username");
    }

    public String getEmailPassword() {
        return environment.getProperty("email.password");
    }
}
