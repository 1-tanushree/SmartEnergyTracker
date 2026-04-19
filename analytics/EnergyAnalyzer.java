package analytics;

import models.Device;
import models.UsageRecord;
import util.Constants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides energy analysis operations over a collection of {@link UsageRecord}s.
 *
 * <p>Every public method uses the Java Streams API with lambda expressions so
 * that no manual loops are needed. This makes the code concise, readable, and
 * easy to parallelise if the data set ever grows large.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Filter records that exceed an energy threshold (Stream + Lambda)</li>
 *   <li>Group records by device category (Collectors.groupingBy)</li>
 *   <li>Calculate average consumption per device (Collectors.averagingDouble)</li>
 *   <li>Identify the top energy-wasting device (max with Comparator)</li>
 *   <li>Predict a monthly electricity bill (Bonus)</li>
 *   <li>Return an ANSI colour label for a kWh value (Bonus)</li>
 * </ul>
 *
 * <p>This class is stateless — every method is given the full list of records
 * as a parameter so it can be freely reused without needing to hold mutable
 * state.
 */
public class EnergyAnalyzer {

    // -----------------------------------------------------------------------
    // 1. Filter high-usage records
    // -----------------------------------------------------------------------

    /**
     * Returns all usage records whose daily kWh consumption exceeds the
     * supplied threshold.
     *
     * <p>Implementation uses a Stream filter with a lambda that delegates to
     * {@link UsageRecord#isHighUsage(double)} — the business rule lives in
     * the model, the pipeline lives here.
     *
     * <p>Example:
     * <pre>
     *   List&lt;UsageRecord&gt; highOnes =
     *       analyzer.getHighUsageRecords(records, 2.0);
     * </pre>
     *
     * @param records      the full list of usage records to search through
     * @param thresholdKWh records whose kWh value is strictly above this
     *                     threshold are included in the result
     * @return             a new list containing only the high-usage records;
     *                     never {@code null}, may be empty
     */
    public List<UsageRecord> getHighUsageRecords(List<UsageRecord> records,
                                                 double thresholdKWh) {
        return records.stream()
                // Keep only records that exceed the threshold
                .filter(record -> record.isHighUsage(thresholdKWh))
                // Collect surviving records into a new List
                .collect(Collectors.toList());
    }

    /**
     * Convenience overload that uses the application-wide default threshold
     * ({@link Constants#HIGH_USAGE_THRESHOLD_KWH}).
     *
     * @param records the full list of usage records
     * @return        high-usage records filtered with the default threshold
     */
    public List<UsageRecord> getHighUsageRecords(List<UsageRecord> records) {
        return getHighUsageRecords(records, Constants.HIGH_USAGE_THRESHOLD_KWH);
    }

    // -----------------------------------------------------------------------
    // 2. Group records by device category
    // -----------------------------------------------------------------------

    /**
     * Groups all usage records by their device's category (AC, LIGHT,
     * APPLIANCE) and returns a map from category name to the list of records
     * in that category.
     *
     * <p>Uses {@link Collectors#groupingBy} with a lambda key extractor.
     * The resulting map preserves all records — nothing is filtered out —
     * making it ideal for per-category reporting.
     *
     * <p>Example result:
     * <pre>
     *   {
     *     "Air Conditioner" -&gt; [rec1, rec4, rec7],
     *     "Light"           -&gt; [rec2, rec5],
     *     "Appliance"       -&gt; [rec3, rec6]
     *   }
     * </pre>
     *
     * @param records the full list of usage records
     * @return        a map where each key is a category display name and each
     *                value is the list of records belonging to that category
     */
    public Map<String, List<UsageRecord>> groupByCategory(
            List<UsageRecord> records) {

        return records.stream()
                // Key extractor: use the human-readable display name from the enum
                .collect(Collectors.groupingBy(
                        record -> record.getDevice().getCategory().getDisplayName()
                ));
    }

    // -----------------------------------------------------------------------
    // 3. Average consumption per device
    // -----------------------------------------------------------------------

    /**
     * Calculates the average daily kWh consumption for every distinct device
     * found in the record list.
     *
     * <p>Uses a two-level collector:
     * <ol>
     *   <li>{@link Collectors#groupingBy} partitions records by device name.</li>
     *   <li>{@link Collectors#averagingDouble} reduces each partition to its
     *       arithmetic mean kWh.</li>
     * </ol>
     *
     * <p>Devices with a single record produce an average equal to that record's
     * kWh — this is mathematically correct and no special case is needed.
     *
     * @param records the full list of usage records
     * @return        a map of device name → average daily kWh; never {@code null}
     */
    public Map<String, Double> averageConsumptionPerDevice(
            List<UsageRecord> records) {

        return records.stream()
                .collect(Collectors.groupingBy(
                        // Group key: device name (unique identifier for the device)
                        record -> record.getDevice().getName(),
                        // Downstream collector: average the kWh values in each group
                        Collectors.averagingDouble(UsageRecord::calculateKWh)
                ));
    }

