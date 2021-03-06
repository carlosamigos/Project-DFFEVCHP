package code.solver;

import java.text.DecimalFormat;
import java.util.*;

import code.problem.ProblemInstance;
import code.problem.nodes.Node;
import code.solver.heuristics.Individual;
import code.solver.heuristics.alns.ALNSIndividual;
import code.solver.heuristics.alns.BestIndividual;
import code.solver.heuristics.alns.TabuList;
import code.solver.heuristics.entities.CarMove;
import code.solver.heuristics.entities.Operator;
import code.solver.heuristics.mutators.*;
import code.solver.heuristics.searches.*;
import constants.Constants;
import constants.HeuristicsConstants;
import utils.SolutionFileMaker;

public class ALNSSolver extends Solver {
	
	private ALNSIndividual individual;
	private BestIndividual bestIndividual;
	private TabuList tabuList;
	private int iterations;
	private int tabuSize;
	
	private long startTime;
	private long endTime;
	private double timeUsed;
	
	private int globalIterationsWithoutImprovement;
	
	private HashMap<Integer, DeltaFitness> mutationToDelta;
	private HashMap<Integer, Perform> mutationToPerform;
	private HashMap<Integer, GenerateNeighborhood> mutationToNeighborhood;
	private HashMap<Integer, GenerateNeighborhood> mutationToNeighborhoodFull;
	
	private HashMap<Integer, Double> mutationToWeight;
	private HashMap<String, Integer> solutionsSeen;
	private HashMap<Integer, Double> mutationScores;
	private HashMap<Integer, Integer> mutationToAttempts;
	private HashMap<Integer, String> mutationIdToDescription;
	private double weightSum = 0.0;
	private boolean set = false;


	// LNS
	private HashMap<Integer, Double> mutationToWeightLNSDestroy;
	private HashMap<Integer, Double> mutationScoresLNSDestroy;
	private HashMap<Integer, Integer> mutationToAttemptsLNSDestroy;
	private double weightSumLNSDestroy = 0.0;

	private HashMap<Integer, Double> mutationToWeightLNSRepair;
	private HashMap<Integer, Double> mutationScoresLNSRepair;
	private HashMap<Integer, Integer> mutationToAttemptsLNSRepair;
	private double weightSumLNSRepair = 0.0;

	private HashMap<Integer, Destroy> searchToPerformDestroy;
	private HashMap<Integer, Repair> searchToPerformRepair;
	private HashMap<Integer, Search> searchToNeighborhood;

	private int numberToHandle;

	private boolean newBestFound = false;
	private boolean newSolutionFound = false;

	// Scalable constants
	
	public ALNSSolver() {}
	
	public ALNSSolver(ProblemInstance problemInstance) {
		setVariables(problemInstance);
	}
	
	private void setVariables(ProblemInstance problemInstance) {
		this.iterations = HeuristicsConstants.TABU_ITERATIONS;
		this.individual =  new ALNSIndividual(problemInstance);
		// Scalable constants
		initializeHeuristicConstants();
		setBest();
		this.tabuSize = HeuristicsConstants.TABU_SIZE;
		this.solutionsSeen = new HashMap<>();
		this.initializeMutationIdToDescription();
		this.initializeMutationWeights();
		this.setMutationToDelta();
		this.setMutationToPerform();
		if(HeuristicsConstants.ALNS_FULL_NEIGHBORHOOD){
			this.setMutationToGenerateNeighborhoodFull();
		}
		else{
			this.setMutationToGenerateNeighborhood();
		}
		this.solutionsSeen.put(this.individual.toString(), 1);
		this.set = true;
		//LNS
		this.setSearchToPerformDestroy();
		this.setSearchToPerformRepair();
		this.globalIterationsWithoutImprovement = 0;
	}

