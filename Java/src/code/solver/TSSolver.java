package code.solver;

import java.util.ArrayList;

import code.StaticProblemFile;
import code.solver.heuristics.tabusearch.TSIndividual;
import code.solver.heuristics.tabusearch.TabuList;

public class TSSolver extends Solver {
	
	private TSIndividual individual;
	private TSIndividual best;
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
	public void solve(StaticProblemFile problem) {
		// TODO Auto-generated method stub
		int iteration = 0;
		this.tabuList = new TabuList();
		// Add individual to tabu list
		while(!done(iteration)) {
			ArrayList<TSIndividual> neighborhood = getNeighbors();
			TSIndividual candidate = neighborhood.remove(this.neighborhoodSize-1);
			for(TSIndividual newCandidate : neighborhood) {
				if (candidate.compareTo(newCandidate) > 0) { // Must also check if candidate is in tabu list
					candidate = newCandidate;
				}
			}
			
			if(this.best.compareTo(candidate) > 0) {
				this.best = candidate;
			}
			
			// Add candidate to tabuList and adjust tabuList accordingly
		}
	}
	
	public void solveParallel(StaticProblemFile problem) {
		
	}
	
	public TSIndividual getBest() {
		return this.best;
	}
	
	private boolean done(int iteration) {
		return iteration > iterations;
	}
	
	private ArrayList<TSIndividual> getNeighbors() {
		ArrayList<TSIndividual> neighborhood = new ArrayList<TSIndividual>();
		return neighborhood;
	}
	

	@Override
	public String getInfo() {
		return "Tabu search";
	}
}
