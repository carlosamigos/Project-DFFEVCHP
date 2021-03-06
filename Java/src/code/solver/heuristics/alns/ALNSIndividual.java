package code.solver.heuristics.alns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import code.problem.entities.Car;
import code.problem.nodes.ChargingNode;
import code.problem.nodes.ParkingNode;
import code.solver.heuristics.RelatednessMeasure;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.mutators.*;
import code.solver.heuristics.searches.*;
import constants.Constants;
import constants.HeuristicsConstants;
import utils.ChromosomeGenerator;
import code.problem.nodes.Node;

import code.problem.ProblemInstance;
import code.solver.heuristics.Individual;
import code.solver.heuristics.entities.Operator;
import utils.MathHelper;

public class ALNSIndividual extends Individual {
	
	private ArrayList<Object> operators;

	// These tracks how good the proposed solution is.
	private HashMap<ChargingNode, Integer> capacitiesUsed;
	private HashMap<ChargingNode, Integer> prevCapacitiesUsed;
	private HashMap<ChargingNode, Integer> capacities; // Only for initialization
	
	private HashMap<ParkingNode, Integer> deviationFromIdealState;
	private HashMap<ParkingNode, Integer> prevDeviationFromIdealState;
	private HashMap<ParkingNode, Integer> initialDeviationFromIdealState;

	//Keep track of all car moves not in use
	private HashMap<Car, ArrayList<CarMove>> unusedCarMoves;
	private HashMap<Car, Integer> carMovesCounter;
	private Set<Car> carsNotInUse;

	//Keep track of problem size
	private int problemSize;

	private ProblemInstance problemInstance;
	
	public ALNSIndividual(HashMap<ChargingNode, Integer> capacitiesUsed) {
		this.capacitiesUsed = capacitiesUsed;
	}

	public ALNSIndividual(ProblemInstance problemInstance) {
		this.problemInstance = problemInstance;
		this.unusedCarMoves = ChromosomeGenerator.generateCarMovesFrom(problemInstance);
		this.carMovesCounter = countCarMoves();
		this.carsNotInUse = new HashSet<Car>();

		// Set up the general construct
		createOperators();
		initateCapacities();
		initiateDeviations();

		//Store the problem size
		this.problemSize = calculateProblemSize();

		// Build the initial solution
		if(HeuristicsConstants.ALNS_INITIAL_GREEDY_BUILD){
			addCarMovesToOperators();
			initiateDeviations();
			//prevDeviationFromIdealState = new HashMap<>(deviationFromIdealState);
			calculateFitness();
			inititatePrevMaps();
		}
		else{
			//prevDeviationFromIdealState = new HashMap<>(deviationFromIdealState);
			calculateFitness();
			inititatePrevMaps();
			addCarMovesToOperatorsRegret();
			initiateDeviations();
			//prevDeviationFromIdealState = new HashMap<>(deviationFromIdealState);
			calculateFitness();
			inititatePrevMaps();
		}
//		for(Object o : operators) {
//			Operator op = (Operator) o;
//			for(CarMove carMove : op.getCarMoveCopy()) {
//				System.out.print(carMove + "[" + carMove.getTravelTime() + "] ");
//			}
//			System.out.println();
//		}

	}

	//================================================================================
	// Construct the initial solutions
	//================================================================================


	// INITIATORS
	// -------------------------------------------------------------------------------

	// * Initiate carMovesCopy
	private HashMap<Car, Integer> countCarMoves(){
		HashMap<Car, Integer> carMovesCounter = new HashMap<>();
		for (Car car: unusedCarMoves.keySet()) {
			carMovesCounter.put(car, unusedCarMoves.get(car).size());
		}
		return carMovesCounter;
	}

	// * Initiate operators
	private void createOperators(){
		operators = new ArrayList<>();
		for (int i = 0; i < problemInstance.getOperators().size(); i++) {
			Operator op = new Operator(problemInstance.getOperators().get(i).getTimeRemainingToCurrentNextNode(), 
					Constants.TIME_LIMIT_STATIC_PROBLEM, problemInstance.getOperators().get(i).getNextOrCurrentNode(), 
					problemInstance.getTravelTimesBike(), problemInstance.getOperators().get(i).getId(), this, 
					problemInstance.getChargingNodes(), problemInstance.getParkingNodes());
			operators.add(op);
		}
	}

	// * Initiate capacities
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

	// * Initiate deviation from ideal states
	private void initiateDeviations(){
		deviationFromIdealState = new HashMap<>();
		initialDeviationFromIdealState =  new HashMap<>();
		for (int i = 0; i < problemInstance.getParkingNodes().size(); i++) {
			int deviation = problemInstance.getParkingNodes().get(i).getCarsRegular().size() + problemInstance.getParkingNodes().get(i).getCarsArrivingThisPeriod()
					- problemInstance.getParkingNodes().get(i).getIdealNumberOfAvailableCars();
			deviationFromIdealState.put(problemInstance.getParkingNodes().get(i), deviation);
			initialDeviationFromIdealState.put(problemInstance.getParkingNodes().get(i), deviation);
		}

	}

	private void inititatePrevMaps(){
		prevCapacitiesUsed = new HashMap<>(capacitiesUsed);
		prevDeviationFromIdealState = new HashMap<>(deviationFromIdealState);
	}

	//  IDENTIFY CAR MOVES FOR INITIAL SOLUTION
	// -------------------------------------------------------------------------------

