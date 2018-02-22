package code.solver.heuristics.mutators;

import code.solver.heuristics.Individual;

public abstract class Mutation {
	public abstract Individual doMutation(Individual representation);
}
