package util;

/**
 * Utility class holding global application constants.
 *
 * This class is a pure constants holder — it should never be instantiated.
 * All fields are {@code public static final} so they can be referenced
 * across the application without creating any objects.
 *
 * Covers:
 *  - Electricity tariff (cost per kWh)
 *  - CO2 emission factor (kg of CO2 per kWh consumed)
 *  - Usage thresholds for detecting high-energy devices
 *  - Billing period constant (days in a month)
 *  - ANSI terminal colour codes for coloured CLI output (Bonus)
 */
public final class Constants {

    // -----------------------------------------------------------------------
    // Private constructor — prevents instantiation of this utility class.
    // -----------------------------------------------------------------------
    private Constants() {
        throw new UnsupportedOperationException(
                "Constants is a utility class and should not be instantiated.");
    }

    // -----------------------------------------------------------------------
    // Tariff & Carbon
    // -----------------------------------------------------------------------

    /**
     * Electricity tariff in Indian Rupees (₹) per kilowatt-hour (kWh).
     *
     * This is an approximate residential slab rate; adjust to match your
     * local MSEDCL / BESCOM / TNEB tariff if needed.
     *
     * Used by: EnergyAnalyzer.calculateMonthlyBill(), CarbonCalculator
     */
    public static final double TARIFF_RATE_PER_KWH = 7.50;

    /**
     * CO₂ emission factor in kilograms of CO₂ emitted per kilowatt-hour
     * of electricity consumed.
     *
     * Source: Central Electricity Authority (CEA), India Grid Emission Factor
     * (~0.82 kg CO₂/kWh for the Indian grid as of recent estimates).
     *
     * Used by: CarbonCalculator.calculateCarbonFootprint()
     */
    public static final double CO2_FACTOR_KG_PER_KWH = 0.82;

    // -----------------------------------------------------------------------
    // Usage thresholds
    // -----------------------------------------------------------------------

    /**
     * Default daily kWh threshold above which a usage record is considered
     * "high usage" and flagged by the analyser's Stream filter.
     *
     * A single {@link models.UsageRecord} that exceeds this limit in one day
     * will be returned by {@code EnergyAnalyzer.getHighUsageRecords()}.
     */
    public static final double HIGH_USAGE_THRESHOLD_KWH = 2.0;

    /**
     * Minimum daily hours a device must be running to be flagged as a
     * "top energy waster" by the Comparator-based max() operation.
     *
     * Devices whose wattage × hours_per_day consumption crosses this level
     * are candidates for the top-waster report.
     */
    public static final double WASTER_THRESHOLD_HOURS = 5.0;

    // -----------------------------------------------------------------------
    // Billing
    // -----------------------------------------------------------------------

    /**
     * Number of days assumed in one billing month.
     *
     * Used by the bonus monthly bill predictor:
     *   monthly_bill = daily_kWh × DAYS_IN_MONTH × TARIFF_RATE_PER_KWH
     */
    public static final int DAYS_IN_MONTH = 30;

    // -----------------------------------------------------------------------
    // ANSI colour codes for terminal output (Bonus feature)
    // -----------------------------------------------------------------------

    /**
     * ANSI escape code to reset terminal colours back to the default.
     * Always append this after a coloured segment to avoid colour bleed.
     */
    public static final String ANSI_RESET  = "\u001B[0m";

    /**
     * ANSI escape code for <b>red</b> foreground text.
     * Intended for high-usage / danger alerts in CLI output.
     */
    public static final String ANSI_RED    = "\u001B[31m";

    /**
     * ANSI escape code for <b>yellow</b> foreground text.
     * Intended for medium-usage / warning messages in CLI output.
     */
    public static final String ANSI_YELLOW = "\u001B[33m";

    /**
     * ANSI escape code for <b>green</b> foreground text.
     * Intended for low-usage / safe / OK messages in CLI output.
     */
    public static final String ANSI_GREEN  = "\u001B[32m";

    /**
     * ANSI escape code for <b>cyan</b> foreground text.
     * Used for informational / header lines in CLI output.
     */
    public static final String ANSI_CYAN   = "\u001B[36m";

    /**
     * ANSI escape code for <b>bold</b> text (works alongside colour codes).
     * Use for section headers and important summary lines.
     */
    public static final String ANSI_BOLD   = "\u001B[1m";

    // -----------------------------------------------------------------------
    // Colour-level thresholds (used by EnergyAnalyzer.getColourLevel)
    // -----------------------------------------------------------------------

    /**
     * kWh value below which consumption is considered LOW (green).
     * If daily kWh &lt; this value, print in green.
     */
    public static final double COLOUR_LOW_KWH  = 1.0;

    /**
     * kWh value below which consumption is considered MEDIUM (yellow).
     * If daily kWh is between {@link #COLOUR_LOW_KWH} and this value,
     * print in yellow; above this value prints in red.
     */
    public static final double COLOUR_HIGH_KWH = 3.0;
}