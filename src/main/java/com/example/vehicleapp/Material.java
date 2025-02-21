package com.example.vehicleapp;

import java.util.Objects;
import java.util.Arrays;
import java.util.List;

public class Material {
    private String materialId;
    private String category;
    private String materialName;
    private String unit;
    private double co2Equivalent;
    private String remarks;
    private String scope;

    // Allowed scope values
    private static final List<String> ALLOWED_SCOPES = Arrays.asList(
        "Scope 1", 
        "Scope 2", 
        "Scope 3"
    );

    // Getters and Setters with Cleaning Logic

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = (materialId != null) ? materialId.trim().toUpperCase() : null;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = (category != null) ? category.trim().toLowerCase() : null;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = (materialName != null) ? materialName.trim() : null;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = (unit != null) ? unit.trim().toLowerCase() : null;
    }

    public double getCo2Equivalent() {
        return co2Equivalent;
    }

    public void setCo2Equivalent(double co2Equivalent) {
        this.co2Equivalent = co2Equivalent;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = (remarks != null) ? remarks.trim() : null;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        if (scope != null && ALLOWED_SCOPES.contains(scope)) {
            this.scope = scope;
        } else {
            throw new IllegalArgumentException("Invalid scope value! Allowed values: " + ALLOWED_SCOPES);
        }
    }

    // Override equals and hashCode to ensure uniqueness based on 'materialId'
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return Objects.equals(materialId, material.materialId);  // Unique by 'materialId'
    }

    @Override
    public int hashCode() {
        return Objects.hash(materialId);
    }

    @Override
    public String toString() {
        return "Material{" +
                "materialId='" + materialId + '\'' +
                ", category='" + category + '\'' +
                ", materialName='" + materialName + '\'' +
                ", unit='" + unit + '\'' +
                ", co2Equivalent=" + co2Equivalent +
                ", remarks='" + remarks + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}
