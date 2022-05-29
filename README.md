# DFVS Solver
 
## Participants

Henri Dickel  
Matija Miskovic  
Lennart Uhrmacher

# Origin

We are students of the Philipps University of Marburg. In our course "Algorithm Engineering" with Prof. Dr. Komusiewicz, we dealt with different approaches to solve the "Directed Feedback Vertex Set" problem. We decided to continue working on our project after finishing the course and to use our solution in the PACE Challenge 2022.

# Concept 

Our solver is an exact solver that uses java.   
In the following **k** will be used as the number of elements in the solution. 

# Reduction rules

Reduction rules are used to make the (input) graph smaller. 
In this chapter we will explain which reduction rules we are using.   
Note: Our algorithm might combine some of those in order to be more efficient. 

## Simple Reduction Rules

We call a reduction rule _simple_ if it does not increase the k value directly, but instead changes the graph in a way that makes it easier to be solved.  
We are using the following _simple_ reduction rules:  

- Removing of Nodes with no in or no out edges 
  - Those are by definition not part of a cycle and therefore not relevant  
- Chaining rule
  - If some vertex v has only one ingoing arc (u, v), then remove v and add arcs from u to all of v’s out-neighbors
  - Similar für outgoing edges
- Tarjan Algorithm
  - Decomposition into strongly connected components

## Advanced Reduction Rules

We call a reduction rule _advanced_ if it does reduce the graph by deleting nodes that will be in the solution. This will not just make the graph smaller but also increase k.

TODO

- a
- b
- c

# SCIP

We use the non-commercial mixed integer programming (MIP) solver SCIP. 
Previously we used our own branch and bound solver, however we decided to use SCIP instead due to its speed.  
We access SCIP through a Java interface called "JSCIPOpt". 
JSCIPopt is open-source and licensed under a MIT-license.   
You can access it here: 
[JSCIPOpt]("https://github.com/scipopt/JSCIPOpt")

# Packing

TODO

# Heuristic

Even though we developed an exact solver, we decided to use heuristics to create an upper bound for our solver. Our heuristic approach claims a certain amount of time (3 seconds) to try out different simple heuristic approaches. We then take the best result as an cutting plane for SCIP. 

# Installation Description

We are using Java 16. 

## Installing SCIP

You can find an installation guide in the JSCIPOpt readme. However we explain the steps here more detailed:  

