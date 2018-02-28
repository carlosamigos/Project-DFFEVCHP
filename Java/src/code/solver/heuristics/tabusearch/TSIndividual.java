package code.solver.heuristics.tabusearch;

import java.util.ArrayList;
import java.util.HashMap;

import code.problem.entities.Car;
import code.solver.heuristics.entities.CarMove;
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
	//private final int[] idealState;

	//TODO

	/*
	private Hashmap<Charging Station, Integer> capacities;
	priavte Hashmap<Nodes, Integer> deviationFromIdealState;
	 */

	private int[] currentState;
	private HashMap<Car, ArrayList<CarMove>> carMoves;
	private double costOfPostponed = 0.0;
	private double costOfTravel = 0.0;
	private double costOfUnmetIdeal = 0.0;
	
	public TSIndividual(ProblemInstance problemInstance) {

		this.representation = new ArrayList<>();
		this.operators = new ArrayList<>();
		for (int i = 0; i < problemInstance.getOperators().size(); i++) {
			Operator op = new Operator(problemInstance.getOperators().get(i).getTimeRemainingToCurrentNextNode());
			//operators.add(op);
			continue;
		}
		this.carMoves = ChromosomeGenerator.generateCarMovesFrom(problemInstance);
		initializeOperators();
		calculateFitness();

	}

	//Choose the car moves that goes into the initial solution for each operator
	private void initializeOperators() {
		boolean operatorAvailable = true;
		while(operatorAvailable){
			operatorAvailable = false;
			for (int i = 0; i < operators.size(); i++) {
				Node startNode =
				//Node nearest = findNearestNode(Node, CarMoves)
				//CarMove chosen = pickBestCarMove(Node)
				//if(timeAvailable(chosen.Time)){
					//operatorAvailable = true;
					//operators.get(i).add(chosen)
					//double getTotalTime = calculateTime(chosen)
					//operators.get(i).UpdateTime(Node node, chosen)
					//chosen.endNode == chargingNode? : updateCapacity() : updateDeviationFromIdeal();
				//}


			}

		}
		
	}

	/* Suggestion 1:

	 For each operator -
	* 1. Choose nearest car
	* 2. Do the shortest drive possible - weighted by the operations
	* 3. Update States for nodes and charging stations
	* 4. Repeat

	Possible TODO: Run a local search on each operator do optimize the given sequence
	Possible TODO: Allow the initial solution twice the planning period to include more operations.


	Suggestion 2:

	For each operator
	* 1. Use insertion operations from the pool of car moves, until timetable is full
	* 2. Do local search with a fixed number of iterations

	Suggestion 3:

	Random approach

	 */
	
	/* OPERATION WEIGHTS
	 * 1. Prioritize charging cars
	 * 2. Minimize travel distance
	 * 3. Meet ideal state
	 */


	private CarMove findnearestCarMove(Node node){
		double distance = Integer.MAX_VALUE;
		for(Car car : carMoves.keySet()){
			Node fromNode = carMoves.get(car).get(0).getFromNode();

		}
	}

	protected void calculateFitness() {
		for(Operator op : operators) {
			this.costOfPostponed += op.getCostOfPostponed();
			this.costOfTravel += op.getCostOfTravel();
		}
		
		this.fitness = this.costOfPostponed + this.costOfTravel + this.costOfUnmetIdeal;
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
