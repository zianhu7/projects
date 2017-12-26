#!usr/bin/env python
from src.struct import *
from src.validator import Validator
import sys
import json
import re
import time
import os
import pycosat
import argparse
import itertools
from simanneal import Annealer

"""
Given input file with line by line buffer delimiters:
1. number of guests
2. number of constraints
3. list of names of guests in no particular order, needs to be alphnumeric string
    of at most 10 characters in string, and separated by commas, but not in string format
    i.e. A, B, C, D, etc.
4. Rows of constraints of form x y z, which indicates that the age of z is NOT between
    that of x or y

Output:
Post conversion to suitable format for pycosat processing and parsing, returns
satisfying ordering for all people. (Note that satisfying may not be optimal or
even the only solution to the problem)

To run: version of python + CSP.py + inputfile.in + outputfile.out

3SAT reduction:
Given i j k: create clauses
let X_{ij} := indicator of (i < j) in terms of age ordering
(X_{ki} or X_{jk}) and (not X_{ki} or not x_{jk}) essentially XOR each argument
This effectively chooses one of a clause and the complement in the other.
For instance, X_{ki} = X_{kj} = True; X_{jk} = X_{ik} = True are the two truth pairings
corresponding to (k i j), (k j i); (j i k), (i j k) respectively, which are the valid perms
that permute the possible valid orderings. Choosing one of these two truth values narrows
down the possible orderings to 2 from 4, and transitivity finalizes.

Transitivity of constraints:
Since X_{ij} and X_{jk} --> X_{ik} & X_{ji} and X_{kj} --> X_{ki}
(X_{ij} or X_{jk} or X_{ki}) and (X_{ji} or X_{kj} or X_{ik})
Triple XOR ensures only 2 of 3 literals in every clause can at most be true
"""
def solve(num_guests, num_constraints, guests, constraints):
    #preprocess
    processing_start = time.time()
    if DEBUG:
        print("Input with {} wizards and {} constraints".format(len(wizards),
                len(constraints)))

    #Generate VariableMap to create all possible variables
    VM = VariableMap(guests)
    assert(len(VM) == len(guests) * (len(guests) - 1) / 2)
    if DEBUG:
        print(VM)

    #Generate appropriate constraints using class container
    clauses = []
    for element in constraints:
        constraint = Constraint(element)
        pycosat_clauses = constraint.to_pycosat_clause(VM)
        clauses.extend(pycosat_clauses)
    assert(len(clauses) == len(constraints) * 2)
    if DEBUG:
        print("Clauses created were: {}".format(str(clauses)))

    #Add transitivity constraints for all guests (easier than selectively finding)
    sort_g = sorted(guests)
    for i in range(len(guests)):
        for j in range(i+1, len(guests)):
            for k in range(j+1, len(guests)):
                g1, g2, g3 = sort_g[i], sort_g[j], sort_g[k]
                l1 = Literal(Variable(g1, g2), True)
                l2 = Literal(Variable(g2, g3), True)
                l3 = Literal(Variable(g1, g3), False)
                l4 = Literal(Variable(g1, g2), False)
                l5 = Literal(Variable(g2, g3), False)
                l6 = Literal(Variable(g1, g3), True)
                trans_clauses = [Clause(l1, l2, l3).to_pycosat(VM),
                                Clause(l4, l5, l6).to_pycosat(VM)]
                clauses.extend(trans_clauses)
    assert(len(clauses) == len(constraints) * 2 + len(list(itertools.
        combinations(range(len(wizards)), 3))) * 2)
    if DEBUG:
        print("First 100 clauses:\n", clauses[:100])

    #solve using pycosat
    algorithm_start = time.time()
    print("Starting pycosat")
    output = pycosat.solve(clauses)
    print("Pycosat returned a candidate assignment.")
    duration = round(time.time() - algorithm_start, 2)
    print("Total pycosat algorithm took {f}".format(duration))
    if DEBUG:
        print("Output of pycosat was: {}".format(output))

    assignments = {}
    for elem in output:
        assignments[abs(elem)] = True if elem >= 0 else False
    if DEBUG:
        print("Assignments:\n", assignments)

    #Ordering guests based on assignment in fashion logically similar to topological sort
    #Check that 'in-degree' of each successive node is 0, i.e. source node after recursive updates

    #Alternate solution ordering using clever insertion based on lexicographical order
    solution = []
    for guest in guests:
        """
        Iterate through existing soln so far and find correct insertion location
        """
        insert_index = 0
        for i in range(len(solution)):
            var = Variable(solution[i], guest)
            literal_value = assignments[VM.encode_variable(var)]
            if var.bound1 == guest:
                #Means variable is not in lexico order, so we need to recover original
                #variable value by inverting var value.
                literal_value = not literal_value
            if literal_value:
                insert_index = i + 1
        #insert after all updates to insert_index are done after passing through all soln
        solution.insert(insert_index, guest)
    assert(len(solution) == len(guests))
    if DEBUG:
        print("Ordered solution is: {}".format(solution))

    process_time = round(time.time() - processing_start, 2) - duration
    print("Total processing time took {} while algorithm took {} seconds.".format(
            process_time, duration))
    return solution

