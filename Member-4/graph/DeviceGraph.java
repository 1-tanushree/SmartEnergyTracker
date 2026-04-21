package graph;

import models.Device;
import java.util.*;

public class DeviceGraph {

 
    //  Core Data Structure
    //  Key   = a Device (node in the graph)
    //  Value = List of Devices connected to it 
   
    private final Map<Device, List<Device>> adjacencyList;

  //constructor 
    public DeviceGraph() {
       
        this.adjacencyList = new LinkedHashMap<>();
    }


    public void addDevice(Device device) {
        // putIfAbsent → only adds if device isn't already in graph
        adjacencyList.putIfAbsent(device, new ArrayList<>());
    }

    //connecting the device 
    public void connectDevices(Device device1, Device device2) {
       
        addDevice(device1);
        addDevice(device2);

        // Add edge in BOTH directions (undirected graph)
        adjacencyList.get(device1).add(device2);  // device1 → device2
        adjacencyList.get(device2).add(device1);  // device2 → device1

        System.out.println("  Linked: " + device1.getName()
                + " ↔ " + device2.getName());
    }

//with the help of bfs it clister all the devices connected 
    //queue is used 
    //visit the node marke it visited add it in the queue then explore it;s neighbore 
    public List<Device> bfsCluster(Device startDevice) {
        List<Device> cluster = new ArrayList<>();  // result list

        // If device not in graph, return empty
        if (!adjacencyList.containsKey(startDevice)) {
            System.out.println("  [Device not found in graph: "
                    + startDevice.getName() + "]");
            return cluster;
        }

        // visited set → prevents revisiting
        Set<Device> visited = new HashSet<>();

        // Queue → BFS uses Queue (FIFO)
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

   //find the cluster 
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

    //  DISPLAY GRAPH  (show all connections)

   
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

  
    //  CLUSTER TOTAL WATTAGE
  
   //scanning the co-runnung devices also 
    public double clusterTotalWattage(List<Device> cluster) {
        double total = 0;
        for (Device d : cluster) {
            total += d.getWattage();
        }
        return total;
    }

  
    //  DISPLAY ALL CLUSTERS WITH WATTAGE
  
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

 
    //  UTILITY
   
    /** Returns true if the device exists in the graph */
    public boolean containsDevice(Device device) {
        return adjacencyList.containsKey(device);
    }

    /** Returns total number of devices (nodes) in the graph */
    public int getDeviceCount() {
        return adjacencyList.size();
    }

    /** Returns the adjacency list */
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
