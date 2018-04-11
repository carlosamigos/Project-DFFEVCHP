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
	
	private HashMap<Integer, DeltaFitness> mutationToDelta;
	private HashMap<Integer, Perform> mutationToPerform;
	private HashMap<Integer, GenerateNeighborhood> mutationToNeighborhood;
	
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

	
	public ALNSSolver() {}
	
	public ALNSSolver(ProblemInstance problemInstance) {
		setVariables(problemInstance);
	}
	
	private void setVariables(ProblemInstance problemInstance) {
		this.iterations = HeuristicsConstants.TABU_ITERATIONS;
		this.individual =  new ALNSIndividual(problemInstance);
		setBest();
		this.tabuSize = HeuristicsConstants.TABU_SIZE;
		this.solutionsSeen = new HashMap<>();
		this.initializeMutationIdToDescription();
		this.initializeMutationWeights();
		this.setMutationToDelta();
		this.setMutationToPerform();
		this.setMutationToGenerateNeighborhood();
		this.solutionsSeen.put(this.individual.toString(), 1);
		this.set = true;
		//LNS
		this.setSearchToPerformDestroy();
		this.setSearchToPerformRepair();
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
				IntraMove.id
				//Inter2Move.id

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
				RegretRepair.id
				//RegretRepair2.id
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
	}

	
	@Override
	public Individual solve(ProblemInstance problemInstance) {
		if(!set) {
			setVariables(problemInstance);
		}
//		System.out.println(bestIndividual);
		this.startTime = System.currentTimeMillis();
		this.endTime = this.startTime + HeuristicsConstants.ALNS_MAX_TIME_SECONDS * 1000;
		this.tabuList = new TabuList(this.tabuSize);
		int iteration = 0;
		int counter = 0;        // counts number of rounds with delta > 0
		int global_counter = 0; // counts number of iterations since new global best

		//LNS
		int neighborhoodDestroyId = 0;
		int neighborhoodRepairId = 0;

		while(!done(iteration)) {
			if(iteration != 0 && iteration % 100 == 0){
				if(HeuristicsConstants.PRINT_OUT_PROGRESS) {
					System.out.println("\nIteration: " + iteration + " Best fitness: "
							+ String.format("%.1f", this.bestIndividual.getFitness()) + ", Current fitness:"
							+ String.format("%.1f", this.individual.getFitness()));
				}
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
			if(this.individual.getFitness() < this.bestIndividual.getFitness()) {
				bestFound = true;
				setBest();
			} else {
				global_counter++;
			}

			updateMutationScores(candidate, candidateDelta, bestFound);
			tabuList.add(candidate);
			iteration++;

			// LNS - DESTROY AND REPAIR
			if(neighborhoodDestroyId != 0){
				updateMutationScoresLNSDestroyAndRepair(neighborhoodDestroyId, neighborhoodRepairId, bestFound);
			}
			if(global_counter > HeuristicsConstants.TABU_MAX_NON_IMPROVING_ITERATIONS_DESTROY){
				this.tabuList.clearTabu();
				updateWeightsLNSDestroy();
				updateWeightsLNSRepair();
				neighborhoodDestroyId = getNeighborhoodLNSDestroy();
				neighborhoodRepairId = getNeighborhoodLNSRepair();
				this.numberToHandle = (int) (this.individual.getTotalNumberOfCarMoves() * HeuristicsConstants.ALNS_DESTROY_FACTOR);
				//Destroy
				this.searchToPerformDestroy.get(neighborhoodDestroyId).runCommand(searchToNeighborhood.get(neighborhoodDestroyId));
				//Repair
				this.searchToPerformRepair.get(neighborhoodRepairId).runCommand(searchToNeighborhood.get(neighborhoodRepairId));
				//destroyAndRepair();
				global_counter = 0;
				counter = 0;
				individual.calculateFitness();
			}
		}
		this.timeUsed = (System.currentTimeMillis() - this.startTime)/1000;
		SolutionFileMaker.writeSolutionToFile(bestIndividual, problemInstance, problemInstance.getFileName() + ".txt");
		return bestIndividual;
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

	//LNS
	public void updateMutationScoresLNSDestroyAndRepair(int destroyId, int repairId, boolean bestFound){
		String individualString = this.individual.toString();
		if(! this.solutionsSeen.containsKey(individualString)) {
			this.mutationScoresLNSDestroy.put(destroyId, this.mutationScoresLNSDestroy.get(destroyId)
					+ HeuristicsConstants.ALNS_FOUND_NEW_SOLUTION_LNS);
			this.mutationScoresLNSRepair.put(repairId, this.mutationScoresLNSRepair.get(repairId)
					+ HeuristicsConstants.ALNS_FOUND_NEW_SOLUTION_LNS);
		}if(bestFound){
			this.mutationScoresLNSDestroy.put(destroyId, this.mutationScoresLNSDestroy.get(destroyId)
					+ HeuristicsConstants.ALNS_FOUND_NEW_GLOBAL_BEST_REWARD_LNS);
			this.mutationScoresLNSRepair.put(repairId, this.mutationScoresLNSRepair.get(repairId)
					+ HeuristicsConstants.ALNS_FOUND_NEW_GLOBAL_BEST_REWARD_LNS);
		}
	}

	/*
		* Destroy and repair
		* Destroy by removing a % proportion of the current solution, at random.
	 */
	private void destroyAndRepair(){
		int numberToHandle = (int) (this.individual.getTotalNumberOfCarMoves() * HeuristicsConstants.ALNS_DESTROY_FACTOR);
		destroy(numberToHandle);
		repair(numberToHandle);
		//initializeMutationWeights();
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
			for (int j = 0; j < index; j++) {
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

	private HashMap<Mutation, Integer> getNeighborhoodRemove(){
		return this.mutationToNeighborhood.get(EjectionRemoveMutation.id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE*3);
	}

	private HashMap<Mutation, Integer> getNeighborhoodInsert(){
		return this.mutationToNeighborhood.get(EjectionInsertMutation.id).runCommand(HeuristicsConstants.TABU_NEIGHBORHOOD_SIZE*3);
	}
	
	@Override
	public String getInfo() {
		return "ALNS search";
	}

	private boolean done(int iteration) {
		return iteration >= iterations || System.currentTimeMillis() > this.endTime;
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
			double newWeight = Math.max(oldWeight * (1 - r) + r * (score / attempts), 1.0);
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
			double newWeight = Math.max(oldWeight * (1 - r) + r * (score / Math.max(attempts, 1)), 1);
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
			double newWeight = Math.max(oldWeight * (1 - r) + r * (score / Math.max(attempts, 1)), 1);
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
		String timeUsed = df.format(this.timeUsed);
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
