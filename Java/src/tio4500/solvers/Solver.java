package tio4500.solvers;

import java.util.HashMap;

import tio4500.StaticProblem;

public abstract class Solver {
	public abstract void solve(StaticProblem problem);
	public abstract String getInfo();
	
}
