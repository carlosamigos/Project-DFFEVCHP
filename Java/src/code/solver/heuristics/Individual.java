package code.solver.heuristics;

import java.util.ArrayList;

public abstract class Individual implements Comparable<Object> {
	protected ArrayList<Object> representation;
	public abstract ArrayList<Object> getRepresentation();
	public abstract double getFitness();
}
