package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Graph {

    List<GraphNode> nodes = new ArrayList<>();
    List<Arc> arcs = new ArrayList<>();

    public Graph removeNode(GraphNode node) {
        nodes.remove(node);
        List<Arc> newArcs = new ArrayList<>();
        for(Arc arc : arcs){
            if(arc.from != node && arc.to != node) newArcs.add(arc);
        }
        arcs = newArcs;

        return this;
    }

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

    public boolean isDAG(){
        return isDAGRecursive(new ArrayList<>());
    }

    public List<GraphNode> getCircle(){
        return getCircleRecursive(List.of(nodes.get(0)));
    }

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

    @Override
    public String toString() {
        List<String> nodesStrings = nodes.stream().map(Object::toString).collect(Collectors.toList());
        List<String> arcStrings = arcs.stream().map(Arc::toString).collect(Collectors.toList());

        return "Graph: " + "\n" +
                "Nodes = {" + String.join(", ", nodesStrings) + "}" + "\n" +
                "Arcs = {" + String.join(", ", arcStrings) + "}";
    }

    public Graph copy(){
        Graph graph = new Graph();
        graph.nodes = new ArrayList<>(nodes);
        graph.arcs = new ArrayList<>(arcs);
        return graph;
    }

}
