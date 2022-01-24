package program.ilp;
import gurobi.*;
import program.algo.DAG;
import program.algo.FullBFS;
import program.algo.PackingManager;
import program.log.Log;
import program.model.Cycle;
import program.model.Graph;
import program.model.Instance;
import program.model.Node;
import program.utils.PerformanceTimer;
import program.utils.TimeoutException;
import program.utils.Timer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ILPSolverLazyCycles  extends GRBCallback{

    private GRBModel model;
    private final Graph graph;
    private final long secondsLeft;
    private long numConstraints;

    public ILPSolverLazyCycles(Graph graph, long secondsLeft) {
        this.graph = graph;
        this.secondsLeft = secondsLeft;
        this.numConstraints = 0;
    }

    protected void callback() {
        try {
            if (where == GRB.CB_MIPSOL) {

                if(Timer.getSecondsLeft() <= 0) {
                    model.terminate();
                    return;
                }

                // MIP solution callback
                //Get the values of the current solution
                double[] solutionValues = getSolution(model.getVars());
                List<Integer> solutionNodes = new LinkedList<>();
                //Find what nodes should be deleted
                for(int i =0; i< solutionValues.length; i++){
                    if(solutionValues[i]>0.9){
                        solutionNodes.add(Integer.parseInt(model.getVars()[i].get(GRB.StringAttr.VarName).substring(1)));
                    }
                }
                //Copy the graph and delete the nodes from the current solution
                Graph copy = graph.copy();
                for(Integer i: solutionNodes){
                    copy.removeNode(i);
                }

                //Check if the graph has been solved, if not add another constraint
                if(!DAG.isDAG(copy)){

                    PerformanceTimer.start();
                    PackingManager pm = new PackingManager(copy);
                    PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
                    // For each cycle in packing
                    for(Cycle cycle: pm.getPacking()) {
                        GRBLinExpr expr = new GRBLinExpr();
                        for(Node node: cycle.getNodes()){
                            GRBVar x = model.getVarByName("x" + node.id);
                            expr.addTerm(1.0, x);
                        }
                        addLazy(expr, GRB.GREATER_EQUAL, cycle.getK());
                        numConstraints++;
                    }
                }
            }
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode());
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error during callback");
            e.printStackTrace();
        }
    }


    public List<Integer> solve(Instance instance, boolean useCyclePackingConstraints, boolean useInitialCyclesConstraint) {

        try {
            // Create empty environment, set options, and start
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "DFVS.log");
            env.set(GRB.IntParam.OutputFlag,0);
            env.start();

            //Get all nodes
            List<Node> g = graph.getNodes();

            // Create empty model
            this.model = new GRBModel(env);
            //Set time limit and limit system output
            model.set(GRB.DoubleParam.TimeLimit, secondsLeft);
            model.set(GRB.IntParam.Threads, 1);
            model.set(GRB.DoubleParam.Heuristics, 0.0);
            model.set(GRB.IntParam.LazyConstraints, 1);
            model.set(GRB.IntParam.OutputFlag, 0);

            //Add a variable X for every node in the graph
            GRBLinExpr expr = new GRBLinExpr();
            for(Node node: g){
                GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x" +node.id);
                expr.addTerm(1.0, x);
            }
            model.update();
            //Set objective to minimize the sum of all Xs
            model.setObjective(expr, GRB.MINIMIZE);

            //Find first cycle and add its constraint, the sum of all Xs of a cycle needs to be >=1 (at least one node needs to be deleted from the cycle)

            PerformanceTimer.start();
            Cycle cycle = FullBFS.findShortestCycle(graph);
            PerformanceTimer.log(PerformanceTimer.MethodType.BFS);
            expr = new GRBLinExpr();
            for(Node node: cycle.getNodes()){
                GRBVar x = model.getVarByName("x" + node.id);
                expr.addTerm(1.0, x);
            }
            model.addConstr(expr,GRB.GREATER_EQUAL, 1.0, "c-0");

            model.update();
            Log.debugLog("GUROBI", "Added " + model.getConstrs().length + " initial constraints to ILP Solver");

            PerformanceTimer.start();
            if(useCyclePackingConstraints) ILPRules.addCyclePackingConstraint(model, graph);
            if(useInitialCyclesConstraint) ILPRules.addInitialCircleConstraints(model, graph);
            PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);

            //Callback setup
            model.setCallback(this);

            model.update();

            // Update number of constraints in instance
            numConstraints += model.getConstrs().length;

            // Solve model and capture solution information
            PerformanceTimer.start();
            model.optimize();
            PerformanceTimer.log(PerformanceTimer.MethodType.ILP);

            instance.numConstraints += numConstraints;

            // Return no solution when timeout
            int status = model.get(GRB.IntAttr.Status);
            if(status != GRB.Status.OPTIMAL) throw new TimeoutException("Gurobi status: " + status);

            List<Integer> result = new ArrayList<>();

            for(GRBVar var: model.getVars()) {
                String varName = var.get(GRB.StringAttr.VarName);
                double x = var.get(GRB.DoubleAttr.X);
                if(x > 0.9 && varName.startsWith("x")) {
                    int id = Integer.parseInt(varName.substring(1));
                    result.add(id);
                }
            }

            model.dispose();
            env.dispose();

            return result;

        } catch (GRBException e) {
            instance.numConstraints += model.getConstrs().length;
            throw new TimeoutException("ILP Error: " + e.getErrorCode() + " - " + e.getMessage());
        }
    }
}
