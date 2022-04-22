package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Reduction {

    public static List<Integer> applyRules(Graph graph, boolean updateAll) {

        //Set updated
        if(updateAll) graph.setAllNodesUpdated();

        //Result
        List<Integer> reduceS = new ArrayList<>();

        //Single edge remove
        graph.getNodes().forEach(x -> doubleEdgeRemoveNormalEdges(graph, x));

        //Superset remove
        reduceS.addAll(supersetCompleteRemove(graph));

        //Diamond
        //reduceS.addAll(diamondCompleteRemove(graph));

        List<Integer> updatedNodeIds;
        while(!(updatedNodeIds = graph.getUpdatedNodeIds()).isEmpty()) {
            for(Integer id: updatedNodeIds) {
                Node node = graph.getNode(id);
                node.updated = false;

                if(node.getOutIdCount() == 0 || node.getInIdCount() == 0) { // trivial vertex
                    graph.removeNode(node.id);
                } else if(node.getOutIds().contains(node.id)) { // self loop
                    graph.removeNode(node.id);
                    reduceS.add(node.id);
                } else if(node.getOutIdCount() == 1) { // chain rule (in >>> node -> out)
                    Integer outId = node.getOutIds().get(0);
                    Node out = graph.getNode(outId);
                    for(Integer inId: node.getInIds()) {
                        Node in = graph.getNode(inId);
                        in.addOutId(outId);
                        out.addInId(inId);
                    }
                    graph.removeNode(node.id);
                } else if(node.getInIdCount() == 1) { // chain rule (in -> node >>> out)
                    Integer inId = node.getInIds().get(0);
                    Node in = graph.getNode(inId);
                    for(Integer outId: node.getOutIds()) {
                        Node out = graph.getNode(outId);
                        out.addInId(inId);
                        in.addOutId(outId);
                    }
                    graph.removeNode(node.id);
                }
            }
        }

        return reduceS;
    }

    private static List<Integer> diamondCompleteRemove(Graph graph){

        //Result
        List<Integer> result = new ArrayList<>();

        //Get shortest cycles
        HashMap<Integer, List<Integer>> shortestCycles = CycleUndirected.shortestCycles(graph);

        //For all nodes
        for(Node node : graph.getNodes()){
            if(result.contains(node.id)) continue;

            List<Integer> shortestCycle = shortestCycles.get(node.id);
            List<Integer> ruleNodes = applyDiamondRule(graph, shortestCycle);

            result.addAll(ruleNodes);

            for(int removeNode : ruleNodes) graph.removeNode(removeNode);
        }

        //Return result
        return result;

    }

    private static List<Integer> applyDiamondRule(Graph graph, List<Integer> cycleNodes){

        //Result
        List<Integer> result = new ArrayList<>();

        //Check if cycle exists
        if(cycleNodes.size() == 0) return new ArrayList<>();

        //One node can be a 'bridge' to the rest of the graph
        Node exceptionNode = null;

        for(Integer nodeId : cycleNodes){
            Node node = graph.getNode(nodeId);
            if(!node.onlyDoubleEdges()) return new ArrayList<>();

            if(node.getOutIdCount() > 2){
                if(exceptionNode != null) return new ArrayList<>();
                else exceptionNode = node;
            }
        }

        //Create path
        List<Integer> sorted = new ArrayList<>();

        //Start node
        if(exceptionNode == null) sorted.add(cycleNodes.get(0));
        else sorted.add(exceptionNode.id);

        //Sort
        while(sorted.size() < cycleNodes.size()){
            Node last = graph.getNode(sorted.get(sorted.size() - 1));
            for(int connected : last.getOutIds()){
                if(!cycleNodes.contains(connected)) continue;
                if(!sorted.contains(connected)){
                    sorted.add(connected);
                    break;
                }
            }
        }

        //Replaceable
        if(cycleNodes.size() % 2 == 0){
            for(int i = 0; i < cycleNodes.size(); i+=2){
                result.add(sorted.get(i));
                graph.removeNode(sorted.get(i));
            }
        }

        //Replaceable uneven (???)
        if(cycleNodes.size() % 2 == 1){
            for(int i = 0; i < cycleNodes.size(); i+=2){
                result.add(sorted.get(i));
                graph.removeNode(sorted.get(i));
            }
        }

        return result;
    }

    private static List<Integer> supersetCompleteRemove(Graph graph){

        //Result
        List<Integer> result = new ArrayList<>();

        //Check if update occurred
        boolean updated = true;

        //As long as something changes
        while(updated){
            //Reset updated
            updated = false;

            //Superset remove
            for(Node node : graph.getNodes()){
                if(supersetRemove(graph, node)){
                    updated = true;
                    result.add(node.id);
                    graph.removeNode(node.id);
                }
            }
        }

        //Return result
        return result;

    }

    private static boolean supersetRemove(Graph graph, Node node){

        //Only double edges
        if(!node.onlyDoubleEdges()) return false;

        //Check if any neighbour is subset
        for(int neighbour : new ArrayList<>(node.getOutIds())){

            //Get Node
            Node connectedNode = graph.getNode(neighbour);

            //Only double edges
            if(!connectedNode.onlyDoubleEdges()) continue;

            //Set to check against
            List<Integer> checkSet = new ArrayList<>(connectedNode.getOutIds());
            checkSet.removeIf(x -> x.equals(node.id));

            //Superset
            if(node.getOutIds().containsAll(checkSet)){
                return true;
            }
        }
        return false;
    }

    private static void doubleEdgeRemoveNormalEdges(Graph graph, Node node){

        //Return if not at least one double edge
        if(node.getOutIds().stream().noneMatch(x -> node.getInIds().contains(x))) return;

        //Get all ingoing (That are no double)
        List<Integer> ingoing = node.getInIds().stream().filter(x -> !node.getOutIds().contains(x)).collect(Collectors.toList());

        //Get all outgoing (That are no double)
        List<Integer> outgoing = node.getOutIds().stream().filter(x -> !node.getInIds().contains(x)).collect(Collectors.toList());

        //Remove edges
        if(ingoing.size() == 0) outgoing.forEach(x -> graph.removeEdge(node.id, x));
        if(outgoing.size() == 0) ingoing.forEach(x -> graph.removeEdge(x, node.id));

    }


}
