import manager.DeviceManager;
import manager.EnergyManager;
import analytics.EnergyAnalyzer;
import analytics.CarbonCalculator;
import graph.DeviceGraph;
import tree.UsageTree;
import models.Device;
import models.UsageRecord;
import util.Constants;

import java.util.*;
import java.time.LocalDate;

public class Main {

   //object creation 
    private static DeviceManager   deviceManager   = new DeviceManager();
    private static EnergyManager   energyManager   = new EnergyManager();
    private static EnergyAnalyzer  energyAnalyzer  = new EnergyAnalyzer();
    private static CarbonCalculator carbonCalc     = new CarbonCalculator();
    private static DeviceGraph     deviceGraph     = new DeviceGraph();
    private static UsageTree       usageTree       = new UsageTree();
    // Scanner for the input 
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        printBanner();   
       
        while (true) {
            printMenu();                   
            int choice = readIntInput();  

            
            switch (choice) {
                case 1  -> addDevice();
                case 2  -> viewAllDevices();
                case 3  -> logUsage();
                case 4  -> showEnergyReport();
                case 5  -> showCarbonFootprint();
                case 6  -> showUsageTree();
                case 7  -> connectDevices();
                case 8  -> findCluster();
                case 9  -> showAllClusters();
                case 10 -> exportReport();
                case 11 -> showTopWaster();
                case 0  -> {
                    System.out.println("\n  Goodbye! Save energy. 🌱");
                    scanner.close();
                    System.exit(0);      
                }
                default -> System.out.println(
                        "\n  [Invalid option — please enter 0 to 11]");
            }

           
            System.out.println();
        }
    }

//device detais with validation of name can't be empty and wattage can't me -ve  
    private static void addDevice() {
        System.out.println("\n  ── Add New Device ──");

        // ── Get device name (must not be empty) ───
        String name = "";
        while (name.trim().isEmpty()) {
            System.out.print("  Enter device name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("  [Name cannot be empty — try again]");
            }
        }

        double wattage = -1;
        while (wattage <= 0)
            {
            System.out.print("  Enter wattage (W): ");
            wattage = readDoubleInput();
            if (wattage <= 0)
              {
                System.out.println("  [Wattage must be greater than 0 — try again]");
              }
           }

       
        System.out.println("  Categories: AC / LIGHT / APPLIANCE");
        System.out.print("  Enter category: ");
        String category = scanner.nextLine().trim().toUpperCase();

        
        if (!category.equals("AC") && !category.equals("LIGHT")
                && !category.equals("APPLIANCE")) {
            System.out.println("  [Unrecognized category — defaulting to APPLIANCE]");
            category = "APPLIANCE";
        }

        //  Creating  device and add to all structures 
        Device newDevice = new Device(name, wattage, category);
        deviceManager.addDevice(newDevice);
        deviceGraph.addDevice(newDevice);     
        
        System.out.println("  ✓ Device added: " + newDevice);
    }

  
   //view all the devices 
    private static void viewAllDevices() {
        System.out.println("\n  ── All Registered Devices ──");
        List<Device> devices = deviceManager.getAllDevices();

        if (devices.isEmpty()) {
            System.out.println("  [No devices registered yet]");
            return;
        }

        System.out.printf("  %-5s %-20s %-10s %-12s%n",
                "#", "Name", "Wattage", "Category"); 
        System.out.println("  " + "─".repeat(50));

        int index = 1;
        for (Device d : devices) {
            System.out.printf("  %-5d %-20s %-10.1fW %-12s%n",
                    index++, d.getName(), d.getWattage(), d.getCategory());
        }
    }

    //how many hrs the use of the perticular device 
    private static void logUsage() {
        System.out.println("\n  ── Log Device Usage ──");

        List<Device> devices = deviceManager.getAllDevices();
        if (devices.isEmpty()) {
            System.out.println("  [Add devices first before logging usage]");
            return;
        }

        // Show numbered list of devices
        for (int i = 0; i < devices.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + devices.get(i).getName());
        }

       
        System.out.print("  Select device number: ");
        int choice = readIntInput() - 1; 
        if (choice < 0 || choice >= devices.size()) {
            System.out.println("  [Invalid selection]");
            return;
        }

        Device selected = devices.get(choice);

    //get hrs from user 
        double hours = -1;
        while (hours < 0 || hours > 24) {
            System.out.print("  Hours used today (0–24): ");
            hours = readDoubleInput();
            if (hours < 0 || hours > 24) {
                System.out.println("  [Hours must be between 0 and 24]");
            }
        }

        // Calculate daily kWh 
        double dailyKwh = (selected.getWattage() / 1000.0) * hours;

        UsageRecord record = new UsageRecord(selected, hours, LocalDate.now());
        energyManager.logUsage(record);

        
        usageTree.insert(selected, dailyKwh);

        System.out.printf("  ✓ Logged: %s used for %.1f hours = %.3f kWh%n",
                selected.getName(), hours, dailyKwh);
    }

    //shows avg consumption ,device above threshold and category 
    private static void showEnergyReport() {
        System.out.println("\n  ── Energy Analysis Report ──");

        List<UsageRecord> records = energyManager.getAllRecords();
        if (records.isEmpty()) {
            System.out.println("  [No usage logged yet — use Option 3 first]");
            return;
        }

        energyAnalyzer.displayReport(records);
    }

    //calulating carboonfootprinting 
    private static void showCarbonFootprint() {
        System.out.println("\n  ── Carbon Footprint Report ──");

        List<UsageRecord> records = energyManager.getAllRecords();
        if (records.isEmpty()) {
            System.out.println("  [No usage logged yet]");
            return;
        }

        carbonCalc.displayCarbonReport(records);
    }

    //display BST in order low to high and bigest and smallest consumer 
    private static void showUsageTree() {
        System.out.println("\n  ── Usage Tree (BST — Low to High kWh) ──");

        usageTree.inOrderDisplay();


        
        Device highest = usageTree.getHighestConsumer();
        Device lowest  = usageTree.getLowestConsumer();

        if (highest != null) {
            System.out.println("\n  🔴 Highest consumer : " + highest.getName());
            System.out.println("  🟢 Lowest consumer  : " + lowest.getName());
        }
    }

    //linking 2 devices as co-running devices 
    private static void connectDevices() {
        System.out.println("\n  ── Connect Co-running Devices ──");
        System.out.println("  (Connect devices that run at the same time)\n");

        List<Device> devices = deviceManager.getAllDevices();
        if (devices.size() < 2) {
            System.out.println("  [Need at least 2 devices — add more first]");
            return;
        }

       
        for (int i = 0; i < devices.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + devices.get(i).getName());
        }

        // Pick first device
        System.out.print("\n  Select first device: ");
        int first = readIntInput() - 1;

        // Pick second device
        System.out.print("  Select second device: ");
        int second = readIntInput() - 1;

        if (first < 0 || second < 0
                || first >= devices.size() || second >= devices.size()) {
            System.out.println("  [Invalid selection]");
            return;
        }
        if (first == second) {
            System.out.println("  [Cannot connect a device to itself]");
            return;
        }

        // Add both to graph and connect them
        Device d1 = devices.get(first);
        Device d2 = devices.get(second);

        connectDevices(d1, d2); 
    }

   
    private static void connectDevices(Device d1, Device d2) {
        deviceGraph.connectDevices(d1, d2);
        System.out.println("  ✓ Connected: " + d1.getName()
                + " ↔ " + d2.getName());
    }

    //run bfs to selected devics and show the co-runninbg devices 
    private static void findCluster() {
        System.out.println("\n  ── Find Co-running Cluster (BFS) ──");

        List<Device> devices = deviceManager.getAllDevices();
        if (devices.isEmpty()) {
            System.out.println("  [No devices available]");
            return;
        }

        for (int i = 0; i < devices.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + devices.get(i).getName());
        }

        System.out.print("\n  Start BFS from device: ");
        int choice = readIntInput() - 1;

        if (choice < 0 || choice >= devices.size()) {
            System.out.println("  [Invalid selection]");
            return;
        }

        Device startDevice = devices.get(choice);

        // Run BFS — returns the cluster list
        List<Device> cluster = deviceGraph.bfsCluster(startDevice);

        // Show total wattage of the cluster
        double totalWatts = deviceGraph.clusterTotalWattage(cluster);
        System.out.printf("%n  Total cluster wattage: %.1fW%n", totalWatts);
        System.out.printf("  Estimated hourly cost: ₹%.2f%n",
                (totalWatts / 1000.0) * Constants.TARIFF_RATE);
    }

  //show all the cluster 
    private static void showAllClusters() {
        System.out.println("\n  ── All Device Clusters ──");
        deviceGraph.displayGraph();
        deviceGraph.displayAllClusters();
    }
    
