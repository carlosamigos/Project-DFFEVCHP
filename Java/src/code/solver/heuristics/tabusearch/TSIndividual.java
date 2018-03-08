package code.solver.heuristics.tabusearch;

import java.util.ArrayList;
import java.util.HashMap;

import code.problem.entities.Car;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.mutators.EjectionMutation;
import code.solver.heuristics.mutators.InterMove;
import code.solver.heuristics.mutators.Mutation;
import constants.Constants;
import constants.HeuristicsConstants;
import utils.ChromosomeGenerator;
import code.problem.nodes.Node;

import code.problem.ProblemInstance;
import code.solver.heuristics.Individual;
import code.solver.heuristics.entities.Operator;
import code.solver.heuristics.mutators.IntraMove;
import utils.MathHelper;

public class TSIndividual extends Individual {
	
	private ArrayList<Object> operators;

	//These tracks how good the proposed solution is. Thus we might need two of them - one that is stable (final) and one that keeps track
	private HashMap<ChargingNode, Integer> capacitiesUsed;
	private HashMap<ChargingNode, Integer> capacities;
	private HashMap<ParkingNode, Integer> deviationFromIdealState;

	//Keep track of all car moves not in use
	private HashMap<Car, ArrayList<CarMove>> carMoves;

	private ProblemInstance problemInstance;
	
	//Fitness parameters
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
		initateCapacities();
		initiateDeviations();
		addCarMovesToOperators();


