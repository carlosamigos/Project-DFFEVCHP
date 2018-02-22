package code.solver.heuristics.tabusearch;

import java.util.HashMap;
import java.util.Queue;

import code.solver.heuristics.mutators.Mutation;

public class TabuList {
	HashMap<Mutation, Boolean> tabuMap;
	Queue<Mutation> tabuQueue;
}
