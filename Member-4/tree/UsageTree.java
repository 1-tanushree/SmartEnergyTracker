package tree;

import models.Device;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * UsageTree.java — Member 4
 *
 * A Binary Search Tree (BST) that stores Device objects
 * sorted by their daily kWh energy consumption.
 *
 * Why BST? It keeps devices automatically ordered by usage,
 * so in-order traversal gives us low → high energy consumers.
 *
 * Structure:
 *         [5.0 kWh]
 *        /         \
 *   [2.0 kWh]    [8.0 kWh]
 *      \              \
 *   [3.5 kWh]      [10.0 kWh]
 *
 * In-order traversal of above → 2.0, 3.5, 5.0, 8.0, 10.0
 */
public class UsageTree {

    // ─────────────────────────────────────────────
    //  Inner Node class — each node holds one Device
    // ─────────────────────────────────────────────
    private static class Node {
        Device device;       // the device stored at this node
        double dailyKwh;     // key used for BST ordering
        Node left;           // left child  → smaller kWh
        Node right;          // right child → larger kWh

        Node(Device device, double dailyKwh) {
            this.device   = device;
            this.dailyKwh = dailyKwh;
            this.left     = null;
            this.right    = null;
        }
    }

    // ─────────────────────────────────────────────
    //  Fields
    // ─────────────────────────────────────────────
    private Node root;   // top of the tree (starts empty)
    private int  size;   // how many devices are in the tree

    // ─────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────
    public UsageTree() {
        this.root = null;
        this.size = 0;
    }

    // ══════════════════════════════════════════════
    //  INSERT
    // ══════════════════════════════════════════════

    /**
     * Inserts a device into the BST using its daily kWh as the key.
     * dailyKwh = (wattage / 1000) * hoursUsedPerDay
     *
     * @param device      the Device object to insert
     * @param dailyKwh    the device's daily energy consumption in kWh
     */
    public void insert(Device device, double dailyKwh) {
        root = insertRec(root, device, dailyKwh);
        size++;
    }

    /**
     * Recursive helper for insert.
     * Goes left if new kWh < current node's kWh, right otherwise.
     */
    private Node insertRec(Node current, Device device, double dailyKwh) {
        // Base case: found the right empty spot → create new node here
        if (current == null) {
            return new Node(device, dailyKwh);
        }

        if (dailyKwh < current.dailyKwh) {
            // New device uses LESS energy → go LEFT
            current.left = insertRec(current.left, device, dailyKwh);
        } else if (dailyKwh > current.dailyKwh) {
            // New device uses MORE energy → go RIGHT
            current.right = insertRec(current.right, device, dailyKwh);
        } else {
            // Equal kWh: insert to the right (handles duplicates)
            current.right = insertRec(current.right, device, dailyKwh);
        }

        return current;
    }

    // ══════════════════════════════════════════════
    //  IN-ORDER TRAVERSAL  (Left → Root → Right)
    //  Gives devices sorted LOW → HIGH kWh
    // ══════════════════════════════════════════════

    /**
     * Prints all devices in ascending order of daily kWh.
     * Uses in-order traversal: Left subtree → current node → Right subtree
     */
    public void inOrderDisplay() {
        if (root == null) {
            System.out.println("  [Usage tree is empty — no devices added yet]");
            return;
        }
        System.out.println("\n  === Devices: Lowest → Highest Daily Usage ===");
        System.out.printf("  %-20s %-12s %-10s%n", "Device Name", "Category", "Daily kWh");
        System.out.println("  " + "─".repeat(45));
        inOrderRec(root);
    }

    /** Recursive in-order traversal */
    private void inOrderRec(Node current) {
        if (current == null) return;           // base case: leaf's child

        inOrderRec(current.left);              // 1. visit left subtree first
        printNode(current);                    // 2. print this node
        inOrderRec(current.right);             // 3. visit right subtree
    }

    /** Formats and prints one node's data */
    private void printNode(Node node) {
        System.out.printf("  %-20s %-12s %.3f kWh%n",
                node.device.getName(),
                node.device.getCategory(),
                node.dailyKwh);
    }

    // ══════════════════════════════════════════════
    //  GET SORTED LIST  (for use by Main.java)
    // ══════════════════════════════════════════════

    /**
     * Returns all devices as a list sorted by daily kWh (low → high).
     * Main.java can use this list to display or process further.
     */
    public List<Device> getSortedDevices() {
        List<Device> result = new ArrayList<>();
        collectInOrder(root, result);
        return result;
    }

    private void collectInOrder(Node current, List<Device> result) {
        if (current == null) return;
        collectInOrder(current.left, result);
        result.add(current.device);
        collectInOrder(current.right, result);
    }

    // ══════════════════════════════════════════════
    //  FIND HIGHEST CONSUMER
    // ══════════════════════════════════════════════

    /**
     * Returns the device with the HIGHEST daily kWh.
     * In a BST, this is always the rightmost node.
     */
    public Device getHighestConsumer() {
        if (root == null) return null;

        Node current = root;
        while (current.right != null) {
            current = current.right;   // keep going right
        }
        return current.device;
    }

    /**
     * Returns the device with the LOWEST daily kWh.
     * In a BST, this is always the leftmost node.
     */
    public Device getLowestConsumer() {
        if (root == null) return null;

        Node current = root;
        while (current.left != null) {
            current = current.left;    // keep going left
        }
        return current.device;
    }

    // ══════════════════════════════════════════════
    //  EXPORT REPORT TO .TXT FILE  (Bonus)
    // ══════════════════════════════════════════════

    /**
     * Exports the in-order device list to a text file.
     * File is saved as "energy_report.txt" in the project root.
     *
     * @param filename  name of the output file (e.g. "energy_report.txt")
     */
    public void exportReport(String filename) {
        // Collect sorted devices first
        List<Device> sorted = getSortedDevices();

        if (sorted.isEmpty()) {
            System.out.println("  [No data to export — tree is empty]");
            return;
        }

        // Try-with-resources: automatically closes the file even if error occurs
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {

            writer.println("========================================");
            writer.println("   SMART ENERGY TRACKER — USAGE REPORT");
            writer.println("========================================");
            writer.printf("%-20s %-12s %-10s%n", "Device Name", "Category", "Daily kWh");
            writer.println("─".repeat(45));

            for (Device d : sorted) {
                // We need kWh — re-traverse to get it
                writeDeviceKwh(root, d.getName(), writer);
            }

            writer.println("─".repeat(45));
            writer.println("Report generated by SmartEnergyTracker — Member 4");

            System.out.println("  ✓ Report exported to: " + filename);

        } catch (IOException e) {
            System.out.println("  [Error writing report: " + e.getMessage() + "]");
        }
    }

    /** Helper: find device by name in BST and write its line to file */
    private void writeDeviceKwh(Node current, String name, PrintWriter writer) {
        if (current == null) return;
        if (current.device.getName().equals(name)) {
            writer.printf("%-20s %-12s %.3f kWh%n",
                    current.device.getName(),
                    current.device.getCategory(),
                    current.dailyKwh);
            return;
        }
        writeDeviceKwh(current.left,  name, writer);
        writeDeviceKwh(current.right, name, writer);
    }

    // ══════════════════════════════════════════════
    //  UTILITY
    // ══════════════════════════════════════════════

    /** Returns how many devices are currently in the tree */
    public int getSize() {
        return size;
    }

    /** Checks if the tree has no devices */
    public boolean isEmpty() {
        return root == null;
    }

    /** Clears the entire tree */
    public void clear() {
        root = null;
        size = 0;
    }
}
