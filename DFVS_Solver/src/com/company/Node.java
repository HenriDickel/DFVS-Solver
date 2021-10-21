package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Node {

        public String label;
        public boolean deleted = false;

        private final List<Node> outNeighbours;

        public Node(String label){
            this.label = label;
            outNeighbours = new ArrayList<>();
        }

        public void addNeighbour(Node neighbour){
            outNeighbours.add(neighbour);
        }

        public List<Node> getOutNeighbours(){
            return outNeighbours.stream().filter(x -> !x.deleted).collect(Collectors.toList());
        }

        public void delete(){
            deleted = true;
        }

        public void unDelete(){
            deleted = false;
        }

        @Override
        public String toString(){
            String nodeString = "[" + label + "]";
            List<String> outNeighboursStrings = outNeighbours.stream().map(x -> x.label).collect(Collectors.toList());
            String outNeighboursStringJoined = String.join(", ", outNeighboursStrings);

            if(!outNeighboursStrings.isEmpty()) outNeighboursStringJoined = " {" + outNeighboursStringJoined + "}";

            return nodeString + outNeighboursStringJoined;
        }

}
