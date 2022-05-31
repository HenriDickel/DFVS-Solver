package program.algo;

import program.model.Graph;
import program.model.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class DAG {

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
}
