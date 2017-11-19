package tests;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import constants.Constants;
import constants.Constants.SolverType;
import tio4500.solvers.MoselSolver;
import tio4500.solvers.Solver;
import utils.FileHandler;
import utils.StringUtils;

public abstract class TestSuite {
	public abstract void runTestSuite();
	protected abstract double calcTimePerRun();
	protected final ArrayList<String> testFileNames;
	protected ArrayList<Solver> solvers;
	protected FileHandler fh;
	protected final SolverType solverType;
	
	protected TestSuite(SolverType solverType, String testFolder, String resultFile) {
		this.solverType = solverType;
		instantiateSolvers();
		File[] testFiles = (new File(testFolder)).listFiles(new FileFilter() {
				    @Override
				    public boolean accept(File pathname) {
				        String name = pathname.getName().toLowerCase();
				        return name.endsWith(".txt") && pathname.isFile();
				    }
		});
		this.testFileNames = (ArrayList<String>) Arrays.stream(testFiles).map(
				file -> StringUtils.removeFileEnding(file.getName()))
				.collect(Collectors.toList());
		
		this.fh = new FileHandler(resultFile, true, true);
	}
	
	protected void instantiateSolvers() {
		switch(this.solverType) {
		case MOSEL:
			instantiateMoselSolvers();
			break;
		default:
			instantiateMoselSolvers();
		}
	}
	
	protected void instantiateMoselSolvers() {
		this.solvers = new ArrayList<Solver>();
		File[] moselFiles = (new File(Constants.MOSEL_TEST_FILES_FOLDER)).listFiles();
		ArrayList<String> moselFileNames =  new ArrayList<String>();
		for(File file : moselFiles) {
			if(file.getName().contains(".mos")) {
				moselFileNames.add(file.getName());
			}
		}

		for(String moselFileName : moselFileNames) {
			solvers.add(new MoselSolver(Constants.MOSEL_TEST_FILES_FOLDER + moselFileName));
		}
	}
	
	protected void printEstimatedTimeLeft(Double timePerRun, int runsLeft){
		String hoursAndMinutes = "";
		double timeLeft = timePerRun * runsLeft;
		int totalMinutes = (int)Math.round(timeLeft/60);
		int totalHours = (int)Math.round(totalMinutes/60);
		int minutes = totalMinutes - totalHours*60;
		hoursAndMinutes += totalHours + " hours and "+minutes + " minutes";
		System.out.println("Worst case time left: " + hoursAndMinutes);
	}
}
