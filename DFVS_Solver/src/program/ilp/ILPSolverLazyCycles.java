package program.ilp;
import gurobi.*;
import program.algo.DAG;
import program.algo.FullBFS;
import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ILPSolverLazyCycles  extends GRBCallback{

    private GRBModel model;
    private final Graph graph;
    private final Graph cycleGraph;

    public ILPSolverLazyCycles(Graph graph) {
        this.graph = graph;
        this.cycleGraph = graph.copy();
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
                    Cycle cycle = FullBFS.findShortestCycle(cycleGraph);
                    cycleGraph.removeCycle(cycle);
                    System.out.println("Add expression for cycle: " + cycle);
                    //Add new lazy constraint
                    expr = new GRBLinExpr();
                    for(Node node: cycle.getNodes()){
                        GRBVar x = model.getVarByName("x" + node.id);
                        expr.addTerm(1.0, x);
                    }
                    addLazy(expr, GRB.GREATER_EQUAL, 1.0);
                } else {
                    System.out.println("Optimal solution found");
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


    public List<Integer> solve() {

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
            model.set(GRB.IntParam.OutputFlag, 0);
            model.set(GRB.DoubleParam.TimeLimit, 180);
            model.set(GRB.IntParam.LazyConstraints, 1);

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

            Cycle cycle = FullBFS.findShortestCycle(cycleGraph);
            cycleGraph.removeCycle(cycle);
            System.out.println("Add expression for cycle: " + cycle);
            expr = new GRBLinExpr();
            for(Node node: cycle.getNodes()){
                GRBVar x = model.getVarByName("x" + node.id);
                expr.addTerm(1.0, x);
            }
            model.addConstr(expr,GRB.GREATER_EQUAL, 1.0, "c-0");

            model.update();

            //Callback setup
            model.setCallback(this);

            // Solve model and capture solution information
            model.optimize();

            List<Integer> result = new ArrayList<>();

            for(GRBVar var: model.getVars()) {
                double x = var.get(GRB.DoubleAttr.X);
                String varName = var.get(GRB.StringAttr.VarName);
                if(x > 0.9 && varName.startsWith("x")) {
                    int id = Integer.parseInt(varName.substring(1));
                    result.add(id);
                }
            }

            model.dispose();
            env.dispose();

            return result;

        } catch (GRBException e) {
            System.err.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
            return new ArrayList<>();
        }

    }
}
