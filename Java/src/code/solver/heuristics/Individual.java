package code.solver.heuristics;

import java.util.ArrayList;

public abstract class Individual implements Comparable<Object> {

	protected abstract void calculateFitnessOfIndividual();
	protected double fitness;
	
	public abstract ArrayList<Object> getRepresentation();
	
	public double getFitness() {
		return this.fitness;
	}
}
