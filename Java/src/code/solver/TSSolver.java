package code.solver;

import java.util.ArrayList;
import java.util.HashMap;

import code.problem.ProblemInstance;
import code.problem.entities.Car;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;
import code.solver.heuristics.mutators.InterMove;
import code.solver.heuristics.mutators.IntraMove;
import code.solver.heuristics.mutators.Mutation;
import code.solver.heuristics.tabusearch.TSIndividual;
import code.solver.heuristics.tabusearch.TabuList;
import constants.HeuristicsConstants;
import utils.ChromosomeGenerator;

public class TSSolver extends Solver {
	
	private TSIndividual individual;
	private TabuList tabuList;
	private final int iterations;
	private final int neighborhoodSize;
	private final int tabuSize;
	
	private HashMap<Integer, DeltaFitness> mutationToDelta;
	private HashMap<Integer, Perform> mutationToPerform;
	
	private final HashMap<Car, ArrayList<CarMove>> carToCarMoves;
	
	public TSSolver(ProblemInstance problemInstance) {
		this(HeuristicsConstants.TABU_ITERATIONS, 
				HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE, HeuristicsConstants.TABU_SIZE, problemInstance);
	}
	
	public TSSolver(int iterations, int neighborhoodSize, int tabuSize, ProblemInstance problemInstance) {
		this.carToCarMoves = ChromosomeGenerator.generateCarMovesFrom(problemInstance);
		this.iterations = iterations;
		this.individual =  new TSIndividual(problemInstance);
		individual.calculateFitness();
			
		this.neighborhoodSize = neighborhoodSize;
		this.tabuSize = tabuSize;
		this.setMutationToDelta();
		this.setMutationToPerform();
	}
	
	/*
	 * Maps available mutation IDs to delta fitness functions. The IDs are defined in the mutation subclasses.
	 */
	private void setMutationToDelta() {
		this.mutationToDelta = new HashMap<>();
		this.mutationToDelta.put(IntraMove.id, (Mutation mutation) -> {
			IntraMove swap = (IntraMove) mutation;
			return this.individual.deltaFitness(swap);
		});
		this.mutationToDelta.put(InterMove.id, (Mutation mutation) -> {
			InterMove interMove = (InterMove) mutation;
			return this.individual.deltaFitness(interMove);
		});
	}
	
	private void setMutationToPerform() {
		this.mutationToPerform = new HashMap<>();
		this.mutationToPerform.put(IntraMove.id, (Mutation mutation) -> {
			IntraMove move = (IntraMove) mutation;
			this.individual.performMutation(move);
		});
		this.mutationToPerform.put(InterMove.id, (Mutation mutation) -> {
			InterMove swap = (InterMove) mutation;
			this.individual.deltaFitness(swap);
		});
	}
	
	@Override
	public void solve(ProblemInstance problemInstance) {
		int iteration = 0;
		this.tabuList = new TabuList(this.tabuSize);
		while(!done(iteration)) {
			System.out.println("Iteration: " + iteration + " Best fitness: " + this.individual.getFitness());
			//System.out.println(this.individual);
			ArrayList<Mutation> neighborhood = getNeighbors();
			Mutation candidate = neighborhood.remove(neighborhood.size()-1);
			double candidateDelta = this.mutationToDelta.get(candidate.getId()).runCommand(candidate);
			for(Mutation newCandidate : neighborhood) {
				double newCandidateDelta = this.mutationToDelta.get(newCandidate.getId()).runCommand(newCandidate);
				if (newCandidateDelta < candidateDelta) {
					candidate = newCandidate;
					candidateDelta = newCandidateDelta;
				}
			}
			
			if(candidateDelta < 0) {
				this.individual.addToFitness(candidateDelta);
				this.mutationToPerform.get(candidate.getId()).runCommand(candidate);
			}
			
			tabuList.add(candidate);
			iteration++;
		}
	}
	
	public void solveParallel(ProblemInstance problemInstance) {
		
	}
	
	public TSIndividual getBest() {
		return this.individual;
	}
	
	private boolean done(int iteration) {
		return iteration >= iterations;
	}
	
	// Get neighbors that are not in the tabuList
	private ArrayList<Mutation> getNeighbors() {
		return individual.getNeighbors(this.neighborhoodSize);
	}



	@Override
	public String getInfo() {
		return "Tabu search";
	}
	
	private interface DeltaFitness {
		double runCommand(Mutation mutation);
	}
	
	private interface Perform {
		void runCommand(Mutation mutation);
	}
}
