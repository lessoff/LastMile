package com.delivery.optimizer.algorithm;

import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Route;

import java.util.*;

public class GreedyEdgeStrategy implements RoutingStrategy {

    @Override
    public Route optimize(DeliveryGraph graph, List<Package> packages) {
        // Collect all nodes to visit (including depot = 0)
        Set<Integer> nodeSet = new HashSet<>();
        nodeSet.add(0);
        for (Package pkg : packages) {
            int idx = graph.getNodeIndex(pkg.getId());
            if (idx >= 0) {
                nodeSet.add(idx);
            }
        }

        List<Integer> nodes = new ArrayList<>(nodeSet);
        int n = nodes.size();

        // Build all edges sorted by distance
        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                edges.add(new int[]{nodes.get(i), nodes.get(j)});
            }
        }
        edges.sort((a, b) -> Double.compare(
            graph.getDistance(a[0], a[1]),
            graph.getDistance(b[0], b[1])
        ));

        // Greedy edge insertion with Union-Find and degree constraints
        int[] degree = new int[graph.getNodeCount()];
        UnionFind uf = new UnionFind(graph.getNodeCount());
        Map<Integer, List<Integer>> adjacency = new HashMap<>();
        for (int node : nodes) {
            adjacency.put(node, new ArrayList<>());
        }

        int edgesAdded = 0;
        int edgesNeeded = n; // n edges to form a Hamiltonian cycle on n nodes

        for (int[] edge : edges) {
            if (edgesAdded >= edgesNeeded) break;

            int u = edge[0];
            int v = edge[1];

            // Skip if either node already has degree 2
            if (degree[u] >= 2 || degree[v] >= 2) continue;

            // Skip if adding this edge creates a premature cycle
            if (uf.find(u) == uf.find(v) && edgesAdded < edgesNeeded - 1) continue;

            // Add the edge
            adjacency.get(u).add(v);
            adjacency.get(v).add(u);
            degree[u]++;
            degree[v]++;
            uf.union(u, v);
            edgesAdded++;
        }

        // Extract the route starting from depot (node 0)
        List<Integer> route = extractRoute(adjacency, nodes, 0);

        Route result = new Route(route, 0, getName());
        result.calculateTotalDistance(graph);
        return result;
    }

    private List<Integer> extractRoute(Map<Integer, List<Integer>> adjacency, List<Integer> nodes, int start) {
        List<Integer> route = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        route.add(start);
        visited.add(start);

        int current = start;
        while (visited.size() < nodes.size()) {
            List<Integer> neighbors = adjacency.get(current);
            boolean found = false;
            for (int neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    route.add(neighbor);
                    visited.add(neighbor);
                    current = neighbor;
                    found = true;
                    break;
                }
            }
            if (!found) break;
        }

        route.add(start); // Return to depot
        return route;
    }

    @Override
    public String getName() {
        return "Greedy Edge Insertion";
    }

    private static class UnionFind {
        private final int[] parent;
        private final int[] rank;

        UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        void union(int x, int y) {
            int px = find(x);
            int py = find(y);
            if (px == py) return;
            if (rank[px] < rank[py]) { parent[px] = py; }
            else if (rank[px] > rank[py]) { parent[py] = px; }
            else { parent[py] = px; rank[px]++; }
        }
    }
}
