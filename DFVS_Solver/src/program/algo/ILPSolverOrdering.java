package program.algo;
import gurobi.*;
import program.log.Log;
import program.model.Instance;
import program.model.Node;
import program.utils.Timer;

import java.util.ArrayList;
import java.util.List;

public class ILPSolverOrdering{

    public static void solveInstance(Instance instance) throws GRBException {

        Timer.start();

        try {
            // Create empty environment, set options, and start
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "DFVS.log");
            env.start();

            // Create empty model
            GRBModel model = new GRBModel(env);
            //Set time limit and limit command line output (comment out to lines of code enable it)
            model.set(GRB.DoubleParam.TimeLimit, Timer.timeout);
            model.set(GRB.IntParam.OutputFlag, 0);
            model.set(GRB.DoubleParam.Heuristics, 0.0);

            //Get all Nodes
            List<Node> g = instance.subGraphs.get(0).getNodes();
            GRBLinExpr expr = new GRBLinExpr();
            //Add variables x and u for every Node in graph g
            for (Node node: g){
                //X is the variable that shows if a vertex is deleted or not
                GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x" +node.id);
                //U is the position of the node in the topological ordering
                GRBVar u = model.addVar(0.0, g.size(), 0.0, GRB.INTEGER, "u" +node.id);
                expr.addTerm(1.0, x);
            }
            model.update();
            //Minimize the sum over all Xs
            model.setObjective(expr, GRB.MINIMIZE);

            //Add constraint Uv -Uw+n*Xw >=1 for every arc(w,u)
            for(int i =0; i < g.size(); i++){
                for(int j =0; j <g.get(i).getOutIds().size(); j++){
                    expr = new GRBLinExpr();
                    expr.addTerm(1.0, model.getVarByName("u" + g.get(i).getOutIds().get(j)));
                    expr.addTerm(-1.0, model.getVarByName("u" + g.get(i).id));
                    expr.addTerm(g.size(), model.getVarByName("x" + g.get(i).id));
                    model.addConstr(expr,GRB.GREATER_EQUAL, 1.0, "c-" + g.get(i).getOutIds().get(j) + "--" + g.get(i).id);
                }
            }
            model.update();
            model.optimize();

            // Log
            for(GRBVar var: model.getVars()) {
                double x = var.get(GRB.DoubleAttr.X);
                String varName = var.get(GRB.StringAttr.VarName);
                if(x > 0.9 && varName.startsWith("x")) {
                    int id = Integer.parseInt(varName.substring(1));
                    instance.S.add(id);
                }
            }
            instance.solvedK = instance.S.size();
            long millis = Timer.getMillis();
            Log.mainLog(instance, millis, 0, true);
            Log.debugLog(instance.NAME, "Found solution with k = " + instance.S.size() + " in " + Timer.format(millis), false);

            model.dispose();
            env.dispose();

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());

            // Log
            long millis = Timer.getMillis();
            Log.mainLog(instance, millis, 0, false);
            Log.debugLog(instance.NAME, "Found no solution in " + Timer.format(millis), true);

        }
    }
}
