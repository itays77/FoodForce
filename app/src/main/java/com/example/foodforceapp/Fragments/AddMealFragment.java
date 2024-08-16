package com.example.foodforceapp.Fragments;

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

import com.example.foodforceapp.MainActivity;
import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;

public class AddMealFragment extends Fragment {

    private EditText descriptionEditText, locationEditText, numberOfPeopleEditText;
    private DatePicker datePicker;
    private CheckBox kosherCheckBox;
    private Button submitButton, backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_meal, container, false);

        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        locationEditText = view.findViewById(R.id.locationEditText);
        numberOfPeopleEditText = view.findViewById(R.id.numberOfPeopleEditText);
        datePicker = view.findViewById(R.id.datePicker);
        kosherCheckBox = view.findViewById(R.id.kosherCheckBox);
        submitButton = view.findViewById(R.id.submitButton);
        backButton = view.findViewById(R.id.backButton);

        submitButton.setOnClickListener(v -> submitMealRequest());
        backButton.setOnClickListener(v -> goBack());

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

        FirebaseDatabase.getInstance().getReference()
                .child("meals")
                .child(mealId)
                .setValue(meal)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Meal added successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                        if (getFragmentManager() != null) {
                            getFragmentManager().popBackStack();
                        }
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).refreshUI();
                        }
                        getFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Failed to add meal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goBack() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
    }

    private void clearFields() {
        descriptionEditText.setText("");
        locationEditText.setText("");
        numberOfPeopleEditText.setText("");
        kosherCheckBox.setChecked(false);
        Calendar calendar = Calendar.getInstance();
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // When the fragment is destroyed, check if we should show the add meal request button again
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showAddMealRequestButtonIfSoldier();
        }
    }
}