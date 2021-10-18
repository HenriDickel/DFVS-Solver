package com.company;

public abstract class TestCreator {

    public static Graph createSimpleDAG(){
        Graph result = new Graph();

        Graph.GraphNode a = new Graph.GraphNode("A");
        Graph.GraphNode b = new Graph.GraphNode("B");
        Graph.GraphNode c = new Graph.GraphNode("C");
        Graph.GraphNode d = new Graph.GraphNode("D");
        Graph.GraphNode e = new Graph.GraphNode("E");

        result.nodes.add(a);
        result.nodes.add(b);
        result.nodes.add(c);
        result.nodes.add(d);
        result.nodes.add(e);

        result.arcs.add(new Graph.Arc(a, b));
        result.arcs.add(new Graph.Arc(a, c));
        result.arcs.add(new Graph.Arc(b, c));
        result.arcs.add(new Graph.Arc(b, e));
        result.arcs.add(new Graph.Arc(c, d));

        return result;
    }

    public static Graph createSimpleNonDAG(){
        Graph result = new Graph();

        Graph.GraphNode a = new Graph.GraphNode("A");
        Graph.GraphNode b = new Graph.GraphNode("B");
        Graph.GraphNode c = new Graph.GraphNode("C");
        Graph.GraphNode d = new Graph.GraphNode("D");
        Graph.GraphNode e = new Graph.GraphNode("E");
        Graph.GraphNode f = new Graph.GraphNode("F");

        result.nodes.add(a);
        result.nodes.add(b);
        result.nodes.add(c);
        result.nodes.add(d);
        result.nodes.add(e);
        result.nodes.add(f);

        result.arcs.add(new Graph.Arc(a, b));
        result.arcs.add(new Graph.Arc(a, c));
        result.arcs.add(new Graph.Arc(c, a));
        result.arcs.add(new Graph.Arc(b, c));
        result.arcs.add(new Graph.Arc(b, e));
        result.arcs.add(new Graph.Arc(e, a));
        result.arcs.add(new Graph.Arc(e, f));
        result.arcs.add(new Graph.Arc(f, e));

        return result;
    }

}
