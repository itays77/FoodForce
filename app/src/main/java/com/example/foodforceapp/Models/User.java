package com.example.foodforceapp.Models;


public class User {
    private String id;
    private String name;
    private String email;
    private UserType type;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {}

    public User(String id, String name, String email, UserType type) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.type = type;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }

    public enum UserType {
        SOLDIER, MAMA, TEMP
    }
}