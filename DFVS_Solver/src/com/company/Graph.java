package com.company;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {

    List<GraphNode> nodes = new ArrayList<>();
    List<Arc> arcs = new ArrayList<>();

    public static class GraphNode{
        String label;

        GraphNode(String label){
            this.label = label;
        }

        @Override
        public String toString(){
            return label;
        }
    }

    public static class Arc{
        GraphNode from;
        GraphNode to;

        public Arc(GraphNode from, GraphNode to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString(){
            return "(" + from.label + " -> " + to.label + ")";
        }
    }

    public void addArc(String from, String to){

        //Add nodes if not existing
        if(nodes.stream().noneMatch(x -> x.label.equals(from))) nodes.add(new GraphNode(from));
        if(nodes.stream().noneMatch(x -> x.label.equals(to))) nodes.add(new GraphNode(to));

        //Get Nodes
        GraphNode a = nodes.stream().filter(x -> x.label.equals(from)).findFirst().get();
        GraphNode b = nodes.stream().filter(x -> x.label.equals(to)).findFirst().get();

        arcs.add(new Arc(a, b));
    }

    public Graph removeNode(GraphNode node) {
        nodes.remove(node);
        List<Arc> newArcs = new ArrayList<>();
        for(Arc arc : arcs){
            if(arc.from != node && arc.to != node) newArcs.add(arc);
        }
        arcs = newArcs;

        return this;
    }

    public boolean isDAG(){
        return isDAGRecursive(new ArrayList<>());
    }

    private boolean isDAGRecursive(List<GraphNode> checked){
        if(checked.size() == nodes.size()) return true;

        //Check if something can be removed
        for(GraphNode node : nodes){

            //Skip those already checked
            if(checked.contains(node)) continue;

            //Get all out going arcs
            List<Arc> outArcs = getOutArcs(node);

            //Remove those with no arcs
            if(outArcs.size() == 0){
                checked.add(node);
                return isDAGRecursive(checked);
            }

            //Remove those where all neighbours are already checked
            List<GraphNode> neighbours = outArcs.stream().map(x -> x.to).collect(Collectors.toList());
            if(checked.containsAll(neighbours)){
                checked.add(node);
                return isDAGRecursive(checked);
            }
        }

        return false;
    }

    public List<GraphNode> getCircle(){

        for(GraphNode node : nodes){
            List<GraphNode> subGraphCircle = getCircleRecursive(List.of(node));
            if(subGraphCircle != null) return subGraphCircle;
        }

        /*
        List<List<GraphNode>> connectedSubGraphs = getConnectedSubGraphs();
        if(connectedSubGraphs.size() > 1){
            int x = 5;
        }
        for(List<GraphNode> subGraph : connectedSubGraphs){
            List<GraphNode> subGraphCircle = getCircleRecursive(List.of(subGraph.get(0)));
            if(subGraphCircle != null) return subGraphCircle;
        }*/

        //No circle found
        return null;
    }

    /*
    private List<List<GraphNode>> getConnectedSubGraphs(){
        List<List<GraphNode>> result = new ArrayList<>();
        List<GraphNode> allNodesInSolution = new ArrayList<>();

        while(allNodesInSolution.size() < nodes.size()){
            //Find unconnected node
            GraphNode unconnectedNode = nodes.stream().filter(x -> !allNodesInSolution.contains(x)).findFirst().get();
            List<GraphNode> connectedNodes = getAllConnectedNodes(unconnectedNode);
            result.add(connectedNodes);
            allNodesInSolution.addAll(connectedNodes);

            if(allNodesInSolution.size() < nodes.size()){
                int x = 5;
            }
        }

        return result;
    }

    private List<GraphNode> getAllConnectedNodes(GraphNode node){
        getAllConnectedNodesRecursive(node);
        return testList;
    }

    public List<GraphNode> testList = new ArrayList<>();

    private void getAllConnectedNodesRecursive(GraphNode node){
        if(testList.contains(node)) return;
        else testList.add(node);

        for(Arc arc : getOutArcs(node)){
            getAllConnectedNodesRecursive(arc.to);
        }
    }*/

    private List<GraphNode> getCircleRecursive(List<GraphNode> path){

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
        List<Arc> outArcs = getOutArcs(path.get(path.size() - 1));

        //Next iteration
        for(Arc arc : outArcs){
            List<GraphNode> copyPath = new ArrayList<>(path);
            copyPath.add(arc.to);
            List<GraphNode> recCircle = getCircleRecursive(copyPath);
            if(recCircle != null) return recCircle;
        }

        //No circle
        return null;
    }

    private List<Arc> getOutArcs(GraphNode node){
        return arcs.stream().filter(x -> x.from == node).collect(Collectors.toList());
    }



    @Override
    public String toString() {
        List<String> nodesStrings = nodes.stream().map(Object::toString).collect(Collectors.toList());
        List<String> arcStrings = arcs.stream().map(Arc::toString).collect(Collectors.toList());

        return  "Nodes = {" + String.join(", ", nodesStrings) + "}" + "\n" +
                "Arcs = {" + String.join(", ", arcStrings) + "}";
    }

    public Graph copy(){
        Graph graph = new Graph();
        graph.nodes = new ArrayList<>(nodes);
        graph.arcs = new ArrayList<>(arcs);
        return graph;
    }

}
