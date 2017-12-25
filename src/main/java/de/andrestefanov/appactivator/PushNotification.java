package de.andrestefanov.appactivator;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PushNotification {

    @SerializedName("message")
    @Expose
    public Message message;

    public static class Message {

        @SerializedName("token")
        @Expose
        public String token;

        @SerializedName("data")
        @Expose
        public Data data;

        public static class Data {

            @SerializedName("activation")
            @Expose
            public String activation;
        }
    }
}
