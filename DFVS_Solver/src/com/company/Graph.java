package com.company;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Graph {

    //Name of the graph for logging
    public String name;

    List<Node> nodes = new ArrayList<>();

    public Graph(String name) {
        this.name = name;
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






    public List<Node> getCircle(){

        Log.log(Log.LogDetail.Unimportant, name, "Get Circle...");

        for(Node node : getActiveNodes()){
            Log.log(Log.LogDetail.Unimportant, name, "Checking node " + node.label + "...");
            List<Node> subGraphCircle = getCircleRecursive(List.of(node));
            if(subGraphCircle != null){
                Log.log(Log.LogDetail.Normal, name, "Found Circle: " + subGraphCircle.stream().map(x -> x.label).collect(Collectors.joining(",")));
                return subGraphCircle;
            }
        }

        //No circle found - ERROR
        return null;
    }

    private List<Node> getCircleRecursive(List<Node> path){

        //Check if element is in path twice
        if(path.size() != Arrays.stream(path.toArray()).distinct().count()){
            Collections.reverse(path);
            for(int i = 1; i < path.size(); i++){
                if(path.get(0) == path.get(i)){
                    return path.subList(0, i);
                }
            }
        }

        //Get all out going arcs
        List<Node> outArcs = path.get(path.size() - 1).getOutNeighbours();

        //Next iteration
        for(Node arc : outArcs){
            List<Node> copyPath = new ArrayList<>(path);
            copyPath.add(arc);
            List<Node> recCircle = getCircleRecursive(copyPath);
            if(recCircle != null) return recCircle;
        }

        //No circle
        return null;
    }

    private List<Node> getActiveNodes() {
        return nodes.stream().filter(node -> !node.deleted).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        List<String> nodesStrings = getActiveNodes().stream().map(Object::toString).collect(Collectors.toList());
        return String.join("\n", nodesStrings);
    }

}
