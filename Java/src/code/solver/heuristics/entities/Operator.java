package code.solver.heuristics.entities;

import java.util.ArrayList;

import code.problem.ProblemInstance;
import code.problem.nodes.Node;
import code.solver.heuristics.mutators.Insert;
import constants.HeuristicsConstants;

public class Operator {

	private ArrayList<CarMove> carMoves;
	private int carsBeingCharged;
	private final Node startNode;
	private final double startTime;
	private final double timeLimit;
	private double travelTime;
	private double fitness;
	
	private double deltaFitness;
	
	public Operator(double startTime, double timeLimit, Node startNode) {
		this.carMoves = new ArrayList<>();
		this.startTime = startTime;
		this.timeLimit = timeLimit;
		this.startNode = startNode;
	}
	
	public CarMove getCarMove(int index) {
		return carMoves.get(index);
	}

	public double getTimeLimit(){
		return timeLimit;
	}

	public int getCarMovesSize(){
		return carMoves.size();
	}
	
	public void insertCarMove(CarMove carMove) {
		this.carMoves.add(carMove);
	}
	
	public void insertCarMove(int position, CarMove carMove) {
		carMoves.add(position, carMove);
	}
		
	public void insertCarMoves(ArrayList<CarMove> carMoves) {
		this.carMoves.addAll(carMoves);
	}
	
	public int getCarsBeingCharged() {
		return this.carsBeingCharged;
	}
	
	public double getTravelTime() {
		return this.travelTime;
	}
	
	public double getStartTime() {
		return this.startTime;
	}
	
	public Node getStartNode() {
		return this.startNode;
	}
	
	public void addToTravelTime(double deltaTime) {
		this.travelTime += deltaTime;
	}

	public void addCarMove(CarMove carMove){
		carMoves.add(carMove);
	}
	
	public void removeCarMove(int position) {
		carMoves.remove(position);
	}
	
	/*
	 * Calculates the change in fitness a mutation of type Insert would cause
	 * Input: A mutation of type Insert. Insert contains an object and an index.
	 */
	public double getDeltaFitness(Insert insert, ProblemInstance problemInstance) {
		double newFitness = 0.0;
		double currentTime = this.startTime;
		
		int index = insert.getIndex();
		
		Node previousNode = this.startNode;
		CarMove currentMove;
		
		for(int i = 0; i < index; i++) {
			currentMove = this.carMoves.get(i);
			currentTime += getChangeInTravelTime(currentMove, previousNode, problemInstance);
		
			if(currentTime > this.timeLimit) {
				return newFitness - this.fitness;
			}
			
			newFitness = addCarToChargingStation(currentMove, currentTime);
			previousNode = currentMove.getToNode();
		}
		
		
		currentMove = (CarMove) insert.getObject();
		currentTime += getChangeInTravelTime(currentMove, previousNode, problemInstance);
		
		if(currentTime > this.timeLimit) {
			return newFitness - this.fitness;
		}
		
		// This check need to check if there are enough available charging spots as well, and alter that count.
		newFitness = addCarToChargingStation(currentMove, currentTime);
		previousNode = currentMove.getToNode();
		
		for(int i = index; i < carMoves.size(); i++) {
			currentMove = this.carMoves.get(i);
			currentTime += getChangeInTravelTime(currentMove, previousNode, problemInstance);
		
			if(currentTime > this.timeLimit) {
				return newFitness - this.fitness;
			}
			
			newFitness = addCarToChargingStation(currentMove, currentTime);
			previousNode = currentMove.getToNode();
		}
		
		return newFitness - this.fitness;
	}
	
	private double getChangeInTravelTime(CarMove currentMove, Node previousNode, ProblemInstance problemInstance) {
		Node currentNode = currentMove.getFromNode();
		return problemInstance.getTravelTimeBike(previousNode.getNodeId(), currentNode.getNodeId()) +
				currentMove.getTravelTime();
	}
	
	
	/*
	 * Checks if a car move ends in charging station. If there is available capacity at the charging station
	 * the value of charging is calculated and returned. The capacity of the charging station is updated accordingly
	 */
	private double addCarToChargingStation(CarMove move, double time) {
		
		if(move.isToCharging()) {
			// if check for capacity at charging station
			return getChargingReward(time);
		}
		
		return 0.0;
	}
	
	private double getChargingReward(double time) {
		return (this.timeLimit - time) * HeuristicsConstants.TABU_CHARGING_UNIT_REWARD;
	}
	
}
