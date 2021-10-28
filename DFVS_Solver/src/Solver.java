import log.Log;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Solver {

    private static List<Node> dfvs_branch(Graph graph, int k, int recursionLevel) throws TimeoutException {

        //Timer
        if(Timer.isTimeout()) throw new TimeoutException("The program stopped after " + Timer.timeout + " minutes.");

        //Break
        if(k < 0) return null;

        //Return if graph has no circles
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

    public static List<Node> dfvsSolve(Graph graph){
        //Start k
        int k = 0;

        //Solution
        List<Node> S = null;

        //Loop
        while(S == null){
            S = dfvs_branch(graph, k, 0);
            k++;
        }

        //Return solution
        return S;
    }

    public static List<Node> dfvsSolveSubgraphs(Graph graph) {

        //Start Timer
        Timer.start();

        //Create Sub Graphs
        List<Graph> cyclicSubGraphs = Preprocessing.findCyclicSubGraphs(graph);

        List<Node> nodes = new ArrayList<>();
        for(Graph subGraph: cyclicSubGraphs) {
            nodes.addAll(dfvsSolve(subGraph));
        }

        //Node Labels
        List<String> nodeLabels = nodes.stream().map(node -> node.label).collect(Collectors.toList());

        //Stop Timer
        Long time = Timer.stop();

        //Verify
        boolean verified = false;
        try{
            String command = "py src/python/dfvs-verify.py " + graph + " " + String.join("\n", nodeLabels);
            Process p = Runtime.getRuntime().exec(command);

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            verified = Objects.equals(in.readLine(), "true");
        }
        catch(Exception ignored){}

        //Log
        Log.MainLog(graph.name, nodes.size(), time, verified);

        //Return
        return nodes;
    }

}
