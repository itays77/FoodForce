package com.example.foodforceapp.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.Models.User;
import com.example.foodforceapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.Locale;

public class MealDetailFragment extends Fragment {
    private static final String ARG_MEAL_ID = "meal_id";
    private static final int PICK_IMAGE_REQUEST = 1;

    private String mealId;
    private Meal meal;
    private TextView descriptionTextView, locationTextView, dateTextView, numberOfPeopleTextView, kosherTextView, statusTextView;
    private ImageView soldierPhotoImageView, mamaPhotoImageView;
    private Button approveButton, uploadPhotoButton, deleteSoldierPhotoButton, deleteMamaPhotoButton;
    private Uri imageUri;
    private boolean isMama;

    public static MealDetailFragment newInstance(String mealId) {
        MealDetailFragment fragment = new MealDetailFragment();
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
        View view = inflater.inflate(R.layout.fragment_meal_detail, container, false);
        view.setBackgroundResource(android.R.color.white);

        initializeViews(view);
        setupListeners();
        checkUserType();
        loadMealDetails();

        return view;
    }

    private void initializeViews(View view) {
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
        deleteSoldierPhotoButton = view.findViewById(R.id.deleteSoldierPhotoButton);
        deleteMamaPhotoButton = view.findViewById(R.id.deleteMamaPhotoButton);
    }

    private void setupListeners() {
        approveButton.setOnClickListener(v -> approveMeal());
        uploadPhotoButton.setOnClickListener(v -> openFileChooser());
        deleteSoldierPhotoButton.setOnClickListener(v -> deleteSoldierPhoto());
        deleteMamaPhotoButton.setOnClickListener(v -> deleteMamaPhoto());
    }

    private void checkUserType() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    isMama = (user != null && user.getType() == User.UserType.MAMA);
                    updateUIBasedOnUserType();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUIBasedOnUserType() {
        if (isMama) {
            approveButton.setVisibility(View.VISIBLE);
            uploadPhotoButton.setText("Upload Mama Photo");
            deleteSoldierPhotoButton.setVisibility(View.GONE);
        } else {
            approveButton.setVisibility(View.GONE);
            uploadPhotoButton.setText("Upload Soldier Photo");
            deleteMamaPhotoButton.setVisibility(View.GONE);
        }
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
            deleteSoldierPhotoButton.setVisibility(isMama ? View.GONE : View.VISIBLE);
        } else {
            soldierPhotoImageView.setVisibility(View.GONE);
            deleteSoldierPhotoButton.setVisibility(View.GONE);
        }

        if (meal.getMamaPhotoUrl() != null && !meal.getMamaPhotoUrl().isEmpty()) {
            mamaPhotoImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(meal.getMamaPhotoUrl()).into(mamaPhotoImageView);
            deleteMamaPhotoButton.setVisibility(isMama ? View.VISIBLE : View.GONE);
        } else {
            mamaPhotoImageView.setVisibility(View.GONE);
            deleteMamaPhotoButton.setVisibility(View.GONE);
        }

        updateUIBasedOnUserType();
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            String photoType = isMama ? "mama_photo" : "soldier_photo";
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("meals/" + mealId + "/" + photoType);
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
        String photoField = isMama ? "mamaPhotoUrl" : "soldierPhotoUrl";
        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(mealId);
        mealRef.child(photoField).setValue(photoUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Photo updated successfully", Toast.LENGTH_SHORT).show();
                    if (isMama) {
                        meal.setMamaPhotoUrl(photoUrl);
                    } else {
                        meal.setSoldierPhotoUrl(photoUrl);
                    }
                    populateViews();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update meal data", Toast.LENGTH_SHORT).show());
    }

    private void deleteSoldierPhoto() {
        deletePhoto(meal.getSoldierPhotoUrl(), "soldierPhotoUrl");
    }

    private void deleteMamaPhoto() {
        deletePhoto(meal.getMamaPhotoUrl(), "mamaPhotoUrl");
    }

    private void deletePhoto(String photoUrl, String photoField) {
        if (photoUrl != null) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
            photoRef.delete().addOnSuccessListener(aVoid -> {
                DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(mealId);
                mealRef.child(photoField).removeValue()
                        .addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(getContext(), "Photo deleted successfully", Toast.LENGTH_SHORT).show();
                            if (photoField.equals("soldierPhotoUrl")) {
                                meal.setSoldierPhotoUrl(null);
                            } else {
                                meal.setMamaPhotoUrl(null);
                            }
                            populateViews();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update meal data", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete photo", Toast.LENGTH_SHORT).show());
        }
    }

    private void approveMeal() {
        if (meal != null && isMama) {
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