		// -----------------------------
		calculateFitness();

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
					problemInstance.getOperators().get(i).getNextOrCurrentNode(), problemInstance.getTravelTimesBike(), problemInstance.getOperators().get(i).getId());
			operators.add(op);
		}
	}

	private void initateCapacities(){
		capacities = new HashMap<>();
		capacitiesUsed = new HashMap<>();
		for(ChargingNode chargingNode : problemInstance.getChargingNodes()){
			capacitiesUsed.put(chargingNode, 0);
			capacities.put(chargingNode, chargingNode.getNumberOfAvailableChargingSpotsNextPeriod());
		}
		for(Object op : operators){
			((Operator)op).setChargingCapacityUsedIndividual(capacitiesUsed);
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
		//TODO: Update starting node aswell
		deviationFromIdealState.put(parkingNode, deviationFromIdealState.get(parkingNode) +1);
	}

	// Update charging station capacities as car moves are chosen
	private void updateCapacity(ChargingNode chargingNode){
		capacities.put(chargingNode, capacities.get(chargingNode) - 1);
	}

	// Identifies which node a service operator is travelling from
	private Node findPreviousNode(Operator op){
		Node node;
		int carMoveSize = op.getCarMoves().size();
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
	// Fitness calculation
	//================================================================================


	protected double testCalculateFitnessOfIndividual() {
		//TODO: To be tested.
		// Considers only charging time and capacity at the moment
		HashMap<ChargingNode, Integer> capacityUsed = new HashMap<>();
		double totalFitness = 0;
		// Calculate time reward fitness
		for(Object op : operators) {
			Operator operator = (Operator) op;
			double currentTime = operator.getStartTime();
			Node prevNode = operator.getStartNode();
			for(CarMove carMove : operator.getCarMoves()){
				//Need to take earliest start time of the move into account
				currentTime += problemInstance.getTravelTimeBike(prevNode, carMove.getFromNode());
				currentTime += carMove.getTravelTime();
				if(currentTime > Constants.TIME_LIMIT_STATIC_PROBLEM){
					break;
				}
				if(carMove.isToCharging()){
					totalFitness -= operator.getChargingReward(currentTime);
					ChargingNode chargingNode = (ChargingNode) carMove.getToNode();
					if(capacityUsed.get(chargingNode) == null){
						capacityUsed.put(chargingNode, 0);
					} capacityUsed.put(chargingNode, capacityUsed.get(chargingNode) + 1);
				}
				prevNode = carMove.getToNode();
			}
		}
		// Calculate capacity penalties
		for(ChargingNode chargingNode : problemInstance.getChargingNodes()){
			int availableChargingSpots = chargingNode.getNumberOfAvailableChargingSpotsNextPeriod();
			if(capacityUsed.get(chargingNode) != null){
				int used = capacityUsed.get(chargingNode);
				totalFitness += HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY * Math.max(used - availableChargingSpots,0);
			}
		}
		return totalFitness;
	}

	private void calculateFitness(){
		double totalFitness = 0;
		for(Object operator : operators){
			totalFitness += ((Operator) operator).getFitness();
		}
		this.fitness = totalFitness;
		//System.out.println(totalFitness);
	}


	@Override
	public ArrayList<Object> getRepresentation() {
		return this.operators;
	}


	// -------------------------------------------------------------------------------

	//================================================================================
	// All mutations/performers for moves that are ejected
	//================================================================================

	public double deltaFitness(EjectionMutation ejectionMove){
		Operator operator = ejectionMove.getOperator();
		CarMove carMoveInsert = ejectionMove.getCarMoveReplace();
		int removeIndex = ejectionMove.getCarMoveIndex();

		HashMap<ChargingNode, Integer> oldChargingCapacityUsed = new HashMap<>(capacitiesUsed);
		ArrayList<CarMove> oldCarMoves = new ArrayList<>(operator.getCarMoves());
		double oldFitness = operator.getFitness();

		CarMove carMove = operator.removeCarMove(removeIndex);
		operator.addCarMove(removeIndex, carMoveInsert);
		operator.calculateFitness();

		double deltaFitness = operator.getFitness() - oldFitness;

		operator.setCarMoves(oldCarMoves);
		operator.setChargingCapacityUsedByOperator(oldChargingCapacityUsed);
		operator.setFitness(oldFitness);
		for(Object operator1 : operators){
			((Operator) operator1).setChargingCapacityUsedIndividual(oldChargingCapacityUsed);
		}
		capacitiesUsed = oldChargingCapacityUsed;

		/*
		 * 1. Fetch a random move for the same car
		 * 2. Remove: Create a remove mutation based on the incoming carmove
		 * 3. Insert: Create a insert mutation based on the new carmove
		 * 4. Return: Deltafitness calculated - I may have to create what it is to be replaced by seperately
		 */
		return deltaFitness;
	}

	public void performeMutation(EjectionMutation ejectionMove){
		Operator operator = ejectionMove.getOperator();
		int removeIndex = ejectionMove.getCarMoveIndex();
		operator.removeCarMove(removeIndex);
		operator.addCarMove(removeIndex, ejectionMove.getCarMoveReplace());
		/*
		 * 1. Remove the car move for operator at the given index
		 * 2. Inject the new carmove at the same position
		 * 3. Calculate new fitness
		 */

	}

	// -------------------------------------------------------------------------------



	//-----------  Delta Fitnesses  --------------

	public double deltaFitness(IntraMove intraMove) {
		Operator operator = intraMove.getOperator();
		int removeIndex = intraMove.getRemoveIndex();
		int insertIndex = intraMove.getInsertIndex();

		HashMap<ChargingNode, Integer> oldChargingCapacityUsed = new HashMap<>(capacitiesUsed);
		HashMap<ChargingNode, Integer> oldChargingCapacityUsedOperator = new HashMap<>(operator.getChargingCapacityUsedOperator());
		ArrayList<CarMove> oldCarMoves = new ArrayList<>(operator.getCarMoves());
		double oldFitness = operator.getFitness();
		
		CarMove carMove = operator.removeCarMove(removeIndex);
		//System.out.println(intraMove);
		operator.addCarMove(insertIndex, carMove);
		operator.calculateFitness();

		double deltaFitness = operator.getFitness() - oldFitness;

		operator.setCarMoves(oldCarMoves);
		operator.setChargingCapacityUsedByOperator(oldChargingCapacityUsedOperator);
		operator.setFitness(oldFitness);
		for(Object operator1 : operators){
			((Operator) operator1).setChargingCapacityUsedIndividual(oldChargingCapacityUsed);
		}
		capacitiesUsed = oldChargingCapacityUsed;

		return deltaFitness;
	}


	public double deltaFitness(InterMove interMove){
		// TODO
		Operator operatorRemove = interMove.getOperatorRemove();
		Operator operatorInsert = interMove.getOperatorInsert();
		int removeIndex 	    = interMove.getInsertIndex();
		int insertIndex 		= interMove.getInsertIndex();

		HashMap<ChargingNode, Integer> oldChargingCapacityUsed = new HashMap<>(capacitiesUsed);
		ArrayList<CarMove> oldCarMovesRemove = new ArrayList<>(operatorRemove.getCarMoves());
		ArrayList<CarMove> oldCarMovesInsert = new ArrayList<>(operatorInsert.getCarMoves());
		double oldFitness = this.fitness;

		CarMove carMove = operatorRemove.removeCarMove(removeIndex);
		operatorRemove.calculateFitness();
		operatorInsert.addCarMove(insertIndex, carMove);
		operatorInsert.calculateFitness();


		// Revert
		operatorRemove.setCarMoves(oldCarMovesRemove);
		//TODO


		double deltaFitness = this.fitness - oldFitness;
		return deltaFitness;

	}


	//-----------  Perform Mutations --------------


	public void performMutation(IntraMove intraMove) {
		Operator operator = intraMove.getOperator();
		int removeIndex = intraMove.getRemoveIndex();
		int insertIndex = intraMove.getInsertIndex();
		CarMove carMove = operator.removeCarMove(removeIndex);
		operator.addCarMove(insertIndex, carMove);

	}


	public void performMutation(InterMove interMove){
		// TODO
	}

	public ArrayList<Mutation> getNeighbors(int neighborhoodSize){
		ArrayList<Mutation> neighbors = new ArrayList<>();
		// TODO: make smarter
		for (int i = 0; i < neighborhoodSize; i++) {
			int randomOperatorIndex = (int)Math.floor(Math.random() * operators.size());
			Operator operator = (Operator) operators.get(randomOperatorIndex);
			int removeIndex = (int)Math.floor(Math.random() * operator.getCarMoves().size());
			int insertIndex = MathHelper.getRandomIntNotEqual(removeIndex, operators.size());
			//System.out.println("removeIndex: " + removeIndex + ", insertIndex: " + insertIndex );
			IntraMove mutation = new IntraMove(operator,removeIndex, insertIndex);
			neighbors.add(mutation);
		}
		return neighbors;
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
