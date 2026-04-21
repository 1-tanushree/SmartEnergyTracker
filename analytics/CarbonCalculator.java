package analytics;

import models.Device;
import models.UsageRecord;
import util.Constants;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CarbonCalculator {

   
    public double calculateTotalCarbonFootprint(List<UsageRecord> records) {
        return records.stream()
                // Convert each record's kWh to kg CO₂ using a lambda
                .mapToDouble(record -> record.calculateKWh() * Constants.CO2_FACTOR_KG_PER_KWH)
                .sum();
    }

   
    public Map<String, Double> carbonFootprintPerDevice(List<UsageRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        // Group by device name
                        record -> record.getDevice().getName(),
                        // Sum the CO₂ for each record in the group
                        Collectors.summingDouble(
                                record -> record.calculateKWh() * Constants.CO2_FACTOR_KG_PER_KWH
                        )
                ));
    }

   
    public String biggestCarbonContributor(List<UsageRecord> records) {
        Map<String, Double> footprintMap = carbonFootprintPerDevice(records);

        // Use a Comparator on the map entries to find the max-value entry
        return footprintMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())  // Comparator.comparingByValue()
                .map(Map.Entry::getKey)             // extract just the device name
                .orElse("N/A");                     // safe fallback for empty input
    }

   
    public double estimateCarbonSaving(Device device,
                                       double currentHours,
                                       double targetHours) {
        // No saving if the target is not actually a reduction
        if (targetHours >= currentHours) {
            return 0.0;
        }

        double hoursSaved = currentHours - targetHours;
        double kWhSaved   = (device.getWattage() * hoursSaved) / 1000.0;
        return kWhSaved * Constants.CO2_FACTOR_KG_PER_KWH;
    }

    public String getCarbonTierLabel(double co2Kg) {
        if (co2Kg < 1.0) {
            return Constants.ANSI_GREEN + "Low" + Constants.ANSI_RESET;
        } else if (co2Kg < 5.0) {
            return Constants.ANSI_YELLOW + "Medium" + Constants.ANSI_RESET;
        } else if (co2Kg < 10.0) {
            return Constants.ANSI_RED + "High" + Constants.ANSI_RESET;
        } else {
            return Constants.ANSI_BOLD + Constants.ANSI_RED
                    + "Critical" + Constants.ANSI_RESET;
        }
    }

  
    public void printCarbonReport(List<UsageRecord> records) {
        if (records == null || records.isEmpty()) {
            System.out.println(Constants.ANSI_YELLOW
                    + "No usage records available for carbon analysis."
                    + Constants.ANSI_RESET);
            return;
        }

        System.out.println(Constants.ANSI_BOLD + Constants.ANSI_CYAN
                + "===== Carbon Footprint Report ====="
                + Constants.ANSI_RESET);

        // Total and tier
        double totalCO2 = calculateTotalCarbonFootprint(records);
        System.out.printf("Total CO₂ footprint : %.3f kg%n", totalCO2);
        System.out.printf("Carbon tier         : %s%n", getCarbonTierLabel(totalCO2));
        System.out.println();

        // Per-device breakdown — sorted from highest to lowest contribution
        System.out.println(Constants.ANSI_CYAN
                + "-- Per-Device Breakdown (highest first) --"
                + Constants.ANSI_RESET);

        carbonFootprintPerDevice(records).entrySet().stream()
                // Sort descending by CO₂ value — biggest contributor first
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry ->
                        System.out.printf("  %-25s : %.3f kg CO₂  [%s]%n",
                                entry.getKey(),
                                entry.getValue(),
                                getCarbonTierLabel(entry.getValue())
                        )
                );

        System.out.println();
        System.out.printf("Biggest contributor : %s%s%s%n",
                Constants.ANSI_RED,
                biggestCarbonContributor(records),
                Constants.ANSI_RESET);

        System.out.println(Constants.ANSI_CYAN
                + "==================================="
                + Constants.ANSI_RESET);
    }
}
