import itertools
"""
Contains all data structures in the form of classes, with relationship schema below:
1. Variable(bound1, bound2) creates a variable involving the relationship between bound1,bound2

For pycosat implementation:
Need to structure data structures as variables that are parametrized by two individuals then
in turn have a wrapper class as a literal. Formatting for pycosat input is a 2d list of
clause lists, element-wise separated by commas where numeric mapping of each literal is either
true for no sign, or -1 for inverse of the literal value.

First define basic skeleton structures, then import this module into the wrapper script
to then write out encoding and pycosat passing/decoding afterwards.

"""

class Variable:
    """
    since bound vars passed into init are string names, they can be compared
    Thus we can establish a standard ordering relating the bound vars (i.e. no confusion
    concerning what the variable represents). We treat this almost like a DAG by linearizing
    by string names then creating variables with automatic pairs just by flipping literal value

    When analyzing a Variable instance, see if this form exists in our VariableMap - checking
    var.bound1 == bound1 verifies whether this is the lexicographical order. Otherwise, we know algorithm_start
    necessarily, Var(bound2, bound1) does exist in VariableMap.

    Note also that VarA.__eq__(VarB) simply if they share the same parameters
    """
    def __init__(self, bound1, bound2):
        self.bound1 = min(bound1, bound2)
        self.bound2 = max(bound1, bound2)

    def __eq__(self, othr):
        return (isinstance(othr, type(self))
                and (self.bound1, self.bound2) == (othr.bound1, othr.bound2))

    def __hash__(self):
        return hash((self.bound1, self.bound2))

    def __repr__(self):
        return 'Variable(bound1={}, bound2={})'.format(self.bound1, self.bound2)

class VariableMap:
    """
    Numeric map from Variable object keys to 1-indexed hashmap values for easy processing
    and standardization to Pycosat format. Creates variables for every single combination
    of 2 pairs in the individuals list.
    """
    def __init__(self, guests):
        combinations = list(itertools.combinations(guests, 2))
        self.encoder = {}
        variables = [Variable(bound1, bound2) for bound1, bound2 in combinations]
        for i in range(len(variables)):
            self.encoder[variables[i]] = i + 1

    def encode_variable(self, variable):
        #note that variable should ALWAYS be in self.encoder bc the latter contains ALL combs
        return self.encoder[variable]

    def __len__(self):
        return len(self.encoder)

    def __repr__(self):
        result = "Variable List <size: {}>".format(len(self.encoder))
        for variable in self.encoder:
            result += "\n{} --> {}".format(str(variable), self.encoder[variable])
        return result

class Literal:
    #Wrapper class for variable, with value attached, input Variable object + bool value
    def __init__(self, variable, value):
        self.variable = variable
        self.value = value

    def to_pycosat(self, variable_map):
        sign = 1 if self.value else -1
        return variable_map.encode_variable(self.variable) * sign

    def __repr__(self):
        return "Literal(variable={}, value={})".format(str(self.variable), self.value)

class Clause:
    def __init__(self, literals):
        self.literals = literals

    def to_pycosat(self, variable_map):
        return [literal.to_pycosat(variable_map) for literal in self.literals]

    def __repr__(self):
        return "Clause:" + "OR".join([str(literal) for literal in self.literals])

class Constraint:
    #Wrapper class to transform constraint to literals, converts to pycosat readable clauses
    def __init__(self, constraint):
        assert(len(constraint) == 3)
        self.bound1 = constraint[0]
        self.bound2 = constraint[1]
        self.excludeVar = constraint[2]

    def to_pycosat_clause(self, variable_map):
        l1 = Literal(Variable(self.bound1, self.excludeVar), self.excludeVar < self.bound1)
        l2 = Literal(Variable(self.bound2, self.excludeVar), self.excludeVar > self.bound2)
        negate_l1 = Literal(Variable(self.bound1, self.excludeVar), self.excludeVar > self.bound1)
        negate_l2 = Literal(Variable(self.bound2, self.excludeVar), self.excludeVar < self.bound2)
        return [Clause([l1, l2]).to_pycosat(variable_map), Clause([negate_l1, negate_l2]).to_pycosat(variable_map)]

    def __repr__(self):
        return "Constraint: {} {} {}".format(self.bound1, self.bound2, self.excludeVar)
