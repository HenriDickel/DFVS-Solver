package program.model;

import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    public List<Node> nodes = new ArrayList<>();

    public Graph() {}

    public Graph(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void resetBFS() {
        nodes.forEach(node -> node.visitIndex = -1);
        nodes.forEach(node -> node.parent = null);
        nodes.forEach(node -> node.explored = false);
    }

    public void addArc(String from, String to){

        //Add nodes if not existing
        if(nodes.stream().noneMatch(x -> x.label.equals(from))) nodes.add(new Node(from));
        if(nodes.stream().noneMatch(x -> x.label.equals(to))) nodes.add(new Node(to));

        //Get Nodes
        Node a = nodes.stream().filter(x -> x.label.equals(from)).findFirst().get();
        Node b = nodes.stream().filter(x -> x.label.equals(to)).findFirst().get();

        //Add to neighbours
        a.addOutNeighbor(b);
        b.addInNeighbor(a);
    }

    public List<Node> getActiveNodes() {
        return nodes.stream().filter(node -> !node.deleted).collect(Collectors.toList());
    }

    public List<Node> getInactiveNodes() {
        return nodes.stream().filter(node -> node.deleted).collect(Collectors.toList());
    }

    public int getDestroyedPetalCount() {
        int count = 0;
        List<Node> deletedNodes = getInactiveNodes();
        deletedNodes.sort(Comparator.comparing(Node::getMaxPetal));
        for(int i = 0; i < deletedNodes.size(); i++) {
            count += deletedNodes.get(i).maxPetal - i;
        }
        return count;
    }

    public int getEdgeCount() {
        return getActiveNodes().stream().mapToInt(node -> node.getOutNeighbors().size()).sum();
    }

    public void fullyRemoveNode(Node nodeToRemove) {
        for (Node node : nodes) {
            node.removeOutNeighbor(nodeToRemove);
        }
        for (Node node : nodes) {
            node.removeInNeighbor(nodeToRemove);
        }
        nodes.remove(nodeToRemove);
    }

    public Graph reducedCopy() {
        Graph copy = new Graph();

        for(Node node: nodes) {
            if(!node.deleted && !(node.forbidden < Integer.MAX_VALUE)) { // if node is not deleted of forbidden
                for(Node out: node.getOutNeighbors()) {
                     if(out.deleted) {
                         // Skip
                     } else if(out.forbidden < Integer.MAX_VALUE) {
                         for(Node outOut: out.getOutNeighbors()) {
                             if(outOut.deleted) {
                                 // Skip
                             } else {
                                 copy.addArc(node.label, outOut.label);
                             }
                         }
                     } else {
                         copy.addArc(node.label, out.label);
                     }
                }
            }
        }

        return copy;
    }

    public Graph clone() {
        List<Node> copyNodes = new ArrayList<>();
        for(int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Node copyNode = new Node(node.label);
            copyNode.forbidden = node.forbidden;
            copyNode.deleted = node.deleted;
            copyNode.petal = node.petal;
            copyNode.maxPetal = node.maxPetal;
            copyNodes.add(copyNode);
        }
        for(int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Node copyNode = copyNodes.get(i);
            for(Node outNeighbor: node.getOutNeighbors()) {
                int index = nodes.indexOf(outNeighbor);
                Node copyOutNeighbor = copyNodes.get(index);
                copyNode.addOutNeighbor(copyOutNeighbor);
                copyOutNeighbor.addInNeighbor(copyNode);
            }
        }
        return new Graph(copyNodes);
    }

    //------------------------------------------------------------------Flowers-----------------------------------------
    public void replaceNode(Node node, boolean isU){

        //Remove node
        nodes.remove(node);

        List<Node> copyNodes = new ArrayList<>(nodes);

        //For each node add edges
        for(Node a : copyNodes){
            //Ingoing to node-
            if(a.getOutNeighbors().contains(node)){
                addArc(a.label, node.label + "-");
                a.getOutNeighbors().remove(node);
            }
            //Outgoing to node+
            if(a.getInNeighbors().contains(node)){
                addArc(a.label, node.label + "+");
                a.getInNeighbors().remove(node);
            }
        }

        //Add node- to node+
        if(!isU) addArc(node.label + "-", node.label + "+");
    }

    public Graph copy(){
        Graph graph = new Graph();
        graph.nodes = new ArrayList<>();

        for(Node node : nodes){
            for(Node out : node.getOutNeighbors()){
                graph.addArc(node.label, out.label);
            }
        }
        return graph;
    }

    @Override
    public String toString() {
        List<String> nodesStrings = getActiveNodes().stream().map(Object::toString).collect(Collectors.toList());
        return String.join("\n", nodesStrings);
    }

    public void removeArc(String from, String to) {
        Node fromNode = nodes.stream().filter(x -> x.label.equals(from)).findFirst().get();
        Node toNode = nodes.stream().filter(x -> x.label.equals(to)).findFirst().get();
        fromNode.removeOutNeighbor(toNode);
    }
}
