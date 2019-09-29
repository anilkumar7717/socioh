package com.example.socialnetwork.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.Posts;
import com.example.socialnetwork.views.adapters.PostsAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView navProfileImg;
    private TextView navProfileUserName;
    private ImageButton addNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, postsRef, likesRef;

    String currentUserId;

    public PostsAdapter postsAdapter;
    boolean likeChecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        addNewPostButton = findViewById(R.id.add_new_post_button);

        drawerLayout = findViewById(R.id.drawable_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        postList = findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View header = navigationView.getHeaderView(0);
        navProfileImg = header.findViewById(R.id.nav_profile_image);
        navProfileUserName = header.findViewById(R.id.nav_user_full_name);


        /* get user profile information to show in navigation drawer */
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /* checking if record for the current user exist */
                if (dataSnapshot.exists()) {
                    /* checking if fullname row exists for the current user */
                    if (dataSnapshot.hasChild("fullname")) {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        navProfileUserName.setText(fullname);
                    }
                    /* checking if profileimage row exists for the current user */
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImg);
                    } /* then show message if both fullname & profileimage*/ else {

//                        Toast.makeText(MainActivity.this, "Profile name do not exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });

        displayAllUserPost();
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

        usersRef.child(currentUserId).child("userState").updateChildren(currentStateMap);
    }


    /* --- function to display all users posts --*/
    private void displayAllUserPost() {
        /* --- function to display all users posts --*/
        Query sortPostInDescendingOrder = postsRef.orderByChild("counter");

        final FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(sortPostInDescendingOrder, Posts.class)
                        .build();


        postsAdapter = new PostsAdapter(options, this, new PostsAdapter.ItemClickListener() {

            @Override
            public void onItemClick(String postKey) {
                Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                System.out.println("----key-----" + postKey);
                clickPostIntent.putExtra("postkey", postKey);
                startActivity(clickPostIntent);
            }

            @Override
            public void onLikeButtonClick(final String postKey) {
                likeChecker = true;
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (likeChecker) {
                            if (dataSnapshot.child(postKey).hasChild(currentUserId)) {
                                likesRef.child(postKey).child(currentUserId).removeValue();
                                likeChecker = false;
                            } else {
                                likesRef.child(postKey).child(currentUserId).setValue(true);
                                likeChecker = false;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCommentButtonClick(String postKey) {
                Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                commentsIntent.putExtra("postkey", postKey);
                startActivity(commentsIntent);
            }
        });


        postsAdapter.startListening();
        postList.setAdapter(postsAdapter);
       // updateUserStatus("online");
    }

    private void sendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {
            checkUserExistence();
        }
    }

    private void checkUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id)) {
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(setupIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToFriendsActivity() {
        Intent friendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void sendUserToSettingActivity() {
        Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(settingIntent);
    }

    private void sendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    private void sendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawable_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_post:
                sendUserToPostActivity();
                break;

            case R.id.nav_profile:
                sendUserToProfileActivity();
                break;

            case R.id.nav_friends:
                sendUserToFriendsActivity();
                break;

            case R.id.nav_find_friends:
                sendUserToFindFriendsActivity();
                break;

            case R.id.nav_messages:
                sendUserToFriendsActivity();
                break;

            case R.id.nav_settings:
                sendUserToSettingActivity();
                break;

            case R.id.nav_logout:
                updateUserStatus("offline");
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
