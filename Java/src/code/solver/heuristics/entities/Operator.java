package code.solver.heuristics.entities;

import java.util.ArrayList;
import java.util.HashMap;

import code.problem.nodes.ChargingNode;
import code.problem.nodes.Node;
import code.solver.heuristics.mutators.Insert;
import code.solver.heuristics.mutators.Remove;
import constants.Constants;
import constants.HeuristicsConstants;

public class Operator {

	private ArrayList<CarMove> chargingMoves; //need to be chronologic
	private HashMap<CarMove, double[]> startEndChargingMoves;
	private HashMap<ChargingNode, Integer> chargingCapacityUsed;
	private HashMap<CarMove, Integer> chargingMoveIndex;
	private ArrayList<ArrayList<Double>> travelTimesBike;
	
	private HashMap<ChargingNode, Integer> chargingCapacityUsedOperator;
	
	private ArrayList<CarMove> carMoves;
	private int carsBeingCharged;
	private final Node startNode;
	private final double startTime;
	private final double timeLimit;
	private double travelTime;
	private double fitness;
	
	public Operator(double startTime, double timeLimit, Node startNode, 
			ArrayList<ArrayList<Double>> travelTimesBike, 
			HashMap<ChargingNode, Integer> chargingCapacity) {
		this.carMoves = new ArrayList<>();
		this.startTime = startTime;
		this.timeLimit = timeLimit;
		this.startNode = startNode;
		this.travelTimesBike = travelTimesBike;
		this.chargingCapacityUsed = chargingCapacity;
		this.chargingMoves = new ArrayList<>();
		this.startEndChargingMoves = new HashMap<>();
		this.chargingMoveIndex = new HashMap<>();
		this.chargingCapacityUsedOperator = new HashMap<>();
		calculateFitness();
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

	public void addCarMove(CarMove carMove){
		carMoves.add(carMove);
	}
	
	public void removeCarMove(int position) {
		carMoves.remove(position);
	}
	
	/*
	 * Calculates the initial fitness of an operator. Could also be used if one wants to calculate fitness
	 * bottom up at some other point. The method iterates through all car moves calculate rewards based on the moves
	 * and when the moves happen. Fitness = chargingRewards + capacityFeasibility
	 */
	private void calculateFitness() {
		
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
				double[] timings = {currentTime-currentMove.getTravelTime(), currentTime};
				this.chargingMoves.add(currentMove);
				this.startEndChargingMoves.put(currentMove, timings);
				this.chargingMoveIndex.put(currentMove, i);
			}
		}
	}
	
	public void performMutation(Insert insert) {
		carMoves.add(insert.getIndex(), (CarMove) insert.getObject());
		calculateFitness();
	}
	
	public void performMutation(Remove remove) {
		carMoves.remove(remove.getIndex());
		calculateFitness();
	}
	
	/*
	 * Calculates the change in fitness a mutation of type Insert would cause
	 * Input: A mutation of type Insert. Insert contains an object and an index.
	 *
	public double getDeltaFitness(Insert insert) {
		double deltaFitness = 0.0;
		double currentTime = this.startTime;
		
		int index = insert.getIndex();
		CarMove insertMove = (CarMove) insert.getObject();
		
		HashMap<ChargingNode, Integer> capacityChanged = new HashMap<>();
		
		double deltaTime;
		if(index == 0) {
			deltaTime = getAbsDeltaTime(this.startNode, insertMove, this.carMoves.get(0).getFromNode());
		} else {
			Node next = (index+1 > this.carMoves.size()) ? null : this.carMoves.get(index).getFromNode();
			deltaTime = getAbsDeltaTime(this.carMoves.get(index-1).getToNode(), insertMove, next);
		}
		
		Node previous = this.startNode;
		
		for(int i = 0; i < index; i++) {
			CarMove move = this.carMoves.get(i);
			currentTime += getTravelTime(previous, move);
			previous = move.getToNode();
		}
		
		currentTime += getTravelTime(previous, insertMove);
		
		if(currentTime < this.timeLimit && insertMove.isToCharging()) {
			ChargingNode chargingNode = (ChargingNode) insertMove.getToNode();
			deltaFitness += getChargingFitness(currentTime, chargingNode);
		}
		
		for(CarMove move : this.chargingMoves) {
			int moveIndex = this.chargingMoveIndex.get(move);
			double endTime = startEndChargingMoves.get(move)[1];
			if (moveIndex >= index && endTime <= this.timeLimit) {
				deltaFitness += getChangeInChargingFitness(endTime, deltaTime);
				
				if(endTime + deltaTime > this.timeLimit) {
					ChargingNode chargingNode = (ChargingNode) move.getToNode();
					deltaFitness += getChangeInCapacityFitness(chargingNode, false, capacityChanged);
				}
			}
		}
		
		return deltaFitness;
	}*/
	
	private double getChangeInChargingFitness(double oldTime, double timeChange) {
		double oldReward = getChargingReward(oldTime);
		double newReward = getChargingReward(oldTime + timeChange);
		return -newReward + oldReward;     
	}
	
	private double getChangeInCapacityFitness(ChargingNode node, boolean isAdding, HashMap<ChargingNode, Integer> capacityChanged) {
		if(capacityChanged.get(node) == null){
			capacityChanged.put(node, 0);
		}
		
		double capacityDelta = getCapacityPenalty(node, isAdding, capacityChanged.get(node));
		int capacityChange = isAdding ? 1 : -1;
		capacityChanged.put(node, capacityChanged.get(node) + capacityChange);
		return capacityDelta;
	}
	
	private double getTravelTime(Node previous, CarMove move) {
		return getTravelTimeBike(previous, move.getFromNode()) 
				+ move.getTravelTime();
	}
	
	private double getChargingFitness(double time, ChargingNode node) {
		return getCapacityPenalty(node, true, 0) - getChargingReward(time);
	}
	
	
	public double getDeltaFitness(Remove remove){
		double currentFitness = this.fitness;
		double newFitness = this.fitness;
		int index = remove.getIndex();
		CarMove toRemove = carMoves.get(index);
		Node prevNode = (index > 0) ? carMoves.get(index - 1).getToNode() : this.startNode;
		Node nextNode = (index < carMoves.size()-1) ? carMoves.get(index + 1).getFromNode() : null;
		double deltaTime = getAbsDeltaTime(prevNode, toRemove , nextNode);
		double currCarMoveChargingFitness = (toRemove.isToCharging())
				? getChargingReward(startEndChargingMoves.get(toRemove)[1]): 0;
		double deltaCapFitness = (toRemove.isToCharging()) ? getCapacityPenalty((ChargingNode) toRemove.getToNode(),
				false, 0) : 0;
		newFitness += -currCarMoveChargingFitness + deltaCapFitness;

		// Iterate over subsequent charging nodes
		boolean doFitnessUpdates = false;
		HashMap<ChargingNode, Integer> capacityChanged = new HashMap<>(); // Those not initialized are 0.
		if(toRemove.isToCharging()){
			capacityChanged.put((ChargingNode) toRemove.getToNode(), -1);
		}
		for (int i = 0; i < chargingMoves.size(); i++) {
			CarMove chargingMove = chargingMoves.get(i);
			int index2 = this.chargingMoveIndex.get(chargingMove);
			if(startEndChargingMoves.get(chargingMove)[1] - deltaTime > this.timeLimit){
				// no change in fitness
				break;
			}
			if(doFitnessUpdates){
				//find fitness before and after
				double endTime = startEndChargingMoves.get(chargingMove)[1];
				newFitness += getChangeInChargingFitness(endTime, -deltaTime);

				// kun en positiv endring i capacity fitness dersom chargingNoden går fra over planning period til under planning period!
				if(endTime > this.timeLimit && endTime - deltaTime < this.timeLimit){
					ChargingNode chargingNode = (ChargingNode) chargingMove.getToNode();
					if(capacityChanged.get(chargingNode) == null){
						capacityChanged.put(chargingNode, 0);
					}
					double deltaCapacityPenalty = getCapacityPenalty(chargingNode, true,capacityChanged.get(chargingNode) );
					capacityChanged.put(chargingNode, capacityChanged.get(chargingNode) + 1);
					newFitness += deltaCapacityPenalty;
				}
			}
			else if(index2 >= index){
				// start doing fitness updates on these charging fitness
				doFitnessUpdates = true;
			}
		}

		return newFitness - currentFitness;

	}

	private double getAbsDeltaTime(Node prev, CarMove curr, Node next){
		double currTimeContribution = getTravelTimeBike(prev, curr.getFromNode())
				+ curr.getTravelTime()
				+ ((next != null) ? getTravelTimeBike(curr.getToNode(), next) : 0);
		double newTimeContribution  = (next != null) ? getTravelTimeBike(prev, next) : 0;
		return Math.abs(currTimeContribution - newTimeContribution);
	}
	
	private double getTravelTimeBike(Node n1, Node n2) {
		double t0 = this.travelTimesBike
				.get(n1.getNodeId() - Constants.START_INDEX)
				.get(n2.getNodeId() - Constants.START_INDEX);
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
}
