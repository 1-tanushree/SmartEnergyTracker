import models.DeviceCategory;
import models.Device;
import models.UsageRecord;
import java.time.LocalDate;

public class M1Test {
    public static void main(String[] args) {
        System.out.println("--- SmartEnergyTracker Sample Run ---");
        
        // 1. Create a Device instance
        Device myAc = new Device("Master Bedroom AC", 1500.0, DeviceCategory.AC, LocalDate.of(2023, 5, 10));
        
        // 2. Create a UsageRecord for the device
        UsageRecord record = new UsageRecord(myAc, 8.5, LocalDate.now());
        
        // 3. Print the results
        System.out.println(myAc.toString());
        System.out.println(record.toString());
        System.out.println("High Usage (>10 kWh)? " + record.isHighUsage(10.0));
        System.out.println("Device Age (Days): " + myAc.getDeviceAgeInDays());
    }
}
