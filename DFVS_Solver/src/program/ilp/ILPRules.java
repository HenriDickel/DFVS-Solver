package program.ilp;

import gurobi.*;
import program.algo.FullBFS;
import program.algo.PackingManager;
import program.model.Cycle;
import program.model.Graph;
import program.model.Node;
import program.utils.PerformanceTimer;

import java.util.List;

import static program.algo.PackingRules.upgradeFullyConnected;

public abstract class ILPRules {

    public static void addFullyUpgradedConstraints(GRBModel model, Graph graph) throws GRBException {
        Cycle pair;
        GRBLinExpr expr;
        while((pair = graph.getFirstPairCycle()) != null) {
            // Look for fully connected triangles, quads etc.
            upgradeFullyConnected(pair,graph);
            expr = new GRBLinExpr();
            for (Node node : pair.getNodes()) {
                expr.addTerm(1.0, model.getVarByName("x" + node.id));
                graph.removeNode(node.id);
            }
            model.addConstr(expr,GRB.GREATER_EQUAL, pair.size()-1, "FullyUpgradedConstraint");
        }

        model.update();
    }

    public static void addInitialCircleConstraints(GRBModel model, Graph graph) throws GRBException {
        List<Cycle> cycles = FullBFS.getAllShortestCycles(graph);
        int index = 0;
        for(Cycle cycle: cycles) {
            GRBLinExpr expr = new GRBLinExpr();
            for(Node node: cycle.getNodes()){
                GRBVar x = model.getVarByName("x" + node.id);
                expr.addTerm(1.0, x);
            }
            model.addConstr(expr,GRB.GREATER_EQUAL, 1.0, "cycle-" + index++);
        }
        model.update();
    }

    public static void addCyclePackingConstraint(GRBModel model, Graph graph) throws GRBException {
        PerformanceTimer.start();
        PackingManager pm = new PackingManager(graph);
        PerformanceTimer.log(PerformanceTimer.MethodType.PACKING);
        int index = 0;
        for(Cycle cycle: pm.getPacking()) {
            GRBLinExpr expr = new GRBLinExpr();
            for(Node node: cycle.getNodes()){
                GRBVar x = model.getVarByName("x" + node.id);
                expr.addTerm(1.0, x);
            }
            model.addConstr(expr,GRB.GREATER_EQUAL, cycle.getK(), "cycle-" + index++);
        }
        model.update();
    }

}