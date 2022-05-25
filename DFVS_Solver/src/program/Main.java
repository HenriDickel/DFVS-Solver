package program;

import jscip.*;
import program.algo.*;
import program.algo.Solver;
import program.ilp.ILPSolver;
import program.ilp.ILPSolverVertexCover;
import program.log.Log;
import program.model.*;
import program.packing.PackingManager;
import program.utils.*;
import program.utils.TimeoutException;
import program.utils.Timer;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {

        if(args.length > 0){

            // Ignore Log
            Log.ignore = true;

            System.loadLibrary("jscip");
            Scip scip = new Scip();
            scip.create("Example");

            // Start timer
            Timer.start(Integer.MAX_VALUE);

            // Create instance
            Instance instance = InstanceCreator.createPaceInstanceFromSystemIn();

            // Solve
            Solver.dfvsSolveInstance(instance);
            //ILPSolver.solve(instance);

            // Print solution
            for(Integer nodeId : instance.S){
                System.out.println(nodeId);
            }
        } else {

            //Reset Log
            Log.Clear();
            Log.ignore = false;

            testScip();

            //testPaceDataLite();
            //testPaceData();
            //testPacePacking();
            //testCorrectness();
            //exportPaceData();

            //testCorrectness();
            //testReduction();
            //paceReduction();
            //vertexCoverILP();

            //List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_3, null);
            //List<GraphFile> files = InstanceCreator.getSelectedFiles();
            //List<GraphFile> files = InstanceCreator.getUnsolvedFiles();
            //List<GraphFile> files = InstanceCreator.getHeuristicFiles(null);
            //List<GraphFile> files = InstanceCreator.getPaceFiles(null);

            //files.forEach(Main::reductionExport);
            //files.forEach(x -> run(x, true));
            //files.forEach(x -> run(x, false));
        }
    }

    private static void testScip() {
        System.loadLibrary("jscip");
        List<GraphFile> files = InstanceCreator.getPaceFilesExact("e_087");
        GraphFile file = files.get(0);

        Timer.start(90);
        PerformanceTimer.reset();

        PerformanceTimer.start();
        Instance instance = InstanceCreator.createFromPaceFile(file);
        Solver.instance = instance;
        PerformanceTimer.log(PerformanceTimer.MethodType.FILE);

        // Apply reduction rules once
        Log.debugLog(instance.NAME, "Applying reduction rules...");
        List<Integer> reduceS = Reduction.applyRules(instance.subGraphs.get(0), true);
        instance.S.addAll(reduceS);

        // Create sub graphs
        Log.debugLog(instance.NAME, "Applying Tarjan...");
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));

        // Apply reduction rules again
        Log.debugLog(instance.NAME, "Applying reduction rules again...");
        for(Graph subGraph: instance.subGraphs) {
            List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
            instance.S.addAll(reduceSubS);
        }

        int singleEdgeCount = 0;
        int overallEdgeCount = 0;
        try {
            for(Graph subGraph: instance.subGraphs) {

                // Count edges in subgraph
                for(Node node: subGraph.getNodes()) {
                    for(Integer outId: node.getOutIds()) {
                        if(!node.getInIds().contains(outId)) {
                            singleEdgeCount++;
                        }
                    }
                }
                overallEdgeCount += subGraph.getEdgeCount();

                // Solve with ILP
                Log.debugLog(instance.NAME, "Call SCIP for subgraph with |N| = " + subGraph.getNodeCount() + ", |M| = " + subGraph.getEdgeCount());
                scip(subGraph);
                //List<Integer> S = new ILPSolverVertexCover(subGraph, 90).solve(instance);
                //instance.S.addAll(S);
            }

            // Add nodes that were ambiguous to result
            instance.addAmbiguousResult();
            Log.debugLog(instance.NAME, "Single edge count: " + singleEdgeCount + "/" + overallEdgeCount);
            Log.debugLog(instance.NAME, "Found solution k = " + instance.S.size() + " in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.PURPLE);
        } catch(TimeoutException e) {

            // Log results
            Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), false);
            Log.debugLog(instance.NAME, "Single edge count: " + singleEdgeCount + "/" + overallEdgeCount);
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.RED);
        }
    }

    private static void scip(Graph graph) {


        Scip scip = new Scip();
        scip.create("Example");
        List<Variable> allVars = new ArrayList<>();
        // Create condition for each double edge
        for(Node node: graph.getNodes()) {
            allVars.add(scip.createVar("x-" + node.id, 0.0, 1.0, 1.0, SCIP_Vartype.SCIP_VARTYPE_INTEGER));
        }

        for(Node node: graph.getNodes()) {
            Variable a = getFromAllVars(allVars,node.id,"x");
            for(Integer outId: node.getOutIds()) {
                if(outId > node.id) {
                    // lower bound, upper bound, objective value (0, 1, 1)
                    Variable b = getFromAllVars(allVars,outId,"x");
                    double[] vals = {1.0, 1.0};
                    Variable[] vars = {a, b};
                    Constraint lincons = scip.createConsLinear("lin-cons-" + node.id + "-" + outId, vars, vals, 1, scip.infinity());
                    scip.addCons(lincons);
                }
            }
        }

        scip.solve();

        Solution sol = scip.getBestSol();
        System.out.println("Objective value = " + scip.getSolOrigObj(sol));
        if(sol != null )
        {
            System.out.println("Var Values: ");
            // TODO why are values emptys in the end?
            for( int i = 0; i < scip.getVars().length; i++)
            {
                Variable var = scip.getVars()[i];
                //System.out.println(var.getName() + " " + scip.getSolVal(sol, var));
            }
        }

        for( int i = 0; i < scip.getVars().length; i++) {
            Variable var = scip.getVars()[i];
            scip.releaseVar(var);
        }

        //scip.free();
    }

    private static Variable getFromAllVars(List<Variable> allVars, int id, String start){
        for(Variable var : allVars){
            if(var.getName().equals(start+"-"+id)){
                return var;
            }
        }
        return null;
    }

    private static void testPaceDataLite() {
        List<GraphFile> files = InstanceCreator.getPaceFilesExact(null);
        int[] optimalKs = new int[]{2, 674, 631, 276, 450, 1917, 547, 788, 364, 1137};

        for(int i = 0; i < 10; i++) {

            Timer.start(5);

            Instance instance = InstanceCreator.createFromPaceFile(files.get(i));

            Log.debugLog(instance.NAME, instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ")", Color.PURPLE);

            Log.ignore = true;
            try {
                Solver.dfvsSolveInstance(instance);
                Log.ignore = false;

                Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);

                // Verify
                instance.solvedK = instance.S.size();
                boolean verified = instance.solvedK == optimalKs[i];

                // Log results
                Color color = verified ? Color.WHITE : Color.RED;
                Log.debugLog(instance.NAME, "Found solution k = " + instance.solvedK + " in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", color);
            }
            catch (TimeoutException e) {
                Log.ignore = false;
                Log.debugLog(instance.NAME, "Found no solution in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.RED);
            }
        }
    }

    private static void testPaceData(){
        List<GraphFile> files = InstanceCreator.getPaceFilesExact("e_085");
        files = files.subList(0, 1);
        files.forEach(x -> run(x, true));
    }

    private static void exportPaceData(){
        List<GraphFile> files = InstanceCreator.getPaceFilesExact("e_085");
        files = files.subList(0, 1);
        files.forEach(Main::reductionExport);
    }

    private static void testCorrectness(){
        List<GraphFile> files = InstanceCreator.getComplexAndSyntheticFiles(Dataset.DATASET_3, null);
        files.forEach(x -> run(x, false));
    }

    private static void paceReduction() {
        List<GraphFile> files = InstanceCreator.getPaceFilesExact(null);
        Log.ignore = false;

        //List<GraphFile> files = InstanceCreator.getPaceFilesHeuristic(null);
        //files = files.subList(0, 67);

        int startKAgg = 0;
        int nodeCountAgg = 0;
        int edgeCountAgg = 0;
        int reducedNodeCountAgg = 0;
        int reducedEdgeCountAgg = 0;
        for(GraphFile file: files) {

            Instance instance = InstanceCreator.createFromPaceFile(file);
            Solver.instance = instance;

            // Apply reduction rules once
            Log.debugLog(instance.NAME, "Applying reduction rules...");
            List<Integer> reduceS = Reduction.applyRules(instance.subGraphs.get(0), true);
            instance.S.addAll(reduceS);

            // Create sub graphs
            Log.debugLog(instance.NAME, "Applying Tarjan...");
            instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));

            // Apply reduction rules again
            Log.debugLog(instance.NAME, "Applying reduction rules again...");
            for(Graph subGraph: instance.subGraphs) {
                List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
                instance.S.addAll(reduceSubS);
            }
            Log.debugLog(instance.NAME, "Reduced node count from " + instance.N + " to " + instance.getCurrentN());
            Log.debugLog(instance.NAME, "Reduced edge count from " + instance.M + " to " + instance.getCurrentM());
            Log.debugLog(instance.NAME, "Start k = " + (instance.startK + instance.ambigousS.size()));
            Log.paceLog(instance, 0L, instance.getCurrentN(), instance.getCurrentM(), instance.S.size(), 0, 0);

            nodeCountAgg += instance.N;
            edgeCountAgg += instance.M;
            reducedNodeCountAgg += instance.getCurrentN();
            reducedEdgeCountAgg += instance.getCurrentM();
            startKAgg += instance.S.size() + instance.ambigousS.size();

            System.out.println(instance.NAME + "\t" + (instance.S.size() + instance.ambigousS.size()) + " (S = " + instance.S.size() + ", ambiS = " + instance.ambigousS.size() + ")");
        }
        Log.debugLog("OVERALL", "Reduced node count from " + nodeCountAgg / (float) files.size() + " to " + reducedNodeCountAgg / (float) files.size());
        Log.debugLog("OVERALL", "Reduced edge count from " + edgeCountAgg / (float) files.size() + " to " + reducedEdgeCountAgg / (float) files.size());
        Log.debugLog("OVERALL", "Average start k = " + startKAgg / (float) files.size());

        System.out.println("Average start k = " + startKAgg / (float) files.size());
    }

    private static void testPacePacking() {
        List<GraphFile> files = InstanceCreator.getPaceFilesExact("e_085");
        files = files.subList(0, 3);

        int packingSize = 0;
        int packingSizeAgg = 0;
        for(GraphFile file: files) {

            Instance instance = InstanceCreator.createFromPaceFile(file);
            Solver.instance = instance;

            // Apply reduction rules once
            List<Integer> reduceS = Reduction.applyRules(instance.subGraphs.get(0), true);
            instance.S.addAll(reduceS);

            // Create sub graphs
            instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));

            // Apply reduction rules again
            for(Graph subGraph: instance.subGraphs) {
                List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
                instance.S.addAll(reduceSubS);
                PackingManager pm = new PackingManager(subGraph, 1000L);
                packingSize = pm.size();
            }
            Log.debugLog(instance.NAME, "|N| = " + instance.getCurrentN() + ", |M| = " + instance.getCurrentM());
            Log.debugLog(instance.NAME, "Packing size = " + packingSize);
            packingSizeAgg += packingSize;

            Log.paceLog(instance, 0L, instance.getCurrentN(), instance.getCurrentM(), instance.S.size(), packingSize, 0);
        }
        System.out.println("Average packing size = " + packingSizeAgg / 3f);
    }

    private static void testReduction() {
        List<GraphFile> files = InstanceCreator.getPaceFilesExact(null);
        files = files.subList(0, 50);

        int nodeCount = 0;
        int reduceNodeCount = 0;
        int tarjanNodeCount = 0;
        int secondReduceNodeCount = 0;
        int kAgg = 0;
        int reduceSAgg = 0;
        for(GraphFile file: files) {
            Instance instance = InstanceCreator.createFromPaceFile(file);
            nodeCount += instance.getCurrentN();

            // Apply reduction rules once
            Log.debugLog(instance.NAME, "Applying reduction rules...");
            List<Integer> reduceS = Reduction.applyRules(instance.subGraphs.get(0), true);
            instance.S.addAll(reduceS);
            reduceNodeCount += instance.getCurrentN();

            // Create sub graphs
            Log.debugLog(instance.NAME, "Applying Tarjan...");
            instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));
            tarjanNodeCount += instance.getCurrentN();

            // Apply reduction rules again
            Log.debugLog(instance.NAME, "Applying reduction rules again...");
            for(Graph subGraph: instance.subGraphs) {
                List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
                instance.S.addAll(reduceSubS);
            }
            secondReduceNodeCount += instance.getCurrentN();

            kAgg += instance.OPTIMAL_K;
            reduceSAgg += instance.S.size();
        }

        System.out.println("Average nodes: " + nodeCount / files.size() + " => " + reduceNodeCount / files.size() + " => " + tarjanNodeCount / files.size() + " => " + secondReduceNodeCount / files.size());
        System.out.println("Average k: " + kAgg / files.size() + ", reduced: " + reduceSAgg / files.size());
    }

    private static void reductionExport(GraphFile file){

        //Get instance
        Instance instance = InstanceCreator.createFromPaceFile(file);
        Solver.instance = instance;

        //Export original graph
        //Export.ExportGraph(instance, "original");

        //Reduce
        List<Integer> reduceInit = Reduction.applyRules(instance.subGraphs.get(0), true);
        instance.S.addAll(reduceInit);

        //Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));

        //Reduce again
        for(Graph graph : instance.subGraphs){
            List<Integer> reduce =  Reduction.applyRules(graph, true);
            instance.S.addAll(reduce);
        }
        Export.ExportGraph(instance, "reduced");
    }

    private static void run(GraphFile file, boolean isPaceData) {

        Timer.start(60);
        PerformanceTimer.reset();

        PerformanceTimer.start();
        Instance instance = isPaceData ? InstanceCreator.createFromPaceFile(file) : InstanceCreator.createFromFile(file);
        PerformanceTimer.log(PerformanceTimer.MethodType.FILE);

        try {
            Log.debugLog(instance.NAME, instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ")", Color.PURPLE);
            Solver.dfvsSolveInstance(instance);

            // Verify
            instance.solvedK = instance.S.size();
            boolean verified = instance.solvedK == instance.OPTIMAL_K || instance.OPTIMAL_K == -1;

            // Log results
            Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), verified);
            Color color = verified ? Color.WHITE : Color.RED;
            Log.debugLog(instance.NAME, "Found solution k = " + instance.solvedK + " in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", color);
            PerformanceTimer.printResult(instance.NAME);

        } catch (TimeoutException e) {
            instance.solvedK = instance.S.size() + Solver.currentK + instance.ambigousS.size();

            // Log results
            Log.debugLogAdd("", true);
            Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), false);
            Log.debugLog(instance.NAME, "Found no solution with k = " + instance.solvedK + " in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.RED);
            PerformanceTimer.printResult(instance.NAME);
        }
    }

    private static void vertexCoverILP() {
        List<GraphFile> files = InstanceCreator.getPaceFilesExact("e_085");
        //files = files.subList(0, 50);

        List<GraphFile> solved = new ArrayList<>();
        List<GraphFile> unsolved = new ArrayList<>();
        for(int i = 0; i < files.size(); i++) {
            GraphFile file = files.get(i);
            Timer.start(90);
            PerformanceTimer.reset();

            PerformanceTimer.start();
            Instance instance = InstanceCreator.createFromPaceFile(file);
            Solver.instance = instance;
            PerformanceTimer.log(PerformanceTimer.MethodType.FILE);

            // Apply reduction rules once
            Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Applying reduction rules...");
            List<Integer> reduceS = Reduction.applyRules(instance.subGraphs.get(0), true);
            instance.S.addAll(reduceS);

            // Create sub graphs
            Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Applying Tarjan...");
            instance.subGraphs = Preprocessing.findCyclicSubGraphs(instance.subGraphs.get(0));

            // Apply reduction rules again
            Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Applying reduction rules again...");
            for(Graph subGraph: instance.subGraphs) {
                List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
                instance.S.addAll(reduceSubS);
            }

            int singleEdgeCount = 0;
            int overallEdgeCount = 0;
            try {
                for(Graph subGraph: instance.subGraphs) {

                    // Count edges in subgraph
                    for(Node node: subGraph.getNodes()) {
                        for(Integer outId: node.getOutIds()) {
                            if(!node.getInIds().contains(outId)) {
                                singleEdgeCount++;
                            }
                        }
                    }
                    overallEdgeCount += subGraph.getEdgeCount();

                    // Solve with ILP
                    //Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Call Gurobi for subgraph with |N| = " + subGraph.getNodeCount() + ", |M| = " + subGraph.getEdgeCount());
                    //List<Integer> S = new ILPSolverVertexCover(subGraph, 90).solve(instance);
                    //instance.S.addAll(S);
                }

                // Add nodes that were ambiguous to result
                //instance.addAmbiguousResult();
                Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Single edge count: " + singleEdgeCount + "/" + overallEdgeCount);
                //Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Found solution k = " + instance.S.size() + " in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.PURPLE);
                //solved.add(file);
            } catch(TimeoutException e) {

                // Log results
                Log.mainLog(instance, Timer.getMillis(), PerformanceTimer.getPackingMillis(), false);
                Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Single edge count: " + singleEdgeCount + "/" + overallEdgeCount);
                Log.debugLog(instance.NAME + " (" + (i + 1) + ")", "Found no solution in " + Timer.getMillis() + " ms (recursive steps: " + instance.recursiveSteps + ")", Color.RED);
                unsolved.add(file);
            }
        }

        System.out.println("Solved " + solved.size() + "/" + files.size() + " instances");
        System.out.println("Unsolved: " + unsolved.stream().map(e -> e.name).collect(Collectors.toList()));
    }
}
