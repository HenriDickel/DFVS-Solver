package program.packing;

import program.algo.*;
import program.model.Component;
import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PackingManager {

    private Map<Integer, Node> initialNodes;
    private Graph packingGraph;
    private List<Cycle> packing = new ArrayList<>();

    private Map<Integer, Cycle> bestCycles = new HashMap<>();

    public PackingManager(Graph initialGraph, long timelimit) {
        // Copy nodes
        Graph copy = initialGraph.copy();
        initialNodes = copy.getNodeMap();
        // Copy into packing graph
        packingGraph = initialGraph.copy();

        initPacking(timelimit);
    }

    public int size() {
        return packing.stream().mapToInt(Component::getK).sum();
    }

    public List<Cycle> getPacking() {
        return packing;
    }

    public Graph getPackingGraph() {
        return packingGraph;
    }

    public PackingManager(PackingManager oldPm, List<Integer> deleteIds, List<Integer> forbiddenIds, int level) {
        this.initialNodes = oldPm.initialNodes;
        this.packingGraph = oldPm.packingGraph.copy();

        // First copy all packing cycles from the previous packing that have not been deleted
        for(Cycle cycle: oldPm.packing) {
            boolean deleted = false;
            for(Integer deleteId: deleteIds) {
                deleted = deleted || cycle.containsId(deleteId);
            }
            if(!deleted) {
                this.packing.add(cycle); // TODO necessary to copy cycle?
            } else {
                // Re-add cycle to the graph
                for(Node node: cycle.getNodes()) {
                    if(!deleteIds.contains(node.id)) { // Only add the not deleted nodes
                        Node initialNode = initialNodes.get(node.id).copy();
                        this.packingGraph.addInitialNode(initialNode, level);
                    }
                }
            }
        }

        // Clean up deleted nodes from packing graph
        for(Integer deleteId: deleteIds) {
            if(packingGraph.hasNode(deleteId)) {
                packingGraph.removeNode(deleteId);
            }
        }

        // Forbidden nodes get re-added to the packing graph
        for(Integer forbiddenId: forbiddenIds) {
            if(!packingGraph.hasNode(forbiddenId)) {
                Node forbiddenNode = initialNodes.get(forbiddenId);
                forbiddenNode.forbidden = true;
                forbiddenNode.pNew = true;
                packingGraph.addInitialNode(forbiddenNode, level);
            } else {
                Node forbiddenNode = packingGraph.getNode(forbiddenId);
                forbiddenNode.forbidden = true;
                forbiddenNode.pNew = true;
            }
        }
        updatePacking();
    }

    public void addDeletedNodes(List<Integer> deletedIds, int level) {
        // Re-add deleted ids
        for(Integer deletedId: deletedIds) {
            Node initialNode = initialNodes.get(deletedId).copy();
            packingGraph.addInitialNode(initialNode, level);
            initialNode.pNew = true;
        }
    }

    public void removeForbiddenNodes(List<Integer> forbiddenIds) {
        List<Cycle> remove = new ArrayList<>();
        for(Cycle cycle: packing) {
            for(Integer forbiddenId: forbiddenIds) {
                if(cycle.containsId(forbiddenId)) remove.add(cycle);
            }
        }
        for(Cycle cycle: remove) {
            packing.remove(cycle);
        }
    }

    private void initPacking(long timeLimit) {

        long endMillis = System.currentTimeMillis() + timeLimit;

        // Fill the packing with pair cycles
        initPackingWithPairs();

        // Initialize the map with the best cycle for each node
        for(Node node: packingGraph.getNodes()) {
            Cycle cycle = PackingBFS.findBestCycle(packingGraph, node);
            if(cycle != null) {
                bestCycles.put(node.id, cycle);
            } else {
                node.acyclic = true;
            }
        }

        // Fill the packing with other cycles
        initPackingWithCycles(endMillis);
    }

    private void initPackingWithPairs() {

        Cycle bestPair;
        while((bestPair = packingGraph.getBestPairCycle()) != null) {

            if(bestPair.get(0).getMinInOut() > 1 && bestPair.get(1).getMinInOut() > 1) {
                // Look for fully connected triangles, quads etc.
                PackingRules.upgradeFullyConnected(bestPair, packingGraph);

                if(bestPair.size() == 2) {
                    Cycle upgrade = PackingRules.lookForFullyConnected(bestPair, packingGraph);
                    if(upgrade != null) bestPair = upgrade;
                }
            }

            for (Node node : bestPair.getNodes()) {
                if(!node.forbidden) packingGraph.removeNode(node.id);
            }
            packing.add(bestPair);
        }
    }

    private void initPackingWithCycles(long endMillis) {
        // While cycles exist
        while(!bestCycles.isEmpty()) {

            // Find the best cycle to remove from the graph in the map
            Cycle bestCycle = null;
            for(Map.Entry<Integer,Cycle> entry: bestCycles.entrySet()) {
                Cycle cycle = entry.getValue();
                if(bestCycle == null || cycle.size() < bestCycle.size() ||(cycle.size() == bestCycle.size() && cycle.getMinInOutSum() < bestCycle.getMinInOutSum())) {
                    bestCycle = cycle;
                }
            }

            // Add the cycle to the packing and remove its nodes from the graph
            packing.add(bestCycle);
            for (Node node : bestCycle.getNodes()) {
                bestCycles.remove(node.id);
                if(!node.forbidden) packingGraph.removeNode(node.id);
            }

            // Remove cycles from the map that contained one of the removed nodes
            List<Integer> updateIds = new ArrayList<>();
            for(Map.Entry<Integer,Cycle> entry: bestCycles.entrySet()) {
                Cycle cycle = entry.getValue();
                for(Node node: bestCycle.getNodes()) {
                    if(cycle.contains(node)) {
                        updateIds.add(entry.getKey());
                    }
                }
            }

            // For ids which cycles were destroyed, recalculate them
            for(Integer id: updateIds) {
                Node node = packingGraph.getNode(id);
                if(node.getMinInOut() == 0) { // If the node is a sink/source, declare it was acyclic
                    node.acyclic = true;
                    bestCycles.remove(node.id);
                    continue;
                }
                // When the time limit is reached, don't add ne cycles to the map
                if(System.currentTimeMillis() > endMillis) {
                    bestCycles.remove(node.id);
                } else { // Else calculate a new cycle for the node
                    Cycle cycle = PackingBFS.findBestCycle(packingGraph, node);
                    if(cycle == null) {
                        node.acyclic = true;
                        bestCycles.remove(node.id);
                    } else {
                        bestCycles.put(node.id, cycle);
                    }
                }
            }
        }
        // When the time limit was reached, the packing could possibly be filled up
        if(System.currentTimeMillis() > endMillis) {
            while(!DAG.isDAGFast(packingGraph)) {
                Cycle cycle = LightBFS.run(packingGraph);

                for (Node node : cycle.getNodes()) {
                    if(!node.forbidden) packingGraph.removeNode(node.id);
                }
                packing.add(cycle);
            }
        }
    }

    public void updatePacking() {

        // Fill the packing with pair cycles
        updatePackingWithPairs();

        // Calculate shortest cycles for new nodes in the packing
        for(Node node: packingGraph.getNodes()) {
            if(!node.pNew) continue;
            Cycle cycle = PackingBFS.findBestCycle(packingGraph, node);
            if(cycle != null) {
                bestCycles.put(node.id, cycle);
            }
        }

        // Fill the packing with other cycles
        initPackingWithCycles(Integer.MAX_VALUE);
    }

    private void updatePackingWithPairs() {

        Cycle bestPair;
        while((bestPair = packingGraph.getBestUpdatePair()) != null) {

            if(bestPair.get(0).getMinInOut() > 1 && bestPair.get(1).getMinInOut() > 1) {
                // Look for fully connected triangles, quads etc.
                PackingRules.upgradeFullyConnectedByLevel(bestPair, packingGraph);

                if(bestPair.size() == 2) {
                    Cycle upgrade = PackingRules.lookForFullyConnected(bestPair, packingGraph);
                    if(upgrade != null) bestPair = upgrade;
                }
            }

            for (Node node : bestPair.getNodes()) {
                if(!node.forbidden) packingGraph.removeNode(node.id);
            }
            packing.add(bestPair);
        }
    }
}

