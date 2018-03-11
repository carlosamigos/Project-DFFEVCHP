package code.solver.heuristics.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import code.problem.nodes.ChargingNode;
import code.problem.nodes.Node;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.tabusearch.TSIndividual;
import constants.Constants;
import constants.HeuristicsConstants;

@SuppressWarnings("serial")
public class Operator implements Serializable {

	public final int id;

	private TSIndividual individual;
	private ArrayList<ArrayList<Double>> travelTimesBike;
	private HashMap<ChargingNode, Integer> chargingCapacityUsedOperator;
	private HashMap<ParkingNode, Integer> movesToParkingNodeByOperator;

	private ArrayList<CarMove> carMoves; // only car moves that are performed
	private final Node startNode;
	private final double startTime;
	private final double timeLimit;
	private double travelTime;
	private double fitness;
	private boolean changed;
	
	public Operator(double startTime, double timeLimit, Node startNode, 
			ArrayList<ArrayList<Double>> travelTimesBike, int id, TSIndividual individual,
			ArrayList<ChargingNode> chargingNodes, ArrayList<ParkingNode> parkingNodes) {
		this.individual = individual;
		this.carMoves = new ArrayList<>();
		this.startTime = startTime;
		this.timeLimit = timeLimit;
		this.startNode = startNode;
		this.travelTimesBike = travelTimesBike;
		initializeChargingCapacityUsedByOperator(chargingNodes);
		initializeMovesToParkingNodeByOperator(parkingNodes);
		this.id = id;
		this.changed = false;
	}
	
	private void initializeChargingCapacityUsedByOperator(ArrayList<ChargingNode> chargingNodes) {
		this.chargingCapacityUsedOperator = new HashMap<>();
		for(ChargingNode chargingNode : chargingNodes) {
			this.chargingCapacityUsedOperator.put(chargingNode, 0);
		}
	}
	
	private void initializeMovesToParkingNodeByOperator(ArrayList<ParkingNode> parkingNodes) {
		this.movesToParkingNodeByOperator = new HashMap<>();
		for(ParkingNode parkingNode : parkingNodes) {
			this.movesToParkingNodeByOperator.put(parkingNode, 0);
		}
	}
	
	public CarMove getCarMove(int index) {
		return this.carMoves.get(index);
	}

	public double getTimeLimit(){
		return this.timeLimit;
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
		for(ChargingNode chargingNode : this.chargingCapacityUsedOperator.keySet()) {
			int used = this.individual.getCapacitiesUsed().get(chargingNode);
			int usedByOperator = this.chargingCapacityUsedOperator.get(chargingNode);
			int delta = used - usedByOperator;
			this.individual.getCapacitiesUsed().put(chargingNode, delta);
			this.chargingCapacityUsedOperator.put(chargingNode, 0);
		}
		
		for(ParkingNode parkingNode : this.movesToParkingNodeByOperator.keySet()) {
			int idealState = this.individual.getDeviationIdealState().get(parkingNode);
			int idealStateMetByOperator = this.movesToParkingNodeByOperator.get(parkingNode);
			int delta = idealState - idealStateMetByOperator;
			this.individual.getDeviationIdealState().put(parkingNode, delta);
			this.movesToParkingNodeByOperator.put(parkingNode, 0);
		}
		
		double currentTime = this.startTime;
		Node previousNode = this.startNode;
		this.fitness = 0.0;
		for(int i = 0; i < this.carMoves.size(); i++) {
			CarMove currentMove = this.carMoves.get(i);
			currentTime += getTravelTime(previousNode, currentMove);
			previousNode = currentMove.getToNode();
			this.fitness += currentTime * HeuristicsConstants.TABU_TRAVEL_COST;

			if(currentTime > this.timeLimit) {
				return;
			}
			
			if(currentMove.isToCharging()) {
				ChargingNode chargingNode = (ChargingNode) currentMove.getToNode();
				this.fitness -= getChargingReward(currentTime);
				
				this.chargingCapacityUsedOperator.put(chargingNode, 
						this.chargingCapacityUsedOperator.get(chargingNode) + 1);
				this.individual.getCapacitiesUsed().put(chargingNode, 
						this.individual.getCapacitiesUsed().get(chargingNode) + 1);
			} else {
				ParkingNode parkingNode = (ParkingNode) currentMove.getToNode();
				this.movesToParkingNodeByOperator.put(parkingNode, 
						this.movesToParkingNodeByOperator.get(parkingNode) + 1);
				this.individual.getDeviationIdealState().put(parkingNode, 
						this.individual.getDeviationIdealState().get(parkingNode) + 1);
			}
		}
	}
	
	private double getTravelTime(Node previous, CarMove move) {
		return getTravelTimeBike(previous, move.getFromNode()) 
				+ move.getTravelTime();
	}
	
	private double getTravelTimeBike(Node n1, Node n2) {
		return this.travelTimesBike
				.get(n1.getNodeId() - Constants.START_INDEX)
				.get(n2.getNodeId() - Constants.START_INDEX);
	}

	public double getChargingReward(double time) {
		return (Math.max(this.timeLimit - time,0) * HeuristicsConstants.TABU_CHARGING_UNIT_REWARD);
	}

	public HashMap<ChargingNode, Integer> getChargingCapacityUsedOperator() {
		return chargingCapacityUsedOperator;
	}

	public void cleanCarMovesNotDone(){
//		Node previousNode = this.startNode;
//		CarMove currentMove;
	}
	
	public void setChanged(boolean change){
		this.changed = change;
	}
	
	public boolean getChanged() {
		return this.changed;
	}

	@Override
	public String toString() {
		String s = "";
		for(CarMove carMove : carMoves) {
			s += carMove + ", ";
		}
		
		return s.substring(0, (s.length() > 0) ? s.length()-2 : 0);
	}

	
	
}
