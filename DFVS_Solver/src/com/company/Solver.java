package com.company;

import java.util.*;

public class Solver {

    public static Set<Graph.GraphNode> dfvs_branch(Graph graph, int k){
        if(k < 0) return null;
        if(graph.isDAG()) return new HashSet<>();
        List<Graph.GraphNode> cycle = graph.getCircle();
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
        int k = 0;
        Set<Graph.GraphNode> S = null;
        while(S == null){
            System.out.println("Running dfvs_branch with k = " + k);
            S = dfvs_branch(graph, k);
            k++;
        }
        System.out.println("Success with k = " + (k - 1));
        return S;
    }

}
