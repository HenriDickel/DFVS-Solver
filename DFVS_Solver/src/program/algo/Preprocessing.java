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
                            subGraph.addArc(node.label, out.label);
                        }
                    }
                }
                subGraphs.add(subGraph);
            }
        }
        return subGraphs;
    }

    public static List<Node> applyRules(Graph graph) {
        List<Node> S = new ArrayList<>();
        Node node;
        while((node = findNextNode(graph)) != null) {
            if(node.getOutIdCount() == 0 || node.getInIdCount() == 0) { // remove trivial vertices
                graph.removeNode(node);
            } else if(node.getOutIds().contains(node.id)) { // remove self loops
                graph.removeNode(node);
                S.add(node);
            } else if(node.getOutIdCount() == 1) { // chain rule (single out neighbor) in >>> node -> out
                Integer outId = node.getOutIds().get(0);
                Node out = graph.getNode(outId);
                for(Integer inId: node.getInIds()) {
                    Node in = graph.getNode(inId);
                    in.addOutId(outId);
                    out.addInId(inId);
                }
                graph.removeNode(node);
            } else if(node.getInIdCount() == 1) { // chain rule (single in neighbor) in -> node >>> out
                Integer inId = node.getInIds().get(0);
                Node in = graph.getNode(inId);
                for(Integer outId: node.getOutIds()) {
                    Node out = graph.getNode(outId);
                    out.addInId(inId);
                    in.addOutId(outId);
                }
                graph.removeNode(node);
            } else {
                System.out.println("Never reached");
            }
        }
        return S;
    }

    private static Node findNextNode(Graph graph) {
        for(Node node: graph.getNodes()) {
            if(node.getOutIdCount() == 0 || node.getInIdCount() == 0) { // trivial vertex rule
                return node;
            } else if(node.getOutIds().contains(node.id)) { // self loop rule
                return node;
            } else if(node.getOutIdCount() == 1) {
                return node;
            } else if(node.getInIdCount() == 1) {
                return node;
            }
        }
        return null;
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
                            graph.removeNode(A);
                            graph.removeNode(B);
                            graph.removeNode(C);
                            Solver.instance.S.add(B);
                            Solver.instance.S.add(C);
                        }
                    }
                }
            }
        }
    }
}





























