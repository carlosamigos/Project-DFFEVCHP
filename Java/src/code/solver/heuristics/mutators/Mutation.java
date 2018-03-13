package code.solver.heuristics.mutators;

public abstract class Mutation {
	public abstract int getId();
	public abstract boolean equals(Object o);
	public abstract int hashCode();
	//public abstract double deltaFitness(Individual individual);
	public int compareTo(Mutation newCandidate) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
}
