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
    private final List<Fuel> fuelList = new ArrayList<>();
    private final List<String> categoryOptions = new ArrayList<>();
    private static final String EQUIPMENT_EXCEL_FILE = "equipment.xlsx";
    private static final String FUEL_EXCEL_FILE = "fuel.xlsx";
    private static final String CATEGORY_EXCEL_FILE = "equipmentCategories.xlsx";

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

    // Load fuel types from Excel
    // Method to load fuel types from the fuel.xlsx file into the fuelList
    private void loadFuelTypesFromExcel() {
        fuelList.clear();
        try (FileInputStream fileIn = new FileInputStream(FUEL_EXCEL_FILE);
             Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                Fuel fuel = new Fuel();
                fuel.setFuelId(row.getCell(0).getStringCellValue().trim().toUpperCase());
                fuel.setFuelName(row.getCell(1).getStringCellValue().trim().toLowerCase());
                fuel.setCo2PerUnit(row.getCell(2).getNumericCellValue());
                fuel.setUnit(row.getCell(3).getStringCellValue().trim().toLowerCase());
                fuel.setRemarks(row.getCell(4) != null ? row.getCell(4).getStringCellValue().trim() : "");
                fuelList.add(fuel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load categories from Excel
    // Method to load equipment categories from the equipmentCategories.xlsx file into categoryOptions
    private void loadCategoryOptionsFromExcel() {
        categoryOptions.clear();
        try (FileInputStream fileIn = new FileInputStream(CATEGORY_EXCEL_FILE);
             Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                String category = row.getCell(0).getStringCellValue().trim().toLowerCase();
                if (!categoryOptions.contains(category)) {
                    categoryOptions.add(category);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save equipment data to Excel
    // Method to save the equipmentSet data back into the equipment.xlsx file
    private void saveEquipmentToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Equipment");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Equipment ID", 
                                "Category", 
                                "Name", 
                                "Fuel Type", 
                                "Fuel Consumption Rate", 
                                "Fuel Consumption Rate Units", 
                                "Remarks"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            int rowNum = 1;
            for (Equipment equipment : equipmentSet) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(equipment.getEquipmentId());
                row.createCell(1).setCellValue(equipment.getCategory());
                row.createCell(2).setCellValue(equipment.getName());
                row.createCell(3).setCellValue(equipment.getFuelType());
                row.createCell(4).setCellValue(equipment.getFuelConsumptionRate());
                row.createCell(5).setCellValue(equipment.getFuelConsumptionRateUnits());
                row.createCell(6).setCellValue(equipment.getRemarks());
            }

            try (FileOutputStream fileOut = new FileOutputStream(EQUIPMENT_EXCEL_FILE)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Show equipment form
    // Displays the equipment form page with all existing equipment data, fuel types, and categories
    @GetMapping("/form")
    public String showForm(Model model, 
                          @ModelAttribute("categoryToShow") String categoryToShow) {
        loadEquipmentFromExcel();
        loadFuelTypesFromExcel();
        loadCategoryOptionsFromExcel();

        List<Equipment> filteredEquipments;
        if (categoryToShow != null && !categoryToShow.isEmpty() && !categoryToShow.equalsIgnoreCase("all")) {
            // Filter equipments based on the selected category
            filteredEquipments = new ArrayList<>();
            for (Equipment equipment : equipmentSet) {
                if (equipment.getCategory().equalsIgnoreCase(categoryToShow)) {
                    filteredEquipments.add(equipment);
                }
            }
        } else {
            // If 'All' is selected or no category selected, show all equipments
            filteredEquipments = new ArrayList<>(equipmentSet);
        }

        model.addAttribute("equipment", new Equipment());
        model.addAttribute("equipmentList", filteredEquipments);
        model.addAttribute("fuelList", fuelList);
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("categoryToShow", categoryToShow);
        return "equipmentForm";
    }

    // Add new equipment
    // Handles the submission of a new equipment entry, validates fuel type, and saves to Excel
    @PostMapping("/add")
    public String addEquipment(@ModelAttribute Equipment equipment, RedirectAttributes redirectAttributes) {
        String fuelType = equipment.getFuelType();
        Optional<Fuel> matchedFuel = fuelList.stream()
                .filter(f -> (fuelType.equalsIgnoreCase(f.getFuelName() + ":" + f.getFuelId())))
                .findFirst();

        if (matchedFuel.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid fuel type!");
            return "redirect:/equipment/form";
        }

        if (equipmentSet.stream().anyMatch(e -> e.getEquipmentId().equalsIgnoreCase(equipment.getEquipmentId()))) {
            redirectAttributes.addFlashAttribute("error", "Equipment ID already exists!");
            return "redirect:/equipment/form";
        }

        equipmentSet.add(equipment);
        saveEquipmentToExcel();
        redirectAttributes.addFlashAttribute("message", "Equipment added successfully!");
        return "redirect:/equipment/form";
    }

    // Edit equipment form
    // Displays the edit form for existing equipment identified by equipmentId
    @GetMapping("/{equipmentId}/edit")
    public String showEditForm(@PathVariable String equipmentId, 
                               Model model, 
                               @RequestParam("categoryToShow") String categoryToShow, 
                               RedirectAttributes redirectAttributes) {
        loadEquipmentFromExcel();
        loadFuelTypesFromExcel();
        loadCategoryOptionsFromExcel();
        Equipment equipmentToEdit = equipmentSet.stream()
                .filter(e -> e.getEquipmentId().equalsIgnoreCase(equipmentId))
                .findFirst()
                .orElse(null);

        if (equipmentToEdit == null) {
            redirectAttributes.addFlashAttribute("error", "Equipment not found!");
            return "redirect:/equipment/form";
        }

        // Filter equipments based on the selected category
        List<Equipment> filteredEquipments;
        if (categoryToShow != null && !categoryToShow.isEmpty() && !categoryToShow.equalsIgnoreCase("all")) {
            filteredEquipments = new ArrayList<>();
            for (Equipment equipment : equipmentSet) {
                if (equipment.getCategory().equalsIgnoreCase(categoryToShow)) {
                    filteredEquipments.add(equipment);
                }
            }
        } else {
            filteredEquipments = new ArrayList<>(equipmentSet);
        }

        model.addAttribute("isEdit", true);
        model.addAttribute("equipmentIdForEdit", equipmentId);
        model.addAttribute("equipment", equipmentToEdit);
        model.addAttribute("equipmentList", new ArrayList<>(equipmentSet));
        model.addAttribute("fuelList", fuelList);
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("categoryToShow", categoryToShow); // Persist the filter
        model.addAttribute("scrollToEquipmentId", equipmentId); //for scrolling to the location of editing equipment
        
        return "equipmentForm";
    }

    // Update equipment details
    // Handles updating of existing equipment details and saves the updated data
    @PostMapping("/{equipmentId}/update")
    public String updateEquipment(@PathVariable String equipmentId, 
                                  @ModelAttribute Equipment updatedEquipment,
                                  @RequestParam("categoryToShow") String categoryToShow,  
                                  RedirectAttributes redirectAttributes) {
        Optional<Equipment> existingEquipmentOptional = equipmentSet.stream()
                .filter(e -> e.getEquipmentId().equalsIgnoreCase(equipmentId))
                .findFirst();

        if (existingEquipmentOptional.isPresent()) {
            Equipment existingEquipment = existingEquipmentOptional.get();

            // Update fields
            existingEquipment.setCategory(updatedEquipment.getCategory());
            existingEquipment.setName(updatedEquipment.getName());
            existingEquipment.setFuelType(updatedEquipment.getFuelType());
            existingEquipment.setFuelConsumptionRate(updatedEquipment.getFuelConsumptionRate());
            existingEquipment.setFuelConsumptionRateUnits(updatedEquipment.getFuelConsumptionRateUnits());
            existingEquipment.setRemarks(updatedEquipment.getRemarks());
            saveEquipmentToExcel();
            redirectAttributes.addFlashAttribute("message", "Equipment updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Equipment not found for update!");
        }

        // Retain the category filter after updating
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow);
        // For retaining the scrolling location
        redirectAttributes.addFlashAttribute("scrollToEquipmentId", equipmentId);
        return "redirect:/equipment/form";
    }

    // Delete equipment
    // Deletes the equipment entry identified by equipmentId and updates Excel
    @PostMapping("/{equipmentId}/delete")
    public String deleteEquipment(@PathVariable String equipmentId, 
                                  @RequestParam("categoryToShow") String categoryToShow, 
                                  RedirectAttributes redirectAttributes) {
        boolean removed = equipmentSet.removeIf(e -> e.getEquipmentId().equalsIgnoreCase(equipmentId));
        if (removed) {
            saveEquipmentToExcel();
            redirectAttributes.addFlashAttribute("message", "Equipment deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Equipment not found!");
        }

        // Retain the category filter after deleting
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow);

        // For retaining the scrolling location
        redirectAttributes.addFlashAttribute("scrollToEquipmentId", equipmentId);
        return "redirect:/equipment/form";
    }

    // Show equipment remark
    // Displays the remark associated with a specific equipment entry
    @PostMapping("/{equipmentId}/showRemark")
    public String showRemark(@PathVariable String equipmentId, 
                             RedirectAttributes redirectAttributes,
                             @RequestParam("categoryToShow") String categoryToShow) {
        Equipment foundEquipment = equipmentSet.stream()
                .filter(e -> e.getEquipmentId().equalsIgnoreCase(equipmentId))
                .findFirst()
                .orElse(null);
        if (foundEquipment != null) {
            redirectAttributes.addFlashAttribute("showRemark", foundEquipment.getRemarks());
            redirectAttributes.addFlashAttribute("equipmentIdForRemark", equipmentId);
        } else {
            redirectAttributes.addFlashAttribute("error", "Equipment not found!");
        }

        // Retain the category filter after showing remark
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow);

        // For retaining the scrolling location
        redirectAttributes.addFlashAttribute("scrollToEquipmentId", equipmentId);
        return "redirect:/equipment/form";
    }

    // Select category to show
    @PostMapping("/selectCategoryToShow")
    public String selectCategoryToShow(@RequestParam("categoryToShow") String categoryToShow, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow.trim().toLowerCase());
        return "redirect:/equipment/form";
    }
}
