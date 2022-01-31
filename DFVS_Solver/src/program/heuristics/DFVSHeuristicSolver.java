package program.heuristics;

import program.algo.*;
import program.log.Log;
import program.model.Cycle;
import program.model.Graph;
import program.model.Instance;
import program.model.Node;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public abstract class DFVSHeuristicSolver {
    public static void solveInstance(Instance instance) {

        Log.debugLog(instance.NAME, "---------- " + instance.NAME + " (n = " + instance.N + ", m = " + instance.M + ", k = " + instance.OPTIMAL_K + ") ----------");

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
        FullBFS(instance);

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

        //---------CombinedTimer-----------
        // 5.16 Timer(100)         //1.004
        //Timer(instance, 100);
        // 5.17 Timer(1000)         //1.003
        //Timer(instance, 1000);
        // 5.18 Timer(3000)         //1.002
        //Timer(instance, 3000);

        Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size(), false);
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
                List<Cycle> cycles = FullBFS.findMultipleShortestCycles(subGraph);
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
        List<Cycle> allShortestCycles = FullBFS.findMultipleShortestCycles(graph);

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
        List<Cycle> allShortestCycles = FullBFS.findMultipleShortestCycles(graph);

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

    //---------CombinedTimer-----------

    private static void Timer(Instance instance, int timelimitMillis) {

        // Destroy cycles by heuristic
        for (Graph subGraph : instance.subGraphs) {
            int timeForThisGraph = timelimitMillis / instance.subGraphs.size();

            //All solutions
            List<List<Integer>> solutions = new ArrayList<>();

            while(timeForThisGraph > 0){
                LocalDateTime startTime = LocalDateTime.now();
                solutions.add(TimerRec_Random(subGraph, new ArrayList<>()));
                timeForThisGraph -= ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
            }

            //Best solution
            List<Integer> sol = Collections.min(solutions, Comparator.comparing(List::size));
            instance.S.addAll(sol);
        }
    }

    private static List<Integer> TimerRec_Random(Graph graph, List<Integer> solution) {

        if (DAG.isDAGFast(graph)) return solution;

        //All shortest
        List<Cycle> allShortestCycles = FullBFS.findMultipleShortestCycles(graph);

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
        return TimerRec_Random(splitGraph, splitSolution);

    }


}
