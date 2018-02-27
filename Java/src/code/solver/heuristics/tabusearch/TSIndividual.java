package code.solver.heuristics.tabusearch;

import java.util.ArrayList;

import code.problem.ProblemInstance;
import code.solver.heuristics.Individual;
import code.solver.heuristics.entities.Operator;
import code.solver.heuristics.mutators.Mutation;
import code.solver.heuristics.mutators.Swap2;
import code.solver.heuristics.mutators.Swap3;

public class TSIndividual extends Individual {
	
	private ArrayList<Operator> operators;
	//private final int[] idealState;
	private int[] currentState;
	
	private double costOfPostponed = 0.0;
	private double costOfTravel = 0.0;
	private double costOfUnmetIdeal = 0.0;
	
	public TSIndividual(ProblemInstance problemInstance) {
		this.representation = new ArrayList<>();
		this.operators = new ArrayList<>();
		initializeOperators();
		calculateFitness();
	}
	
	private void initializeOperators() {
		
	}
	
	/*
	 * 1. Prioritize charging cars
	 * 2. Minimize travel distance
	 * 3. Meet ideal state
	 */
	protected void calculateFitness() {
		for(Operator op : operators) {
			this.costOfPostponed += op.getCostOfPostponed();
			this.costOfTravel += op.getCostOfTravel();
		}
		
		this.fitness = this.costOfPostponed + this.costOfTravel + this.costOfUnmetIdeal;
	}
	
	public double deltaFitness(Swap2 swap) {
		int i = swap.getI();
		int j = swap.getJ();
		double before =  Math.abs((int) this.representation.get(i) - i) +  Math.abs((int) this.representation.get(j) - j); 
		double after =  Math.abs((int) this.representation.get(i) - j) +  Math.abs((int) this.representation.get(j) - i); 
		return after - before;
	}
	
	public double deltaFitness(Swap3 swap) {
		return 0.0;
	}
	
	public void addToFitness(double delta) {
		this.fitness += delta;
	}
	
	public void performMutation(Mutation mutation) {
		mutation.doMutation(this);
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public double optimalInsertionValue() {
		return 0.0;
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
