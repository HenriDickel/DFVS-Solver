package com.company;

import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        //LogLevel
        Log.Clear();
        Log.type = Log.LogType.File;
        Log.detail = Log.LogDetail.Important;

        /*
        List<Graph> Synthetics = GraphFileManager.CreateFromFolder("src/com/company/synthetic");
        Synthetics.forEach(x -> {
            List<Node> nodes = Solver.dfvs_solve(x);
            GraphFileManager.SaveSolution("src/com/company/solutions/synthetic/" + x.name, nodes);
        } );*/

        List<Graph> Complex = GraphFileManager.CreateFromFolder("src/com/company/complex");
        Complex.forEach(x -> {
            List<Node> nodes = Solver.dfvs_solve(x);
            GraphFileManager.SaveSolution("src/com/company/solutions/complex/" + x.name, nodes);
        } );

        //Graph K3Test = TestCreator.createK3Test();
        //Solver.dfvs_solve(K3Test);

        /*
        Graph DAG = TestCreator.createSimpleDAG();
        Graph NonDAG = TestCreator.createSimpleNonDAG();

        System.out.println("DAG: ");
        System.out.println(DAG);
        System.out.println("Is DAG? " + DAG.isDAG());

        System.out.println("");

        System.out.println("Non DAG: ");
        System.out.println(NonDAG);
        System.out.println("Is DAG? " + NonDAG.isDAG());
        System.out.println("Circle: " + NonDAG.getCircle().toString());

        System.out.println("");

        System.out.println(Solver.dfvs_solve(NonDAG));
*/
    }
}
