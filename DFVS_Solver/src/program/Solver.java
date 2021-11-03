package program;

import program.utils.DAG;
import program.utils.FirstCycle;
import program.log.Log;
import program.utils.MinMaxK;


import java.io.File;
import java.io.IOException;
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
        int k = MinMaxK.minK(graph);

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

        //Result
        List<Node> nodes = new ArrayList<>();

        //Run for all sub graphs
        try{
            for(Graph subGraph: cyclicSubGraphs) {
                nodes.addAll(dfvsSolve(subGraph));
            }
        }
        catch(TimeoutException timeoutException){
            Long time = Timer.stop();
            Log.mainLog(graph.name, nodes.size(), time, false);
            Log.debugLog(graph.name, "Found no solution in " + Timer.format(time), true);
            return new ArrayList<>();
        }

        //program.Node Labels
        List<String> nodeLabels = nodes.stream().map(node -> node.label).collect(Collectors.toList());

        //Stop program.Timer
        Long time = Timer.stop();

        //Verify
        boolean verified = false;
        try{
            Scanner scan = new Scanner(new File("src/inputs/optimal_solution_sizes.txt"));
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                if(line.startsWith(graph.name)){
                    String optimalK = line.split("     ")[1];
                    verified = Integer.parseInt(optimalK) == nodeLabels.size();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Log
        Log.mainLog(graph.name, nodes.size(), time, verified);
        Log.debugLog(graph.name, "Found solution with k = " + nodes.size() + " in " + Timer.format(time), !verified);

        //Return
        return nodes;
    }

}
