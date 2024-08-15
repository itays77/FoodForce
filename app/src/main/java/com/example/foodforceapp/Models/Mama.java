package com.example.foodforceapp.Models;

import java.util.List;

public class Mama extends User {
    private String location;
    private List<String> specialties;

    public Mama() {
        super();
        setType(UserType.MAMA);
    }

    public String getLocation() {
        return location;
    }

    public Mama setLocation(String location) {
        this.location = location;
        return this;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public Mama setSpecialties(List<String> specialties) {
        this.specialties = specialties;
        return this;
    }
}
