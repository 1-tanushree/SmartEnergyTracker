package models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents a device in the smart energy tracker.
 */
public class Device implements Comparable<Device> {
    private String name;
    private double wattage;
    private DeviceCategory category;
    private LocalDate purchaseDate;

    /**
     * Constructor for Device without purchase date.
     * @param name the device name
     * @param wattage the device wattage
     * @param category the device category
     */
    public Device(String name, double wattage, DeviceCategory category) {
        this.name = name;
        this.wattage = wattage;
        this.category = category;
        this.purchaseDate = LocalDate.now(); // Bonus: default to today
    }

    /**
     * Constructor for Device with purchase date.
     * @param name the device name
     * @param wattage the device wattage
     * @param category the device category
     * @param purchaseDate the purchase date
     */
    public Device(String name, double wattage, DeviceCategory category, LocalDate purchaseDate) {
        this.name = name;
        this.wattage = wattage;
        this.category = category;
        this.purchaseDate = purchaseDate;
    }

    /**
     * Gets the name of the device.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the wattage of the device.
     * @return wattage
     */
    public double getWattage() {
        return wattage;
    }

    /**
     * Gets the category of the device.
     * @return category
     */
    public DeviceCategory getCategory() {
        return category;
    }

    /**
     * Gets the purchase date of the device.
     * @return purchase date
     */
    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    /**
     * Calculates the age of the device in days.
     * @return days since purchase
     */
    public long getDeviceAgeInDays() {
        if (purchaseDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(purchaseDate, LocalDate.now());
    }

    /**
     * Compares this device to another based on wattage ascending.
     * @param other the other device
     * @return negative if less, 0 if equal, positive if greater
     */
    @Override
    public int compareTo(Device other) {
        return Double.compare(this.wattage, other.wattage);
    }

    /**
     * Checks if two devices are equal based on name and category.
     * @param o the other object
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(name, device.name) && category == device.category;
    }

    /**
     * Generates a hash code based on name and category.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, category);
    }

    /**
     * Returns a formatted string representation of the device.
     * @return formatted string
     */
    @Override
    public String toString() {
        return String.format("Device[name=%s, wattage=%.1f, category=%s]", name, wattage, category.toString());
    }
}
