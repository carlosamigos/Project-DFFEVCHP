package code.solver.heuristics.entities;

import java.util.ArrayList;
import java.util.HashMap;

import code.problem.ProblemInstance;
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
		this.chargingMoveIndex = new HashMap<>();
		calculateInitialFitness(problemInstance);
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
	 * Calculates the initial fitness of an operator. Could also be used if one wants to calculate fitness
	 * bottom up at some other point. The method iterates through all car moves calculate rewards based on the moves
	 * and when the moves happen. Fitness = chargingRewards + capacityFeasibility
	 */
	private void calculateInitialFitness(ProblemInstance problemInstance) {
		double currentTime = this.startTime;
		Node previousNode = this.startNode;
		CarMove currentMove;
		this.fitness = 0.0;
		
		for(int i = 0; i < this.carMoves.size(); i++) {
			currentMove = this.carMoves.get(i);
			//currentTime += getChangeInTravelTime(currentMove, previousNode, problemInstance);
			
			if(currentTime > this.timeLimit) {
				return;
			}
			
			if(currentMove.isToCharging()) {
				ChargingNode node = (ChargingNode) currentMove.getToNode();
				double chargingFitness = getChargingFitness(currentTime, node);
				fitness += chargingFitness;
				this.chargingCapacityUsed.put(node, this.chargingCapacityUsed.get(node)+1);
				double[] timings = {currentTime-currentMove.getTravelTime(), currentTime};
				this.chargingMoves.add(currentMove);
				this.startEndChargingMoves.put(currentMove, timings);
				this.chargingMoveIndex.put(currentMove, i);
			}
		}
	}
	
	/*
	 * Calculates the change in fitness a mutation of type Insert would cause
	 * Input: A mutation of type Insert. Insert contains an object and an index.
	 */
	public double getDeltaFitness(Insert insert, ProblemInstance problemInstance) {
		double deltaFitness = 0.0;
		double currentTime = this.startTime;
		
		int index = insert.getIndex();
		CarMove insertMove = (CarMove) insert.getObject();
		Node toNode = insertMove.getToNode();
		Node fromNode = insertMove.getFromNode();
		
		
		double addedTravelTime;
		if(index == 0) {
			addedTravelTime = problemInstance.getTravelTimeBike(this.startNode, fromNode) 
					+ problemInstance.getTravelTimeBike(toNode, this.carMoves.get(0).getFromNode())
					+ insertMove.getTravelTime()
					- problemInstance.getTravelTimeBike(this.startNode, this.carMoves.get(0).getFromNode());
		}
		
		for(CarMove move : this.chargingMoves) {
			int moveIndex = this.chargingMoveIndex.get(move);
			if (moveIndex >= index) {
				
			}
		}
		
		return 0.0;
	}
	private double getChargingFitness(double time, ChargingNode node) {
		return getCapacityPenalty(node, true, 0) - getChargingReward(time);
	}
	
	public double getDeltaFitness(Remove remove, ProblemInstance problemInstance){
		double currentFitness = this.fitness;
		double newFitness = this.fitness;
		int index = remove.getIndex();
		CarMove toRemove = carMoves.get(index);
		Node prevNode = (index > 0) ? carMoves.get(index - 1).getToNode() : this.startNode;
		Node nextNode = (index < carMoves.size()-1) ? carMoves.get(index + 1).getFromNode() : null;
		double deltaTime = getAbsDeltaTime(prevNode, toRemove , nextNode, problemInstance);
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
			if(startEndChargingMoves.get(chargingMove)[1] - deltaTime > Constants.TIME_LIMIT_STATIC_PROBLEM){
				// no change in fitness
				break;
			}
			if(doFitnessUpdates){
				//find fitness before and after
				double endTime = startEndChargingMoves.get(chargingMove)[1];
				double currentChargingReward = getChargingReward(endTime);
				double newChargingReward     = getChargingReward(endTime - deltaTime);
				double deltaReward = newChargingReward - currentChargingReward;
				newFitness += deltaReward;
				// kun en positiv endring i capacity fitness dersom chargingNoden går fra over planning period til under planning period!
				if(endTime > Constants.TIME_LIMIT_STATIC_PROBLEM && endTime - deltaTime < Constants.TIME_LIMIT_STATIC_PROBLEM){
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

	private double getAbsDeltaTime(Node prev, CarMove curr, Node next, ProblemInstance problemInstance){
		double currTimeContribution = problemInstance.getTravelTimeBike(prev, curr.getFromNode())
				+ curr.getTravelTime()
				+ ((next != null) ? problemInstance.getTravelTimeBike(curr.getToNode(), next) : 0);
		double newTimeContribution  = (next != null) ? problemInstance.getTravelTimeBike(prev, next) : 0;
		return Math.abs(currTimeContribution - newTimeContribution);
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
	
	private double getChargingReward(double time) {
		return (Math.max(this.timeLimit - time,0) * HeuristicsConstants.TABU_CHARGING_UNIT_REWARD);
	}
}
