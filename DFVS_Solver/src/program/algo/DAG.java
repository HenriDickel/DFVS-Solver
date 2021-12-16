package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.List;


public abstract class DAG {

    public static boolean isDAG(Graph graph){

        //Start Recursion
        return isDAGRecursive(new ArrayList<>(), graph);
    }

    public static boolean isDAGFast(Graph graph) {
        Graph dagGraph = graph.copy();

        while(true) {
            List<Integer> checkedIds = new ArrayList<>();
            for(Node node: dagGraph.getNodes()) {
                if(node.getInIdCount() == 0 || node.getOutIdCount() == 0) {
                    checkedIds.add(node.id);
                }
            }
            for(Integer id: checkedIds) {
                dagGraph.removeNode(id);
            }
            if(checkedIds.isEmpty()) {
                return dagGraph.getNodeCount() == 0;
            }
        }
    }

    private static boolean isDAGRecursive(List<Node> checked, Graph graph){

        if(checked.size() == graph.getNodeCount()) return true;

        //Check if something can be removed
        for(Node node : graph.getNodes()){

            //Skip those already checked
            if(checked.contains(node)) continue;

            //Get all out going arcs
            List<Node> outNodes = graph.getOutNodes(node);

            //Remove those with no arcs
            if(outNodes.size() == 0){
                checked.add(node);
                return isDAGRecursive(checked, graph);
            }

            //Remove those where all neighbours are already checked
            if(checked.containsAll(outNodes)){
                checked.add(node);
                return isDAGRecursive(checked, graph);
            }
        }
        return false;
    }
}
