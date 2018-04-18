package tests;

import java.io.File;
import java.io.FileFilter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import code.solver.ALNSSolver;
import code.solver.MoselSolver;
import code.solver.Solver;
import constants.Constants.SolverType;
import constants.FileConstants;
import utils.FileHandler;
import utils.StringUtils;

public abstract class TestSuite {
	public abstract void runTestSuite();
	protected abstract double calcTimePerRun();
	protected final ArrayList<String> testFileNames;
	protected ArrayList<Solver> solvers;
	protected FileHandler fh;
	protected final SolverType solverType;
	protected final String timeStamp;
	
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
		Arrays.sort(testFiles);
		for(File f : testFiles) {
			System.out.println(f.getName());
		}
		this.testFileNames = (ArrayList<String>) Arrays.stream(testFiles).map(
				file -> StringUtils.removeFileEnding(file.getName()))
				.collect(Collectors.toList());
		
		this.timeStamp = getTimestamp();
		this.fh = new FileHandler(resultFile + this.timeStamp + "_" + this.getSolverType(), true, true);
	}
	
	protected void instantiateSolvers() {
		this.solvers = new ArrayList<Solver>();
		switch(this.solverType) {
		case MOSEL:
			instantiateMoselSolvers();
			break;
		case ALNS:
			instantiateALNSSolvers();
			break;
		default:
			instantiateMoselSolvers();
		}
	}
	
	protected void instantiateMoselSolvers() {
		File[] moselFiles = (new File(FileConstants.MOSEL_TEST_FILES_FOLDER)).listFiles();
		System.out.println(FileConstants.MOSEL_TEST_FILES_FOLDER);
		ArrayList<String> moselFileNames =  new ArrayList<String>();
		for(File file : moselFiles) {
			if(file.getName().contains(".mos")) {
				moselFileNames.add(file.getName());
			}
		}

		for(String moselFileName : moselFileNames) {
			solvers.add(new MoselSolver(FileConstants.MOSEL_TEST_FILES_FOLDER + moselFileName));
		}
	}
	
	protected void instantiateALNSSolvers() {
		solvers.add(new ALNSSolver());
	}
	
	protected void printEstimatedTimeLeft(Double timePerRun, int runsLeft){
		String hoursAndMinutes = "";
		double timeLeft = timePerRun * runsLeft;
		int totalMinutes = (int)Math.round(timeLeft/60);
		int totalHours = (int)Math.round(totalMinutes/60);
		int minutes = totalMinutes - totalHours*60;
		hoursAndMinutes += totalHours + " hours and "+minutes + " minutes";
		System.out.println("(Time left: " + hoursAndMinutes + ")");
	}
	
	protected String getTimestamp() {
		LocalTime time = LocalTime.now();
		LocalDate date = LocalDate.now();
		return date.getDayOfMonth() + "_" + date.getMonth() + "-" + time.getHour() + "_" + time.getMinute() +
				"_" + time.getSecond();
	}
	
	protected String getSolverType() {
		switch(this.solverType) {
		case MOSEL:
			return "mosel";
		case ALNS:
			return "alns";
		default:
			return "mosel";
		}
	}
}
