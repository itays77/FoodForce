package com.example.foodforceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodforceapp.Adapters.MealRequestAdapter;
import com.example.foodforceapp.Fragments.AddMealFragment;
import com.example.foodforceapp.Fragments.MamaMealDetailFragment;
import com.example.foodforceapp.Fragments.SoldierMealDetailFragment;
import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.Models.Soldier;
import com.example.foodforceapp.Models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MealRequestAdapter.OnMealClickListener {

    private TextView userInfoTextView;
    private RecyclerView mealRequestsRecyclerView;
    private ExtendedFloatingActionButton addMealRequestButton;
    private MealRequestAdapter mealRequestAdapter;
    private List<Meal> allMealRequests = new ArrayList<>();
    private List<Meal> filteredMealRequests = new ArrayList<>();
    private boolean isSoldier = false;
    private String currentUserId;
    private AutoCompleteTextView filterSpinner;
    private int selectedFilterPosition = 0;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);

        mealRequestsRecyclerView = findViewById(R.id.mealRequestsRecyclerView);
        addMealRequestButton = findViewById(R.id.addMealRequestButton);
        filterSpinner = findViewById(R.id.filterSpinner);

        mealRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mealRequestAdapter = new MealRequestAdapter(filteredMealRequests, this);
        mealRequestsRecyclerView.setAdapter(mealRequestAdapter);

        userInfoTextView = findViewById(R.id.userInfoTextView);
        mealRequestsRecyclerView = findViewById(R.id.mealRequestsRecyclerView);
        addMealRequestButton = findViewById(R.id.addMealRequestButton);
        filterSpinner = findViewById(R.id.filterSpinner);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUserInfo();
        setupFilterSpinner();
        loadAllMealRequests();

        addMealRequestButton.setOnClickListener(v -> openAddMealFragment());
    }


    private void setupFilterSpinner() {
        String[] filterOptions = new String[]{"All Requests", "Open Requests"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, filterOptions);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemClickListener((parent, view, position, id) -> {
            selectedFilterPosition = position; // Add this line
            filterMealRequests(position);
        });
    }

    private void filterMealRequests(int filterPosition) {
        filteredMealRequests.clear();
        for (Meal meal : allMealRequests) {
            if (filterPosition == 0) { // All requests
                if (isSoldier) {
                    if (meal.getSoldierUserId().equals(currentUserId)) {
                        filteredMealRequests.add(meal);
                    }
                } else {
                    filteredMealRequests.add(meal);
                }
            } else if (filterPosition == 1) { // Open requests
                if (isSoldier) {
                    if (meal.getSoldierUserId().equals(currentUserId) && meal.getStatus().equals("open")) {
                        filteredMealRequests.add(meal);
                    }
                } else {
                    if (meal.getStatus().equals("open")) {
                        filteredMealRequests.add(meal);
                    }
                }
            }
        }
        mealRequestAdapter.notifyDataSetChanged();
    }

    private void loadAllMealRequests() {
        DatabaseReference mealsRef = FirebaseDatabase.getInstance().getReference("meals");
        mealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allMealRequests.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Meal meal = snapshot.getValue(Meal.class);
                    if (meal != null) {
                        if (!isSoldier || meal.getSoldierUserId().equals(currentUserId)) {
                            allMealRequests.add(meal);
                        }
                    }
                }
                filterMealRequests(selectedFilterPosition);
                mealRequestAdapter.notifyDataSetChanged();
                updateUIVisibility();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void updateUIVisibility() {
        if (filteredMealRequests.isEmpty()) {
            mealRequestsRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.noMealsTextView).setVisibility(View.VISIBLE);
        } else {
            mealRequestsRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.noMealsTextView).setVisibility(View.GONE);
        }
    }



    public void refreshUI() {
        loadAllMealRequests();
        updateAddButtonVisibility();
    }

    private void updateAddButtonVisibility() {
        if (addMealRequestButton != null) {
            addMealRequestButton.setVisibility(isSoldier ? View.VISIBLE : View.GONE);
        }
    }

    private void openAddMealFragment() {
        AddMealFragment addMealFragment = new AddMealFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, addMealFragment)
                .addToBackStack(null)
                .commit();
        fragmentContainer.setVisibility(View.VISIBLE);
        mealRequestsRecyclerView.setVisibility(View.GONE);
        addMealRequestButton.setVisibility(View.GONE);
    }


    private void loadUserInfo() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    StringBuilder userInfo = new StringBuilder();
                    switch (user.getType()) {
                        case SOLDIER:
                            Soldier soldier = dataSnapshot.getValue(Soldier.class);
                            if (soldier != null) {
                                userInfo.append(soldier.getName()).append(" - ").append(soldier.getUnit()).append("\n")
                                        .append(soldier.getSType().toString());
                                isSoldier = true;
                            } else {
                                userInfo.append("Error loading soldier data");
                                isSoldier = false;
                            }
                            break;
                        case MAMA:
                            userInfo.append(user.getName()).append(" - Mama");
                            isSoldier = false;
                            break;
                        case TEMP:
                            userInfo.append(user.getName()).append(" - Temporary User");
                            isSoldier = false;
                            break;
                        default:
                            userInfo.append("Unknown user type");
                            isSoldier = false;
                    }
                    userInfoTextView.setText(userInfo.toString());
                    userInfoTextView.setGravity(Gravity.START);
                    addMealRequestButton.setVisibility(isSoldier ? View.VISIBLE : View.GONE);
                } else {
                    userInfoTextView.setText("User data not found");
                    addMealRequestButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                userInfoTextView.setText("Error loading user data");
                addMealRequestButton.setVisibility(View.GONE);
            }
        });

    }




    private void openSoldierMealDetailFragment(String mealId) {
        SoldierMealDetailFragment detailFragment = SoldierMealDetailFragment.newInstance(mealId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit();
        showFragmentContainer();
    }


    private void openMamaMealDetailFragment(String mealId) {
        MamaMealDetailFragment detailFragment = MamaMealDetailFragment.newInstance(mealId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit();
        showFragmentContainer();
    }

    private void showFragmentContainer() {
        fragmentContainer.setVisibility(View.VISIBLE);
        mealRequestsRecyclerView.setVisibility(View.GONE);
        filterSpinner.setVisibility(View.GONE);
        addMealRequestButton.setVisibility(View.GONE);
        userInfoTextView.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            hideFragmentContainer();
        } else {
            super.onBackPressed();
        }
    }


    private void hideFragmentContainer() {
        fragmentContainer.setVisibility(View.GONE);
        mealRequestsRecyclerView.setVisibility(View.VISIBLE);
        filterSpinner.setVisibility(View.VISIBLE);
        userInfoTextView.setVisibility(View.VISIBLE);
        if (isSoldier) {
            addMealRequestButton.setVisibility(View.VISIBLE);
        }
        refreshUI();
    }

    private void showMainContent() {
        fragmentContainer.setVisibility(View.GONE);
        mealRequestsRecyclerView.setVisibility(View.VISIBLE);
        filterSpinner.setVisibility(View.VISIBLE);
        refreshUI();
    }

    private void hideMainContent() {
        fragmentContainer.setVisibility(View.VISIBLE);
        mealRequestsRecyclerView.setVisibility(View.GONE);
        filterSpinner.setVisibility(View.GONE);
    }
    @Override
    public void onMealClick(String mealId) {
        if (isSoldier) {
            Log.d("FragmentOpened", "Opening SoldierMealDetailFragment");
            openSoldierMealDetailFragment(mealId);
        } else {
            Log.d("FragmentOpened", "Opening MamaMealDetailFragment");
            openMamaMealDetailFragment(mealId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }




    public void showAddMealRequestButtonIfSoldier() {
        if (isSoldier) {
            addMealRequestButton.setVisibility(View.VISIBLE);
        }
    }


}