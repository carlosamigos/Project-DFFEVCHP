package code.solver.heuristics.alns;

import java.util.ArrayList;
import java.util.HashSet;

import code.solver.heuristics.mutators.Mutation;
import constants.HeuristicsConstants;

public class TabuList {

	private HashSet<Mutation> tabuSet;
	private ArrayList<Mutation> tabuQueue;
	private int tabuSize = 0;

	public TabuList(int tabuSize) {
		this.tabuSet = new HashSet<>();
		this.tabuQueue = new ArrayList<>();
		this.tabuSize = tabuSize;
	}


	public boolean add(Mutation mutation){
		// returns true if move is allowed, false if not
		if(isTabu(mutation)){
			return false;
		} else {
			tabuQueue.add(0, mutation);
			tabuSet.add(mutation);
			if(tabuQueue.size() > tabuSize){
				Mutation pop = tabuQueue.remove(tabuSize);
				tabuSet.remove(pop);
			}
			return true;
		}
	}
	
	public boolean isTabu(Mutation mutation) {
		return tabuSet.contains(mutation); 
	}
	
	public void increaseSize() {
		this.tabuSize = Math.min(this.tabuSize *= 4, HeuristicsConstants.TABU_MAX_SIZE);
	}
	
	public void decreaseSize() {
		this.tabuSize = Math.max(HeuristicsConstants.TABU_SIZE, this.tabuSize/2 );
		while(tabuQueue.size() > tabuSize) {
			Mutation pop = tabuQueue.remove(tabuSize);
			tabuSet.remove(pop);
		}
	}

	public void clearTabu(){
		this.tabuSet = new HashSet<>();
		this.tabuQueue = new ArrayList<>();
		this.tabuSize = HeuristicsConstants.TABU_SIZE;
	}

	public int getTabuSize(){
		return this.tabuSize;
	}


	@Override
	public String toString() {
		return tabuQueue.toString();
	}




}
