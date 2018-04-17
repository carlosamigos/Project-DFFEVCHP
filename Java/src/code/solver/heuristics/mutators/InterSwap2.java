package code.solver.heuristics.mutators;

import code.solver.heuristics.entities.Operator;

public class InterSwap2 extends Mutation{

	public final static int id = 4;
	
	private final int index1;
	private final int index2;
	private final Operator operator1;
	private final Operator operator2;
	private final int hashCode;

	public InterSwap2(int index1, int index2, Operator operator1, Operator operator2) {
		this.index1 = index1;
		this.index2 = index2;
		this.operator1 = operator1;
		this.operator2 = operator2;
		String hashString = id + ((operator1.id < operator2.id) ? ("" + operator1.id + operator2.id) : ("" + operator2.id + operator1.id)) +
				((index1 <= index2) ? ("" + index1 + index2) : ("" + index2 + index1));
		hashCode = Integer.parseInt(hashString);

	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof  InterSwap2){
			return ((InterSwap2) o).hashCode() == this.hashCode;
		} return false;
	}

	public int getIndex1() {
		return index1;
	}

	public int getIndex2() {
		return index2;
	}
	
	public Operator getOperator1() {
		return operator1;
	}
	
	public Operator getOperator2() {
		return operator2;
	}
	
	public int getId() {
		return id;
	}

	@Override
	public int hashCode() {
	    return hashCode;
	}

	@Override
	public String toString() {
		return "[(Operator 1: " + operator1 + ", Index: " + index1 + "), (Operator 2: " + operator2 + ", Index: " + index2 + ")]";
	}

}
