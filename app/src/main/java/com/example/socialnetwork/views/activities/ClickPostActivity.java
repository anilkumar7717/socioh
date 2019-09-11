package com.example.socialnetwork.views.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialnetwork.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ClickPostActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView clickPostImage;
    private TextView clickPostDescription;
    private Button deletePostButton, editPostButton;

    private String postKey, currentUserID, databaseUserID, description, image;

    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);
        init();
        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        if (b != null) {
            postKey = b.get("postkey").toString();
            System.out.println("----key----" + postKey);
        }

        deletePostButton.setVisibility(View.INVISIBLE);
        editPostButton.setVisibility(View.INVISIBLE);

        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);


        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("uid")) {
                        databaseUserID = dataSnapshot.child("uid").getValue().toString();
                    }
                    if (dataSnapshot.hasChild("description")) {
                        description = dataSnapshot.child("description").getValue().toString();
                        clickPostDescription.setText(description);
                    }
                    if (dataSnapshot.hasChild("postImage")) {
                        image = dataSnapshot.child("postImage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.add_post_high).into(clickPostImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                clickPostImage.setVisibility(View.VISIBLE);
                                mShimmerViewContainer.stopShimmerAnimation();
                                mShimmerViewContainer.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    }

                    if (currentUserID.equals(databaseUserID)) {
                        deletePostButton.setVisibility(View.VISIBLE);
                        editPostButton.setVisibility(View.VISIBLE);
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
        currentUserID = mAuth.getCurrentUser().getUid();
        clickPostImage = findViewById(R.id.click_post_image);
        clickPostDescription = findViewById(R.id.click_post_description);
        deletePostButton = findViewById(R.id.delete_post_button);
        editPostButton = findViewById(R.id.edit_post_button);
        deletePostButton.setOnClickListener(this);
        editPostButton.setOnClickListener(this);

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.delete_post_button:
                deleteCurrentPost();
                break;

            case R.id.edit_post_button:
                editCurrentPost(description);
                break;
        }
    }

    private void editCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ClickPostActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert));
        builder.setTitle("Edit Post:");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post updated successfully...", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
    }

    private void deleteCurrentPost() {
        clickPostRef.removeValue();
        sendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted.", Toast.LENGTH_SHORT).show();
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    private void facebookShare() {
        //String urlToShare = "http://onelink.to/hyndxg";
        String urlToShare = "";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, urlToShare);


        // See if official Facebook app is found
        boolean facebookAppFound = false;
        List<ResolveInfo> matches = this.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                intent.setPackage(info.activityInfo.packageName);
                facebookAppFound = true;
                break;
            }
        }

        // As fallback, launch sharer.php in a browser
        if (!facebookAppFound) {
            String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
        }

        startActivity(intent);
    }


    private void instaShare() {

        Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.instagram.android");
        if (intent != null) {
            try {
                Intent shareOnAppIntent = new Intent();
                shareOnAppIntent.setAction(Intent.ACTION_SEND);
                shareOnAppIntent.putExtra(Intent.EXTRA_TEXT, "");
                shareOnAppIntent.setType("text/plain");
                shareOnAppIntent.setPackage("com.instagram.android");
                startActivity(Intent.createChooser(shareOnAppIntent, "Share to"));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getResources().getString(R.string.message_app_not_install), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.message_app_not_install), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmerAnimation();
    }

    @Override
    protected void onPause() {
        mShimmerViewContainer.stopShimmerAnimation();
        super.onPause();
    }
}
