package de.andrestefanov.appactivator;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import org.simplejavamail.email.Email;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.ServerConfig;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public Controller() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/v1/projects/" + FCM_PROJECT + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        firebase = retrofit.create(FirebaseService.class);
    }

    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public ResponseEntity<?> validate(@RequestParam String email, @RequestParam String token) {
        if (!validateEmailAddress(email)) {
            return new ResponseEntity<>("invalid email", HttpStatus.BAD_REQUEST);
        } else if (!StringUtils.isEmpty(token)) {
            return new ResponseEntity<>("invalid token", HttpStatus.BAD_REQUEST);
        } else {
            sendMail(email, token);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/activate", method = RequestMethod.GET)
    public ResponseEntity<?> activate(@RequestParam String token) {
        if (!StringUtils.isEmpty(token)) {
            return new ResponseEntity<>("invalid token", HttpStatus.BAD_REQUEST);
        } else {
            try {
                PushNotification notification = new PushNotification();
                notification.message = new PushNotification.Message();
                notification.message.token = token;
                notification.message.data = new PushNotification.Message.Data();
                notification.message.data.activation = "activated";

                Response<Void> response = firebase.sendMessage(getAccessToken(), FCM_PROJECT, notification).execute();

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
        File serviceAccountFile = new File("/opt/appactivator/service-account.json");

        GoogleCredential googleCredential = GoogleCredential
                .fromStream(new FileInputStream(serviceAccountFile))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredential.refreshToken();
        return "Bearer " + googleCredential.getAccessToken();
    }

    private static boolean validateEmailAddress(String emailAddress) {
        return (!StringUtils.isEmpty(emailAddress) &&
               VALID_EMAIL_ADDRESS_REGEX.matcher(emailAddress).find() &&
                (!StringUtils.isEmpty(EMAIL_REGEX) && emailAddress.matches(EMAIL_REGEX))) ||
                (DEBUG_EMAIL != null && DEBUG_EMAIL.equals(emailAddress));
    }

    private void sendMail(String address, String token) {
        Email email = new Email();

        email.setFromAddress(SENDER_NAME, SENDER_EMAIL);
        email.addToRecipients(address);

        email.setSubject(EMAIL_SUBJECT);
        try {
            email.setTextHTML(fileContentToString(emailBody).replace("USER_PUSH_TOKEN", token));
        } catch (IOException e) {
            logger.warn("Failed to load email.html");

        }

        new Mailer(
                new ServerConfig(
                        SENDER_SMTP_SERVER,
                        Integer.valueOf(SENDER_SMTP_PORT),
                        SENDER_USERNAME,
                        SENDER_PASSWORD),
                TransportStrategy.SMTP_TLS
        ).sendMail(email);
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
