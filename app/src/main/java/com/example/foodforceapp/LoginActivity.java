package com.example.foodforceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodforceapp.Models.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if user is signed in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is signed in, navigate to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // No user is signed in, show sign-in options
            createSignInIntent();
        }
    }

    private void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build();
        signInLauncher.launch(signInIntent);
    }

    public void createSignInIntent() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.foodforcelogo)  // Set your logo here
                .setIsSmartLockEnabled(false)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Log.d("LoginActivity", "Sign-in successful for user: " + user.getUid());
                checkUserTypeAndProceed(user.getUid());
            } else {
                Log.e("LoginActivity", "Error: User is null after successful sign-in");
                Toast.makeText(this, "Sign-in error: User is null", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Sign in failed
            Log.e("LoginActivity", "Sign-in failed");
            if (response == null) {
                Log.d("LoginActivity", "Sign-in cancelled by user");
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("LoginActivity", "Sign-in error: " + response.getError());
                Toast.makeText(this, "Sign in failed: " + response.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkUserTypeAndProceed(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User.UserType userType = dataSnapshot.child("type").getValue(User.UserType.class);
                    if (userType != null && userType != User.UserType.TEMP) {
                        Log.d("LoginActivity", "Existing user with type: " + userType + ". Navigating to MainActivity.");
                        transactToMainActivity();
                    } else {
                        Log.d("LoginActivity", "Existing user with TEMP type. Navigating to UserTypeSelectionActivity.");
                        transactToUserTypeSelectionActivity();
                    }
                } else {
                    // New user, create a new User object with TEMP type
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    User newUser = new User(userId, firebaseUser.getDisplayName(), firebaseUser.getEmail(), User.UserType.TEMP);
                    userRef.setValue(newUser).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("LoginActivity", "New user created with TEMP type. Navigating to UserTypeSelectionActivity.");
                            transactToUserTypeSelectionActivity();
                        } else {
                            Log.e("LoginActivity", "Failed to create new user", task.getException());
                            Toast.makeText(LoginActivity.this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LoginActivity", "Database error: " + databaseError.getMessage());
                Toast.makeText(LoginActivity.this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void transactToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void transactToUserTypeSelectionActivity() {
        Intent intent = new Intent(this, UserTypeSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}