package com.example.vehicleapp;

import java.util.Objects;

public class Equipment {
    private String equipmentId;
    private String category;
    private String name;
    private String type;
    private String fuelType;
    private double consumptionPerHour;
    private String remarks;

    // Getters and setters with cleaning logic
    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = (equipmentId != null) ? equipmentId.trim().toUpperCase() : null;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = (category != null) ? category.trim().toLowerCase() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name != null) ? name.trim() : null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = (type != null) ? type.trim().toLowerCase() : null;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = (fuelType != null) ? fuelType.trim().toLowerCase() : null;
    }

    public double getConsumptionPerHour() {
        return consumptionPerHour;
    }

    public void setConsumptionPerHour(double consumptionPerHour) {
        this.consumptionPerHour = consumptionPerHour;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = (remarks != null) ? remarks.trim() : null;
    }

    // Override equals and hashCode to ensure uniqueness based on 'equipmentId'
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return Objects.equals(equipmentId, equipment.equipmentId);  // Uniqueness by 'equipmentId'
    }

    @Override
    public int hashCode() {
        return Objects.hash(equipmentId);
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "equipmentId='" + equipmentId + '\'' +
                ", category='" + category + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", fuelType='" + fuelType + '\'' +
                ", consumptionPerHour=" + consumptionPerHour +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
