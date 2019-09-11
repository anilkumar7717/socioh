package com.example.socialnetwork.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.Messages;
import com.example.socialnetwork.views.adapters.MessagesAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar chatToolbar;
    private ImageButton sendMessageButton, sendImageFileButton;
    private EditText userMessageInput;

    private RecyclerView userMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private String messageReceiverId, messageReceiverName, messageSenderId,saveCurrentDate,saveCurrentTime;

    private TextView receiverName,userLastSeen;
    private CircleImageView receiverProfileImage;

    private DatabaseReference rootRef,usersRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        if (b != null) {
            messageReceiverId = b.get("userKey").toString();
            messageReceiverName = b.get("userName").toString();
        }
        init();
        displayReceiverInfo();
        fetchMessages();
    }

    private void fetchMessages() {
        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()){
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messagesAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void updateUserStatus(String state){
        String saveCurrentDate,saveCurrentTime;
        Calendar calForeDate =Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForeDate.getTime());

        Calendar calForTime =Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map<String,Object> currentStateMap = new HashMap<>();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        usersRef.child(messageSenderId).child("userState").updateChildren(currentStateMap);
    }

    private void displayReceiverInfo() {
        receiverName.setText(messageReceiverName);
        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("profileimage")){
                        final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                        if (dataSnapshot.hasChild("userState")){
                            final String type = dataSnapshot.child("userState").child("type").getValue().toString();
                            final String lastDate = dataSnapshot.child("userState").child("date").getValue().toString();
                            final String lastTime = dataSnapshot.child("userState").child("time").getValue().toString();
                            if (type.equals("online")){
                                userLastSeen.setText("online");
                            } else {
                                String lastSeenTxt = "last seen: " + lastTime + " " + lastDate;
                                userLastSeen.setText(lastSeenTxt);
                            }
                        } else {
                            userLastSeen.setText("Say, hi to chat");
                        }

                        Picasso.get().load(profileImage).placeholder(R.drawable.add_post_high).into(receiverProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatToolbar = findViewById(R.id.chat_bar_toolbar);
        setSupportActionBar(chatToolbar);

       /* -- Connect custom toolbar view to chat activity xml file --*/
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.custom_app_bar_layout,null);
        actionBar.setCustomView(action_bar_view);

        receiverName = chatToolbar.findViewById(R.id.custom_profile_name);
        userLastSeen = chatToolbar.findViewById(R.id.custom_user_last_seen);
        receiverProfileImage = chatToolbar.findViewById(R.id.custom_profile_image);

        userMessageInput = findViewById(R.id.input_message);
        userMessageList = findViewById(R.id.messages_list_users);
        sendMessageButton = findViewById(R.id.send_message_button);
        sendMessageButton.setOnClickListener(this);
        sendImageFileButton = findViewById(R.id.send_image_file_button);
        sendImageFileButton.setOnClickListener(this);

        messagesAdapter = new MessagesAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messagesAdapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_message_button:
                sendMessage();
                break;

            case R.id.send_image_file_button:
                break;
        }
    }

    private void sendMessage() {
        updateUserStatus("online");
        String messageText = userMessageInput.getText().toString();
        if (TextUtils.isEmpty(messageText) && !messageText.equals("")){
            Toast.makeText(this, "Please type a message first....", Toast.LENGTH_SHORT).show();
        } else {
            String message_sender_ref = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String message_receiver_ref = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();
            String message_push_id = user_message_key.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calForTime.getTime());

            Map<String,Object> messageTextBody = new HashMap<>();
            messageTextBody.put("message",messageText);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            Map<String,Object> messageBodyDetails = new HashMap<>();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }
                }
            });

        }
    }
}
