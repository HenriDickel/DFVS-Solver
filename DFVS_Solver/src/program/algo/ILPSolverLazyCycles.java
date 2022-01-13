package program.algo;
import gurobi.*;
import program.model.Cycle;
import program.model.Graph;
import program.model.Instance;
import program.model.Node;

import java.util.LinkedList;
import java.util.List;

public class ILPSolverLazyCycles  extends GRBCallback{

    private GRBVar[]  vars;
    private GRBModel model;
    private Instance instance;

    public ILPSolverLazyCycles(GRBVar[] xvars, Instance xinstance, GRBModel xmodel) {
        vars = xvars;
        instance =xinstance;
        model = xmodel;

    }

    protected void callback() {
        try {
            if (where == GRB.CB_MIPSOL) {
                // MIP solution callback
                //Get the values of the current solution
                double[] solutionValues = getSolution(vars);
                List<Integer> solutionNodes = new LinkedList<>();
                //Find what nodes should be deleted
                for(int i =0; i< solutionValues.length; i++){
                    if(solutionValues[i]>0.9){
                        solutionNodes.add(Integer.parseInt(vars[i].get(GRB.StringAttr.VarName).substring(1)));
                    }
                }
                //Copy the graph and delete the nodes from the current solution
                Graph copy = instance.subGraphs.get(0).copy();
                for(Integer i: solutionNodes){
                    copy.removeNode(i);
                }

                //Check if the graph has been solved, if not add another constraint
                if(!DAG.isDAG(copy)){
                    GRBLinExpr expr;
                    //Find new cycle
                    Cycle shortCycle = FullBFS.findShortestCycle(copy);

                    //Add new lazy constraint
                    expr = new GRBLinExpr();
                    for(Node node: shortCycle.getNodes()){
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
            System.out.println("Error during callback");
            e.printStackTrace();
        }
    }


    public static void solveInstance(Instance instance) throws GRBException {
        try {
            // Create empty environment, set options, and start
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "DFVS.log");
            env.start();

            //Get all nodes
            Graph graph = instance.subGraphs.get(0);
            List<Node> g = graph.getNodes();

            // Create empty model
            GRBModel model = new GRBModel(env);
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
            Cycle shortCycle = FullBFS.findShortestCycle(graph);
            expr = new GRBLinExpr();
            for(Node node: shortCycle.getNodes()){
                GRBVar x =model.getVarByName("x" + node.id);
                expr.addTerm(1.0, x);
            }
            model.addConstr(expr,GRB.GREATER_EQUAL, 1.0, "c-0");
            model.update();

            //Callback setup
            GRBVar[] vars = model.getVars();
            ILPSolverLazyCycles cb   = new ILPSolverLazyCycles(vars, instance, model);
            model.setCallback(cb);

            // Solve model and capture solution information
            model.optimize();

            System.out.println(instance.NAME);
            System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
            System.out.println(instance.OPTIMAL_K);
            if(model.get(GRB.DoubleAttr.ObjVal)!= instance.OPTIMAL_K){
                System.err.println("Wrong solution. Found: " + model.get(GRB.DoubleAttr.ObjVal) + " Actual Value: " + instance.OPTIMAL_K);
            }
            model.dispose();
            env.dispose();

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }
    }
}
