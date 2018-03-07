package code.solver.heuristics.mutators;

import java.util.ArrayList;

import code.solver.heuristics.Individual;

public class Insert extends Mutation{

	public final static int id = 3;
	
	private final int i;
	private final Object o;
	private final int hashType = 3;
	private final int hashCode;

	public Insert(int i, Object o) {
		this.i = i;
		this.o = o;
		String hashString = hashType + "" + i;
		hashCode = Integer.parseInt(hashString);

	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof  Insert){
			return o.hashCode() == this.hashCode;
		} return false;
	}

	public int getIndex() {
		return this.i;
	}

	public int getId() {
		return id;
	}
	
	public Object getObject() {
		return this.o;
	}

	@Override
	public int hashCode() {
	    return hashCode;
	}

	@Override
	public String toString() {
		return "Insert: " + i;
	}
}

