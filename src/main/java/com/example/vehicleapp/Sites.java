package com.example.vehicleapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Sites {
    
    @JsonProperty("siteName")
    private String siteName;

    @JsonProperty("tasks")
    private List<Task> tasks;

    // Default constructor (needed for JSON serialization/deserialization)
    public Sites() {
        this.tasks = new ArrayList<>();
    }

    // Constructor with JsonProperty
    @JsonCreator
    public Sites(@JsonProperty("siteName") String siteName) {
        this.siteName = siteName;
        this.tasks = new ArrayList<>();
    }

    // Getters and Setters with @JsonProperty
    @JsonProperty("siteName")
    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    @JsonProperty("tasks")
    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    // Methods to manage tasks
    public void addTask(Task task) {
        this.tasks.add(task);
    }

    public void removeTask(String taskName) {
        tasks.removeIf(task -> task.getTaskName().equalsIgnoreCase(taskName));
    }
}
