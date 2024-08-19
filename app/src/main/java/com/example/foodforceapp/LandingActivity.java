package com.example.foodforceapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LandingActivity extends AppCompatActivity {

    private ImageView pot, grenade;
    private TextView text1, text2;
    private Button startButton;
    private LinearLayout iconContainer;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        mAuth = FirebaseAuth.getInstance();

        iconContainer = findViewById(R.id.iconContainer);
        pot = findViewById(R.id.pot);
        grenade = findViewById(R.id.grenade);
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        startButton = findViewById(R.id.startButton);

        Animation slideFromLeft = AnimationUtils.loadAnimation(this, R.anim.slide_from_left);
        Animation slideFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_from_right);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        iconContainer.post(() -> {
            pot.setVisibility(View.VISIBLE);
            grenade.setVisibility(View.VISIBLE);

            pot.startAnimation(slideFromLeft);
            grenade.startAnimation(slideFromRight);
        });

        text1.startAnimation(fadeIn);
        text2.startAnimation(fadeIn);
        startButton.startAnimation(fadeIn);

        startButton.setOnClickListener(v -> {
            checkUserAndProceed();
        });
    }

    private void checkUserAndProceed() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Intent intent;
        if (currentUser != null) {
            intent = new Intent(LandingActivity.this, MainActivity.class);
        } else {
            intent = new Intent(LandingActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(LandingActivity.this, MainActivity.class));
            finish();
        }
    }
}