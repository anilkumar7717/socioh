package com.example.socialnetwork.views.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.socialnetwork.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class PostActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ProgressDialog loadingBar;
    private ImageView viewselectPostImage;
    private ImageView updatePostButton, selectPostImage;
    private CircleImageView profileImg;
    private EditText postDescription;
    private TextView userName;

    public static final int GALLERY_PICK = 1;
    private Uri imageUri;
    private String description;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;

    private StorageReference postImagesRef;
    private DatabaseReference userRef, postRef;
    private FirebaseAuth mAuth;


    private long countPosts = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        init();

        /*--click listener for open gallery--*/
        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        /*--click listener for post button--*/
        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });

        userRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        userName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImg);
                        // Glide.with(MainActivity.this).load(image).centerCrop().placeholder(R.drawable.profile).into(navProfileImg);
                    } else {
                        Toast.makeText(PostActivity.this, "Profile name do not exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /*--function to check image and post description are valid--*/
    private void validatePostInfo() {
        description = postDescription.getText().toString();
        if (imageUri == null) {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please say something about your post...", Toast.LENGTH_SHORT).show();
        } else {
            if (imageUri!=null){
                loadingBar.setTitle("Add New Post");
                loadingBar.setMessage("Please wait, while we are updating your New Post...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                storingImageToFireBaseStorage();
            }

        }
    }

    /*--function post image store into fireBase storage--*/
    private void storingImageToFireBaseStorage() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calForTime.getTime());
        postRandomName = saveCurrentDate + saveCurrentTime;

        /*--creating filePath for post image--*/
        final StorageReference filePath = postImagesRef.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");


        /*--completion listener for upload file into fireBase storage--*/
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                Toast.makeText(PostActivity.this, "image uploaded successfully to storage...", Toast.LENGTH_SHORT).show();
                                savePostInformationToDataBase();
                            }
                        });
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(PostActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    }

    private void savePostInformationToDataBase() {
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    countPosts = dataSnapshot.getChildrenCount();
                } else {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        userRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap<String, Object> postMap = new HashMap<>();
                    postMap.put("uid", current_user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", description);
                    postMap.put("postImage", downloadUrl);
                    postMap.put("profileimage", userProfileImage);
                    postMap.put("fullname", userFullName);
                    postMap.put("timestamp", ServerValue.TIMESTAMP);
                    postMap.put("counter", countPosts);

                    postRef.child(current_user_id + postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(PostActivity.this, "Error occurred while updating your post.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*--function to open gallery for choose image--*/
    private void openGallery() {
        /*--opening gallery to select the image--*/
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);
    }

    /*--function for initialization--*/
    private void init() {
        postImagesRef = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        mToolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        viewselectPostImage = findViewById(R.id.view_select_post_image);
        selectPostImage = findViewById(R.id.select_post_image);
        updatePostButton = findViewById(R.id.update_post_button);
        postDescription = findViewById(R.id.post_description);
        profileImg = findViewById(R.id.post_profile_image);
        userName = findViewById(R.id.post_user_name);
        loadingBar = new ProgressDialog(this);


    }

    /*--callback function for gallery intent--*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            viewselectPostImage.setVisibility(View.VISIBLE);
            viewselectPostImage.setImageURI(imageUri);
            Glide.with(PostActivity.this).load(imageUri).centerCrop().placeholder(R.drawable.profile).into(viewselectPostImage);
        }
    }

    /*--backPress function for navigate from PostActivity to MainActivity--*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    /*--function for navigate from PostActivity to MainActivity--*/
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finishAffinity();
    }
}
