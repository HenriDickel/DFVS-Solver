package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SimpleBFS {

    private static final List<Node> queue = new LinkedList<>();

    public static Cycle findBestCycle(Graph graph, Node root, int maxBranchSize) {

        // Reset node attributes
        graph.resetBFS();
        queue.clear();
        queue.add(root);
        root.visitIndex = 0;

        while(!queue.isEmpty()) {
            Node nextNode = queue.remove(0);
            if(nextNode.visitIndex >= maxBranchSize) return null;
            Cycle cycle = visitNode(graph, nextNode, root);
            if(cycle != null) return cycle;
        }
        return null;
    }

    private static Cycle visitNode(Graph graph, Node node, Node root) {
        for(Integer outId: node.getOutIds()) {
            Node out = graph.getNode(outId);
            if(out.equals(root)) {
                return pathToRoot(node);
            } else if(out.parent == null){
                out.parent = node;
                out.visitIndex = node.visitIndex + 1;
                queue.add(out);
            }
        }
        return null;
    }

    private static Cycle pathToRoot(Node node) {

        Cycle cycle = new Cycle(node);
        Node pointer = node.parent;
        while(pointer != null) {
            cycle.add(pointer);
            pointer = pointer.parent;
        }
        return cycle;
    }

    public static List<Cycle> findBestCycles(Graph graph, Node root, int maxBranchSize) {
        // Reset node attributes
        graph.resetBFS();
        queue.clear();
        queue.add(root);
        root.visitIndex = 0;

        List<Cycle> cycles = new ArrayList<>();

        while(!queue.isEmpty()) {
            Node nextNode = queue.remove(0);

            // When max branch size is reached, return cycles
            if(nextNode.visitIndex >= maxBranchSize) return cycles;

            Cycle cycle = visitNode(graph, nextNode, root);
            if(cycle != null) { // First cycle found
                maxBranchSize = cycle.size();
                cycles.add(cycle);
            }
        }
        return cycles;
    }
}
