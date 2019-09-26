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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.socialnetwork.R;
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


public class SettingActivity extends AppCompatActivity {
    private Toolbar mToolabr;

    private EditText userName, userProfName, userStatus, userCountry, userGender, userRelation, userDob;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfImage;

    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;
    private StorageReference userProfileRef;

    private String currentUserID;
    public static final int GALLERY_PICK = 1;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init();

        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileimage")) {
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(myProfileImage).placeholder(R.drawable.add_post_high).into(userProfImage);
                    }

                    if (dataSnapshot.hasChild("username")) {
                        String myUserName = dataSnapshot.child("username").getValue().toString();
                        userName.setText(myUserName);
                    }

                    if (dataSnapshot.hasChild("fullname")) {
                        String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                        userProfName.setText(myProfileName);
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

        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAccountInfo();
            }
        });

        /*--click listener for change user profile image--*/
        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*--opening gallery to select the image--*/
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

    }


    private void init() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userProfileRef = FirebaseStorage.getInstance().getReference().child("profile Images");
        loadingBar = new ProgressDialog(this);
        mToolabr = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolabr);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = findViewById(R.id.settings_username);
        userProfName = findViewById(R.id.settings_profile_full_name);
        userStatus = findViewById(R.id.settings_status);
        userCountry = findViewById(R.id.settings_country);
        userGender = findViewById(R.id.settings_gender);
        userRelation = findViewById(R.id.settings_relationship_status);
        userDob = findViewById(R.id.settings_dob);
        userProfImage = findViewById(R.id.settings_profile_image);
        updateAccountSettingsButton = findViewById(R.id.update_account_settings_button);

    }

    private void validateAccountInfo() {
        String username = userName.getText().toString();
        String profilename = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDob.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(profilename)) {
            Toast.makeText(this, "Please write your profile name...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Please write your status...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(dob)) {
            Toast.makeText(this, "Please write your date of birth...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please write your country name...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please write your gender...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(relation)) {
            Toast.makeText(this, "Please write your relationship status...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we are updating your profile...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            updateAccountInformation(username, profilename, status, dob, country, gender, relation);
        }
    }

    private void updateAccountInformation(String username, String profilename, String status, String dob, String country, String gender, String relation) {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("fullname", profilename);
        userMap.put("status", status);
        userMap.put("dob", dob);
        userMap.put("country", country);
        userMap.put("gender", gender);
        userMap.put("relationshipstatus", relation);
        settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    sendUserToMainActivity();
                    Toast.makeText(SettingActivity.this, "Account Settings Updated Successfully... ", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                } else {
                    Toast.makeText(SettingActivity.this, "Error occurred, while updating account setting info...", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }


    /*--call back function for select image from gallery --*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*--check requestCode & resultCode & data then call CropImage Activity--*/
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }/*--check requestCode & resultCode & data then call CropImage Activity--*/
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we are updating your profile...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();
                /*--creating file path for file to be stored in fireBase Storage--*/
                final StorageReference filePath = userProfileRef.child(currentUserID + ".jpg");
                /*--Completion listener for Upload image into Storage--*/
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Toast.makeText(SettingActivity.this, "profile image stored successfully to fireBase storage...", Toast.LENGTH_SHORT).show();
                                    final String downloadUrl = uri.toString();
                                    Map<String, Object> taskMap = new HashMap<>();
                                    taskMap.put("profileimage", downloadUrl);
                                    /*--update user table after image uploaded into storage--*/
                                    settingsUserRef.updateChildren(taskMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(SettingActivity.this, "profile image stored to fireBase database successfully.", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SettingActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
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
