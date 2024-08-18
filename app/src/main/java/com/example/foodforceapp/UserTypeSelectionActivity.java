package com.example.foodforceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodforceapp.Data.UserManager;
import com.example.foodforceapp.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserTypeSelectionActivity extends AppCompatActivity {
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type_selection);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Button soldierButton = findViewById(R.id.soldierButton);
        Button mamaButton = findViewById(R.id.mamaButton);

        soldierButton.setOnClickListener(v -> selectUserType(User.UserType.SOLDIER));
        mamaButton.setOnClickListener(v -> selectUserType(User.UserType.MAMA));
    }

    private void showConfirmationDialog(User.UserType type) {
        String typeString = (type == User.UserType.SOLDIER) ? "Soldier" : "Mama";
        new AlertDialog.Builder(this)
                .setTitle("Confirm Selection")
                .setMessage("Are you sure you want to register as a " + typeString + "?")
                .setPositiveButton("Yes", (dialog, which) -> selectUserType(type))
                .setNegativeButton("No", null)
                .show();
    }

    private void selectUserType(User.UserType type) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("type").setValue(type)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(this, ProfileSetupActivity.class);
                        intent.putExtra("USER_TYPE", type);
                        intent.putExtra("USER_ID", userId);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update user type", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void navigateToProfileSetup(User.UserType type) {
        Intent intent = new Intent(this, ProfileSetupActivity.class);
        intent.putExtra("USER_TYPE", type);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}