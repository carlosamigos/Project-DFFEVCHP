package code.solver.heuristics.alns;

import java.util.ArrayList;

import code.solver.heuristics.Individual;
import code.solver.heuristics.entities.CarMove;

public class BestIndividual extends Individual {
	
	private ArrayList<ArrayList<CarMove>> operators;
	private ArrayList<Double> endTimes;
	
	public BestIndividual(ArrayList<ArrayList<CarMove>> operators, double fitness, ArrayList<Double> endTimes, int numberOfUnchargedCars, int deviationFromIdeal) {
		this.operators = operators;
		this.fitness   = fitness;
		this.endTimes  = endTimes;
		this.numberOfUnchargedCars = numberOfUnchargedCars;
		this.deviationFromIdeal    = deviationFromIdeal;
	}
	
	public String toString() {
		ArrayList<String> strings = new ArrayList<>();
		int maxLength = 0;
		String s = "\nFitness: " + String.format("%.2f", this.fitness) + "\n" 
				+ "Deviation from ideal: " + this.deviationFromIdeal 
				+ "\nNumber of uncharged: " + this.numberOfUnchargedCars + "\n";
		for(ArrayList<CarMove> carMoves : operators) {
			String currS = "";
			for(CarMove carMove : carMoves) {
				currS += carMove + " ";
			}
			if(currS.length() > maxLength) {
				maxLength = currS.length();
			}
			strings.add(currS);
		}
		
		for(int i = 0; i < endTimes.size(); i++) {
			String currS = strings.get(i);
			while(currS.length() < maxLength) {
				currS += " ";
			}
			s += currS + "(End time: " + String.format("%.1f", endTimes.get(i)) + ")\n";
		}

		return s.substring(0, s.length()-1);
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
