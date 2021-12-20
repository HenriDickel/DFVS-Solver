package program.model;

import program.algo.DAG;
import program.algo.LightBFS;
import program.algo.PackingRules;
import program.algo.Reduction;

import java.util.ArrayList;
import java.util.List;

public class CyclePacking {

    private final List<Cycle> cycles = new ArrayList<>();

    private final Graph packingGraph;

    public CyclePacking(Graph packingGraph) {
        this.packingGraph = packingGraph;
        initPacking(Integer.MAX_VALUE);
    }

    public CyclePacking(Graph packingGraph, int k) {
        this.packingGraph = packingGraph;
        initPacking(k);
    }

    public int size() {
        return cycles.stream().mapToInt(Component::getK).sum();
    }

    private void initPacking(int k) {

        Cycle pair;
        while((pair = packingGraph.getFirstPairCycle()) != null && size() <= k) {

            // Look for fully connected triangles, quads etc.
            PackingRules.upgradeFullyConnected(pair, packingGraph);
            if(pair.size() == 2) PackingRules.upgradeK2Quad(pair, packingGraph);

            for (Node node : pair.getNodes()) {
                packingGraph.removeNode(node.id);
            }
            cycles.add(pair);

            List<Integer> reduceS = Reduction.applyRules(packingGraph, false);
            for(Integer nodeId: reduceS) {
                // Add dummy node
                cycles.add(new Cycle(new Node(nodeId)));
            }

        }

        // Add cycles to the packing as long as a cycle is found
        while(!DAG.isDAGFast(packingGraph) && size() <= k) {
            Cycle cycle = LightBFS.findShortestCycle(packingGraph);

            if(cycle.size() == 3) PackingRules.upgradeTriforce(cycle, packingGraph);
            if(cycle.size() == 3) PackingRules.upgradeK2Penta(cycle, packingGraph);

            for (Node node : cycle.getNodes()) {
                packingGraph.removeNode(node.id);
            }
            cycles.add(cycle);

            List<Integer> reduceS = Reduction.applyRules(packingGraph, false);
            for(Integer nodeId: reduceS) {
                // Add dummy node
                cycles.add(new Cycle(new Node(nodeId)));
            }
        }
    }
}
