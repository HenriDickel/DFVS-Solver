package com.company.util;

import com.company.Graph;
import com.company.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class TarjanAlgorithm {

    private static int index;
    private static Stack<Node> stack;

    /**
     * Finds the first circle in the graph by using the Tarjan's Algorithm.
     * @param graph The graph.
     * @return The nodes that make up the first found cycle.
     */
    /*public static List<Node> findFirstCircle(Graph graph) {

        index = 0;
        stack = new Stack<>();

        // Initialize attributes
        for(Node node: graph.getActiveNodes()) {
            node.index = null;
            node.lowLink = null;
            node.onStack = false;
        }

        for(Node node: graph.getActiveNodes()) {
            if(node.index == null) {
                List<Node> component = strongConnect(node);
                if(component != null) {
                    return component;
                }
            }
        }

        return null;
    }

    private static List<Node> strongConnect(Node node) {

        node.index = index;
        node.lowLink = index;
        index++;
        stack.push(node);
        node.onStack = true;

        for(Node out: node.getOutNeighbours()) {
            if(out.index == null) {
                List<Node> component = strongConnect(out);
                if(component != null) return component;
                node.lowLink = Math.min(node.lowLink, out.lowLink);
            } else if(out.onStack) {
                node.lowLink = Math.min(node.lowLink, out.index);
            }
        }

        if(node.lowLink.equals(node.index)) {
            List<Node> component = new ArrayList<>();
            Node nodeFromStack;
            do {
                nodeFromStack = stack.pop();
                nodeFromStack.onStack = false;
                component.add(nodeFromStack);
            } while(!nodeFromStack.equals(node));
            if(component.size() > 1) return component;
        }

        return null;
    }*/
}
