package com.example.foodforceapp.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class MamaMealDetailFragment extends Fragment {
    private static final String ARG_MEAL_ID = "meal_id";
    private static final int PICK_IMAGE_REQUEST = 1;

    private String mealId;
    private Meal meal;
    private TextView descriptionTextView, locationTextView, dateTextView, numberOfPeopleTextView, kosherTextView, statusTextView;
    private ImageView soldierPhotoImageView, mamaPhotoImageView;
    private Button approveButton, uploadPhotoButton, deleteMamaPhotoButton;
    private Uri imageUri;

    public static MamaMealDetailFragment newInstance(String mealId) {
        MamaMealDetailFragment fragment = new MamaMealDetailFragment();
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
        View view = inflater.inflate(R.layout.fragment_mama_meal_detail, container, false);

        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        locationTextView = view.findViewById(R.id.locationTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        numberOfPeopleTextView = view.findViewById(R.id.numberOfPeopleTextView);
        kosherTextView = view.findViewById(R.id.kosherTextView);
        statusTextView = view.findViewById(R.id.statusTextView);
        soldierPhotoImageView = view.findViewById(R.id.soldierPhotoImageView);
        mamaPhotoImageView = view.findViewById(R.id.mamaPhotoImageView);
        approveButton = view.findViewById(R.id.approveButton);
        uploadPhotoButton = view.findViewById(R.id.uploadPhotoButton);
        deleteMamaPhotoButton = view.findViewById(R.id.deleteMamaPhotoButton);

        approveButton.setOnClickListener(v -> approveMeal());
        uploadPhotoButton.setOnClickListener(v -> openFileChooser());
        deleteMamaPhotoButton.setOnClickListener(v -> deleteMamaPhoto());

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
            soldierPhotoImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(meal.getSoldierPhotoUrl()).into(soldierPhotoImageView);
        } else {
            soldierPhotoImageView.setVisibility(View.GONE);
        }

        if (meal.getMamaPhotoUrl() != null && !meal.getMamaPhotoUrl().isEmpty()) {
            displayMamaPhoto(meal.getMamaPhotoUrl());
        } else {
            mamaPhotoImageView.setVisibility(View.GONE);
            deleteMamaPhotoButton.setVisibility(View.GONE);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
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
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("meals/" + mealId + "/mama_photo");
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
        meal.setMamaPhotoUrl(photoUrl);
        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(mealId);
        mealRef.child("mamaPhotoUrl").setValue(photoUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Photo updated successfully", Toast.LENGTH_SHORT).show();
                    displayMamaPhoto(photoUrl);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update meal data", Toast.LENGTH_SHORT).show());
    }

    private void displayMamaPhoto(String photoUrl) {
        mamaPhotoImageView.setVisibility(View.VISIBLE);
        Glide.with(this).load(photoUrl).into(mamaPhotoImageView);
        deleteMamaPhotoButton.setVisibility(View.VISIBLE);
    }

    private void deleteMamaPhoto() {
        if (meal != null && meal.getMamaPhotoUrl() != null) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(meal.getMamaPhotoUrl());
            photoRef.delete().addOnSuccessListener(aVoid -> {
                meal.setMamaPhotoUrl(null);
                DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(mealId);
                mealRef.child("mamaPhotoUrl").removeValue()
                        .addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(getContext(), "Photo deleted successfully", Toast.LENGTH_SHORT).show();
                            mamaPhotoImageView.setImageResource(android.R.color.transparent);
                            mamaPhotoImageView.setVisibility(View.GONE);
                            deleteMamaPhotoButton.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update meal data", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete photo", Toast.LENGTH_SHORT).show());
        }
    }

    private void approveMeal() {
        if (meal != null) {
            meal.setStatus("approved");
            meal.setMamaUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());

            DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(mealId);
            mealRef.setValue(meal).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Meal approved successfully", Toast.LENGTH_SHORT).show();
                    if (getFragmentManager() != null) {
                        getFragmentManager().popBackStack();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to approve meal", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}