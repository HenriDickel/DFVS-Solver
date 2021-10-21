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

        //Count
        long deleteCount = nodes.stream().filter(x -> x.deleted).count();

        //While there are unchecked nodes
        while(deleteCount < nodes.size()){

            //Remember if something was removed
            boolean nodeWasRemoved = false;

            //Check each unchecked node
            for(Node node : nodes.stream().filter(x -> !x.deleted).collect(Collectors.toList())){
                //If it has no neighbours remove it
                if(node.getOutNeighbours().isEmpty()){
                    node.delete();
                    deleteCount++;
                    nodeWasRemoved = true;
                }
            }

            //Check if nodeWasRemoved
            if(!nodeWasRemoved){
                return false;
            }
        }

        //Every node was checked
        return true;
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

    @Override
    public String toString() {
        List<String> nodesStrings = nodes.stream().map(Object::toString).collect(Collectors.toList());
        return String.join("\n", nodesStrings);
    }

}
