# DFVS Solver

## Participants

Henri Dickel  
Matija Miskovic  
Lennart Uhrmacher

## Mentors

Prof. Dr. Christian Komusiewicz  
M.Sc. Frank Sommer

# Origin

We are students of the Philipps University of Marburg. In our course "Algorithm Engineering" by Prof. Dr. Komusiewicz, we dealt with different approaches to solve the "Directed Feedback Vertex Set" problem. We decided to continue working on our project after finishing the course and to submit our solver in the PACE Challenge 2022.

# Concept 

Our solver is an exact solver that uses java.   

Note: 
- In the following **OPT** will be used as the number of elements in the solution **S** 
- A **double-edge** between a and b describes the two edges (a,b) and (b,a)


# Reduction rules

Reduction rules are used to make the (input) graph smaller. 
In this chapter we will explain which reduction rules we are using.   
Note: Our algorithm might combine some of those in order to be more efficient. 

## Simple Reduction Rules

We call a reduction rule **simple** if it does not add a node to the solution S, but instead changes the graph in a way that makes it easier/faster to be solved.  
We are using the following simple reduction rules:  

- Removing of nodes without in- or without out-edges 
  - Those are by definition not part of a cycle and therefore not relevant  
- Chaining rule
  - If some vertex v has only one ingoing edge (u, v), then remove v and add edges from u to all of v’s out-neighbors
  - Similar rule for single outgoing edge
- Tarjan Algorithm
  - Decomposition into strongly connected components
  - Also removes unnecessary edges between the components
- Remove trivial edge for double-edged nodes
  - If a node has double-edges and in addition only in- or instead only out-edges, then those "single" edges can be removed


## Advanced Reduction Rules

We call a reduction rule **advanced** if it does reduce the graph by removing nodes and adding them to the solution.

- Removing nodes with self-edges
  - Add node to S if it has an self edge
- Subset rule
  - If all edges of a node v are a subset of a double-connected neighbor w, then add w to S
- Fully-connected subgraph
  - If all nodes of subgraph SG are double-connected to all other nodes in SG and a node v $\in$ SG exists, which has no additional edges, add all but v to S 
- Double chain remove
  - If a node x has exactly two double-connected neighbors a and b, delete x and b, connect all edges of b to a and add a node "b/x" to S
  - In the end it can be determined if "b/x" is b or x depending on whether a has been deleted

# SCIP

We use the non-commercial mixed integer programming (MIP) solver SCIP. 
Previously we used our own branch and bound solver, however we switched to an ILP approach because of slightly better results.  
We access SCIP through a Java interface called "JSCIPOpt". 
JSCIPopt is open-source and licensed under a MIT-license.   
You can access it here: 
<a href="https://github.com/scipopt/JSCIPOpt">JSCIPOpt</a>

# ILP-Constraints

- For double-edges and cliques we add linear constraints
- For single-edges we add linear ordering constraints
- We also use a cycle packing approach for additional constraints
  
# Installation Description

- We are using Java 16
- You have to install SCIP and add it to the system path: <a href="https://www.scipopt.org/index.php#download">SCIP</a>




