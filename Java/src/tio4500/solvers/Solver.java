package tio4500.solvers;

import java.util.HashMap;

import tio4500.StaticProblem;

public abstract class Solver {
	protected HashMap<String, String> results;
	public abstract void solve(StaticProblem problem);
	public abstract HashMap<String,String> getResults();
	public abstract String getInfo();
	
}
