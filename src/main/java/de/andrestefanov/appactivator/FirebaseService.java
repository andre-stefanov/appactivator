package de.andrestefanov.appactivator;

import org.springframework.web.bind.annotation.PathVariable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FirebaseService {

    @POST("https://fcm.googleapis.com/v1/projects/{project}/messages:send")
    Call<Void> sendMessage(@Header("Authorization") String token, @PathVariable String project, @Body PushNotification content);

}
