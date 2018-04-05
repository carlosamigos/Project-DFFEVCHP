package code.solver;

import java.util.ArrayList;

import code.problem.ProblemInstance;
import code.solver.heuristics.Individual;

public abstract class Solver {
	public abstract Individual solve(ProblemInstance problemInstance);
	public abstract String getInfo();
	public abstract ArrayList<String> getResults();

}
