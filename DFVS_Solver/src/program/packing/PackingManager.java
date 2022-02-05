package program.packing;

import program.algo.*;
import program.log.Log;
import program.model.Component;
import program.model.Cycle;
import program.model.Graph;
import program.model.Node;
import program.utils.PerformanceTimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackingManager {

    private Map<Integer, Node> initialNodes;
    private Graph packingGraph;
    private List<Cycle> packing = new ArrayList<>();

    private Map<Integer, Cycle> bestCycles = new HashMap<>();

    public PackingManager(Graph initialGraph) {
        // Copy nodes
        Graph copy = initialGraph.copy();
        initialNodes = copy.getNodeMap();
        // Copy into packing graph
        packingGraph = initialGraph.copy();

        initPacking(10);
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

        for(Integer forbiddenId: forbiddenIds) {
            if(!packingGraph.hasNode(forbiddenId)) {
                Node forbiddenNode = initialNodes.get(forbiddenId);
                forbiddenNode.forbidden = true;
                packingGraph.addInitialNode(forbiddenNode);
            } else {
                Node forbiddenNode = packingGraph.getNode(forbiddenId);
                forbiddenNode.forbidden = true;
            }
        }
        fillPacking();
    }

    public void addDeletedNodes(List<Integer> deletedIds) {
        // Re-add deleted ids
        for(Integer deletedId: deletedIds) {
            Node initialNode = initialNodes.get(deletedId).copy();
            packingGraph.addInitialNode(initialNode);
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

    private void initPacking(int timeLimit) {

        long endMillis = System.currentTimeMillis() + timeLimit * 1000L;
        Cycle pair;
        while((pair = packingGraph.getFirstPairCycle()) != null) {

            // Look for fully connected triangles, quads etc.
            PackingRules.upgradeFullyConnected(pair, packingGraph);
            if(pair.size() == 2) PackingRules.upgradeK2Quad(pair, packingGraph);


            for (Node node : pair.getNodes()) {
                packingGraph.removeNode(node.id);
            }
            packing.add(pair);
        }

        Log.debugLog("Packing", "Calculate best cycles...");
        // Initialize bestCycles with the best cycle for each node
        for(Node node: packingGraph.getNodes()) {
            Cycle cycle = SimpleBFS.findBestCycle(packingGraph, node, 3);
            if(cycle != null) {
                bestCycles.put(node.id, cycle);
            } else {
                node.acyclic = true;
            }
        }

        Log.debugLog("Packing", "Fill packing...");
        while(!bestCycles.isEmpty() && System.currentTimeMillis() < endMillis) {

            // Find the best cycle to remove from the graph in bestCycles
            Cycle bestCycle = null;
            for(Map.Entry<Integer,Cycle> entry: bestCycles.entrySet()) {
                Cycle cycle = entry.getValue();
                if(bestCycle == null || cycle.size() < bestCycle.size() ||(cycle.size() == bestCycle.size() && cycle.getMinInOutSum() < bestCycle.getMinInOutSum())) {
                    bestCycle = cycle;
                }
            }

            // Remove nodes from packing graph and add cycle to the packing
            for (Node node : bestCycle.getNodes()) {
                bestCycles.remove(node.id);
                packingGraph.removeNode(node.id);
            }
            packing.add(bestCycle);

            // Remove cycles from bestCycles that contained one of the nodes
            List<Integer> updateIds = new ArrayList<>();
            for(Map.Entry<Integer,Cycle> entry: bestCycles.entrySet()) {
                Cycle cycle = entry.getValue();
                for(Node node: bestCycle.getNodes()) {
                    if(cycle.contains(node)) {
                        updateIds.add(entry.getKey());
                    }
                }
            }

            for(Integer id: updateIds) {
                Node node = packingGraph.getNode(id);
                if(node.getMinInOut() == 0) {
                    node.acyclic = true;
                    bestCycles.remove(node.id);
                    continue;
                }
                Cycle cycle = SimpleBFS.findBestCycle(packingGraph, node, 3);
                if(cycle == null) {
                    node.acyclic = true;
                    bestCycles.remove(node.id);
                } else {
                    bestCycles.put(node.id, cycle);
                }
            }
        }
        // When the time limit is reached, the packing needs to be filled up
        if(System.currentTimeMillis() > endMillis) {
            fillPacking();
        }
    }

    public void fillPacking() {

        Cycle pair;
        while((pair = packingGraph.getFirstPairCycle()) != null) {

            // Look for fully connected triangles, quads etc.
            PackingRules.upgradeFullyConnected(pair, packingGraph);
            if(pair.size() == 2) PackingRules.upgradeK2Quad(pair, packingGraph);


            for (Node node : pair.getNodes()) {
                if(!node.forbidden) packingGraph.removeNode(node.id);
            }
            packing.add(pair);
        }
        while(!isDAG()) {
            PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
            PerformanceTimer.start();
            Cycle cycle = LightBFS.run(packingGraph);
            PerformanceTimer.log(PerformanceTimer.MethodType.PACKING_BFS);
            PerformanceTimer.start();

            if(cycle.size() == 3) PackingRules.upgradeK2Penta(cycle, packingGraph);

            for (Node node : cycle.getNodes()) {
                if(!node.forbidden) packingGraph.removeNode(node.id);
            }
            packing.add(cycle);
        }
    }

    private boolean isDAG() {
        PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
        PerformanceTimer.start();
        boolean isDAG = DAG.isDAGFast(packingGraph);
        PerformanceTimer.log(PerformanceTimer.MethodType.PACKING_DAG);
        PerformanceTimer.start();
        return isDAG;
    }
}

