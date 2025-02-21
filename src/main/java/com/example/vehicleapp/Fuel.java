package com.example.vehicleapp;

import java.util.Objects;

public class Fuel {
    private String fuelId;
    private String fuelName; // Renamed from fuelType to fuelName
    private double co2PerUnit;
    private String unit; // New member for unit (e.g., liters, gallons)
    private String remarks;

    // Getters and setters
    public String getFuelId() {
        return fuelId;
    }

    public void setFuelId(String fuelId) {
        this.fuelId = (fuelId != null) ? fuelId.trim().toUpperCase() : null;
    }

    public String getFuelName() {
        return fuelName;
    }

    public void setFuelName(String fuelName) {
        this.fuelName = (fuelName != null) ? fuelName.trim().toLowerCase() : null;
    }

    public double getCo2PerUnit() {
        return co2PerUnit;
    }

    public void setCo2PerUnit(double co2PerUnit) {
        this.co2PerUnit = co2PerUnit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = (unit != null) ? unit.trim().toLowerCase() : null;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = (remarks != null) ? remarks.trim() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Fuel fuel = (Fuel) o;
        return Objects.equals(fuelName, fuel.fuelName); // Updated comparison
    }

    @Override
    public int hashCode() {
        return Objects.hash(fuelName); // Updated hash code
    }

    @Override
    public String toString() {
        return "Fuel{" +
                "fuelId='" + fuelId + '\'' +
                ", fuelName='" + fuelName + '\'' +
                ", co2PerUnit=" + co2PerUnit +
                ", unit='" + unit + '\'' +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
