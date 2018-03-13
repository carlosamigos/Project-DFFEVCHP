package code.solver;

import java.util.ArrayList;

import code.problem.ProblemInstance;
import code.solver.heuristics.Individual;

public class GASolver extends Solver {
	
	private ArrayList<Individual> population;
	private Individual best;
	private final int iterations;
	private final int maxNumberOfIterationsWithoutImprovement;
	private final int populationSize;
	
	public GASolver(int iterations, int maxNumberOfIterationsWithoutImprovement, int populationSize) {
		this.iterations = iterations;
		this.maxNumberOfIterationsWithoutImprovement = maxNumberOfIterationsWithoutImprovement;
		this.populationSize = populationSize;
	}

	@Override
	public void solve(ProblemInstance problemInstance) {
		this.population = initializePopulation();
		int iteration = 0;
		int iterationsSinceBestUpdated = 0;
		while(!done(iteration)) {
			System.out.println("Iteration: " + iteration + ", Best fitness: " + best.getFitness());
			selectParents(this.population);
			generateOffsprings(this.population);
			educateOffsprings(this.population);
			selectSurvivors(this.population);
			adjustPenaltyParameters();
			if(iterationsSinceBestUpdated > maxNumberOfIterationsWithoutImprovement) {
				diversifyPopulation(this.population);
			}
		}
		
	}

	private void diversifyPopulation(ArrayList<Individual> population2) {
		// TODO Auto-generated method stub
		
	}

	private void adjustPenaltyParameters() {
		// TODO Auto-generated method stub
		
	}

	private void selectSurvivors(ArrayList<Individual> population2) {
		// TODO Auto-generated method stub
		
	}

	private void educateOffsprings(ArrayList<Individual> population2) {
		// TODO Auto-generated method stub
		
	}

	private void generateOffsprings(ArrayList<Individual> population2) {
		// TODO Auto-generated method stub
		
	}

	private void selectParents(ArrayList<Individual> population2) {
		// TODO Auto-generated method stub
		
	}

	private boolean done(int iteration) {
		return iteration >= iterations;
	}

	private ArrayList<Individual> initializePopulation() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Individual getBest() {
		return this.best;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
