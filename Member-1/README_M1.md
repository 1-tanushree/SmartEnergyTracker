# SmartEnergyTracker - Member 1 (Models)

This directory contains the `models/` package classes that represent the core domain entities for tracking smart energy usage.

## Files

- **`DeviceCategory.java`**
  An enumeration that defines the supported categories for devices (e.g., `AC`, `LIGHT`, `APPLIANCE`) and provides a human-readable display name for each category.

- **`Device.java`**
  Represents an electrical device with properties such as name, wattage, category, and an optional purchase date. It supports comparing devices by wattage, equality checks, and calculating the device's age in days.

- **`UsageRecord.java`**
  Tracks the usage of a specific device on a given date, including the total hours used. It provides functionality to calculate the kilowatt-hours (kWh) consumed and check if usage exceeds a certain threshold.

## Sample Usage

```java
import models.DeviceCategory;
import models.Device;
import models.UsageRecord;
import java.time.LocalDate;

// 1. Create a Device instance
Device myAc = new Device("Master Bedroom AC", 1500.0, DeviceCategory.AC, LocalDate.of(2023, 5, 10));

// 2. Create a UsageRecord for the device
UsageRecord record = new UsageRecord(myAc, 8.5, LocalDate.now());

// 3. Print the results
System.out.println(myAc.toString());
System.out.println(record.toString());
System.out.println("High Usage (>10 kWh)? " + record.isHighUsage(10.0));
System.out.println("Device Age (Days): " + myAc.getDeviceAgeInDays());
```
