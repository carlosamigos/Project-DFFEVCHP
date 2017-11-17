package tio4500.solvers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.util.stream.Stream;

import constants.Constants;
import tio4500.StaticProblem;
//import com.dashoptimization.*;

public class MoselSolver extends Solver {
	
	private final String moselFileName;
	private final String bimFileName;

	//private XPRM mosel;
	//private XPRMModel model;

	
	public MoselSolver(String moselFileName) {
		//this.mosel = new XPRM();
		this.moselFileName = moselFileName;
		this.bimFileName = moselFileName.substring(0, moselFileName.length() - 3) + "bim";
	}
	
	@Override
	public void solve(StaticProblem problem) {
		compile();
		fixParameters();
		fixDataFile(problem.getFilePath());
		System.out.println("Solving " + problem.getFilePath());
		//this.model.run();
		setResults(problem.getFilePath());
		//this.model.reset();
    }

    private void fixDataFile(String dataFile) {
		String parameters = "DataFile=" + dataFile + ".txt";
		//this.model.execParams += parameters;
	}
	
	private void fixParameters() {
		String parameters =
		  "printParams=" + Constants.PRINT_MOSEL_PARAMETERS + "," +
		  "printResults=" + Constants.PRINT_MOSEL_RESULTS + "," +
		  "MaxSolveTimeSeconds=" + Constants.MAX_SOLVE_TIME_MOSEL_SECONDS + "," +
		  "OutputPathArtificial=" + Constants.MOSEL_OUTPUT + Constants.OUTPUT_ARTIFICIAL_SERVICE_PATHS 
		  + Constants.EXAMPLE_NUMBER + ".txt," +
		  "OutputPathRegular=" + Constants.MOSEL_OUTPUT + Constants.OUTPUT_REAL_SERVICE_PATHS 
		  + Constants.EXAMPLE_NUMBER + ".txt,";
		
		//model.execParams = parameters;
	}
	
	private void compile() {
        try{
            //this.mosel.compile(Constants.PROBLEM_FOLDER + this.moselFileName);
			//this.model = this.mosel.loadModel(Constants.PROBLEM_FOLDER + this.bimFileName);
        }

        catch (Exception e){
            System.out.println("Could not compile mosel file");}

	}
	
	private void setResults(String dataFilePath) {
		/*ArrayList<String> currResults = new ArrayList<String>();
		try (Stream<String> stream = Files.lines(Paths.get(Constants.STATIC_RUN_STATS))) {
			stream.forEach(result -> currResults.add(result));
		} catch (IOException e) {
			System.out.println("Could not find stats file from static run");
		}

		DecimalFormat df = 	new DecimalFormat("#.##");
		String gapString;
		String bestSolution;
		if(currResults.get(0).contains("e")) {
			gapString = "N/A";
			bestSolution = "N/A";
		} else {
			double gap = (Double.parseDouble(currResults.get(0)) - Double.parseDouble(currResults.get(1)))/
					Double.parseDouble(currResults.get(0)) *100;
			System.out.println(gap);
			gapString = df.format(gap);
			System.out.println(gapString);
			bestSolution = currResults.get(0);
		}

		String timeString = df.format(Double.parseDouble(currResults.get(2)));

		this.results = new HashMap<String, String>();
		this.results.put("Name", dataFilePath);
		this.results.put("BestSolution", bestSolution);
		this.results.put("BestBound", currResults.get(1));
		this.results.put("TimeUsed", timeString);
		this.results.put("Gap", gapString);*/
		
	}

	@Override
	public HashMap<String,String> getResults() {
		return this.results;
	}
	
	@Override
	public String getInfo() {
		return this.moselFileName;
	}

}
