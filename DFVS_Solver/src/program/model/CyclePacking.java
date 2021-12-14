package program.model;

import program.algo.LightBFS;
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

        // Add pair cycles to the packing as long as a cycle is found
        boolean pairFound = true;
        while(pairFound && size() <= k) {

            // Look for pair cycle to remove
            Cycle cycle = packingGraph.getFirstPairCycle();

            if(cycle != null) {
                // Look for fully connected triangle
                Node a = cycle.get(0);
                Node b = cycle.get(1);
                for(Integer cId: a.getOutIds()) {
                    if(a.getInIds().contains(cId)) { // b <-> node <-> a
                        if(b.getOutIds().contains(cId) && b.getInIds().contains(cId)) { // fully connected triangle found
                            cycle = new Cycle(a, b, packingGraph.getNode(cId));
                            cycle.setK(2); // set k = 2 for fully connected triangle
                            break;
                        }
                    }
                }

                for (Node node : cycle.getNodes()) {
                    packingGraph.removeNode(node.id);
                }
                cycles.add(cycle);

                List<Integer> reduceS = Reduction.applyRules(packingGraph, false);
                for(Integer nodeId: reduceS) {
                    // Add dummy node
                    cycles.add(new Cycle(new Node(nodeId)));
                }
            } else {
                pairFound = false;
            }
        }

        // Add cycles to the packing as long as a cycle is found
        boolean cycleFound = true;
        while(cycleFound && cycles.size() <= k) {

            // Look for cycle to remove
            Cycle cycle = LightBFS.findShortestCycle(packingGraph);

            if(cycle != null) {
                for (Node node : cycle.getNodes()) {
                    packingGraph.removeNode(node.id);
                }
                cycles.add(cycle);

                List<Integer> reduceS = Reduction.applyRules(packingGraph, false);
                for(Integer nodeId: reduceS) {
                    // Add dummy node
                    cycles.add(new Cycle(new Node(nodeId)));
                }
            } else {
                cycleFound = false;
            }
        }
    }
}
