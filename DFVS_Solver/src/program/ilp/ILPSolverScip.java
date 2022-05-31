package program.ilp;

import jscip.*;
import program.algo.DAG;
import program.model.Cycle;
import program.model.Graph;
import program.model.Node;
import program.algo.PackingRules;
import program.utils.TimeoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ILPSolverScip {

    public static List<Integer> solve(Graph graph, long timeLimit) {

        if(timeLimit < 0) throw new TimeoutException();

        Scip scip = new Scip();
        scip.create("SubgraphSolver");
        scip.hideOutput(true);

        scip.setRealParam("limits/time", timeLimit);

        // Add and create all variables
        Map<Integer, Variable> xMap = new HashMap<>();
        Map<Integer, Variable> uMap = new HashMap<>();
        final int N = graph.getNodeCount();
        for(Node node: graph.getNodes()) {
            // lower bound, upper bound, objective value (0, 1, 1)
            Variable x = scip.createVar("x-" + node.id, 0.0, 1.0, 1.0, SCIP_Vartype.SCIP_VARTYPE_BINARY);
            xMap.put(node.id, x);
            if(node.hasSingleEdge()) {
                Variable u = scip.createVar("u-" + node.id, 0.0, N, 0.0, SCIP_Vartype.SCIP_VARTYPE_INTEGER);
                uMap.put(node.id, u);
            }
        }

        // Add a linear constraint for each double edge a <-> b
        int numPairCons = 0;
        int numOrderingCons = 0;
        int numPackingCons = 0;

        Cycle pair;
        while((pair = graph.getFirstPairCycle()) != null) {
            PackingRules.upgradeFullyConnected(pair, graph);
            Variable[] vars = new Variable[pair.size()];
            double[] vals = new double[pair.size()];
            StringBuilder name = new StringBuilder("lin-cons");
            for(int i = 0; i < pair.size(); i++) {
                Node node = pair.get(i);
                Variable var = xMap.get(node.id);
                vars[i] = var;
                vals[i] = -1.0;
                name.append("-").append(node.id);
            }
            Constraint pairCons = scip.createConsLinear(name.toString(), vars, vals, -scip.infinity(), -(pair.size() - 1));
            scip.addCons(pairCons);
            numPairCons++;
            graph.removeEdgesFromFC(pair);
        }

        // For the remaining nodes, create ordering conditions
        for (Node node : graph.getNodes()) {
            for (Integer outId : node.getOutIds()) {
                Variable ua = uMap.get(node.id);
                Variable b = xMap.get(outId);
                Variable ub = uMap.get(outId);
                double[] vals = {-1.0, 1.0, -N};
                Variable[] vars = {ua, ub, b};
                Constraint orderingCons = scip.createConsLinear("ordering-cons-" + node.id + "-" + outId, vars, vals, -scip.infinity(), -1);
                scip.addCons(orderingCons);
                numOrderingCons++;
            }
        }

        while (!DAG.isDAGFast(graph)) {
            ILPPacking packing = new ILPPacking(graph);
            for (Cycle cycle : packing.cycles) {
                Variable[] varsCycle = new Variable[cycle.size()];
                double[] valsCycle = new double[cycle.size()];
                Node remove = null;
                for (int i = 0; i < cycle.size(); i++) {
                    Node node = cycle.get(i);
                    varsCycle[i] = xMap.get(node.id);
                    valsCycle[i] = -1;
                    if(remove == null || node.getMinInOut() < remove.getMinInOut()) {
                        remove = node;
                    }
                }
                Constraint packingCons = scip.createConsLinear("packing-cons-" + cycle, varsCycle, valsCycle, -scip.infinity(), -1);
                scip.addCons(packingCons);
                numPackingCons++;
                graph.removeNode(remove.id);
            }
        }

         scip.solve();
        List<Integer> S = new ArrayList<>();
        Solution sol = scip.getBestSol();

        if(sol != null && scip.getGap() == 0)
        {
            for(Variable var: xMap.values())
            {
                if(scip.getSolVal(sol,var) > 0.9){
                    int nodeId = Integer.parseInt(var.getName().substring(2));
                    S.add(nodeId);
                }
            }
            for( int i = 0; i < scip.getVars().length; i++) {
                Variable var = scip.getVars()[i];
                scip.releaseVar(var);
            }
            return S;
        }
        else {
           for( int i = 0; i < scip.getVars().length; i++) {
                Variable var = scip.getVars()[i];
                scip.releaseVar(var);
            }
            throw new TimeoutException();
        }
    }
}
