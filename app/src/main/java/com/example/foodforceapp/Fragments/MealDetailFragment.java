package com.example.foodforceapp.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.Models.User;
import com.example.foodforceapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MealDetailFragment extends Fragment {
    private Meal meal;
    private TextView descriptionTextView, locationTextView, dateTextView, numberOfPeopleTextView, kosherTextView, statusTextView;
    private Button approveButton, backButton;
    private boolean isMama;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_detail_mama, container, false);

        // Initialize views
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        locationTextView = view.findViewById(R.id.locationTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        numberOfPeopleTextView = view.findViewById(R.id.numberOfPeopleTextView);
        kosherTextView = view.findViewById(R.id.kosherTextView);
        statusTextView = view.findViewById(R.id.statusTextView);
        approveButton = view.findViewById(R.id.approveButton);
        backButton = view.findViewById(R.id.backButton);

        // Check if current user is Mama
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                isMama = (user != null && user.getType() == User.UserType.MAMA);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Load meal details
        String mealId = getArguments().getString("mealId");
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
                // Handle error
            }
        });

        approveButton.setOnClickListener(v -> approveMeal());
        backButton.setOnClickListener(v -> getFragmentManager().popBackStack());

        return view;
    }

    private void populateViews() {
        descriptionTextView.setText(meal.getDescription());
        locationTextView.setText(meal.getLocation());
        dateTextView.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(meal.getDate())));
        numberOfPeopleTextView.setText(String.valueOf(meal.getNumberOfPeople()));
        kosherTextView.setText(meal.isKosher() ? "Kosher" : "Not Kosher");
        statusTextView.setText(meal.getStatus());
    }

    private void updateUI() {
        if (isMama && "open".equals(meal.getStatus())) {
            approveButton.setVisibility(View.VISIBLE);
        } else {
            approveButton.setVisibility(View.GONE);
        }
    }

    private void approveMeal() {
        meal.setStatus("approved");
        meal.setMamaUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(meal.getId());
        mealRef.setValue(meal).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Meal approved successfully", Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Failed to approve meal", Toast.LENGTH_SHORT).show();
            }
        });
    }
}