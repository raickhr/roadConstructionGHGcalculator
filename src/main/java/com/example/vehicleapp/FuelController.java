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
@RequestMapping("/fuel")
public class FuelController {
    private final Set<Fuel> fuelSet = new HashSet<>();
    private static final String FUEL_EXCEL_FILE = "fuel.xlsx";

    // Load fuels from Excel
    private void loadFuelsFromExcel() {
        if (!fuelSet.isEmpty())
            return;
        try (FileInputStream fileIn = new FileInputStream(FUEL_EXCEL_FILE);
                Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
                Fuel fuel = new Fuel();
                fuel.setFuelId(row.getCell(0).getStringCellValue().trim().toUpperCase());
                fuel.setFuelName(row.getCell(1).getStringCellValue().trim().toLowerCase());
                fuel.setCo2PerUnit(row.getCell(2).getNumericCellValue());
                fuel.setUnit(row.getCell(3).getStringCellValue().trim().toLowerCase());
                fuel.setRemarks(row.getCell(4) != null ? row.getCell(4).getStringCellValue().trim() : "");
                fuelSet.add(fuel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save fuels to Excel
    private void saveFuelsToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fuels");
            Row headerRow = sheet.createRow(0);
            String[] columns = { "Fuel ID", "Fuel Name", "CO2 Per Unit", "Unit", "Remarks" };
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }
            int rowNum = 1;
            for (Fuel fuel : fuelSet) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(fuel.getFuelId());
                row.createCell(1).setCellValue(fuel.getFuelName());
                row.createCell(2).setCellValue(fuel.getCo2PerUnit());
                row.createCell(3).setCellValue(fuel.getUnit());
                row.createCell(4).setCellValue(fuel.getRemarks());
            }
            try (FileOutputStream fileOut = new FileOutputStream(FUEL_EXCEL_FILE)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/form")
    public String showForm(Model model) {
        loadFuelsFromExcel();
        model.addAttribute("fuel", new Fuel());
        model.addAttribute("fuelList", new ArrayList<>(fuelSet));
        return "fuelForm";
    }

    @PostMapping("/add")
    public String addFuel(@ModelAttribute Fuel fuel, RedirectAttributes redirectAttributes) {
        if (fuelSet.stream().anyMatch(f -> f.getFuelId().equalsIgnoreCase(fuel.getFuelId()))) {
            redirectAttributes.addFlashAttribute("error", "Fuel ID already exists!");
            return "redirect:/fuel/form";
        }
        fuelSet.add(fuel);
        saveFuelsToExcel();
        redirectAttributes.addFlashAttribute("message", "Fuel added successfully!");
        return "redirect:/fuel/form";
    }

    @GetMapping("/{fuelId}/edit")
    public String showEditForm(@PathVariable String fuelId, Model model, RedirectAttributes redirectAttributes) {
        loadFuelsFromExcel();
        Fuel fuelToEdit = fuelSet.stream().filter(f -> f.getFuelId().equalsIgnoreCase(fuelId)).findFirst().orElse(null);

        if (fuelToEdit == null) {
            redirectAttributes.addFlashAttribute("error", "Fuel not found!");
            return "redirect:/fuel/form";
        }

        model.addAttribute("fuel", fuelToEdit);
        model.addAttribute("fuelList", new ArrayList<>(fuelSet));
        model.addAttribute("isEdit", true);
        model.addAttribute("fuelIdForEdit", fuelId);
        return "fuelForm";
    }

    @PostMapping("/{fuelId}/update")
    public String updateFuel(@PathVariable String fuelId, @ModelAttribute Fuel updatedFuel,
            RedirectAttributes redirectAttributes) {
        Optional<Fuel> existingFuelOptional = fuelSet.stream().filter(f -> f.getFuelId().equalsIgnoreCase(fuelId))
                .findFirst();

        if (existingFuelOptional.isPresent()) {
            Fuel existingFuel = existingFuelOptional.get();
            existingFuel.setFuelName(updatedFuel.getFuelName());
            existingFuel.setCo2PerUnit(updatedFuel.getCo2PerUnit());
            existingFuel.setUnit(updatedFuel.getUnit());
            existingFuel.setRemarks(updatedFuel.getRemarks());
            saveFuelsToExcel();
            redirectAttributes.addFlashAttribute("message", "Fuel updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Fuel not found for update!");
        }

        return "redirect:/fuel/form";
    }

    @PostMapping("/{fuelId}/delete")
    public String deleteFuel(@PathVariable String fuelId, RedirectAttributes redirectAttributes) {
        boolean removed = fuelSet.removeIf(f -> f.getFuelId().equalsIgnoreCase(fuelId));
        if (removed) {
            saveFuelsToExcel();
            redirectAttributes.addFlashAttribute("message", "Fuel deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Fuel not found!");
        }
        return "redirect:/fuel/form";
    }

    @PostMapping("/{fuelId}/showRemark")
    public String showFuelRemark(@PathVariable String fuelId, RedirectAttributes redirectAttributes) {
        Fuel foundFuel = fuelSet.stream().filter(f -> f.getFuelId().equalsIgnoreCase(fuelId)).findFirst().orElse(null);
        if (foundFuel != null) {
            String remark = foundFuel.getRemarks();
            redirectAttributes.addFlashAttribute("showRemark", remark);
            redirectAttributes.addFlashAttribute("fuelIdForRemark", fuelId);
        } else {
            redirectAttributes.addFlashAttribute("error", "Fuel not found!");
        }
        return "redirect:/fuel/form";
    }
}
