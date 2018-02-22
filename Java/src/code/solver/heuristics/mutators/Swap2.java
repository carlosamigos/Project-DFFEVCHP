package code.solver.heuristics.mutators;

import code.solver.heuristics.Individual;

public class Swap2 extends Mutation{

	private final int i;
	private final int j;
	private final String hash;
	
	public Swap2(int i, int j) {
		this.i = i;
		this.j = j;
		this.hash = "Swap2" + ((i <= j) ? ("" + i + j) : ("" + j + i));
	}
	
	@Override
	public boolean equals(Object o) {
		return false;
		
	}
	
	@Override
	public int hashCode() {
	    return 0;
	}
	


	@Override
	public Individual doMutation(Individual representation) {
		// TODO Auto-generated method stub
		return null;
	}

}
