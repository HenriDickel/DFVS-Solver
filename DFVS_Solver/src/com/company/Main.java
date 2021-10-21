package com.company;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        //LogLevel
        Log.Clear();
        Log.type = Log.LogType.Console;
        Log.detail = Log.LogDetail.Important;

        //Custom Tests
        //Solver.dfvs_solve(TestCreator.createSimpleDAG());
        //Solver.dfvs_solve(TestCreator.createSimpleNonDAG());
        //Solver.dfvs_solve(TestCreator.createK3Test());

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



    }
}
