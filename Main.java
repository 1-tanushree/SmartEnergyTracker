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

/**
 * Main.java — Member 4
 *
 * The ENTRY POINT and INTEGRATION HUB of the Smart Energy Tracker.
 *
 * This class:
 *  1. Runs a while(true) CLI menu loop
 *  2. Takes user input and validates it
 *  3. Calls methods from ALL other members' classes
 *  4. Displays results neatly on the command line
 *
 * Menu Structure:
 *  ┌─────────────────────────────────────┐
 *  │  1. Add Device                      │
 *  │  2. View All Devices                │
 *  │  3. Log Usage                       │
 *  │  4. Show Energy Report              │
 *  │  5. Show Carbon Footprint           │
 *  │  6. Show Usage Tree (BST)           │
 *  │  7. Connect Devices (Graph)         │
 *  │  8. Find Co-running Cluster (BFS)   │
 *  │  9. Show All Clusters               │
 *  │  10. Export Report to File          │
 *  │  11. Top Energy Waster              │
 *  │  0. Exit                            │
 *  └─────────────────────────────────────┘
 */
public class Main {

    // ─────────────────────────────────────────────
    //  Shared objects — created once, used everywhere
    // ─────────────────────────────────────────────
    private static DeviceManager   deviceManager   = new DeviceManager();
    private static EnergyManager   energyManager   = new EnergyManager();
    private static EnergyAnalyzer  energyAnalyzer  = new EnergyAnalyzer();
    private static CarbonCalculator carbonCalc     = new CarbonCalculator();
    private static DeviceGraph     deviceGraph     = new DeviceGraph();
    private static UsageTree       usageTree       = new UsageTree();

    // Scanner reads user input from the keyboard
    private static Scanner scanner = new Scanner(System.in);

    // ══════════════════════════════════════════════
    //  MAIN METHOD — Program starts here
    // ══════════════════════════════════════════════
    public static void main(String[] args) {

        printBanner();   // show welcome screen

        // ── The main loop ──────────────────────────
        // Runs FOREVER until user chooses 0 (Exit)
        while (true) {
            printMenu();                   // display options
            int choice = readIntInput();   // read & validate choice

            // Route to the right method based on choice
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
                    System.exit(0);       // cleanly exit the program
                }
                default -> System.out.println(
                        "\n  [Invalid option — please enter 0 to 11]");
            }

