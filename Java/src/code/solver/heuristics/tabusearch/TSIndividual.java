package code.solver.heuristics.tabusearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import code.problem.entities.Car;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.mutators.*;
import constants.Constants;
import constants.HeuristicsConstants;
import utils.ChromosomeGenerator;
import code.problem.nodes.Node;

import code.problem.ProblemInstance;
import code.solver.heuristics.Individual;
import code.solver.heuristics.entities.Operator;
import utils.MathHelper;

@SuppressWarnings("serial")
public class TSIndividual extends Individual implements Serializable {
	
	private ArrayList<Object> operators;

	// These tracks how good the proposed solution is.
	private HashMap<ChargingNode, Integer> capacitiesUsed;
	private HashMap<ChargingNode, Integer> prevCapacitiesUsed;
	private HashMap<ChargingNode, Integer> capacities;
	private HashMap<ParkingNode, Integer> deviationFromIdealState;

	//Keep track of all car moves not in use
	private HashMap<Car, ArrayList<CarMove>> unusedCarMoves;

	private ProblemInstance problemInstance;
	

	// Constructor used exlusively for testing
	public TSIndividual(HashMap<ChargingNode, Integer> capacitiesUsed) {
		this.capacitiesUsed = capacitiesUsed;
	}

	public TSIndividual(ProblemInstance problemInstance) {
		this.problemInstance = problemInstance;
		this.unusedCarMoves = ChromosomeGenerator.generateCarMovesFrom(problemInstance);

		// Constructing initial solution
		createOperators();
		initateCapacities();
		initiateDeviations();
		addCarMovesToOperators();
		calculateFitness();
		prevCapacitiesUsed = new HashMap<>(capacitiesUsed);
		// -----------------------------
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
					problemInstance.getOperators().get(i).getNextOrCurrentNode(), problemInstance.getTravelTimesBike(), problemInstance.getOperators().get(i).getId(), this);
			operators.add(op);
		}
	}

	private void initateCapacities(){
		capacities = new HashMap<>();
		capacitiesUsed = new HashMap<>();
		prevCapacitiesUsed = new HashMap<>();
		for(ChargingNode chargingNode : problemInstance.getChargingNodes()){
			capacitiesUsed.put(chargingNode, 0);
			prevCapacitiesUsed.put(chargingNode, 0);
			capacities.put(chargingNode, chargingNode.getNumberOfAvailableChargingSpotsNextPeriod());
		}
	}
	
	public void setCapacitiesUsed(HashMap<ChargingNode, Integer> capacitiesUsed) {
		this.capacitiesUsed = capacitiesUsed;
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
		HashMap<Car, ArrayList<CarMove>> carMovesCopy = new HashMap<>(this.unusedCarMoves);
		while(operatorAvailable){
			operatorAvailable = false;
			for (Object obop: this.operators) {
				Operator op = (Operator) obop;
				Node startNode = findPreviousNode(op);
				double startTime = op.getStartTime() + op.getTravelTime();
				CarMove chosen = findnearestCarMove(startNode, startTime, carMovesCopy);
				if(chosen != null){
					operatorAvailable = true;
					this.unusedCarMoves.get(chosen.getCar()).remove(chosen);
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
					double fitNessCancidate = rateCarMove(carMove, distance + carMove.getTravelTime());
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
			fitNess += -HeuristicsConstants.TABU_CHARGING_UNIT_INITIAL_REWARD * capacities.get(carMove.getToNode());
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
		//TODO: Update starting node as well
		deviationFromIdealState.put(parkingNode, deviationFromIdealState.get(parkingNode) +1);
	}

	// Update charging station capacities as car moves are chosen
	private void updateCapacity(ChargingNode chargingNode){
		capacities.put(chargingNode, capacities.get(chargingNode) - 1);
	}

	// Identifies which node a service operator is travelling from
	private Node findPreviousNode(Operator op){
		Node node;
		int carMoveSize = op.getCarMoveCopy().size();
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
			for(CarMove carMove : operator.getCarMoveCopy()){
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

	public void calculateFitness(){
		double totalFitness = 0;
		for(Object operator : operators){
			totalFitness += ((Operator) operator).getFitness();
			((Operator) operator).cleanCarMovesNotDone();
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

	public double deltaFitness(EjectionReplaceMutation ejectionReplaceMutation){
		Operator operator = ejectionReplaceMutation.getOperator();
		CarMove carMoveInsert = ejectionReplaceMutation.getCarMoveReplace();
		int removeIndex = ejectionReplaceMutation.getCarMoveIndex();

		HashMap<ChargingNode, Integer> oldChargingCapacityUsed = new HashMap<>(operator.getChargingCapacityUsedOperator());
		ArrayList<CarMove> oldCarMoves = operator.getCarMoveCopy();
		double oldFitness = operator.getFitness();

		operator.removeCarMove(removeIndex);
		operator.addCarMove(removeIndex, carMoveInsert);
		operator.getFitness(); // updates fitness

		double deltaFitness = operator.getFitness() - oldFitness;

		operator.setCarMoves(oldCarMoves);
		operator.setChargingCapacityUsedByOperator(oldChargingCapacityUsed);
		operator.setFitness(oldFitness);
		operator.setChanged(false);
		this.capacitiesUsed = new HashMap<>(this.prevCapacitiesUsed);
		return deltaFitness;
	}

	public double deltaFitness(EjectionInsertMutation ejectionInsertMutation){
		Operator operator = ejectionInsertMutation.getOperator();
		CarMove carMoveInsert = ejectionInsertMutation.getCarMoveReplace();
		int removeIndex = ejectionInsertMutation.getCarMoveIndex();

		HashMap<ChargingNode, Integer> oldChargingCapacityUsedOperator = new HashMap<>(operator.getChargingCapacityUsedOperator());
		ArrayList<CarMove> oldCarMoves = operator.getCarMoveCopy();
		double oldFitness = operator.getFitness();

		operator.addCarMove(removeIndex, carMoveInsert);
		operator.getFitness();

		double deltaFitness = operator.getFitness() - oldFitness;
		operator.setCarMoves(oldCarMoves);
		operator.setChargingCapacityUsedByOperator(oldChargingCapacityUsedOperator);
		operator.setFitness(oldFitness);
		operator.setChanged(false);
		this.capacitiesUsed = new HashMap<>(this.prevCapacitiesUsed);
		return deltaFitness;

	}

	public void performeMutation(EjectionReplaceMutation ejectionReplaceMutation){
		Operator operator = ejectionReplaceMutation.getOperator();
		int removeIndex = ejectionReplaceMutation.getCarMoveIndex();
		CarMove carMoveRemoved = operator.removeCarMove(removeIndex);
		operator.addCarMove(removeIndex, ejectionReplaceMutation.getCarMoveReplace());
		this.unusedCarMoves.get(carMoveRemoved.getCar()).remove(ejectionReplaceMutation.getCarMoveReplace());
		this.unusedCarMoves.get(carMoveRemoved.getCar()).add(carMoveRemoved);
		operator.getFitness();
		operator.cleanCarMovesNotDone();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);

		//Update list accordingly
	}

	public void performMutation(EjectionInsertMutation ejectionInsertMutation){
		Operator operator = ejectionInsertMutation.getOperator();
		int addIndex = ejectionInsertMutation.getCarMoveIndex();
		operator.addCarMove(addIndex, ejectionInsertMutation.getCarMoveReplace());
		this.unusedCarMoves.get(ejectionInsertMutation).remove(ejectionInsertMutation.getCarMoveReplace());
		operator.getFitness();
		operator.cleanCarMovesNotDone();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
	}

	// -------------------------------------------------------------------------------



	//-----------  Delta Fitnesses  --------------

	public double deltaFitness(IntraMove intraMove) {
		Operator operator = intraMove.getOperator();
		int removeIndex = intraMove.getRemoveIndex();
		int insertIndex = intraMove.getInsertIndex();

		// Save old state
		HashMap<ChargingNode, Integer> oldChargingCapacityUsedOperator = new HashMap<>(operator.getChargingCapacityUsedOperator());
		ArrayList<CarMove> oldCarMoves = operator.getCarMoveCopy();
		double oldFitness = operator.getFitness();

		// Do change
		CarMove carMove = operator.removeCarMove(removeIndex);
		operator.addCarMove(insertIndex, carMove);
		double deltaFitness = operator.getFitness() - oldFitness;

		// Revert
		operator.setCarMoves(oldCarMoves);
		operator.setChargingCapacityUsedByOperator(oldChargingCapacityUsedOperator);
		operator.setFitness(oldFitness);
		operator.setChanged(false);
		this.capacitiesUsed = new HashMap<>(this.prevCapacitiesUsed);
		return deltaFitness;
	}


	public double deltaFitness(InterMove interMove){
		Operator operatorRemove = interMove.getOperatorRemove();
		Operator operatorInsert = interMove.getOperatorInsert();
		int removeIndex 	    = interMove.getInsertIndex();
		int insertIndex 		= interMove.getInsertIndex();

		// Save old state
		HashMap<ChargingNode, Integer> oldChargingCapacityUsedRemoveOperator
				= new HashMap<>(operatorRemove.getChargingCapacityUsedOperator());
		HashMap<ChargingNode, Integer> oldChargingCapacityUsedInsertOperator
				= new HashMap<>(operatorInsert.getChargingCapacityUsedOperator());
		ArrayList<CarMove> oldCarMovesRemove = operatorRemove.getCarMoveCopy();
		ArrayList<CarMove> oldCarMovesInsert = operatorInsert.getCarMoveCopy();
		double oldFitnessRemove = operatorRemove.getFitness();
		double oldFitnessInsert = operatorInsert.getFitness();

		// Do change
		CarMove carMove = operatorRemove.removeCarMove(removeIndex);
		operatorRemove.getFitness();
		this.capacitiesUsed = new HashMap<>(this.prevCapacitiesUsed);
		operatorInsert.addCarMove(insertIndex, carMove);
		operatorInsert.getFitness();
		this.capacitiesUsed = new HashMap<>(this.prevCapacitiesUsed);
		double deltaFitness = (operatorInsert.getFitness() + operatorRemove.getFitness())
				- (oldFitnessInsert + oldFitnessRemove);

		// Revert
		operatorRemove.setCarMoves(oldCarMovesRemove);
		operatorInsert.setCarMoves(oldCarMovesInsert);
		operatorRemove.setChargingCapacityUsedByOperator(oldChargingCapacityUsedRemoveOperator);
		operatorInsert.setChargingCapacityUsedByOperator(oldChargingCapacityUsedInsertOperator);
		operatorRemove.setFitness(oldFitnessRemove);
		operatorInsert.setFitness(oldFitnessInsert);
		operatorRemove.setChanged(false);
		operatorInsert.setChanged(false);
		

		return deltaFitness;

	}


	//-----------  Perform Mutations --------------


	public void performMutation(IntraMove intraMove) {
		Operator operator = intraMove.getOperator();
		int removeIndex = intraMove.getRemoveIndex();
		int insertIndex = intraMove.getInsertIndex();
		CarMove carMove = operator.removeCarMove(removeIndex);
		operator.addCarMove(insertIndex, carMove);
		operator.getFitness();
		operator.cleanCarMovesNotDone();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
	}


	public void performMutation(InterMove interMove){
		Operator operatorRemove = interMove.getOperatorRemove();
		Operator operatorInsert = interMove.getOperatorInsert();
		int removeIndex 	    = interMove.getInsertIndex();
		int insertIndex 		= interMove.getInsertIndex();
		CarMove carMove = operatorRemove.removeCarMove(removeIndex);
		operatorRemove.getFitness();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
		operatorRemove.cleanCarMovesNotDone();
		operatorInsert.addCarMove(insertIndex, carMove);
		operatorInsert.getFitness();
		operatorInsert.cleanCarMovesNotDone();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
	}

	public ArrayList<Mutation> getNeighbors(int neighborhoodSize, TabuList tabuList){
		ArrayList<Mutation> neighbors = new ArrayList<>();
		// TODO: make smarter
		// 2/3 intra swaps
		while(neighbors.size() < neighborhoodSize) {
			int randomOperatorIndex = (int)Math.floor(Math.random() * operators.size());
			Operator operator = (Operator) operators.get(randomOperatorIndex);
			if(operator.getCarMoveListSize() <= 1) {
				continue;
			}
			int removeIndex = (int)Math.floor(Math.random() * operator.getCarMoveListSize());
			int insertIndex = MathHelper.getRandomIntNotEqual(removeIndex, operator.getCarMoveListSize());
			IntraMove intraMove = new IntraMove(operator,removeIndex, insertIndex);
			if(!tabuList.isTabu(intraMove)) {
				neighbors.add(intraMove);
			}
		}
		// 1/3 interswaps

		/*while(neighbors.size() < neighborhoodSize) {
			int removeOperatorIndex = (int)Math.floor(Math.random() * operators.size());
			Operator removeOperator = (Operator) operators.get(removeOperatorIndex);
			int insertOperatorIndex = MathHelper.getRandomIntNotEqual(removeOperatorIndex, operators.size());
			Operator insertOperator = (Operator) operators.get(insertOperatorIndex);
			if(removeOperator.getCarMoveListSize() == 0) {
				continue;
			}
			int removeIndex = (int)Math.floor(Math.random() * removeOperator.getCarMoveListSize());
			int insertIndex = (int)Math.floor(Math.random() * insertOperator.getCarMoveListSize());
			InterMove interMove = new InterMove(removeOperator,removeIndex, insertOperator, insertIndex);
			if(!tabuList.isTabu(interMove)) {
				neighbors.add(interMove);
			}
		}*/

		// ejectionReplace
		/*
		for (int i = 0; i < neighborhoodSize/3; i++) {
			int removeOperatorIndex = (int)Math.floor(Math.random() * operators.size());
			Operator removeOperator = (Operator) operators.get(removeOperatorIndex);
			int insertIndex = (int)Math.floor(Math.random() * removeOperator.getCarMoveListSize());
			int swapIndex = (int)Math.floor(Math.random() * this.unusedCarMoves.get(removeOperator.getCarMove(insertIndex).getCar()).size());
			CarMove swapCarMove = this.unusedCarMoves.get(removeOperator.getCarMove(insertIndex).getCar()).get(swapIndex);
			EjectionReplaceMutation ejectionReplaceMutation = new EjectionReplaceMutation(removeOperator, insertIndex, swapCarMove);
			//neighbors.add(ejectionReplaceMutation);


		}
		*/

		return neighbors;
	}


	public ArrayList<Object> getOperators() {
		return operators;
	}

	public void addToFitness(double delta) {
		this.fitness += delta;
	}

	public HashMap<ChargingNode, Integer> getCapacitiesUsed() {
		return capacitiesUsed;
	}
	
	public HashMap<ChargingNode, Integer> getPrevCapacitiesUsed() {
		return prevCapacitiesUsed;
	}

	public HashMap<Car, ArrayList<CarMove>> getUnusedCarMoves() {
		return unusedCarMoves;
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
			s += i.toString() + "\n";
		}
		return s;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	

}
