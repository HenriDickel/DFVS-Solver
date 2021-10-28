package GraphUtils;

import java.util.ArrayList;
import java.util.List;


public class DAG {

    public static boolean isDAG(){
        //Start Recursion
        return isDAGRecursive(new ArrayList<>());
    }

    private static boolean isDAGRecursive(List<Node> checked){

        if(checked.size() == nodes.size()) return true;

        //Check if something can be removed
        for(Node node : nodes){

            //Skip those already checked
            if(checked.contains(node)) continue;

            //Get all out going arcs
            List<Node> outArcs = node.getOutNeighbours();

            //Remove those with no arcs
            if(outArcs.size() == 0){
                checked.add(node);
                return isDAGRecursive(checked);
            }

            //Remove those where all neighbours are already checked
            if(checked.containsAll(outArcs)){
                checked.add(node);
                return isDAGRecursive(checked);
            }
        }
        return false;
    }
}
