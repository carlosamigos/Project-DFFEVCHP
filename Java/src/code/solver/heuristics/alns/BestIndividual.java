package code.solver.heuristics.alns;

import java.util.ArrayList;

import code.solver.heuristics.Individual;
import code.solver.heuristics.entities.CarMove;

public class BestIndividual extends Individual {
	
	private ArrayList<ArrayList<CarMove>> operators;
	
	public BestIndividual(ArrayList<ArrayList<CarMove>> operators, double fitness, int numberOfUnchargedCars, int deviationFromIdeal) {
		this.operators = operators;
		this.fitness   = fitness;
		this.numberOfUnchargedCars = numberOfUnchargedCars;
		this.deviationFromIdeal = deviationFromIdeal;
	}
	
	public String toString() {
		String s = "\nFitness: " + String.format("%.2f", this.fitness) + "\n" 
				+ "Deviation from ideal: " + this.deviationFromIdeal 
				+ "\nNumber of uncharged: " + this.numberOfUnchargedCars + "\n";
		for(ArrayList<CarMove> carMoves : operators) {
			for(CarMove carMove : carMoves) {
				s += carMove + " ";
			}
			s += "\n";
		}
		return s.substring(0, s.length()-2);
	}

	@Override
	public ArrayList<Object> getRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ArrayList<ArrayList<CarMove>> getOperators() {
		return this.operators;
	}
}
