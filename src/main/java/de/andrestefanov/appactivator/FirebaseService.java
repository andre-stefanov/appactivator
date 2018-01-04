package de.andrestefanov.appactivator;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FirebaseService {

    @POST("https://fcm.googleapis.com/v1/projects/{project}/messages:send")
    Call<Void> sendMessage(@Header("Authorization") String token, @Path("project") String project, @Body PushNotification content);

}
