package com.example.foodforceapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodforceapp.MainActivity;
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


public class MamaMealDetailFragment extends Fragment {
    private static final String ARG_MEAL_ID = "meal_id";

    private String mealId;
    private Meal meal;
    private TextView descriptionTextView, locationTextView, numberOfPeopleTextView, dateTextView, kosherTextView;
    private Button approveButton, backButton;

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
        View view = inflater.inflate(R.layout.fragment_meal_detail_mama, container, false);

        initializeViews(view);
        loadMealDetails();

        return view;
    }

    private void initializeViews(View view) {
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        locationTextView = view.findViewById(R.id.locationTextView);
        numberOfPeopleTextView = view.findViewById(R.id.numberOfPeopleTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        kosherTextView = view.findViewById(R.id.kosherTextView);

        approveButton = view.findViewById(R.id.approveButton);
        if (approveButton != null) {
            approveButton.setOnClickListener(v -> approveMeal());
        } else {
            Log.e("MamaMealDetailFragment", "approveButton not found in layout");
        }

        backButton = view.findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                }
            });
        } else {
            Log.e("MamaMealDetailFragment", "backButton not found in layout");
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
        numberOfPeopleTextView.setText(String.valueOf(meal.getNumberOfPeople()));
        dateTextView.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(meal.getDate())));
        kosherTextView.setText(meal.isKosher() ? "Kosher" : "Not Kosher");
    }

    private void approveMeal() {
        meal.setStatus("approved");
        meal.setMamaUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(mealId);
        mealRef.setValue(meal).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Meal approved successfully", Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Failed to approve meal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showAddMealRequestButtonIfSoldier();

        }
    }
}