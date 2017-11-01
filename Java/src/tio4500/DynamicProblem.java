package tio4500;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DynamicProblem {

	static int noOfSubproblems;
	
	public DynamicProblem(int noOfSubproblems) {
		this.noOfSubproblems = noOfSubproblems;

		//TODO: 1. Get initial state

		//TODO: 2. Repeat for all steps during day:

			//TODO: 2.1. Use Solver

			//TODO: 2.2. Get Operator Paths

			//TODO: 2.3. Simulate one step ahead:

				//TODO: 2.3.1. Operators travel, cars are picked up and delivered by customers

				//TODO: 2.3.2. Some cars may be finished charging

		//TODO: 3. Create new state for Solver


	}
	
	public void solve() {
		for(int subproblemNo = 0; subproblemNo < noOfSubproblems; subproblemNo++) {
			StaticProblem staticProblem = new StaticProblem(this.noOfSubproblems, subproblemNo);
			staticProblem.compile();
			staticProblem.solve();
			//generateNextSubproblem();
		}
	}
	
	public void generateNextSubproblem() {
		throw new NotImplementedException();
	}
}
