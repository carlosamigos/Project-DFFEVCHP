package code.solver.heuristics.mutators;

import code.solver.heuristics.entities.Operator;

public class IntraMove extends Mutation {

	public final static int id = 1;

	private final Operator operator;
	private final int removeIndex;
	private final int insertIndex;
	private final int hashCode;


	public IntraMove(Operator operator, int removeIndex , int insertIndex) {
		this.operator = operator;
		this.removeIndex = removeIndex;
		this.insertIndex = insertIndex;
		String hashString = id + "" + operator.id
				+((removeIndex <= insertIndex) ? (removeIndex +" " + insertIndex) : ( insertIndex +"" + removeIndex));
		this.hashCode = Integer.parseInt(hashString);
	}

	public Operator getOperator() {
		return operator;
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
