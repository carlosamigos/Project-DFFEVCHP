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
import code.solver.heuristics.mutators.Mutation;
import code.solver.heuristics.mutators.Swap2;
import code.solver.heuristics.mutators.Swap3;

public class TSIndividual extends Individual {
	
	private ArrayList<Operator> operators;

	//These tracks how good the proposed solution is. Thus we might need two of them - one that is stable (final) and one that keeps track
	private HashMap<ChargingNode, Integer> capacities;
	private HashMap<ParkingNode, Integer> deviationFromIdealState;
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
		this.representation = new ArrayList<>();
		createOperators();
		initiateCapacities();
		initiateDeviations();
		this.carMoves = ChromosomeGenerator.generateCarMovesFrom(problemInstance);
		initializeOperators();
		calculateFitness();
	}

	private void createOperators(){
		this.operators = new ArrayList<>();
		for (int i = 0; i < problemInstance.getOperators().size(); i++) {
			Operator op = new Operator(problemInstance.getOperators().get(i).getTimeRemainingToCurrentNextNode(), Constants.TIME_LIMIT_STATIC_PROBLEM,
					problemInstance.getOperators().get(i).getNextOrCurrentNode(), problemInstance, capacities);
			operators.add(op);
		}

	}

	private void initiateCapacities(){
		this.capacities = new HashMap<>();
		for (int i = 0; i < problemInstance.getChargingNodes().size(); i++) {
			capacities.put(problemInstance.getChargingNodes().get(i), problemInstance.getChargingNodes().get(i).getNumberOfAvailableChargingSpotsNextPeriod());
		}
	}

	private void initiateDeviations(){
		this.deviationFromIdealState = new HashMap<>();
		for (int i = 0; i < problemInstance.getParkingNodes().size(); i++) {
			//Todo: Does not take into account cars that we know will arrive to the parking node during the planning period.
			int deviation = problemInstance.getParkingNodes().get(i).getCarsRegular().size() - problemInstance.getParkingNodes().get(i).getIdealNumberOfAvailableCars();
			deviationFromIdealState.put(problemInstance.getParkingNodes().get(i), deviation);
		}
	}

	//Choose the car moves that goes into the initial solution for each operator
	private void initializeOperators() {
		boolean operatorAvailable = true;
		HashMap<Car, ArrayList<CarMove>> carMovesCopy = ChromosomeGenerator.generateCarMovesFrom(problemInstance);
		while(operatorAvailable){
			operatorAvailable = false;
			for (Operator op: operators) {
				Node startNode = findPreviousNode(op);
				CarMove chosen = findnearestCarMove(startNode, carMovesCopy);
				if(chosen != null){
					if(timeAvailable(op, startNode, chosen)){
						operatorAvailable = true;
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

	private void updateDeviation(ParkingNode parkingNode){
		deviationFromIdealState.put(parkingNode, deviationFromIdealState.get(parkingNode) +1);
	}

	private void updateCapacity(ChargingNode chargingNode){
		capacities.put(chargingNode, capacities.get(chargingNode) - 1);
	}

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



	private double calculateDistanceCarMove(Node previousNode, CarMove carMove){
		return problemInstance.getTravelTimeBike(previousNode, carMove.getFromNode()) + carMove.getTravelTime();
	}

	
	private boolean timeAvailable(Operator op, Node previousNode, CarMove carMove){
		double addDistance = calculateDistanceCarMove(previousNode, carMove);
		return (op.getStartTime() + op.getTravelTime() + addDistance < op.getTimeLimit());
	}

	private CarMove findnearestCarMove(Node node, HashMap<Car, ArrayList<CarMove>> carMovesCopy){
		double distance = Integer.MAX_VALUE;
		double fitNess = Double.MAX_VALUE;
		CarMove cMove = null;
		for(Car car : carMovesCopy.keySet()){
			if(carMovesCopy.get(car).size() > 0){
				Node fromNode = carMovesCopy.get(car).get(0).getFromNode();
				double distanceCandidate = problemInstance.getTravelTimeBike(node, fromNode);
				//Accepting twice the distance
				if(distanceCandidate < distance*2){
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

	private double rateCarMove(CarMove carMove, double distance){
		double fitNess = 0;
		fitNess += (carMove.getTravelTime() + distance)* HeuristicsConstants.TABU_TRAVEL_COST;
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

	protected void calculateFitness() {
		for(Operator op : operators) {
		}
		
		this.fitness = this.awardForCharging + this.costOfTravel + this.costOfUnmetIdeal;
	}
	
	public double deltaFitness(Swap2 swap) {
		int i = swap.getI();
		int j = swap.getJ();
		double before =  Math.abs((int) this.representation.get(i) - i) +  Math.abs((int) this.representation.get(j) - j); 
		double after =  Math.abs((int) this.representation.get(i) - j) +  Math.abs((int) this.representation.get(j) - i); 
		return after - before;
	}
	
	public double deltaFitness(Swap3 swap) {
		return 0.0;
	}
	
	public void addToFitness(double delta) {
		this.fitness += delta;
	}
	
	public void performMutation(Mutation mutation) {
		mutation.doMutation(this);
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
		for(Object i : this.representation) {
			s += i.toString() + " ";
		}
		return s + "\n";
	}
}
