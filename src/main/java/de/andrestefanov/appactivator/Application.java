package de.andrestefanov.appactivator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.regex.Pattern;

@SpringBootApplication
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

	// Mandatory parameters
    static final String SENDER_NAME = System.getenv("SENDER_NAME");
	static final String SENDER_EMAIL = System.getenv("SENDER_EMAIL");
    static final String SENDER_SMTP_SERVER = System.getenv("SENDER_SMTP_SERVER");
    static final String SENDER_SMTP_PORT = System.getenv("SENDER_SMTP_PORT");
    static final String SENDER_USERNAME = System.getenv("SENDER_USERNAME");
    static final String SENDER_PASSWORD = System.getenv("SENDER_PASSWORD");
    static final String FCM_PROJECT = System.getenv("FCM_PROJECT");
    static final String EMAIL_SUBJECT = System.getenv("EMAIL_SUBJECT");

	// Optional parameters
    static final String EMAIL_REGEX = System.getenv("EMAIL_REGEX");
    static final String DEBUG_EMAIL = System.getenv("DEBUG_EMAIL");

    static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	public static void main(String[] args) {
        if (SENDER_NAME == null) {
            logger.error("SENDER_NAME not defined");
            System.exit(1);
        }
	    if (SENDER_EMAIL == null) {
            logger.error("SENDER_EMAIL not defined");
            System.exit(1);
        }
        if (SENDER_SMTP_SERVER == null) {
            logger.error("SENDER_SMTP_SERVER not defined");
            System.exit(1);
        }
        if (SENDER_SMTP_PORT == null) {
            logger.error("SENDER_SMTP_PORT not defined");
            System.exit(1);
        }
        if (SENDER_USERNAME == null) {
            logger.error("SENDER_USERNAME not defined");
            System.exit(1);
        }
        if (SENDER_PASSWORD == null) {
            logger.error("SENDER_PASSWORD not defined");
            System.exit(1);
        }
        if (FCM_PROJECT == null) {
	        logger.error("FCM_PROJECT not defined");
	        System.exit(1);
        }
        if (EMAIL_SUBJECT == null) {
            logger.error("EMAIL_SUBJECT not defined");
            System.exit(1);
        }

		SpringApplication.run(Application.class, args);
	}
}