    // -----------------------------------------------------------------------
    // 4. Find the top energy-wasting device
    // -----------------------------------------------------------------------

    /**
     * Finds the single usage record that represents the highest energy
     * consumption across the entire list.
     *
     * <p>Uses {@link java.util.stream.Stream#max} with a custom
     * {@link Comparator} that compares records by their calculated kWh.
     * If two records have the same kWh the one appearing later in the stream
     * wins (stable ordering is not guaranteed by the Streams API).
     *
     * <p>Returns an {@link Optional} because the input list might be empty.
     * Callers should always check {@code isPresent()} before calling
     * {@code get()}.
     *
     * @param records the full list of usage records
     * @return        an Optional wrapping the top-wasting UsageRecord,
     *                or {@link Optional#empty()} if the list is empty
     */
    public Optional<UsageRecord> getTopEnergyWaster(List<UsageRecord> records) {
        return records.stream()
                // Custom Comparator: compare two records by their kWh values
                .max(Comparator.comparingDouble(UsageRecord::calculateKWh));
    }

    // -----------------------------------------------------------------------
    // 5. Total kWh consumed (helper used by other classes)
    // -----------------------------------------------------------------------

    /**
     * Calculates the total kWh consumed across all records in the list.
     *
     * <p>Uses {@link java.util.stream.DoubleStream#sum} via
     * {@code mapToDouble}, which is more numerically stable than reducing
     * with {@code Double::sum} on a boxed stream.
     *
     * @param records the full list of usage records
     * @return        total kWh as a {@code double}; 0.0 if the list is empty
     */
    public double totalKWh(List<UsageRecord> records) {
        return records.stream()
                .mapToDouble(UsageRecord::calculateKWh)
                .sum();
    }

    // -----------------------------------------------------------------------
    // 6. Bonus: Monthly bill prediction
    // -----------------------------------------------------------------------

    /**
     * Predicts the monthly electricity bill in Indian Rupees (₹) based on
     * the average daily consumption observed in the supplied records.
     *
     * <p>Formula:
     * <pre>
     *   daily_kWh   = totalKWh(records) / number_of_unique_days
     *   monthly_kWh = daily_kWh × {@link Constants#DAYS_IN_MONTH}
     *   bill (₹)    = monthly_kWh × {@link Constants#TARIFF_RATE_PER_KWH}
     * </pre>
     *
     * <p>The number of unique days is derived by streaming the records,
     * extracting each record's date, and collecting into a {@link Set} —
     * this automatically deduplicates multiple records on the same date.
     *
     * @param records the full list of usage records; must not be {@code null}
     * @return        predicted monthly bill in ₹, or 0.0 if the list is empty
     */
    public double predictMonthlyBill(List<UsageRecord> records) {
        if (records == null || records.isEmpty()) {
            return 0.0;
        }

        // Count the number of distinct calendar days covered by these records
        long distinctDays = records.stream()
                .map(UsageRecord::getDate)          // extract the date of each record
                .collect(Collectors.toSet())        // deduplicate dates via Set semantics
                .size();

        // Guard against a degenerate case (all records on the same day gives 1 day)
        if (distinctDays == 0) {
            distinctDays = 1;
        }

        double totalKWh     = totalKWh(records);
        double dailyAvgKWh  = totalKWh / distinctDays;
        double monthlyKWh   = dailyAvgKWh * Constants.DAYS_IN_MONTH;

        return monthlyKWh * Constants.TARIFF_RATE_PER_KWH;
    }

    // -----------------------------------------------------------------------
    // 7. Bonus: ANSI colour level for a kWh reading
    // -----------------------------------------------------------------------

