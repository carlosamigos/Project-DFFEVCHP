package code.solver.heuristics.tabusearch;

import java.util.ArrayList;
import java.util.HashMap;

import code.problem.entities.Car;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.entities.CarMove;
import constants.Constants;
import constants.HeuristicsConstants;
import utils.ChromosomeGenerator;
import code.problem.nodes.Node;

import code.problem.ProblemInstance;
import code.solver.heuristics.Individual;
import code.solver.heuristics.entities.Operator;
import code.solver.heuristics.mutators.IntraMove;

public class TSIndividual extends Individual {
	
	private ArrayList<Object> operators;

	//These tracks how good the proposed solution is. Thus we might need two of them - one that is stable (final) and one that keeps track
	private HashMap<ChargingNode, Integer> capacities;
	private HashMap<ParkingNode, Integer> deviationFromIdealState;

	//Keep track of all car moves not in use
	private HashMap<Car, ArrayList<CarMove>> carMoves;

	private ProblemInstance problemInstance;
	
	//Fitness parameters
	private double fitness = 0.0;
	private double costOfPostponed = 0.0;
	private double awardForCharging = 0.0;
	private double awardForMeetingIdeal = 0.0;
	private double costOfTravel = 0.0;
	private double costOfUnmetIdeal = 0.0;

	//Placeholder Weights


	public TSIndividual(ProblemInstance problemInstance) {
		this.problemInstance = problemInstance;
		this.carMoves = ChromosomeGenerator.generateCarMovesFrom(problemInstance);

		// Constructing initial solution
		createOperators();
		initiateCapacities();
		initiateDeviations();
		addCarMovesToOperators();
		// -----------------------------
		calculateFitnessOfIndividual();
	}

	//================================================================================
	// Construct the initial solutions
	//================================================================================


	// INITIATORS
	// -------------------------------------------------------------------------------

	//Initiate operators
	private void createOperators(){
		this.operators = new ArrayList<>();
		for (int i = 0; i < problemInstance.getOperators().size(); i++) {
			Operator op = new Operator(problemInstance.getOperators().get(i).getTimeRemainingToCurrentNextNode(), Constants.TIME_LIMIT_STATIC_PROBLEM,
					problemInstance.getOperators().get(i).getNextOrCurrentNode(), problemInstance.getTravelTimesBike(), capacities, problemInstance.getOperators().get(i).getId());
			operators.add(op);
		}

	}

	//Initiate charging station capacities
	private void initiateCapacities(){
		this.capacities = new HashMap<>();
		for (int i = 0; i < problemInstance.getChargingNodes().size(); i++) {
			capacities.put(problemInstance.getChargingNodes().get(i), problemInstance.getChargingNodes().get(i).getNumberOfAvailableChargingSpotsNextPeriod());
		}
	}

	//Initiate deviation from ideal states
	private void initiateDeviations(){
		this.deviationFromIdealState = new HashMap<>();
		for (int i = 0; i < problemInstance.getParkingNodes().size(); i++) {
			int deviation = problemInstance.getParkingNodes().get(i).getCarsRegular().size() + problemInstance.getParkingNodes().get(i).getCarsArrivingThisPeriod()
					- problemInstance.getParkingNodes().get(i).getIdealNumberOfAvailableCars();
			deviationFromIdealState.put(problemInstance.getParkingNodes().get(i), deviation);
		}
	}

	//  IDENTIFY CAR MOVES FOR INITIAL SOLUTION
	// -------------------------------------------------------------------------------

	// Overall algorithm
	// * Adds car moves until no car moves remaining
	private void addCarMovesToOperators() {
		boolean operatorAvailable = true;
		//HashMap<Car, ArrayList<CarMove>> carMovesCopy = ChromosomeGenerator.generateCarMovesFrom(problemInstance);
		HashMap<Car, ArrayList<CarMove>> carMovesCopy = new HashMap<>(this.carMoves);
		while(operatorAvailable){
			operatorAvailable = false;
			for (Object obop: this.operators) {
				Operator op = (Operator) obop;
				Node startNode = findPreviousNode(op);
				double startTime = op.getStartTime() + op.getTravelTime();
				CarMove chosen = findnearestCarMove(startNode, startTime, carMovesCopy);
				if(chosen != null){
					operatorAvailable = true;
					this.carMoves.get(chosen.getCar()).remove(chosen);
					op.addCarMove(chosen);
					carMovesCopy.remove(chosen.getCar());
					double addDistance = calculateDistanceCarMove(startNode, startTime, chosen) ;
					op.addToTravelTime(addDistance);
					if(chosen.getToNode() instanceof ChargingNode){
						updateCapacity((ChargingNode) chosen.getToNode());
					}else{
						updateDeviation((ParkingNode) chosen.getToNode());
					}
				}
			}
		}
	}

	// Identify car moves to be evaluated
	//* Identifies all car moves, and chooses the best each iteration
	private CarMove findnearestCarMove(Node node, double startTime, HashMap<Car, ArrayList<CarMove>> carMovesCopy){
		double distance = Integer.MAX_VALUE;
		double fitNess = Double.MAX_VALUE;
		CarMove cMove = null;
		for(Car car : carMovesCopy.keySet()){
			if(carMovesCopy.get(car).size() > 0){
				Node fromNode = carMovesCopy.get(car).get(0).getFromNode();
				double distanceCandidate = problemInstance.getTravelTimeBike(node, fromNode) +
						Math.max(carMovesCopy.get(car).get(0).getEarliestDepartureTime() - (startTime + problemInstance.getTravelTimeBike(node, fromNode)), 0);
				distance = distanceCandidate;
				for(CarMove carMove: carMovesCopy.get(car)){
					double fitNessCancidate = rateCarMove(carMove, distance);
					if(fitNessCancidate < fitNess){
						cMove = carMove;
						fitNess = fitNessCancidate;
					}
				}
			}
		}

		return cMove;
	}

