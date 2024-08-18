package com.example.foodforceapp.Fragments;

import static android.app.Activity.RESULT_OK;
import static androidx.core.app.ActivityCompat.startActivityForResult;
import static java.security.AccessController.getContext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.foodforceapp.MainActivity;
import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SoldierMealDetailFragment extends Fragment {
    private static final String ARG_MEAL_ID = "meal_id";
    private static final int PICK_IMAGE_REQUEST = 1;

    private String mealId;
    private Meal meal;
    private TextView descriptionTextView, locationTextView, dateTextView, numberOfPeopleTextView, kosherTextView, statusTextView;
    private ImageView mealImageView;
    private Button uploadPhotoButton;
    private Button deleteSoldierPhotoButton;
    private Uri imageUri;

    public static SoldierMealDetailFragment newInstance(String mealId) {
        SoldierMealDetailFragment fragment = new SoldierMealDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEAL_ID, mealId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mealId = getArguments().getString(ARG_MEAL_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_soldier_meal_detail, container, false);

        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        locationTextView = view.findViewById(R.id.locationTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        numberOfPeopleTextView = view.findViewById(R.id.numberOfPeopleTextView);
        kosherTextView = view.findViewById(R.id.kosherTextView);
        statusTextView = view.findViewById(R.id.statusTextView);
        mealImageView = view.findViewById(R.id.mealImageView);
        uploadPhotoButton = view.findViewById(R.id.uploadPhotoButton);
        deleteSoldierPhotoButton = view.findViewById(R.id.deleteSoldierPhotoButton);

        uploadPhotoButton.setOnClickListener(v -> openFileChooser());
        deleteSoldierPhotoButton.setOnClickListener(v -> deleteSoldierPhoto());

        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> goBack());

        loadMealDetails();

        return view;
    }


    private void loadMealDetails() {
        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(mealId);
        mealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                meal = dataSnapshot.getValue(Meal.class);
                if (meal != null) {
                    populateViews();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load meal details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateViews() {
        descriptionTextView.setText(meal.getDescription());
        locationTextView.setText(meal.getLocation());
        dateTextView.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(meal.getDate())));
        numberOfPeopleTextView.setText(String.valueOf(meal.getNumberOfPeople()));
        kosherTextView.setText(meal.isKosher() ? "Kosher" : "Not Kosher");
        statusTextView.setText(meal.getStatus());

        if (meal.getSoldierPhotoUrl() != null && !meal.getSoldierPhotoUrl().isEmpty()) {
            mealImageView.setVisibility(View.VISIBLE);
            deleteSoldierPhotoButton.setVisibility(View.VISIBLE);
            Glide.with(this).load(meal.getSoldierPhotoUrl()).into(mealImageView);
        } else {
            mealImageView.setVisibility(View.GONE);
            deleteSoldierPhotoButton.setVisibility(View.GONE);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void deleteSoldierPhoto() {
        if (meal != null && meal.getSoldierPhotoUrl() != null) {
            // Delete from Firebase Storage
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(meal.getSoldierPhotoUrl());
            photoRef.delete().addOnSuccessListener(aVoid -> {
                // Update Firebase Realtime Database
                meal.setSoldierPhotoUrl(null);
                FirebaseDatabase.getInstance().getReference("meals").child(meal.getId())
                        .child("soldierPhotoUrl").removeValue()
                        .addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(getContext(), "Photo deleted successfully", Toast.LENGTH_SHORT).show();
                            mealImageView.setImageResource(android.R.color.transparent);
                            deleteSoldierPhotoButton.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update meal data", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete photo", Toast.LENGTH_SHORT).show());
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("meals/" + mealId + "/soldier_photo");
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String photoUrl = uri.toString();
                            updateMealWithPhoto(photoUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateMealWithPhoto(String photoUrl) {
        meal.setSoldierPhotoUrl(photoUrl);
        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(mealId);
        mealRef.setValue(meal).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Photo updated successfully", Toast.LENGTH_SHORT).show();
                mealImageView.setVisibility(View.VISIBLE);
                Glide.with(this).load(photoUrl).into(mealImageView);
            } else {
                Toast.makeText(getContext(), "Failed to update photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goBack() {
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }
}