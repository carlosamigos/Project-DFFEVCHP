package code.solver;

import java.util.ArrayList;
import java.util.HashMap;
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
	private final int tabuSize;
	
	private HashMap<Integer, DeltaFitness> mutationToDelta;
	private HashMap<Integer, Perform> mutationToPerform;
	
	public TSSolver(ProblemInstance problemInstance) {
		this(HeuristicsConstants.TABU_ITERATIONS, HeuristicsConstants.TABU_SIZE, problemInstance);
	}
	
	public TSSolver(int iterations, int tabuSize, ProblemInstance problemInstance) {
		this.iterations = iterations;
		this.individual =  new TSIndividual(problemInstance);
		this.best = (TSIndividual) DeepCopy.copy(this.individual);
		this.best.setFitness(this.individual.getFitness());
		this.tabuSize = tabuSize;
		this.setMutationToDelta();
		this.setMutationToPerform();
	}
	
	@Override
	public void solve(ProblemInstance problemInstance) {
		int iteration = 0;
		this.tabuList = new TabuList(this.tabuSize);
		while(!done(iteration)) {
			/*System.out.println("Iteration: " + iteration + " Best fitness: " 
					+ String.format("%.1f", this.best.getFitness()) + ", Current fitness:" 
					+ String.format("%.1f", this.individual.getFitness()));
			System.out.println(individual);*/
			Set<Mutation> neighborhood = this.individual.getNeighbors(this.tabuList).keySet();
			Mutation candidate = null;
			for(Mutation mutation : neighborhood) {
				candidate = mutation;
				break;
			}
			
			if(candidate == null) {
				continue;
			}
			
			double candidateDelta = this.mutationToDelta.get(candidate.getId()).runCommand(candidate);
			for(Mutation newCandidate : neighborhood) {
				double newCandidateDelta = this.mutationToDelta.get(newCandidate.getId()).runCommand(newCandidate);
				if (newCandidateDelta < candidateDelta) {
					candidate = newCandidate;
					candidateDelta = newCandidateDelta;
				}
			}

			this.individual.addToFitness(candidateDelta);
			this.mutationToPerform.get(candidate.getId()).runCommand(candidate);
			
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
	
	private interface DeltaFitness {
		double runCommand(Mutation mutation);
	}
	
	private interface Perform {
		void runCommand(Mutation mutation);
	}

	private void cleanBest(){
		for(Object object : best.getOperators()){
			Operator operator = (Operator) object;
			ArrayList<CarMove> newCarMoveList = new ArrayList<>();
			double currentTime = operator.getStartTime();
			Node prevNode = operator.getStartNode();
			for(CarMove carMove : operator.getCarMoveCopy()) {
				//Need to take earliest start time of the move into account
				currentTime += best.getProblemInstance().getTravelTimeBike(prevNode, carMove.getFromNode());
				currentTime += carMove.getTravelTime();
				prevNode = carMove.getToNode();
				if (currentTime > Constants.TIME_LIMIT_STATIC_PROBLEM) {
					break;
				} else {
					newCarMoveList.add(carMove);
				}
			}
			operator.setCarMoves(newCarMoveList);
		}
	}
}
