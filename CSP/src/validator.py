"""
INPUT:
    1. candidate solution list with ordered sequence of guest string IDs
    2. guest ordering constraints
OUTPUT:
    1. If all satisfied: True, []
    2. If not all satisfied: False, constraints_not_satisfied_list
"""

class Validator:
    def __init__(self, solution, constraints):
        self.solution = solution
        self.constraints = constraints

    def validate_solution(self):
        encoder = {}
        not_satisfied = []
        for i in range(len(self.solution)):
            encoder[solution[i]] = i

        for constraint in self.constraints:
            bound1 = encoder[constraint[0]]
            bound2 = encoder[constraint[1]]
            excludeVar = encoder[constraint[2]]
            if excludeVar > min(bound1, bound2) and excludeVar < max(bound1, bound2):
                not_satisfied.append(constraint)

        return (len(not_satisfied) == 0), not_satisfied
