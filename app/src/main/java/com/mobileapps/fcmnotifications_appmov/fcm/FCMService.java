package com.mobileapps.fcmnotifications_appmov.fcm;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.mobileapps.fcmnotifications_appmov.models.Message;
import com.mobileapps.fcmnotifications_appmov.util.NotificationUtil;

import org.json.JSONObject;


public class FCMService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        JSONObject object = new JSONObject(remoteMessage.getData());
        String json = object.toString();
        Message message = new Gson().fromJson(json, Message.class);

        NotificationUtil.showNotification(getApplicationContext(), "Nuevo mensaje", message.getMessage());
    }
}
