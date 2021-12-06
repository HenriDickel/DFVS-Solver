package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
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
        return subGraphs;
    }

    /**
     * Improvement of the normal triangle rule that only needs one Node to only be in the fully connected graph
     */

    public static void removePendantFullTrianglePP(Graph graph){
        for(Node A: graph.getNodes()){
            if(A.getOutIds().size()==2) {
                int inEdges = (int) graph.getNodes().stream().filter(n -> n.getOutIds().contains(A.id)).count();
                if (inEdges == 2) {
                    Integer bId = A.getOutIds().get(0);
                    Integer cId = A.getOutIds().get(1);
                    Node B = graph.getNode(bId);
                    Node C = graph.getNode(cId);
                    if (B.getOutIds().contains(A.id) && B.getOutIds().contains(C.id)) {
                        if (C.getOutIds().contains(B.id) && C.getOutIds().contains(A.id)) {
                            graph.removeNode(A.id);
                            graph.removeNode(B.id);
                            graph.removeNode(C.id);
                            Solver.instance.S.add(B.id);
                            Solver.instance.S.add(C.id);
                        }
                    }
                }
            }
        }
    }
}





























