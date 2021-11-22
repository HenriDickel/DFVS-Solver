package program.algo;

import program.model.Graph;
import program.model.Instance;
import program.model.Node;
import program.model.Pair;

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
                    for(Node out: node.getOutNeighbors()) {
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

    private static void checkSelfEdge(Graph graph, Node node) {
        if(node.getOutNeighbors().contains(node)) {
            graph.fullyRemoveNode(node);
            Solver.instance.S.add(node);
            Solver.instance.solvedK++;
            System.out.println("Remove self node " + node.label + " with in = " + node.getInNeighbors() + " and out = " + node.getOutNeighbors());
        }
    }

    private static Node findNextNode(Graph graph) {
        for(Node node: graph.nodes) {
            if(node.getOutNeighbors().isEmpty() || node.getInNeighbors().isEmpty()) { // trivial vertex rule
                return node;
            } else if(node.getOutNeighbors().contains(node)) { // self loop rule
                return node;
            } else if(node.getOutNeighbors().size() == 1) {
                return node;
            } else if(node.getInNeighbors().size() == 1) {
                return node;
            }
        }
        return null;
    }

    public static void applyRules(Graph graph) {
        Node node;
        while((node = findNextNode(graph)) != null) {
            if(node.getOutNeighbors().isEmpty() || node.getInNeighbors().isEmpty()) { // remove trivial vertices
                graph.fullyRemoveNode(node);
            } else if(node.getOutNeighbors().contains(node)) { // remove self loops
                graph.fullyRemoveNode(node);
                Solver.instance.S.add(node);
                Solver.instance.solvedK++;
            } else if(node.getOutNeighbors().size() == 1) { // chain rule (single out neighbor) in >>> node -> out
                Node out = node.getOutNeighbors().get(0);
                for(Node in: node.getInNeighbors()) {
                    in.addOutNeighbor(out);
                    out.addInNeighbor(in);
                }
                graph.fullyRemoveNode(node);
            } else if(node.getInNeighbors().size() == 1) { // chain rule (single in neighbor) in -> node >>> out
                Node in = node.getInNeighbors().get(0);
                for(Node out: node.getOutNeighbors()) {
                    out.addInNeighbor(in);
                    in.addOutNeighbor(out);
                }
                graph.fullyRemoveNode(node);
            } else {
                System.out.println("Never reached");
            }
        }
    }

    /**
     * Using a slightly adjusted chaining rule remove Nodes with only one ingoing our only one outgoing Edge and improve performance where this might help
     * @param instance The current Graph instance
     */
    public static void removeChain(Instance instance){
        for(Graph subGraph: instance.subGraphs) {
            List<Node> oneInNodes;
            List<Node> oneOutNodes;
            do{
                oneInNodes = new ArrayList<>();
                oneOutNodes = new ArrayList<>();
                // Collect Nodes with either only one outgoing or one ingoing Edge
                for (Node A : subGraph.getActiveNodes()) {
                    if (A.getOutNeighbors().size() <= 1) {
                        if (A.getOutNeighbors().size() == 0) {
                            fullyRemoveNode(instance,A);
                        }else{
                            oneOutNodes.add(A);
                        }
                    } else {
                        List<Node> inEdgeNodes = subGraph.getActiveNodes().stream().filter(n -> n.getOutNeighbors().contains(A)).toList();
                        if (inEdgeNodes.size() == 1) {
                            oneInNodes.add(A);
                        }
                    }
                }
                //Call methodes for removing the specific nodes
                removeOneOutNodes(instance,subGraph,oneOutNodes);
                removeOneInNodes(instance,subGraph,oneInNodes);
            }while(!oneInNodes.isEmpty() || !oneOutNodes.isEmpty());
        }
    }

    /**
     * Function for removing Nodes with only one outgoing edge
     * @param instance The current Graph instance
     * @param subGraph The current subGraph
     * @param oneOutNodes The List of Nodes that needs to be deleted
     */
    private static  void removeOneOutNodes(Instance instance,Graph subGraph, List<Node>oneOutNodes){
        for(Node A :oneOutNodes) {
            List<Node> inEdgeNodes = subGraph.getActiveNodes().stream().filter(n -> n.getOutNeighbors().contains(A)).toList();
            //Needed because Nodes can be deleted in the whole process
            if(A.getOutNeighbors().size()>0) {
                Node C = A.getOutNeighbors().get(0);
                for (Node B : inEdgeNodes) {
                    //If a self-circle is about to be created, delete the node
                    if(B.label == C.label ){
                        fullyRemoveNode(instance,C);
                        instance.S.add(B);
                        instance.solvedK++;
                        break;
                    }else{
                        //Only add Neighbor if it isn't already in the list
                        if (!B.getOutNeighbors().contains(C)) {
                            B.addOutNeighbor(C);
                        }
                    }
                }
            }
            fullyRemoveNode(instance, A);
        }
    }

    /**
     * Function for removing Nodes with only one ingoing edge
     * @param instance The current Graph instance
     * @param subGraph The current subGraph
     * @param oneInNodes The List of Nodes that needs to be deleted
     */
    private static void removeOneInNodes(Instance instance, Graph subGraph, List<Node> oneInNodes){
        for (Node A :oneInNodes) {
            List<Node> inEdgeNodes = subGraph.getActiveNodes().stream().filter(n -> n.getOutNeighbors().contains(A)).toList();
            //Needed because Nodes can be deleted in the whole process
            if (inEdgeNodes.size() > 0) {
                Node C = inEdgeNodes.get(0);
                for (Node B : A.getOutNeighbors()) {
                    //If a self-circle is about to be created, delete the node
                    if(C.label==B.label){
                        fullyRemoveNode(instance,C);
                        instance.S.add(B);
                        instance.solvedK++;
                        break;
                    }else{
                        //Only add Neighbor if it isn't already in the list
                        if (!C.getOutNeighbors().contains(B)) {
                            C.addOutNeighbor(B);
                        }
                    }
                }
                fullyRemoveNode(instance, A);
            }
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
            subGraph.nodes.sort(Comparator.comparingInt(n -> n.weight));
        }
    }

    /**
     * Using the pendant full triangle rule delete the Nodes ahead of time so they don't have to be looked at later
     * @param instance
     */

    public static void removePendantFullTriangle(Instance instance){
            for(Graph subgraph: instance.subGraphs){
                for(Node A: subgraph.getActiveNodes()){
                    if(A.getOutNeighbors().size()==2) {
                        int inEdges = (int) subgraph.getActiveNodes().stream().filter(n -> n.getOutNeighbors().contains(A)).count();
                        if (inEdges == 2) {
                            Node B = A.getOutNeighbors().get(0);
                            Node C = A.getOutNeighbors().get(1);
                            int inEdgesB = (int) subgraph.getActiveNodes().stream().filter(n -> n.getOutNeighbors().contains(B)).count();
                            int inEdgesC = (int) subgraph.getActiveNodes().stream().filter(n -> n.getOutNeighbors().contains(C)).count();
                            if((B.getOutNeighbors().size() ==2 && inEdgesB==2) || (C.getOutNeighbors().size()==2 && inEdgesC==2)) {
                                if (B.getOutNeighbors().contains(A) && B.getOutNeighbors().contains(C)) {
                                    if (C.getOutNeighbors().contains(B) && C.getOutNeighbors().contains(A)) {
                                        fullyRemoveNode(instance, A);
                                        fullyRemoveNode(instance, B);
                                        fullyRemoveNode(instance, C);
                                        instance.S.add(B);
                                        instance.solvedK++;
                                        instance.S.add(C);
                                        instance.solvedK++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    /**
     * Improvement of the normal triangle rule that only needs one Node to only be in the fully connected graph
     * @param instance
     */

    public static void removePendantFullTrianglePP(Instance instance){
        for( Graph subgraph: instance.subGraphs){
            for(Node A: subgraph.getActiveNodes()){
                if(A.getOutNeighbors().size()==2) {
                    int inEdges = (int) subgraph.getActiveNodes().stream().filter(n -> n.getOutNeighbors().contains(A)).count();
                    if (inEdges == 2) {
                        Node B = A.getOutNeighbors().get(0);
                        Node C = A.getOutNeighbors().get(1);
                        if (B.getOutNeighbors().contains(A) && B.getOutNeighbors().contains(C)) {
                            if (C.getOutNeighbors().contains(B) && C.getOutNeighbors().contains(A)) {
                                fullyRemoveNode(instance, A);
                                fullyRemoveNode(instance, B);
                                fullyRemoveNode(instance, C);
                                instance.S.add(B);
                                instance.solvedK++;
                                instance.S.add(C);
                                instance.solvedK++;
                            }
                        }
                    }
                }
            }
        }
    }
}





























