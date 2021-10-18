package com.company;

public class Main {

    public static void main(String[] args) {
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

    }
}
