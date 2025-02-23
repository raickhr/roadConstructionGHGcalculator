package com.example.vehicleapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.util.*;



@Controller
@RequestMapping("/sites")
public class SiteController {
    private static final String FILE_PATH = "sites.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Equipment file and Set
    private static final String EQUIPMENT_EXCEL_FILE = "equipment.xlsx";
    private final Set<Equipment> equipmentSet = new HashSet<>();
    
    // Material file and set
    private static final String MATERIAL_EXCEL_FILE = "materials.xlsx";
    private final Set<Material> materialSet = new HashSet<>();
    
    
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
        // Reading equipments from excel file
        loadEquipmentFromExcel();

        // Reading materials from excel file
        loadMaterialsFromExcel();


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
    public String showTaskDetailsForEquipment(@PathVariable String siteName, 
                                              @PathVariable String taskName, 
                                               Model model) {
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

    // Load equipment data from Excel
    // Method to load equipment data from the equipment.xlsx file into the equipmentSet
    private void loadEquipmentFromExcel() {
        if (!equipmentSet.isEmpty()) return;
        try (FileInputStream fileIn = new FileInputStream(EQUIPMENT_EXCEL_FILE);
            Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                Equipment equipment = new Equipment();
                equipment.setEquipmentId(row.getCell(0).getStringCellValue().trim().toUpperCase());
                equipment.setCategory(row.getCell(1).getStringCellValue().trim().toLowerCase());
                equipment.setName(row.getCell(2).getStringCellValue().trim());
                equipment.setFuelType(row.getCell(3).getStringCellValue().trim().toLowerCase());
                equipment.setFuelConsumptionRate(row.getCell(4).getNumericCellValue());
                equipment.setFuelConsumptionRateUnits(row.getCell(5).getStringCellValue().trim().toLowerCase());
                equipment.setRemarks(row.getCell(6) != null ? row.getCell(6).getStringCellValue().trim() : "");
                equipmentSet.add(equipment);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Dynamic Eqiupment Search Endpoint
    @GetMapping("/equipment/search")
    @ResponseBody
    public ResponseEntity<List<Equipment>> searchEquipment(@RequestParam("query") String query) {
        List<Equipment> matchingEquipments = new ArrayList<>();
        for (Equipment equipment : equipmentSet) {
            if (equipment.getName().toLowerCase().contains(query.toLowerCase()) ||
                equipment.getEquipmentId().toLowerCase().contains(query.toLowerCase()) ||
                equipment.getCategory().toLowerCase().contains(query.toLowerCase())) {
                matchingEquipments.add(equipment);
            }
        }
        return ResponseEntity.ok(matchingEquipments);
    }


    // Add Equipment to a Task in a Site
    @PostMapping("/{siteName}/tasks/{taskName}/equipment/add")
    public String addEquipment(@PathVariable String siteName, 
                               @PathVariable String taskName, 
                               @ModelAttribute EquipmentUsed equipment, 
                               RedirectAttributes redirectAttributes) {
        // Reading Sites
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

    // Edit Material Entry
    @PostMapping("/{siteName}/tasks/{taskName}/equipment/{equipmentName}/edit")
    public String editEquipment(@PathVariable String siteName, 
                                @PathVariable String taskName,
                                @PathVariable String equipmentName,
                                Model model) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                for (Task task : site.getTasks()) {
                    if (task.getTaskName().equalsIgnoreCase(taskName)) {
                        for (EquipmentUsed equipment : task.getListEquipmentUsed()) {
                            if (equipment.getEquipmentName().equalsIgnoreCase(equipmentName)) {
                                // Pass material for editing
                                model.addAttribute("editEquipment", true);
                                model.addAttribute("selectedSite", site);
                                model.addAttribute("selectedTask", task);

                                model.addAttribute("isEquipmentUsedEdit", true);
                                model.addAttribute("equipmentUsedNameForEdit", equipmentName);
                                model.addAttribute("equipmentUsed", equipment);
                                return "sites/siteForm"; // Direct rendering without redirection
                            }
                        }
                    }
                }
            }
        }
        return "redirect:/sites/form";
    }


    // Update Material Entry after editing
    @PostMapping("/{siteName}/tasks/{taskName}/equipment/{equipmentUsedName}/update")
    public String updateEqiupment(@PathVariable String siteName, 
                                 @PathVariable String taskName,
                                 @PathVariable String equipmentUsedName,
                                 @ModelAttribute EquipmentUsed updatedEquipment,
                                 RedirectAttributes redirectAttributes) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                for (Task task : site.getTasks()) {
                    if (task.getTaskName().equalsIgnoreCase(taskName)) {
                        for (EquipmentUsed existingEquipment : task.getListEquipmentUsed()) {
                            if (existingEquipment.getEquipmentName().equalsIgnoreCase(equipmentUsedName)) {
                                // Remove the old equipment
                                task.removeEquipmentUsed(equipmentUsedName);

                                // Preserve original equpment name
                                updatedEquipment.setEquipmentName(existingEquipment.getEquipmentName());
                                // updatedEquipment.setUnit(existingEquipment.getUnit());

                                // Add updated material back to the list
                                task.addEquipmentUsed(updatedEquipment);
                                writeSites(sites);

                                redirectAttributes.addFlashAttribute("message", "Equipment updated successfully!");
                                return "redirect:/sites/" + siteName + "/tasks/" + taskName + "/editEquipment";
                            }
                        }
                    }
                }
            }
        }
        return "redirect:/sites/form";
    } 

    // ****************** MATERIAL MANAGEMENT ******************

    // Load materials from Excel
    private void loadMaterialsFromExcel() {
        if (!materialSet.isEmpty()) return;
        try (FileInputStream fileIn = new FileInputStream(MATERIAL_EXCEL_FILE);
             Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            // Assuming the first row is the header
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                Material material = new Material();
                // Assume columns: materialId, category, materialName, unit, co2Equivalent, scope, remarks
                material.setMaterialId(row.getCell(0).getStringCellValue().trim().toUpperCase());
                material.setCategory(row.getCell(1).getStringCellValue().trim().toLowerCase());
                material.setMaterialName(row.getCell(2).getStringCellValue().trim());
                material.setUnit(row.getCell(3).getStringCellValue().trim().toLowerCase());
                material.setCo2Equivalent(row.getCell(4).getNumericCellValue());
                material.setScope(row.getCell(5).getStringCellValue().trim());
                material.setRemarks(row.getCell(6) != null ? row.getCell(6).getStringCellValue().trim() : "");
                materialSet.add(material);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Dynamic Material Search Endpoint
    @GetMapping("/materials/search")
    @ResponseBody
    public ResponseEntity<List<Material>> searchMaterials(@RequestParam("query") String query) {
        List<Material> matchingMaterials = new ArrayList<>();
        for (Material material : materialSet) {
            if (material.getMaterialName().toLowerCase().contains(query.toLowerCase()) ||
                material.getMaterialId().toLowerCase().contains(query.toLowerCase()) ||
                material.getCategory().toLowerCase().contains(query.toLowerCase())) {
                matchingMaterials.add(material);
            }
        }
        return ResponseEntity.ok(matchingMaterials);
    }

    // Add Material to a Task in a Site
    @PostMapping("/{siteName}/tasks/{taskName}/materials/add")
    public String addMaterial(@PathVariable String siteName, 
                              @PathVariable String taskName,
                              @ModelAttribute MaterialUsed material, 
                              RedirectAttributes redirectAttributes) {
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
    public String deleteMaterial(@PathVariable String siteName, 
                                 @PathVariable String taskName, 
                                 @PathVariable String materialName, 
                                 RedirectAttributes redirectAttributes) {
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

    // Edit Material Entry
    @PostMapping("/{siteName}/tasks/{taskName}/materials/{materialName}/edit")
    public String editMaterial(@PathVariable String siteName, 
                            @PathVariable String taskName,
                            @PathVariable String materialName,
                            Model model) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                for (Task task : site.getTasks()) {
                    if (task.getTaskName().equalsIgnoreCase(taskName)) {
                        for (MaterialUsed material : task.getListMaterialUsed()) {
                            if (material.getMaterialName().equalsIgnoreCase(materialName)) {
                                // Pass material for editing
                                model.addAttribute("editMaterial", true);
                                model.addAttribute("selectedSite", site);
                                model.addAttribute("selectedTask", task);

                                model.addAttribute("isMaterialUsedEdit", true);
                                model.addAttribute("materialUsedNameForEdit", materialName);
                                model.addAttribute("materialUsed", material);
                                return "sites/siteForm"; // Direct rendering without redirection
                            }
                        }
                    }
                }
            }
        }
        return "redirect:/sites/form";
    }


    // Update Material Entry after editing
    @PostMapping("/{siteName}/tasks/{taskName}/materials/{materialUsedName}/update")
    public String updateMaterial(@PathVariable String siteName, 
                                @PathVariable String taskName,
                                @PathVariable String materialUsedName,
                                @ModelAttribute MaterialUsed updatedMaterial,
                                RedirectAttributes redirectAttributes) {
        List<Sites> sites = readSites();
        for (Sites site : sites) {
            if (site.getSiteName().equalsIgnoreCase(siteName)) {
                for (Task task : site.getTasks()) {
                    if (task.getTaskName().equalsIgnoreCase(taskName)) {
                        for (MaterialUsed existingMaterial : task.getListMaterialUsed()) {
                            if (existingMaterial.getMaterialName().equalsIgnoreCase(materialUsedName)) {
                                // Remove the old material
                                task.removeMaterialUsed(materialUsedName);

                                // Preserve original material name and unit
                                updatedMaterial.setMaterialName(existingMaterial.getMaterialName());
                                updatedMaterial.setUnit(existingMaterial.getUnit());

                                // Add updated material back to the list
                                task.addMaterialUsed(updatedMaterial);
                                writeSites(sites);

                                redirectAttributes.addFlashAttribute("message", "Material updated successfully!");
                                return "redirect:/sites/" + siteName + "/tasks/" + taskName + "/editMaterial";
                            }
                        }
                    }
                }
            }
        }
        return "redirect:/sites/form";
    } 

}
