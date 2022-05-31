package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PackingRules {

    public static void upgradeFullyConnected(Cycle pair, Graph packingGraph) {

        Node a = pair.get(0);
        boolean upgrade = true;
        Node bestNode = null;
        while(upgrade) {
            upgrade = false;
            List<Node> outNodes = a.getOutIds().stream().map(packingGraph::getNode).collect(Collectors.toList());
            outNodes.sort(Comparator.comparing(Node::getMinInOut));
            for(Node out: outNodes) {
                if(pair.isFullyConnected(out.id)) {
                    if(bestNode == null) {
                        bestNode = out;
                    }
                }
            }
            // When there is an upgrade node, upgrade cycle
            if(bestNode != null) {
                pair.add(bestNode);
                pair.setK(pair.getK() + 1);
                upgrade = true;
                bestNode = null;
            }
        }
    }
}