	// Evaluate car moves based on fitness
	// * Assumes that deviation from ideal state is a positive number if there is no deficit.
	private double rateCarMove(CarMove carMove, double distance){
		double fitNess = 0;
		fitNess += (distance)* HeuristicsConstants.TABU_TRAVEL_COST_INITIAL_CONSTRUCTION;
		if(carMove.getToNode() instanceof ChargingNode){
			if(capacities.get(carMove.getToNode()) <= 0){
				fitNess += HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY;
			}
			// Capacity is positive
			fitNess += -HeuristicsConstants.TABU_CHARGING_UNIT_REWARD * capacities.get(carMove.getToNode());
		}if(carMove.getToNode() instanceof ParkingNode){
			if(deviationFromIdealState.get(carMove.getToNode()) >= 0){
				fitNess += HeuristicsConstants.TABU_SURPLUS_IDEAL_STATE_COST;
			}
			// Deviation is negative if deficit node
			fitNess += HeuristicsConstants.TABU_IDEAL_STATE_REWARD * deviationFromIdealState.get(carMove.getToNode());
		}
		return fitNess;

	}

	//  HELPER METHODS
	// -------------------------------------------------------------------------------

	// Update deviation from ideal state as car moves are chosen
	private void updateDeviation(ParkingNode parkingNode){
		deviationFromIdealState.put(parkingNode, deviationFromIdealState.get(parkingNode) +1);
	}

	// Update charging station capacities as car moves are chosen
	private void updateCapacity(ChargingNode chargingNode){
		capacities.put(chargingNode, capacities.get(chargingNode) - 1);
	}

	// Identifies which node a service operator is travelling from
	private Node findPreviousNode(Operator op){
		Node node;
		int carMoveSize = op.getCarMovesSize();
		if(carMoveSize > 0){
			node = op.getCarMove(carMoveSize - 1).getToNode();
		}else{
			node = op.getStartNode();
		}
		return node;
	}

	// Calculates the total distance to travel when evaluating a car move
	private double calculateDistanceCarMove(Node previousNode, double startTime, CarMove carMove){
		return problemInstance.getTravelTimeBike(previousNode, carMove.getFromNode()) +
				Math.max(0, carMove.getEarliestDepartureTime() - (startTime + problemInstance.getTravelTimeBike(previousNode, carMove.getFromNode())))
						+ carMove.getTravelTime();
	}


	// -------------------------------------------------------------------------------

	//================================================================================
	// Find moves that can replace ejected
	//================================================================================




	protected void calculateFitnessOfIndividual() {
		//TODO: To be tested. Should update all fitness-related parameters in Operators
		// Considers only charging time and capacity at the moment
		HashMap<ChargingNode, Integer> capacityUsed = new HashMap<>();
		double totalFitness = 0;
		// Calculate time reward fitness
		for(Object op : operators) {
			Operator operator = (Operator) op;
			double currentTime = operator.getStartTime();
			Node prevNode = operator.getStartNode();
			for(CarMove carMove : operator.getCarMoves()){
				currentTime += problemInstance.getTravelTimeBike(prevNode, carMove.getFromNode());
				currentTime += carMove.getTravelTime();
				if(carMove.isToCharging()){
					totalFitness -= operator.getChargingReward(currentTime);
					increaseCapacityUsedInNode((ChargingNode) carMove.getToNode(), capacityUsed);
				}
				prevNode = carMove.getToNode();
			}
		}
		for(ChargingNode chargingNode : problemInstance.getChargingNodes()){
			int availableChargingSpots = chargingNode.getNumberOfAvailableChargingSpotsNextPeriod();
			if(capacityUsed.get(chargingNode) != null){
				int used = capacityUsed.get(chargingNode);
				totalFitness += HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY * Math.max(used - availableChargingSpots,0);
			}
		}
		this.fitness = totalFitness;
	}

	@Override
	public ArrayList<Object> getRepresentation() {
		return this.operators;
	}

	private void increaseCapacityUsedInNode(ChargingNode chargingNode, HashMap<ChargingNode, Integer> capacityUsed){
		if(capacityUsed.get(chargingNode) == null){
			capacityUsed.put(chargingNode, 0);
		} capacityUsed.put(chargingNode, capacityUsed.get(chargingNode) + 1);
	}
	
	public double deltaFitness(IntraMove intraMove) {


		return 0.0;
		
	}
	
	public void performMutation(Swap1 swap) {
		/*
		 * 1. Remove
		 * 2. Insert
		 */
	}
	
	public void addToFitness(double delta) {
		this.fitness += delta;
	}
	
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public double optimalInsertionValue() {
		return 0.0;
	}
	
	@Override
	public String toString() {
		String s = "";
		for(Object i : this.getRepresentation()) {
			s += i.toString() + " ";
		}
		return s + "\n";
	}
}
