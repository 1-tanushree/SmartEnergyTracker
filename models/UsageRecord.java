package models;

import java.time.LocalDate;

/**
 * Represents a record of device usage.
 */
public class UsageRecord {
    private Device device;
    private double hoursUsed;
    private LocalDate date;

    /**
     * Constructor for UsageRecord.
     * @param device the device used
     * @param hoursUsed the number of hours used
     * @param date the date of usage
     */
    public UsageRecord(Device device, double hoursUsed, LocalDate date) {
        this.device = device;
        this.hoursUsed = hoursUsed;
        this.date = date;
    }

    /**
     * Gets the device.
     * @return device
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Gets the hours used.
     * @return hours used
     */
    public double getHoursUsed() {
        return hoursUsed;
    }

    /**
     * Gets the date of the record.
     * @return date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Calculates the kilowatt-hours (kWh) consumed.
     * @return kWh
     */
    public double calculateKWh() {
        return (device.getWattage() * hoursUsed) / 1000.0;
    }

    /**
     * Checks if the usage exceeds a given threshold.
     * @param thresholdKWh the threshold to check against
     * @return true if high usage, false otherwise
     */
    public boolean isHighUsage(double thresholdKWh) {
        return calculateKWh() > thresholdKWh;
    }

    /**
     * Returns a string representation of the usage record.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("UsageRecord[device=%s, hoursUsed=%.1f, date=%s, kWh=%.3f]", 
                device.getName(), hoursUsed, date.toString(), calculateKWh());
    }
}