//exporting a file 
    private static void exportReport() {
        System.out.println("\n  ── Export Usage Report ──");

        if (usageTree.isEmpty()) {
            System.out.println("  [No usage data yet — log some usage first]");
            return;
        }

        System.out.print("  Enter filename (e.g. report.txt): ");
        String filename = scanner.nextLine().trim();

       
        if (filename.isEmpty()) {
            filename = "energy_report.txt";
            System.out.println("  [Using default: energy_report.txt]");
        }

        usageTree.exportReport(filename);
    }

   //membes have already done it just taking from that 
    private static void showTopWaster() {
        System.out.println("\n  ── Top Energy Waster ──");

        // From BST — rightmost node is highest kWh
        Device topFromTree = usageTree.getHighestConsumer();

        if (topFromTree == null) {
            System.out.println("  [No usage data yet]");
            return;
        }

        System.out.println("  🔴 Top energy waster: " + topFromTree.getName());
        System.out.println("     Category : " + topFromTree.getCategory());
        System.out.println("     Wattage  : " + topFromTree.getWattage() + "W");

       
        List<UsageRecord> records = energyManager.getAllRecords();
        if (!records.isEmpty()) {
            energyAnalyzer.displayTopWaster(records);
        }
    }

  
    private static int readIntInput() {
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double readDoubleInput() {
        try {
            String line = scanner.nextLine().trim();
            return Double.parseDouble(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      SMART ENERGY CONSUMPTION TRACKER   ║");
        System.out.println("║         Powered by Java DSA              ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
    }


    private static void printMenu() {
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│               MAIN MENU                 │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│  1.  Add Device                         │");
        System.out.println("│  2.  View All Devices                   │");
        System.out.println("│  3.  Log Usage (hours used today)       │");
        System.out.println("│  4.  Energy Analysis Report             │");
        System.out.println("│  5.  Carbon Footprint Report            │");
        System.out.println("│  6.  Usage Tree (BST — low → high)      │");
        System.out.println("│  7.  Connect Co-running Devices         │");
        System.out.println("│  8.  Find Cluster from Device (BFS)     │");
        System.out.println("│  9.  Show All Device Clusters           │");
        System.out.println("│  10. Export Report to .txt File         │");
        System.out.println("│  11. Top Energy Waster                  │");
        System.out.println("│  0.  Exit                               │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.print("  Choose an option: ");
    }
}
