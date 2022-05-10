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

        improvePairPacking();

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

    private Cycle getPackingCycle(Integer nodeId) {
        for(Cycle cycle: packing) {
            if (cycle.containsId(nodeId)) {
                return cycle;
            }
        }
        return null;
    }

    private Cycle getICycle(Cycle cycle) {
        Cycle iCycle = new Cycle();
        for (Node node : cycle.getNodes()) {
            iCycle.add(initialNodes.get(node.id));
        }
        iCycle.setK(cycle.getK());
        return iCycle;
    }

    private void improvePairPacking() {

        int remainingNodeSize = 0;
        while(remainingNodeSize != packingGraph.getNodeCount()) {
            remainingNodeSize = packingGraph.getNodeCount();

            Node remove = null;
            Cycle cycle1 = null;
            Cycle cycle2 = null;
            mainloop:
            for(Node node: packingGraph.getNodes()) {
                Node iNode = initialNodes.get(node.id);

                List<Cycle> iCycles = new ArrayList<>();
                for(Integer otherId: iNode.getFullyConnectedIds()) {
                    if(iCycles.stream().noneMatch(e -> e.containsId(otherId))) {
                        Cycle cycle = getPackingCycle(otherId);
                        if(cycle != null) {
                            Cycle iCycle = getICycle(cycle);
                            if(iCycle.isClique()) {
                                iCycles.add(iCycle);
                            }
                        }
                    }
                }

                for(Cycle iCycle: iCycles) {
                    for(Node cNode: iCycle.getNodes()) {
                        cNode.marked = false;
                        if(iNode.hasDoubleEdge(cNode.id)) { // mark all nodes connected to iNode
                            cNode.marked = true;
                        }
                    }
                }

                for(Cycle iCycle: iCycles) {
                    List<Node> unmarked = iCycle.getNodes().stream().filter(e -> !e.marked).collect(Collectors.toList());
                    if(unmarked.size() == 1) {
                        for(Cycle iCycle2: iCycles) {
                            if(iCycle2.equals(iCycle)) continue;
                            List<Node> unmarked2 = iCycle2.getNodes().stream().filter(e -> !e.marked).collect(Collectors.toList());

                            if(unmarked2.size() == 1) {
                                Node a = unmarked.get(0);
                                Node b = unmarked2.get(0);
                                if(a.hasDoubleEdge(b.id)) {
                                    remove = node;
                                    cycle1 = getPackingCycle(a.id);
                                    cycle2 = getPackingCycle(b.id);
                                    break mainloop;
                                }
                            }
                        }
                    }
                }
            }

            if(remove != null && cycle1 != null && cycle2 != null) {
                packingGraph.removeNode(remove.id);
                packing.remove(cycle1);
                packing.remove(cycle2);

                Cycle newCycle = new Cycle(remove);
                newCycle.addAll(cycle1.getNodes());
                newCycle.addAll(cycle2.getNodes());
                newCycle.setK(cycle1.getK() + cycle2.getK() + 1);
                packing.add(newCycle);
            }
        }
    }

    /**
     * Improve packing by merging two pairs to a cycle of 5
     */
    private void improvePacking2() {
        for(Cycle cycle: packing) {
            if(cycle.size() > 2) continue;

            // Create the initial cycle
            Cycle iCycle = new Cycle();
            for(Node node: cycle.getNodes()) {
                iCycle.add(initialNodes.get(node.id));
            }
            Node a = iCycle.get(0);
            Node b = iCycle.get(1);

            Cycle mergeCycle = null;
            Node mergeNode = null;

            loop:
            for(Node remainingNode: packingGraph.getNodes()) {
                if(a.hasDoubleEdge(remainingNode.id)) {
                    Node iRemainingNode = initialNodes.get(remainingNode.id);
                    for(Integer outId: iRemainingNode.getFullyConnectedIds()) {
                        if(outId.equals(a.id) || outId.equals(b.id)) continue;

                        for(Cycle otherCycle: packing) {
                            if(otherCycle.containsId(outId)) {
                                if(otherCycle.size() > 2) break;

                                Integer otherId = (otherCycle.get(0).id.equals(outId)) ? otherCycle.get(1).id : otherCycle.get(0).id;

                                if(b.hasDoubleEdge(otherId)) {
                                    mergeCycle = otherCycle;
                                    mergeNode = remainingNode;
                                    break loop;
                                }
                            }
                        }
                    }
                } else if (b.hasDoubleEdge(remainingNode.id)) {
                    Node iRemainingNode = initialNodes.get(remainingNode.id);
                    for(Integer outId: iRemainingNode.getFullyConnectedIds()) {
                        if(outId.equals(a.id) || outId.equals(b.id)) continue;

                        for(Cycle otherCycle: packing) {
                            if(otherCycle.containsId(outId)) {
                                if(otherCycle.size() > 2) break;

                                Integer otherId = (otherCycle.get(0).id.equals(outId)) ? otherCycle.get(1).id : otherCycle.get(0).id;

                                if(a.hasDoubleEdge(otherId)) {
                                    mergeCycle = otherCycle;
                                    mergeNode = remainingNode;
                                    break loop;
                                }
                            }
                        }
                    }
                }
            }

            if(mergeCycle != null) {
                System.out.println("Merge found");
            }
        }
    }

    private void tryImprovePacking() {
        for(Cycle cycle: packing) {
            Cycle initialCycle = new Cycle();
            for(Node node: cycle.getNodes()) {
                initialCycle.add(initialNodes.get(node.id));
            }

            for(Node other: initialNodes.values()) {
                if(initialCycle.contains(other)) continue;

                if(initialCycle.isConnected(other.id)) {

                    for(Cycle otherCycle: packing) {
                        if(otherCycle.containsId(other.id)) {

                            for(Node node: otherCycle.getNodes()) {
                                initialCycle.add(initialNodes.get(node.id));
                            }

                            Graph merge = new Graph();
                            for(Node node: initialCycle.getNodes()) {
                                for(Integer outId: node.getOutIds()) {
                                    if(initialCycle.containsId(outId)) {
                                        merge.addArc(node.id, outId);
                                    }
                                }
                            }
                            List<Integer> S = SimpleSolver.dfvsSolve(merge);
                            if(S.size() > cycle.getK() + otherCycle.getK()) {
                                System.out.println("Upgrade found!");
                            }
                        }
                    }
                }
            }
        }
    }

    private void fillPackingWithPairs() {

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
                packingGraph.removeNode(node.id);
            }
            packing.add(bestPair);
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




    /*private void upgradePacking() {

        boolean updateFound = true;
        while(updateFound) {
            Cycle add = null;
            Cycle remove = null;
            for(Cycle packingCycle: packing) {
                if(packingCycle.getK() > 1) continue;
                for(Node node: packingCycle.getNodes()) {
                    Node initialNode = initialNodes.get(node.id);
                    packingGraph.addInitialNode(initialNode);
                }
                List<Cycle> Scycles = SimpleSolver.dfvsSolve(packingGraph.copy());
                System.out.println(Scycles.size());

                if(Scycles.size() <= packingCycle.getK()) {
                    for (Node node : packingCycle.getNodes()) {
                        packingGraph.removeNode(node.id);
                    }
                } else {
                    Cycle newCycle = new Cycle();
                    newCycle.setK(Scycles.size());
                    for(Cycle Scycle: Scycles) {
                        newCycle.addAll(Scycle.getNodes());
                    }
                    add = newCycle;
                    remove = packingCycle;
                    break;
                }
            }

            updateFound = false;
            if(add != null) {
                System.out.println("Add new cycle with k = " + add.getK() + " instead of " + remove.getK());
                System.out.println(packingGraph.getNodeCount());
                packing.add(add);
                for(Node node: add.getNodes()) {
                    packingGraph.removeNode(node.id);
                }

                packing.remove(remove);
                // was already added to packing graph
                updateFound = true;
            }
        }
    }*/
}

