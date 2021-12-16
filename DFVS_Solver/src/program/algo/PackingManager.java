package program.algo;

import program.model.Component;
import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackingManager {

    private Map<Integer, Node> initialNodes;
    private Graph packingGraph;

    private List<Cycle> packing = new ArrayList<>();

    public PackingManager(Graph initialGraph) {
        // Copy nodes
        Graph copy = initialGraph.copy();
        initialNodes = copy.getNodeMap();
        // Copy into packing graph
        packingGraph = initialGraph.copy();

        initPacking();
    }

    public PackingManager(PackingManager oldPm, List<Integer> deleteIds) {
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
        initPacking();
    }

    private void fillWithCostTPacking() {
        List<Cycle> S = SimpleSolver.dfvsSolve(packingGraph.copy());
        Cycle comp = new Cycle();
        for(Cycle cycle: S) {
            for(Node node: cycle.getNodes()) {
                if(!comp.containsId(node.id)) {
                    comp.add(new Node(node.id));
                    packingGraph.removeNode(node.id);
                }
            }
        }
        comp.setK(S.size());
        packing.add(comp);
        //System.out.println("Add component " + comp + " with k = " + comp.getK());
        //System.out.println(S.size());
        //int startSize = size();
        //initPacking();
        //System.out.println("Improved packing: " + startSize + " -> " + size());
        //if(size() - startSize < S.size()) System.err.println("Didn't found the best packing");
    }

    public void addDeletedNodes(List<Integer> deletedIds) {
        // Re-add deleted ids
        for(Integer deletedId: deletedIds) {
            Node initialNode = initialNodes.get(deletedId).copy();
            packingGraph.addInitialNode(initialNode);
        }
        initPacking();
    }

    public int size() {
        return packing.stream().mapToInt(Component::getK).sum();
    }

    private void initPacking() {

        Cycle pair;
        while((pair = packingGraph.getFirstPairCycle()) != null) {

            // Look for fully connected triangles, quads etc.
            upgradeFullyConnected(pair);

            for (Node node : pair.getNodes()) {
                packingGraph.removeNode(node.id);
            }
            packing.add(pair);

        }

        while(!DAG.isDAGFast(packingGraph)) {
            Cycle cycle = LightBFS.findShortestCycle(packingGraph);

            if(cycle.size() == 3) upgradeK2Penta(cycle);

            for (Node node : cycle.getNodes()) {
                packingGraph.removeNode(node.id);
            }
            packing.add(cycle);
        }
    }

    private void upgradeFullyConnected(Cycle pair) {

        Node a = pair.get(0);
        boolean upgrade = true;
        while(upgrade) {
            upgrade = false;
            for(Integer outId: a.getOutIds()) {
                if(pair.isFullyConnected(outId)) {
                    Node newNode = packingGraph.getNode(outId);
                    pair.add(newNode);
                    pair.setK(pair.getK() + 1);
                    upgrade = true;
                    break;
                }
            }
        }
    }

    private void upgradeK2Penta(Cycle triangle) {
        for(int i = 0; i < 3; i++) {
            Node a = triangle.get(i);
            Node b = triangle.get((i + 1) % 3);
            Node c = triangle.get((i + 2) % 3);
            // Due to the structure of Light BFS, the cycle goes c -> b -> a

            for (Integer dId : a.getOutIds()) {
                if (b.getInIds().contains(dId)) { // a -> d -> b exists
                    if(c.getOutIds().contains(dId)) {
                        Node d = packingGraph.getNode(dId);
                        for(Integer eId: d.getOutIds()) {
                            if(c.getInIds().contains(eId)) {
                                Node e = packingGraph.getNode(eId);
                                triangle.add(d);
                                triangle.add(e);
                                triangle.setK(2);
                                return;
                            }
                        }
                    }
                    if(c.getInIds().contains(dId)) {
                        Node d = packingGraph.getNode(dId);
                        for(Integer eId: d.getInIds()) {
                            if(c.getOutIds().contains(eId)) {
                                Node e = packingGraph.getNode(eId);
                                triangle.add(d);
                                triangle.add(e);
                                triangle.setK(2);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks, if the graph has a cycle. Is more efficient than running a isDAG() on the not reduced graph!
     * @return true, when the graph has a cycle.
     */
    private boolean hasCycle() {
        Graph copy = packingGraph.copy();
        List<Integer> S = Reduction.applyRules(copy, true);
        return !S.isEmpty() || copy.getNodeCount() > 0;
    }
}
