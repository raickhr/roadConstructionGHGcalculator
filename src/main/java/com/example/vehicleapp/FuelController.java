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
    private final Set<Fuel> fuelSet = new HashSet<>();  // Ensures uniqueness by fuelType
    private static final String FUEL_EXCEL_FILE = "fuel.xlsx";

    // Load fuel types from fuel.xlsx
    private void loadFuelTypesFromExcel() {
        if (!fuelSet.isEmpty()) return; // Avoid redundant loading
        try (FileInputStream fileIn = new FileInputStream(FUEL_EXCEL_FILE);
             Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                String fuelType = row.getCell(0).getStringCellValue().trim().toLowerCase();
                double co2PerLiter = row.getCell(1).getNumericCellValue();

                Fuel fuel = new Fuel();
                fuel.setFuelType(fuelType);
                fuel.setCo2PerLiter(co2PerLiter);

                fuelSet.add(fuel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save fuel types to Excel ensuring uniqueness
    private void saveFuelTypesToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fuel Types");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Fuel Type", "CO2 per Liter"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            int rowNum = 1;
            for (Fuel fuel : fuelSet) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(fuel.getFuelType());
                row.createCell(1).setCellValue(fuel.getCo2PerLiter());
            }

            try (FileOutputStream fileOut = new FileOutputStream(FUEL_EXCEL_FILE)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/form")
    public String showFuelForm(Model model) {
        loadFuelTypesFromExcel();
        model.addAttribute("fuel", new Fuel());
        model.addAttribute("fuelList", new ArrayList<>(fuelSet));
        return "fuelForm";
    }

    @PostMapping("/add")
    public String addFuel(@ModelAttribute Fuel fuel, RedirectAttributes redirectAttributes) {
        if (fuelSet.contains(fuel)) {
            redirectAttributes.addFlashAttribute("error", "Fuel type already exists!");
            return "redirect:/fuel/form";
        }
        fuelSet.add(fuel);
        redirectAttributes.addFlashAttribute("message", "Fuel type added successfully!");
        return "redirect:/fuel/form";
    }

    @PostMapping("/done")
    public String saveToExcel(RedirectAttributes redirectAttributes) {
        saveFuelTypesToExcel();
        redirectAttributes.addFlashAttribute("message", "Fuel data saved to Excel!");
        return "redirect:/fuel/form";
    }
}
