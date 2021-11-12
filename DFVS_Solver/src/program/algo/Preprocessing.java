package program.algo;

import program.model.Graph;
import program.model.Instance;
import program.model.Node;
import program.log.Log;
import program.model.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Preprocessing {

    /**
     * Splits up the graph into it's cyclic components by using the Tarjan's algorithm.
     */
    public static List<Graph> findCyclicSubGraphs(String name, Graph graph) {

        // Find cyclic components
        List<List<Node>> components = Tarjan.run(graph);

        // Generate sub graphs
        List<Graph> subGraphs = new ArrayList<>();
        for(List<Node> component: components) {
            if(component.size() > 1) {
                Graph subGraph = new Graph();
                for(Node node: component) {
                    for(Node out: node.getOutNeighbors()) {
                        if(component.contains(out)) { // Only add the arcs which are part of the subgraph
                            subGraph.addArc(node.label, out.label);
                        }
                    }
                }
                subGraphs.add(subGraph);
            }
        }
        Log.debugLog(name, "Found " + subGraphs.size() + " cyclic sub graph(s) with n = " + subGraphs.stream().map(g -> g.nodes.size()).collect(Collectors.toList()));
        return subGraphs;
    }

    /**
     * Fully removes all nodes with self edges.
     */
    public static void fullyRemoveSelfEdges(Instance instance) {

        for(Graph component: instance.subGraphs) {
            for(Node node: component.getActiveNodes()) {
                if(node.getOutNeighbors().contains(node)) {
                    fullyRemoveNode(instance, node);
                    instance.S.add(node);
                    instance.solvedK++;
                }
            }
        }
    }

    private static void fullyRemoveNode(Instance instance, Node node) {
        for(Graph subGraph: instance.subGraphs) {
            subGraph.fullyRemoveNode(node);
        }
    }

    /**
     * Sorts the edges of each component by their number of edges.
     */
    public static void sortNodesByEdgeCount(Instance instance) {
        for(Graph subGraph: instance.subGraphs) {
            for(Node node: subGraph.getActiveNodes()) {
                int weight = node.getOutNeighbors().size();
                for(Node other: subGraph.getActiveNodes()) {
                    if(other.getOutNeighbors().contains(node)) weight++;
                }
                node.weight = weight;
            }
            subGraph.nodes.sort(Comparator.comparingInt(n -> -n.weight));
        }
    }

    public static List<Pair> findPairs(Graph graph) {

        List<Pair> pairs = new ArrayList<>();
        for(Node A: graph.nodes) {
            for(Node B: A.getOutNeighbors()) {
                if(B.getOutNeighbors().contains(A)) {
                    pairs.add(new Pair(A, B));
                }
            }
        }
        return pairs;
    }

    public static void fullyRemovePairs(Instance instance) {
        for(Graph subGraph: instance.subGraphs) {
            for(Node A: subGraph.getActiveNodes()) {
                if(A.getOutNeighbors().size() == 1) { // If the node A has exactly one out neighbor B
                    Node B = A.getOutNeighbors().get(0);
                    if(B.getOutNeighbors().contains(A)) { // If there is an edge back from B to A
                        long inEdgeCount = subGraph.getActiveNodes().stream().filter(n -> n.getOutNeighbors().contains(A)).count();
                        if(inEdgeCount == 1) { // if there are no other edges to A
                            fullyRemoveNode(instance, B);
                            instance.S.add(B);
                            instance.solvedK++;
                        }
                    }
                }
            }
        }
    }
}





























