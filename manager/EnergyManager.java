package manager;

import models.UsageRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the collection of energy usage records.
 * This class handles logging and retrieving usage data.
 */
public class EnergyManager {
    private List<UsageRecord> usageRecords;

    /**
     * Constructor for EnergyManager.
     * Initializes an empty list of usage records.
     */
    public EnergyManager() {
        this.usageRecords = new ArrayList<>();
    }

    /**
     * Logs a new usage record into the system.
     * @param record the usage record to log
     */
    public void logUsage(UsageRecord record) {
        if (record != null) {
            usageRecords.add(record);
        }
    }

    /**
     * Retrieves all logged usage records.
     * @return a list of all usage records
     */
    public List<UsageRecord> getAllRecords() {
        return new ArrayList<>(usageRecords); // Return a copy for safety
    }
}