package code.solver.heuristics.mutators;

import code.solver.heuristics.entities.Operator;

public class IntraMove extends Mutation {

	public final static int id = 1;

	private final Operator operator;
	private final int removeIndex;
	private final int insertIndex;
	private final int hashType = 2;
	private final int hashCode;


	public IntraMove(Operator operator, int removeIndex , int insertIndex, int hashCode) {
		this.operator = operator;
		this.removeIndex = removeIndex;
		this.insertIndex = insertIndex;
		String hashString = hashType + ((removeIndex <= insertIndex) ? ("" + removeIndex + insertIndex) : ("" + insertIndex + removeIndex));
		this.hashCode = Integer.parseInt(hashString);
	}

	public Operator getOperator() {
		return operator;
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
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
	
	public Operator getOperator() {
		return this.operator;
	}
	
	public int getRemoveIndex() {
		return this.removeIndex;
	}
	
	public int getInsertIndex() {
		return this.insertIndex;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return hashCode;
	}

	@Override
	public String toString() {
		return "Operator "+ operator.id +" [" + removeIndex + ", "+ insertIndex + "]";
	}

}
