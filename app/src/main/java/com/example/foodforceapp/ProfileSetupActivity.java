package com.example.foodforceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodforceapp.Data.UserManager;
import com.example.foodforceapp.Models.User;
import com.example.foodforceapp.Models.Soldier;
import com.example.foodforceapp.Models.Mama;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.gms.tasks.Task;
import java.util.ArrayList;
import java.util.List;

public class ProfileSetupActivity extends AppCompatActivity {
    private EditText nameEditText, locationEditText;
    private RadioGroup specialtiesRadioGroup, unitRadioGroup, soldierTypeRadioGroup;
    private MaterialCardView specialtiesCard, unitCard, soldierTypeCard;
    private TextView emailTextView;
    private MaterialButton saveButton;
    private String userId;
    private User.UserType userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        userType = (User.UserType) getIntent().getSerializableExtra("USER_TYPE");
        userId = getIntent().getStringExtra("USER_ID");

        initializeViews();
        setupTypeSpecificUI();
        loadUserData();

        saveButton.setOnClickListener(v -> saveUserProfile());
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailTextView = findViewById(R.id.emailTextView);
        locationEditText = findViewById(R.id.locationEditText);
        specialtiesCard = findViewById(R.id.specialtiesCard);
        specialtiesRadioGroup = findViewById(R.id.specialtiesRadioGroup);
        unitCard = findViewById(R.id.unitCard);
        unitRadioGroup = findViewById(R.id.unitRadioGroup);
        soldierTypeCard = findViewById(R.id.soldierTypeCard);
        soldierTypeRadioGroup = findViewById(R.id.soldierTypeRadioGroup);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupTypeSpecificUI() {
        if (userType == User.UserType.MAMA) {
            specialtiesCard.setVisibility(View.VISIBLE);
            unitCard.setVisibility(View.GONE);
            soldierTypeCard.setVisibility(View.GONE);
        } else if (userType == User.UserType.SOLDIER) {
            specialtiesCard.setVisibility(View.GONE);
            unitCard.setVisibility(View.VISIBLE);
            soldierTypeCard.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserData() {
        // ... (existing code to load user data)
    }

    private void saveUserProfile() {
        String name = nameEditText.getText().toString();
        String email = emailTextView.getText().toString();
        String location = locationEditText.getText().toString();

        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userType == User.UserType.MAMA) {
            Mama mama = new Mama();
            mama.setId(userId);
            mama.setName(name);
            mama.setEmail(email);
            mama.setLocation(location);
            mama.setType(User.UserType.MAMA);

            int selectedSpecialtyId = specialtiesRadioGroup.getCheckedRadioButtonId();
            if (selectedSpecialtyId == -1) {
                Toast.makeText(this, "Please select a cooking specialty", Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> specialties = new ArrayList<>();
            if (selectedSpecialtyId == R.id.radioHomeMeal) {
                specialties.add("Home Meal");
            } else if (selectedSpecialtyId == R.id.radioItalian) {
                specialties.add("Italian");
            } else if (selectedSpecialtyId == R.id.radioOther) {
                specialties.add("Other");
            }
            mama.setSpecialties(specialties);

            UserManager.saveMama(mama).addOnCompleteListener(this::handleSaveResult);
        } else if (userType == User.UserType.SOLDIER) {
            Soldier soldier = new Soldier();
            soldier.setId(userId);
            soldier.setName(name);
            soldier.setEmail(email);
            soldier.setType(User.UserType.SOLDIER);

            int selectedUnitId = unitRadioGroup.getCheckedRadioButtonId();
            if (selectedUnitId == -1) {
                Toast.makeText(this, "Please select a unit", Toast.LENGTH_SHORT).show();
                return;
            }
            String unit = "Other";
            if (selectedUnitId == R.id.radioGolani) {
                unit = "Golani";
            } else if (selectedUnitId == R.id.radioGivati) {
                unit = "Givati";
            } else if (selectedUnitId == R.id.radioTanks) {
                unit = "Tanks";
            } else if (selectedUnitId == R.id.radioAirForce) {
                unit = "Air Force";
            } else if (selectedUnitId == R.id.radioNavy) {
                unit = "Navy";
            } else if (selectedUnitId == R.id.radioIntelligence) {
                unit = "Intelligence";
            } else if (selectedUnitId == R.id.radioOtherUnit) {
                unit = "Other";
            }
            soldier.setUnit(unit);

            int selectedTypeId = soldierTypeRadioGroup.getCheckedRadioButtonId();
            if (selectedTypeId == -1) {
                Toast.makeText(this, "Please select a soldier type", Toast.LENGTH_SHORT).show();
                return;
            }
            Soldier.SoldierType soldierType = selectedTypeId == R.id.radioReserved ?
                    Soldier.SoldierType.RESERVED : Soldier.SoldierType.MANDATORY;
            soldier.setSType(soldierType);

            UserManager.saveSoldier(soldier).addOnCompleteListener(this::handleSaveResult);
        }
    }

    private void handleSaveResult(Task<Void> task) {
        if (task.isSuccessful()) {
            Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
        }

    }
}
