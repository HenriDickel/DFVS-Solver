package com.company;

import com.company.util.GraphUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public abstract class Solver {

    public static LocalDateTime startTime;

    private static List<Node> dfvs_branch(Graph graph, int k, int recursionLevel) throws TimeoutException {

        //Timer
        if(ChronoUnit.MINUTES.between(startTime, LocalDateTime.now()) > 1) throw new TimeoutException("The programm stopped after 60 seconds");
        //if(ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()) > 10) throw new Exception();

        //Break
        if(k < 0) return null;

        if(graph.isDAG()) return new ArrayList<>();

        // Break to skip the redundant dfvs_branch()-call when k = 0
        if(k <= 0) return null;

        //Next Circle
        List<Node> cycle = GraphUtils.findFirstCycle(graph);
        //List<Node> cycle = graph.getCircle();
        Log.log(Log.LogDetail.Normal, graph.name, recursionLevel, "Found cycle: " + cycle);

        //Loop
        for(Node node : cycle.subList(0, cycle.size())){
            Log.log(Log.LogDetail.Normal, graph.name, recursionLevel, "Removing " + node.label + " from graph");
            node.delete();
            List<Node> S = dfvs_branch(graph, k - 1, recursionLevel + 1);
            Log.log(Log.LogDetail.Normal, graph.name, recursionLevel, "Adding " + node.label + " back to graph");
            node.unDelete();
            if(S != null){
                S.add(node);
                return S;
            }
        }
        return null;
    }

    public static List<Node> dfvs_solve(Graph graph){
        Log.log(Log.LogDetail.Important, graph.name, 0, "");
        Log.log(Log.LogDetail.Important, graph.name, 0, "Start solving for graph " + graph.name);

        //Timer
        startTime = LocalDateTime.now();

        int k = 0;
        List<Node> S = null;
        while(S == null){
            Log.log(Log.LogDetail.Unimportant, graph.name, 0, "Try solving with k = " + k + "...");
            try{
                S = dfvs_branch(graph, k, 0);
            }
           catch (TimeoutException e){
               Log.log(Log.LogDetail.Important, graph.name, 0,"Failed with k = " + (k - 1) + " because of the timelimit");
               return new ArrayList<>();
           }
            k++;
        }

        long seconds = ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
        long millis = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
        String timer = seconds == 0 ? millis + "ms" : seconds + "." + millis + "s";
        Log.log(Log.LogDetail.Important, graph.name, 0,"Success with k = " + (k - 1) + " in " + timer);
        Log.log(Log.LogDetail.Normal, graph.name, 0,"Nodes to remove: " + S);
        return S;
    }

}
