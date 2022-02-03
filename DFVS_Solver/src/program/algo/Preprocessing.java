package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class Preprocessing {

    /**
     * Splits up the graph into it's cyclic components by using the Tarjan's algorithm.
     */
    public static List<Graph> findCyclicSubGraphs(Graph graph) {

        // Find cyclic components
        List<List<Node>> components = Tarjan.run(graph);

        // Generate sub graphs
        List<Graph> subGraphs = new ArrayList<>();
        for(List<Node> component: components) {
            if(component.size() > 1) {
                Graph subGraph = new Graph();
                for(Node node: component) {
                    for(Integer outId: node.getOutIds()) {
                        Node out = graph.getNode(outId);
                        if(component.contains(out)) { // Only add the arcs which are part of the subgraph
                            subGraph.addArc(node.id, out.id);
                        }
                    }
                }
                subGraphs.add(subGraph);
            }
        }

        subGraphs.sort(Comparator.comparing(Graph::getNodeCount));
        return subGraphs;
    }
}





























