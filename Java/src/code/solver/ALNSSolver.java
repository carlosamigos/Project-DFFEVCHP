package code.solver;

import java.util.*;

import code.problem.ProblemInstance;
import code.problem.nodes.Node;
import code.solver.heuristics.Individual;
import code.solver.heuristics.alns.ALNSIndividual;
import code.solver.heuristics.alns.TabuList;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;
import code.solver.heuristics.mutators.*;
import constants.Constants;
import constants.HeuristicsConstants;
import utils.DeepCopy;
import utils.SolutionFileMaker;

public class ALNSSolver extends Solver {
	
	private ALNSIndividual individual;
	private ALNSIndividual best;
	private TabuList tabuList;
	private final int iterations;
	private int tabuSize;
	
	private HashMap<Integer, DeltaFitness> mutationToDelta;
	private HashMap<Integer, Perform> mutationToPerform;
	private HashMap<Integer, GenerateNeighborhood> mutationToNeighborhood;
	
	private HashMap<Integer, Double> mutationToWeight;
	private HashMap<String, Integer> solutionsSeen;
	private HashMap<Integer, Double> mutationScores;
	private HashMap<Integer, Integer> mutationToAttempts;
	private HashMap<Integer, String> mutationIdToDescription;
	private double weightSum = 0.0;
	
	public ALNSSolver(ProblemInstance problemInstance) {
		this(HeuristicsConstants.TABU_ITERATIONS, HeuristicsConstants.TABU_SIZE, problemInstance);
	}
	
	public ALNSSolver(int iterations, int tabuSize, ProblemInstance problemInstance) {
		this.iterations = iterations;
		this.individual =  new ALNSIndividual(problemInstance);
		this.best = (ALNSIndividual) DeepCopy.copy(this.individual);
		this.best.setFitness(this.individual.getFitness());
		this.tabuSize = tabuSize;
		this.solutionsSeen = new HashMap<>();
		this.initializeMutationIdToDescription();
		this.initializeMutationWeights();
		this.setMutationToDelta();
		this.setMutationToPerform();
		this.setMutationToGenerateNeighborhood();
		this.solutionsSeen.put(this.individual.toString(), 1);
	}
	
	private void initializeMutationWeights() {
		this.mutationToWeight = new HashMap<>();
		this.mutationScores = new HashMap<>();
		this.mutationToAttempts = new HashMap<>();
		int[] mutationIds = {
				EjectionInsertMutation.id,
				EjectionRemoveMutation.id,
				EjectionReplaceMutation.id,
				InterMove.id,
				InterSwap2.id,
				IntraMove.id
			};
		
		for(int id : mutationIds) {
			this.mutationToWeight.put(id, 1.0);
			this.mutationScores.put(id, 0.0);
			this.mutationToAttempts.put(id, 0);
			this.weightSum++;
		}
	}
	
