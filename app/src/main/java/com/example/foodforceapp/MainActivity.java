package com.example.foodforceapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodforceapp.Adapters.MealRequestAdapter;
import com.example.foodforceapp.Fragments.AddMealFragment;
import com.example.foodforceapp.Fragments.MamaMealDetailFragment;
import com.example.foodforceapp.Fragments.MealDetailFragment;
import com.example.foodforceapp.Fragments.SoldierMealDetailFragment;
import com.example.foodforceapp.Models.Meal;
import com.example.foodforceapp.Models.Soldier;
import com.example.foodforceapp.Models.User;
import com.google.firebase.auth.FirebaseAuth;
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
    private Button addMealRequestButton;
    private MealRequestAdapter mealRequestAdapter;
    private List<Meal> allMealRequests = new ArrayList<>();
    private List<Meal> filteredMealRequests = new ArrayList<>();
    private boolean isSoldier = false;
    private String currentUserId;
    private Spinner filterSpinner;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInfoTextView = findViewById(R.id.userInfoTextView);
        mealRequestsRecyclerView = findViewById(R.id.mealRequestsRecyclerView);
        addMealRequestButton = findViewById(R.id.addMealRequestButton);
        filterSpinner = findViewById(R.id.filterSpinner);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        mealRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mealRequestAdapter = new MealRequestAdapter(filteredMealRequests, this);
        mealRequestsRecyclerView.setAdapter(mealRequestAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUserInfo();
        setupFilterSpinner();
        loadAllMealRequests();

        addMealRequestButton.setOnClickListener(v -> openAddMealFragment());
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterMealRequests(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
                        allMealRequests.add(meal);
                    }
                }
                filterMealRequests(filterSpinner.getSelectedItemPosition());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void openAddMealFragment() {
        AddMealFragment addMealFragment = new AddMealFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, addMealFragment)
                .addToBackStack(null)
                .commit();
        addMealRequestButton.setVisibility(View.GONE);
        mealRequestsRecyclerView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
    }


    private void loadUserInfo() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    String userInfo;
                    switch (user.getType()) {
                        case SOLDIER:
                            Soldier soldier = dataSnapshot.getValue(Soldier.class);
                            if (soldier != null) {
                                userInfo = soldier.getName() + " - " + soldier.getUnit();
                                isSoldier = true;
                            } else {
                                userInfo = "Error loading soldier data";
                                isSoldier = false;
                            }
                            break;
                        case MAMA:
                            userInfo = user.getName() + " - Mama";
                            isSoldier = false;
                            break;
                        case TEMP:
                            userInfo = user.getName() + " - Temporary User";
                            isSoldier = false;
                            // You might want to redirect to a user type selection screen here
                            break;
                        default:
                            userInfo = "Unknown user type";
                            isSoldier = false;
                    }
                    userInfoTextView.setText(userInfo);
                    addMealRequestButton.setVisibility(isSoldier ? View.VISIBLE : View.GONE);
                } else {
                    userInfoTextView.setText("User data not found");
                    addMealRequestButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                userInfoTextView.setText("Error loading user data");
                addMealRequestButton.setVisibility(View.GONE);
            }
        });
    }






    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            mealRequestsRecyclerView.setVisibility(View.VISIBLE);
            fragmentContainer.setVisibility(View.GONE);
            if (isSoldier) {
                addMealRequestButton.setVisibility(View.VISIBLE);
            }
        } else {
            super.onBackPressed();
        }
    }
    private void openSoldierMealDetailFragment(String mealId) {
        SoldierMealDetailFragment detailFragment = SoldierMealDetailFragment.newInstance(mealId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit();
        addMealRequestButton.setVisibility(View.GONE);
        mealRequestsRecyclerView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
    }

    private void openMamaMealDetailFragment(String mealId) {
        MamaMealDetailFragment detailFragment = MamaMealDetailFragment.newInstance(mealId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit();
        mealRequestsRecyclerView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
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




    public void showAddMealRequestButtonIfSoldier() {
        if (isSoldier) {
            addMealRequestButton.setVisibility(View.VISIBLE);
        }
    }


}