package de.andrestefanov.appactivator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.regex.Pattern;

@SpringBootApplication
@EnableConfigurationProperties
public class Application {

    static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
