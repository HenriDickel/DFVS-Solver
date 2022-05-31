package program.utils;

import program.model.Graph;
import program.model.Instance;

import java.util.Scanner;

public abstract class InstanceCreator {

    private static Instance createInstance(String name, Graph graph, int optimalK) {
        return new Instance(name, graph, optimalK);
    }

    public static Instance createPaceInstanceFromSystemIn() {

        Graph graph = new Graph();
        int id = 0;

        Scanner scan = new Scanner(System.in);
        while(scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] split = line.trim().split(" ");
            if(id == 0) { // first line
                Integer n = Integer.parseInt(split[0]);
                Integer m = Integer.parseInt(split[1]);
            } else {
                for(String outString: split) {
                    if(outString.length() == 0) continue;
                    Integer outId = Integer.parseInt(outString);
                    graph.addArc(id, outId);
                }
            }
            id++;
        }

        return createInstance("PACE", graph, -1);
    }

}
