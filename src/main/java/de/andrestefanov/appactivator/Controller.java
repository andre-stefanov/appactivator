package de.andrestefanov.appactivator;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

@RestController
public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private FirebaseService firebase;

    private final MailService mailService;

    private Configuration freeMarkerConfig;

    private FirebaseConfig firebaseConfig;

    @Autowired
    public Controller(MailService mailService, FirebaseConfig firebaseConfig, @Qualifier("freeMarkerConfiguration") Configuration freeMarkerConfig) {
        this.firebaseConfig = firebaseConfig;
        this.mailService = mailService;
        this.freeMarkerConfig = freeMarkerConfig;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/v1/projects/" + firebaseConfig.getProject() + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        firebase = retrofit.create(FirebaseService.class);
    }

    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public ResponseEntity<?> validate(@RequestParam String email, @RequestParam String token) throws Exception {
        if (StringUtils.isEmpty(token)) {
            logger.warn("provided token " + token + " is not valid");
            return new ResponseEntity<>("invalid token: " + token, HttpStatus.BAD_REQUEST);
        } else {
            mailService.prepareAndSendValidation(email, "token: " + token);
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

                Response<Void> response = firebase.sendMessage(getAccessToken(), firebaseConfig.getProject(), notification).execute();

                if (response.isSuccessful()) {
                    Template template = freeMarkerConfig.getTemplate("success.ftl");
                    return new ResponseEntity<>(template.toString(), HttpStatus.OK);
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
        File serviceAccountFile = new File(firebaseConfig.getConfig());

        GoogleCredential googleCredential = GoogleCredential
                .fromStream(new FileInputStream(serviceAccountFile))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredential.refreshToken();
        return "Bearer " + googleCredential.getAccessToken();
    }
}
