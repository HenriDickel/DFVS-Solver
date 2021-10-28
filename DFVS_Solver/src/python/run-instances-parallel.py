#!/usr/bin/env python

from __future__ import print_function
from subprocess import call

import sys
import os
import argparse
import glob
import subprocess as sp
import multiprocessing as mp

parser = argparse.ArgumentParser(description='A script for running instances parallel on the server.')
parser.add_argument('solver', type=str, help='path to your algorithm')
parser.add_argument('graph_files', type=str, help='list of graph files')
parser.add_argument('time_limit', type=int, help='time limit in seconds for each instance')
args = parser.parse_args()

# graph files can be data-list.txt

opt_sol_syn = open("../instances/solution_size_dfvs")
opt_sizes = dict()
for line in opt_sol_syn:
    sp_line = line.split()
    opt_sizes.update({sp_line[0] : sp_line[1]})
def work(in_file):
    """Defines the work unit on an input file"""
    # each line in the file contains a graph-file
    print(in_file, args.solver)
    split_line = in_file.split("/")
    split_line = split_line[-1]
    if split_line in opt_sizes:
    	opt_size = str(opt_sizes[split_line])
    else:
    	opt_size = "-1"
    #opt_size = "-1"
    print(in_file, args.solver, args.solver+".res", opt_size, split_line+".program.log", split_line+".time")
    sp.call(["bash","test-instance",in_file, args.solver, str(args.time_limit), args.solver+".res", opt_size, split_line+".program.log", split_line+".time"])
    return 0
 
if __name__ == '__main__':
    files = []
    # list of files
    for line in open(args.graph_files):
        if not line.startswith("#"):
            files += [line.strip()]
    
    #Set up the parallel task pool to use all available processors
    count = 24
    pool = mp.Pool(processes=count)
 
    #Run the jobs parallel
	
    pool.map(work, files)


