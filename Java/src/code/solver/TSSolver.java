package code.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import code.problem.ProblemInstance;
import code.problem.nodes.Node;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;
import code.solver.heuristics.mutators.*;
import code.solver.heuristics.tabusearch.TSIndividual;
import code.solver.heuristics.tabusearch.TabuList;
import constants.Constants;
import constants.HeuristicsConstants;
import utils.DeepCopy;

public class TSSolver extends Solver {
	
	private TSIndividual individual;
	private TSIndividual best;
	private TabuList tabuList;
	private final int iterations;
	private int tabuSize;
	
	private HashMap<Integer, DeltaFitness> mutationToDelta;
	private HashMap<Integer, Perform> mutationToPerform;
	private HashMap<Integer, GenerateNeighborhood> mutationToNeighborhood;
	
	private HashMap<Integer, Double> mutationToWeight;
	private HashMap<String, Integer> solutionsSeen;
	
	public TSSolver(ProblemInstance problemInstance) {
		this(HeuristicsConstants.TABU_ITERATIONS, HeuristicsConstants.TABU_SIZE, problemInstance);
	}
	
	public TSSolver(int iterations, int tabuSize, ProblemInstance problemInstance) {
		this.iterations = iterations;
		this.individual =  new TSIndividual(problemInstance);
		this.best = (TSIndividual) DeepCopy.copy(this.individual);
		this.best.setFitness(this.individual.getFitness());
		this.tabuSize = tabuSize;
		this.solutionsSeen = new HashMap<>();
		this.initializeMutationWeights();
		this.setMutationToDelta();
		this.setMutationToPerform();
		this.setMutationToGenerateNeighborhood();
		this.solutionsSeen.put(this.individual.toString(), 1);
	}
	
	private void initializeMutationWeights() {
		this.mutationToWeight = new HashMap<>();
		int[] mutationIds = {
				EjectionInsertMutation.id,
				EjectionRemoveMutation.id,
				EjectionReplaceMutation.id,
				InterMove.id,
				InterSwap2.id,
				IntraMove.id
			};
		double initialWeight = 1 / ((double) mutationIds.length);
		
		for(int id : mutationIds) {
			this.mutationToWeight.put(id, initialWeight);
		}
	}

	
	@Override
	public void solve(ProblemInstance problemInstance) {
		best.calculateMoselFitness();
		int iteration = 0;
		this.tabuList = new TabuList(this.tabuSize);
		double counter = 0; // counts number of rounds with 0 delta
		while(!done(iteration)) {
			if(iteration % 100 == 0){
				System.out.println("Iteration: " + iteration + " Best fitness: "
						+ String.format("%.1f", this.best.getFitness()) + ", Current fitness:"
						+ String.format("%.1f", this.individual.getFitness()));
				//System.out.println(individual);
			}

			Set<Mutation> neighborhood = this.getNeighborhood().keySet();
			//Set<Mutation> neighborhood = this.individual.generateFullNeighborhood(this.tabuList).keySet();
			Mutation candidate = null;
			for(Mutation mutation : neighborhood) {
				candidate = mutation;
				break;
			}
			
			// Checks if the neighborhood is empty, if so jump to the next iteration.
			if(candidate == null) {
				continue;
			}
			double candidateDelta;
			if(counter >= HeuristicsConstants.TABU_MAX_NON_IMPROVING_ITERATIONS){
				// Scramble if stuck
				counter = 0;
				Mutation newCandidate = getRandomMutation(neighborhood);
				candidateDelta = this.mutationToDelta.get(newCandidate.getId()).runCommand(newCandidate);
				candidate = newCandidate;
			} else {
				candidateDelta = this.mutationToDelta.get(candidate.getId()).runCommand(candidate);
				for(Mutation newCandidate : neighborhood) {
					double newCandidateDelta = this.mutationToDelta.get(newCandidate.getId()).runCommand(newCandidate);
					if (newCandidateDelta < candidateDelta ) {
						candidate = newCandidate;
						candidateDelta = newCandidateDelta;
					}
				}
			}

			this.individual.addToFitness(candidateDelta);
			this.mutationToPerform.get(candidate.getId()).runCommand(candidate);
			if(candidateDelta == 0){
				counter ++;
			} else {
				counter = 0;
			}
			
			// Check if the solution is seen before, and store the current solution
			String individualString = this.individual.toString();
			if(this.solutionsSeen.containsKey(individualString)) {
				this.solutionsSeen.put(individualString, this.solutionsSeen.get(individualString));
			} else {
				this.solutionsSeen.put(individualString, 1);
			}


			// Update best individual
			if(this.individual.getFitness() < this.best.getFitness()) {
				this.best = (TSIndividual) DeepCopy.copy(this.individual);
				this.best.setFitness(this.individual.getFitness());
			}
			
			tabuList.add(candidate);
			iteration++;
		}
		cleanBest();
		best.calculateMoselFitness();
	}
	
	public void solveParallel(ProblemInstance problemInstance) {
		
	}
	
	public TSIndividual getBest() {
		return this.best;
	}
	
	@Override
	public String getInfo() {
		return "Tabu search";
	}
	
	private boolean done(int iteration) {
		return iteration >= iterations;
	}
	
	private HashMap<Mutation, Integer> getNeighborhood() {
		double accumulated = 0.0;
		double p = Math.random();
		for(int id : this.mutationToWeight.keySet()) {
			accumulated += this.mutationToWeight.get(id);
			if(p <= accumulated) {
				return this.mutationToNeighborhood.get(id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE);
			}
		}
		return this.mutationToNeighborhood.get(IntraMove.id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE);
	}
	
