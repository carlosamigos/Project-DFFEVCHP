package code.solver.heuristics.entities;

import java.util.ArrayList;
import java.util.HashMap;

import code.problem.nodes.ChargingNode;
import code.problem.nodes.Node;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.alns.ALNSIndividual;
import constants.Constants;
import constants.HeuristicsConstants;

public class Operator {

	public final int id;

	private ALNSIndividual individual;
	private ArrayList<ArrayList<Double>> travelTimesBike;
	private HashMap<ChargingNode, Integer> chargingCapacityUsedOperator;
	public HashMap<ParkingNode, Integer> movesToParkingNodeByOperator;

	private ArrayList<CarMove> carMoves; // only car moves that are performed
	private final Node startNode;
	private final double startTime;
	private final double timeLimit;
	private double travelTime;
	private double fitness;
	private boolean changed;
	
	public Operator(double startTime, double timeLimit, Node startNode, 
			ArrayList<ArrayList<Double>> travelTimesBike, int id, ALNSIndividual individual,
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
	
	public void setMovesToParkingNodeByOperator(HashMap<ParkingNode, Integer> movesToParkingNodeByOperator) {
		this.movesToParkingNodeByOperator = movesToParkingNodeByOperator;
	}

	/*
	 * Calculates the initial fitness of an operator. Could also be used if one wants to calculate fitness
	 * bottom up at some other point. The method iterates through all car moves calculate rewards based on the moves
	 * and when the moves happen. Fitness = chargingRewards + capacityFeasibility
	 */
	private void calculateFitness() {

		if(carMoves.size() == 0) {
			this.fitness = 100;
			return;
		}
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
		double travelTimeCarMove = 0.0;
		Node previousNode = this.startNode;
		this.fitness = 0.0;
		for(int i = 0; i < this.carMoves.size(); i++) {
			CarMove currentMove = this.carMoves.get(i);
			travelTimeCarMove += currentMove.getTravelTime();
			double travelTime = getTravelTime(previousNode, currentMove, currentTime);
			currentTime += travelTime;
			previousNode = currentMove.getToNode();

			if(currentTime > this.timeLimit) {
				this.fitness += travelTimeCarMove * HeuristicsConstants.ALNS_TRAVEL_TIME_CAR_MOVE_PENALTY;
				this.fitness += (this.carMoves.size() - (i+1)) * HeuristicsConstants.TABU_SIZE_OF_OPERATOR_LIST;
				this.fitness += (currentTime - travelTime) * HeuristicsConstants.ALNS_TRAVEL_COST;
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
		this.fitness += travelTimeCarMove * HeuristicsConstants.ALNS_TRAVEL_TIME_CAR_MOVE_PENALTY;
		this.fitness += currentTime * HeuristicsConstants.ALNS_TRAVEL_COST;
	}
	
	public double getTravelTime(Node previous, CarMove move, double currentTime) {
		double travelTimeBike = getTravelTimeBike(previous, move.getFromNode());
		return travelTimeBike + Math.max(0, move.getEarliestDepartureTime() - (currentTime + travelTimeBike) )
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
	
	public HashMap<ParkingNode, Integer> getMovesToParkingNodeByOperator() {
		return movesToParkingNodeByOperator;
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

	public double calculateTotalTravelTime(){
		double currentTime = this.startTime;
		Node previousNode = this.startNode;
		for(int i = 0; i < this.carMoves.size(); i++) {
			CarMove currentMove = this.carMoves.get(i);
			currentTime += getTravelTime(previousNode, currentMove, currentTime);
			previousNode = currentMove.getToNode();
		}
		return currentTime;
	}

	public void resetOperator(){
		for(ChargingNode chargingNode : chargingCapacityUsedOperator.keySet()) {
			this.chargingCapacityUsedOperator.put(chargingNode, 0);
		}
		for(ParkingNode parkingNode : movesToParkingNodeByOperator.keySet()) {
			this.movesToParkingNodeByOperator.put(parkingNode, 0);
		}
		this.changed = true;
		this.fitness = 0;

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
