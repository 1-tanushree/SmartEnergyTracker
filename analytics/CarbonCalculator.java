package analytics;

import models.Device;
import models.UsageRecord;
import util.Constants;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calculates the carbon (CO₂) footprint of household electricity consumption.
 *
 * <p>Carbon footprint is computed by multiplying the kWh consumed by the
 * grid's CO₂ emission factor ({@link Constants#CO2_FACTOR_KG_PER_KWH}).
 * The Indian national grid emits approximately 0.82 kg of CO₂ for every
 * kWh of electricity generated and transmitted.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Calculate the total CO₂ footprint for a list of records</li>
 *   <li>Calculate the per-device CO₂ footprint using a Lambda map</li>
 *   <li>Identify the single biggest CO₂ contributor</li>
 *   <li>Estimate CO₂ saved if a device were run fewer hours (Bonus)</li>
 *   <li>Provide a friendly "carbon tier" label for quick comparisons (Bonus)</li>
 * </ul>
 *
 * <p>Like {@link EnergyAnalyzer}, this class is stateless — all inputs are
 * passed as method parameters.
 */
public class CarbonCalculator {

    // -----------------------------------------------------------------------
    // 1. Total carbon footprint
    // -----------------------------------------------------------------------

    /**
     * Calculates the total CO₂ footprint (in kilograms) for all records in
     * the supplied list.
     *
     * <p>Formula:
     * <pre>
     *   total_CO2 = Σ (record.calculateKWh() × CO2_FACTOR_KG_PER_KWH)
     * </pre>
     *
     * <p>Uses {@code mapToDouble} + {@code sum} for efficient, numerically
     * stable aggregation without boxing overhead.
     *
     * @param records the full list of usage records; must not be {@code null}
     * @return        total CO₂ in kilograms; 0.0 if the list is empty
     */
    public double calculateTotalCarbonFootprint(List<UsageRecord> records) {
        return records.stream()
                // Convert each record's kWh to kg CO₂ using a lambda
                .mapToDouble(record -> record.calculateKWh() * Constants.CO2_FACTOR_KG_PER_KWH)
                .sum();
    }

    // -----------------------------------------------------------------------
    // 2. Per-device carbon footprint map
    // -----------------------------------------------------------------------

    /**
     * Computes the cumulative CO₂ footprint for each device and returns a
     * map of device name → total CO₂ (kg).
     *
     * <p>The pipeline:
     * <ol>
     *   <li>Groups records by device name using {@link Collectors#groupingBy}.</li>
     *   <li>Reduces each group to the sum of (kWh × CO2 factor) using
     *       {@link Collectors#summingDouble} with a Lambda.</li>
     * </ol>
     *
     * <p>This is the primary output used by the CLI report to show which
     * devices are the biggest carbon contributors.
     *
     * @param records the full list of usage records
     * @return        a map of device name → cumulative CO₂ in kg;
     *                never {@code null}, may be empty
     */
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

    // -----------------------------------------------------------------------
    // 3. Highest CO₂ contributing device
    // -----------------------------------------------------------------------

    /**
     * Returns the name of the device that produced the most CO₂ emissions
     * across all records.
     *
     * <p>Internally this calls {@link #carbonFootprintPerDevice} to get the
     * per-device map, then uses {@link Map.Entry#comparingByValue} — a
     * Comparator factory — to find the entry with the maximum value.
     *
     * <p>Returns {@code "N/A"} when the record list is empty so callers never
     * have to handle a {@code null} or an empty {@link java.util.Optional}.
     *
     * @param records the full list of usage records
     * @return        the device name with the highest CO₂ total, or "N/A"
     */
    public String biggestCarbonContributor(List<UsageRecord> records) {
        Map<String, Double> footprintMap = carbonFootprintPerDevice(records);

        // Use a Comparator on the map entries to find the max-value entry
        return footprintMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())  // Comparator.comparingByValue()
                .map(Map.Entry::getKey)             // extract just the device name
                .orElse("N/A");                     // safe fallback for empty input
    }

    // -----------------------------------------------------------------------
    // 4. Bonus: Carbon saving estimation
    // -----------------------------------------------------------------------

    /**
     * Estimates how many kilograms of CO₂ would be saved per day if a given
     * device were used for fewer hours each day.
     *
     * <p>This is useful for the "what-if" feature in the CLI:
     * "If you ran your AC for 4 hours instead of 8, you would save X kg CO₂."
     *
     * <p>Formula:
     * <pre>
     *   saved_kWh = device.wattage × (currentHours - targetHours) / 1000
     *   saved_CO2 = saved_kWh × CO2_FACTOR_KG_PER_KWH
     * </pre>
     *
     * @param device       the device to evaluate
     * @param currentHours the number of hours the device is currently used
     *                     per day (must be ≥ 0)
     * @param targetHours  the desired reduced usage in hours per day
     *                     (must be ≥ 0 and ≤ currentHours)
     * @return             CO₂ saved in kg per day; 0.0 if targetHours ≥ currentHours
     */
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

    // -----------------------------------------------------------------------
    // 5. Bonus: Carbon tier label
    // -----------------------------------------------------------------------

    /**
     * Returns a human-readable tier label for a given CO₂ value in kg.
     *
     * <p>Tiers (approximate daily household benchmarks):
     * <ul>
     *   <li><b>Low</b>    — below 1.0 kg CO₂/day</li>
     *   <li><b>Medium</b> — 1.0 kg to below 5.0 kg CO₂/day</li>
     *   <li><b>High</b>   — 5.0 kg to below 10.0 kg CO₂/day</li>
     *   <li><b>Critical</b> — 10.0 kg or above</li>
     * </ul>
     *
     * <p>The label is colour-coded using ANSI codes when printed to a terminal
     * that supports them.
     *
     * @param co2Kg a CO₂ quantity in kilograms
     * @return      a colour-coded string label such as
     *              {@code "\u001B[32mLow\u001B[0m"}
     */
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

    // -----------------------------------------------------------------------
    // 6. Bonus: Full carbon report printed to stdout
    // -----------------------------------------------------------------------

    /**
     * Prints a formatted carbon footprint report to standard output.
     *
     * <p>The report includes:
     * <ol>
     *   <li>Total CO₂ footprint across all records</li>
     *   <li>CO₂ tier classification</li>
     *   <li>Per-device CO₂ breakdown (sorted descending by contribution)</li>
     *   <li>The biggest single contributor</li>
     * </ol>
     *
     * <p>The per-device list is sorted so the biggest polluter appears first.
     * This uses {@link Map.Entry#comparingByValue} reversed, applied via
     * {@code sorted()} in the stream.
     *
     * @param records the full list of usage records to report on
     */
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