package tests;

import constants.Constants;
import constants.Constants.SolverType;
import tio4500.DynamicProblem;
import tio4500.KPITrackerDynamic;
import tio4500.ProblemInstance;
import tio4500.SimulationModel;
import tio4500.solvers.Solver;
import utils.StringUtils;

public class DynamicTestSuite extends TestSuite{
	private final int days;
	
	public DynamicTestSuite(SolverType solverType, int days) {
		super(solverType, Constants.TEST_FOLDER, Constants.DYNAMIC_TEST_SUITE_RESULTS_FILE);
		this.days = days;
	}
	
	public void runTestSuite() {
		System.out.println("Starting dynamic test suite...");
		System.out.println("Number of test files: " + testFileNames.size() + "\n");
		double estimatedTime = (Constants.END_TIME - Constants.START_TIME)/60 - 1;
		estimatedTime *= (60/Constants.TIME_INCREMENTS);
		estimatedTime *= (Constants.MAX_SOLVE_TIME_MOSEL_SECONDS/3600);
		estimatedTime *= this.testFileNames.size()*this.solvers.size();
		System.out.println("Estimated running time (hours): " + estimatedTime);
		
		for(String test : testFileNames) {
			
			System.out.println("Solving " + test);
			ProblemInstance cleanProblemInstance = new ProblemInstance(test);
			
			for (int day = 0; day < days; day++) {
				
				// Simulate demand requests during day
				SimulationModel simModel = new SimulationModel(day, cleanProblemInstance);
				simModel.createNewDaySimulationModel(); // I konstruktøren til SimulationModel?
				simModel.saveDaySimulationModel();
				//Solve for each solver
				
				for(Solver solver : this.solvers) {
					System.out.println("Using " + solver.getInfo());
					// Read clean demand requests
					ProblemInstance problemInstance = new ProblemInstance(test);
					SimulationModel solverSimulationModel = new SimulationModel(day, problemInstance);
					solverSimulationModel.readSimulationModelFromFile(); // Burde vært i konstruktøren til SimulationModel?
					writeTestHeader(solver.getInfo());
					System.out.println("Running tests with " + solver.getInfo() + " on day " + day + " on test set " + test);
					DynamicProblem problem = new DynamicProblem(problemInstance, solverSimulationModel, solver);
					problem.solve();
					
					// Results:
					KPITrackerDynamic tracker = problem.getKpiTrackerDyanmic();
					writeTestResult(tracker);
				}
				System.out.println("\n");
			}
		}
		
		System.out.println("\nDone with all tests. See the file " + Constants.DYNAMIC_TEST_SUITE_RESULTS_FILE + " for results.");
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
	
	private void writeTestResult(KPITrackerDynamic tracker) {
		System.out.println(tracker);
		/*
		String[] namePath = tracker.getName().split("/");
		String data = "\n" + StringUtils.center(namePath[namePath.length - 1], 30);
		data += "|";
		data += StringUtils.center(tracker.getTimeUsed() + "s.", 10);
		data += "|";
		data += StringUtils.center(tracker.getBestSolution(),10);
		data += "|";
		data += StringUtils.center(tracker.getGap() + "%", 10);
		
		fh.writeFile(data);
		*/
	}
}
