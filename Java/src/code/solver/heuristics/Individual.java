package code.solver.heuristics;

import java.util.ArrayList;

public abstract class Individual {

	protected double fitness;
	protected int numberOfUnchargedCars;
	protected int deviationFromIdeal;
	
	public abstract ArrayList<Object> getRepresentation();
	
	public double getFitness() {
		return this.fitness;
	}
	
	public int getNumberOfUnchargedCars() {
		return this.numberOfUnchargedCars;
	}
	
	public int getDeviationFromIdeal() {
		return this.deviationFromIdeal;
	}
}
