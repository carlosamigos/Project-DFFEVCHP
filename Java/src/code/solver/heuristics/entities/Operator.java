package code.solver.heuristics.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import code.problem.nodes.ChargingNode;
import code.problem.nodes.Node;
import code.solver.heuristics.tabusearch.TSIndividual;
import constants.Constants;
import constants.HeuristicsConstants;

public class Operator {

	public final int id;

	private TSIndividual individual;
	private ArrayList<ArrayList<Double>> travelTimesBike;
	private HashMap<ChargingNode, Integer> chargingCapacityUsedOperator;
	private HashSet<ChargingNode> chargingNodesVisited;

	private ArrayList<CarMove> carMoves; // only car moves that are performed
	private final Node startNode;
	private final double startTime;
	private final double timeLimit;
	private double freeTime = 0;
	private double travelTime;
	private double fitness;
	private boolean changed;
	
	public Operator(double startTime, double timeLimit, Node startNode, 
			ArrayList<ArrayList<Double>> travelTimesBike, int id, TSIndividual individual) {
		this.individual = individual;
		this.carMoves = new ArrayList<>();
		this.startTime = startTime;
		this.timeLimit = timeLimit;
		this.startNode = startNode;
		this.travelTimesBike = travelTimesBike;
		this.chargingCapacityUsedOperator = new HashMap<>();
		this.chargingNodesVisited = new HashSet<>();
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
		if(index > this.carMoves.size()){
			index = this.carMoves.size();
		}
		this.carMoves.add(index, carMove);
	}
	
	public void addCarMoves(ArrayList<CarMove> carMoves) {
		this.changed = true;
		this.carMoves.addAll(carMoves);
	}

	public ArrayList<CarMove> getCarMoveCopy() {
		return new ArrayList<>(carMoves);
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
		if(position >= carMoves.size()){
			position = carMoves.size()-1;
		}
		return carMoves.remove(position);
	}

	public int getCarMoveListSize(){
		return this.carMoves.size();
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
	
	public void setChargingCapacityUsedByOperator(HashMap<ChargingNode, Integer> capacityUsedByOperator) {
		this.chargingCapacityUsedOperator = capacityUsedByOperator;
	}

	/*
	 * Calculates the initial fitness of an operator. Could also be used if one wants to calculate fitness
	 * bottom up at some other point. The method iterates through all car moves calculate rewards based on the moves
	 * and when the moves happen. Fitness = chargingRewards + capacityFeasibility
	 */
	private void calculateFitness() {
		// ToDo: Need to take start time for the car move into account
		this.chargingNodesVisited.clear();
		for(ChargingNode chargingNode : this.chargingCapacityUsedOperator.keySet()) {
			this.individual.getPrevCapacitiesUsed().put(chargingNode, 
					this.individual.getCapacitiesUsed().get(chargingNode));
			this.individual.getCapacitiesUsed().put(chargingNode,
					this.individual.getCapacitiesUsed().get(chargingNode)
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
				this.freeTime = (this.timeLimit - currentTime);
				this.fitness += getCapacityPenalty();
				return;
			}
			
			if(currentMove.isToCharging()) {
				ChargingNode chargingNode = (ChargingNode) currentMove.getToNode();
				this.chargingNodesVisited.add(chargingNode);
				this.fitness -= getChargingReward(currentTime);
				
				
				if(!this.chargingCapacityUsedOperator.containsKey(chargingNode)) {
					this.chargingCapacityUsedOperator.put(chargingNode, 0);
				}
				
				this.chargingCapacityUsedOperator.put(chargingNode, 
						this.chargingCapacityUsedOperator.get(chargingNode)+1);

				this.individual.getCapacitiesUsed().put(chargingNode, this.individual.getCapacitiesUsed().get(chargingNode)+1);
			}
		}
		
		this.fitness += getCapacityPenalty();
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
	
	private double getCapacityPenalty() {
		double newPenalty = 0.0;
		double oldPenalty = 0.0;
		
		for(ChargingNode chargingNode : this.chargingNodesVisited) {
			int usedNow = this.individual.getCapacitiesUsed().get(chargingNode);
			int usedBefore = this.individual.getPrevCapacitiesUsed().get(chargingNode); 
			int capacity = chargingNode.getNumberOfAvailableChargingSpotsNextPeriod();
			
			newPenalty += Math.max(0, usedNow - capacity);
			oldPenalty += Math.max(0, usedBefore - capacity);
		}
		
		return (newPenalty - oldPenalty) * HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY;
	}
	
	private double getTravelTimeBike(Node n1, Node n2) {
		return this.travelTimesBike
				.get(n1.getNodeId() - Constants.START_INDEX)
				.get(n2.getNodeId() - Constants.START_INDEX);
	}

	
	/*
	 * Calculates the penalty of charging an extra car at a charging node.
	 * The penalty is zero as long as the capacity is not broken.
	 *
	private double getCapacityPenalty(ChargingNode arrivalNode, boolean isAddingCarMove, int alreadyAdjusted) {
		int currentUsed = this.individual.getCapacitiesUsed().get(arrivalNode) + alreadyAdjusted;
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
	*/
	
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

	public void cleanCarMovesNotDone(){
		// Todo: make smarter by using the remembered start index
		// Todo: need to take starttime for the car move into account
		/*Node previousNode = this.startNode;
		CarMove currentMove;
		double currentTime = this.startTime;
		for (int j = 0; j < this.carMoves.size(); j++) {
			currentMove = this.carMoves.get(j);
			currentTime += getTravelTime(previousNode, currentMove);
			previousNode = currentMove.getToNode();
			if(currentTime > timeLimit){
				CarMove carMoveToRemove = this.carMoves.remove(j);
				individual.getUnusedCarMoves().get(carMoveToRemove.getCar()).add(carMoveToRemove);
			}

		}*/
	}

	public void setChanged(boolean change){
		this.changed = change;
	}

	@Override
	public String toString() {
		String s = "";
		for(CarMove carMove : carMoves) {
			s += carMove + ", ";
		}
		return s.substring(0, s.length()-2);
	}
	
}
