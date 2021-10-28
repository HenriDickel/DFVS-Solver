import log.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public abstract class Preprocessing {

    public static List<Graph> findCyclicSubGraphs(Graph graph) {
        Log.log(Log.LogDetail.Important, graph.name, "----------Starting preprocessing of graph " + graph.name + " with " + graph.nodes.size() + " nodes... ----------");

        // Find cyclic components
        List<List<Node>> components = TarjanAlgorithm.findComponents(graph);

        Log.log(Log.LogDetail.Important, graph.name, "Found " + components.size() + " components: " + components);

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
        Log.log(Log.LogDetail.Important, graph.name, "Found " + subGraphs.size() + " cyclic sub graphs"); // + subGraphs.stream().map(s -> s.nodes).collect(Collectors.toList()));
        return subGraphs;
    }

}
