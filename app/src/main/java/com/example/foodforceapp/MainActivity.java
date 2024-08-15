package com.example.foodforceapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private List<Meal> openMealRequests = new ArrayList<>();
    private boolean isSoldier = false;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInfoTextView = findViewById(R.id.userInfoTextView);
        mealRequestsRecyclerView = findViewById(R.id.mealRequestsRecyclerView);
        addMealRequestButton = findViewById(R.id.addMealRequestButton);

        mealRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mealRequestAdapter = new MealRequestAdapter(openMealRequests, this);
        mealRequestsRecyclerView.setAdapter(mealRequestAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUserInfo();
        loadOpenMealRequests();

        addMealRequestButton.setOnClickListener(v -> openAddMealFragment());
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

    private void loadOpenMealRequests() {
        DatabaseReference mealsRef = FirebaseDatabase.getInstance().getReference("meals");
        mealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                openMealRequests.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Meal meal = snapshot.getValue(Meal.class);
                    if (meal != null && meal.getDate() > System.currentTimeMillis()) {
                        if (!isSoldier && "open".equals(meal.getStatus())) {
                            openMealRequests.add(meal);
                        } else if (isSoldier && currentUserId.equals(meal.getSoldierUserId())) {
                            openMealRequests.add(meal);
                        }
                    }
                }
                mealRequestAdapter.notifyDataSetChanged();
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0 && isSoldier) {
            addMealRequestButton.setVisibility(View.VISIBLE);
        }
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

    private void openSoldierMealDetailFragment(String mealId) {
        SoldierMealDetailFragment detailFragment = SoldierMealDetailFragment.newInstance(mealId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit();
        addMealRequestButton.setVisibility(View.GONE);
    }

    private void openMamaMealDetailFragment(String mealId) {
        MamaMealDetailFragment detailFragment = MamaMealDetailFragment.newInstance(mealId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit();
    }
    public void showAddMealRequestButtonIfSoldier() {
        if (isSoldier) {
            addMealRequestButton.setVisibility(View.VISIBLE);
        }
    }


}