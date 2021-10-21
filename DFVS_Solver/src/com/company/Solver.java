package com.company;

import java.util.*;

public class Solver {

    private static List<Node> dfvs_branch(Graph graph, int k){
        //Break
        if(k < 0) return null;
        if(graph.isDAG()) return new ArrayList<>();

        //Next Circle
        List<Node> cycle = graph.getCircle();

        //Loop
        for(Node node : cycle.subList(0, cycle.size())){
            Log.log(Log.LogDetail.Unimportant, graph.name, "Removing " + node.label + " from graph");
            node.delete();
            List<Node> S = dfvs_branch(graph, k - 1);
            Log.log(Log.LogDetail.Unimportant, graph.name, "Adding " + node.label + " back to graph");
            node.unDelete();
            if(S != null){
                S.add(node);
                return S;
            }
        }
        return null;
    }

    public static List<Node> dfvs_solve(Graph graph){
        Log.log(Log.LogDetail.Important, graph.name, "Start solving for: \n" + graph);

        int k = 0;
        List<Node> S = null;
        while(S == null){
            Log.log(Log.LogDetail.Normal, graph.name, "Try solving with k = " + k);
            S = dfvs_branch(graph, k);
            k++;
        }
        Log.log(Log.LogDetail.Important, graph.name,"Success with k = " + (k - 1) + "\n");
        return S;
    }

}
