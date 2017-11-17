package tests;

import constants.Constants;
import constants.Constants.SolverType;
import tio4500.DynamicProblem;
import tio4500.KPITrackerDynamic;
import tio4500.KPITrackerStatic;
import tio4500.ProblemInstance;
import tio4500.SimulationModel;
import utils.StringUtils;

public class DynamicTestSuite extends TestSuite{
	private final int days;
	
	
	public DynamicTestSuite(SolverType solverType, int days) {
		super(solverType, Constants.TEST_FOLDER, Constants.DYNAMIC_TEST_SUITE_RESULTS_FILE);
		this.days = days;
	}
	
	public void runTestSuite() {
		System.out.println("Starting static suite...");
		System.out.println("Number of test files: " + testFilesNames.size());
		System.out.println("##########################################\n");	
		
		for(int day : days) {
			SimulationModel simModel = new SimulationModel(day, problemInstance)
		
		
			for(Solver solver : this.solvers) {
				
				writeTestHeader(solver.getInfo());
				System.out.println("Running tests with " + solver.getInfo());
				for(String test : testFileNames) {
					SimulationModel model = new SimulationModel(dayNumber, problemInstance)
					ProblemInstance problemInstance = new ProblemInstance(1);
					DynamicProblem problem = new DynamicProblem(problemInstance, simulationModel, type)
					KPITrackerDynamic tracker = new KPITrackerDynamic();
				}
			}
		}
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
