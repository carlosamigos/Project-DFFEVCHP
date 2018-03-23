package code.solver;

import code.problem.ProblemInstance;
import code.solver.heuristics.Individual;

public abstract class Solver {
	public abstract Individual solve(ProblemInstance problemInstance);
	public abstract String getInfo();

}
