package program.ilp;

import gurobi.*;
import program.algo.DAG;
import program.log.Log;
import program.model.Cycle;
import program.model.Graph;
import program.model.Instance;
import program.model.Node;
import program.packing.PackingManager;
import program.utils.PerformanceTimer;
import program.utils.TimeoutException;
import program.utils.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ILPSolverVertexCover extends GRBCallback{

    private GRBModel model;
    private Graph graph;
    private long secondsLeft;
    private long numConstraints;

    public ILPSolverVertexCover(Graph graph, long secondsLeft) {
        this.graph = graph;
        this.secondsLeft = secondsLeft;
        this.numConstraints = 0;
    }

    @Override
    protected void callback() {
        try {
            if (where == GRB.CB_MIPSOL) {

                if (Timer.getSecondsLeft() <= 0) {
                    model.terminate();
                    return;
                }

                // MIP solution callback
                //Get the values of the current solution
                double[] solutionValues = getSolution(model.getVars());
                List<Integer> solutionNodes = new LinkedList<>();
                //Find what nodes should be deleted
                for (int i = 0; i < solutionValues.length; i++) {
                    if (solutionValues[i] > 0.9) {
                        solutionNodes.add(Integer.parseInt(model.getVars()[i].get(GRB.StringAttr.VarName).substring(1)));
                    }
                }
                //Copy the graph and delete the nodes from the current solution
                Graph copy = graph.copy();
                for (Integer i : solutionNodes) {
                    copy.removeNode(i);
                }

                //Check if the graph has been solved, if not add another constraint
                if (!DAG.isDAG(copy)) {

                    PerformanceTimer.start();

                    PackingManager pm = new PackingManager(copy, 1000L);
                    PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
                    // For each cycle in packing
                    for (Cycle cycle : pm.getPacking()) {
                        GRBLinExpr expr = new GRBLinExpr();
                        for (Node node : cycle.getNodes()) {
                            GRBVar x = model.getVarByName("x" + node.id);
                            expr.addTerm(1.0, x);
                        }
                        addLazy(expr, GRB.GREATER_EQUAL, cycle.getK());
                        numConstraints++;
                    }
                    //Log.debugLog("GUROBI", "Added " + pm.getPacking().size() + " cycle constraints in callback");
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

    public List<Integer> solve(Instance instance){

        try {
            // Create empty environment, set options, and start
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "DFVS.log");
            env.set(GRB.IntParam.OutputFlag,0);
            env.start();

            // Create empty model
            this.model = new GRBModel(env);

            //Set time limit and limit command line output (comment out to lines of code enable it)
            model.set(GRB.DoubleParam.TimeLimit, secondsLeft);
            model.set(GRB.IntParam.Threads, 1);
            model.set(GRB.IntParam.OutputFlag, 0);
            model.set(GRB.DoubleParam.Heuristics, 0.0);
            model.set(GRB.IntParam.LazyConstraints, 1);

            //Get all Nodes
            List<Node> nodes = graph.getNodes();

            //Expression Object
            GRBLinExpr obj = new GRBLinExpr();

            //Add variables x and u for every Node in graph g
            for (Node node: nodes){
                //X is the variable that shows if a vertex is deleted or not
                GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x" + node.id);
                obj.addTerm(1.0, x);
            }

            //Minimize the sum over all Xs
            model.setObjective(obj, GRB.MINIMIZE);

            //Update Model
            model.update();

            //Add constraint Uv -Uw+n*Xw >=1 for every arc(w,u)
            for(int i = 0; i < nodes.size(); i++){
                for(int j = 0; j < nodes.get(i).getFullyConnectedIds().size(); j++){

                    Integer xId = nodes.get(i).id;
                    Integer yId = nodes.get(i).getFullyConnectedIds().get(j);
                    if(xId > yId) continue;

                    GRBLinExpr expr = new GRBLinExpr();
                    GRBVar x = model.getVarByName("x" + xId);
                    expr.addTerm(1.0, x);
                    GRBVar y = model.getVarByName("x" + yId);
                    expr.addTerm(1.0, y);
                    model.addConstr(expr, GRB.GREATER_EQUAL, 1.0, i + "-" + j);
                }
            }

            //Update Model
            model.update();
            //Log.debugLog("GUROBI", "Added " + model.getConstrs().length + " initial constraints");

            //Update Model
            model.update();

            //Callback setup
            model.setCallback(this);

            model.update();

            // Update number of constraints in instance
            numConstraints += model.getConstrs().length;

            //Start
            PerformanceTimer.start();
            model.optimize();
            PerformanceTimer.log(PerformanceTimer.MethodType.ILP);

            // Update number of constraints in instance
            instance.numConstraints += model.getConstrs().length;

            // Return no solution when timeout
            int status = model.get(GRB.IntAttr.Status);
            if(status != GRB.Status.OPTIMAL) {
                model.dispose();
                env.dispose();
                throw new TimeoutException();
            }

            //Ids of deleted nodes
            List<Integer> result = new ArrayList<>();

            //Convert model variables back to node ids
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
            System.out.println(e);
            throw new TimeoutException();
        }
    }

}