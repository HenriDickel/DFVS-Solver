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

    public Graph removeNode(Node node) {
        //node.delete();
        for(Node node1 : nodes){
           node1.outNeighbours.remove(node);
        }
        nodes.remove(node);
        return this;
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

        for(Node node : nodes){
            List<Node> subGraphCircle = getCircleRecursive(List.of(node));
            if(subGraphCircle != null){
                Log.log(Log.LogDetail.Normal, name, "Found Circle: " + subGraphCircle.stream().map(x -> x.label).collect(Collectors.joining(",")));
                return subGraphCircle;
            }
        }

        /*
        List<List<Node>> connectedSubGraphs = getConnectedSubGraphs();
        if(connectedSubGraphs.size() > 1){
            int x = 5;
        }
        for(List<Node> subGraph : connectedSubGraphs){
            List<Node> subGraphCircle = getCircleRecursive(List.of(subGraph.get(0)));
            if(subGraphCircle != null) return subGraphCircle;
        }*/

        //No circle found
        return null;
    }

    /*
    private List<List<Node>> getConnectedSubGraphs(){
        List<List<Node>> result = new ArrayList<>();
        List<Node> allNodesInSolution = new ArrayList<>();

        while(allNodesInSolution.size() < nodes.size()){
            //Find unconnected node
            Node unconnectedNode = nodes.stream().filter(x -> !allNodesInSolution.contains(x)).findFirst().get();
            List<Node> connectedNodes = getAllConnectedNodes(unconnectedNode);
            result.add(connectedNodes);
            allNodesInSolution.addAll(connectedNodes);

            if(allNodesInSolution.size() < nodes.size()){
                int x = 5;
            }
        }

        return result;
    }

    private List<Node> getAllConnectedNodes(Node node){
        getAllConnectedNodesRecursive(node);
        return testList;
    }

    public List<Node> testList = new ArrayList<>();

    private void getAllConnectedNodesRecursive(Node node){
        if(testList.contains(node)) return;
        else testList.add(node);

        for(Arc arc : getOutArcs(node)){
            getAllConnectedNodesRecursive(arc.to);
        }
    }*/

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

    @Override
    public String toString() {
        List<String> nodesStrings = nodes.stream().map(Object::toString).collect(Collectors.toList());
        return String.join("\n", nodesStrings);
    }

    public Graph copy(){
        Graph graph = new Graph(this.name);
        List<Node> nodes = new ArrayList<>();
        for(Node neighbour : nodes){
            //nodes.add(neighbour.copy())
        }
        graph.nodes = new ArrayList<>(nodes);
        return graph;
    }

    public void enableNode(Node node) {
        node.unDelete();
    }

    public void disableNode(Node node) {
        node.delete();
    }
}
