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

    public PackingManager(PackingManager oldPm, List<Integer> deleteIds, List<Integer> forbiddenIds) {
        this.initialNodes = oldPm.initialNodes;
        this.packingGraph = oldPm.packingGraph.copy();

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
                        initialNode.pNew = true;
                        this.packingGraph.addInitialNode(initialNode);
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
                packingGraph.addInitialNode(forbiddenNode);
            } else {
                Node forbiddenNode = packingGraph.getNode(forbiddenId);
                forbiddenNode.forbidden = true;
                forbiddenNode.pNew = true;
            }
        }
        updatePacking();
        //fillPackingWithPairs();
        //fillPackingWithLightBFS();
    }

    public void addDeletedNodes(List<Integer> deletedIds) {
        // Re-add deleted ids
        for(Integer deletedId: deletedIds) {
            Node initialNode = initialNodes.get(deletedId).copy();
            packingGraph.addInitialNode(initialNode);
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

    public void updatePacking() {

        // Fill the packing with pair cycles
        fillPackingWithPairs();

        // Calculate shortest cycles for new nodes in the packing
        for(Node node: packingGraph.getNodes()) {
            if(!node.pNew) continue;
            Cycle cycle = PackingBFS.findBestCycle(packingGraph, node);
            if(cycle != null) {
                bestCycles.put(node.id, cycle);
            }
        }

        // Fill the packing with other cycles
        fillPackingWithCycles(Integer.MAX_VALUE);
    }

    private void initPacking(long timeLimit) {

        long endMillis = System.currentTimeMillis() + timeLimit;

        // Fill the packing with pair cycles
        fillPackingWithPairs();

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
        fillPackingWithCycles(endMillis);
    }

    private void fillPackingWithPairs() {
        // First add pair cycles to the packing
        Cycle pair;
        while((pair = packingGraph.getFirstPairCycle()) != null) {

            // Look for fully connected triangles, quads etc.
            PackingRules.upgradeFullyConnected(pair, packingGraph);
            //PackingRules.upgradeK2Quad(pair, packingGraph);

            for (Node node : pair.getNodes()) {
                packingGraph.removeNode(node.id);
            }
            packing.add(pair);
        }
    }

    private void fillPackingWithCycles(long endMillis) {
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
            if(bestCycle.size() == 3) PackingRules.upgradeK2Penta(bestCycle, packingGraph);

            // Add the cycle to the packing and remove its nodes from the graph
            packing.add(bestCycle);
            for (Node node : bestCycle.getNodes()) {
                bestCycles.remove(node.id);
                packingGraph.removeNode(node.id);
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
            fillPackingWithLightBFS();
        }
    }

    private void fillPackingWithLightBFS() {

        while(!DAG.isDAGFast(packingGraph)) {
            Cycle cycle = LightBFS.run(packingGraph);

            for (Node node : cycle.getNodes()) {
                if(!node.forbidden) packingGraph.removeNode(node.id);
            }
            packing.add(cycle);
        }
    }
}

