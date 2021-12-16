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
                if (!triangle.containsId(dId) && b.getInIds().contains(dId)) { // a -> d -> b exists
                    if(c.getOutIds().contains(dId)) {
                        Node d = packingGraph.getNode(dId);
                        for(Integer eId: d.getOutIds()) {
                            if(!triangle.containsId(eId) && c.getInIds().contains(eId)) {
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

    private void initPacking(int k) {

        Cycle pair;
        while((pair = packingGraph.getFirstPairCycle()) != null && size() <= k) {

            // Look for fully connected triangles, quads etc.
            upgradeFullyConnected(pair);

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
        Cycle cycle;
        while((cycle = LightBFS.findShortestCycle(packingGraph)) != null && cycles.size() <= k) {

            if(cycle.size() == 3) upgradeK2Penta(cycle);

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
