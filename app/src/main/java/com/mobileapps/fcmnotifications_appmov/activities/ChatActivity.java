package com.mobileapps.fcmnotifications_appmov.activities;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.mobileapps.fcmnotifications_appmov.R;
import com.mobileapps.fcmnotifications_appmov.fcm.FCMMessage;
import com.mobileapps.fcmnotifications_appmov.models.Chat;
import com.mobileapps.fcmnotifications_appmov.models.Message;
import com.mobileapps.fcmnotifications_appmov.models.User;
import com.mobileapps.fcmnotifications_appmov.util.HTTPSWebUtilDomi;

import java.util.Date;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private User user;
    private User contact;

    private Chat chat;

    private EditText messageET;
    private TextView messagesTV;
    private Button sendBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        user = (User) getIntent().getExtras().get("user");
        contact = (User) getIntent().getExtras().get("contact");

        messageET = findViewById(R.id.messageET);
        messagesTV = findViewById(R.id.messagesTV);
        messagesTV.setMovementMethod(new ScrollingMovementMethod());

        sendBtn = findViewById(R.id.sendBtn);


        FirebaseFirestore.getInstance().collection("users").document(user.getId()).collection("chats")
                .whereEqualTo("contactID", contact.getId()).get().addOnCompleteListener(
                        task->{
                            if(task.getResult().size() == 0){
                                createChat();
                            }else{
                                for(DocumentSnapshot doc : task.getResult()){
                                    chat = doc.toObject(Chat.class);
                                    break;
                                }
                            }
                            getMessages();
                        }
        );


        sendBtn.setOnClickListener(
                v->{
                    Message m = new Message(UUID.randomUUID().toString(), messageET.getText().toString(), user.getId(), new Date().getTime());
                    FirebaseFirestore.getInstance().collection("chats")
                            .document(chat.getId()).collection("messages").document(m.getId()).set(m);


                    //Enviar la noticiación


                    new Thread(
                            ()->{
                                FCMMessage<Message> fcmMessage = new FCMMessage<>("/topics/"+contact.getId(), m);
                                String json = new Gson().toJson(fcmMessage);
                                new HTTPSWebUtilDomi().POSTtoFCM(json);
                            }
                    ).start();

                }
        );

    }

    private void getMessages() {
        FirebaseFirestore.getInstance().collection("chats").document(chat.getId()).collection("messages").orderBy("date").limitToLast(10).addSnapshotListener(
                (value, error) -> {

                    for(DocumentChange dc : value.getDocumentChanges()){
                        switch (dc.getType()){
                            case ADDED:
                                Message message = dc.getDocument().toObject(Message.class);
                                messagesTV.append(message.getMessage()+ "\n\n");
                                break;
                        }
                    }

                }
        );
    }

    public void createChat(){
        String id = UUID.randomUUID().toString();
        chat = new Chat(id, contact.getId());
        Chat foreingChat = new Chat(id, user.getId());
        FirebaseFirestore.getInstance().collection("users").document(user.getId()).collection("chats").document(id).set(chat);
        FirebaseFirestore.getInstance().collection("users").document(contact.getId()).collection("chats").document(id).set(foreingChat);
    }
}