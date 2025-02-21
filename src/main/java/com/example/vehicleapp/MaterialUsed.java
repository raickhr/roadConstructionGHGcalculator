package com.example.vehicleapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class MaterialUsed implements Serializable {
    private String materialName;
    private Double massUsedInKg;

    // Constructor for JSON Deserialization
    @JsonCreator
    public MaterialUsed(@JsonProperty("materialName") String name, @JsonProperty("massUsedInKg") double massUsed) {
        this.materialName = name;
        this.massUsedInKg = massUsed;
    }

    // Getters and Setters
    public String getMaterialName() {
        return materialName;
    }

    public double getMassUsedInKg() {
        return massUsedInKg;
    }

    public void setMaterialName(String name) {
        this.materialName = name;
    }

    public void setMassUsedInKg(double massUsed) {
        this.massUsedInKg = massUsed;
    }

    // Default constructor (important for Spring & Thymeleaf)
    public MaterialUsed() {
        this.materialName = "";
        this.massUsedInKg = 0.0;  
    }
    
    // Display Material Info
    @Override
    public String toString() {
        return "MaterialUsed{name='" + materialName + "', massUsed=" + massUsedInKg + " kg}";
    }

    
}
