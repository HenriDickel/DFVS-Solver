package program.algo;

import program.model.Component;
import program.model.Cycle;
import program.model.Graph;
import program.model.Node;
import program.utils.PerformanceTimer;

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
        initPacking();
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

    public int size() {
        return packing.stream().mapToInt(Component::getK).sum();
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

    public void initPacking() {

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
            Cycle cycle = new LightBFS().run(packingGraph);
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

    /*private Cycle nextCycleExists() {

        Node firstNode = packingGraph.getNodes().get(0);
        Cycle firstCycle = new PackingBFS().run(firstNode, packingGraph);

        if(firstCycle == null) {
            if(DAG.isDAGFast(packingGraph)) return null;
        }

        for(int i = 1; i < packingGraph.getNodeCount(); i++) {
            Node node = packingGraph.getNodes().get(i);
            Cycle cycle = new PackingBFS().run(node, packingGraph);
            if(cycle != null) return cycle;
        }
        return null;
    }*/
}