class CSPAnnealer(Annealer):
    Tmax = 60
    Tmin = 0.01
    steps = 1000000
    udpates = steps / 100

    def __init__(self, guests, constraints):
        self.constraints = constraints
        super(CSPAnnealer, self).__init__(guests)

    def move(self):
        a = random.randint(0, len(self.state) - 1)
        b = random.randint(0, len(self.state) - 1)
        self.state[a], self.state[b] = self.state[b], self.state[a]

    def energy(self):
        output_ordering_set= set(self.state)
        output_ordering_map = {k:v for k,v in enumerate(self.state)}
        not_satisfied = 0
        for constraint in self.constraints:
            c = constraint
            m = output_ordering_map
            guest_a = m[c[0]]
            guest_b = m[c[1]]
            guest_ex = m[c[2]]
            if (guest_a < guest_ex < guest_b) or (guest_b < guest_ex < guest_a):
                not_satisfied += 1
        return not_satisfied

def anneal(num_guests, num_constraints, guests, constraints):
    #pre-processing
    algorithm_start = time.time()
    if DEBUG:
        print("Input with {} wizards and {} constraints.".format(len(wizards),
                len(constraints)))
    random.shuffle(wizards)

    solver = CSPAnnealer(guests, constraints)
    print("Starting with ordering where {} constraints are violated.".format(
            solver.energy()))
    if solver.energy() < 100:
        solver.Tmax = 1
    if solver.energy() < 25:
        solver.Tmax = 0.5
    if solver.energy() < 10:
        solver.Tmax = 0.01
    solution, num_constraints_failed = solver.anneal()

    #completion details:
    algorithm_duration = round(time.time() - algorithm_start, 2)
    print("\nSolver complete. Algorithm took {} seconds.".format(algorithm_duration))
    return solution




"""
======================================================================
Input parsing happens below this line.
======================================================================
"""

def read_input(filename):
    with open(filename, 'r') as f:
        num_guests = int(f.readline().strip())
        num_constraints = int(f.readline().strip())
        guests = [x.strip() for x in f.readline().strip().split(',')]
        constraints = []
        for line in f.readlines():
            constraint = line.strip().split()
            if len(constraint) == 3:
                constraints.append([constraint[i] for i in range(3)])
    return num_guests, num_constraints, guests, constraints

def write_output(filename, solution):
    with open(filename, 'w') as f:
        for guest in solution:
            f.write("{0} ".format(guest))

def atoi(text):
    return int(text) if text.isdigit() else text

def natural_keys(text):
    return [atoi(c) for c in re.split('(\d+)', text)]

if __name__ == '__main__':
    #using argparse
    parser = argparse.ArgumentParser(description = "GuestAge CSP Solver")
    parser.add_argument("input", type=str, help = "read input path to file")
    parser.add_argument("output", type=str, help = "provide file/folder for output")
    parser.add_argument('--debug', '-d', dest="debug", action="store_true",
                        help="print verbose debug messages")
    parser.add_argument("--anneal", '-a', dest="anneal", action ="store_true",
                        help="Using simulated annealing instead of 3SAT reduction solver.")

    args = parser.parse_args()

    if args.debug:
        DEBUG = True

    #handle case where args.input is actually a folder
    inputs = [args.input]
    if os.path.isdir(args.input):
        #modify inputs instead by making a list of input file names in directory
        inputs = [os.path.join(args.input, f) for f in os.listdir(args.input)
                    if os.path.isfile(os.path.join(args.input, f)) and f.endswith(".in")]
        inputs.sort(key=natural_keys)

    for input_file in inputs:
        if not os.path.isdir(args.output):
            output_file = args.output
        else:
            #formats for instance input50.in --> output50.out in output dir
            output_file = os.path.join(args.output, os.path.split(input_file)
                                    [1].replace('.in', '.out').replace('input', 'output'))
        f = anneal if args.anneal else solve
        #start functional processing
        num_guests, num_constraints, guests, constraints = read_input(input_file)
        solution = f(num_guests, num_constraints, guests, constraints)
        write_output(output_file, solution)
        satisfied, constraints_broken = Validator(solution, constraints).validate_solution()

        if not satisfied:
            print("Constraints broken were: {}".format(constraints_broken))
        else:
            print("Success, {} solution was verified.".format(output_file))










"""
___________________________________________________________

Complete
___________________________________________________________

"""
