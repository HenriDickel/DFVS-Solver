package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // Custom Tests
        if(false) {
            solveTests();
            return;
        }

        //As args
        if(args.length > 0){
            String fileName = args[0];
            Graph graph = GraphFileManager.CreateFromFile("", fileName);
            List<Node> nodes = Solver.dfvs_solve(graph);
            GraphFileManager.SaveSolution("Solution_" + fileName, nodes);
            return;
        }


        //LogLevel
        Log.Clear();
        Log.type = Log.LogType.File;
        Log.detail = Log.LogDetail.Important;


        solveSynthetics();

        /*List<Graph> Complex = GraphFileManager.CreateFromFolder("src/com/company/complex");
        Complex.forEach(x -> {
            List<Node> nodes = Solver.dfvs_solve(x);
            GraphFileManager.SaveSolution("src/com/company/solutions/complex/" + x.name, nodes);
        } );*/
    }

    public static void solveSynthetics() {
        List<Graph> Synthetics = GraphFileManager.CreateFromFolder("src/com/company/synthetic");
        Synthetics.forEach(x -> {
            List<Node> nodes = Solver.dfvs_solve(x);
            GraphFileManager.SaveSolution("src/com/company/solutions/synthetic/" + x.name, nodes);
            verify(x.name, "src/com/company/synthetic/" + x.name, "src/com/company/solutions/synthetic/" + x.name);
        } );
    }

    // For testing the performance with a few graphs, that take a while
    public static void solveSelectedGraphs() {
        List<Graph> graphs = new ArrayList<>();
        graphs.add(GraphFileManager.CreateFromFile("src/com/company/synthetic/", "synth-n_30-m_117-k_10-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile("src/com/company/synthetic/", "synth-n_30-m_111-k_8-p_0.2.txt"));
        graphs.add(GraphFileManager.CreateFromFile("src/com/company/synthetic/", "synth-n_40-m_114-k_10-p_0.1.txt"));
        graphs.add(GraphFileManager.CreateFromFile("src/com/company/synthetic/", "synth-n_40-m_114-k_15-p_0.1.txt"));
        graphs.forEach(graph -> {
            List<Node> nodes = Solver.dfvs_solve(graph);
            GraphFileManager.SaveSolution("src/com/company/solutions/synthetic/" + graph.name, nodes);
            verify(graph.name, "src/com/company/synthetic/" + graph.name, "src/com/company/solutions/synthetic/" + graph.name);
        } );
    }

    public static void solveTests() {
        //Solver.dfvs_solve(TestCreator.createSimpleDAG());
        //Solver.dfvs_solve(TestCreator.createSimpleNonDAG());
        //Solver.dfvs_solve(TestCreator.createComplexNonDAG());
        Solver.dfvs_solve(TestCreator.createK3Test());
    }

    public static boolean verify(String graphName, String inputGraph, String solutionPath){
        try{

            String command = "py src/com/company/dfvs-verify.py " + inputGraph + " " + solutionPath;
            Process p = Runtime.getRuntime().exec(command);

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String ret = in.readLine();
            Log.log(Log.LogDetail.Important, graphName, ret != null ? "Verified" : "Not Verified");
        }catch(Exception e){e.printStackTrace();}

        return false;
    }
}
