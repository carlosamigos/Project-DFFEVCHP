package code.solver.heuristics.entities;

import java.util.ArrayList;

import code.solver.heuristics.mutators.Insert;
import code.solver.heuristics.mutators.Swap2;
import constants.HeuristicsConstants;

public class Operator {

	private final double startingTime;
	private final double timeLimit;	
	private ArrayList<CarMove> carMoves;	
	
	private double fitness;
	
	public Operator(ArrayList<CarMove> carMoves, double startingTime, double timeLimit) {
		this.carMoves = carMoves;
		this.startingTime = startingTime;
		this.timeLimit = timeLimit;
	}
	
	public CarMove getCarMove(int index) {
		return carMoves.get(index);
	}
	
	public void insertCar(CarMove carMove) {
		this.carMoves.add(carMove);
	}
	
	public void insertCarMove(int position, CarMove carMove) {
		carMoves.add(position, carMove);
	}
	
	public void removeCarMove(int position) {
		carMoves.remove(position);
	}
	
	/*
	 * Calculates the change in fitness a mutation of type Insert would cause
	 * Input: A mutation of type Insert. Insert contains an object and an index
	 */
	public double getDeltaFitness(Insert insert) {
		double newFitness = 0.0;
		double currentTime = 0.0;
		int start = 0;
		int end = carMoves.size();
		int index = insert.getIndex();
		CarMove carMove = (CarMove) insert.getObject();
		
		if(index == 0) {
			currentTime += carMove.getTravelTime();
			start++;
		} else if (index == end-1) {
			end--;
		}
		
		for(int i = start; i < end; i++) {
			if(i == index) {
				
			}
		}
		
		return newFitness - this.fitness;
	}
	
	private double getChargingReward(double time) {
		return (this.timeLimit - time) * HeuristicsConstants.TABU_CHARGING_UNIT_REWARD;
	}
	
}
