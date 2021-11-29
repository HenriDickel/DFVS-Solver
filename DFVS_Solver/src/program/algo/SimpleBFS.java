package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.LinkedList;
import java.util.List;

public abstract class SimpleBFS {

    private static final List<Node> queue = new LinkedList<>();

    public static Cycle findBestCycle(Graph graph, Node root) {

        // Reset node attributes
        graph.resetBFS();
        queue.clear();
        queue.add(root);

        while(!queue.isEmpty()) {
            Cycle cycle = visitNode(queue.remove(0), root);
            if(cycle != null) return cycle;
        }
        return null;
    }

    private static Cycle visitNode(Node node, Node root) {
        for(Node outNeighbor: node.getOutNeighbors()) {
            if(outNeighbor.equals(root)) {
                return pathToRoot(node, root);
            } else if(outNeighbor.parent == null){
                outNeighbor.parent = node;
                if(outNeighbor.forbidden < Integer.MAX_VALUE) queue.add(0, outNeighbor);
                else queue.add(outNeighbor);
            }
        }
        return null;
    }

    private static Cycle pathToRoot(Node node, Node root) {
        Cycle cycle = new Cycle(root);
        Node pointer = node;
        while(!pointer.equals(root)) {
            cycle.add(pointer);
            pointer = pointer.parent;
        }
        return cycle;
    }
}
