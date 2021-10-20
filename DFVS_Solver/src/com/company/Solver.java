package com.company;

import java.util.*;

public class Solver {

    public static Set<Graph.GraphNode> dfvs_branch(Graph graph, int k){
        //Break
        if(k < 0) return null;
        if(graph.isDAG()) return new HashSet<>();

        //Next Circle
        List<Graph.GraphNode> cycle = graph.getCircle();

        //Loop
        for(Graph.GraphNode node : cycle.subList(0, cycle.size())){
            Graph reducedGraph = graph.copy().removeNode(node);
            Set<Graph.GraphNode> S = dfvs_branch(reducedGraph, k - 1);
            if(S != null){
                S.add(node);
                return S;
            }
        }
        return null;
    }

    public static Set<Graph.GraphNode> dfvs_solve(Graph graph){
        Log.log("Start solving for: \n" + graph);

        int k = 0;
        Set<Graph.GraphNode> S = null;
        while(S == null){
            S = dfvs_branch(graph, k);
            k++;
        }
        Log.log("Success with k = " + (k - 1) + "\n");
        return S;
    }

}
