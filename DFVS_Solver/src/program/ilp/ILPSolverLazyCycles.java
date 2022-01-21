package program.ilp;
import gurobi.*;
import program.algo.DAG;
import program.algo.FullBFS;
import program.algo.Reduction;
import program.log.Log;
import program.model.Cycle;
import program.model.Graph;
import program.model.Node;
import program.utils.PerformanceTimer;
import program.utils.TimeoutException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ILPSolverLazyCycles  extends GRBCallback{

    private GRBModel model;
    private final Graph graph;

    public ILPSolverLazyCycles(Graph graph) {
        this.graph = graph;
    }

    protected void callback() {
        try {
            if (where == GRB.CB_MIPSOL) {
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
                    GRBLinExpr expr;
                    //Find new cycle
                    PerformanceTimer.log(PerformanceTimer.MethodType.ILP);
                    Cycle cycle = new FullBFS().findShortestCycle(copy);
                    PerformanceTimer.log(PerformanceTimer.MethodType.BFS);
                    //Add new lazy constraint
                    expr = new GRBLinExpr();
                    for(Node node: cycle.getNodes()){
                        GRBVar x = model.getVarByName("x" + node.id);
                        expr.addTerm(1.0, x);
                    }
                    addLazy(expr, GRB.GREATER_EQUAL, 1.0);
                }
            }
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode());
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.debugLog("GUROBI", "Error during callback: " + e.getMessage());
        }
    }


    public List<Integer> solve(long TIME_OUT, boolean useCyclePackingConstraints) {

        try {
            // Create empty environment, set options, and start
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "DFVS.log");
            env.set(GRB.IntParam.OutputFlag,0);
            env.start();

            // Create empty model
            this.model = new GRBModel(env);
            //Set time limit and limit system output
            model.set(GRB.IntParam.OutputFlag, 0);
            model.set(GRB.IntParam.Threads, 1);
            model.set(GRB.DoubleParam.TimeLimit, TIME_OUT);
            model.set(GRB.IntParam.LazyConstraints, 1);

            //Add a variable X for every node in the graph
            GRBLinExpr expr = new GRBLinExpr();
            for(Node node: graph.getNodes()){
                GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x" +node.id);
                expr.addTerm(1.0, x);
            }
            model.update();
            //Set objective to minimize the sum of all Xs
            model.setObjective(expr, GRB.MINIMIZE);

            //Find first cycle and add its constraint, the sum of all Xs of a cycle needs to be >=1 (at least one node needs to be deleted from the cycle)

            PerformanceTimer.log(PerformanceTimer.MethodType.ILP);
            Cycle cycle = new FullBFS().findShortestCycle(graph);
            PerformanceTimer.log(PerformanceTimer.MethodType.BFS);
            expr = new GRBLinExpr();
            for(Node node: cycle.getNodes()){
                GRBVar x = model.getVarByName("x" + node.id);
                expr.addTerm(1.0, x);
            }
            model.addConstr(expr,GRB.GREATER_EQUAL, 1.0, "c-0");

            model.update();

            if(useCyclePackingConstraints) {
                PerformanceTimer.log(PerformanceTimer.MethodType.ILP);
                ILPRules.addCyclePackingConstraint(model, graph);
                PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
            }

            //Callback setup
            model.setCallback(this);

            // Solve model and capture solution information
            model.optimize();

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

            PerformanceTimer.log(PerformanceTimer.MethodType.ILP);
            return result;

        } catch (GRBException e) {
            throw new TimeoutException("ILP Error: " + e.getErrorCode() + " - " + e.getMessage());
        }
    }
}
