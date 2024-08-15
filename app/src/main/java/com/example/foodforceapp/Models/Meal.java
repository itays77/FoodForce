package com.example.foodforceapp.Models;

public class Meal {
    private String id;
    private String soldierUserId;
    private String mamaUserId;
    private long date;
    private String description;
    private boolean kosher;
    private String location;
    private int numberOfPeople;
    private String status;

    // Default constructor required for calls to DataSnapshot.getValue(Meal.class)
    public Meal() {}

    public Meal(String id, String soldierUserId, long date, String description, boolean kosher, String location, int numberOfPeople) {
        this.id = id;
        this.soldierUserId = soldierUserId;
        this.date = date;
        this.description = description;
        this.kosher = kosher;
        this.location = location;
        this.numberOfPeople = numberOfPeople;
        this.status = "open";  // Initially set to open
        this.mamaUserId = "";  // Initially empty
    }

    // Getters
    public String getId() { return id; }
    public String getSoldierUserId() { return soldierUserId; }
    public String getMamaUserId() { return mamaUserId; }
    public long getDate() { return date; }
    public String getDescription() { return description; }
    public boolean isKosher() { return kosher; }
    public String getLocation() { return location; }
    public int getNumberOfPeople() { return numberOfPeople; }
    public String getStatus() { return status; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setSoldierUserId(String soldierUserId) { this.soldierUserId = soldierUserId; }
    public void setMamaUserId(String mamaUserId) { this.mamaUserId = mamaUserId; }
    public void setDate(long date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setKosher(boolean kosher) { this.kosher = kosher; }
    public void setLocation(String location) { this.location = location; }
    public void setNumberOfPeople(int numberOfPeople) { this.numberOfPeople = numberOfPeople; }
    public void setStatus(String status) { this.status = status; }
}