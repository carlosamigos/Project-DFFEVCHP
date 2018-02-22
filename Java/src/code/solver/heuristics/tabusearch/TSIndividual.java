package code.solver.heuristics.tabusearch;

import code.solver.heuristics.Individual;

public class TSIndividual extends Individual {

	private double fitness;
	private boolean altered = false;
	
	public TSIndividual() {
		
	}
	
	public double getFitness() {
		if (this.altered) {
			calculateFitness();
		}
		return this.fitness;
	}
	
	private void calculateFitness() {
		
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
