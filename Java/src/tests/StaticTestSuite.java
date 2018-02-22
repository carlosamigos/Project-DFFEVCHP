package tests;

import constants.Constants;
import constants.Constants.SolverType;
import tio4500.KPITrackerStatic;
import tio4500.StaticProblem;
import tio4500.solvers.Solver;
import utils.StringUtils;

public class StaticTestSuite extends TestSuite {
	
	public StaticTestSuite(SolverType solverType) {
		super(solverType, Constants.TEST_STATIC_FOLDER, Constants.STATIC_TEST_SUITE_RESULTS_FILE);
	}
	
	public void runTestSuite() {
		
		System.out.println("\nStarting static test suite...");
		System.out.println("Number of test files: " + testFileNames.size());
		int runsLeft = this.solvers.size() * this.testFileNames.size();
		double timePerRun = calcTimePerRun();
		
		for(Solver solver : this.solvers) {
			writeTestHeader(solver.getInfo());
			System.out.println("Running tests with " + solver.getInfo());
			
			for(String testName : testFileNames) {
				printEstimatedTimeLeft(timePerRun, runsLeft);
				KPITrackerStatic tracker = new KPITrackerStatic();
				StaticProblem staticProblem = new StaticProblem(Constants.TEST_STATIC_FOLDER + testName);
				solver.solve(staticProblem);
				tracker.setResults(staticProblem.getFilePath());
				writeTestResult(tracker);
				runsLeft--;
			}
			
			System.out.println("\n");
			fh.writeFile("\n\n");
		}
		
		System.out.println("\nDone with all tests. See the file " + Constants.STATIC_TEST_SUITE_RESULTS_FILE 
				+ this.timeStamp + " for results.");
	}
	
	private void writeTestHeader(String fileName) {
		String data = "Solver: " + fileName + "\n";
		
		String name = StringUtils.center("Test", 60);
		String time = StringUtils.center("Time", 10);
		String value = StringUtils.center("Value", 10);
		String gap = StringUtils.center("Gap", 10);
		String rdev = StringUtils.center("rDev", 10);
		String cdev = StringUtils.center("cDev", 10);
		String headerLine = name + "|" + time + "|" + value + "|" + gap + "|" + rdev + "|" + cdev;
		fh.writeFile(data + headerLine);

	}
	
	private void writeTestResult(KPITrackerStatic tracker) {
		String[] namePath = tracker.getName().split("/");
		String data = "\n" + StringUtils.center(namePath[namePath.length - 1], 60);
		data += "|";
		data += StringUtils.center(tracker.getTimeUsed() + "s.", 10);
		data += "|";
		data += StringUtils.center(tracker.getBestSolution(),10);
		data += "|";
		data += StringUtils.center(tracker.getGap() + "%", 10);
		data += "|";
		data += StringUtils.center(tracker.getRDev(), 10);
		data += "|";
		data += StringUtils.center(tracker.getCDev(), 10);
		
		fh.writeFile(data);
	}
	
	@Override
	protected double calcTimePerRun() {
		double timePerRun = Constants.MAX_SOLVE_TIME_MOSEL_SECONDS;
		return timePerRun;
		
	}
}	
