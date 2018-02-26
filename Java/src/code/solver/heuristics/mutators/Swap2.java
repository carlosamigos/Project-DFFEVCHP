package code.solver.heuristics.mutators;

import code.solver.heuristics.Individual;

import java.util.ArrayList;
import java.util.Collections;

public class Swap2 extends Mutation{

	public final static int id = 1;
	
	private final int i;
	private final int j;
	private final int hashType = 1;
	private final int hashCode;

	public Swap2(int i, int j) {
		this.i = i;
		this.j = j;
		String hashString = hashType + ((i <= j) ? ("" + i + j) : ("" + j + i));
		hashCode = Integer.parseInt(hashString);

	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof  Swap2){
			return ((Swap2) o).hashCode() == this.hashCode;
		} return false;
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}
	
	public int getId() {
		return id;
	}

	@Override
	public int hashCode() {
	    return hashCode;
	}

	/*
	public double deltaFitness(Individual individual) {
		return individual.deltaFitness(this);
	}
	*/

	@Override
	public void doMutation(Individual individual) {
		ArrayList<Object> rep = individual.getRepresentation();
		Collections.swap(rep, i,j);
	}

	@Override
	public String toString() {
		return "[" + i + ", "+ j + "]";
	}

}
