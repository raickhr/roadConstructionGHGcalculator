// Updated EquipmentController.java to match MaterialController functionality
package com.example.vehicleapp;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.util.*;

@Controller
@RequestMapping("/equipment")
public class EquipmentController {
    private final Set<Equipment> equipmentSet = new HashSet<>();
    private final List<String> fuelTypeOptions = new ArrayList<>();
    private final List<String> categoryOptions = new ArrayList<>();
    private static final String EQUIPMENT_EXCEL_FILE = "equipment.xlsx";
    private static final String FUEL_EXCEL_FILE = "fuel.xlsx";
    private static final String CATEGORY_EXCEL_FILE = "equipmentCategories.xlsx";

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
                equipment.setType(row.getCell(3).getStringCellValue().trim().toLowerCase());
                equipment.setFuelType(row.getCell(4).getStringCellValue().trim().toLowerCase());
                equipment.setConsumptionPerHour(row.getCell(5).getNumericCellValue());
                equipment.setRemarks(row.getCell(6) != null ? row.getCell(6).getStringCellValue().trim() : "");
                equipmentSet.add(equipment);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load Fuel Types from Excel
    private void loadFuelTypesFromExcel() {
        fuelTypeOptions.clear();
        try (FileInputStream fileIn = new FileInputStream(FUEL_EXCEL_FILE);
             Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                String fuelType = row.getCell(0).getStringCellValue().trim().toLowerCase();
                if (!fuelTypeOptions.contains(fuelType)) {
                    fuelTypeOptions.add(fuelType);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Load Equipment Categories from excel
    private void loadCategoryOptionsFromExcel() {
        categoryOptions.clear();
        try (FileInputStream fileIn = new FileInputStream(CATEGORY_EXCEL_FILE);
             Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                String category = row.getCell(0).getStringCellValue().trim();
                if (!categoryOptions.contains(category)) {
                    categoryOptions.add(category);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // save equipments to excel
    private void saveEquipmentToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Equipment");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Equipment ID", "Category", "Name", "Type", "Fuel Type", "Consumption Per Hour", "Remarks"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            int rowNum = 1;
            for (Equipment equipment : equipmentSet) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(equipment.getEquipmentId());
                row.createCell(1).setCellValue(equipment.getCategory());
                row.createCell(2).setCellValue(equipment.getName());
                row.createCell(3).setCellValue(equipment.getType());
                row.createCell(4).setCellValue(equipment.getFuelType());
                row.createCell(5).setCellValue(equipment.getConsumptionPerHour());
                row.createCell(6).setCellValue(equipment.getRemarks());
            }

            try (FileOutputStream fileOut = new FileOutputStream(EQUIPMENT_EXCEL_FILE)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // get list of equipments and the options for equpment, fuel, and fiter category
    @GetMapping("/form")
    public String showForm(Model model, @ModelAttribute("categoryToShow") String categoryToShow) {
        loadEquipmentFromExcel();
        loadFuelTypesFromExcel();
        loadCategoryOptionsFromExcel();

        List<Equipment> filteredEquipment;
        if (categoryToShow != null && !categoryToShow.isEmpty() && !categoryToShow.equalsIgnoreCase("all")) {
            filteredEquipment = new ArrayList<>();
            for (Equipment equipment : equipmentSet) {
                if (equipment.getCategory().equalsIgnoreCase(categoryToShow)) {
                    filteredEquipment.add(equipment);
                }
            }
        } else {
            filteredEquipment = new ArrayList<>(equipmentSet);
        }

        model.addAttribute("equipment", new Equipment());
        model.addAttribute("equipmentList", filteredEquipment);
        model.addAttribute("fuelTypeOptions", fuelTypeOptions);
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("categoryToShow", categoryToShow);
        return "equipmentForm";
    }

    // Selecting Category to display only the selected category
    @PostMapping("/selectCategoryToShow")
    public String selectCategoryToShow(@RequestParam("categoryToShow") String categoryToShow, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow.trim().toLowerCase());
        return "redirect:/equipment/form";
    }

    // Add equipment 
    @PostMapping("/add")
    public String addEquipment(@ModelAttribute Equipment equipment, RedirectAttributes redirectAttributes) {
        if (equipmentSet.stream().anyMatch(e -> e.getEquipmentId().equalsIgnoreCase(equipment.getEquipmentId()))) {
            redirectAttributes.addFlashAttribute("error", "Equipment ID already exists!");
            return "redirect:/equipment/form";
        }

        equipmentSet.add(equipment);
        saveEquipmentToExcel();
        redirectAttributes.addFlashAttribute("message", "Equipment added successfully!");
        return "redirect:/equipment/form";
    }

    //Delete equipment
    @PostMapping("/{equipmentId}/delete")
    public String deleteEquipment(@PathVariable String equipmentId, @RequestParam("categoryToShow") String categoryToShow, RedirectAttributes redirectAttributes) {
        boolean removed = equipmentSet.removeIf(equipment -> equipment.getEquipmentId().equalsIgnoreCase(equipmentId));
        if (removed) {
            saveEquipmentToExcel();
            redirectAttributes.addFlashAttribute("message", "Equipment deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Equipment not found!");
        }
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow);
        return "redirect:/equipment/form";
    }

    // Edit Equipment
    @GetMapping("/{equipmentId}/edit")
    public String showEditForm(@PathVariable String equipmentId,
                            @RequestParam(value = "categoryToShow", required = false) String categoryToShow, 
                            Model model, RedirectAttributes redirectAttributes) {
        loadEquipmentFromExcel(); // Ensure data is loaded
        loadFuelTypesFromExcel(); // Load fuel types
        loadCategoryOptionsFromExcel(); // Load category options

        // Find the equipment to edit
        Equipment equipmentToEdit = equipmentSet.stream()
                .filter(e -> e.getEquipmentId().equalsIgnoreCase(equipmentId))
                .findFirst()
                .orElse(null);

        if (equipmentToEdit == null) {
            redirectAttributes.addFlashAttribute("error", "Equipment not found!");
            return "redirect:/equipment/form";
        }

        // Filter equipment based on the selected category
        List<Equipment> filteredEquipment;
        if (categoryToShow != null && !categoryToShow.isEmpty() && !categoryToShow.equalsIgnoreCase("all")) {
            filteredEquipment = new ArrayList<>();
            for (Equipment equipment : equipmentSet) {
                if (equipment.getCategory().equalsIgnoreCase(categoryToShow)) {
                    filteredEquipment.add(equipment);
                }
            }
        } else {
            filteredEquipment = new ArrayList<>(equipmentSet);
        }

        // Add existing equipment details to the model for pre-filling the form
        model.addAttribute("equipment", equipmentToEdit);
        model.addAttribute("equipmentList", filteredEquipment); // Use filtered list
        model.addAttribute("fuelTypeOptions", fuelTypeOptions);
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("isEdit", true); // Flag to indicate edit mode
        model.addAttribute("equipmentIdForEdit", equipmentId);
        model.addAttribute("categoryToShow", categoryToShow); // Persist the filter

        return "equipmentForm";
    }

    // Process the Edit Form Submission for Equipment
    @PostMapping("/{equipmentId}/update")
    public String updateEquipment(@PathVariable String equipmentId, 
                                @RequestParam("categoryToShow") String categoryToShow, 
                                @ModelAttribute Equipment updatedEquipment, 
                                RedirectAttributes redirectAttributes) {
        // Find the existing equipment by ID
        Optional<Equipment> existingEquipmentOptional = equipmentSet.stream()
                .filter(e -> e.getEquipmentId().equalsIgnoreCase(equipmentId))
                .findFirst();

        if (existingEquipmentOptional.isPresent()) {
            Equipment existingEquipment = existingEquipmentOptional.get();

            // Update equipment fields
            existingEquipment.setCategory(updatedEquipment.getCategory());
            existingEquipment.setName(updatedEquipment.getName());
            existingEquipment.setType(updatedEquipment.getType());
            existingEquipment.setFuelType(updatedEquipment.getFuelType());
            existingEquipment.setConsumptionPerHour(updatedEquipment.getConsumptionPerHour());
            existingEquipment.setRemarks(updatedEquipment.getRemarks());

            // Save updated equipment to Excel
            saveEquipmentToExcel();
            redirectAttributes.addFlashAttribute("message", "Equipment updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Equipment not found for update!");
        }

        // Retain the category filter after updating
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow);
        return "redirect:/equipment/form";
    }

    // Show Equipment Remark (similar to Material)
    @PostMapping("/{equipmentId}/showRemark")
    public String showEquipmentRemark(@PathVariable String equipmentId, 
                                    @RequestParam("categoryToShow") String categoryToShow, 
                                    RedirectAttributes redirectAttributes) {
        // Find the equipment by ID
        Equipment foundEquipment = equipmentSet.stream()
                .filter(e -> e.getEquipmentId().equalsIgnoreCase(equipmentId))
                .findFirst()
                .orElse(null);

        if (foundEquipment != null) {
            // If equipment is found, fetch and display the remark
            String remark = foundEquipment.getRemarks();
            redirectAttributes.addFlashAttribute("showRemark", remark);
            redirectAttributes.addFlashAttribute("equipmentIdForRemark", equipmentId);
        } else {
            // If not found, display an error message
            redirectAttributes.addFlashAttribute("error", "Equipment not found!");
        }

        // Retain the category filter after showing remark
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow);
        return "redirect:/equipment/form";
    }


}
