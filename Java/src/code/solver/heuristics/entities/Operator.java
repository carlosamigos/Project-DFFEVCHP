package code.solver.heuristics.entities;

import java.util.ArrayList;
import java.util.HashMap;

import code.problem.nodes.ChargingNode;
import code.problem.nodes.Node;
import constants.Constants;
import constants.HeuristicsConstants;

public class Operator {

	public final int id;

	private HashMap<ChargingNode, Integer> chargingCapacityUsed;
	private ArrayList<ArrayList<Double>> travelTimesBike;
	private HashMap<ChargingNode, Integer> chargingCapacityUsedOperator;

	private ArrayList<CarMove> carMoves;
	private final Node startNode;
	private final double startTime;
	private final double timeLimit;
	private double travelTime;
	private double fitness;
	private boolean changed;
	
	public Operator(double startTime, double timeLimit, Node startNode, 
			ArrayList<ArrayList<Double>> travelTimesBike, int id) {
		this.carMoves = new ArrayList<>();
		this.startTime = startTime;
		this.timeLimit = timeLimit;
		this.startNode = startNode;
		this.travelTimesBike = travelTimesBike;
		this.chargingCapacityUsedOperator = new HashMap<>();
		this.id = id;
		changed = false;
	}
	
	public CarMove getCarMove(int index) {
		return carMoves.get(index);
	}

	public double getTimeLimit(){
		return timeLimit;
	}

	public void addCarMove(CarMove carMove) {
		this.changed = true;
		this.carMoves.add(carMove);
	}
	

	public void addCarMove(int index, CarMove carMove) {
		this.changed = true;
		//System.out.println(carMoves + " " + index);
		this.carMoves.add(index, carMove);
	}
	
	public void addCarMoves(ArrayList<CarMove> carMoves) {
		this.changed = true;
		this.carMoves.addAll(carMoves);
	}

	public ArrayList<CarMove> getCarMoves() {
		return carMoves;
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

	public CarMove removeCarMove(int position) {
		this.changed = true;
		return carMoves.remove(position);
	}
	
	public double getFitness() {
		if(changed) {
			calculateFitness();
			changed = false;
		}
		return this.fitness;
	}
	
	public void setCarMoves(ArrayList<CarMove> carMoves) {
		this.changed = true;
		this.carMoves = carMoves;
	}
	
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	public void setChargingCapacityUsedByOperator( HashMap<ChargingNode, Integer> capacityUsedByOperator) {
		this.chargingCapacityUsedOperator = capacityUsedByOperator;
	}

	public void setChargingCapacityUsedIndividual(HashMap<ChargingNode, Integer> chargingCapacityUsed){
		this.chargingCapacityUsed = chargingCapacityUsed;
	}
	
	/*
	 * Calculates the initial fitness of an operator. Could also be used if one wants to calculate fitness
	 * bottom up at some other point. The method iterates through all car moves calculate rewards based on the moves
	 * and when the moves happen. Fitness = chargingRewards + capacityFeasibility
	 */
	public void calculateFitness() {
		changed = false;
		for(ChargingNode chargingNode : this.chargingCapacityUsedOperator.keySet()) {
			this.chargingCapacityUsed.put(chargingNode, 
					  this.chargingCapacityUsed.get(chargingNode) 
					- this.chargingCapacityUsedOperator.get(chargingNode));
			this.chargingCapacityUsedOperator.put(chargingNode, 0);
		}
		
		double currentTime = this.startTime;
		Node previousNode = this.startNode;
		CarMove currentMove;
		this.fitness = 0.0;
		for(int i = 0; i < this.carMoves.size(); i++) {
			currentMove = this.carMoves.get(i);
			currentTime += getTravelTime(previousNode, currentMove);
			previousNode = currentMove.getToNode();

			if(currentTime > this.timeLimit) {
				return;
			}
			
			if(currentMove.isToCharging()) {
				ChargingNode chargingNode = (ChargingNode) currentMove.getToNode();
				double chargingFitness = getChargingFitness(currentTime, chargingNode);
				fitness += chargingFitness;
				
				if(!this.chargingCapacityUsedOperator.containsKey(chargingNode)) {
					this.chargingCapacityUsedOperator.put(chargingNode, 0);
				}
				
				this.chargingCapacityUsedOperator.put(chargingNode, 
						this.chargingCapacityUsedOperator.get(chargingNode)+1);

				this.chargingCapacityUsed.put(chargingNode, this.chargingCapacityUsed.get(chargingNode)+1);
			}
		}
	}
	
	/*
	private double getChangeInCapacityFitness(ChargingNode node, boolean isAdding, HashMap<ChargingNode, Integer> capacityChanged) {
		if(capacityChanged.get(node) == null){
			capacityChanged.put(node, 0);
		}
		
		double capacityDelta = getCapacityPenalty(node, isAdding, capacityChanged.get(node));
		int capacityChange = isAdding ? 1 : -1;
		capacityChanged.put(node, capacityChanged.get(node) + capacityChange);
		return capacityDelta;
	}
	*/
	
	
	private double getTravelTime(Node previous, CarMove move) {
		return getTravelTimeBike(previous, move.getFromNode()) 
				+ move.getTravelTime();
	}
	
	private double getChargingFitness(double time, ChargingNode node) {
		return getCapacityPenalty(node, true, 0) - getChargingReward(time);
	}

	private double getTravelTimeBike(Node n1, Node n2) {
		return this.travelTimesBike
				.get(n1.getNodeId() - Constants.START_INDEX)
				.get(n2.getNodeId() - Constants.START_INDEX);
	}

	
	/*
	 * Calculates the penalty of charging an extra car at a charging node.
	 * The penalty is zero as long as the capacity is not broken.
	 */
	private double getCapacityPenalty(ChargingNode arrivalNode, boolean isAddingCarMove, int alreadyAdjusted) {
		int currentUsed = this.chargingCapacityUsed.get(arrivalNode) + alreadyAdjusted;
		int available = arrivalNode.getNumberOfAvailableChargingSpotsNextPeriod();
		if(isAddingCarMove){
			if(currentUsed >= available){
				return HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY;
			}
		}else {
			if(currentUsed > available){
				return -HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY;
			}
		}
		return 0;
	}
	
	public double getChargingReward(double time) {
		return (Math.max(this.timeLimit - time,0) * HeuristicsConstants.TABU_CHARGING_UNIT_REWARD);
	}

	public HashMap<ChargingNode, Integer> getChargingCapacityUsedOperator() {
		return chargingCapacityUsedOperator;
	}

	/*
	private double getAbsDeltaTime(Node prev, CarMove curr, Node next){
		double currTimeContribution = getTravelTimeBike(prev, curr.getFromNode())
				+ curr.getTravelTime()
				+ ((next != null) ? getTravelTimeBike(curr.getToNode(), next) : 0);
		double newTimeContribution  = (next != null) ? getTravelTimeBike(prev, next) : 0;
		return Math.abs(currTimeContribution - newTimeContribution);
	}
	*/
}
