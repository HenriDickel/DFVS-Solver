package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class TarjanAlgorithm {

    private static int index;
    private static Stack<Node> stack;
    private static List<List<Node>> components;

    /**
     * Finds the strong connected components in the graph by using the Tarjan's Algorithm.
     * @param graph The graph.
     * @return The strong connected components.
     */
    public static List<List<Node>> findComponents(Graph graph) {

        index = 0;
        stack = new Stack<>();
        components = new ArrayList<>();

        // Initialize attributes
        for(Node node: graph.getActiveNodes()) {
            node.index = -1;
            node.lowLink = -1;
            node.onStack = false;
        }

        // Loop over all nodes, skip the ones already visited (index != -1)
        for(Node node: graph.getActiveNodes()) {
            if(node.index == -1) {
                strongConnect(node);
            }
        }

        return components;
    }

    /**
     * Starts at the given node and looks for all nodes which are connected by a cycle.
     * Stores the found cyclic component in "components".
     * @param node The start node.
     */
    private static void strongConnect(Node node) {

        // Set the node's index to the current index and increment it.
        node.index = index;
        node.lowLink = index;
        index++;
        stack.push(node);
        node.onStack = true;

        // Loop over all out neighbors. Recursively call strongConnect() if they are not visited yet and update the lowLink-attribute
        // LowLink is the lowest index of all nodes in the component of the selected node
        for(Node out: node.getOutNeighbours()) {
            if(out.index == -1) {
                strongConnect(out);
                node.lowLink = Math.min(node.lowLink, out.lowLink);
            } else if(out.onStack) {
                node.lowLink = Math.min(node.lowLink, out.index);
            }
        }

        // If lowLink == index, all nodes of the component are found. The component is build from the nodes on the stack and is stored in "components".
        if(node.lowLink == node.index) {
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
}
