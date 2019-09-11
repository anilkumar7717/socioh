package com.example.socialnetwork.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.socialnetwork.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDob;
    private CircleImageView userProfileImage;
    private Button personSendRequest, personDeclineRequest;

    private DatabaseReference friendRequestRef, usersRef, friendsRef;
    private FirebaseAuth mAuth;

    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        if (b != null) {
            receiverUserId = b.get("userKey").toString();
            System.out.println("----key----" + receiverUserId);
        }

        init();


        usersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileimage")) {
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(myProfileImage).placeholder(R.drawable.add_post_high).into(userProfileImage);
                    }

                    if (dataSnapshot.hasChild("username")) {
                        String myUserName = dataSnapshot.child("username").getValue().toString();
                        userName.setText(myUserName);
                    }

                    if (dataSnapshot.hasChild("fullname")) {
                        String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                        userProfileName.setText(myProfileName);
                    }

                    if (dataSnapshot.hasChild("status")) {
                        String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                        userStatus.setText(myProfileStatus);
                    }

                    if (dataSnapshot.hasChild("dob")) {
                        String myDOB = dataSnapshot.child("dob").getValue().toString();
                        userDob.setText(myDOB);
                    }

                    if (dataSnapshot.hasChild("country")) {
                        String myCountry = dataSnapshot.child("country").getValue().toString();
                        userCountry.setText(myCountry);
                    }

                    if (dataSnapshot.hasChild("gender")) {
                        String myGender = dataSnapshot.child("gender").getValue().toString();
                        userGender.setText(myGender);
                    }

                    if (dataSnapshot.hasChild("relationshipstatus")) {
                        String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();
                        userRelation.setText(myRelationStatus);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        personDeclineRequest.setVisibility(View.INVISIBLE);
        personDeclineRequest.setEnabled(false);

        /*-- checking if current User Id is not equal to person profile
        Id to set Visible and Invisible Send And Accept request buttons --*/
        if (!senderUserId.equals(receiverUserId)) {
            personSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    personSendRequest.setEnabled(false);
                    if (CURRENT_STATE.equals("not_friends")) {
                        sendFriendRequestToPerson();
                    }

                    if (CURRENT_STATE.equals("request_sent")) {
                        cancelFriendRequest();
                    }

                    if (CURRENT_STATE.equals("request_received")) {
                        acceptFriendRequest();
                    }

                    if (CURRENT_STATE.equals("friends")) {
                        unFriendExistingFriend();
                    }

                }
            });

        } else {
            personSendRequest.setVisibility(View.GONE);
            personDeclineRequest.setVisibility(View.GONE);
        }

    }

    /*-- function for UnFriend Existing Friend --*/
    private void unFriendExistingFriend() {
        friendsRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                personSendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                personSendRequest.setText("Send Request");

                                                personDeclineRequest.setVisibility(View.INVISIBLE);
                                                personDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    /*-- function for Accept friend request --*/
    private void acceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        friendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    friendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        friendRequestRef.child(senderUserId).child(receiverUserId).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            friendRequestRef.child(receiverUserId).child(senderUserId).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                personSendRequest.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                personSendRequest.setText("Unfriend");

                                                                                personDeclineRequest.setVisibility(View.INVISIBLE);
                                                                                personDeclineRequest.setEnabled(false);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });
    }

    /*-- function for cancel friend request --*/
    private void cancelFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                personSendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                personSendRequest.setText("Send Request");

                                                personDeclineRequest.setVisibility(View.INVISIBLE);
                                                personDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    /*-- function for send request to person--*/
    private void sendFriendRequestToPerson() {
        friendRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                personSendRequest.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                personSendRequest.setText("Cancel Request");

                                                personDeclineRequest.setVisibility(View.INVISIBLE);
                                                personDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        userName = findViewById(R.id.person_username);
        userProfileName = findViewById(R.id.person_full_name);
        userStatus = findViewById(R.id.person_status);
        userCountry = findViewById(R.id.person_country);
        userGender = findViewById(R.id.person_gender);
        userRelation = findViewById(R.id.person_relationship_status);
        userDob = findViewById(R.id.person_dob);
        userProfileImage = findViewById(R.id.person_profile_pic);
        personSendRequest = findViewById(R.id.person_send_request);
        personDeclineRequest = findViewById(R.id.person_decline_request);


        CURRENT_STATE = "not_friends";
        maintananceOfButtons();

    }

    /*-- function for set send request Text to Cancel friend request if user Already sent request --*/
    private void maintananceOfButtons() {
        friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserId)) {
                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {
                        CURRENT_STATE = "request_sent";
                        personSendRequest.setText("Cancel Request");

                        personDeclineRequest.setVisibility(View.INVISIBLE);
                        personDeclineRequest.setEnabled(false);
                    } else if (request_type.equals("received")) {
                        CURRENT_STATE = "request_received";
                        personSendRequest.setText("Accept Request");

                        personDeclineRequest.setVisibility(View.VISIBLE);
                        personDeclineRequest.setEnabled(true);

                        /*-- Decline Friend request --*/
                        personDeclineRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelFriendRequest();
                            }
                        });
                    }
                } else {
                    friendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserId)) {
                                CURRENT_STATE = "friends";
                                personSendRequest.setText("Unfriend");

                                personDeclineRequest.setVisibility(View.INVISIBLE);
                                personDeclineRequest.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
