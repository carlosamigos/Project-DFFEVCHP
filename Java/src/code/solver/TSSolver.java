package code.solver;

import java.util.ArrayList;

import code.StaticProblemFile;
import code.problem.ProblemInstance;
import code.solver.heuristics.mutators.Mutation;
import code.solver.heuristics.tabusearch.TSIndividual;
import code.solver.heuristics.tabusearch.TabuList;

public class TSSolver extends Solver {
	
	private TSIndividual individual;
	private TabuList tabuList;
	private final int iterations;
	private final int neighborhoodSize;
	private final int tabuSize;
	
	public TSSolver(int iterations, int neighborhoodSize, int tabuSize) {
		this.iterations = iterations;
		this.individual =  new TSIndividual();
		this.neighborhoodSize = neighborhoodSize;
		this.tabuSize = tabuSize;
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
				if (this.individual.deltaFitness(candidate) > this.individual.deltaFitness(newCandidate)) {
					candidate = newCandidate;
				}
			}
			
			if(this.individual.deltaFitness(candidate) < 0) {
				this.individual.performMutation(candidate);
			}
			
			tabuList.add(candidate);
			iteration++;
		}
	}
	
	public void solveParallel(StaticProblemFile problem) {
		
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
		}
		return neighborhood;
	}
	

	@Override
	public String getInfo() {
		return "Tabu search";
	}
}
