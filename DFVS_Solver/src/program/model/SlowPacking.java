package program.model;

import program.algo.DAG;
import program.algo.LightBFS;

import java.util.ArrayList;
import java.util.List;

public class SlowPacking {

    public final List<Cycle> cycles = new ArrayList<>();

    private final Graph packingGraph;

    public SlowPacking(Graph packingGraph) {
        this.packingGraph = packingGraph;
        initPacking();
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

    private void initPacking() {

        Cycle pair;
        while((pair = packingGraph.getFirstPairCycle()) != null) {

            // Look for fully connected triangles, quads etc.
            upgradeFullyConnected(pair);

            for (Node node : pair.getNodes()) {
                packingGraph.removeNode(node.id);
            }
            cycles.add(pair);
        }

        while(!DAG.isDAGFast(packingGraph)) {
            Cycle cycle = new LightBFS().run(packingGraph);

            if(cycle.size() == 3) upgradeK2Penta(cycle);

            for (Node node : cycle.getNodes()) {
                packingGraph.removeNode(node.id);
            }
            cycles.add(cycle);
        }
    }
}
