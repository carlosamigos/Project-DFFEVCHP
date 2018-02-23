package code.solver.heuristics.tabusearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

import code.solver.heuristics.Individual;
import code.solver.heuristics.mutators.Mutation;
import code.solver.heuristics.mutators.Swap2;

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


	@Override
	public String toString() {
		return tabuQueue.toString();
	}




}
