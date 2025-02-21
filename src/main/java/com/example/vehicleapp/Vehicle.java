package com.example.vehicleapp;

import java.util.Objects;

public class Vehicle {
    private String vehicleId;
    private String vehicleName;
    private String fuelType;
    private String mileageUnit; // Changed from double to String
    private double mileageValue;
    private String remarks; // Add remarks field

    // Getters and Setters with cleaning logic for each member
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = (vehicleId != null) ? vehicleId.trim() : null;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = (vehicleName != null) ? vehicleName.trim() : null;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = (fuelType != null) ? fuelType.trim().toLowerCase() : null; // Trim spaces and lowercase
    }

    public String getMileageUnit() { // Changed from double to String
        return mileageUnit;
    }

    public void setMileageUnit(String mileageUnit) { // Changed from double to String
        this.mileageUnit = (mileageUnit != null) ? mileageUnit.trim() : null; // Ensure it's cleaned
    }

    public double getMileageValue() {
        return mileageValue;
    }

    public void setMileageValue(double mileageValue) {
        this.mileageValue = mileageValue;
    }

    public String getRemarks() { // Getter for remarks
        return remarks;
    }

    public void setRemarks(String remarks) { // Setter for remarks
        this.remarks = (remarks != null) ? remarks.trim() : null;
    }

    // Override equals and hashCode to ensure uniqueness based on 'vehicleId'
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(vehicleId, vehicle.vehicleId); // Compare based on 'vehicleId'
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId); // Generate hash based only on 'vehicleId'
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicleId='" + vehicleId + '\'' +
                ", vehicleName='" + vehicleName + '\'' +
                ", fuelType='" + fuelType + '\'' +
                ", mileageUnit='" + mileageUnit + '\'' + // Updated to String
                ", mileageValue=" + mileageValue +
                ", remarks='" + remarks + '\'' + // Add remarks in toString
                '}';
    }
}
