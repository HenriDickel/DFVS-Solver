package com.company;

public abstract class TestCreator {

    public static Graph createSimpleDAG(){
        Graph result = new Graph("S1");

        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");
        Node d = new Node("D");
        Node e = new Node("E");

        a.addNeighbour(b);
        a.addNeighbour(c);
        b.addNeighbour(c);
        b.addNeighbour(e);
        c.addNeighbour(d);

        result.nodes.add(a);
        result.nodes.add(b);
        result.nodes.add(c);
        result.nodes.add(d);
        result.nodes.add(e);

        return result;
    }

    public static Graph createSimpleNonDAG(){
        Graph result = new Graph("S2");

        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");
        Node d = new Node("D");
        Node e = new Node("E");
        Node f = new Node("F");

        a.addNeighbour(b);
        a.addNeighbour(c);
        b.addNeighbour(c);
        b.addNeighbour(e);
        c.addNeighbour(a);
        e.addNeighbour(a);
        e.addNeighbour(f);
        f.addNeighbour(e);

        result.nodes.add(a);
        result.nodes.add(b);
        result.nodes.add(c);
        result.nodes.add(d);
        result.nodes.add(e);
        result.nodes.add(f);

        return result;
    }

    public static Graph createComplexNonDAG(){
        Graph result = new Graph("S3");

        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");
        Node d = new Node("D");
        Node e = new Node("E");

        a.addNeighbour(b);
        b.addNeighbour(e);
        e.addNeighbour(a);

        c.addNeighbour(d);
        d.addNeighbour(e);
        e.addNeighbour(c);

        result.nodes.add(a);
        result.nodes.add(b);
        result.nodes.add(c);
        result.nodes.add(d);
        result.nodes.add(e);

        return result;
    }

    public static Graph createK3Test(){
        Graph result = new Graph("S2");

        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");
        Node d = new Node("D");
        Node e = new Node("E");
        Node f = new Node("F");

        a.addNeighbour(b);
        b.addNeighbour(a);

        c.addNeighbour(d);
        d.addNeighbour(c);

        e.addNeighbour(f);
        f.addNeighbour(e);

        result.nodes.add(a);
        result.nodes.add(b);
        result.nodes.add(c);
        result.nodes.add(d);
        result.nodes.add(e);
        result.nodes.add(f);

        return result;
    }

}
