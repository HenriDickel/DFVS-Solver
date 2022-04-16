package program;

import gurobi.GRBException;
import program.algo.*;
import program.heuristics.HeuristicSolver;
import program.heuristics.Heuristics;
import program.log.Log;
import program.model.*;
import program.packing.Packings;
import program.utils.*;
import program.utils.TimeoutException;

import java.util.List;

public class Main {

    public static void main(String[] args) throws GRBException {

        if(args.length > 0){

            // Ignore Log
            Log.ignore = true;

            // Start timer
            Timer.start(90);

            // Path
            String fileName = args[0];

            // Create instance
            Instance instance = InstanceCreator.createFromFile(new GraphFile("", fileName, -1));

            // Solve
            HeuristicSolver.dfvsSolveInstance(instance);

            // Print solution
            for(Integer nodeId : instance.S){
                System.out.println(nodeId);
            }

            // Print recursive steps
            System.out.println("#recursive steps: " + instance.recursiveSteps);
        } else {

            //Reset Log
            Log.Clear();
            Log.ignore = false;

            //List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_3, null);
            //List<GraphFile> files = InstanceCreator.getSelectedFiles();
            //List<GraphFile> files = InstanceCreator.getUnsolvedFiles();
            //List<GraphFile> files = InstanceCreator.getHeuristicFiles(null);
            List<GraphFile> files = InstanceCreator.getPaceFiles(null);

            //Heuristics.testQuality(files);
            //Packings.testQuality(files);
            files.forEach(Main::run);
        }
    }

    private static void run(GraphFile file) {

        Timer.start(90);
        PerformanceTimer.reset();

        PerformanceTimer.start();
        Instance instance = InstanceCreator.createFromPaceFile(file);
        PerformanceTimer.log(PerformanceTimer.MethodType.FILE);

        try {
            Log.debugLog(instance.NAME, instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ")", Color.PURPLE);
            HeuristicSolver.dfvsSolveInstance(instance);

            // Verify
            instance.solvedK = instance.S.size();
            boolean verified = instance.solvedK == instance.OPTIMAL_K || instance.OPTIMAL_K == -1;

            // Log results
            Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), verified);
            Color color = verified ? Color.WHITE : Color.RED;
            Log.debugLog(instance.NAME, "Found solution k = " + instance.solvedK + " in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", color);
            PerformanceTimer.printResult(instance.NAME);

        } catch (TimeoutException e) {
            instance.solvedK = instance.S.size() + HeuristicSolver.currentK;

            // Log results
            Log.debugLogAdd("", true);
            Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), false);
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.RED);
            PerformanceTimer.printResult(instance.NAME);
        }
    }
}
