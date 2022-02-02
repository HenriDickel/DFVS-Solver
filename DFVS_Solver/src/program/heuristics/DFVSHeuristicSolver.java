package program.heuristics;

import program.algo.*;
import program.log.Log;
import program.model.*;
import program.utils.Timer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class DFVSHeuristicSolver {

    public static void solveInstance(Instance instance) {

        Graph initialGraph = instance.subGraphs.get(0);

        // Preprocessing
        List<Integer> reduceS = Reduction.applyRules(initialGraph, true);
        instance.S.addAll(reduceS);

        // Create sub graphs
        instance.subGraphs = Preprocessing.findCyclicSubGraphs(initialGraph);
        Log.debugLog(instance.NAME, "Found " + instance.subGraphs.size() + " cyclic sub graph(s) with n = " + instance.subGraphs.stream().map(Graph::getNodeCount).collect(Collectors.toList()));

        // Apply rules on each sub graph
        for (Graph subGraph : instance.subGraphs) {
            List<Integer> reduceSubS = Reduction.applyRules(subGraph, true);
            instance.S.addAll(reduceSubS);
        }

        // Log preprocessing
        instance.preRemovedNodes = instance.N - instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();
        instance.startK = instance.S.size();
        Log.debugLog(instance.NAME, "Removed " + instance.preRemovedNodes + " nodes in preprocessing, starting with k = " + instance.startK);


        //---------Initial-----------------
        // 5.00 WorstSolution       //5.504
        //WorstSolution(instance);

        //---------DeleteNodes-------------
        // 5.01 Max_In              //1.319
        //Max_In(instance);
        // 5.02 Max_Out             //1.170
        //Max_Out(instance);
        // 5.03 Max_Max_In_Out      //1.161
        //Max_Max_In_Out(instance);
        // 5.04 Max_Min_In_Out      //1.044
        //Max_Min_In_Out(instance);

        //---------DeleteCircles-----------
        // 5.05 LightBFS            //1.632
        //LightBFS(instance);
        // 5.06 FullBFS_AllCycles   //1.239
        //FullBFS_AllCycles(instance);
        // 5.07 FullBFS_Multiple    //1.047
        //FullBFS_Multiple(instance);
        // 5.08 FullBFS             //1.038
        //FullBFS(instance);

        //---------Splits------------------
        // 5.09 Splits(1)           //1.038
        //Splits(instance, 1);
        // 5.10 Splits(3)           //1.034
        //Splits(instance, 3);
        // 5.11 Splits(5)           //1.030
        //Splits(instance, 5);

        //---------Combined----------------
        // 5.12 FullBFS_Max_Min     //1.017
        //FullBFS_Max_Min(instance);
        // 5.13 Splits_Max_Min(1)   //1.017
        //Splits_Max_Min(instance, 3);
        // 5.14 Splits_Max_Min(3)   //1.014
        //Splits_Max_Min(instance, 3);
        // 5.15 Splits_Max_Min(5)   //1.012
        //Splits_Max_Min(instance, 5);

        //---------TimerRandom-----------
        // 5.16 TimerRandom(100)    //1.004 - 77ms - 49682ms
        //TimerRandom(instance, 100);
        // 5.17 TimerRandom(1000)   //1.003
        //TimerRandom(instance, 1000);
        // 5.18 TimerRandom(3000)   //1.002
        //TimerRandom(instance, 3000);

        //---------TimerBreak--------------
        // 5.19 TimerBreak(1000)    //1.010 - 452ms
        //TimerBreak(instance, 1000);

        //---------TimerNodes--------------
        // 5.20 TimerNodes(100)     //1.086
        //TimerNodes(instance, 100);
        // 5.21 TimerNodes(1000)    //1.050 - 712ms
        //TimerNodes(instance, 1000);

        //---------TimerBorder-------------
        // 5.22 TimerBorder(100)    //1.004 - 61ms - 39547ms
        //TimerBorder(instance, 100);
        // 5.23 TimerBorder(1000)   //1.003 - 470ms - 302526
        //TimerBorder(instance, 1000);

        //---------TimerFast----------------
        // 5.24 TimerFast(100)       //
        TimerFast(instance, 100, 0.95f);
        // 5.25 TimerFast(1000)      //1.0025257 (in 907.6053 ms)
        //TimerFast(instance, 1000, 0.95f);
        // 5.26 TimerFast(10000)     //1.0018415 (in 8721.539 ms)
        //TimerFast(instance, 10000, 0.95f);

        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size());
    }

    //---------Initial-----------------

    private static void WorstSolution(Instance instance) {
        for (Graph subGraph : instance.subGraphs) {
            List<Integer> nodes = subGraph.getNodes().stream().map(x -> x.id).collect(Collectors.toList());
            instance.S.addAll(nodes);
        }
    }

    //---------DeleteNodes-------------

    private static void Max_In(Instance instance) {
        for (Graph subGraph : instance.subGraphs) {
            while (!DAG.isDAGFast(subGraph)) {
                Node node = Collections.max(subGraph.getNodes(), Comparator.comparing(Node::getInIdCount));
                subGraph.removeNode(node.id);
                instance.S.add(node.id);

                // Apply reduction rules
                List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                instance.S.addAll(reduceS);
            }
        }
    }

    private static void Max_Out(Instance instance) {
        for (Graph subGraph : instance.subGraphs) {
            while (!DAG.isDAGFast(subGraph)) {
                Node node = Collections.max(subGraph.getNodes(), Comparator.comparing(Node::getOutIdCount));
                subGraph.removeNode(node.id);
                instance.S.add(node.id);

                // Apply reduction rules
                List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                instance.S.addAll(reduceS);
            }
        }
    }

    private static void Max_Max_In_Out(Instance instance) {
        for (Graph subGraph : instance.subGraphs) {
            while (!DAG.isDAGFast(subGraph)) {
                Node node = Collections.max(subGraph.getNodes(), Comparator.comparing(x -> Math.max(x.getInIdCount(), x.getOutIdCount())));
                subGraph.removeNode(node.id);
                instance.S.add(node.id);

                // Apply reduction rules
                List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                instance.S.addAll(reduceS);
            }
        }
    }

    private static void Max_Min_In_Out(Instance instance) {
        for (Graph subGraph : instance.subGraphs) {
            while (!DAG.isDAGFast(subGraph)) {
                Node node = Collections.max(subGraph.getNodes(), Comparator.comparing(x -> Math.min(x.getOutIdCount(), x.getInIdCount())));
                subGraph.removeNode(node.id);
                instance.S.add(node.id);

                // Apply reduction rules
                List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                instance.S.addAll(reduceS);
            }
        }
    }

    //---------DeleteCircles-----------

    private static void LightBFS(Instance instance) {
        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            while (!DAG.isDAGFast(subGraph)) {
                Cycle cycle = LightBFS.findShortestCycle(subGraph);
                Node node = cycle.getNodes().get(0);

                subGraph.removeNode(node.id);
                instance.S.add(node.id);

                // Apply reduction rules
                List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                instance.S.addAll(reduceS);
            }
        }
    }

    private static void FullBFS_AllCycles(Instance instance) {
        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            while (!DAG.isDAGFast(subGraph)) {
                List<Cycle> cycles = FullBFS.getAllShortestCycles(subGraph);
                for (Cycle cycle : cycles) {
                    Node node = cycle.getNodes().get(0);

                    try {
                        subGraph.removeNode(node.id);
                        instance.S.add(node.id);


                    } catch (Exception ignored) {
                        //Node was already removed
                    }

                    // Apply reduction rules
                    List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                    instance.S.addAll(reduceS);
                }

            }
        }
    }

    private static void FullBFS_Multiple(Instance instance) {
        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            while (!DAG.isDAGFast(subGraph)) {
                List<Cycle> cycles = FullBFS.findMultipleShortestCycles(subGraph, true);
                for (Cycle cycle : cycles) {
                    Node node = cycle.getNodes().get(0);

                    try {
                        subGraph.removeNode(node.id);
                        instance.S.add(node.id);


                    } catch (Exception ignored) {
                        //Node was already removed
                    }

                    // Apply reduction rules
                    List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                    instance.S.addAll(reduceS);
                }

            }
        }
    }

    private static void FullBFS(Instance instance) {
        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            while (!DAG.isDAGFast(subGraph)) {
                Cycle cycle = FullBFS.findShortestCycle(subGraph);
                Node node = cycle.getNodes().get(0);

                subGraph.removeNode(node.id);
                instance.S.add(node.id);

                // Apply reduction rules
                List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                instance.S.addAll(reduceS);
            }
        }
    }

    //---------Splits------------------

    private static void Splits(Instance instance, int splitSize) {
        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            instance.S.addAll(SplitRec(subGraph, new ArrayList<>(), splitSize));
        }
    }

    private static List<Integer> SplitRec(Graph graph, List<Integer> solution, int splitCount) {

        if (DAG.isDAGFast(graph)) return solution;

        //All shortest
        List<Cycle> allShortestCycles = FullBFS.findMultipleShortestCycles(graph, true);

        //Border
        if (allShortestCycles.size() > splitCount) {
            allShortestCycles = allShortestCycles.subList(0, splitCount);
        }

        //Solutions
        List<List<Integer>> solutions = new ArrayList<>();

        //Split on all
        for (Cycle cycle : allShortestCycles) {
            Node node = cycle.getNodes().get(0);

            Graph splitGraph = graph.copy();
            splitGraph.removeNode(node.id);

            List<Integer> splitSolution = new ArrayList<>(solution);
            splitSolution.add(node.id);

            //reduction
            splitSolution.addAll(Reduction.applyRules(splitGraph, false));

            //Reduce SplitSize
            splitCount = splitCount == 1 ? 1 : splitCount - 1;

            solutions.add(SplitRec(splitGraph, splitSolution, splitCount));
        }

        //Find best
        return Collections.min(solutions, Comparator.comparing(List::size));

    }

    //---------Combined----------------

    private static void FullBFS_Max_Min(Instance instance) {
        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            while (!DAG.isDAGFast(subGraph)) {
                Cycle cycle = FullBFS.findShortestCycle(subGraph);
                Node node = Collections.max(cycle.getNodes(), Comparator.comparing(x -> Math.min(x.getOutIdCount(), x.getInIdCount())));

                subGraph.removeNode(node.id);
                instance.S.add(node.id);

                // Apply reduction rules
                List<Integer> reduceS = Reduction.applyRules(subGraph, false);
                instance.S.addAll(reduceS);
            }
        }
    }

    private static void Splits_Max_Min(Instance instance, int splitSize) {
        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            instance.S.addAll(SplitRec_Max_Min(subGraph, new ArrayList<>(), splitSize));
        }
    }

    private static List<Integer> SplitRec_Max_Min(Graph graph, List<Integer> solution, int splitCount) {

        if (DAG.isDAGFast(graph)) return solution;

        //All shortest
        List<Cycle> allShortestCycles = FullBFS.findMultipleShortestCycles(graph, true);

        //Border
        if (allShortestCycles.size() > splitCount) {
            allShortestCycles = allShortestCycles.subList(0, splitCount);
        }

        //Solutions
        List<List<Integer>> solutions = new ArrayList<>();

        //Split on all
        for (Cycle cycle : allShortestCycles) {
            Node node = Collections.max(cycle.getNodes(), Comparator.comparing(x -> Math.min(x.getOutIdCount(), x.getInIdCount())));

            Graph splitGraph = graph.copy();
            splitGraph.removeNode(node.id);

            List<Integer> splitSolution = new ArrayList<>(solution);
            splitSolution.add(node.id);

            //reduction
            splitSolution.addAll(Reduction.applyRules(splitGraph, false));

            //Reduce SplitSize
            splitCount = splitCount == 1 ? 1 : splitCount - 1;

            solutions.add(SplitRec_Max_Min(splitGraph, splitSolution, splitCount));
        }

        //Find best
        return Collections.min(solutions, Comparator.comparing(List::size));

    }

    //---------TimerRandom-----------

    private static void TimerRandom(Instance instance, int timeLimitMillis) {

        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            int timeForThisGraph = timeLimitMillis / instance.subGraphs.size();

            //All solutions
            List<List<Integer>> solutions = new ArrayList<>();

            while(timeForThisGraph > 0){
                LocalDateTime startTime = LocalDateTime.now();
                solutions.add(TimerRecRandom(subGraph, new ArrayList<>()));
                timeForThisGraph -= ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
            }

            //Need at least 1
            if(solutions.isEmpty()) solutions.add(TimerRecRandom(subGraph, new ArrayList<>()));

            //Best solution
            List<Integer> sol = Collections.min(solutions, Comparator.comparing(List::size));
            instance.S.addAll(sol);
        }
    }

    private static List<Integer> TimerRecRandom(Graph graph, List<Integer> solution) {

        if (DAG.isDAGFast(graph)) return solution;

        //All shortest
        List<Cycle> allShortestCycles = FullBFS.findMultipleShortestCycles(graph, true);

        //Random
        Random random = new Random();
        Cycle randomCycle = allShortestCycles.get(random.nextInt(allShortestCycles.size()));

        //Get best node
        Node node = Collections.max(randomCycle.getNodes(), Comparator.comparing(x -> Math.min(x.getOutIdCount(), x.getInIdCount())));

        //Copy
        Graph splitGraph = graph.copy();
        splitGraph.removeNode(node.id);

        //Solution
        List<Integer> splitSolution = new ArrayList<>(solution);
        splitSolution.add(node.id);

        //Reduction
        splitSolution.addAll(Reduction.applyRules(splitGraph, false));

        //Reduce SplitSize
        return TimerRecRandom(splitGraph, splitSolution);

    }

    //---------TimerBreak------------

    private static List<List<Integer>> timerBreakSolutions;

    private static void TimerBreak(Instance instance, int timeLimitInMillis) {

        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            timerBreakSolutions = new ArrayList<>();
            TimerRecBreak(subGraph, new ArrayList<>(), LocalDateTime.now(), timeLimitInMillis);
            List<Integer> bestSolution = Collections.min(timerBreakSolutions, Comparator.comparing(List::size));
            instance.S.addAll(bestSolution);
        }
    }

    private static void TimerRecBreak(Graph graph, List<Integer> solution, LocalDateTime startTime, int timeLimitInMillis) {

        if (DAG.isDAGFast(graph)) timerBreakSolutions.add(solution);

        //All shortest
        List<Cycle> allShortestCycles = FullBFS.findMultipleShortestCycles(graph, true);

        //Foreach
        for(Cycle cycle : allShortestCycles){

            //Check if break
            if(ChronoUnit.MILLIS.between(startTime, LocalDateTime.now()) > timeLimitInMillis) return;

            //Get best node
            Node node = Collections.max(cycle.getNodes(), Comparator.comparing(x -> Math.min(x.getOutIdCount(), x.getInIdCount())));

            //Copy
            Graph splitGraph = graph.copy();
            splitGraph.removeNode(node.id);

            //Solution
            List<Integer> splitSolution = new ArrayList<>(solution);
            splitSolution.add(node.id);

            //Reduction
            splitSolution.addAll(Reduction.applyRules(splitGraph, false));

            //Next call
            TimerRecBreak(splitGraph, splitSolution, startTime, timeLimitInMillis);

        }

    }


    //---------TimerNodes------------

    private static void TimerNodes(Instance instance, int timeLimitMillis) {

        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            int timeForThisGraph = timeLimitMillis / instance.subGraphs.size();

            //All solutions
            List<List<Integer>> solutions = new ArrayList<>();

            while(timeForThisGraph > 0){
                LocalDateTime startTime = LocalDateTime.now();
                solutions.add(TimerRecNodes(subGraph, new ArrayList<>()));
                timeForThisGraph -= ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
            }

            //Best solution
            List<Integer> sol = Collections.min(solutions, Comparator.comparing(List::size));
            instance.S.addAll(sol);
        }
    }

    private static List<Integer> TimerRecNodes(Graph graph, List<Integer> solution) {

        if (DAG.isDAGFast(graph)) return solution;

        //All shortest
        List<Cycle> allShortestCycles = FullBFS.findMultipleShortestCycles(graph, true);

        //Random
        Random random = new Random();
        Cycle randomCycle = allShortestCycles.get(random.nextInt(allShortestCycles.size()));

        //Get best node
        Node node = randomCycle.getNodes().get(random.nextInt(randomCycle.getNodes().size()));

        //Copy
        Graph splitGraph = graph.copy();
        splitGraph.removeNode(node.id);

        //Solution
        List<Integer> splitSolution = new ArrayList<>(solution);
        splitSolution.add(node.id);

        //Reduction
        splitSolution.addAll(Reduction.applyRules(splitGraph, false));

        //Reduce SplitSize
        return TimerRecNodes(splitGraph, splitSolution);

    }

    //---------TimerBorder-----------

    private static void TimerBorder(Instance instance, int timeLimitMillis) {

        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            int timeForThisGraph = timeLimitMillis / instance.subGraphs.size();

            //All solutions
            List<List<Integer>> solutions = new ArrayList<>();

            while(timeForThisGraph > 0){
                //Check if there are enough solutions
                if(solutions.size() > subGraph.getNodeCount() * subGraph.getEdgeCount()) break;

                LocalDateTime startTime = LocalDateTime.now();
                solutions.add(TimerRecRandom(subGraph, new ArrayList<>()));
                timeForThisGraph -= ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
            }

            //Best solution
            List<Integer> sol = Collections.min(solutions, Comparator.comparing(List::size));
            instance.S.addAll(sol);
        }
    }

    //---------TimerFast-------------

    private static void TimerFast(Instance instance, int timeLimitMillis, float precision) {

        float totalNodeCount = instance.subGraphs.stream().mapToInt(Graph::getNodeCount).sum();

        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            float percentage = (float) subGraph.getNodeCount() / totalNodeCount;
            long millisForThisGraph = (long) (timeLimitMillis * percentage);

            //All solutions
            List<List<Integer>> solutions = new ArrayList<>();

            long startTime = System.currentTimeMillis();

            while(System.currentTimeMillis() <= startTime + millisForThisGraph + 1){
                solutions.add(TimerRecFast(subGraph, new ArrayList<>(), precision));
            }

            //Best solution
            List<Integer> sol = Collections.min(solutions, Comparator.comparing(List::size));
            instance.S.addAll(sol);
        }
    }

    private static List<Integer> TimerRecFast(Graph graph, List<Integer> solution, float precision) {

        //Check if DAG
        if (DAG.isDAGFast(graph)) return solution;

        //All shortest
        List<Cycle> allShortestCycles = FullBFS.findMultipleShortestCycles(graph, false);

        //Copy
        Graph copyGraph = graph.copy();
        List<Integer> copySolution = new ArrayList<>(solution);

        //Random
        Random random = new Random();

        //Random
        for(int i = 0; i <= (float) allShortestCycles.size() * (1f - precision); i++){

            //Random cycle
            Cycle randomCycle = allShortestCycles.get(random.nextInt(allShortestCycles.size()));

            //Get best node
            Node node = Collections.max(randomCycle.getNodes(), Comparator.comparing(x -> Math.min(x.getOutIdCount(), x.getInIdCount())));

            //Don't delete twice
            if(copySolution.contains(node.id)) continue;

            //Remove node
            copyGraph.removeNode(node.id);
            copySolution.add(node.id);

            //Reduction
            copySolution.addAll(Reduction.applyRules(copyGraph, false));

            //Check if DAG
            if(DAG.isDAGFast(graph)) return solution;
        }

        //Do again
        return TimerRecFast(copyGraph, copySolution, precision);

    }


}
