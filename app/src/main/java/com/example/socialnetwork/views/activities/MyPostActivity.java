package com.example.socialnetwork.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.Posts;
import com.example.socialnetwork.views.adapters.PostsAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MyPostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;

    private FirebaseAuth mAuth;
    private DatabaseReference postsRef, likesRef;

    private String currentUserID;

    private PostsAdapter postsAdapter;
    boolean likeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);
        init();
        displayAllMyPosts();
    }


    private void init() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        mToolbar = findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostsList = findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);
    }

    private void displayAllMyPosts() {
        Query myPostQuery = postsRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff");
        final FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(myPostQuery, Posts.class)
                        .build();


        postsAdapter = new PostsAdapter(options, this, new PostsAdapter.ItemClickListener() {

            @Override
            public void onItemClick(String postKey) {
                Intent clickPostIntent = new Intent(MyPostActivity.this, ClickPostActivity.class);
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
                            if (dataSnapshot.child(postKey).hasChild(currentUserID)) {
                                likesRef.child(postKey).child(currentUserID).removeValue();
                                likeChecker = false;
                            } else {
                                likesRef.child(postKey).child(currentUserID).setValue(true);
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
                Intent commentsIntent = new Intent(MyPostActivity.this, CommentsActivity.class);
                commentsIntent.putExtra("postkey", postKey);
                startActivity(commentsIntent);
            }
        });


        postsAdapter.startListening();
        myPostsList.setAdapter(postsAdapter);
    }
}
