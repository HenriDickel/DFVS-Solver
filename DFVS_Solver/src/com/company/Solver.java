package com.company;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public abstract class Solver {

    public static LocalDateTime startTime;

    private static List<Node> dfvs_branch(Graph graph, int k) throws Exception {

        //Timer
        if(ChronoUnit.MINUTES.between(startTime, LocalDateTime.now()) > 1) throw new Exception();
        //if(ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()) > 10) throw new Exception();

        //Break
        if(k < 0) return null;

        if(graph.isDAG()) return new ArrayList<>();

        //Next Circle
        List<Node> cycle = graph.getCircle();

        // Break to skip the redundant dfvs_branch()-call when k = 0
        if(k <= 0) return null;

        //Loop
        for(Node node : cycle.subList(0, cycle.size())){
            Log.log(Log.LogDetail.Unimportant, graph.name, "Removing " + node.label + " from graph with k = " + k);
            node.delete();
            List<Node> S = dfvs_branch(graph, k - 1);
            Log.log(Log.LogDetail.Unimportant, graph.name, "Adding " + node.label + " back to graph with k = " + k);
            node.unDelete();
            if(S != null){
                S.add(node);
                return S;
            }
        }
        return null;
    }

    public static List<Node> dfvs_solve(Graph graph){
        Log.log(Log.LogDetail.Unimportant, graph.name, "Start solving for: \n" + graph);

        //Timer
        startTime = LocalDateTime.now();

        int k = 0;
        List<Node> S = null;
        while(S == null){
            Log.log(Log.LogDetail.Normal, graph.name, "Try solving with k = " + k);
            try{
                S = dfvs_branch(graph, k);
            }
           catch (Exception e){
               Log.log(Log.LogDetail.Important, graph.name,"Failed with k = " + (k - 1) + " because of the timelimit");
               return new ArrayList<>();
           }
            k++;
        }

        long seconds = ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
        long millis = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
        String timer = seconds == 0 ? millis + "ms" : seconds + "." + millis + "s";
        Log.log(Log.LogDetail.Important, graph.name,"Success with k = " + (k - 1) + " in " + timer);
        return S;
    }

}
