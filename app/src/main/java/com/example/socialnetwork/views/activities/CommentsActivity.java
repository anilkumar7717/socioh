package com.example.socialnetwork.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.Comments;
import com.example.socialnetwork.views.adapters.CommentsAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton postCommentButton;
    private EditText commentInputText;
    private RecyclerView commentsList;

    String post_Key, current_user_id;

    private DatabaseReference usersRef, postRef;
    private FirebaseAuth mAuth;

    private CommentsAdapter commentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Intent intentExtra = getIntent();
        Bundle b = intentExtra.getExtras();
        if (b != null) {
            post_Key = b.get("postkey").toString();
            System.out.println("----comments-key----" + post_Key);
        }
        init();

        displayAllUserComments();

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("username")) {
                                String userName = dataSnapshot.child("username").getValue().toString();
                                validateComment(userName);
                                commentInputText.setText("");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    private void validateComment(String userName) {
        String commentText = commentInputText.getText().toString();
        if (TextUtils.isEmpty(commentText) && commentText.equals("")) {
            Toast.makeText(this, "please write text to comment...", Toast.LENGTH_SHORT).show();
        } else {

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            final String randomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap<String, Object> commentsMap = new HashMap<>();
            commentsMap.put("uid", current_user_id);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("username", userName);

            postRef.child(randomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CommentsActivity.this, "you have commented successfully...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CommentsActivity.this, "Error Occurred, try again...", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_Key).child("Comments");

        postCommentButton = findViewById(R.id.post_comment_btn);
        commentInputText = findViewById(R.id.comment_input);
        commentsList = findViewById(R.id.comments_lists);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);
    }

    private void displayAllUserComments() {
        final FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(postRef, Comments.class)
                        .build();

        commentsAdapter = new CommentsAdapter(options, this);
        commentsAdapter.startListening();
        commentsList.setAdapter(commentsAdapter);
    }

}
