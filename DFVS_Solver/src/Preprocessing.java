import log.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public abstract class Preprocessing {

    private static int index;
    private static int cycleStartIndex;

    private static List<List<Node>> cycles;
    private static Stack<Node> stack;

    public static List<List<Node>> findAllCycles(Graph graph) {

        Log.log(Log.LogDetail.Important, graph.name, "----------Starting preprocessing of graph " + graph.name + "... ----------");
        cycles = new ArrayList<>();

        // Find cyclic components
        List<List<Node>> components = TarjanAlgorithm.findComponents(graph);

        Log.log(Log.LogDetail.Important, graph.name, "Found " + components.size() + " components: " + components);

        // Generate sub graphs
        List<Graph> subGraphs = new ArrayList<>();
        for(List<Node> component: components) {
            if(component.size() > 1) {
                Graph subGraph = new Graph(graph.name + " subgraph");
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
        Log.log(Log.LogDetail.Important, graph.name, "Found " + subGraphs.size() + " cyclic sub graphs: " + subGraphs.stream().map(s -> s.nodes).collect(Collectors.toList()));

        for(Graph subGraph: subGraphs) {
            findCyclesInComponent(subGraph);
        }

        // Remove duplicate cycles
        // TODO sorry
        List<List<Node>> distinctCycles = cycles.stream()
                .map(HashSet::new)
                .collect(Collectors.toList())
                .stream()
                .distinct()
                .map(e -> e.stream().toList())
                .collect(Collectors.toList());


;        Log.log(Log.LogDetail.Important, graph.name, "Found " + cycles.size() + " cycles: " + cycles);

        return distinctCycles;
    }

    private static void findCyclesInComponent(Graph subGraph) {
        index = 0;
        stack = new Stack<>();
        subGraph.unvisitAllNodes();
        Node startNode = subGraph.getActiveNodes().get(0);
        visitNode(startNode);
    }


    private static void visitNode(Node node) {

        if(node.visitIndex != -1) { // was already visited
            List<Node> cycle = stack.stream().toList().subList(node.visitIndex, index);
            cycles.add(cycle);
        } else { // was not visited yet
            node.visitIndex = index;
            index++;
            stack.push(node);

            for(Node out: node.getOutNeighbours()) {
                visitNode(out);
            }

            stack.pop();
            index--;
            node.visitIndex = -1;
        }
    }

    private static List<Node> visitNodeInComponent(Node node) {

        if(node.visitIndex != -1) {
            List<Node> pathToStart = new ArrayList<>();
            pathToStart.add(node);
            cycleStartIndex = node.visitIndex;
            return pathToStart;
        } else {
            node.visitIndex = index;
            index++;

            for (Node out : node.getOutNeighbours()) {
                List<Node> pathToStart = visitNodeInComponent(out);
                if(pathToStart != null) {
                    if(node.visitIndex > cycleStartIndex) {
                        pathToStart.add(node);
                        //node.visited = -1;
                        return pathToStart;
                    } else if(node.visitIndex == cycleStartIndex) {
                        cycles.add(pathToStart);
                        //node.visited = -1;
                        return null;
                    } else {
                        //node.visited = -1;
                        return pathToStart;
                    }
                }

            }

            node.visitIndex = -1;
            return null;
        }
    }
}
