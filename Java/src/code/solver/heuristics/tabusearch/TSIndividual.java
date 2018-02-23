package code.solver.heuristics.tabusearch;

import java.util.ArrayList;

import code.solver.heuristics.Individual;
import code.solver.heuristics.mutators.Mutation;
import code.solver.heuristics.mutators.Swap2;

public class TSIndividual extends Individual {
	
	public TSIndividual() {
		this.representation = new ArrayList<>();
		for(int i = 10; i >= 0; i--) {
			representation.add(i);
		}
		calculateFitness();
	}
	
	protected void calculateFitness() {
		this.fitness = 0.0;
		for(int i = 0; i < this.representation.size(); i++) {
			fitness += Math.abs((Integer) representation.get(i) - i);
		}
	}
	
	public double deltaFitness(Swap2 swap) {
		int i = swap.getI();
		int j = swap.getJ();
		double before =  Math.abs((int) this.representation.get(i) - i) +  Math.abs((int) this.representation.get(j) - j); 
		double after =  Math.abs((int) this.representation.get(i) - j) +  Math.abs((int) this.representation.get(j) - i); 
		return after - before;
		
	}
	
	public void performMutation(Mutation mutation) {
		this.fitness += mutation.deltaFitness(this);
		mutation.doMutation(this);
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double deltaFitness(Mutation mutation) {
		if(mutation instanceof Swap2) {
			return deltaFitness((Swap2) mutation); 
		} else {
			return 0;
		}
	}
	
	@Override
	public String toString() {
		String s = "";
		for(Object i : this.representation) {
			s += i.toString() + " ";
		}
		return s + "\n";
	}
}
