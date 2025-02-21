package com.example.vehicleapp;

import java.util.Objects;

public class Fuel {
    private String fuelType;
    private double co2PerLiter;

    // Getters and setters with cleaning logic
    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = (fuelType != null) ? fuelType.trim().toLowerCase() : null;
    }

    public double getCo2PerLiter() {
        return co2PerLiter;
    }

    public void setCo2PerLiter(double co2PerLiter) {
        this.co2PerLiter = co2PerLiter;
    }

    // Override equals and hashCode to ensure uniqueness based on 'fuelType'
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fuel fuel = (Fuel) o;
        return Objects.equals(fuelType, fuel.fuelType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fuelType);
    }

    @Override
    public String toString() {
        return "Fuel{" +
                "fuelType='" + fuelType + '\'' +
                ", co2PerLiter=" + co2PerLiter +
                '}';
    }
}
