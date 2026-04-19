package models;

/**
 * Represents the category of a device.
 */
public enum DeviceCategory {
    AC("Air Conditioner"),
    LIGHT("Light"),
    APPLIANCE("Appliance");

    private final String displayName;

    /**
     * Constructor for DeviceCategory.
     * @param displayName the display name of the category
     */
    DeviceCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name.
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the display name of the category.
     * @return display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
