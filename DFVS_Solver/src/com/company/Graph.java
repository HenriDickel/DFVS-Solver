package com.company;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    //Name of the graph for logging
    public String name;

    List<Node> nodes = new ArrayList<>();

    public Graph(String name) {
        this.name = name;
    }

    public void unvisitAllNodes() {
        nodes.forEach(node -> node.visited = -1);
    }

    public void addArc(String from, String to){

        //Add nodes if not existing
        if(nodes.stream().noneMatch(x -> x.label.equals(from))) nodes.add(new Node(from));
        if(nodes.stream().noneMatch(x -> x.label.equals(to))) nodes.add(new Node(to));

        //Get Nodes
        Node a = nodes.stream().filter(x -> x.label.equals(from)).findFirst().get();
        Node b = nodes.stream().filter(x -> x.label.equals(to)).findFirst().get();

        //Add to neighbours
        a.addNeighbour(b);
    }

    public boolean isDAG(){
        Log.log(Log.LogDetail.Unimportant, name, "Test if graph is DAG...");

        //Start Recursion
        boolean isDag = isDAGRecursive(new ArrayList<>());
        Log.log(Log.LogDetail.Unimportant, name, isDag ? "Graph is a DAG" : "Graph is not a DAG");

        return isDag;
    }
    private boolean isDAGRecursive(List<Node> checked){

        if(checked.size() == nodes.size()) return true;

        //Check if something can be removed
        for(Node node : nodes){

            //Skip those already checked
            if(checked.contains(node)) continue;

            //Get all out going arcs
            List<Node> outArcs = node.getOutNeighbours();

            //Remove those with no arcs
            if(outArcs.size() == 0){
                checked.add(node);
                return isDAGRecursive(checked);
            }

            //Remove those where all neighbours are already checked
            if(checked.containsAll(outArcs)){
                checked.add(node);
                return isDAGRecursive(checked);
            }
        }
        return false;
    }

    public List<Node> getActiveNodes() {
        return nodes.stream().filter(node -> !node.deleted).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        List<String> nodesStrings = getActiveNodes().stream().map(Object::toString).collect(Collectors.toList());
        return String.join("\n", nodesStrings);
    }

}
