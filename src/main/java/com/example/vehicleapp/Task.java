package com.example.vehicleapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Task implements Serializable {
    
    @JsonProperty("taskName")
    private String taskName;

    @JsonProperty("listEquipmentUsed")
    private List<EquipmentUsed> listEquipmentUsed;

    @JsonProperty("listMaterialUsed")
    private List<MaterialUsed> listMaterialUsed;

    // Constructor for JSON Deserialization
    @JsonCreator
    public Task(@JsonProperty("taskName") String taskName) {
        this.taskName = taskName;
        this.listEquipmentUsed = new ArrayList<>();
        this.listMaterialUsed = new ArrayList<>();
    }

    // ****************** GETTER METHODS ******************

    @JsonProperty("taskName")
    public String getTaskName() {
        return taskName;
    }

    @JsonProperty("listEquipmentUsed")
    public List<EquipmentUsed> getListEquipmentUsed() {
        return listEquipmentUsed;
    }

    @JsonProperty("listMaterialUsed")
    public List<MaterialUsed> getListMaterialUsed() {
        return listMaterialUsed;
    }

    // ****************** EQUIPMENT MANAGEMENT ******************

    public void addEquipmentUsed(EquipmentUsed equipment) {
        listEquipmentUsed.add(equipment);
    }

    public void removeEquipmentUsed(String equipmentName) {
        listEquipmentUsed.removeIf(equipment -> equipment.getEquipmentName().equalsIgnoreCase(equipmentName));
    }

    public void editNameOfEquipmentUsed(String oldName, String newName) {
        for (EquipmentUsed equipment : listEquipmentUsed) {
            if (equipment.getEquipmentName().equalsIgnoreCase(oldName)) {
                equipment.setEquipmentName(newName);
                return;
            }
        }
    }

    public void editTimeUsedOfEquipmentUsed(String equipmentName, double newTimeUsed) {
        for (EquipmentUsed equipment : listEquipmentUsed) {
            if (equipment.getEquipmentName().equalsIgnoreCase(equipmentName)) {
                equipment.setTimeUsedInHours(newTimeUsed);
                return;
            }
        }
    }

    // ****************** MATERIAL MANAGEMENT ******************

    public void addMaterialUsed(MaterialUsed material) {
        listMaterialUsed.add(material);
    }

    public void removeMaterialUsed(String materialName) {
        listMaterialUsed.removeIf(material -> material.getMaterialName().equalsIgnoreCase(materialName));
    }

    public void editNameOfMaterialUsed(String oldName, String newName) {
        for (MaterialUsed material : listMaterialUsed) {
            if (material.getMaterialName().equalsIgnoreCase(oldName)) {
                material.setMaterialName(newName);
                return;
            }
        }
    }

    public void editMassUsedOfMaterialUsed(String materialName, double newMassUsed) {
        for (MaterialUsed material : listMaterialUsed) {
            if (material.getMaterialName().equalsIgnoreCase(materialName)) {
                material.setMassUsedInKg(newMassUsed);
                return;
            }
        }
    }
}