    /**
     * Returns an ANSI colour-coded string that wraps the supplied kWh value
     * with a colour based on how high the consumption is.
     *
     * <ul>
     *   <li><b>Green</b>  — kWh &lt; {@link Constants#COLOUR_LOW_KWH}  (low,  safe)</li>
     *   <li><b>Yellow</b> — kWh in [{@link Constants#COLOUR_LOW_KWH}, {@link Constants#COLOUR_HIGH_KWH}) (medium, caution)</li>
     *   <li><b>Red</b>    — kWh ≥ {@link Constants#COLOUR_HIGH_KWH}   (high, danger)</li>
     * </ul>
     *
     * <p>The ANSI reset code is always appended at the end so the calling
     * code's terminal colour is not affected.
     *
     * <p>Example output printed to a terminal:
     * <pre>
     *   System.out.println(analyzer.colourCodedKWh(0.5));  // green  "0.500 kWh"
     *   System.out.println(analyzer.colourCodedKWh(1.8));  // yellow "1.800 kWh"
     *   System.out.println(analyzer.colourCodedKWh(4.2));  // red    "4.200 kWh"
     * </pre>
     *
     * @param kWh the energy value to display
     * @return    a formatted string with ANSI colour codes, ready to print
     */
    public String colourCodedKWh(double kWh) {
        String colour;

        if (kWh < Constants.COLOUR_LOW_KWH) {
            colour = Constants.ANSI_GREEN;
        } else if (kWh < Constants.COLOUR_HIGH_KWH) {
            colour = Constants.ANSI_YELLOW;
        } else {
            colour = Constants.ANSI_RED;
        }

        // Format: colour + numeric value (3 decimal places) + unit + reset
        return String.format("%s%.3f kWh%s", colour, kWh, Constants.ANSI_RESET);
    }

    // -----------------------------------------------------------------------
    // 8. Bonus: Summary report printed to stdout
    // -----------------------------------------------------------------------

    /**
     * Prints a formatted energy summary report to standard output.
     *
     * <p>The report includes:
     * <ol>
     *   <li>Total and average kWh across all records</li>
     *   <li>Per-category breakdown (using {@link #groupByCategory})</li>
     *   <li>Average consumption per device (using {@link #averageConsumptionPerDevice})</li>
     *   <li>The single top energy waster</li>
     *   <li>Predicted monthly bill</li>
     * </ol>
     *
     * <p>Uses ANSI colour codes from {@link Constants} so the output is
     * colour-coded when run in a terminal that supports ANSI escape sequences
     * (Linux, macOS, Windows Terminal).
     *
     * @param records the full list of usage records to report on
     */
    public void printSummaryReport(List<UsageRecord> records) {
        if (records == null || records.isEmpty()) {
            System.out.println(Constants.ANSI_YELLOW
                    + "No usage records available to analyse."
                    + Constants.ANSI_RESET);
            return;
        }

        System.out.println(Constants.ANSI_BOLD + Constants.ANSI_CYAN
                + "===== Smart Energy Tracker — Analysis Report ====="
                + Constants.ANSI_RESET);

        // --- Totals ---
        double total = totalKWh(records);
        double avg   = total / records.size();

        System.out.printf("Total consumption : %s%n", colourCodedKWh(total));
        System.out.printf("Average per record: %s%n", colourCodedKWh(avg));
        System.out.println();

        // --- Per-category breakdown ---
        System.out.println(Constants.ANSI_CYAN + "-- By Category --" + Constants.ANSI_RESET);
        groupByCategory(records).forEach((category, recs) -> {
            double catTotal = recs.stream()
                    .mapToDouble(UsageRecord::calculateKWh)
                    .sum();
            System.out.printf("  %-20s : %s  (%d record(s))%n",
                    category, colourCodedKWh(catTotal), recs.size());
        });
        System.out.println();

        // --- Per-device average ---
        System.out.println(Constants.ANSI_CYAN + "-- Device Averages --" + Constants.ANSI_RESET);
        averageConsumptionPerDevice(records).forEach((deviceName, avgKWh) ->
                System.out.printf("  %-25s avg: %s%n",
                        deviceName, colourCodedKWh(avgKWh)));
        System.out.println();

        // --- Top waster ---
        System.out.println(Constants.ANSI_CYAN + "-- Top Energy Waster --" + Constants.ANSI_RESET);
        getTopEnergyWaster(records).ifPresent(record ->
                System.out.printf("  %s → %s%n",
                        record.getDevice().getName(),
                        colourCodedKWh(record.calculateKWh())));
        System.out.println();

        // --- Monthly bill prediction ---
        System.out.println(Constants.ANSI_CYAN + "-- Monthly Bill Prediction --" + Constants.ANSI_RESET);
        System.out.printf("  Estimated bill: %s₹ %.2f%s%n",
                Constants.ANSI_BOLD,
                predictMonthlyBill(records),
                Constants.ANSI_RESET);

        System.out.println(Constants.ANSI_CYAN
                + "=================================================="
                + Constants.ANSI_RESET);
    }
}