	private void initializeMutationWeights() {
		this.mutationToWeight = new HashMap<>();
		this.mutationScores = new HashMap<>();
		this.mutationToAttempts = new HashMap<>();
		int[] mutationIds = {
				EjectionInsertMutation.id,
				EjectionRemoveMutation.id,
				EjectionReplaceMutation.id,
				EjectionSwapMutation.id,
				InterMove.id,
				InterSwap2.id,
				IntraMove.id,
				IntraSwap.id,
				Inter2Move.id

			};
		
		for(int id : mutationIds) {
			this.mutationToWeight.put(id, 1.0);
			this.mutationScores.put(id, 0.0);
			this.mutationToAttempts.put(id, 1);
			this.weightSum++;
		}

		// LNS
		this.mutationToWeightLNSDestroy = new HashMap<>();
		this.mutationScoresLNSDestroy = new HashMap<>();
		this.mutationToAttemptsLNSDestroy = new HashMap<>();
		int[] searchIds = {
				RandomDestroy.id,
				RelatedDestroy.id,
				WorstDestroy.id
		};

		for(int id : searchIds) {
			this.mutationToWeightLNSDestroy.put(id, 1.0);
			this.mutationScoresLNSDestroy.put(id, 0.0);
			this.mutationToAttemptsLNSDestroy.put(id, 1);
			this.weightSumLNSDestroy++;
		}

		this.mutationToWeightLNSRepair = new HashMap<>();
		this.mutationScoresLNSRepair = new HashMap<>();
		this.mutationToAttemptsLNSRepair = new HashMap<>();
		int[] searchIdsRepair = {
				BestRepair.id,
				RegretRepair.id,
				RegretRepair2.id
		};
		for(int id : searchIdsRepair) {
			this.mutationToWeightLNSRepair.put(id, 1.0);
			this.mutationScoresLNSRepair.put(id, 0.0);
			this.mutationToAttemptsLNSRepair.put(id, 1);
			this.weightSumLNSRepair++;
		}
		this.searchToNeighborhood = new HashMap<>();
		searchToNeighborhood.put(BestRepair.id, new BestRepair());
		searchToNeighborhood.put(RandomDestroy.id, new RandomDestroy());
		searchToNeighborhood.put(RegretRepair.id, new RegretRepair());
		searchToNeighborhood.put(RegretRepair2.id, new RegretRepair2());
		searchToNeighborhood.put(RelatedDestroy.id, new RelatedDestroy());
		searchToNeighborhood.put(WorstDestroy.id, new WorstDestroy());

	}

	private void initializeHeuristicConstants(){
		if(HeuristicsConstants.ALNS_SCALE_CONSTANT_DESTROY > 0){
			HeuristicsConstants.TABU_MAX_NON_IMPROVING_ITERATIONS_DESTROY = (int) Math.max(Math.floor(HeuristicsConstants.ALNS_SCALE_CONSTANT_DESTROY *
				 Math.log(this.individual.getProblemSize())), 1);
		}if(HeuristicsConstants.ALNS_SCALE_CONSTANT_MUTATION > 0){
			HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE = (int) Math.max(Math.floor(HeuristicsConstants.ALNS_SCALE_CONSTANT_MUTATION *
				Math.log(this.individual.getProblemSize())), 1);
		}if(HeuristicsConstants.ALNS_SCALE_CONSTANT_WEIGHT > 0){
			HeuristicsConstants.TABU_WEIGHT_UPDATE = (int) Math.max(Math.ceil(HeuristicsConstants.ALNS_SCALE_CONSTANT_WEIGHT *
					Math.log(this.individual.getProblemSize())), 1);
		}
	}