	private void initializeMutationIdToDescription() {
		this.mutationIdToDescription =  new HashMap<>();
		this.mutationIdToDescription.put(EjectionInsertMutation.id, "Ejection Insert");
		this.mutationIdToDescription.put(EjectionRemoveMutation.id, "Ejection Remove");
		this.mutationIdToDescription.put(EjectionReplaceMutation.id, "Ejection Replace");
		this.mutationIdToDescription.put(InterMove.id, "Inter Move");
		this.mutationIdToDescription.put(IntraMove.id, "Intra Move");
		this.mutationIdToDescription.put(InterSwap2.id, "Inter Swap 2");
	}

	
	@Override
	public Individual solve(ProblemInstance problemInstance) {
		best.calculateMoselFitness();
		this.tabuList = new TabuList(this.tabuSize);
		int iteration = 0;
		int counter = 0;        // counts number of rounds with delta > 0
		int global_counter = 0; // counts number of iterations since new global best
		while(!done(iteration)) {
			if(iteration != 0 && iteration % 100 == 0){
				System.out.println("\nIteration: " + iteration + " Best fitness: "
						+ String.format("%.1f", this.best.getFitness()) + ", Current fitness:"
						+ String.format("%.1f", this.individual.getFitness()));
				//System.out.println(individual);
				this.updateWeights();
			}

			// Basic checks
			Set<Mutation> neighborhood = this.getNeighborhood().keySet();
			if(neighborhood.isEmpty()) {
				iteration++;
				continue;
			}
			Mutation candidate = null;
			for(Mutation mutation : neighborhood) {
				candidate = mutation;
				break;
			}

			// Test neighborhood
			double candidateDelta;
			if(global_counter >= HeuristicsConstants.TABU_MAX_NON_IMPROVING_ITERATIONS){
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

			// Perform mutations
			this.individual.addToFitness(candidateDelta);
			this.mutationToPerform.get(candidate.getId()).runCommand(candidate);

			// Update counters and tabulist Size
			if(candidateDelta >= 0){
				counter ++;
			} else {
				counter = 0;
				this.tabuList.decreaseSize();
			}
			if(counter > HeuristicsConstants.TABU_MAX_NON_IMPROVING_LOCAL_ITERATIONS) {
				this.tabuList.increaseSize();
				counter = 0;
			}

			// Update best individual
			boolean bestFound = false;
			if(this.individual.getFitness() < this.best.getFitness()) {
				bestFound = true;
				this.best = (ALNSIndividual) DeepCopy.copy(this.individual);
				this.best.setFitness(this.individual.getFitness());
			} else {
				global_counter++;
			}

			updateMutationScores(candidate, candidateDelta, bestFound);
			tabuList.add(candidate);
			iteration++;
			if(global_counter > HeuristicsConstants.TABU_MAX_NON_IMPROVING_ITERATIONS_DESTROY){
				destroyAndRepair();
				global_counter = 0;
				counter = 0;
				individual.calculateFitness();
			}

		}
		cleanBest();
		best.calculateMoselFitness();
		return best;
	}
	
	public void solveParallel(ProblemInstance problemInstance) {
		
	}

	/*
		* Updates mutation scores

	 */

	public void updateMutationScores(Mutation candidate, double candidateDelta, boolean bestFound){
		// Check if the solution is seen before, and store the current solution
		String individualString = this.individual.toString();
		if(this.solutionsSeen.containsKey(individualString)) {
			this.solutionsSeen.put(individualString, this.solutionsSeen.get(individualString));
		} else {
			this.mutationScores.put(candidate.getId(), this.mutationScores.get(candidate.getId())
					+ HeuristicsConstants.ALNS_FOUND_NEW_SOLUTION);
			this.solutionsSeen.put(individualString, 1);
		}
		// Give reward to mutation type if new solution is better than the current one (locally, not globally)
		if(candidateDelta < 0) {
			this.mutationScores.put(candidate.getId(), this.mutationScores.get(candidate.getId())
					+ HeuristicsConstants.ALNS_FOUND_NEW_BEST_REWARD);
		}
		if(bestFound){
			this.mutationScores.put(candidate.getId(), this.mutationScores.get(candidate.getId())
					+ HeuristicsConstants.ALNS_FOUND_NEW_GLOBAL_BEST_REWARD);
		}
	}

	/*
		* Destroy and repair
		* Destroy by removing a % proportion of the current solution, at random.
	 */

	private void destroyAndRepair(){
		System.out.println("\n Destroys and repairs!");
		int numberToHandle = (int) (this.individual.getTotalNumberOfCarMoves() * HeuristicsConstants.ALNS_DESTROY_FACTOR);
		destroy(numberToHandle);
		repair(numberToHandle);
		this.tabuList.clearTabu();
		initializeMutationWeights();
	}

	private void destroy(int numberToDestroy){
		for (int i = 0; i < numberToDestroy; i++) {
			Set<Mutation> neighborhood  = getNeighborhoodRemove().keySet();
			Mutation candidate;
			if(neighborhood.size() < 1){
				break;
			}
			Random rand = new Random();
			int index = rand.nextInt(neighborhood.size());
			Iterator<Mutation> iter = neighborhood.iterator();
			for (int j = 0; i < index; i++) {
				iter.next();
			}
			candidate = iter.next();
			this.mutationToPerform.get(candidate.getId()).runCommand(candidate);
		}
	}

	private void repair(int numberToRepair){
		for (int i = 0; i < numberToRepair; i++) {
			Set<Mutation> neighborhood  = getNeighborhoodInsert().keySet();
			Mutation candidate = null;
			if(neighborhood.size() < 1){
				break;
			}
			for(Mutation mutation : neighborhood) {
				candidate = mutation;
				break;
			}
			double candidateDelta;
			candidateDelta = this.mutationToDelta.get(candidate.getId()).runCommand(candidate);
			for(Mutation newCandidate : neighborhood) {
				double newCandidateDelta = this.mutationToDelta.get(newCandidate.getId()).runCommand(newCandidate);
				if (newCandidateDelta < candidateDelta ) {
					candidate = newCandidate;
					candidateDelta = newCandidateDelta;
				}
			}
			this.individual.addToFitness(candidateDelta);
			this.mutationToPerform.get(candidate.getId()).runCommand(candidate);
		}
	}
	
	public ALNSIndividual getBest() {
		return this.best;
	}
	
	@Override
	public String getInfo() {
		return "Tabu search";
	}

	private HashMap<Mutation, Integer> getNeighborhoodRemove(){
		return this.mutationToNeighborhood.get(EjectionRemoveMutation.id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE);
	}

	private HashMap<Mutation, Integer> getNeighborhoodInsert(){
		return this.mutationToNeighborhood.get(EjectionInsertMutation.id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE);
	}
	
	private boolean done(int iteration) {
		return iteration >= iterations;
	}
	
	/*
	 * Selects what mutation type to use to create a neighborhood by using accumulated probability
	 */
	private HashMap<Mutation, Integer> getNeighborhood() {
		double accumulated = 0.0;
		double p = Math.random();
		for(int id : this.mutationToWeight.keySet()) {
			accumulated += this.mutationToWeight.get(id) / this.weightSum;
			if(p <= accumulated) {
				this.mutationToAttempts.put(id, this.mutationToAttempts.get(id)+1);
				return this.mutationToNeighborhood.get(id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE);
			}
		}
		return this.mutationToNeighborhood.get(IntraMove.id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE);
	}
	
	/*
	 * Updates the weights for each mutation based on how well each mutation have performed in the last
	 * segment.
	 */
	private void updateWeights() {
		this.weightSum = 0.0;
		for(int id: this.mutationToWeight.keySet()) {
			double r = HeuristicsConstants.ALNS_UPDATE_FACTOR;
			double oldWeight = this.mutationToWeight.get(id);
			double score = this.mutationScores.get(id);
			double attempts = this.mutationToAttempts.get(id);
			double newWeight = oldWeight * (1 - r) + r * (score / attempts);
			this.weightSum += newWeight;
			this.mutationToWeight.put(id, newWeight);
			this.mutationScores.put(id, 0.0);
			this.mutationToAttempts.put(id, 0);
		}
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
	
	/*
	 * Maps available mutation IDs to perform functions. The IDs are defined in the mutation subclasses.
	 */
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
	
	/*
	 * Maps available mutation IDs to generate neighborhood functions. The IDs are defined in the mutation subclasses.
	 */
	private void setMutationToGenerateNeighborhood() {
		this.mutationToNeighborhood = new HashMap<>();
		this.mutationToNeighborhood.put(IntraMove.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodIntraMove(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(InterMove.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodInterMove(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(InterSwap2.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodInterSwap2(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(EjectionInsertMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodEjectionInsert(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(EjectionRemoveMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodEjectionRemove(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(EjectionReplaceMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodEjectionReplace(this.tabuList, tabuSize);
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
