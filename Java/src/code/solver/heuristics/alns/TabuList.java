package code.solver.heuristics.alns;

import java.util.ArrayList;
import java.util.HashSet;

import code.solver.heuristics.mutators.Mutation;

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
		this.tabuSize *= 2;
	}
	
	public void decreaseSize() {
		this.tabuSize = Math.max(2, this.tabuSize/2);
		while(tabuQueue.size() > tabuSize) {
			Mutation pop = tabuQueue.remove(tabuSize);
			tabuSet.remove(pop);
		}
	}

	public void clearTabu(){
		this.tabuSet = new HashSet<>();
		this.tabuQueue = new ArrayList<>();
		this.tabuSize = 2;
	}


	@Override
	public String toString() {
		return tabuQueue.toString();
	}




}
