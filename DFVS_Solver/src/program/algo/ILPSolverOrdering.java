package program.algo;
import gurobi.*;
import program.log.Log;
import program.model.Graph;
import program.model.Instance;
import program.model.Node;
import program.utils.Timer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ILPSolverOrdering extends GRBCallback{

    private GRBVar[]  vars;
    private GRBModel model;
    private Graph graph;

    public ILPSolverOrdering(GRBVar[] xvars, Graph xgraph, GRBModel xmodel) {
        vars = xvars;
        graph = xgraph;
        model = xmodel;
    }

    @Override
    protected void callback() {
        try {
            //If we found a (non-minimal) Solution
            if (where == GRB.CB_MIPSOL) {

                //Get the values of the current solution
                double[] solutionValues = getSolution(vars);

                //Convert to node ids
                List<Integer> solutionNodes = new LinkedList<>();

                //Find what nodes should be deleted
                for (int i = 0; i < solutionValues.length; i++) {
                    if (solutionValues[i] > 0.9) {
                        solutionNodes.add(Integer.parseInt(vars[i].get(GRB.StringAttr.VarName).substring(1)));
                    }
                }

                //Get current solution
                System.out.println("MIP Solution: k = " + solutionNodes.size());
            }
        }
        catch (GRBException e){
            System.err.println("ILP Callback Error:\nCode: " + e.getErrorCode() + "\n" + e.getMessage());
        }
    }

    public static List<Integer> solveGraph(Graph graph, boolean useFullyUpgradedConstraints, boolean useInitalCirclesConstraints, boolean useCallback){

        try {
            // Create empty environment, set options, and start
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "DFVS.log");
            env.set(GRB.IntParam.OutputFlag,0);
            env.start();

            // Create empty model
            GRBModel model = new GRBModel(env);

            //Set time limit and limit command line output (comment out to lines of code enable it)
            model.set(GRB.DoubleParam.TimeLimit, Timer.timeout);
            model.set(GRB.IntParam.OutputFlag, 0);
            model.set(GRB.DoubleParam.Heuristics, 0.0);

            //Get all Nodes
            List<Node> g = graph.getNodes();

            //Expression Object
            GRBLinExpr expr = new GRBLinExpr();

            //Add variables x and u for every Node in graph g
            for (Node node: g){
                //X is the variable that shows if a vertex is deleted or not
                GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x" +node.id);
                //U is the position of the node in the topological ordering
                GRBVar u = model.addVar(0.0, g.size(), 0.0, GRB.INTEGER, "u" +node.id);
                expr.addTerm(1.0, x);
            }

            //Minimize the sum over all Xs
            model.setObjective(expr, GRB.MINIMIZE);

            //Update Model
            model.update();

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

            //Update Model
            model.update();

            //Additional Rules
            if(useFullyUpgradedConstraints) ILPRules.addFullyUpgradedConstraints(model, graph);
            if(useInitalCirclesConstraints) ILPRules.addInitialCircleConstraints(model, graph);

            //Callback setup
            if(useCallback){
                GRBVar[] vars = model.getVars();
                ILPSolverOrdering cb = new ILPSolverOrdering(vars, graph, model);
                model.setCallback(cb);
            }

            //Update Model
            model.update();

            //Start
            model.optimize();

            //Ids of deleted nodes
            List<Integer> result = new ArrayList<>();

            //Convert model variables back to node ids
            for(GRBVar var: model.getVars()) {
                double x = var.get(GRB.DoubleAttr.X);
                String varName = var.get(GRB.StringAttr.VarName);
                if(x > 0.9 && varName.startsWith("x")) {
                    int id = Integer.parseInt(varName.substring(1));
                    result.add(id);
                }
            }

            //Dispose
            model.dispose();
            env.dispose();

            //Return
            return result;

        } catch (GRBException e) {
            System.err.println("ILP Error:\nCode: " + e.getErrorCode() + "\n" + e.getMessage());
            return new ArrayList<>();

        }
    }

}