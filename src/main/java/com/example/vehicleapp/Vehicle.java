package com.example.vehicleapp;

import java.util.Objects;

public class Vehicle {
    private String type;
    private String fuelType;
    private double mpg;

    // Getters and setters with cleaning logic
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = (type != null) ? type.trim().toLowerCase() : null;  // Trim spaces and lowercase
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = (fuelType != null) ? fuelType.trim().toLowerCase() : null;  // Trim spaces and lowercase
    }

    public double getMpg() {
        return mpg;
    }

    public void setMpg(double mpg) {
        this.mpg = mpg;
    }

    // Override equals and hashCode to ensure uniqueness based on 'type'
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(type, vehicle.type);  // Compare based on 'type'
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);  // Generate hash based only on 'type'
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "type='" + type + '\'' +
                ", fuelType='" + fuelType + '\'' +
                ", mpg=" + mpg +
                '}';
    }
}