	private void initializeMutationIdToDescription() {
		this.mutationIdToDescription =  new HashMap<>();
		this.mutationIdToDescription.put(EjectionInsertMutation.id, "Ejection Insert");
		this.mutationIdToDescription.put(EjectionRemoveMutation.id, "Ejection Remove");
		this.mutationIdToDescription.put(EjectionReplaceMutation.id, "Ejection Replace");
		this.mutationIdToDescription.put(EjectionSwapMutation.id, "Ejection Swap");
		this.mutationIdToDescription.put(InterMove.id, "Inter Move");
		this.mutationIdToDescription.put(IntraMove.id, "Intra Move");
		this.mutationIdToDescription.put(InterSwap2.id, "Inter Swap 2");
		this.mutationIdToDescription.put(Inter2Move.id, "Inter Move 2");
		this.mutationIdToDescription.put(IntraSwap.id, "Intra Swap");
	}

	
	@Override
	public Individual solve(ProblemInstance problemInstance) {
		if(!set) {
			setVariables(problemInstance);
		}
		this.startTime = System.currentTimeMillis();
		this.endTime = this.startTime + HeuristicsConstants.ALNS_MAX_TIME_SECONDS * 1000;
		this.tabuList = new TabuList(this.tabuSize);
		int iteration = 0;
		int counter = 0;        // counts number of rounds with delta > 0
		int global_counter = 0; // counts number of iterations since new global best
		int destroy_counter = 0; // count number of iteration since new global best, resets each destroy
		int tabuCounter = 0; // counts number of iterations with delta < 0

		//LNS
		int neighborhoodDestroyId = 0;
		int neighborhoodRepairId = 0;

		while(!done(iteration)) {
			if(iteration != 0 && iteration % HeuristicsConstants.TABU_WEIGHT_UPDATE == 0){
				if(HeuristicsConstants.PRINT_OUT_PROGRESS) {
					System.out.println("\nIteration: " + iteration + " Best fitness: "
							+ String.format("%.1f", this.bestIndividual.getFitness()) + ", Current fitness:"
							+ String.format("%.1f", this.individual.getFitness()));
				}
				this.updateWeights();

			}

			Set<Mutation> neighborhood;
			if(HeuristicsConstants.ALNS_FULL_ALL_NEIGHBORHOOD){
				neighborhood = this.individual.generateFullNeighborhood(this.tabuList).keySet();
			}else{
				neighborhood = this.getNeighborhood().keySet();
			}

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
					if(HeuristicsConstants.BEST_FIRST && candidateDelta < 0) {
						break;
					}
					double newCandidateDelta = this.mutationToDelta.get(newCandidate.getId()).runCommand(newCandidate);
					if (newCandidateDelta < candidateDelta) {
						candidate = newCandidate;
						candidateDelta = newCandidateDelta;
					}
				}
			}

			// Perform mutations
			this.individual.addToFitness(candidateDelta);
			this.mutationToPerform.get(candidate.getId()).runCommand(candidate);


			// Update Counters and TabuList Size
			if(candidateDelta >= 0){
				counter ++;
				tabuCounter = 0;
				if(counter > HeuristicsConstants.TABU_MAX_NON_IMPROVING_LOCAL_ITERATIONS) {
					this.tabuList.increaseSize();
					counter = 0;
				}
			} else {
				counter = 0;
				tabuCounter++;
				if(tabuCounter > HeuristicsConstants.TABU_MIN_IMPROVING_LOCAL_ITERATIONS){
					this.tabuList.decreaseSize();
					tabuCounter = 0;
				}
			}


			// Update best individual
			boolean bestFound = false;
			if(this.individual.getFitness() < this.bestIndividual.getFitness()) {
				bestFound = true;
				setBest();
				this.globalIterationsWithoutImprovement = 0;
				destroy_counter = 0;
				global_counter = 0;
			} else {
				global_counter++;
				this.globalIterationsWithoutImprovement++;
				destroy_counter++;
			}

			updateMutationScores(candidate, candidateDelta, bestFound);
			tabuList.add(candidate);
			iteration++;

