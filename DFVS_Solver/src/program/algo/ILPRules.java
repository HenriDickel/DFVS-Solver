package program.algo;

import gurobi.*;
import program.log.Log;
import program.model.Cycle;
import program.model.Graph;
import program.model.Instance;
import program.model.Node;
import program.utils.Timer;

import java.util.ArrayList;
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
            String cName = "";
            for (Node node : pair.getNodes()) {
                expr.addTerm(1.0, model.getVarByName("x" + node.id));
                cName += node.id;
                graph.removeNode(node.id);
            }
            model.addConstr(expr,GRB.GREATER_EQUAL, pair.size()-1, cName);
        }

        model.update();
    }

}
