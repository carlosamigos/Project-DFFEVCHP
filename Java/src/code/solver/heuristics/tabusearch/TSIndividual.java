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

	//Keep track of all car moves
	private HashMap<Car, ArrayList<CarMove>> carMoves;

	//Keep track of car moves that are not currently being used
	private HashMap<Car, ArrayList<CarMove>> unusedCarMoves;

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
		this.unusedCarMoves = ChromosomeGenerator.generateCarMovesFrom(problemInstance);

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
			//Todo: Does not take into account cars that we know will arrive to the parking node during the planning period.
			int deviation = problemInstance.getParkingNodes().get(i).getCarsRegular().size() + problemInstance.getParkingNodes().get(i).getCarsArrivingThisPeriod()
					- problemInstance.getParkingNodes().get(i).getIdealNumberOfAvailableCars();
			deviationFromIdealState.put(problemInstance.getParkingNodes().get(i), deviation);
		}
	}

	//  IDENTIFY CAR MOVES FOR INITIAL SOLUTION
	// -------------------------------------------------------------------------------

	// Overall algorithm
	private void addCarMovesToOperators() {
		boolean operatorAvailable = true;
		HashMap<Car, ArrayList<CarMove>> carMovesCopy = ChromosomeGenerator.generateCarMovesFrom(problemInstance);
		while(operatorAvailable){
			operatorAvailable = false;
			for (Object obop: this.operators) {
				Operator op = (Operator) obop;
				Node startNode = findPreviousNode(op);
				CarMove chosen = findnearestCarMove(startNode, carMovesCopy);
				if(chosen != null){
					if(timeAvailable(op, startNode, chosen)){
						operatorAvailable = true;
						this.unusedCarMoves.get(chosen.getCar()).remove(chosen);
						op.addCarMove(chosen);
						carMovesCopy.remove(chosen.getCar());
						double addDistance = calculateDistanceCarMove(startNode, chosen);
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
	}

	// Identify car moves to be evaluated
	private CarMove findnearestCarMove(Node node, HashMap<Car, ArrayList<CarMove>> carMovesCopy){
		double distance = Integer.MAX_VALUE;
		double fitNess = Double.MAX_VALUE;
		CarMove cMove = null;
		for(Car car : carMovesCopy.keySet()){
			if(carMovesCopy.get(car).size() > 0){
				Node fromNode = carMovesCopy.get(car).get(0).getFromNode();
				double distanceCandidate = problemInstance.getTravelTimeBike(node, fromNode);
				//Accepting twice the distance
				if(distanceCandidate < distance*3){
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
		}
		return cMove;
	}

	// Evaluate car moves based on fitness
	private double rateCarMove(CarMove carMove, double distance){
		double fitNess = 0;
		fitNess += (carMove.getTravelTime() + distance)* HeuristicsConstants.TABU_TRAVEL_COST_INITIAL_CONSTRUCTION;
		if(carMove.getToNode() instanceof ChargingNode){
			if(capacities.get(carMove.getToNode()) <= 0){
				fitNess = HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY;
			}
			fitNess += -HeuristicsConstants.TABU_CHARGING_UNIT_REWARD * capacities.get(carMove.getToNode());
		}if(carMove.getToNode() instanceof ParkingNode){
			if(deviationFromIdealState.get(carMove.getToNode()) >= 0){
				fitNess = HeuristicsConstants.TABU_SURPLUS_IDEAL_STATE_COST;
			}
			fitNess += -HeuristicsConstants.TABU_IDEAL_STATE_REWARD * deviationFromIdealState.get(carMove.getToNode());
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
	private double calculateDistanceCarMove(Node previousNode, CarMove carMove){
		return problemInstance.getTravelTimeBike(previousNode, carMove.getFromNode()) + carMove.getTravelTime();
	}

	// Calculates if there is available time left to do another move.
	private boolean timeAvailable(Operator op, Node previousNode, CarMove carMove){
		double addDistance = calculateDistanceCarMove(previousNode, carMove);
		return (op.getStartTime() + op.getTravelTime() + addDistance < op.getTimeLimit());
	}

	// -------------------------------------------------------------------------------



	protected double calculateFitnessOfIndividual() {
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
		return totalFitness;
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
		Operator operator = intraMove.getOperator();
		int removeIndex = intraMove.getRemoveIndex();
		int insertIndex = intraMove.getInsertIndex();
		
		HashMap<ChargingNode, Integer> oldChargingCapacityUsed = new HashMap<>(capacities); 
		ArrayList<CarMove> oldCarMoves = new ArrayList<>(operator.getCarMoves());
		double oldFitness = operator.getFitness();
		
		CarMove carMove = operator.removeCarMove(removeIndex);
		operator.addCarMove(insertIndex, carMove);
		operator.calculateFitness();
		
		double deltaFitness = operator.getFitness() - oldFitness;
		
		operator.setCarMoves(oldCarMoves);
		operator.setChargingCapacityUsed(oldChargingCapacityUsed);
		operator.setFitness(oldFitness);
		
		return deltaFitness;
	}
	
	public void performMutation(IntraMove intraMove) {
		Operator operator = intraMove.getOperator();
		int removeIndex = intraMove.getRemoveIndex();
		int insertIndex = intraMove.getInsertIndex();
		CarMove carMove = operator.removeCarMove(removeIndex);
		operator.addCarMove(insertIndex, carMove);
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
