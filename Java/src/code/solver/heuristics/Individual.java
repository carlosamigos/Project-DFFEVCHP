package code.solver.heuristics;

import java.util.ArrayList;

import code.solver.heuristics.mutators.Mutation;

public abstract class Individual implements Comparable<Object> {
	protected ArrayList<Object> representation;
	protected abstract void calculateFitness();
	protected double fitness;
	
	public ArrayList<Object> getRepresentation() {
    	return this.representation;
    }
	
	public double getFitness() {
		return this.fitness;
	}
	public abstract double deltaFitness(Mutation mutation);

	
}
