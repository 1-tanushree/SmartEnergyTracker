package manager;

import models.Device;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the collection of devices in the energy tracker.
 * This class handles adding and retrieving devices.
 */
public class DeviceManager {
    private List<Device> devices;

    /**
     * Constructor for DeviceManager.
     * Initializes an empty list of devices.
     */
    public DeviceManager() {
        this.devices = new ArrayList<>();
    }

    /**
     * Adds a new device to the system.
     * @param device the device to add
     */
    public void addDevice(Device device) {
        if (device != null) {
            devices.add(device);
        }
    }

    /**
     * Retrieves all registered devices.
     * @return a list of all devices
     */
    public List<Device> getAllDevices() {
        return new ArrayList<>(devices); // Return a copy for safety
    }
}