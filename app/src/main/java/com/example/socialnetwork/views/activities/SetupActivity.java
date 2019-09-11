package com.example.socialnetwork.views.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {

    private EditText userName, fullName, countryName;
    private Button saveInformationButton;
    private CircleImageView profileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference userProfileRef;

    String currentUserId;
    final static int GALLERY_PICK_INTENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        init();

        /*--click listener for save information--*/
        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });

        /*--click listener for change user profile image--*/
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*--opening gallery to select the image--*/
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK_INTENT);
            }
        });

        /*--Event Listener for show user profile image into navigation drawer header*/
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /*--check if dataSnapshot exists then show load profile image using Picasso*/
                if (dataSnapshot.exists()) {
                    /*--check if profileimage child exists in user node of User table*/
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                        // Glide.with(SetupActivity.this).load(image).centerCrop().placeholder(R.drawable.profile).into(profileImage);
                    } else {
                        Toast.makeText(SetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*--Function for initialization*/
    private void init() {
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileRef = FirebaseStorage.getInstance().getReference().child("profile Images");

        userName = findViewById(R.id.setup_username);
        fullName = findViewById(R.id.setup_full_name);
        countryName = findViewById(R.id.setup_country_name);
        saveInformationButton = findViewById(R.id.setup_information_button);
        profileImage = findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);
    }


    /*--Function for save User information into fireBase*/
    private void saveAccountSetupInformation() {
        String username = userName.getText().toString();
        String fullname = fullName.getText().toString();
        String country = countryName.getText().toString();

        /*--check if username is empty*/
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
        }

        /*--check if fullname is empty*/
        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show();
        }

        /*--check if country is empty*/
        if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please write your country...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, while we are creating your new Account...");
            loadingBar.show();
            /*--setting user data to User Model--*/
            User user = new User(username, "", "", "", "", fullname, "", country);
            /*--adding value to fireBase user table*/
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("username", user.getUsername());
            userMap.put("fullname", user.getFullname());
            userMap.put("country", user.getCountry());
            userMap.put("status", user.getStatus());
            userMap.put("gender", user.getGender());
            userMap.put("dob", user.getDob());
            userMap.put("relationshipstatus", user.getRelationshipstatus());
            /*--update user information into user table--*/
            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    /*--if information saved into user table navigate to Main Activity--*/
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "your account is created successfully.", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    } /*--else show error message--*/ else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }

    }

    /*--function to navigate from SetupActivity to MainActivity*/
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    /*--call back function for select image from gallery --*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*--check requestCode & resultCode & data then call CropImage Activity--*/
        if (requestCode == GALLERY_PICK_INTENT && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        /*--check requestCode & resultCode & data then call CropImage Activity--*/
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we are updating your profile...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();
                /*--creating file path for file to be stored in fireBase Storage--*/
                final StorageReference filePath = userProfileRef.child(currentUserId + ".jpg");
                /*--Completion listener for Upload image into Storage--*/
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Toast.makeText(SetupActivity.this, "profile image stored successfully to fireBase storage...", Toast.LENGTH_SHORT).show();
                                    final String downloadUrl = uri.toString();
                                    Map<String, Object> taskMap = new HashMap<>();
                                    taskMap.put("profileimage", downloadUrl);
                                    /*--update user table after image uploaded into storage--*/
                                    userRef.updateChildren(taskMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //  Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                        //  startActivity(selfIntent);
                                                        Toast.makeText(SetupActivity.this, "profile image stored to fireBase database successfully.", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }
        } /*--else show error message--*/ else {
            Toast.makeText(this, "Error Occurred: Image can't be cropped", Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }
    }
}
