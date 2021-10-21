package com.company;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;

public class Main {

    public static void main(String[] args) {

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

        //Custom Tests
        //Solver.dfvs_solve(TestCreator.createSimpleDAG());
        //Solver.dfvs_solve(TestCreator.createSimpleNonDAG());
        //Solver.dfvs_solve(TestCreator.createK3Test());


        List<Graph> Synthetics = GraphFileManager.CreateFromFolder("src/com/company/synthetic");
        Synthetics.forEach(x -> {
            List<Node> nodes = Solver.dfvs_solve(x);
            GraphFileManager.SaveSolution("src/com/company/solutions/synthetic/" + x.name, nodes);
            verify(x.name, "src/com/company/synthetic/" + x.name, "src/com/company/solutions/synthetic/" + x.name);
        } );


        /*List<Graph> Complex = GraphFileManager.CreateFromFolder("src/com/company/complex");
        Complex.forEach(x -> {
            List<Node> nodes = Solver.dfvs_solve(x);
            GraphFileManager.SaveSolution("src/com/company/solutions/complex/" + x.name, nodes);
        } );*/

        //Test


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