	// * Approach 1: Add car moves sequentially
	private void addCarMovesToOperators() {
		boolean operatorAvailable = true;
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
						updateDeviation(chosen);
					}
				}
			}
		}
	}
	// * Approach 2: Calculates based on regret value
	private void addCarMovesToOperatorsRegret(){
		boolean operatorAvailable = true;
		HashMap<Car, ArrayList<CarMove>> carMovesCopy = new HashMap<>(this.unusedCarMoves);
		while(operatorAvailable){
			operatorAvailable = false;
			CarMove carMove = null;
			Operator op = null;
			int indexRegret = 0;
			double regret = Double.MAX_VALUE;
			for (Car car: carMovesCopy.keySet()) {
				for (CarMove carM: carMovesCopy.get(car)) {
					int index = 0;
					double[] operatorFitness = new double[this.operators.size()];
					int[] operatorIndex = new int[this.operators.size()];
					for (Object operat: this.operators){
						Operator operator = (Operator) operat;
						double fitnessBest = Double.MAX_VALUE;
						int insertIndexCandidate = 0;
						for (int i = 0; i <= operator.getCarMoveListSize(); i++) {
							double fitnessDelta = rateCarMoveRegret(operator, carM, i);
							if (fitnessDelta < fitnessBest) {
								fitnessBest = fitnessDelta;
								insertIndexCandidate = i;
								operatorAvailable = true;
							}
						}
						operatorFitness[index] = fitnessBest;
						operatorIndex[index] = insertIndexCandidate;
						index++;
					}
					double regretCandidate = calculateRegret(operatorFitness);
					if(regretCandidate < regret){
						regret = regretCandidate;
						int candidateIndex = calculateLargestIndex(operatorFitness);
						op = (Operator) this.operators.get(candidateIndex);
						carMove = carM;
						indexRegret = operatorIndex[candidateIndex];
					}
				}
			}
			if(operatorAvailable) {
				op.addCarMove(indexRegret, carMove);
				carMovesCopy.remove(carMove.getCar());
				op.getFitness();
				this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
				this.prevDeviationFromIdealState = new HashMap<>(this.deviationFromIdealState);
				this.unusedCarMoves.get(carMove.getCar()).remove(carMove);
			}
		}
		resetOperator();
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
				fitNess += HeuristicsConstants.TABU_INITIAL_BREAK_CHARGING_CAPACITY;
			}
			// Capacity is positive
			fitNess += -HeuristicsConstants.TABU_CHARGING_UNIT_INITIAL_REWARD * capacities.get(carMove.getToNode());
		}if(carMove.getToNode() instanceof ParkingNode){
			if(deviationFromIdealState.get(carMove.getToNode()) >= 0){
				fitNess += HeuristicsConstants.TABU_INITIAL_SURPLUS_IDEAL_STATE_COST;
			}
			// Deviation is negative if deficit node
			fitNess += HeuristicsConstants.TABU_IDEAL_STATE_INITIAL_REWARD * deviationFromIdealState.get(carMove.getToNode());
		}
		return fitNess;

	}

	private double rateCarMoveRegret(Operator op, CarMove carMove, int index){
		OperatorState operatorState = setOperatorState(op);
		op.addCarMove(index, carMove);

		// Calculate fitness
		double deltaFitness = calculateDeltaFitness(op, operatorState);
		deltaFitness += HeuristicsConstants.TABU_TRAVEL_COST_INITIAL_CONSTRUCTION * op.calculateTotalTravelTime();

		// Revert
		resetOperator(operatorState);

		return deltaFitness;

	}

	//  HELPER METHODS
	// -------------------------------------------------------------------------------

	// Update deviation from ideal state as car moves are chosen
	private void updateDeviation(CarMove carMove){
		ParkingNode fromNode = (ParkingNode) carMove.getFromNode();
		ParkingNode toNode = (ParkingNode) carMove.getToNode();
		deviationFromIdealState.put(toNode, deviationFromIdealState.get(toNode) + 1);
		deviationFromIdealState.put(fromNode, deviationFromIdealState.get(fromNode) - 1);
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

	//Calculate
	private int calculateLargestIndex(double[] fitness) {
		int index = 0;
		double largest = Double.MAX_VALUE;
		for (int i = 0; i < fitness.length; i++) {
			if (fitness[i] < largest) {
				largest = fitness[i];
				index = i;
			}
		}
		return index;
	}

	private double calculateRegret(double[] fitness){
		double largest = Double.MAX_VALUE;
		double second = Double.MAX_VALUE;
		for (int i = 0; i < fitness.length; i++) {
			if(fitness[i] < largest){
				largest = fitness[i];
			}else if(fitness[i] < second){
				second = fitness[i];
			}
		}
		return largest - second;
	}

	private void resetOperator(){
		for(Object op: this.operators){
			Operator operator = (Operator) op;
			operator.resetOperator();
		}
	}

	private int calculateProblemSize(){
		int problemSize = 0;
		for(Car car: this.unusedCarMoves.keySet()){
			problemSize += this.unusedCarMoves.get(car).size();
		}return problemSize;
	}

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
//			((Operator) operator).cleanCarMovesNotDone();
		}
		totalFitness += calculateCapacityFitness();
		
		totalFitness += calculateIdealStateFitness();
		this.fitness = totalFitness;
	}

	private double calculateCapacityFitness(){
		double penalty = 0.0;
		double reward = 0.0;
		for(ChargingNode chargingNode : this.capacitiesUsed.keySet()) {
			int usedNow = this.capacitiesUsed.get(chargingNode);
			int capacity = chargingNode.getNumberOfAvailableChargingSpotsNextPeriod();

			reward += usedNow;
			penalty += Math.max(0, usedNow - capacity);
		}

		return (penalty * HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY) 
				- (reward * HeuristicsConstants.ALNS_CHARGING_REWARD);
	}

	private double calculateDeltaCapacityFitness(){
		double newPenalty = 0.0;
		double oldPenalty = 0.0;
		double newReward = 0.0;
		double oldReward = 0.0;

		for(ChargingNode chargingNode : this.capacitiesUsed.keySet()) {
			int usedNow = this.capacitiesUsed.get(chargingNode);
			int usedBefore = this.prevCapacitiesUsed.get(chargingNode);
			int capacity = chargingNode.getNumberOfAvailableChargingSpotsNextPeriod();

			newReward += usedNow;
			oldReward += usedBefore;
			newPenalty += Math.max(0, usedNow - capacity);
			oldPenalty += Math.max(0, usedBefore - capacity);
		}

		return (newPenalty - oldPenalty) * HeuristicsConstants.TABU_BREAK_CHARGING_CAPACITY
				- (newReward - oldReward) * HeuristicsConstants.ALNS_CHARGING_REWARD;
	}
	
	private double calculateIdealStateFitness() {
		int deviationNow = 0;
		int initialDeviation = 0;
		
		
		for(ParkingNode parkingNode : this.deviationFromIdealState.keySet()) {
			deviationNow += Math.min(0, this.deviationFromIdealState.get(parkingNode));
			initialDeviation += Math.min(0, this.initialDeviationFromIdealState.get(parkingNode));
		}
		
		// If the difference is positive we have better met ideal state than before
		return - (deviationNow - initialDeviation) * HeuristicsConstants.ALNS_IDEAL_STATE_UNIT_REWARD;
	}
	
	private double calculateDeltaIdealStateFitness() {
		int deviationNow = 0;
		int deviationBefore = 0;
		
		for(ParkingNode parkingNode : this.deviationFromIdealState.keySet()) {
			deviationNow += Math.min(0, this.deviationFromIdealState.get(parkingNode));
			deviationBefore += Math.min(0, this.prevDeviationFromIdealState.get(parkingNode));
		}
		
		// If the difference is positive we have better met ideal state than before
		return - (deviationNow - deviationBefore) * HeuristicsConstants.ALNS_IDEAL_STATE_UNIT_REWARD;
	}



	//================================================================================
	// Delta fitness
	//================================================================================

	public double deltaFitness(IntraMove intraMove) {
		Operator operator = intraMove.getOperator();
		int removeIndex = intraMove.getRemoveIndex();
		int insertIndex = intraMove.getInsertIndex();

		// Save old state
		OperatorState operatorState = setOperatorState(operator);

		// Do mutation
		CarMove carMove = operator.removeCarMove(removeIndex);
		operator.addCarMove(insertIndex, carMove);
		
		// Calculate fitness
		double deltaFitness = calculateDeltaFitness(operator, operatorState);
		
		// Revert
		resetOperator(operatorState);
		
		return deltaFitness;
	}

	public double deltaFitness(InterMove interMove){
		Operator operatorRemove = interMove.getOperatorRemove();
		Operator operatorInsert = interMove.getOperatorInsert();
		int removeIndex 	    = interMove.getInsertIndex();
		int insertIndex 		= interMove.getInsertIndex();
		
		// Save old state
		ArrayList<Operator> operatorsToChange = new ArrayList<Operator>() {{
			add(operatorRemove);
			add(operatorInsert);
		}};
		ArrayList<OperatorState> operatorStates = setOperatorStates(operatorsToChange);
		
		// Do mutation
		CarMove carMove = operatorRemove.removeCarMove(removeIndex);
		operatorInsert.addCarMove(insertIndex, carMove);
		
		// Calculate fitness
		double deltaFitness = calculateDeltaFitness(operatorsToChange, operatorStates);
		
		//Revert
		resetOperators(operatorStates);

		return deltaFitness;

	}
	
	public double deltaFitness(InterSwap2 interSwap2) {
		Operator operator1 = interSwap2.getOperator1();
		Operator operator2 = interSwap2.getOperator2();
		int index1 = interSwap2.getIndex1();
		int index2 = interSwap2.getIndex2();
		
		// Save old state
		ArrayList<Operator> operatorsToChange = new ArrayList<Operator>() {{
			add(operator1);
			add(operator2);
		}};
		ArrayList<OperatorState> operatorStates = setOperatorStates(operatorsToChange);
		
		// Do mutation
		CarMove carMove1 = operator1.removeCarMove(index1);
		CarMove carMove2 = operator2.removeCarMove(index2);
		operator1.addCarMove(index1, carMove2);
		operator2.addCarMove(index2, carMove1);
		
		// Calculate fitness
		double deltaFitness = calculateDeltaFitness(operatorsToChange, operatorStates);
				
		//Revert
		resetOperators(operatorStates);
		
		return deltaFitness;
	}

	public double deltaFitness(Inter2Move inter2move){
		Operator operator1 = inter2move.getOperatorRemove();
		Operator operator2 = inter2move.getOperatorInsert();
		int index1 = inter2move.getRemoveIndex1();
		int index2 = inter2move.getRemoveIndex2();
		//index2 = index2 > index1 ? index2--: index2;

		ArrayList<Operator> operatorsToChange = new ArrayList<Operator>() {{
			add(operator1);
			add(operator2);
		}};
		ArrayList<OperatorState> operatorStates = setOperatorStates(operatorsToChange);

		// Do mutation
		CarMove carMove1 = operator1.removeCarMove(index1);
		CarMove carMove2 = operator1.removeCarMove(index1);
		operator2.addCarMove(inter2move.getInsertIndex(), carMove1);
		operator2.addCarMove(inter2move.getInsertIndex() + 1, carMove2);

		// Calculate fitness
		double deltaFitness = calculateDeltaFitness(operatorsToChange, operatorStates);

		//Revert
		resetOperators(operatorStates);

		return deltaFitness;

	}
	
	public double deltaFitness(EjectionReplaceMutation ejectionReplaceMutation){
		Operator operator = ejectionReplaceMutation.getOperator();
		CarMove carMoveInsert = ejectionReplaceMutation.getCarMoveReplace();
		int removeIndex = ejectionReplaceMutation.getCarMoveIndex();

		//Save old operator
		OperatorState operatorState = setOperatorState(operator);

		//Do mutation;
		operator.removeCarMove(removeIndex);
		operator.addCarMove(removeIndex, carMoveInsert);

		//Calculate fitness
		double deltaFitness = calculateDeltaFitness(operator, operatorState);

		//Reset operator
		resetOperator(operatorState);

		return deltaFitness;
	}

	public double deltaFitness(EjectionInsertMutation ejectionInsertMutation){
		Operator operator = ejectionInsertMutation.getOperator();
		CarMove carMoveInsert = ejectionInsertMutation.getCarMoveReplace();
		int insertIndex = ejectionInsertMutation.getCarMoveIndex();

		//Save old operator
		OperatorState operatorState = setOperatorState(operator);

		//Do mutation;
		operator.addCarMove(insertIndex, carMoveInsert);

		//Calculate fitness
		double deltaFitness = calculateDeltaFitness(operator, operatorState);

		//Reset operator
		resetOperator(operatorState);

		return deltaFitness;
	}

	public double deltaFitness(EjectionRemoveMutation ejectionRemoveMutation){
		Operator operator = ejectionRemoveMutation.getOperator();
		int removeIndex = ejectionRemoveMutation.getCarMoveIndex();

		//Save old operator
		OperatorState operatorState = setOperatorState(operator);

		//Do mutation;
		operator.removeCarMove(removeIndex);

		//Calculate fitness
		double deltaFitness = calculateDeltaFitness(operator, operatorState);

		//Reset operator
		resetOperator(operatorState);

		return deltaFitness;
	}

	public double deltaFitness(EjectionSwapMutation ejectionSwapMutation){
		Operator operator = ejectionSwapMutation.getOperator();
		int removeIndex = ejectionSwapMutation.getCarMoveIndex();

		OperatorState operatorState = setOperatorState(operator);

		//Do mutation;
		operator.removeCarMove(removeIndex);
		operator.addCarMove(removeIndex, ejectionSwapMutation.getCarMoveReplace());

		//Calculate fitness
		double deltaFitness = calculateDeltaFitness(operator, operatorState);

		//Reset operator
		resetOperator(operatorState);

		return deltaFitness;

	}

	public double deltaFitness(IntraSwap intraSwap){

		Operator operator = intraSwap.getOperator1();
		int index1 = intraSwap.getIndex1();
		int index2 = intraSwap.getIndex2();

		// Save old state
		OperatorState operatorState = setOperatorState(operator);

		//Do mutation
		CarMove carMove1 = operator.removeCarMove(index1);
		operator.addCarMove(index2, carMove1);
		CarMove carMove2 = operator.removeCarMove(index2 - 1);
		operator.addCarMove(index1, carMove2);

		// Calculate fitness
		double deltaFitness = calculateDeltaFitness(operator, operatorState);

		//Revert
		resetOperator(operatorState);

		return deltaFitness;


	}


	//================================================================================
	// Perform mutation
	//================================================================================


	public void performMutation(IntraMove intraMove) {
		Operator operator = intraMove.getOperator();
		int removeIndex = intraMove.getRemoveIndex();
		int insertIndex = intraMove.getInsertIndex();
		CarMove carMove = operator.removeCarMove(removeIndex);
		
		operator.addCarMove(insertIndex, carMove);
		operator.getFitness();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
		this.prevDeviationFromIdealState = new HashMap<>(this.deviationFromIdealState);
	}

	public void performMutation(InterMove interMove){
		Operator operatorRemove = interMove.getOperatorRemove();
		Operator operatorInsert = interMove.getOperatorInsert();
		int removeIndex 	   	   = interMove.getInsertIndex();
		int insertIndex 		   = interMove.getInsertIndex();
		CarMove carMove         = operatorRemove.removeCarMove(removeIndex);
		
		operatorInsert.addCarMove(insertIndex, carMove);
		operatorRemove.getFitness();
		operatorInsert.getFitness();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
		this.prevDeviationFromIdealState = new HashMap<>(this.deviationFromIdealState);
	}
	
	public void performMutation(InterSwap2 interSwap2) {
		Operator operator1 = interSwap2.getOperator1();
		Operator operator2 = interSwap2.getOperator2();
		int index1         = interSwap2.getIndex1();
		int index2         = interSwap2.getIndex2();
		CarMove carMove1   = operator1.removeCarMove(index1);
		CarMove carMove2   = operator2.removeCarMove(index2);
		
		operator1.addCarMove(index1, carMove2);
		operator2.addCarMove(index2, carMove1);
		operator1.getFitness();
		operator2.getFitness();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
		this.prevDeviationFromIdealState = new HashMap<>(this.deviationFromIdealState);
	}

	public void performMutation(Inter2Move inter2move){
		Operator operator1 = inter2move.getOperatorRemove();
		Operator operator2 = inter2move.getOperatorInsert();
		int index1 = inter2move.getRemoveIndex1();
		int index2 = inter2move.getRemoveIndex2();
		//index2 = index2 > index1 ? index2--: index2;

		// Do mutation
		CarMove carMove1 = operator1.removeCarMove(index1);
		CarMove carMove2 = operator1.removeCarMove(index1);
		operator2.addCarMove(inter2move.getInsertIndex(), carMove1);
		operator2.addCarMove(inter2move.getInsertIndex() + 1, carMove2);

		operator1.getFitness();
		operator2.getFitness();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
		this.prevDeviationFromIdealState = new HashMap<>(this.deviationFromIdealState);

	}
	
	public void performMutation(EjectionReplaceMutation ejectionReplaceMutation){
		Operator operator      = ejectionReplaceMutation.getOperator();
		int removeIndex        = ejectionReplaceMutation.getCarMoveIndex();
		
		CarMove carMoveRemoved = operator.removeCarMove(removeIndex);
		operator.addCarMove(removeIndex, ejectionReplaceMutation.getCarMoveReplace());
		this.unusedCarMoves.get(carMoveRemoved.getCar()).remove(ejectionReplaceMutation.getCarMoveReplace());
		this.unusedCarMoves.get(carMoveRemoved.getCar()).add(carMoveRemoved);
		operator.getFitness();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
		this.prevDeviationFromIdealState = new HashMap<>(this.deviationFromIdealState);
	}

	public void performMutation(EjectionInsertMutation ejectionInsertMutation){
		Operator operator = ejectionInsertMutation.getOperator();
		int addIndex      = ejectionInsertMutation.getCarMoveIndex();
		
		operator.addCarMove(addIndex, ejectionInsertMutation.getCarMoveReplace());
		this.unusedCarMoves.get(ejectionInsertMutation.getCarMoveReplace().getCar()).remove(ejectionInsertMutation.getCarMoveReplace());
		operator.getFitness();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
		this.prevDeviationFromIdealState = new HashMap<>(this.deviationFromIdealState);
		carsNotInUse.remove(ejectionInsertMutation.getCarMoveReplace().getCar());
	}

	public void performMutation(EjectionRemoveMutation ejectionRemoveMutation){
		Operator operator = ejectionRemoveMutation.getOperator();
		int removeIndex   = ejectionRemoveMutation.getCarMoveIndex();
		
		CarMove carMove = operator.removeCarMove(removeIndex);
		this.unusedCarMoves.get(carMove.getCar()).add(carMove);
		operator.getFitness();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
		this.prevDeviationFromIdealState = new HashMap<>(this.deviationFromIdealState);
		carsNotInUse.add(carMove.getCar());

	}

	public void performMutation(EjectionSwapMutation ejectionSwapMutation){
		Operator operator = ejectionSwapMutation.getOperator();
		int removeIndex = ejectionSwapMutation.getCarMoveIndex();

		CarMove carMove = operator.removeCarMove(removeIndex);
		this.unusedCarMoves.get(carMove.getCar()).add(carMove);
		operator.addCarMove(removeIndex, ejectionSwapMutation.getCarMoveReplace());
		this.unusedCarMoves.get(ejectionSwapMutation.getCarMoveReplace().getCar()).remove(ejectionSwapMutation.getCarMoveReplace());
		operator.getFitness();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
		this.prevDeviationFromIdealState = new HashMap<>(this.deviationFromIdealState);
		carsNotInUse.add(carMove.getCar());
		carsNotInUse.remove(ejectionSwapMutation.getCarMoveReplace().getCar());

	}

	public void performMutation(IntraSwap intraSwap){
		Operator operator = intraSwap.getOperator1();
		int index1 = intraSwap.getIndex1();
		int index2 = intraSwap.getIndex2();

		CarMove carMove1 = operator.removeCarMove(index1);
		operator.addCarMove(index2, carMove1);
		CarMove carMove2 = operator.removeCarMove(index2 - 1);
		operator.addCarMove(index1, carMove2);
		operator.getFitness();
		this.prevCapacitiesUsed = new HashMap<>(this.capacitiesUsed);
		this.prevDeviationFromIdealState = new HashMap<>(this.deviationFromIdealState);
	}

	//================================================================================
	// Generate neighborhood
	//================================================================================
	
	// FUll
	public HashMap<Mutation, Integer> generateFullNeighborhood(TabuList tabuList) {
		HashMap<Mutation, Integer> neighborhood = new HashMap<>();
		
		for(int o = 0; o < operators.size(); o++) {
			Operator operator = (Operator) operators.get(o);
			// IntraMove && EjectionRemove
			for(int i = 0; i < operator.getCarMoveListSize(); i++) {
				for(int j = 0; j < operator.getCarMoveListSize(); j++) {
					if(i == j) {
						continue;
					}
					IntraMove intraMove = new IntraMove(operator, i, j);
					if(!tabuList.isTabu(intraMove)) {
						neighborhood.put(intraMove, 1);
					}
				}
				
				EjectionRemoveMutation ejectionRemoveMutation = new EjectionRemoveMutation(operator, i, operator.getCarMove(i));
				if(!tabuList.isTabu(ejectionRemoveMutation)) {
					neighborhood.put(ejectionRemoveMutation, 1);
				}


				// Ejection Replace
				CarMove carMove = operator.getCarMove(i);
				for(CarMove replaceCarMove : unusedCarMoves.get(carMove.getCar())) {
					EjectionReplaceMutation ejectionReplaceMutation = new EjectionReplaceMutation(operator, i, replaceCarMove, carMove);
					if(!tabuList.isTabu(ejectionReplaceMutation)) {
						neighborhood.put(ejectionReplaceMutation, 1);
					}
				}
				//Ejection Swap
				for(Car car: carsNotInUse){
					for(CarMove insertCarMove: unusedCarMoves.get(car)){
						EjectionSwapMutation ejectionSwapMutation = new EjectionSwapMutation(operator, i, insertCarMove);
						if(!tabuList.isTabu(ejectionSwapMutation)){
							neighborhood.put(ejectionSwapMutation,1);
						}
					}
				}
			}

			// Ejection Insert
			for (int i = 0; i < operator.getCarMoveListSize() +1; i++) {
				for(Car car : carsNotInUse) {
					for(CarMove carMove:  unusedCarMoves.get(car)) {
						EjectionInsertMutation ejectionInsertMutation =  new EjectionInsertMutation(operator, i, carMove);
						if(!tabuList.isTabu(ejectionInsertMutation)) {
							neighborhood.put(ejectionInsertMutation, 1);
						}
					}
				}
			}

			// InterMove
			for(int p = 0; p < operators.size(); p++) {
				if(p == o) {
					continue;
				}
				Operator operator2 = (Operator) operators.get(p);
				for(int i = 0; i < operator.getCarMoveListSize(); i++) {
					for(int j = 0; j < operator2.getCarMoveListSize()+1; j++) {
						InterMove interMove =  new InterMove(operator, i, operator2, j);
						if(!tabuList.isTabu(interMove)) {
							neighborhood.put(interMove, 1);
						}
					}
				}
			}
			
			//  InterSwap2
			for(int p = o+1; p < this.operators.size(); p++) {
				Operator operator2 = (Operator) operators.get(p);
				for(int i = 0; i < operator.getCarMoveListSize(); i++) {
					for(int j = 0; j < operator2.getCarMoveListSize(); j++) {
						InterSwap2 interSwap2 = new InterSwap2(i, j, operator, operator2);
						if(!tabuList.isTabu(interSwap2)) {
							neighborhood.put(interSwap2, 1);
						}
					}
				}
			}
		}
		
		return neighborhood;
	}


	// Random
	public HashMap<Mutation, Integer> getNeighborhoodInterMove(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int i = 0; i < size; i++) {
			int removeOperatorIndex = (int)Math.floor(Math.random() * operators.size());
			Operator removeOperator = (Operator) operators.get(removeOperatorIndex);
			int insertOperatorIndex = MathHelper.getRandomIntNotEqual(removeOperatorIndex, operators.size());
			Operator insertOperator = (Operator) operators.get(insertOperatorIndex);
			if(removeOperator.getCarMoveListSize() == 0) {
				continue;
			}
			int removeIndex = (int)Math.floor(Math.random() * removeOperator.getCarMoveListSize());
			int insertIndex = (int)Math.floor(Math.random() * (insertOperator.getCarMoveListSize() + 1));
			InterMove interMove = new InterMove(removeOperator,removeIndex, insertOperator, insertIndex);
			if(!tabuList.isTabu(interMove)) {
				neighbors.put(interMove, 1);
			}
		}
		
		return neighbors;
	}
	
	public HashMap<Mutation, Integer> getNeighborhoodIntraMove(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int i = 0; i < size; i++) {
			int randomOperatorIndex = (int)Math.floor(Math.random() * operators.size());
			Operator operator = (Operator) operators.get(randomOperatorIndex);
			if(operator.getCarMoveListSize() <= 1) {
				continue;
			}
			int removeIndex = (int)Math.floor(Math.random() * operator.getCarMoveListSize());
			int insertIndex = MathHelper.getRandomIntNotEqual(removeIndex, operator.getCarMoveListSize());
			IntraMove intraMove = new IntraMove(operator,removeIndex, insertIndex);
			if(!tabuList.isTabu(intraMove)) {
				neighbors.put(intraMove,1);
			}
		}
		
		return neighbors;
	}
	
	public HashMap<Mutation, Integer> getNeighborhoodInterSwap2(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int i = 0; i < size; i++) {
			int operator1Index = (int) Math.floor(Math.random() * operators.size());
			int operator2Index = MathHelper.getRandomIntNotEqual(operator1Index, operators.size());
			Operator operator1 = (Operator) operators.get(operator1Index);
			Operator operator2 = (Operator) operators.get(operator2Index);
			if(operator1.getCarMoveListSize() == 0 || operator2.getCarMoveListSize() == 0) {
				continue;
			}
			int index1 = (int)Math.floor(Math.random() * operator1.getCarMoveListSize());
			int index2 = (int)Math.floor(Math.random() * operator2.getCarMoveListSize());
			InterSwap2 interSwap2 = new InterSwap2(index1, index2, operator1, operator2);
			if(!tabuList.isTabu(interSwap2)) {
				neighbors.put(interSwap2, 1);
			}
		}
		
		return neighbors;
	}

	public HashMap<Mutation, Integer> getNeighborhoodInter2Move(TabuList tabuList, int size){
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for (int i = 0; i < size; i++) {
			int operator1Index = (int) Math.floor(Math.random() * operators.size());
			int operator2Index = MathHelper.getRandomIntNotEqual(operator1Index, operators.size());
			Operator operator1 = (Operator) operators.get(operator1Index);
			Operator operator2 = (Operator) operators.get(operator2Index);
			if(operator1.getCarMoveListSize() <= 1) {
				continue;
			}
			int index1 = (int)Math.floor(Math.random() * (operator1.getCarMoveListSize() -1));
			int index2 = index1 + 1;
			int index3 = (int)Math.floor(Math.random() * operator2.getCarMoveListSize());
			Inter2Move inter2move = new Inter2Move(operator1, index1, index2, operator2, index3);
			if(!tabuList.isTabu(inter2move)){
				neighbors.put(inter2move, 1);
			}
		}
		return neighbors;
	}
	
	public HashMap<Mutation, Integer> getNeighborhoodEjectionInsert(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int i = 0; i < size; i++) {
			int removeOperatorIndex = (int)Math.floor(Math.random() * operators.size());
			Operator removeOperator = (Operator) operators.get(removeOperatorIndex);
			int insertIndex = (int)Math.floor(Math.random() * (removeOperator.getCarMoveListSize()+1));

			if(this.carsNotInUse.size() == 0){
				break;
			}
			int carSwapIndex = (int)Math.floor(Math.random()* this.carsNotInUse.size());
			ArrayList<Car> keysAsArray = new ArrayList<>(carsNotInUse);
			Car car = keysAsArray.get(carSwapIndex);
			int swapIndex = (int)Math.floor(Math.random() * this.unusedCarMoves.get(car).size());
			CarMove insertCarMove = this.unusedCarMoves.get(car).get(swapIndex);
			EjectionInsertMutation ejectionInsertMutation = new EjectionInsertMutation(removeOperator, insertIndex, insertCarMove);
			if(!tabuList.isTabu(ejectionInsertMutation)) {
				neighbors.put(ejectionInsertMutation, 1);
			}
		}
		
		return neighbors;
	}
	
	public HashMap<Mutation, Integer> getNeighborhoodEjectionRemove(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int i = 0; i < size; i++) {
			int removeOperatorIndex = (int)Math.floor(Math.random() * operators.size());
			Operator removeOperator = (Operator) operators.get(removeOperatorIndex);
			if(removeOperator.getCarMoveListSize() == 0){
				continue;
			}
			int removeIndex = (int)Math.floor(Math.random() * removeOperator.getCarMoveListSize());
			EjectionRemoveMutation ejectionRemoveMutation = new EjectionRemoveMutation(removeOperator, removeIndex, removeOperator.getCarMove(removeIndex));
			if(!tabuList.isTabu(ejectionRemoveMutation)) {
				neighbors.put(ejectionRemoveMutation, 1);
			}
		}
		return neighbors;
	}
	
	public HashMap<Mutation, Integer> getNeighborhoodEjectionReplace(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int i = 0; i < size; i++) {
			int removeOperatorIndex = (int)Math.floor(Math.random() * operators.size());
			Operator removeOperator = (Operator) operators.get(removeOperatorIndex);
			if(removeOperator.getCarMoveListSize() == 0){
				continue;
			}
			int insertIndex = (int)Math.floor(Math.random() * removeOperator.getCarMoveListSize());
			if(this.unusedCarMoves.get(removeOperator.getCarMove(insertIndex).getCar()).size() == 0){
				continue;
			}
			int swapIndex = (int)Math.floor(Math.random() * this.unusedCarMoves.get(removeOperator.getCarMove(insertIndex).getCar()).size());
			CarMove swapCarMove = this.unusedCarMoves.get(removeOperator.getCarMove(insertIndex).getCar()).get(swapIndex);
			EjectionReplaceMutation ejectionReplaceMutation = new EjectionReplaceMutation(removeOperator, insertIndex, swapCarMove, removeOperator.getCarMove(insertIndex));
			if(!tabuList.isTabu(ejectionReplaceMutation)) {
				neighbors.put(ejectionReplaceMutation, 1);
			}
		}
		
		return neighbors;
	}

	public HashMap<Mutation, Integer> getNeighborhoodEjectionSwap(TabuList tabuList, int size){
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for (int i = 0; i < size ; i++) {
			int removeOperatorIndex = (int)Math.floor(Math.random() * operators.size());
			Operator removeOperator = (Operator) operators.get(removeOperatorIndex);
			if(removeOperator.getCarMoveListSize() == 0){
				continue;
			}
			int insertIndex = (int)Math.floor(Math.random() * removeOperator.getCarMoveListSize());
			if(this.carsNotInUse.size() == 0){
				break;
			}
			int carSwapIndex = (int)Math.floor(Math.random()* this.carsNotInUse.size());
			ArrayList<Car> keysAsArray = new ArrayList<Car>(carsNotInUse);
			Car carSwap = keysAsArray.get(carSwapIndex);
			int carMoveSwapIndex = (int)Math.floor(Math.random()* this.unusedCarMoves.get(carSwap).size());
			CarMove carMoveReplace = unusedCarMoves.get(carSwap).get(carMoveSwapIndex);
			EjectionSwapMutation ejectionSwapMutation = new EjectionSwapMutation(removeOperator, insertIndex, carMoveReplace);
			if(!tabuList.isTabu(ejectionSwapMutation)){
				neighbors.put(ejectionSwapMutation, 1);
			}
		}
		return neighbors;

	}

	public HashMap<Mutation, Integer> getNeighborhoodIntraSwap(TabuList tabuList, int size){
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int i = 0; i < size; i++) {
			int operator1Index = (int) Math.floor(Math.random() * operators.size());
			Operator operator1 = (Operator) operators.get(operator1Index);
			if(operator1.getCarMoveListSize() == 0){
				continue;
			}
			int index1 = (int)Math.floor(Math.random() * (operator1.getCarMoveListSize()-1));
			int index2 = MathHelper.getRandomGreater(index1, operator1.getCarMoveListSize());
			IntraSwap intraSwap = new IntraSwap(index1, index2, operator1);
			if(!tabuList.isTabu(intraSwap)){
				neighbors.put(intraSwap, 1);
			}
		}
		return neighbors;
	}

	//================================================================================
	// Generate full neighborhood
	//================================================================================


	public HashMap<Mutation, Integer> getNeighborhoodIntraMoveFull(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int o = 0; o < operators.size(); o++) {
			Operator operator = (Operator) operators.get(o);
			// IntraMove
			for(int i = 0; i < operator.getCarMoveListSize(); i++) {
				for(int j = 0; j < operator.getCarMoveListSize(); j++) {
					if(i == j){
						continue;
					}
					IntraMove intraMove = new IntraMove(operator, i, j);
					if(!tabuList.isTabu(intraMove)) {
						neighbors .put(intraMove, 1);
					}
				}
			}
		}
		return neighbors;
	}

	public HashMap<Mutation, Integer> getNeighborhoodInterMoveFull(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int o = 0; o < operators.size(); o++) {
			Operator operator = (Operator) operators.get(o);
			for(int p = 0; p < operators.size(); p++) {
				if(p == o) {
					continue;
				}
				Operator operator2 = (Operator) operators.get(p);
				for(int i = 0; i < operator.getCarMoveListSize(); i++) {
					for(int j = 0; j < operator2.getCarMoveListSize() + 1; j++) {
						InterMove interMove =  new InterMove(operator, i, operator2, j);
						if(!tabuList.isTabu(interMove)) {
							neighbors.put(interMove, 1);
						}
					}
				}
			}
		}
		return neighbors;
	}

	public HashMap<Mutation, Integer> getNeighborhoodInterSwap2Full(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int o = 0; o < operators.size(); o++) {
			Operator operator = (Operator) operators.get(o);
			for(int p = o+1; p < this.operators.size(); p++) {
				Operator operator2 = (Operator) operators.get(p);
				for(int i = 0; i < operator.getCarMoveListSize(); i++) {
					for(int j = 0; j < operator2.getCarMoveListSize(); j++) {
						InterSwap2 interSwap2 = new InterSwap2(i, j, operator, operator2);
						if(!tabuList.isTabu(interSwap2)) {
							neighbors.put(interSwap2, 1);
						}
					}
				}
			}
		}
		return neighbors;
	}

	public HashMap<Mutation, Integer> getNeighborhoodEjectionInsertFull(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		if(this.carsNotInUse.size() == 0){
			return neighbors;
		}
		ArrayList<Car> carsNotInSolution = new ArrayList<Car>(carsNotInUse);
		for(int o = 0; o < operators.size(); o++) {
			Operator operator = (Operator) operators.get(o);
			for (int i = 0; i < operator.getCarMoveListSize() + 1; i++) {
				for (int c = 0; c < carsNotInSolution.size(); c++) {
					Car car = carsNotInSolution.get(c);
					for (int j = 0; j < this.unusedCarMoves.get(car).size(); j++) {
						CarMove insertCarMove = this.unusedCarMoves.get(car).get(j);
						EjectionInsertMutation ejectionInsertMutation = new EjectionInsertMutation(operator, i, insertCarMove);
						if(!tabuList.isTabu(ejectionInsertMutation)) {
							neighbors.put(ejectionInsertMutation, 1);
						}
					}
				}
			}
		}
		return neighbors;

	}

	public HashMap<Mutation, Integer> getNeighborhoodEjectionRemoveFull(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int o = 0; o < operators.size(); o++) {
			Operator operator = (Operator) operators.get(o);
			for (int i = 0; i < operator.getCarMoveListSize(); i++) {
				EjectionRemoveMutation ejectionRemoveMutation = new EjectionRemoveMutation(operator, i, operator.getCarMove(i));
				if(!tabuList.isTabu(ejectionRemoveMutation)) {
					neighbors.put(ejectionRemoveMutation, 1);
				}
			}
		}
		return neighbors;
	}

	public HashMap<Mutation, Integer> getNeighborhoodEjectionReplaceFull(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int o = 0; o < operators.size(); o++) {
			Operator operator = (Operator) operators.get(o);
			for (int i = 0; i < operator.getCarMoveListSize(); i++) {
				for (int c = 0; c < this.unusedCarMoves.get(operator.getCarMove(i).getCar()).size(); c++) {
					CarMove swapCarMove = this.unusedCarMoves.get(operator.getCarMove(i).getCar()).get(c);
					EjectionReplaceMutation ejectionReplaceMutation = new EjectionReplaceMutation(operator, i, swapCarMove, operator.getCarMove(i));
					if(!tabuList.isTabu(ejectionReplaceMutation)) {
						neighbors.put(ejectionReplaceMutation, 1);
					}
				}
			}
		}
		return neighbors;
	}

	public HashMap<Mutation, Integer> getNeighborhoodEjectionSwapFull(TabuList tabuList, int size){
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		if(this.carsNotInUse.size() == 0){
			return neighbors;
		}
		ArrayList<Car> carsNotInSolution = new ArrayList<>(carsNotInUse);
		for(int o = 0; o < operators.size(); o++) {
			Operator operator = (Operator) operators.get(o);
			for (int i = 0; i < operator.getCarMoveListSize(); i++) {
				for (int c = 0; c < carsNotInSolution.size(); c++) {
					Car car = carsNotInSolution.get(c);
					for (int j = 0; j < this.unusedCarMoves.get(car).size(); j++) {
						CarMove carMoveReplace = unusedCarMoves.get(car).get(j);
						EjectionSwapMutation ejectionSwapMutation = new EjectionSwapMutation(operator, i, carMoveReplace);
						if(!tabuList.isTabu(ejectionSwapMutation)){
							neighbors.put(ejectionSwapMutation, 1);
						}
					}
				}
			}
		}
		return neighbors;

	}

	public HashMap<Mutation, Integer> getNeighborhoodInter2MoveFull(TabuList tabuList, int size){
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int o = 0; o < operators.size(); o++) {
			Operator operator = (Operator) operators.get(o);
			for(int p = 0; p < operators.size(); p++) {
				if(p == o) {
					continue;
				}
				Operator operator2 = (Operator) operators.get(p);
				for(int i = 0; i < operator.getCarMoveListSize() - 1; i++) {
					for(int j = 0; j < operator2.getCarMoveListSize() + 1; j++) {
						Inter2Move inter2move = new Inter2Move(operator, i, i+1, operator2, j);
						if(!tabuList.isTabu(inter2move)){
							neighbors.put(inter2move, 1);
						}
					}
				}
			}
		}
		return neighbors;
	}

	public HashMap<Mutation, Integer> getNeighborhoodIntraSwapFull(TabuList tabuList, int size) {
		HashMap<Mutation, Integer> neighbors = new HashMap<>();
		for(int o = 0; o < operators.size(); o++) {
			Operator operator = (Operator) operators.get(o);
			for(int i = 0; i < operator.getCarMoveListSize() - 1; i++) {
				for(int j = i+1; j < operator.getCarMoveListSize(); j++) {
					IntraSwap intraSwap = new IntraSwap(i, j, operator);
					if(!tabuList.isTabu(intraSwap)) {
						neighbors.put(intraSwap, 1);
					}
				}
			}
		}
		return neighbors;
	}




	//================================================================================
	// Destroy
	//================================================================================

	public void destroy(RandomDestroy randomDestroy, TabuList tabuList, int numberToHandle){
		for (int i = 0; i < numberToHandle; i++) {
			ArrayList<Mutation> neighborhood = new ArrayList<>(getNeighborhoodEjectionRemove(tabuList,
					HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE * 3).keySet());
			Mutation candidate;
			if (neighborhood.size() < 1) {
				break;
			}
			int removeIndex = (int) Math.floor(Math.random() * neighborhood.size());
			candidate = neighborhood.get(removeIndex);
			performMutation((EjectionRemoveMutation) candidate);
		}
	}

	public void destroy(RelatedDestroy relatedDestroy, TabuList tabuList, int numberToHandle){
		ArrayList<Mutation> neighborhood = new ArrayList<>(getNeighborhoodEjectionRemove(tabuList,
				HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE * 3).keySet());
		if(neighborhood.size() == 0){
			return;
		}
		// Start with choosing one car move
		ArrayList<CarMove> carMovesDeleted = new ArrayList<>();
		int removeIndex = (int) Math.floor(Math.random() * neighborhood.size());
		EjectionRemoveMutation mut = ((EjectionRemoveMutation)neighborhood.get(removeIndex));
		int index = mut.getCarMoveIndex();
		Operator operator = mut.getOperator();
		CarMove carMoveToRemove = operator.getCarMove(index);
		carMovesDeleted.add(carMoveToRemove);
		performMutation(mut);

		CarMove carMoveChosen;
		Operator operator1;
		for (int i = 0; i < numberToHandle; i++) {
			CarMove mostSimilarCarMove = null;
			Operator mostSimilarOperator = null;
			double lowestSimilarity = Double.MAX_VALUE;                           
			int mostSimilarIndex = 0;                                             
			removeIndex = (int) Math.floor(Math.random() * carMovesDeleted.size());
			carMoveChosen = carMovesDeleted.get(removeIndex);

			// Find most similar car move
			for(Object obj : this.operators){
				operator1 = (Operator) obj;
				for (int j = 0; j < operator1.getCarMoveListSize(); j++) {
					CarMove checkCarMove = operator1.getCarMove(j);
					double simMeasure = RelatednessMeasure.relatedsessMeasure(carMoveChosen,checkCarMove);
					if(simMeasure < lowestSimilarity){
						mostSimilarIndex = j + 0;
						mostSimilarCarMove = checkCarMove;
						mostSimilarOperator = operator1;
						lowestSimilarity = simMeasure + 0;
					}
				}
			}
			if(mostSimilarCarMove == null){
				return;
			}
			performMutation(new EjectionRemoveMutation(mostSimilarOperator,mostSimilarIndex,mostSimilarCarMove));
		}
	}

	public void destroy(WorstDestroy bestDestroy,  TabuList tabuList, int numberToHandle){
		for (int i = 0; i < numberToHandle; i++) {
			//ArrayList<Mutation> neighborhood = new ArrayList<>(getNeighborhoodEjectionRemove(tabuList,
					//HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE * 3).keySet());
			ArrayList<Mutation> neighborhood = new ArrayList<>(getNeighborhoodEjectionRemoveFull(tabuList,
					HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE).keySet());
					Mutation candidate;
			if (neighborhood.size() < 1) {
				break;
			}
			Mutation bestMutation = null;
			double lowestDeltaFitness = Double.MAX_VALUE;
			for(Mutation mutation : neighborhood){
				double delta = deltaFitness((EjectionRemoveMutation) mutation);
				if (delta < lowestDeltaFitness){
					bestMutation = mutation;
					lowestDeltaFitness = delta;
				}
			}
			if(bestMutation != null){
				performMutation((EjectionRemoveMutation) bestMutation);
			}

		}                                                                                             	
	}

	//================================================================================
	// Repair
	//================================================================================

	public void repair(BestRepair bestRepair, TabuList tabuList , int numberToHandle){
		for (int i = 0; i < numberToHandle; i++) {
			ArrayList<Mutation> neighborhood = new ArrayList<>(getNeighborhoodEjectionInsertFull(tabuList,
					HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE).keySet());
			Mutation candidate = null;
			if(neighborhood.size() < 1){
				break;
			}
			candidate = neighborhood.get(0);
			double candidateDelta;
			candidateDelta = deltaFitness((EjectionInsertMutation) candidate);
			for(Mutation newCandidate : neighborhood) {
				double newCandidateDelta = deltaFitness((EjectionInsertMutation) newCandidate);
				if (newCandidateDelta < candidateDelta ) {
					candidate = newCandidate;
					candidateDelta = newCandidateDelta;
				}
			}
			addToFitness(candidateDelta);
			performMutation((EjectionInsertMutation) candidate);
		}
	}

	public void repair(RegretRepair regretRepair, TabuList tabuList, int numberToHandle){
		// Checks the sum of the best and the second best place to put the car move
		for (int i = 0; i < numberToHandle; i++) {
			EjectionInsertMutation bestEjectionInsert = null;
			double bestRegretValue = -1;
			double bestRegretsDeltaFit = 0;
			for(Car car : carsNotInUse){
				for(CarMove carMove : unusedCarMoves.get(car)){
					double bestDelta = Double.MAX_VALUE/2;
					double secondBestDelta = Double.MAX_VALUE;
					EjectionInsertMutation candidate = null;
					for(Object obj : operators){
						Operator operator = (Operator)(obj);
						for (int j = 0; j < operator.getCarMoveListSize() + 1; j++) {
							EjectionInsertMutation insertMutation = new EjectionInsertMutation(operator, j, carMove);
							double deltaFit = deltaFitness(insertMutation);
							if(deltaFit < bestDelta){
								secondBestDelta = bestDelta + 0;
								bestDelta = deltaFit;
								candidate = insertMutation;
							} else if(deltaFit < secondBestDelta && deltaFit >= bestDelta){
								secondBestDelta = deltaFit;
							}
						}
					}
					double regretValue = secondBestDelta - bestDelta;
					if(regretValue > bestRegretValue){
						bestEjectionInsert = candidate;
						bestRegretsDeltaFit = bestDelta;
						bestRegretValue = regretValue;
					}
				}
			}

			if(bestEjectionInsert == null){
				return;
			}
			addToFitness(bestRegretsDeltaFit);
			performMutation(bestEjectionInsert);
		}
	}

	public void repair(RegretRepair2 regretRepair2, TabuList tabuList, int numberToHandle){
		for (int i = 0; i < numberToHandle; i++) {
			EjectionInsertMutation bestEjectionInsert = null;
			double bestRegretValue = 0;
			double bestRegretsDeltaFit = 0;
			for(Car car : carsNotInUse){
				for(CarMove carMove : unusedCarMoves.get(car)){
					double bestDelta = Double.MAX_VALUE/3;
					double secondBestDelta = Double.MAX_VALUE/2;
					double thirdBestDelta = Double.MAX_VALUE;
					EjectionInsertMutation candidate = null;
					for(Object obj : operators){
						Operator operator = (Operator)(obj);
						for (int j = 0; j < operator.getCarMoveListSize() + 1; j++) {
							EjectionInsertMutation insertMutation = new EjectionInsertMutation(operator, j, carMove);
							double deltaFit = deltaFitness(insertMutation);
							if(deltaFit < bestDelta){
								thirdBestDelta = secondBestDelta + 0;
								secondBestDelta = bestDelta + 0;
								bestDelta = deltaFit;
								candidate = insertMutation;
							} else if(deltaFit < secondBestDelta && deltaFit >= bestDelta){
								thirdBestDelta = secondBestDelta + 0;
								secondBestDelta = deltaFit;
							} else if(deltaFit < thirdBestDelta && deltaFit >= secondBestDelta){
								thirdBestDelta = deltaFit + 0;
							}
						}
					}
					double regretValue = thirdBestDelta + secondBestDelta - 2*bestDelta;
					if(regretValue > bestRegretValue){
						bestEjectionInsert = candidate;
						bestRegretsDeltaFit = bestDelta;
						bestRegretValue = regretValue;
					}
				}
			}

			if(bestEjectionInsert == null){
				return;
			}
			addToFitness(bestRegretsDeltaFit);
			performMutation(bestEjectionInsert);
		}
	}

	//================================================================================
	// Getters and setters
	//================================================================================	

	public ArrayList<Object> getRepresentation() {
		return operators;
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
	
	public HashMap<ParkingNode, Integer> getDeviationIdealState() {
		return this.deviationFromIdealState;
	}
	
	public HashMap<ParkingNode, Integer> getInitialDeviationFromIdealState() {
		return this.initialDeviationFromIdealState;
	}
	
	public HashMap<ParkingNode, Integer> getPrevDeviationFromIdealState() {
		return this.prevDeviationFromIdealState;
	}
	
	public void addToFitness(double delta) {
		this.fitness += delta;
	}
	
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public int getTotalNumberOfCarMoves(){
		int numberOfMoves = 0;
		for(Object op: this.operators){
			Operator operator = (Operator) op;
			numberOfMoves += operator.getCarMoveListSize();
		}
		return numberOfMoves;
	}
	
	@Override
	public String toString() {
		String s = "";
		for(Object i : this.getRepresentation()) {
			s += i.toString() + "\n";
		}
		return s;
	}

	
	
	private class OperatorState {
		Operator operator;
		HashMap<ChargingNode, Integer> oldChargingCapacityUsedOperator;
		HashMap<ParkingNode, Integer> oldMovesToParkingNodeByOperator;
		ArrayList<CarMove> oldCarMoves;
		double oldFitness;
		
		OperatorState(Operator operator) {
			this.operator = operator;
			this.oldChargingCapacityUsedOperator = new HashMap<>(operator.getChargingCapacityUsedOperator());
			this.oldMovesToParkingNodeByOperator = new HashMap<>(operator.getMovesToParkingNodeByOperator());
			this.oldCarMoves = operator.getCarMoveCopy();
			this.oldFitness = operator.getFitness(); 
		}
		
		void resetState() {
			this.operator.setCarMoves(oldCarMoves);
			this.operator.setChargingCapacityUsedByOperator(oldChargingCapacityUsedOperator);
			this.operator.setFitness(oldFitness);
			this.operator.setMovesToParkingNodeByOperator(oldMovesToParkingNodeByOperator);
			this.operator.setChanged(false);
		}
	}
	
	private void resetOperators(ArrayList<OperatorState> operatorStates) {
		for(OperatorState operatorState : operatorStates) {
			operatorState.resetState();
		}
		this.capacitiesUsed = new HashMap<>(this.prevCapacitiesUsed);
		this.deviationFromIdealState = new HashMap<>(this.prevDeviationFromIdealState);
	}
	
	private void resetOperator(OperatorState operatorState) {
		operatorState.resetState();
		this.capacitiesUsed = new HashMap<>(this.prevCapacitiesUsed);
		this.deviationFromIdealState = new HashMap<>(this.prevDeviationFromIdealState);
	}
	
	private ArrayList<OperatorState> setOperatorStates(ArrayList<Operator> operators) {
		ArrayList<OperatorState> operatorState =  new ArrayList<>();
		for(Operator operator : operators) {
			operatorState.add(new OperatorState(operator));
		}
		
		return operatorState;
	}
	
	private OperatorState setOperatorState(Operator operator) {
		return new OperatorState(operator);
	}
	
	private double calculateDeltaFitness(ArrayList<Operator> operators, ArrayList<OperatorState> operatorStates) {
		double delta = 0.0;
		for(int i = 0; i < operators.size(); i++) {
			delta += operators.get(i).getFitness() - operatorStates.get(i).oldFitness;
		}
		
		return delta + calculateDeltaCapacityFitness() + calculateDeltaIdealStateFitness();
	}
	
	private double calculateDeltaFitness(Operator operator, OperatorState operatorState) {
		return operator.getFitness() - operatorState.oldFitness + calculateDeltaCapacityFitness() 
			+ calculateDeltaIdealStateFitness();
	}

	
	public void calculateMoselFitness(){
		int devIdeal = 0;
		for(ParkingNode parkingNode : deviationFromIdealState.keySet()){
			devIdeal += -Math.min(deviationFromIdealState.get(parkingNode),0) ;
		}
		this.deviationFromIdeal = devIdeal;

		int numberOfChargedCars = 0;
		for(ChargingNode chargingNode : capacitiesUsed.keySet()){
			if(capacitiesUsed.get(chargingNode) > chargingNode.getNumberOfAvailableChargingSpotsNextPeriod()) {
				numberOfChargedCars += chargingNode.getNumberOfAvailableChargingSpotsNextPeriod();
			} else {
				numberOfChargedCars += capacitiesUsed.get(chargingNode);
			}
		}
		
		int numberOfCarsToCharge = 0;
		for(ParkingNode parkingNode : problemInstance.getParkingNodes()){
			numberOfCarsToCharge += parkingNode.getCarsInNeed().size();
		}
		
		int numberPostponed = Math.max(numberOfCarsToCharge - numberOfChargedCars,0);
		this.numberOfUnchargedCars = numberPostponed;
	}

	public int getProblemSize(){
		return this.problemSize;
	}



	public ProblemInstance getProblemInstance() {
		return problemInstance;
	}
}
