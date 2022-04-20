package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.*;

public class TarjanIterative {

    // Iteration variables to replace recursion
    private static LinkedList<Node> nodesToVisit;
    private static Stack<Node> nodesToCheck;

    // Tarjan variables
    private static int index;
    private static Stack<Node> stack;
    private static List<List<Node>> components;

    /**
     * Finds the strong connected components in the graph by using the Tarjan's Algorithm.
     * @param graph The graph.
     * @return The strong connected components.
     */
    public static List<List<Node>> run(Graph graph) {

        nodesToVisit = new LinkedList<>();
        nodesToCheck = new Stack<>();

        index = 0;
        stack = new Stack<>();
        components = new ArrayList<>();

        // Initialize attributes
        for(Node node: graph.getNodes()) {
            node.index = -1;
            node.lowLink = -1;
            node.depth = -1;
            node.onStack = false;
        }

        // Loop over all nodes, skip the ones already visited (index != -1)
        for(Node node: graph.getNodes()) {
            if(node.index == -1) {
                node.depth = 0;
                nodesToVisit.add(node);

                while(!nodesToVisit.isEmpty() || !nodesToCheck.isEmpty()) {

                    if(nodesToCheck.isEmpty() || !nodesToVisit.isEmpty() && nodesToVisit.get(0).depth > nodesToCheck.peek().depth) {
                        Node visit = nodesToVisit.removeFirst();
                        visitNode(visit, graph);
                        nodesToCheck.add(visit);
                    } else {
                        Node check = nodesToCheck.pop();
                        checkNode(check, graph);
                    }
                }
            }
        }

        return components;
    }

    private static void checkNode(Node node, Graph graph) {

        for(Integer outId: node.getOutIds()) {
            Node out = graph.getNode(outId);
            if(out.onStack) {
                if(out.index < node.index) {
                    node.lowLink = Math.min(node.lowLink, out.index);
                } else {
                    node.lowLink = Math.min(node.lowLink, out.lowLink);
                }
            }
        }

        // If lowLink == index, all nodes of the component are found. The component is build from the nodes on the stack and is stored in "components"
        if(node.lowLink.equals(node.index)) {
            List<Node> component = new ArrayList<>();
            Node nodeFromStack;
            do {
                nodeFromStack = stack.pop();
                nodeFromStack.onStack = false;
                component.add(nodeFromStack);
            } while(!nodeFromStack.equals(node));
            components.add(component);
        }
    }

    /**
     * Starts at the given node and looks for all nodes which are connected by a cycle.
     * Stores the found cyclic component in "components".
     * @param node The start node.
     */
    private static void visitNode(Node node, Graph graph) {

        // Set the node's index to the current index and increment it
        node.index = index;
        node.lowLink = index;
        index++;
        stack.push(node);
        node.onStack = true;

        for(Integer outId: node.getOutIds()) {
            Node out = graph.getNode(outId);
            if(out.index == -1) {
                out.depth = node.depth + 1;
                nodesToVisit.remove(out); // Remove node when it was added before (DFS)
                nodesToVisit.add(0, out); // Add out node at 0 (DFS)
            }
        }
    }
}
