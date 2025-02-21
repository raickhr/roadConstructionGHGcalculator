package com.example.vehicleapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class EquipmentUsed implements Serializable {
    private String equipmentName;
    private Double timeUsedInHours;  //

    // Constructor for JSON Deserialization
    @JsonCreator
    public EquipmentUsed(@JsonProperty("equipmentName") String name, @JsonProperty("timeUsedInHours") Double timeUsed) {
        this.equipmentName = name;
        this.timeUsedInHours = timeUsed;
    }

    // Getters and Setters
    public String getEquipmentName() {
        return equipmentName;
    }

    public Double getTimeUsedInHours() {  // 
        return timeUsedInHours;
    }

    public void setEquipmentName(String name) {
        this.equipmentName = name;
    }

    public void setTimeUsedInHours(Double timeUsed) {  
        this.timeUsedInHours = timeUsed;
    }

    // Default constructor (important for Spring & Thymeleaf)
    public EquipmentUsed() {
        this.equipmentName = "";
        this.timeUsedInHours = 0.0;  
    }

    @Override
    public String toString() {
        return "EquipmentUsed{name='" + equipmentName + "', timeUsed=" + timeUsedInHours + " hours}";
    }
}
