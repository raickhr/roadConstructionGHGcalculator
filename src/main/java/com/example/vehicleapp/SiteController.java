package com.example.vehicleapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sites")
public class SiteController {
    private static final String FILE_PATH = "sites.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Ensure sites.json exists before reading/writing
    private List<Sites> readSites() {
        File file = new File(FILE_PATH);
        
        // If file doesn't exist or is empty, initialize an empty list
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
    
        try {
            return objectMapper.readValue(file, new TypeReference<List<Sites>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }    

    // Write data to sites.json
    private void writeSites(List<Sites> sites) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), sites);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    

    // ****************** SITE MANAGEMENT ******************

    // Show the site form with all existing sites
    @GetMapping("/form")
    public String showSiteForm(Model model) {
        List<Sites> sites = readSites();
        model.addAttribute("sites", sites);
        model.addAttribute("site", new Sites(""));
        return "sites/siteForm";
    }

    // Show details of a specific site
    @GetMapping("/{siteName}")
    public String showSiteDetails(@PathVariable String siteName, Model model) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                model.addAttribute("selectedSite", site);
                model.addAttribute("task", new Task(""));
                model.addAttribute("sites", sites);
                // System.out.println("Selected Site: " + site.getSiteName()); // Debug log
                // System.out.println("Tasks Count: " + site.getTasks().size()); // Debug log
                return "sites/siteForm";
            }
        }
        return "redirect:/sites/form";
    }

    // Create a new site
    @PostMapping("/create")
    public String createSite(@ModelAttribute Sites site, RedirectAttributes redirectAttributes) {
        List<Sites> sites = readSites();
        sites.add(site);
        writeSites(sites);
        redirectAttributes.addFlashAttribute("message", 
        "Site created successfully!");
        return "redirect:/sites/form";
    }

    // Delete a site
    @PostMapping("/{siteName}/delete")
    public String deleteSite(@PathVariable String siteName, RedirectAttributes redirectAttributes) {
        List<Sites> sites = readSites();
        boolean removed = sites.removeIf(site -> site.getSiteName().equalsIgnoreCase(siteName));
        if (removed) {
            writeSites(sites);
            redirectAttributes.addFlashAttribute("message", 
            "Site deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", 
            "Site not found!");
        }
        return "redirect:/sites/form";
    }

    // ****************** TASK MANAGEMENT ******************

    // Show details of a specific task for editing Equipment
    @GetMapping("/{siteName}/tasks/{taskName}/editEquipment")
    public String showTaskDetailsForEquipment(@PathVariable String siteName, @PathVariable String taskName, Model model) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                for (Task task : site.getTasks()) {
                    if (task.getTaskName().equalsIgnoreCase(taskName)) {
                        model.addAttribute("selectedSite", site);
                        model.addAttribute("selectedTask", task);
                        model.addAttribute("tasks", site.getTasks());
                        model.addAttribute("equipmentUsed", new EquipmentUsed());
                        model.addAttribute("editEquipment", true);
                        return "sites/siteForm";
                    }
                }
            }
        }
        return "redirect:/sites/form";
    }

    // Show details of a specific task for editing material
    @GetMapping("/{siteName}/tasks/{taskName}/editMaterial")
    public String showTaskDetailsForMaterial(@PathVariable String siteName, @PathVariable String taskName, Model model) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                for (Task task : site.getTasks()) {
                    if (task.getTaskName().equalsIgnoreCase(taskName)) {
                        model.addAttribute("selectedSite", site);
                        model.addAttribute("selectedTask", task);
                        model.addAttribute("tasks", site.getTasks());
                        model.addAttribute("materialUsed", new MaterialUsed());
                        model.addAttribute("editMaterial", true);
                        return "sites/siteForm";
                    }
                }
            }
        }
        return "redirect:/sites/form";
    }
    


    // Add a task to a site
    @PostMapping("/{siteName}/tasks/create")
    public String addTaskToSite(@PathVariable String siteName, @ModelAttribute Task task, RedirectAttributes redirectAttributes) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                site.addTask(task);
                writeSites(sites);
                redirectAttributes.addFlashAttribute("message", 
                "Task added successfully!");
                return "redirect:/sites/" + siteName;
            }
        }
        redirectAttributes.addFlashAttribute("error", 
        "Site not found!");
        return "redirect:/sites/form";
    }

    // Delete a task from a site
    @PostMapping("/{siteName}/tasks/{taskName}/delete")
    public String deleteTask(@PathVariable String siteName, @PathVariable String taskName, RedirectAttributes redirectAttributes) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                site.removeTask(taskName);
                writeSites(sites);
                redirectAttributes.addFlashAttribute("message", 
                "Task deleted successfully!");
                return "redirect:/sites/" + siteName;
            }
        }
        return "redirect:/sites/form";
    }

    // ****************** EQUIPMENT MANAGEMENT ******************

    // Add Equipment to a Task in a Site
    @PostMapping("/{siteName}/tasks/{taskName}/equipment/add")
    public String addEquipment(@PathVariable String siteName, 
                               @PathVariable String taskName, 
                               @ModelAttribute EquipmentUsed equipment, 
                               RedirectAttributes redirectAttributes) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                for (Task task : site.getTasks()) {
                    if (task.getTaskName().equalsIgnoreCase(taskName)) {
                        task.addEquipmentUsed(equipment);
                        writeSites(sites);
                        redirectAttributes.addFlashAttribute("message", 
                        "Equipment added successfully!");
                        return "redirect:/sites/" + siteName + "/tasks/"+ taskName + "/editEquipment";
                    }
                }
            }
        }
        return "redirect:/sites/form";
    }

    // Delete Equipment from a Task
    @PostMapping("/{siteName}/tasks/{taskName}/equipment/{equipmentName}/delete")
    public String deleteEquipment(@PathVariable String siteName, @PathVariable String taskName, @PathVariable String equipmentName, RedirectAttributes redirectAttributes) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                for (Task task : site.getTasks()) {
                    if (task.getTaskName().equalsIgnoreCase(taskName)) {
                        task.removeEquipmentUsed(equipmentName);
                        writeSites(sites);
                        redirectAttributes.addFlashAttribute("message", 
                        "Equipment deleted successfully!");
                        return "redirect:/sites/" + siteName + "/tasks/"+ taskName + "/editEquipment";
                    }
                }
            }
        }
        return "redirect:/sites/form";
    }

    // ****************** MATERIAL MANAGEMENT ******************

    // Add Material to a Task in a Site
    @PostMapping("/{siteName}/tasks/{taskName}/materials/add")
    public String addMaterial(@PathVariable String siteName, @PathVariable String taskName, @ModelAttribute MaterialUsed material, RedirectAttributes redirectAttributes) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                for (Task task : site.getTasks()) {
                    if (task.getTaskName().equalsIgnoreCase(taskName)) {
                        task.addMaterialUsed(material);
                        writeSites(sites);
                        redirectAttributes.addFlashAttribute("message", 
                        "Material added successfully!");
                        return "redirect:/sites/" + siteName + "/tasks/"+ taskName +"/editMaterial";
                    }
                }
            }
        }
        return "redirect:/sites/form";
    }

    // Delete Material from a Task
    @PostMapping("/{siteName}/tasks/{taskName}/materials/{materialName}/delete")
    public String deleteMaterial(@PathVariable String siteName, @PathVariable String taskName, @PathVariable String materialName, RedirectAttributes redirectAttributes) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                for (Task task : site.getTasks()) {
                    if (task.getTaskName().equalsIgnoreCase(taskName)) {
                        task.removeMaterialUsed(materialName);
                        writeSites(sites);
                        redirectAttributes.addFlashAttribute("message", 
                        "Material deleted successfully!");
                        return "redirect:/sites/" + siteName + "/tasks/"+ taskName +"/editMaterial";
                    }
                }
            }
        }
        return "redirect:/sites/form";
    }
}
