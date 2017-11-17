package tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import constants.Constants;
import constants.Constants.SolverType;
import tio4500.KPITrackerStatic;
import tio4500.StaticProblem;
import tio4500.solvers.MoselSolver;
import tio4500.solvers.Solver;
import utils.FileHandler;
import utils.StringUtils;

public class StaticTestSuite extends TestSuite {

	private final ArrayList<String> testFilesNames;
	private ArrayList<Solver> solvers;
	private FileHandler fh;
	
	public StaticTestSuite(SolverType solverType) {
		instantiateSolvers(solverType);
		
		File[] testFiles = (new File(Constants.TEST_FOLDER)).listFiles();
		this.testFilesNames = (ArrayList<String>) Arrays.stream(testFiles).map(
				file -> StringUtils.removeFileEnding(file.getName()))
				.collect(Collectors.toList());
		
		this.fh = new FileHandler(Constants.STATIC_TEST_SUITE_RESULTS_FILE, true, true);
	}
	
	public void runTestSuite() {
		System.out.println("Starting static suite...");
		System.out.println("Number of test files: " + testFilesNames.size());
		System.out.println("##########################################\n");	
		
		for(Solver solver : this.solvers) {
			writeTestHeader(solver.getInfo());
			System.out.println("Running tests with " + solver.getInfo());
			for(String testName : testFilesNames) {
				KPITrackerStatic tracker = new KPITrackerStatic();
				StaticProblem staticProblem = new StaticProblem(Constants.TEST_FOLDER + testName);
				solver.solve(staticProblem);
				tracker.setResults(staticProblem.getFilePath());
				writeTestResult(tracker);
			}
			System.out.println("\n");
			fh.writeFile("\n\n");
		}
		System.out.println("\nDone with all tests. See the file " + Constants.STATIC_TEST_SUITE_RESULTS_FILE + " for results.");
	}
	
	private void instantiateSolvers(SolverType type) {
		switch(type) {
		case MOSEL:
			instantiateMoselSolvers();
			break;
		default:
			instantiateMoselSolvers();
		}
	}
	
	private void instantiateMoselSolvers() {
		this.solvers = new ArrayList<Solver>();
		File[] moselFiles = (new File(Constants.PROBLEM_FOLDER + Constants.MOSEL_TEST_FILES_FOLDER)).listFiles();
		ArrayList<String> moselFileNames =  new ArrayList<String>();
		for(File file : moselFiles) {
			if(!file.getName().contains(".bim")) {
				moselFileNames.add(file.getName());
			}
		}

		for(String moselFileName : moselFileNames) {
			solvers.add(new MoselSolver(Constants.MOSEL_TEST_FILES_FOLDER + moselFileName));
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
