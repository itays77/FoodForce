package com.example.foodforceapp.Models;

public class Soldier extends User {
    private String unit;
    private SoldierType sType;

    public Soldier() {
        super();
    }

    public Soldier(String id, String name, String email, String unit, SoldierType sType) {
        super(id, name, email, UserType.SOLDIER);
        this.unit = unit;
        this.sType = sType;
    }

    // Getters and setters
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public SoldierType getSType() { return sType; }
    public void setSType(SoldierType sType) { this.sType = sType; }

    public enum SoldierType {
        RESERVED, MANDATORY
    }
}