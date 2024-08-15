package com.example.foodforceapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.foodforceapp.MainActivity;
import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;


public class SoldierMealDetailFragment extends Fragment {
    private static final String ARG_MEAL_ID = "meal_id";

    private String mealId;
    private Meal meal;
    private EditText descriptionEditText, locationEditText, numberOfPeopleEditText;
    private DatePicker datePicker;
    private CheckBox kosherCheckBox;
    private Button editButton, backButton;

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
        initializeViews(view);
        loadMealDetails();
        return view;
    }

    private void initializeViews(View view) {
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        locationEditText = view.findViewById(R.id.locationEditText);
        numberOfPeopleEditText = view.findViewById(R.id.numberOfPeopleEditText);
        datePicker = view.findViewById(R.id.datePicker);
        kosherCheckBox = view.findViewById(R.id.kosherCheckBox);
        editButton = view.findViewById(R.id.editButton);
        backButton = view.findViewById(R.id.backButton);

        editButton.setOnClickListener(v -> editMeal());
        backButton.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });

        // Initially disable editing
        setEditingEnabled(false);
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
        descriptionEditText.setText(meal.getDescription());
        locationEditText.setText(meal.getLocation());
        numberOfPeopleEditText.setText(String.valueOf(meal.getNumberOfPeople()));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(meal.getDate());
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        kosherCheckBox.setChecked(meal.isKosher());
    }

    private void editMeal() {
        if (editButton.getText().equals("Edit")) {
            setEditingEnabled(true);
            editButton.setText("Save");
        } else {
            saveMealChanges();
        }
    }

    private void setEditingEnabled(boolean enabled) {
        descriptionEditText.setEnabled(enabled);
        locationEditText.setEnabled(enabled);
        numberOfPeopleEditText.setEnabled(enabled);
        datePicker.setEnabled(enabled);
        kosherCheckBox.setEnabled(enabled);
    }

    private void saveMealChanges() {
        meal.setDescription(descriptionEditText.getText().toString());
        meal.setLocation(locationEditText.getText().toString());
        meal.setNumberOfPeople(Integer.parseInt(numberOfPeopleEditText.getText().toString()));

        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        meal.setDate(calendar.getTimeInMillis());

        meal.setKosher(kosherCheckBox.isChecked());

        DatabaseReference mealRef = FirebaseDatabase.getInstance().getReference("meals").child(mealId);
        mealRef.setValue(meal).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Meal updated successfully", Toast.LENGTH_SHORT).show();
                setEditingEnabled(false);
                editButton.setText("Edit");
            } else {
                Toast.makeText(getContext(), "Failed to update meal", Toast.LENGTH_SHORT).show();
            }
        });
    }


}