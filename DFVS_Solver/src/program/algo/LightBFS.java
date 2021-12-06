package program.algo;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.LinkedList;
import java.util.List;

public abstract class LightBFS {

    private final static List<Node> queue = new LinkedList<>();

    public static Cycle findShortestCycle(Graph graph) {

        for(Node node: graph.getNodes()) {

            // Reset node attributes
            queue.clear();
            graph.resetBFS();

            // Visit the start node
            node.visitIndex = 0;

            Cycle cycle = visitNode(node, graph);
            if(cycle != null) {
                return cycle;
            }

            while(!queue.isEmpty()) {
                Node next = queue.remove(0);
                next.visitIndex = next.parent.visitIndex + 1;
                cycle = visitNode(next, graph);
                if(cycle != null) {
                    return cycle;
                }
            }
        }
        return null;
    }

    /**
     * Visits a node from the queue. All outgoing neighbors are checked:
     * When they don't have a parent yet, they are added to the queue.
     * When they were already visited, it is checked, if a cycle exists.
     * @param node node A.
     * @return a cycle when found.
     */
    private static Cycle visitNode(Node node, Graph graph) {

        for(Integer outId: node.getOutIds()) {
            Node out = graph.getNode(outId);
            if(out.parent == null && out.visitIndex == -1) { // Node was not visited and is not in queue
                out.parent = node;
                queue.add(out);
            } else if(out.visitIndex > -1){ // Node is already visited
                Cycle cycle = findCycle(out, node);
                if(cycle != null) return cycle;
            } else {
                // Node is in queue, but not visited (= out neighbor is on same depth as node))
            }
        }
        return null;
    }

    /**
     * Called, when an edge B -> A to a visited node A is found.
     * Checks, if there is a path A -> ... -> B. If yes, a cycle is found.
     * @param first node A.
     * @param second node B.
     * @return cycle when found.
     */
    private static Cycle findCycle(Node first, Node second) {

        Node pointer = second.parent;
        Cycle cycle = new Cycle(second);

        if(first.equals(second)) return cycle; // For self-edges

        while(pointer != null) {
            cycle.add(pointer);
            if(pointer.equals(first)) {
                return cycle;
            } else {
                pointer = pointer.parent;
            }
        }
        return null;
    }
}
