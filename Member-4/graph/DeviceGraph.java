package graph;

import models.Device;
import java.util.*;

/**
 * DeviceGraph.java — Member 4
 *
 * Represents devices as a GRAPH where edges mean two devices
 * are "co-running" (used at the same time, e.g. AC + Fan + Light).
 *
 * Why Graph? Real energy analysis needs to know which devices
 * run together — that's a cluster. BFS finds all devices in
 * the same cluster starting from any one device.
 *
 * Data Structure Used:
 *   Adjacency List → Map<Device, List<Device>>
 *
 * Visual Example:
 *   AC ──── Fan
 *   |
 *   Light ── TV
 *
 *   adjacencyList = {
 *       AC    : [Fan, Light]
 *       Fan   : [AC]
 *       Light : [AC, TV]
 *       TV    : [Light]
 *   }
 *
 * BFS from AC → finds cluster: AC, Fan, Light, TV
 */
public class DeviceGraph {

    // ─────────────────────────────────────────────
    //  Core Data Structure
    //  Key   = a Device (node in the graph)
    //  Value = List of Devices connected to it (edges)
    // ─────────────────────────────────────────────
    private final Map<Device, List<Device>> adjacencyList;

    // ─────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────
    public DeviceGraph() {
        // LinkedHashMap preserves insertion order for consistent display
        this.adjacencyList = new LinkedHashMap<>();
    }

    // ══════════════════════════════════════════════
    //  ADD DEVICE  (adds a node to the graph)
    // ══════════════════════════════════════════════

    /**
     * Adds a device as a node in the graph.
     * A device with no connections is still a valid node (isolated node).
     *
     * @param device  the Device to add
     */
    public void addDevice(Device device) {
        // putIfAbsent → only adds if device isn't already in graph
        adjacencyList.putIfAbsent(device, new ArrayList<>());
    }

    // ══════════════════════════════════════════════
    //  CONNECT DEVICES  (adds an edge)
    // ══════════════════════════════════════════════

    /**
     * Connects two devices — marks them as co-running.
     * This is an UNDIRECTED edge: if A connects to B, B also connects to A.
     *
     * @param device1  first device
     * @param device2  second device
     */
    public void connectDevices(Device device1, Device device2) {
        // Make sure both devices exist as nodes first
        addDevice(device1);
        addDevice(device2);

        // Add edge in BOTH directions (undirected graph)
        adjacencyList.get(device1).add(device2);  // device1 → device2
        adjacencyList.get(device2).add(device1);  // device2 → device1

        System.out.println("  Linked: " + device1.getName()
                + " ↔ " + device2.getName());
    }

    // ══════════════════════════════════════════════
    //  BFS TRAVERSAL  — Find Co-running Cluster
    // ══════════════════════════════════════════════

