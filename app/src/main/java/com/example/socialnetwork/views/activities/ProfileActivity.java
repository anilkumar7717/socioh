package com.example.socialnetwork.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.socialnetwork.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private Toolbar mToolabr;

    private TextView userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDob, myPosts, myFriends;
    private CircleImageView userProfileImage;
    private Button myFriendsButton, myPostsButton;

    private DatabaseReference profileUserRef, friendsRef, postsRef;
    private FirebaseAuth mAuth;

    private String currentUserId;
    private int countFriends = 0, countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();

        profileUserRef.addValueEventListener(new ValueEventListener() {
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

        myFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToFriendsActivity();
            }
        });

        friendsRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    myFriends.setText(String.valueOf(countFriends));
                } else {
                    myFriends.setText("0");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToMyPostActivity();
            }
        });

        postsRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            countPosts = (int) dataSnapshot.getChildrenCount();
                            myPosts.setText(String.valueOf(countPosts));
                        } else {
                            myPosts.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mToolabr = findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolabr);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = findViewById(R.id.my_username);
        userProfileName = findViewById(R.id.my_profile_full_name);
        userStatus = findViewById(R.id.my_profile_status);
        userCountry = findViewById(R.id.my_country);
        userGender = findViewById(R.id.my_gender);
        userRelation = findViewById(R.id.my_relationship_status);
        userDob = findViewById(R.id.my_dob);
        userProfileImage = findViewById(R.id.my_profile_pic);
        myPosts = findViewById(R.id.myPostsCount);
        myFriends = findViewById(R.id.myFriendsCount);
        myFriendsButton = findViewById(R.id.my_friends_button);
        myPostsButton = findViewById(R.id.my_posts_button);
    }

    private void sendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void sendUserToMyPostActivity() {
        Intent friendsIntent = new Intent(ProfileActivity.this, MyPostActivity.class);
        startActivity(friendsIntent);
    }
}
