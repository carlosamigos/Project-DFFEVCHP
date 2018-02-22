package code.solver.heuristics.mutators;

import code.solver.heuristics.Individual;

public abstract class Mutation {
	public abstract void doMutation(Individual individual);
	public abstract boolean equals(Object o);
	public abstract int hashCode();
}
