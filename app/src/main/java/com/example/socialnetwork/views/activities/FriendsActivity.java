package com.example.socialnetwork.views.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.Friends;
import com.example.socialnetwork.views.adapters.FriendsAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView myFriendsList;

    private DatabaseReference friendsRef, usersRef;
    private FirebaseAuth mAuth;

    private String online_user_id;

    private FriendsAdapter friendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        init();
        displayAllFriends();
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

        usersRef.child(online_user_id).child("userState").updateChildren(currentStateMap);
    }


    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");
    }

    private void displayAllFriends() {
        final FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(friendsRef, Friends.class)
                        .build();

        friendsAdapter = new FriendsAdapter(options, this, new FriendsAdapter.ItemClickListener() {
            @Override
            public void onItemClick(final String usersIDs, final String userName) {
                CharSequence options[] = new CharSequence[]{
                        userName + "'s Profile",
                        "Send Message"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                builder.setTitle("Select Options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                            profileIntent.putExtra("userKey", usersIDs);
                            startActivity(profileIntent);
                        }

                        if (which == 1) {
                            Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                            chatIntent.putExtra("userKey", usersIDs);
                            chatIntent.putExtra("userName", userName);
                            startActivity(chatIntent);
                        }
                    }
                });
                builder.show();
            }
        });
        friendsAdapter.startListening();
        myFriendsList.setAdapter(friendsAdapter);
    }

    private void init() {
        mToolbar = findViewById(R.id.my_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Friends");

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        myFriendsList = findViewById(R.id.friends_list);
        myFriendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendsList.setLayoutManager(linearLayoutManager);

    }
}
