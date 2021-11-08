package program.algo;

import program.model.Graph;
import program.model.Node;
import program.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Preprocessing {

    public static List<Graph> findCyclicSubGraphs(Graph graph) {
        Log.debugLog(graph.name, "----------Starting preprocessing of graph " + graph.name + " with " + graph.getActiveNodes().size() + " nodes... ----------");

        // Find cyclic components
        List<List<Node>> components = Tarjan.findComponents(graph);

        Log.debugLog(graph.name, "Found " + components.size() + " components: " + components);

        // Generate sub graphs
        List<Graph> subGraphs = new ArrayList<>();
        for(List<Node> component: components) {
            if(component.size() > 1) {
                Graph subGraph = new Graph(graph.name);
                for(Node node: component) {
                    for(Node out: node.getOutNeighbours()) {
                        if(component.contains(out)) { // Only add the arcs which are part of the subgraph
                            subGraph.addArc(node.label, out.label);
                        }
                    }
                }
                subGraphs.add(subGraph);
            }
        }
        Log.debugLog(graph.name, "Found " + subGraphs.size() + " cyclic sub graphs: " + subGraphs.stream().map(Graph::getActiveNodes).collect(Collectors.toList()));
        return subGraphs;
    }

}