            // Small pause before showing menu again
            System.out.println();
        }
    }

    // ══════════════════════════════════════════════
    //  MENU OPTION 1 — Add Device
    // ══════════════════════════════════════════════

    /**
     * Asks user for device details and adds it to the system.
     * Validates: name cannot be empty, wattage cannot be negative.
     */
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

        // ── Get wattage (must be positive) ────────
        double wattage = -1;
        while (wattage <= 0) {
            System.out.print("  Enter wattage (W): ");
            wattage = readDoubleInput();
            if (wattage <= 0) {
                System.out.println("  [Wattage must be greater than 0 — try again]");
            }
        }

        // ── Get category ──────────────────────────
        System.out.println("  Categories: AC / LIGHT / APPLIANCE");
        System.out.print("  Enter category: ");
        String category = scanner.nextLine().trim().toUpperCase();

        // Validate category — default to APPLIANCE if unrecognized
        if (!category.equals("AC") && !category.equals("LIGHT")
                && !category.equals("APPLIANCE")) {
            System.out.println("  [Unrecognized category — defaulting to APPLIANCE]");
            category = "APPLIANCE";
        }

        // ── Create device and add to all structures ─
        Device newDevice = new Device(name, wattage, category);
        deviceManager.addDevice(newDevice);
        deviceGraph.addDevice(newDevice);     // also add to graph as isolated node

        System.out.println("  ✓ Device added: " + newDevice);
    }

    // ══════════════════════════════════════════════
    //  MENU OPTION 2 — View All Devices
    // ══════════════════════════════════════════════

    /**
     * Displays all devices stored in DeviceManager.
     * Uses M2's display method.
     */
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

    // ══════════════════════════════════════════════
    //  MENU OPTION 3 — Log Usage
    // ══════════════════════════════════════════════

    /**
     * Logs how many hours a device was used today.
     * Also inserts the device into the UsageTree with its daily kWh.
     */
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

        // Pick a device
        System.out.print("  Select device number: ");
        int choice = readIntInput() - 1;  // convert to 0-based index

        if (choice < 0 || choice >= devices.size()) {
            System.out.println("  [Invalid selection]");
            return;
        }

        Device selected = devices.get(choice);

        // Get hours used (must be 0–24)
        double hours = -1;
        while (hours < 0 || hours > 24) {
            System.out.print("  Hours used today (0–24): ");
            hours = readDoubleInput();
            if (hours < 0 || hours > 24) {
                System.out.println("  [Hours must be between 0 and 24]");
            }
        }

        // Calculate daily kWh: (wattage / 1000) × hours
        double dailyKwh = (selected.getWattage() / 1000.0) * hours;

        // Create UsageRecord (M1's class) and log it
        UsageRecord record = new UsageRecord(selected, hours, LocalDate.now());
        energyManager.logUsage(record);

        // Insert into BST (UsageTree) with the daily kWh as key
        usageTree.insert(selected, dailyKwh);

        System.out.printf("  ✓ Logged: %s used for %.1f hours = %.3f kWh%n",
                selected.getName(), hours, dailyKwh);
    }

    // ══════════════════════════════════════════════
    //  MENU OPTION 4 — Energy Report
    // ══════════════════════════════════════════════

    /**
     * Calls M3's EnergyAnalyzer to display:
     * - Average consumption
     * - Devices above threshold
     * - Grouped by category
     */
    private static void showEnergyReport() {
        System.out.println("\n  ── Energy Analysis Report ──");

        List<UsageRecord> records = energyManager.getAllRecords();
        if (records.isEmpty()) {
            System.out.println("  [No usage logged yet — use Option 3 first]");
            return;
        }

        // M3 handles all the Stream + Lambda magic here
        energyAnalyzer.displayReport(records);
    }

    // ══════════════════════════════════════════════
    //  MENU OPTION 5 — Carbon Footprint
    // ══════════════════════════════════════════════

    /**
     * Uses M3's CarbonCalculator.
     * Formula: kWh × CO2_FACTOR (from Constants.java)
     */
    private static void showCarbonFootprint() {
        System.out.println("\n  ── Carbon Footprint Report ──");

        List<UsageRecord> records = energyManager.getAllRecords();
        if (records.isEmpty()) {
            System.out.println("  [No usage logged yet]");
            return;
        }

        carbonCalc.displayCarbonReport(records);
    }

    // ══════════════════════════════════════════════
    //  MENU OPTION 6 — Show Usage Tree (BST)
    // ══════════════════════════════════════════════

    /**
     * Displays the BST in-order → devices sorted low to high kWh.
     * Also shows the biggest and smallest consumer.
     */
    private static void showUsageTree() {
        System.out.println("\n  ── Usage Tree (BST — Low to High kWh) ──");

        usageTree.inOrderDisplay();

        // Extra info: highest and lowest consumer
        Device highest = usageTree.getHighestConsumer();
        Device lowest  = usageTree.getLowestConsumer();

        if (highest != null) {
            System.out.println("\n  🔴 Highest consumer : " + highest.getName());
            System.out.println("  🟢 Lowest consumer  : " + lowest.getName());
        }
    }

    // ══════════════════════════════════════════════
    //  MENU OPTION 7 — Connect Devices (Graph Edge)
    // ══════════════════════════════════════════════

    /**
     * Links two devices as co-running (adds an edge in the graph).
     * User picks two devices from the list.
     */
    private static void connectDevices() {
        System.out.println("\n  ── Connect Co-running Devices ──");
        System.out.println("  (Connect devices that run at the same time)\n");

        List<Device> devices = deviceManager.getAllDevices();
        if (devices.size() < 2) {
            System.out.println("  [Need at least 2 devices — add more first]");
            return;
        }

        // Show list
        for (int i = 0; i < devices.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + devices.get(i).getName());
        }

        // Pick first device
        System.out.print("\n  Select first device: ");
        int first = readIntInput() - 1;

        // Pick second device
        System.out.print("  Select second device: ");
        int second = readIntInput() - 1;

        // Validate selection
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

        connectDevices(d1, d2);   // helper method below
    }

    /** Helper: connects two Device objects in the graph */
    private static void connectDevices(Device d1, Device d2) {
        deviceGraph.connectDevices(d1, d2);
        System.out.println("  ✓ Connected: " + d1.getName()
                + " ↔ " + d2.getName());
    }

    // ══════════════════════════════════════════════
    //  MENU OPTION 8 — Find Cluster (BFS)
    // ══════════════════════════════════════════════

    /**
     * Runs BFS from a selected device.
     * Shows all co-running devices (the cluster) and total wattage.
     */
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

    // ══════════════════════════════════════════════
    //  MENU OPTION 9 — Show All Clusters
    // ══════════════════════════════════════════════

    /**
     * Finds all connected components in the graph and displays them.
     */
    private static void showAllClusters() {
        System.out.println("\n  ── All Device Clusters ──");
        deviceGraph.displayGraph();
        deviceGraph.displayAllClusters();
    }

    // ══════════════════════════════════════════════
    //  MENU OPTION 10 — Export Report to File
    // ══════════════════════════════════════════════

    /**
     * Exports the BST in-order list to a .txt file.
     * File is saved in the project's root directory.
     */
    private static void exportReport() {
        System.out.println("\n  ── Export Usage Report ──");

        if (usageTree.isEmpty()) {
            System.out.println("  [No usage data yet — log some usage first]");
            return;
        }

        System.out.print("  Enter filename (e.g. report.txt): ");
        String filename = scanner.nextLine().trim();

        // Default filename if user presses enter without typing
        if (filename.isEmpty()) {
            filename = "energy_report.txt";
            System.out.println("  [Using default: energy_report.txt]");
        }

        usageTree.exportReport(filename);
    }

    // ══════════════════════════════════════════════
    //  MENU OPTION 11 — Top Energy Waster
    // ══════════════════════════════════════════════

    /**
     * Uses M3's EnergyAnalyzer to find the device consuming the most energy.
     * Cross-checks with UsageTree's getHighestConsumer() (BST rightmost node).
     */
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

        // Also call M3's analyzer for stream-based top waster
        List<UsageRecord> records = energyManager.getAllRecords();
        if (!records.isEmpty()) {
            energyAnalyzer.displayTopWaster(records);
        }
    }

    // ══════════════════════════════════════════════
    //  INPUT HELPERS — Validation methods
    // ══════════════════════════════════════════════

    /**
     * Safely reads an integer from the user.
     * If they type something that isn't a number, returns -1 (handled by callers).
     */
    private static int readIntInput() {
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            // User typed letters instead of a number
            return -1;
        }
    }

    /**
     * Safely reads a double (decimal number) from the user.
     * Returns -1 if input is invalid.
     */
    private static double readDoubleInput() {
        try {
            String line = scanner.nextLine().trim();
            return Double.parseDouble(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ══════════════════════════════════════════════
    //  DISPLAY HELPERS
    // ══════════════════════════════════════════════

    /** Prints the welcome banner shown at startup */
    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      SMART ENERGY CONSUMPTION TRACKER   ║");
        System.out.println("║         Powered by Java DSA              ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
    }

    /** Prints the main menu options */
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
