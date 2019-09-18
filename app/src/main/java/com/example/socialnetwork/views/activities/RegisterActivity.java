package com.example.socialnetwork.views.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.socialnetwork.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText userEmail, userPassword, userConfirmPassword;
    private Button createAccountButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        init();
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sendUserToMainActivity();
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    private void init() {
        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_password);
        userConfirmPassword = findViewById(R.id.register_confirm_password);
        createAccountButton = findViewById(R.id.register_create_account);

        Typeface sfUiTextRegularFont = Typeface.createFromAsset(getAssets(), "SF-UI-Text-Regular.ttf");
        userEmail.setTypeface(sfUiTextRegularFont);
        userPassword.setTypeface(sfUiTextRegularFont);
        userConfirmPassword.setTypeface(sfUiTextRegularFont);

        Typeface avenirHeavy = Typeface.createFromAsset(getAssets(), "Avenir_Heavy.ttf");
        createAccountButton.setTypeface(avenirHeavy);

        loadingBar = new ProgressDialog(this);
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPassword = userConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please write your email...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please confirm your password...", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "your password do not match with your confirm password...", Toast.LENGTH_SHORT).show();

        } else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we are creating your new Account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        sendEmailVerificationMessage();
                        loadingBar.dismiss();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    /*-- function to send verification email --*/
    private void sendEmailVerificationMessage(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        /* if email is verified send user to login activity and Sign out */
                        Toast.makeText(RegisterActivity.this, "Registration Successful, we'va sent you a mail. Please check and verify your account...", Toast.LENGTH_SHORT).show();
                        sendUserToLoginActivity();
                        mAuth.signOut();
                    } else {
                        /* if error occurred then show error and Sign out */
                        String message = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error Occurred: " +message, Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                }
            });
        }
    }


    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
        finish();
    }
}
