package code.solver;

import java.util.ArrayList;
import java.util.HashMap;

import code.problem.ProblemInstance;
import code.solver.heuristics.mutators.Mutation;
import code.solver.heuristics.mutators.Swap2;
import code.solver.heuristics.mutators.Swap3;
import code.solver.heuristics.tabusearch.TSIndividual;
import code.solver.heuristics.tabusearch.TabuList;
import constants.HeuristicsConstants;

public class TSSolver extends Solver {
	
	private TSIndividual individual;
	private TabuList tabuList;
	private final int iterations;
	private final int neighborhoodSize;
	private final int tabuSize;
	private HashMap<Integer, Command> mutationToDelta;
	
	public TSSolver() {
		this(HeuristicsConstants.TABU_ITERATIONS, 
				HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE, HeuristicsConstants.TABU_SIZE);
	}
	
	public TSSolver(int iterations, int neighborhoodSize, int tabuSize) {
		this.iterations = iterations;
		this.individual =  new TSIndividual();
		this.neighborhoodSize = neighborhoodSize;
		this.tabuSize = tabuSize;
		this.setMutationToDelta();
	}
	
	/*
	 * Maps available mutation IDs to delta fitness functions. The IDs are defined in the mutation subclasses.
	 */
	private void setMutationToDelta() {
		this.mutationToDelta = new HashMap<>();
		this.mutationToDelta.put(Swap2.id, (Mutation mutation) -> {
			Swap2 swap = (Swap2) mutation;
			return this.individual.deltaFitness(swap);
		});
		
		this.mutationToDelta.put(Swap3.id, (Mutation mutation) -> {
			Swap3 swap = (Swap3) mutation;
			return this.individual.deltaFitness(swap);
		});
	}
	
	@Override
	public void solve(ProblemInstance problemInstance) {
		int iteration = 0;
		this.tabuList = new TabuList(this.tabuSize);
		while(!done(iteration)) {
			System.out.println("Iteration: " + iteration + " Best fitness: " + this.individual.getFitness());
			ArrayList<Mutation> neighborhood = getNeighbors();
			Mutation candidate = neighborhood.remove(neighborhood.size()-1);

			for(Mutation newCandidate : neighborhood) {
				if (this.mutationToDelta.get(newCandidate.getId()).runCommand(newCandidate) 
						< this.mutationToDelta.get(candidate.getId()).runCommand(candidate)) {
					candidate = newCandidate;
				}
			}
			
			if(this.mutationToDelta.get(candidate.getId()).runCommand(candidate) < 0) {
				this.individual.performMutation(candidate);
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
		ArrayList<Mutation> neighborhood = new ArrayList<>();
		for(int i = 0; i < this.neighborhoodSize; i++) {
			for(int j = 0; j < 9; j++) {
				for(int k = j+1; k < 10; k++) {
					neighborhood.add(new Swap2(j, k));
				}
			}
		}
		return neighborhood;
	}

	@Override
	public String getInfo() {
		return "Tabu search";
	}
	
	private interface Command {
		double runCommand(Mutation mutation);
	}
}
