package com.example.foodforceapp.Data;

import com.example.foodforceapp.Models.User;
import com.example.foodforceapp.Models.Soldier;
import com.example.foodforceapp.Models.Mama;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UserManager {

    private static final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    public static Task<Void> saveUser(User user) {
        return usersRef.child(user.getId()).setValue(user);
    }

    public static Task<Void> saveSoldier(Soldier soldier) {
        return usersRef.child(soldier.getId()).setValue(soldier);
    }

    public static Task<Void> saveMama(Mama mama) {
        return usersRef.child(mama.getId()).setValue(mama);
    }

    public static Task<DataSnapshot> getUserById(String userId) {
        return usersRef.child(userId).get();
    }

    public static Task<Void> updateUserProfile(String userId, Map<String, Object> updates) {
        return usersRef.child(userId).updateChildren(updates);
    }
}
