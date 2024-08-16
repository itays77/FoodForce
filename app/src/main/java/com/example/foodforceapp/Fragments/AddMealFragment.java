package com.example.foodforceapp.Fragments;

import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class AddMealFragment extends Fragment {

    private EditText descriptionEditText, locationEditText, numberOfPeopleEditText;
    private DatePicker datePicker;
    private CheckBox kosherCheckBox;
    private Button submitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_meal, container, false);
        view.setBackgroundResource(android.R.color.white);

        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        locationEditText = view.findViewById(R.id.locationEditText);
        numberOfPeopleEditText = view.findViewById(R.id.numberOfPeopleEditText);
        datePicker = view.findViewById(R.id.datePicker);
        kosherCheckBox = view.findViewById(R.id.kosherCheckBox);
        submitButton = view.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> submitMealRequest());

        return view;
    }

    private void submitMealRequest() {
        String description = descriptionEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String numberOfPeopleStr = numberOfPeopleEditText.getText().toString().trim();
        boolean kosher = kosherCheckBox.isChecked();

        if (description.isEmpty() || location.isEmpty() || numberOfPeopleStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int numberOfPeople;
        try {
            numberOfPeople = Integer.parseInt(numberOfPeopleStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number of people", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        long date = calendar.getTimeInMillis();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String mealId = FirebaseDatabase.getInstance().getReference().child("meals").push().getKey();
        if (mealId == null) {
            Toast.makeText(getContext(), "Failed to generate meal ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Meal meal = new Meal(mealId, currentUser.getUid(), date, description, kosher, location, numberOfPeople);
        saveMealToDatabase(meal);
    }

    private void saveMealToDatabase(Meal meal) {
        FirebaseDatabase.getInstance().getReference()
                .child("meals")
                .child(meal.getId())
                .setValue(meal)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Meal added successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                        if (getFragmentManager() != null) {
                            getFragmentManager().popBackStack();
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to add meal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        descriptionEditText.setText("");
        locationEditText.setText("");
        numberOfPeopleEditText.setText("");
        kosherCheckBox.setChecked(false);
    }
}