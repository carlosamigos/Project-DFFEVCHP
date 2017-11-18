package tests;

import constants.Constants;
import constants.Constants.SolverType;
import tio4500.KPITrackerStatic;
import tio4500.StaticProblem;
import tio4500.solvers.Solver;
import utils.StringUtils;

public class StaticTestSuite extends TestSuite {
	
	public StaticTestSuite(SolverType solverType) {
		super(solverType, Constants.TEST_DYNAMIC_INITIAL_FOLDER, Constants.STATIC_TEST_SUITE_RESULTS_FILE);
	}
	
	public void runTestSuite() {
		System.out.println("\nStarting static test suite...");
		System.out.println("Number of test files: " + testFileNames.size());
		System.out.println("Estimated running time (hours): " + 
							(Constants.MAX_SOLVE_TIME_MOSEL_SECONDS*this.testFileNames.size()*
							this.solvers.size()/3600) + "\n");
		
		for(Solver solver : this.solvers) {
			writeTestHeader(solver.getInfo());
			System.out.println("Running tests with " + solver.getInfo());
			
			for(String testName : testFileNames) {
				KPITrackerStatic tracker = new KPITrackerStatic();
				StaticProblem staticProblem = new StaticProblem(Constants.TEST_STATIC_FOLDER + testName);
				solver.solve(staticProblem);
				tracker.setResults(staticProblem.getFilePath());
				writeTestResult(tracker);
			}
			
			System.out.println("\n");
			fh.writeFile("\n\n");
		}
		
		System.out.println("\nDone with all tests. See the file " + Constants.STATIC_TEST_SUITE_RESULTS_FILE + " for results.");
	}
	
	private void writeTestHeader(String fileName) {
		String data = "Solver: " + fileName + "\n";
		
		String name = StringUtils.center("Test", 30);
		String time = StringUtils.center("Time", 10);
		String value = StringUtils.center("Value", 10);
		String gap = StringUtils.center("Gap", 10);
		String headerLine = name + "|" + time + "|" + value + "|" + gap;
		fh.writeFile(data + headerLine);

	}
	
	private void writeTestResult(KPITrackerStatic tracker) {
		String[] namePath = tracker.getName().split("/");
		String data = "\n" + StringUtils.center(namePath[namePath.length - 1], 30);
		data += "|";
		data += StringUtils.center(tracker.getTimeUsed() + "s.", 10);
		data += "|";
		data += StringUtils.center(tracker.getBestSolution(),10);
		data += "|";
		data += StringUtils.center(tracker.getGap() + "%", 10);
		
		fh.writeFile(data);
	}


}	
