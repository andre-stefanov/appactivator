package de.andrestefanov.appactivator;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import org.simplejavamail.email.Email;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.ServerConfig;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.*;
import java.util.Collections;

import static de.andrestefanov.appactivator.Application.*;

@RestController
public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private FirebaseService firebase;

    private File emailBody = new File("/opt/appactivator/email.html");

    private File successBody = new File("/opt/appactivator/success.html");

    private AppConfig config;

    private Mailer mailer;

    @Autowired
    public Controller(AppConfig config) {
        this.config = config;
        this.mailer =  new Mailer(
                new ServerConfig(
                        config.getEmailServer(),
                        Integer.valueOf(config.getEmailPort()),
                        config.getEmailUsername(),
                        config.getEmailPassword()),
                TransportStrategy.SMTP_TLS
        );

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/v1/projects/" + config.getFcmProject() + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        firebase = retrofit.create(FirebaseService.class);
    }

    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public ResponseEntity<?> validate(@RequestParam String email, @RequestParam String token) {
        if (!validateEmailAddress(email)) {
            logger.warn("provided email " + email + " is not valid");
            return new ResponseEntity<>("invalid email: " + email, HttpStatus.BAD_REQUEST);
        } else if (StringUtils.isEmpty(token)) {
            logger.warn("provided token " + token + " is not valid");
            return new ResponseEntity<>("invalid token: " + token, HttpStatus.BAD_REQUEST);
        } else {
            sendMail(email, token);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/activate", method = RequestMethod.GET)
    public ResponseEntity<?> activate(@RequestParam String token) {
        if (StringUtils.isEmpty(token)) {
            return new ResponseEntity<>("invalid token: " + token, HttpStatus.BAD_REQUEST);
        } else {
            try {
                PushNotification notification = new PushNotification();
                notification.message = new PushNotification.Message();
                notification.message.token = token;
                notification.message.data = new PushNotification.Message.Data();
                notification.message.data.activation = "activated";

                Response<Void> response = firebase.sendMessage(getAccessToken(), config.getFcmProject(), notification).execute();

                if (response.isSuccessful()) {
                    return new ResponseEntity<>(fileContentToString(successBody), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(response.message(), HttpStatus.valueOf(response.code()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private String getAccessToken() throws IOException {
        File serviceAccountFile = new File("/run/secrets/service-account.json");

        GoogleCredential googleCredential = GoogleCredential
                .fromStream(new FileInputStream(serviceAccountFile))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredential.refreshToken();
        return "Bearer " + googleCredential.getAccessToken();
    }

    private boolean validateEmailAddress(String emailAddress) {
        return (!StringUtils.isEmpty(emailAddress) &&
               VALID_EMAIL_ADDRESS_REGEX.matcher(emailAddress).find() &&
                emailAddress.endsWith(config.getEmailValidatorSuffix())) ||
                (!StringUtils.isEmpty(config.debugMailAddress()) && config.debugMailAddress().equals(emailAddress));
    }

    private void sendMail(String address, String token) {
        Email email = new Email();

        email.setFromAddress(config.getSenderName(), config.getSenderAddress());
        email.addToRecipients(address);

        email.setSubject(config.getEmailSubject());
        try {
            email.setTextHTML(fileContentToString(emailBody).replace("USER_PUSH_TOKEN", token));
        } catch (IOException e) {
            logger.warn("Failed to load email.html");
        }

        this.mailer.sendMail(email);
    }

    private String fileContentToString(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        String content;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new InputStreamReader(new FileInputStream(file));
            br = new BufferedReader(isr);
            while ((content = br.readLine()) != null) {
                sb.append(content);
            }
        } catch (IOException ioe) {
            logger.error("Failed to read file", ioe);
            throw ioe;
        } finally {
            if (isr != null)
                isr.close();

            if (br != null)
                br.close();
        }

        return sb.toString();
    }
}
