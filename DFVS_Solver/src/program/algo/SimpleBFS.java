package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.LinkedList;
import java.util.List;

public class SimpleBFS {

    private final List<Node> queue = new LinkedList<>();

    public Cycle findBestCycle(Graph graph, Node root, int maxBranchSize) {

        // Reset node attributes
        graph.resetBFS();
        queue.clear();
        queue.add(root);
        root.visitIndex = 0;

        while(!queue.isEmpty()) {
            String q = queue.toString();
            Node nextNode = null;
            try {
                nextNode = queue.remove(0);
            } catch (Exception e) {
                System.out.println("Queue: " + q);
                return null;
            }
            if(nextNode.visitIndex >= maxBranchSize) return null;
            Cycle cycle = visitNode(graph, nextNode, root);
            if(cycle != null) return cycle;
        }
        return null;
    }

    private Cycle visitNode(Graph graph, Node node, Node root) {
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
}
