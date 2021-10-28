package program.utils;

import program.Graph;
import program.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class FirstCycle {

    private static int index;
    private static int cycleStartIndex;

    public static List<Node> findFirstCycle(Graph graph) {

        index = 0;
        for(Node node: graph.getActiveNodes()) {
            graph.unvisitAllNodes();
            if(node.visitIndex == -1) {
                List<Node> cycle = visitNode(node);
                if(cycle != null) return cycle;
            }
        }
        return null;
    }

    private static List<Node> visitNode(Node node) {

        if(node.visitIndex != -1) {
            List<Node> pathToStart = new ArrayList<>();
            pathToStart.add(node);
            cycleStartIndex = node.visitIndex;
            return pathToStart;
        } else {
            node.visitIndex = index;
            index++;

            for (Node out : node.getOutNeighbours()) {
                List<Node> pathToStart = visitNode(out);
                if(pathToStart != null) {
                    if(node.visitIndex > cycleStartIndex) pathToStart.add(node);
                    node.visitIndex = -1;
                    return pathToStart;
                }
            }

            node.visitIndex = -1;
            return null;
        }
    }
}
