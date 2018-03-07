package code.solver.heuristics.mutators;

import code.solver.heuristics.Individual;
import code.solver.heuristics.entities.Operator;

public class IntraMove extends Mutation {

	public final static int id = 1;

	private final Operator operator;
	private final int i;
	private final int j;
	private final int hashType = 2;
	private final int hashCode;


	public IntraMove(Operator operator, int i, int j, int hashCode) {
		this.operator = operator;
		this.i = i;
		this.j = j;
		String hashString = hashType + ((i <= j) ? ("" + i + j) : ("" + j + i));
		this.hashCode = Integer.parseInt(hashString);
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof IntraMove){
			return ((IntraMove) o).hashCode() == this.hashCode;
		}return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return hashCode;
	}

	@Override
	public String toString() {
		return "Operator "+operator.id +"[" + i + ", "+ j + "]";
	}

}
