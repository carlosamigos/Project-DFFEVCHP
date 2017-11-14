package tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import constants.Constants;
import constants.Constants.SolverType;
import tio4500.KPITrackerStatic;
import tio4500.StaticProblem;
import tio4500.solvers.MoselSolver;
import tio4500.solvers.Solver;
import utils.StringUtils;

public class StaticTestSuite {

	private final ArrayList<String> testFilesNames;
	private BufferedWriter bw;
	private FileWriter fw;
	private File resultsFile;
	private ArrayList<Solver> solvers;
	
	public StaticTestSuite(SolverType solverType) {
		instantiateSolvers(solverType);
		
		File[] testFiles = (new File(Constants.TEST_FOLDER)).listFiles();
		this.testFilesNames = (ArrayList<String>) Arrays.stream(testFiles).map(
				file -> StringUtils.removeFileEnding(file.getName()))
				.collect(Collectors.toList());
	}
	
	public void runTestSuite() {
		System.out.println("Starting static suite...");
		System.out.println("Number of test files: " + testFilesNames.size());
		System.out.println("##########################################\n");
		
		openResultsFile();		
		
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
			writeResultsFile("\n\n");
		}
		closeResultsFile();
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
		writeResultsFile(data);
		
		String name = StringUtils.center("Test", 30);
		String time = StringUtils.center("Time", 8);
		String value = StringUtils.center("Value", 10);
		String gap = StringUtils.center("Gap", 8);
		String headerLine = name + "|" + time + "|" + value + "|" + gap;
		writeResultsFile(headerLine);
	}
	
	private void openResultsFile() {
		this.bw = null;
		this.fw = null;
		
		try {
			resultsFile = new File(Constants.STATIC_TEST_SUITE_RESULTS_FILE);

			// if file doesnt exists, then create it
			if (!resultsFile.exists()) {
				resultsFile.createNewFile();
			} 

			// true = append file
			fw = new FileWriter(resultsFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	private void closeResultsFile() {
		try {
			if (bw != null)
				bw.close();

			if (fw != null)
				fw.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeResultsFile(String data) {
		try {
			this.bw.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeTestResult(KPITrackerStatic tracker) {
		String[] namePath = tracker.getName().split("/");
		String data = "\n" + StringUtils.center(namePath[namePath.length - 1], 30);
		data += "|";
		data += StringUtils.center(tracker.getTimeUsed() + "s.", 8);
		data += "|";
		data += StringUtils.center(tracker.getBestSolution(),10);
		data += "|";
		data += StringUtils.center(tracker.getGap() + "%", 10);
		
		writeResultsFile(data);
	}


}	
