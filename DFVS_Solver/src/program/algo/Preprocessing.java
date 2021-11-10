package program.algo;

import program.model.Graph;
import program.model.Instance;
import program.model.Node;
import program.log.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Preprocessing {

    /**
     * Splits up the graph into it's cyclic components by using the Tarjan's algorithm.
     */
    public static List<Graph> findCyclicSubGraphs(String name, Graph graph) {
        Log.debugLog(name, "----------Starting preprocessing of graph " + name + " with " + graph.getActiveNodes().size() + " nodes... ----------");

        // Find cyclic components
        List<List<Node>> components = Tarjan.run(graph);

        Log.debugLog(name, "Found " + components.size() + " components: " + components);

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
        Log.debugLog(name, "Found " + subGraphs.size() + " cyclic sub graphs: " + subGraphs.stream().map(Graph::getActiveNodes).collect(Collectors.toList()));
        return subGraphs;
    }

    /**
     * Fully removes all nodes with self edges.
     */
    public static void fullyRemoveSelfEdges(Instance instance) {

        for(Graph component: instance.subGraphs) {
            for(Node node: component.getActiveNodes()) {
                if(node.getOutNeighbors().contains(node)) {
                    System.out.println(node);
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
}