	/*
	 * Maps available mutation IDs to delta fitness functions. The IDs are defined in the mutation subclasses.
	 */
	private void setMutationToDelta() {
		this.mutationToDelta = new HashMap<>();
		this.mutationToDelta.put(IntraMove.id, (Mutation mutation) -> {
			IntraMove intraMove = (IntraMove) mutation;
			return this.individual.deltaFitness(intraMove);
		});
		this.mutationToDelta.put(InterMove.id, (Mutation mutation) -> {
			InterMove interMove = (InterMove) mutation;
			return this.individual.deltaFitness(interMove);
		});
		this.mutationToDelta.put(EjectionReplaceMutation.id, (Mutation mutation) -> {
			EjectionReplaceMutation ejectionReplaceMutation = (EjectionReplaceMutation) mutation;
			return this.individual.deltaFitness(ejectionReplaceMutation);
		});
		this.mutationToDelta.put(InterSwap2.id, (Mutation mutation) -> {
			InterSwap2 interSwap2 = (InterSwap2) mutation;
			return this.individual.deltaFitness(interSwap2);
		});
		this.mutationToDelta.put(EjectionInsertMutation.id, (Mutation mutation) -> {
			EjectionInsertMutation ejectionInsertMutation = (EjectionInsertMutation) mutation;
			return this.individual.deltaFitness(ejectionInsertMutation);
		});
		this.mutationToDelta.put(EjectionRemoveMutation.id, (Mutation mutation) -> {
			EjectionRemoveMutation ejectionRemoveMutation = (EjectionRemoveMutation) mutation;
			return this.individual.deltaFitness(ejectionRemoveMutation);
		});
	}
	
	private void setMutationToPerform() {
		this.mutationToPerform = new HashMap<>();
		this.mutationToPerform.put(IntraMove.id, (Mutation mutation) -> {
			IntraMove intraMove = (IntraMove) mutation;
			this.individual.performMutation(intraMove);
		});
		this.mutationToPerform.put(InterMove.id, (Mutation mutation) -> {
			InterMove interMove = (InterMove) mutation;
			this.individual.performMutation(interMove);
		});
		this.mutationToPerform.put(EjectionReplaceMutation.id, (Mutation mutation) -> {
			EjectionReplaceMutation ejectionReplaceMutation = (EjectionReplaceMutation) mutation;
			this.individual.performMutation(ejectionReplaceMutation);
		});
		this.mutationToPerform.put(InterSwap2.id, (Mutation mutation) -> {
			InterSwap2 interSwap2 = (InterSwap2) mutation;
			this.individual.performMutation(interSwap2);
		});
		this.mutationToPerform.put(EjectionInsertMutation.id, (Mutation mutation) -> {
			EjectionInsertMutation ejectionInsertMutation = (EjectionInsertMutation) mutation;
			this.individual.performMutation(ejectionInsertMutation);
		});
		this.mutationToPerform.put(EjectionRemoveMutation.id, (Mutation mutation) -> {
			EjectionRemoveMutation ejectionRemoveMutation = (EjectionRemoveMutation) mutation;
			this.individual.performMutation(ejectionRemoveMutation);
		});
	}
	
	private void setMutationToGenerateNeighborhood() {
		this.mutationToNeighborhood = new HashMap<>();
		this.mutationToNeighborhood.put(IntraMove.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodIntraMove(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(InterMove.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodIntraMove(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(InterSwap2.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodIntraMove(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(EjectionInsertMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodIntraMove(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(EjectionRemoveMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodIntraMove(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(EjectionReplaceMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodIntraMove(this.tabuList, tabuSize);
		});
	}
	
	private interface DeltaFitness {
		double runCommand(Mutation mutation);
	}
	
	private interface Perform {
		void runCommand(Mutation mutation);
	}
	
	private interface GenerateNeighborhood {
		HashMap<Mutation, Integer> runCommand(int size);
	}

	private void cleanBest(){
		double currentTime;
		for(Object object : best.getOperators()){
			Operator operator = (Operator) object;
			ArrayList<CarMove> newCarMoveList = new ArrayList<>();
			currentTime = operator.getStartTime();
			Node prevNode = operator.getStartNode();
			for(CarMove carMove : operator.getCarMoveCopy()) {
				//Need to take earliest start time of the move into account
				currentTime += best.getProblemInstance().getTravelTimeBike(prevNode, carMove.getFromNode());
				currentTime += carMove.getTravelTime();
				if (currentTime > Constants.TIME_LIMIT_STATIC_PROBLEM) {
					currentTime += - best.getProblemInstance().getTravelTimeBike(prevNode, carMove.getFromNode()) - carMove.getTravelTime();
					break;
				} else {
					newCarMoveList.add(carMove);
				}
				prevNode = carMove.getToNode();
			}
			System.out.println("Operator: " + operator.id + " Time: " + currentTime);
			operator.setCarMoves(newCarMoveList);
			
		}
	}

	private Mutation getRandomMutation(Set<Mutation> neighborhood){
		int size = neighborhood.size();
		int item = new Random().nextInt(size);
		int i = 0;
		for(Mutation mutation : neighborhood)
		{
			if (i == item)
				return mutation;
			i++;
		}
		return neighborhood.iterator().next();
	}
}