			// LNS - DESTROY AND REPAIR
			/*
			if(neighborhoodDestroyId != 0){
				updateMutationScoresLNSDestroyAndRepair(neighborhoodDestroyId, neighborhoodRepairId, bestFound);
			}
			*/
			if(destroy_counter > HeuristicsConstants.TABU_MAX_NON_IMPROVING_ITERATIONS_DESTROY){
				//this.tabuList.clearTabu();
				if(neighborhoodDestroyId != 0) {
					updateMutationScoresLNSDestroyAndRepair(neighborhoodDestroyId, neighborhoodRepairId, bestFound);
				}
				updateWeightsLNSDestroy();
				updateWeightsLNSRepair();
				neighborhoodDestroyId = getNeighborhoodLNSDestroy();
				neighborhoodRepairId = getNeighborhoodLNSRepair();
				//this.numberToHandle = (int) (Math.random() * HeuristicsConstants.ALNS_DESTROY_FACTOR * this.individual.getTotalNumberOfCarMoves());
				this.numberToHandle = (int) (this.individual.getTotalNumberOfCarMoves() * HeuristicsConstants.ALNS_DESTROY_FACTOR);
				//Destroy
				this.searchToPerformDestroy.get(neighborhoodDestroyId).runCommand(searchToNeighborhood.get(neighborhoodDestroyId));
				//Repair
				this.searchToPerformRepair.get(neighborhoodRepairId).runCommand(searchToNeighborhood.get(neighborhoodRepairId));
				destroy_counter = 0;
				counter = 0;
				tabuCounter = 0;
				individual.calculateFitness();

			}
		}
		this.timeUsed = (System.currentTimeMillis() - this.startTime)/1000;
		SolutionFileMaker.writeSolutionToFile(bestIndividual, problemInstance, problemInstance.getFileName() + ".txt");
		this.set = false;
		if(HeuristicsConstants.PRINT_OUT_BEST_SOLUTION){
			System.out.println(bestIndividual);
		}
		return bestIndividual;
	}


	/*
		* Updates mutation scores
	 */
	/*
	public void updateMutationScores(Mutation candidate, double candidateDelta, boolean bestFound){
		// Check if the solution is seen before, and store the current solution
		String individualString = this.individual.toString();
		if(this.solutionsSeen.containsKey(individualString)) {
			this.solutionsSeen.put(individualString, this.solutionsSeen.get(individualString) + 1);
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
	*/

	public void updateMutationScores(Mutation candidate, double candidateDelta, boolean bestFound){
		// Check if the solution is seen before, and store the current solution
		String individualString = this.individual.toString();
		if(bestFound){
			this.mutationScores.put(candidate.getId(), this.mutationScores.get(candidate.getId())
					+ HeuristicsConstants.ALNS_FOUND_NEW_GLOBAL_BEST_REWARD);
			this.solutionsSeen.put(individualString, 1);
			this.newBestFound = true;
		}
		else{
			if(this.solutionsSeen.containsKey(individualString)){
				this.solutionsSeen.put(individualString, this.solutionsSeen.get(individualString) + 1);
			}else {
				if(candidateDelta < 0){
					this.newSolutionFound = true;
					this.mutationScores.put(candidate.getId(), this.mutationScores.get(candidate.getId())
						+ HeuristicsConstants.ALNS_FOUND_NEW_BEST_REWARD);
				}else {
					this.mutationScores.put(candidate.getId(), this.mutationScores.get(candidate.getId())
							+ HeuristicsConstants.ALNS_FOUND_NEW_SOLUTION);
				}this.solutionsSeen.put(individualString, 1);
			}
		}
	}

	//LNS
	/*
	public void updateMutationScoresLNSDestroyAndRepair(int destroyId, int repairId, boolean bestFound){
		String individualString = this.individual.toString();
		if(bestFound){
			this.mutationScoresLNSDestroy.put(destroyId, this.mutationScoresLNSDestroy.get(destroyId)
					+ HeuristicsConstants.ALNS_FOUND_NEW_GLOBAL_BEST_REWARD_LNS);
			this.mutationScoresLNSRepair.put(repairId, this.mutationScoresLNSRepair.get(repairId)
					+ HeuristicsConstants.ALNS_FOUND_NEW_GLOBAL_BEST_REWARD_LNS);
		}else{
			if(this.solutionsSeen.containsKey(individualString)){
				this.solutionsSeen.put(individualString, this.solutionsSeen.get(individualString) + 1);
			}else{
				this.mutationScoresLNSDestroy.put(destroyId, this.mutationScoresLNSDestroy.get(destroyId)
						+ HeuristicsConstants.ALNS_FOUND_NEW_SOLUTION_LNS);
				this.mutationScoresLNSRepair.put(repairId, this.mutationScoresLNSRepair.get(repairId)
						+ HeuristicsConstants.ALNS_FOUND_NEW_SOLUTION_LNS);

			}this.solutionsSeen.put(individualString, 1);
		}
	}
	*/
	public void updateMutationScoresLNSDestroyAndRepair(int destroyId, int repairId, boolean bestFound){
		if(newBestFound){
			this.newBestFound = false;
			this.mutationScoresLNSDestroy.put(destroyId, this.mutationScoresLNSDestroy.get(destroyId)
					+ HeuristicsConstants.ALNS_FOUND_NEW_GLOBAL_BEST_REWARD_LNS);
			this.mutationScoresLNSRepair.put(repairId, this.mutationScoresLNSRepair.get(repairId)
					+ HeuristicsConstants.ALNS_FOUND_NEW_GLOBAL_BEST_REWARD_LNS);
		}else if(newSolutionFound){
			this.newSolutionFound = false;
			this.mutationScoresLNSDestroy.put(destroyId, this.mutationScoresLNSDestroy.get(destroyId)
					+ HeuristicsConstants.ALNS_FOUND_NEW_SOLUTION_LNS);
			this.mutationScoresLNSRepair.put(repairId, this.mutationScoresLNSRepair.get(repairId)
					+ HeuristicsConstants.ALNS_FOUND_NEW_SOLUTION_LNS);

		}
	}
	
	@Override
	public String getInfo() {
		return "ALNS search";
	}

	private boolean done(int iteration) {
		return iteration >= iterations || 
				System.currentTimeMillis() > this.endTime ||
				HeuristicsConstants.ALNS_MAX_ITERATIONS_WITHOUT_IMPROVEMENT < this.globalIterationsWithoutImprovement;
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
				if(HeuristicsConstants.ALNS_FULL_NEIGHBORHOOD){
					return this.mutationToNeighborhoodFull.get(id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE);
				}else{
					return this.mutationToNeighborhood.get(id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE);
				}
			}
		}
		if(HeuristicsConstants.ALNS_FULL_NEIGHBORHOOD){
			return this.mutationToNeighborhoodFull.get(IntraMove.id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE);
		}
		return this.mutationToNeighborhood.get(IntraMove.id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE);
	}

	//LNS
	private int getNeighborhoodLNSDestroy() {
		double accumulated = 0.0;
		double p = Math.random();
		for(int id: this.mutationToWeightLNSDestroy.keySet()){
			accumulated += this.mutationToWeightLNSDestroy.get(id) /this.weightSumLNSDestroy;
			if(p <= accumulated){
				this.mutationToAttemptsLNSDestroy.put(id, this.mutationToAttemptsLNSDestroy.get(id) +1);
				return id;
			}
		}
		return RandomDestroy.id;
	}

	private int getNeighborhoodLNSRepair() {
		double accumulated = 0.0;
		double p = Math.random();
		for(int id: this.mutationToWeightLNSRepair.keySet()){
			accumulated += this.mutationToWeightLNSRepair.get(id) /this.weightSumLNSRepair;
			if(p <= accumulated){
				this.mutationToAttemptsLNSRepair.put(id, this.mutationToAttemptsLNSRepair.get(id) +1);
				return id;
			}
		}
		return BestRepair.id;
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
			double attempts = this.mutationToAttempts.get(id) + 1;
			double newWeight = Math.max(oldWeight * (1 - r) + r * (score / attempts), HeuristicsConstants.ALNS_MINIMAL_WEIGHT_FACTOR);
			this.weightSum += newWeight;
			this.mutationToWeight.put(id, newWeight);
			this.mutationScores.put(id, 0.0);
			this.mutationToAttempts.put(id, 1);
		}
	}

	//LNS
	private void updateWeightsLNSDestroy(){
		this.weightSumLNSDestroy = 0.0;
		for(int id: this.mutationToWeightLNSDestroy.keySet()){
			double r = HeuristicsConstants.ALNS_UPDATE_FACTOR_LNS;
			double oldWeight = this.mutationToWeightLNSDestroy.get(id);
			double score = this.mutationScoresLNSDestroy.get(id);
			double attempts = this.mutationToAttemptsLNSDestroy.get(id) + 1;
			double newWeight = Math.max(oldWeight * (1 - r) + r * (score / attempts), HeuristicsConstants.ALNS_MINIMAL_WEIGHT_FACTOR_LNS);
			this.weightSumLNSDestroy += newWeight;
			this.mutationToWeightLNSDestroy.put(id, newWeight);
			this.mutationScoresLNSDestroy.put(id, 0.0);
			this.mutationToAttemptsLNSDestroy.put(id, 1);
		}
	}

	private void updateWeightsLNSRepair(){
		this.weightSumLNSRepair = 0.0;
		for(int id: this.mutationToWeightLNSRepair.keySet()){
			double r = HeuristicsConstants.ALNS_UPDATE_FACTOR_LNS;
			double oldWeight = this.mutationToWeightLNSRepair.get(id);
			double score = this.mutationScoresLNSRepair.get(id);
			double attempts = this.mutationToAttemptsLNSRepair.get(id) + 1;
			double newWeight = Math.max(oldWeight * (1 - r) + r * (score / attempts), HeuristicsConstants.ALNS_MINIMAL_WEIGHT_FACTOR_LNS);
			this.weightSumLNSRepair += newWeight;
			this.mutationToWeightLNSRepair.put(id, newWeight);
			this.mutationScoresLNSRepair.put(id, 0.0);
			this.mutationToAttemptsLNSRepair.put(id, 1);
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
		this.mutationToDelta.put(Inter2Move.id, (Mutation mutation) -> {
			Inter2Move inter2move = (Inter2Move) mutation;
			return this.individual.deltaFitness(inter2move);
		});
		this.mutationToDelta.put(EjectionInsertMutation.id, (Mutation mutation) -> {
			EjectionInsertMutation ejectionInsertMutation = (EjectionInsertMutation) mutation;
			return this.individual.deltaFitness(ejectionInsertMutation);
		});
		this.mutationToDelta.put(EjectionRemoveMutation.id, (Mutation mutation) -> {
			EjectionRemoveMutation ejectionRemoveMutation = (EjectionRemoveMutation) mutation;
			return this.individual.deltaFitness(ejectionRemoveMutation);
		});
		this.mutationToDelta.put(EjectionSwapMutation.id, (Mutation mutation) -> {
			EjectionSwapMutation ejectionSwapMutation = (EjectionSwapMutation) mutation;
			return this.individual.deltaFitness(ejectionSwapMutation);
		});
		this.mutationToDelta.put(IntraSwap.id, (Mutation mutation) -> {
			IntraSwap intraSwap = (IntraSwap) mutation;
			return this.individual.deltaFitness(intraSwap);
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
		this.mutationToPerform.put(Inter2Move.id, (Mutation mutation) -> {
			Inter2Move inter2Move = (Inter2Move) mutation;
			this.individual.performMutation(inter2Move);
		});
		this.mutationToPerform.put(EjectionInsertMutation.id, (Mutation mutation) -> {
			EjectionInsertMutation ejectionInsertMutation = (EjectionInsertMutation) mutation;
			this.individual.performMutation(ejectionInsertMutation);
		});
		this.mutationToPerform.put(EjectionRemoveMutation.id, (Mutation mutation) -> {
			EjectionRemoveMutation ejectionRemoveMutation = (EjectionRemoveMutation) mutation;
			this.individual.performMutation(ejectionRemoveMutation);
		});
		this.mutationToPerform.put(EjectionSwapMutation.id, (Mutation mutation) -> {
			EjectionSwapMutation ejectionSwapMutation = (EjectionSwapMutation) mutation;
			this.individual.performMutation(ejectionSwapMutation);
		});
		this.mutationToPerform.put(IntraSwap.id, (Mutation mutation) -> {
			IntraSwap intraSwap = (IntraSwap) mutation;
			this.individual.performMutation(intraSwap);
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
		this.mutationToNeighborhood.put(Inter2Move.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodInter2Move(this.tabuList, tabuSize);
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
		this.mutationToNeighborhood.put(EjectionSwapMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodEjectionSwap(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhood.put(IntraSwap.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodIntraSwap(this.tabuList, tabuSize);
		});

	}

	private void setMutationToGenerateNeighborhoodFull() {
		this.mutationToNeighborhoodFull = new HashMap<>();
		this.mutationToNeighborhoodFull.put(IntraMove.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodIntraMoveFull(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhoodFull.put(InterMove.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodInterMoveFull(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhoodFull.put(InterSwap2.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodInterSwap2Full(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhoodFull.put(Inter2Move.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodInter2MoveFull(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhoodFull.put(EjectionInsertMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodEjectionInsertFull(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhoodFull.put(EjectionRemoveMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodEjectionRemoveFull(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhoodFull.put(EjectionReplaceMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodEjectionReplaceFull(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhoodFull.put(EjectionSwapMutation.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodEjectionSwapFull(this.tabuList, tabuSize);
		});
		this.mutationToNeighborhoodFull.put(IntraSwap.id, (int tabuSize) -> {
			return this.individual.getNeighborhoodIntraSwapFull(this.tabuList, tabuSize);
		});
	}

	//LNS
	private void setSearchToPerformDestroy(){
		this.searchToPerformDestroy = new HashMap<>();
		this.searchToPerformDestroy.put(RandomDestroy.id, (Search search) ->{
			RandomDestroy randomDestroy = (RandomDestroy) search;
			this.individual.destroy(randomDestroy, this.tabuList, this.numberToHandle);
		});
		this.searchToPerformDestroy.put(RelatedDestroy.id, (Search search) ->{
			RelatedDestroy relatedDestroy = (RelatedDestroy) search;
			this.individual.destroy(relatedDestroy, this.tabuList, this.numberToHandle);
		});
		this.searchToPerformDestroy.put(WorstDestroy.id, (Search search) ->{
			WorstDestroy worstDestroy = (WorstDestroy) search;
			this.individual.destroy(worstDestroy, this.tabuList, this.numberToHandle);
		});
	}

	private void setSearchToPerformRepair(){
		this.searchToPerformRepair = new HashMap<>();
		this.searchToPerformRepair.put(BestRepair.id, (Search search) ->{
			BestRepair bestRepair = (BestRepair) search;
			this.individual.repair(bestRepair, this.tabuList, this.numberToHandle);
		});
		this.searchToPerformRepair.put(RegretRepair.id, (Search search) ->{
			RegretRepair regretRepair = (RegretRepair) search;
			this.individual.repair(regretRepair, this.tabuList, this.numberToHandle);
		});
		this.searchToPerformRepair.put(RegretRepair2.id, (Search search) ->{
			RegretRepair2 regretRepair2 = (RegretRepair2) search;
			this.individual.repair(regretRepair2, this.tabuList, this.numberToHandle);
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

	//LNS
	private interface Destroy {
		void runCommand(Search search);
	}

	private interface Repair {
		void runCommand(Search search);
	}

	
	private void setBest(){
		double currentTime;
		ArrayList<ArrayList<CarMove>> newOperators = new ArrayList<>();
		ArrayList<Double> endTimes = new ArrayList<>();
		for(Object object : individual.getRepresentation()){
			Operator operator = (Operator) object;
			ArrayList<CarMove> newCarMoveList = new ArrayList<>();
			currentTime = operator.getStartTime();
			Node prevNode = operator.getStartNode();
			for(CarMove carMove : operator.getCarMoveCopy()) {
				//Need to take earliest start time of the move into account
				currentTime += individual.getProblemInstance().getTravelTimeBike(prevNode, carMove.getFromNode());
				currentTime += carMove.getTravelTime();
				if (currentTime > Constants.TIME_LIMIT_STATIC_PROBLEM) {
					currentTime += - individual.getProblemInstance().getTravelTimeBike(prevNode, carMove.getFromNode()) - carMove.getTravelTime();
					break;
				} else {
					newCarMoveList.add(carMove);
				}
				prevNode = carMove.getToNode();
			}
			newOperators.add(newCarMoveList);
			endTimes.add(currentTime);
		}
		
		this.individual.calculateMoselFitness();
		this.bestIndividual = new BestIndividual(newOperators, individual.getFitness(), endTimes,
				individual.getNumberOfUnchargedCars(), individual.getDeviationFromIdeal());
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

	@Override
	public ArrayList<String> getResults() {
		DecimalFormat df = 	new DecimalFormat("#.##");
		String bestSolution = df.format(-bestIndividual.getFitness());
		String timeUsed = Double.toString(this.timeUsed);
		String deviation = "" + bestIndividual.getDeviationFromIdeal();
		String uncharged = "" + bestIndividual.getNumberOfUnchargedCars();
		
		return new ArrayList<String>(){{
			add(bestSolution);
			add("N/A");
			add("N/A");
			add(timeUsed);
			add(deviation);
			add(uncharged);
		}};
	}
}
