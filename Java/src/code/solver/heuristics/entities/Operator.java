package code.solver.heuristics.entities;

import java.util.ArrayList;
import java.util.HashMap;

import code.problem.ProblemInstance;
import code.problem.entities.Car;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.Node;
import code.solver.heuristics.mutators.Insert;
import code.solver.heuristics.mutators.Remove;
import constants.HeuristicsConstants;

public class Operator {

	private ArrayList<CarMove> chargingMoves;
	private HashMap<CarMove, double[]> startEndChargingMoves;
	private HashMap<ChargingNode, Integer> chargingCapacityUsed;
	
	private ArrayList<CarMove> carMoves;
	private int carsBeingCharged;
	private final Node startNode;
	private final double startTime;
	private final double timeLimit;
	private double travelTime;
	private double fitness;
	
	public Operator(double startTime, double timeLimit, Node startNode, ProblemInstance problemInstance, 
			HashMap<ChargingNode, Integer> chargingCapacity) {
		this.carMoves = new ArrayList<>();
		this.startTime = startTime;
		this.timeLimit = timeLimit;
		this.startNode = startNode;
		this.chargingCapacityUsed = chargingCapacity;
		this.chargingMoves = new ArrayList<>();
		this.startEndChargingMoves = new HashMap<>();
		calculateInitialFitness(problemInstance);
	}
	
	public CarMove getCarMove(int index) {
		return carMoves.get(index);
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
	
	public void removeCarMove(int position) {
		carMoves.remove(position);
	}
	
	/*
	 * Calculates the initial fitness of an operator. Could also be used if one wants to calculate fitness
	 * bottom up at some other point. The method iterates through all car moves calculate rewards based on the moves
	 * and when the moves happen.
	 */
	private void calculateInitialFitness(ProblemInstance problemInstance) {
		double currentTime = this.startTime;
		Node previousNode = this.startNode;
		CarMove currentMove;
		this.fitness = 0.0;
		
		
		for(int i = 0; i < this.carMoves.size(); i++) {
			currentMove = this.carMoves.get(i);
			currentTime += getChangeInTravelTime(currentMove, previousNode, problemInstance);
			
			if(currentTime > this.timeLimit) {
				return;
			}
			
			if(currentMove.isToCharging()) {
				ChargingNode node = (ChargingNode) currentMove.getToNode();
				fitness += getChargingFitness(currentTime, node);
				this.chargingCapacityUsed.put(node, this.chargingCapacityUsed.get(node)+1);
				double[] timings = {currentTime-currentMove.getTravelTime(), currentTime};
				this.chargingMoves.add(currentMove);
				this.startEndChargingMoves.put(currentMove, timings);
			}
		}
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


	public double getDeltaFitness(Remove remove, ProblemInstance problemInstance){

		double currentFitness = this.fitness;
		int index = remove.getIndex();
		CarMove toRemove = carMoves.get(index);
		Node prevNode = (index > 0) ? carMoves.get(index - 1).getToNode() : this.startNode;
		Node nextNode = (index < carMoves.size()-1) ? carMoves.get(index + 1).getFromNode() : null;
		double currTimeContribution = getTimeContribution(prevNode,toRemove, nextNode, problemInstance);
		double newTimeContribution  = (nextNode != null) ? problemInstance.getTravelTimeBike(prevNode.getNodeId(), nextNode.getNodeId()) : 0;
		//TODO


		return 0.0;


	}

	private double getTimeContribution(Node prev, CarMove curr, Node next, ProblemInstance problemInstance){
		return problemInstance.getTravelTimeBike(prev.getNodeId(), curr.getFromNode().getNodeId())
				+ curr.getTravelTime()
				+ ((next != null) ? problemInstance.getTravelTimeBike(curr.getToNode().getNodeId(), next.getNodeId()) : 0);
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
			//return getChargingReward(time);
		}
		
		return 0.0;
	}
	
	
	private double getChargingFitness(double time, ChargingNode node) {
		double capacityPenalty = (Math.max(0, this.chargingCapacityUsed.get(node) - 
				node.getNumberOfAvailableChargingSpotsNextPeriod())) * HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY;
		double chargingReward = (Math.max(this.timeLimit - time,0) * HeuristicsConstants.TABU_CHARGING_UNIT_REWARD);
		return capacityPenalty - chargingReward;
	}
}
