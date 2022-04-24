package program.packing;

import program.model.Cycle;
import program.model.Graph;
import program.model.Node;

public abstract class PackingRules {


    public static void upgradeK2Quad(Cycle pair, Graph packingGraph) {
        for(int i = 0; i < 2; i++) { // a <-> b
            Node a = pair.get(i);
            Node b = pair.get((i + 1) % 2);

            for(Integer cId: a.getOutIds()) {
                if(!cId.equals(b.id) && a.getInIds().contains(cId)){ // a <-> c
                    Node c = packingGraph.getNode(cId);
                    if(b.getOutIds().contains(cId)) { // b -> c
                        for (Integer dId : c.getOutIds()) { // c -> d
                            if (!dId.equals(a.id) && b.getInIds().contains(dId)) { // d -> b
                                Node d = packingGraph.getNode(dId);
                                pair.add(c);
                                pair.add(d);
                                pair.setK(2);
                                return;
                            }
                        }
                    } else if(b.getInIds().contains(cId)) { // c -> b
                        for (Integer dId : b.getOutIds()) { // b -> d
                            if (!dId.equals(a.id) && c.getInIds().contains(dId)) { // d -> c
                                Node d = packingGraph.getNode(dId);
                                pair.add(c);
                                pair.add(d);
                                pair.setK(2);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void upgradeFullyConnected(Cycle pair, Graph packingGraph) {

        Node a = pair.get(0);
        boolean upgrade = true;
        Node bestNode = null;
        while(upgrade) {
            upgrade = false;
            for(Integer outId: a.getOutIds()) {
                if(pair.isFullyConnected(outId)) {
                    Node out = packingGraph.getNode(outId);
                    if(bestNode == null || out.getMinInOut() < bestNode.getMinInOut()) {
                        bestNode = out;
                    }
                }
            }
            // When there is an upgrade node, upgrade cycle
            if(bestNode != null) {
                pair.add(bestNode);
                pair.setK(pair.getK() + 1);
                upgrade = true;
                bestNode = null;
            }
        }
    }

    public static void upgradeTriforce(Cycle triangle, Graph packingGraph) {
        for(int i = 0; i < 3; i++) {
            Node a = triangle.get(i);
            Node b = triangle.get((i + 1) % 3);
            Node c = triangle.get((i + 2) % 3);
            // Due to the structure of Light BFS, the cycle goes c -> b -> a

            for (Integer dId : a.getOutIds()) {
                if (!dId.equals(c.id) && b.getInIds().contains(dId)) { // a -> d -> b
                    for (Integer eId : b.getOutIds()) {
                        if (!eId.equals(a.id) && c.getInIds().contains(eId)) { // b -> e -> c
                            for (Integer fId : c.getOutIds()) {
                                if (!fId.equals(b.id) && a.getInIds().contains(fId)) { // c -> f -> a
                                    // Look for seventh node
                                    Node d = packingGraph.getNode(dId);
                                    Node e = packingGraph.getNode(eId);
                                    Node f = packingGraph.getNode(fId);

                                    for(Integer xId: c.getOutIds()) {
                                        if(e.getInIds().contains(xId)) {
                                            if(f.getInIds().contains(xId) && a.getOutIds().contains(xId)) {
                                                if(d.getInIds().contains(xId) && b.getOutIds().contains(xId)) {
                                                    Node x = packingGraph.getNode(xId);
                                                    triangle.add(d);
                                                    triangle.add(e);
                                                    triangle.add(f);
                                                    triangle.add(x);
                                                    triangle.setK(3);
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                    for(Integer xId: c.getInIds()) {
                                        if(f.getOutIds().contains(xId)) {
                                            if(a.getInIds().contains(xId) && d.getOutIds().contains(xId)) {
                                                if(d.getInIds().contains(xId) && b.getOutIds().contains(xId)) {
                                                    Node x = packingGraph.getNode(xId);
                                                    triangle.add(d);
                                                    triangle.add(e);
                                                    triangle.add(f);
                                                    triangle.add(x);
                                                    triangle.setK(3);
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void upgradeK2Penta(Cycle triangle, Graph packingGraph) {
        for(int i = 0; i < 3; i++) {
            Node a = triangle.get(i);
            Node b = triangle.get((i + 1) % 3);
            Node c = triangle.get((i + 2) % 3);
            // Due to the structure of Light BFS, the cycle goes c -> b -> a

            for (Integer dId : a.getOutIds()) {
                if (!dId.equals(c.id) && b.getInIds().contains(dId)) { // a -> d -> b exists
                    if(c.getOutIds().contains(dId)) {
                        Node d = packingGraph.getNode(dId);
                        for(Integer eId: d.getOutIds()) {
                            if(!eId.equals(b.id) && c.getInIds().contains(eId)) {
                                Node e = packingGraph.getNode(eId);
                                triangle.add(d);
                                triangle.add(e);
                                triangle.setK(2);
                                return;
                            }
                        }
                    }
                    if(c.getInIds().contains(dId)) {
                        Node d = packingGraph.getNode(dId);
                        for(Integer eId: d.getInIds()) {
                            if(!eId.equals(a.id) && c.getOutIds().contains(eId)) {
                                Node e = packingGraph.getNode(eId);
                                triangle.add(d);
                                triangle.add(e);
                                triangle.setK(2);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public static Cycle checkTriangle(Cycle bestPair, Graph packingGraph) {

        for(Node node: bestPair.getNodes()) {
            if(node.getInIdCount() < 2 || node.getOutIdCount() < 2 || !node.hasOnlyDoubleEdges()) return null;
        }
        for(Node node: bestPair.getNodes()) {
            if(node.getOutIdCount() < 2) continue;

            Node bestA = null;
            Node bestB = null;
            int minInOutSumMin = Integer.MAX_VALUE;
            for(Integer aId: node.getOutIds()) {
                if(node.getInIds().contains(aId)) { // check out nodes with double edge
                    for(Integer bId: node.getOutIds()) {
                        if(!bId.equals(aId) && node.getInIds().contains(aId)) { // check out nodes with double edge
                            Node a = packingGraph.getNode(aId);
                            if(a.getOutIds().contains(bId) && a.getInIds().contains(bId)) {
                                Node b = packingGraph.getNode(bId);

                                if(a.getMinInOut() + b.getMinInOut() < minInOutSumMin) {
                                    bestA = a;
                                    bestB = b;
                                    minInOutSumMin = a.getMinInOut() + b.getMinInOut();
                                }
                            }
                        }
                    }
                }
            }
            if(bestA != null) {
                Cycle triangle = new Cycle(node, bestA, bestB);
                triangle.setK(2);
                return triangle;
            }
        }
        return null;
    }
}
