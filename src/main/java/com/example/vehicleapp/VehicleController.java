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
    private final Set<Vehicle> vehicleSet = new HashSet<>();
    private final List<Fuel> fuelList = new ArrayList<>(); // List to store Fuels
    private static final String VEHICLE_EXCEL_FILE = "vehicles.xlsx";
    private static final String FUEL_EXCEL_FILE = "fuel.xlsx";

    // Load vehicles from Excel
    private void loadVehiclesFromExcel() {
        if (!vehicleSet.isEmpty())
            return; // Avoid redundant loading
        try (FileInputStream fileIn = new FileInputStream(VEHICLE_EXCEL_FILE);
                Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header
                String vehicleId = row.getCell(0).getStringCellValue().trim().toLowerCase();
                String vehicleName = row.getCell(1).getStringCellValue().trim();
                String fuelType = row.getCell(2).getStringCellValue().trim().toLowerCase();
                String mileageUnit = row.getCell(3).getStringCellValue().trim(); // Read as string
                double mileageValue = row.getCell(4).getNumericCellValue();
                String remarks = row.getCell(5) != null ? row.getCell(5).getStringCellValue().trim() : ""; // Handle
                                                                                                           // remarks

                Vehicle vehicle = new Vehicle();
                vehicle.setVehicleId(vehicleId);
                vehicle.setVehicleName(vehicleName);
                vehicle.setFuelType(fuelType);
                vehicle.setMileageUnit(mileageUnit); // Set mileage unit
                vehicle.setMileageValue(mileageValue);
                vehicle.setRemarks(remarks); // Set remarks

                vehicleSet.add(vehicle);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load fuel types from fuel.xlsx (Refreshes every time)
    private void loadFuelTypesFromExcel() {
        fuelList.clear(); // Clear existing fuel types to refresh
        try (FileInputStream fileIn = new FileInputStream(FUEL_EXCEL_FILE);
                Workbook workbook = new XSSFWorkbook(fileIn)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Skip header row
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

    // Save vehicles to Excel ensuring uniqueness by vehicleId
    private void saveVehiclesToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Vehicles");
            Row headerRow = sheet.createRow(0);
            String[] columns = { "Vehicle ID", "Vehicle Name", "Fuel Type", "Mileage Unit", "Mileage Value",
                    "Remarks" };
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            int rowNum = 1;
            for (Vehicle vehicle : vehicleSet) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(vehicle.getVehicleId());
                row.createCell(1).setCellValue(vehicle.getVehicleName()); // Save vehicle name
                row.createCell(2).setCellValue(vehicle.getFuelType());
                row.createCell(3).setCellValue(vehicle.getMileageUnit()); // Save mileage unit as String
                row.createCell(4).setCellValue(vehicle.getMileageValue());
                row.createCell(5).setCellValue(vehicle.getRemarks()); // Save remarks
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
        model.addAttribute("fuelList", fuelList); // Pass refreshed fuel types
        return "vehicleForm";
    }

    @PostMapping("/add")
    public String addVehicle(@ModelAttribute Vehicle vehicle, RedirectAttributes redirectAttributes) {
        // Save fuelType as fuelName:fuelId
        String fuelType = vehicle.getFuelType();
        Optional<Fuel> matchedFuel = fuelList.stream()
                .filter(f -> (fuelType.equalsIgnoreCase(f.getFuelName() + ":" + f.getFuelId()))).findFirst();

        if (matchedFuel.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Fuel type is invalid or not available!");
            return "redirect:/vehicle/form";
        }

        // Check for duplicates based on vehicleId
        boolean vehicleExists = vehicleSet.stream().anyMatch(v -> v.getVehicleId().equals(vehicle.getVehicleId()));
        if (vehicleExists) {
            redirectAttributes.addFlashAttribute("error", "Vehicle already present!");
            return "redirect:/vehicle/form";
        }

        vehicleSet.add(vehicle);
        redirectAttributes.addFlashAttribute("message", "Vehicle added successfully!");
        saveVehiclesToExcel();
        return "redirect:/vehicle/form";
    }

    @GetMapping("/{vehicleId}/edit")
    public String showEditForm(@PathVariable String vehicleId, Model model, RedirectAttributes redirectAttributes) {
        loadVehiclesFromExcel();
        Vehicle vehicleToEdit = vehicleSet.stream()
                .filter(v -> v.getVehicleId().equalsIgnoreCase(vehicleId))
                .findFirst()
                .orElse(null);

        if (vehicleToEdit == null) {
            redirectAttributes.addFlashAttribute("error", "Vehicle not found!");
            return "redirect:/vehicle/form";
        }

        model.addAttribute("vehicle", vehicleToEdit);
        model.addAttribute("vehicleList", new ArrayList<>(vehicleSet));
        model.addAttribute("isEdit", true);
        model.addAttribute("vehicleIdForEdit", vehicleId);
        model.addAttribute("fuelList", fuelList); // Pass refreshed fuel types
        return "vehicleForm";
    }

    @PostMapping("/{vehicleId}/update")
    public String updateVehicle(@PathVariable String vehicleId, @ModelAttribute Vehicle updatedVehicle,
            RedirectAttributes redirectAttributes) {
        Optional<Vehicle> existingVehicleOptional = vehicleSet.stream()
                .filter(v -> v.getVehicleId().equalsIgnoreCase(vehicleId))
                .findFirst();

        if (existingVehicleOptional.isPresent()) {
            Vehicle existingVehicle = existingVehicleOptional.get();
            existingVehicle.setVehicleName(updatedVehicle.getVehicleName()); // Update vehicle name
            existingVehicle.setFuelType(updatedVehicle.getFuelType());
            existingVehicle.setMileageUnit(updatedVehicle.getMileageUnit()); // Update mileage unit
            existingVehicle.setMileageValue(updatedVehicle.getMileageValue());
            existingVehicle.setRemarks(updatedVehicle.getRemarks()); // Update remarks
            saveVehiclesToExcel();
            redirectAttributes.addFlashAttribute("message", "Vehicle updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Vehicle not found for update!");
        }

        return "redirect:/vehicle/form";
    }

    @PostMapping("/{vehicleId}/delete")
    public String deleteVehicle(@PathVariable String vehicleId, RedirectAttributes redirectAttributes) {
        boolean removed = vehicleSet.removeIf(v -> v.getVehicleId().equalsIgnoreCase(vehicleId));
        if (removed) {
            saveVehiclesToExcel();
            redirectAttributes.addFlashAttribute("message", "Vehicle deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Vehicle not found!");
        }
        return "redirect:/vehicle/form";
    }

    // Show remarks for a specific vehicle
    @PostMapping("/{vehicleId}/showRemark")
    public String showVehicleRemark(@PathVariable String vehicleId, RedirectAttributes redirectAttributes) {
        Vehicle foundVehicle = vehicleSet.stream().filter(v -> v.getVehicleId().equalsIgnoreCase(vehicleId)).findFirst()
                .orElse(null);
        if (foundVehicle != null) {
            String remark = foundVehicle.getRemarks();
            redirectAttributes.addFlashAttribute("showRemark", remark);
            redirectAttributes.addFlashAttribute("vehicleIdForRemark", vehicleId);
        } else {
            redirectAttributes.addFlashAttribute("error", "Vehicle not found!");
        }
        return "redirect:/vehicle/form";
    }

    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }
}
