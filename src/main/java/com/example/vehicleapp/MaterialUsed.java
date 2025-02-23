package com.example.vehicleapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class MaterialUsed implements Serializable {
    private String materialName;
    private Double unitsUsed; // Represents how much material was used
    private String unit;      // Represents the unit measurement for the material (e.g., kg, meters)

    // Constructor for JSON Deserialization
    @JsonCreator
    public MaterialUsed(@JsonProperty("materialName") String materialName, 
                        @JsonProperty("unitsUsed") Double unitsUsed,
                        @JsonProperty("unit") String unit) {
        this.materialName = materialName;
        this.unitsUsed = unitsUsed;
        this.unit = unit;
    }

    // Default constructor (important for Spring & Thymeleaf)
    public MaterialUsed() {
        this.materialName = "";
        this.unitsUsed = 0.0;
        this.unit = "";
    }

    // Getters and Setters
    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public Double getUnitsUsed() {
        return unitsUsed;
    }

    public void setUnitsUsed(Double unitsUsed) {
        this.unitsUsed = unitsUsed;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    // Display Material Info
    @Override
    public String toString() {
        return "MaterialUsed{" +
                "materialName='" + materialName + '\'' +
                ", unitsUsed=" + unitsUsed +
                ", unit=" + unit +
                '}';
    }
}
