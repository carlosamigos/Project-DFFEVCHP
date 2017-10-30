package tio4500;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DynamicProblem {

	static int noOfSubproblems;
	
	public DynamicProblem(int noOfSubproblems) {
		this.noOfSubproblems = noOfSubproblems;
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
