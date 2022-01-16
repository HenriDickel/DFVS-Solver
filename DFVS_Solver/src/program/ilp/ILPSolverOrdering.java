package program.ilp;
import gurobi.*;
import program.model.Graph;
import program.model.Node;
import program.utils.PerformanceTimer;
import program.utils.TimeoutException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ILPSolverOrdering extends GRBCallback{

    private GRBModel model;
    private Graph graph;
    private long secondsLeft;

    public ILPSolverOrdering(Graph graph, long secondsLeft) {
        this.graph = graph;
        this.secondsLeft = secondsLeft;
    }

    @Override
    protected void callback() {
        try {
            //If we found a (non-minimal) Solution
            if (where == GRB.CB_MIPSOL) {

                //Get the values of the current solution
                double[] solutionValues = getSolution(model.getVars());

                //Convert to node ids
                List<Integer> solutionNodes = new LinkedList<>();

                //Find what nodes should be deleted
                for (int i = 0; i < solutionValues.length; i++) {
                    if (solutionValues[i] > 0.9) {
                        solutionNodes.add(Integer.parseInt(model.getVars()[i].get(GRB.StringAttr.VarName).substring(1)));
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

    public List<Integer> solve(boolean useFullyUpgradedConstraints, boolean useInitalCirclesConstraints, boolean useCyclePackingConstraints, boolean useCallback){

        try {
            // Create empty environment, set options, and start
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "DFVS.log");
            env.set(GRB.IntParam.OutputFlag,0);
            env.start();

            // Create empty model
            GRBModel model = new GRBModel(env);

            //Set time limit and limit command line output (comment out to lines of code enable it)
            model.set(GRB.DoubleParam.TimeLimit, secondsLeft);
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
            if(useCyclePackingConstraints) ILPRules.addCyclePackingConstraint(model, graph);

            //Callback setup
            if(useCallback){
                model.setCallback(this);
            }

            //Update Model
            model.update();

            //Start
            PerformanceTimer.start();
            model.optimize();
            PerformanceTimer.log(PerformanceTimer.MethodType.ILP);

            // Return no solution when timeout
            int status = model.get(GRB.IntAttr.Status);
            if(status != GRB.Status.OPTIMAL) throw new TimeoutException("Gurobi status: " + status);

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
            throw new TimeoutException("ILP Error: " + e.getErrorCode() + " - " + e.getMessage());
        }
    }

}