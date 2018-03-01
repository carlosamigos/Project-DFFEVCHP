package code.solver.heuristics;

import java.util.ArrayList;

public abstract class Individual implements Comparable<Object> {
	protected ArrayList<Object> representation;
	protected abstract void calculateFitnessOfIndividual();
	protected double fitness;
	
	public ArrayList<Object> getRepresentation() {
    	return this.representation;
    }
	
	public double getFitness() {
		return this.fitness;
	}
}
