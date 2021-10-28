package program;

import program.utils.DAG;
import program.utils.FirstCycle;
import program.log.Log;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Solver {

    private static List<Node> dfvsBranch(Graph graph, int k) throws TimeoutException {

        //program.Timer
        if(Timer.isTimeout()) throw new TimeoutException("The program stopped after " + Timer.timeout + " minutes.");

        //Return if graph has no circles
        if(DAG.isDAG(graph)) return new ArrayList<>();

        // Break to skip the redundant dfvs_branch()-call when k = 0
        if(k <= 0) return null;

        //Next Circle
        List<Node> cycle = FirstCycle.findFirstCycle(graph);

        //Loop
        for(Node node : cycle.subList(0, cycle.size())){
            node.delete();
            List<Node> S = dfvsBranch(graph, k - 1);
            node.unDelete();
            if(S != null){
                S.add(node);
                return S;
            }
        }
        return null;
    }

    public static List<Node> dfvsSolve(Graph graph){
        //Start k
        int k = 0;

        //Solution
        List<Node> S = null;

        //Loop
        while(S == null){
            S = dfvsBranch(graph, k);
            k++;
        }

        //Return solution
        return S;
    }

    public static List<Node> dfvsSolveSubGraphs(Graph graph) {

        //Start program.Timer
        Timer.start();

        //Create Sub Graphs
        List<Graph> cyclicSubGraphs = Preprocessing.findCyclicSubGraphs(graph);

        List<Node> nodes = new ArrayList<>();
        for(Graph subGraph: cyclicSubGraphs) {
            nodes.addAll(dfvsSolve(subGraph));
        }

        //program.Node Labels
        List<String> nodeLabels = nodes.stream().map(node -> node.label).collect(Collectors.toList());

        //Stop program.Timer
        Long time = Timer.stop();

        //Verify
        boolean verified = false;
        try{
            String command = "py src/python/dfvs-verify.py " + graph + " " + String.join("\n", nodeLabels);
            Process p = Runtime.getRuntime().exec(command);

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            System.out.println(in.readLine());
            verified = Objects.equals(in.readLine(), "1");
        }
        catch(Exception error){
            Log.debugLog(graph.name, "Could not verify because python error");
        }

        //Log
        Log.mainLog(graph.name, nodes.size(), time, verified);
        Log.debugLog(graph.name, "Found solution with k = " + nodes.size() + " in " + time + "ms", !verified);

        //Return
        return nodes;
    }

}
