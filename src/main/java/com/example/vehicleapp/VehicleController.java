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
@RequestMapping("/vehicle")
public class VehicleController {
    private final Set<Vehicle> vehicleSet = new HashSet<>();  // Ensures uniqueness by type
    private final List<String> fuelTypeOptions = new ArrayList<>(); // Stores fuel types from Excel
    private static final String VEHICLE_EXCEL_FILE = "vehicles.xlsx";
    private static final String FUEL_EXCEL_FILE = "fuel.xlsx";

    // Load vehicles from Excel
    private void loadVehiclesFromExcel() {
        if (!vehicleSet.isEmpty()) return; // Avoid redundant loading
        try (FileInputStream fileIn = new FileInputStream(VEHICLE_EXCEL_FILE);
             Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                String type = row.getCell(0).getStringCellValue().trim().toLowerCase();
                String fuelType = row.getCell(1).getStringCellValue().trim().toLowerCase();
                double mpg = row.getCell(2).getNumericCellValue();

                Vehicle vehicle = new Vehicle();
                vehicle.setType(type);
                vehicle.setFuelType(fuelType);
                vehicle.setMpg(mpg);

                vehicleSet.add(vehicle);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load fuel types from fuel.xlsx (Refreshes every time)
    private void loadFuelTypesFromExcel() {
        fuelTypeOptions.clear(); // Clear existing fuel types to refresh
        try (FileInputStream fileIn = new FileInputStream(FUEL_EXCEL_FILE);
             Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                String fuelType = row.getCell(0).getStringCellValue().trim().toLowerCase();
                if (!fuelTypeOptions.contains(fuelType)) {
                    fuelTypeOptions.add(fuelType);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save vehicles to Excel ensuring uniqueness by type
    private void saveVehiclesToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Vehicles");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Vehicle Type", "Fuel Type", "MPG"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            int rowNum = 1;
            for (Vehicle vehicle : vehicleSet) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(vehicle.getType());
                row.createCell(1).setCellValue(vehicle.getFuelType());
                row.createCell(2).setCellValue(vehicle.getMpg());
            }

            try (FileOutputStream fileOut = new FileOutputStream(VEHICLE_EXCEL_FILE)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/form")
    public String showForm(Model model) {
        loadVehiclesFromExcel();
        loadFuelTypesFromExcel(); // Refresh fuel type options
        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("vehicleList", new ArrayList<>(vehicleSet));
        model.addAttribute("fuelTypeOptions", fuelTypeOptions); // Pass refreshed fuel types
        return "vehicleForm";
    }

    @PostMapping("/add")
    public String addVehicle(@ModelAttribute Vehicle vehicle, RedirectAttributes redirectAttributes) {
        if (vehicleSet.contains(vehicle)) {
            redirectAttributes.addFlashAttribute("error", "Vehicle already present!");
            return "redirect:/vehicle/form";
        }
        vehicleSet.add(vehicle);
        redirectAttributes.addFlashAttribute("message", "Vehicle added successfully!");
        return "redirect:/vehicle/form";
    }

    @PostMapping("/done")
    public String saveToExcel(RedirectAttributes redirectAttributes) {
        saveVehiclesToExcel();
        redirectAttributes.addFlashAttribute("message", "Data saved to Excel!");
        return "redirect:/vehicle/form";
    }

    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }
}