    /**
     * BFS (Breadth-First Search) starting from a given device.
     * Finds ALL devices reachable from it = the "co-running cluster".
     *
     * BFS Algorithm:
     *   1. Start at source device, mark it visited
     *   2. Add it to a Queue
     *   3. While queue is not empty:
     *      a. Remove front device
     *      b. Print it
     *      c. Add all its unvisited neighbors to queue
     *
     * @param startDevice  the device to start BFS from
     * @return             list of all devices in the same cluster
     */
    public List<Device> bfsCluster(Device startDevice) {
        List<Device> cluster = new ArrayList<>();  // result list

        // If device not in graph, return empty
        if (!adjacencyList.containsKey(startDevice)) {
            System.out.println("  [Device not found in graph: "
                    + startDevice.getName() + "]");
            return cluster;
        }

        // visited set → prevents revisiting / infinite loops
        Set<Device> visited = new HashSet<>();

        // Queue → BFS uses Queue (FIFO), not Stack
        Queue<Device> queue = new LinkedList<>();

        // Step 1: Initialize with start device
        visited.add(startDevice);
        queue.add(startDevice);

        System.out.println("\n  === Co-running Cluster (BFS from: "
                + startDevice.getName() + ") ===");

        // Step 2: BFS loop
        while (!queue.isEmpty()) {
            Device current = queue.poll();   // remove from front of queue
            cluster.add(current);

            System.out.println("  → " + current.getName()
                    + " [" + current.getCategory() + "]"
                    + " | " + current.getWattage() + "W");

            // Step 3: Visit all neighbors of current device
            List<Device> neighbors = adjacencyList.get(current);
            for (Device neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);    // mark before adding to avoid duplicates
                    queue.add(neighbor);      // add to back of queue
                }
            }
        }

        return cluster;
    }

    // ══════════════════════════════════════════════
    //  FIND ALL CLUSTERS  (all connected components)
    // ══════════════════════════════════════════════

    /**
     * Finds ALL clusters in the graph (connected components).
     * Useful when some devices are independent of others.
     *
     * Example:
     *   Cluster 1: AC, Fan, Light
     *   Cluster 2: Washing Machine (runs alone)
     *
     * @return  list of clusters, each cluster is a list of devices
     */
    public List<List<Device>> findAllClusters() {
        List<List<Device>> allClusters = new ArrayList<>();
        Set<Device> globalVisited = new HashSet<>();

        for (Device device : adjacencyList.keySet()) {
            // Only start BFS if this device hasn't been visited yet
            if (!globalVisited.contains(device)) {
                List<Device> cluster = bfsClusterSilent(device, globalVisited);
                allClusters.add(cluster);
            }
        }

        return allClusters;
    }

    /**
     * Silent BFS (no print output) — used internally by findAllClusters.
     * Updates the globalVisited set so we don't revisit across clusters.
     */
    private List<Device> bfsClusterSilent(Device startDevice,
                                           Set<Device> globalVisited) {
        List<Device> cluster = new ArrayList<>();
        Queue<Device> queue  = new LinkedList<>();

        globalVisited.add(startDevice);
        queue.add(startDevice);

        while (!queue.isEmpty()) {
            Device current = queue.poll();
            cluster.add(current);

            for (Device neighbor : adjacencyList.get(current)) {
                if (!globalVisited.contains(neighbor)) {
                    globalVisited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return cluster;
    }

    // ══════════════════════════════════════════════
    //  DISPLAY GRAPH  (show all connections)
    // ══════════════════════════════════════════════

    /**
     * Displays the full adjacency list — all devices and their connections.
     * Useful for debugging and showing the graph structure in the CLI.
     */
    public void displayGraph() {
        if (adjacencyList.isEmpty()) {
            System.out.println("  [Graph is empty — no devices added]");
            return;
        }

        System.out.println("\n  === Device Connection Graph ===");
        System.out.println("  (Devices that run together are connected)\n");

        for (Map.Entry<Device, List<Device>> entry : adjacencyList.entrySet()) {
            Device device      = entry.getKey();
            List<Device> neighbors = entry.getValue();

            System.out.print("  " + device.getName() + " ──→ ");

            if (neighbors.isEmpty()) {
                System.out.println("[no connections — runs alone]");
            } else {
                // Print all neighbors separated by commas
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < neighbors.size(); i++) {
                    sb.append(neighbors.get(i).getName());
                    if (i < neighbors.size() - 1) sb.append(", ");
                }
                System.out.println(sb.toString());
            }
        }
    }

    // ══════════════════════════════════════════════
    //  CLUSTER TOTAL WATTAGE
    // ══════════════════════════════════════════════

    /**
     * Calculates the total wattage of a cluster of co-running devices.
     * This helps identify power-heavy combinations.
     *
     * @param cluster  list of devices in a cluster (from bfsCluster)
     * @return         total wattage of all devices in cluster
     */
    public double clusterTotalWattage(List<Device> cluster) {
        double total = 0;
        for (Device d : cluster) {
            total += d.getWattage();
        }
        return total;
    }

    // ══════════════════════════════════════════════
    //  DISPLAY ALL CLUSTERS WITH WATTAGE
    // ══════════════════════════════════════════════

    /**
     * Displays every cluster found in the graph along with
     * total combined wattage of each cluster.
     */
    public void displayAllClusters() {
        List<List<Device>> clusters = findAllClusters();

        if (clusters.isEmpty()) {
            System.out.println("  [No clusters found]");
            return;
        }

        System.out.println("\n  === All Co-running Device Clusters ===");

        for (int i = 0; i < clusters.size(); i++) {
            List<Device> cluster = clusters.get(i);
            double totalWatts    = clusterTotalWattage(cluster);

            System.out.println("\n  Cluster " + (i + 1) + ":"
                    + "  [Total: " + totalWatts + "W]");

            for (Device d : cluster) {
                System.out.println("    • " + d.getName()
                        + " (" + d.getWattage() + "W)");
            }
        }
    }

    // ══════════════════════════════════════════════
    //  UTILITY
    // ══════════════════════════════════════════════

    /** Returns true if the device exists in the graph */
    public boolean containsDevice(Device device) {
        return adjacencyList.containsKey(device);
    }

    /** Returns total number of devices (nodes) in the graph */
    public int getDeviceCount() {
        return adjacencyList.size();
    }

    /** Returns the adjacency list (for advanced use by Main.java) */
    public Map<Device, List<Device>> getAdjacencyList() {
        return adjacencyList;
    }

    /** Removes a device and all its connections from the graph */
    public void removeDevice(Device device) {
        // Remove this device from all neighbor lists first
        adjacencyList.remove(device);
        for (List<Device> neighbors : adjacencyList.values()) {
            neighbors.remove(device);
        }
    }
}
