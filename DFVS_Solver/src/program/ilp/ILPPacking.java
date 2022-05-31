package program.ilp;

import program.algo.DAG;
import program.algo.SimpleBFS;
import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ILPPacking {

    Graph packingGraph;
    List<Cycle> cycles = new ArrayList<>();

    public ILPPacking(Graph graph) {
        packingGraph = graph.copy();
        initPacking();
    }

    private void initPacking() {
        // While cycles exist
        while(!DAG.isDAGFast(packingGraph)) {
            List<Node> nodes = packingGraph.getNodes().stream().sorted(Comparator.comparing(Node::getMinInOut)).collect(Collectors.toList());
            Collections.reverse(nodes);
            for(Node node: nodes) {
                Cycle cycle = SimpleBFS.findBestCycle(packingGraph, node, Integer.MAX_VALUE);
                if(cycle != null) {
                    cycles.add(cycle);
                    for(Node cNode: cycle.getNodes()) {
                        packingGraph.removeNode(cNode.id);
                    }
                    break;
                } else {
                    packingGraph.removeNode(node.id);
                }
            }
        }
    }
}
