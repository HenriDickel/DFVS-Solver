import sys

# script to test if a vertex set is a directed feedback vertex set
# first parameter: graph file
# second parameter: list of vertices (solution)
# the script tests if the deletion of all vertices of the list in the graph results in a DAG

graph = open(sys.argv[1].rstrip())

deletion_set = open(sys.argv[2])

edges = []
vertices_to_delete = []
vertices_in_graph = set()

for line in graph:
    if not (line.startswith('#') or line.startswith('%')):
        fields = line.split()
        edges.append(str(fields[0]) + " " + str(fields[1]))
        vertices_in_graph.add(fields[0])
        vertices_in_graph.add(fields[1])

for line in deletion_set:
    if not (line.startswith('#') or line.startswith('%')):
        vertices_to_delete.append(line.rstrip())
        vertices_in_graph.remove(line.rstrip())

size = len(vertices_to_delete)

# remove all edge including a vertex in the deletion set
for vertex in vertices_to_delete:
    del_set = []
    for edge in edges:
        fields = edge.split()
        if vertex == fields[0] or vertex == fields[1]:
            del_set.append(edge)
    for edge in del_set:
        edges.remove(edge)

# now remove vertices with no incomming edge until the graph is empty
# otherwise there is an error

flag = False

while len(edges) > 0:
    no_candidates = set()
    for edge in edges:
        fields = edge.split()
        no_candidates.add(fields[1])
    if no_candidates == vertices_in_graph:
        flag = True
        break
    else:
        candidates = vertices_in_graph.difference(no_candidates)
        vertex_to_remove = candidates.pop()
        vertices_in_graph.remove(vertex_to_remove)
        del_set = []
        for edge in edges:
            fields = edge.split()
            if vertex_to_remove == fields[0] or vertex_to_remove == fields[1]:
                del_set.append(edge)
        for edge in del_set:
            edges.remove(edge)

if flag is False:
    print(size)
else:
    sys.stderr.write("The given vertex set is no directed feedback vertex set!\n")
