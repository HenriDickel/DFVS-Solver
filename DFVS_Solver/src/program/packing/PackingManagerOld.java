package program.packing;

import program.algo.*;
import program.model.Component;
import program.model.Cycle;
import program.model.Graph;
import program.model.Node;
import program.utils.PerformanceTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackingManagerOld {

    /*private Map<Integer, Node> initialNodes;
    private Graph packingGraph;

    private List<Cycle> packing = new ArrayList<>();

    public PackingManagerOld(Graph initialGraph) {
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

    public PackingManagerOld(PackingManagerOld oldPm, List<Integer> deleteIds, List<Integer> forbiddenIds) {
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

        while(!DAG.isDAGFast(packingGraph)) {
            Cycle cycle = LightBFS.run(packingGraph);

            if(cycle.size() == 3) PackingRules.upgradeK2Penta(cycle, packingGraph);

            for (Node node : cycle.getNodes()) {
                if(!node.forbidden) packingGraph.removeNode(node.id);
            }
            packing.add(cycle);
        }
    }*/
}
