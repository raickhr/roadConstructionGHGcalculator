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
@RequestMapping("/material")
public class MaterialController {
    // Ensures uniqueness by materialId
    private final Set<Material> materialSet = new HashSet<>();
    private static final String MATERIAL_EXCEL_FILE = "materials.xlsx";
    private static final String CATEGORY_EXCEL_FILE = "materialCategories.xlsx"; // if you have one
    private static final String UNIT_EXCEL_FILE = "materialUnits.xlsx"; // if you have one

    // (Optional) Lists for additional drop-down data; adjust as needed.
    private final List<String> categoryOptions = new ArrayList<>();
    private final List<String> unitOptions = new ArrayList<>();

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

    // Optionally load category options from an Excel file
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

    // Optionally load unit options from an Excel file
    private void loadUnitOptionsFromExcel() {
        unitOptions.clear();
        try (FileInputStream fileIn = new FileInputStream(UNIT_EXCEL_FILE);
             Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                String unit = row.getCell(0).getStringCellValue().trim();
                if (!unitOptions.contains(unit)) {
                    unitOptions.add(unit);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save materials to Excel
    private void saveMaterialsToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Materials");
            Row headerRow = sheet.createRow(0);
            // Define headers (adjust order as needed)
            String[] columns = {"Material ID", 
                                "Category", 
                                "Material Name", 
                                "Unit", 
                                "CO2 Equivalent (per Unit)", 
                                "Scope", 
                                "Remarks"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }
            int rowNum = 1;
            for (Material material : materialSet) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(material.getMaterialId());
                row.createCell(1).setCellValue(material.getCategory());
                row.createCell(2).setCellValue(material.getMaterialName());
                row.createCell(3).setCellValue(material.getUnit());
                row.createCell(4).setCellValue(material.getCo2Equivalent());
                row.createCell(5).setCellValue(material.getScope());
                row.createCell(6).setCellValue(material.getRemarks());
            }
            try (FileOutputStream fileOut = new FileOutputStream(MATERIAL_EXCEL_FILE)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/form")
    public String showMaterialForm(Model model, @ModelAttribute("categoryToShow") String categoryToShow) {
        loadMaterialsFromExcel();
        loadCategoryOptionsFromExcel();
        loadUnitOptionsFromExcel();

        List<Material> filteredMaterials;
        if (categoryToShow != null && !categoryToShow.isEmpty() && !categoryToShow.equalsIgnoreCase("all")) {
            // Filter materials based on the selected category
            filteredMaterials = new ArrayList<>();
            for (Material material : materialSet) {
                if (material.getCategory().equalsIgnoreCase(categoryToShow)) {
                    filteredMaterials.add(material);
                }
            }
        } else {
            // If 'All' is selected or no category selected, show all materials
            filteredMaterials = new ArrayList<>(materialSet);
        }

        model.addAttribute("material", new Material());
        model.addAttribute("materialList", filteredMaterials);
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("unitOptions", unitOptions);
        model.addAttribute("categoryToShow", categoryToShow);

        return "materialForm";
    }

    @PostMapping("/add")
    public String addMaterial(@ModelAttribute Material material, RedirectAttributes redirectAttributes) {
        // Ensure uniqueness by materialId
        if (materialSet.stream().anyMatch(m -> m.getMaterialId().equalsIgnoreCase(material.getMaterialId()))) {
            redirectAttributes.addFlashAttribute("error", "Material ID already exists!");
            return "redirect:/material/form";
        }
        materialSet.add(material);
        saveMaterialsToExcel();
        redirectAttributes.addFlashAttribute("message", "Material added successfully!");
        return "redirect:/material/form";
    }

    // Edit Material
    @GetMapping("/{materialId}/edit")
    public String showEditForm(@PathVariable String materialId,
                            @RequestParam(value = "categoryToShow", required = false) String categoryToShow, 
                            Model model, RedirectAttributes redirectAttributes) {
        loadMaterialsFromExcel(); // Ensure data is loaded
        Material materialToEdit = materialSet.stream()
                .filter(m -> m.getMaterialId().equalsIgnoreCase(materialId))
                .findFirst()
                .orElse(null);

        if (materialToEdit == null) {
            redirectAttributes.addFlashAttribute("error", "Material not found!");
            return "redirect:/material/form";
        }

        // Load category and unit options
        loadCategoryOptionsFromExcel();
        loadUnitOptionsFromExcel();

        // Filter materials based on the selected category
        List<Material> filteredMaterials;
        if (categoryToShow != null && !categoryToShow.isEmpty() && !categoryToShow.equalsIgnoreCase("all")) {
            filteredMaterials = new ArrayList<>();
            for (Material material : materialSet) {
                if (material.getCategory().equalsIgnoreCase(categoryToShow)) {
                    filteredMaterials.add(material);
                }
            }
        } else {
            filteredMaterials = new ArrayList<>(materialSet);
        }

        // Add the existing material to the model for pre-filling the form
        model.addAttribute("material", materialToEdit);
        model.addAttribute("materialList", filteredMaterials);  // Use filtered list
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("unitOptions", unitOptions);
        model.addAttribute("isEdit", true); // Flag to indicate edit mode
        model.addAttribute("materialIdForEdit", materialId);
        model.addAttribute("categoryToShow", categoryToShow); // Persist the filter
        return "materialForm";
    }



    // Process the Edit Form Submission
    @PostMapping("/{materialId}/update")
    public String updateMaterial(@PathVariable String materialId, 
                                @RequestParam("categoryToShow") String categoryToShow, 
                                @ModelAttribute Material updatedMaterial, 
                                RedirectAttributes redirectAttributes) {
        Optional<Material> existingMaterialOptional = materialSet.stream()
                .filter(m -> m.getMaterialId().equalsIgnoreCase(materialId))
                .findFirst();

        if (existingMaterialOptional.isPresent()) {
            Material existingMaterial = existingMaterialOptional.get();

            // Update fields
            existingMaterial.setCategory(updatedMaterial.getCategory());
            existingMaterial.setMaterialName(updatedMaterial.getMaterialName());
            existingMaterial.setUnit(updatedMaterial.getUnit());
            existingMaterial.setCo2Equivalent(updatedMaterial.getCo2Equivalent());
            existingMaterial.setScope(updatedMaterial.getScope());
            existingMaterial.setRemarks(updatedMaterial.getRemarks());

            saveMaterialsToExcel();
            redirectAttributes.addFlashAttribute("message", "Material updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Material not found for update!");
        }

        // Retain the category filter after updating
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow);
        return "redirect:/material/form";
    }

    // Delete Material
    @PostMapping("/{materialId}/delete")
    public String deleteMaterial(@PathVariable String materialId, 
                                @RequestParam("categoryToShow") String categoryToShow, 
                                 RedirectAttributes redirectAttributes) {
        boolean removed = materialSet.removeIf(m -> m.getMaterialId().equalsIgnoreCase(materialId));
        if (removed) {
            saveMaterialsToExcel();
            redirectAttributes.addFlashAttribute("message", "Material deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Material not found!");
        }
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow);
        return "redirect:/material/form";
    }

    // Show Material Remark (if needed, similar to equipment)
    @PostMapping("/{materialId}/showRemark")
    public String showMaterialRemark(@PathVariable String materialId, 
                                    @RequestParam("categoryToShow") String categoryToShow, 
                                    RedirectAttributes redirectAttributes) {
        Material foundMaterial = materialSet.stream()
                .filter(m -> m.getMaterialId().equalsIgnoreCase(materialId))
                .findFirst().orElse(null);
        if (foundMaterial != null) {
            String remark = foundMaterial.getRemarks();
            redirectAttributes.addFlashAttribute("showRemark", remark);
            redirectAttributes.addFlashAttribute("materialIdForRemark", materialId);
        } else {
            redirectAttributes.addFlashAttribute("error", "Material not found!");
        }
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow);
        return "redirect:/material/form";
    }

    // Seclect Category to Show
    @PostMapping("/selectCategoryToShow")
    public String selectCategoryToShow(@RequestParam("categoryToShow") String categoryToShow, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("categoryToShow", categoryToShow.trim().toLowerCase());
        return "redirect:/material/form";
    }
    

    @PostMapping("/done")
    public String saveToExcel(RedirectAttributes redirectAttributes) {
        saveMaterialsToExcel();
        redirectAttributes.addFlashAttribute("message", "Materials data saved to Excel!");
        return "redirect:/material/form";
    }
    
    @GetMapping("/home")
    public String showHomePage() {
        saveMaterialsToExcel();
        return "home";
    }
}
