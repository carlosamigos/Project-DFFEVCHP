package code.solver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.stream.Stream;

import code.problem.ProblemInstance;
import code.solver.heuristics.Individual;
import constants.Constants;
import constants.FileConstants;

public class MoselSolver extends Solver {

	private final String moselFileName;
	private final String bimFileName;
	private final Constants.SolverType solverType = Constants.SolverType.MOSEL;

	private XPRM mosel;
	private XPRMModel model;

	public MoselSolver(String moselFileName) {
		//	this.mosel = new XPRM();
		this.moselFileName = moselFileName;
		this.bimFileName = moselFileName.substring(0, moselFileName.length() - 3) + "bim";
	}

	@Override
	public Individual solve(ProblemInstance problemInstance) {
		compile();
		fixParameters(problemInstance.getFileName());
		fixDataFile(problemInstance.getFilePath());

		System.out.print("Solving " + problemInstance.getFilePath() + " ");
		this.model.run();
		this.model.reset();
		return null;
	}

	private void fixDataFile(String dataFile) {
		String parameters = "DataFile=" + dataFile + ".txt";
		this.model.execParams += parameters;
	}

	private void fixParameters(String fileName) {
		String parameters = "printParams=" + Constants.PRINT_MOSEL_PARAMETERS + "," + "printResults="
				+ Constants.PRINT_MOSEL_RESULTS + "," + "MaxSolveTimeSeconds=" + Constants.MAX_SOLVE_TIME_MOSEL_SECONDS
				+ "," + "OutputPathArtificial=" + FileConstants.MOSEL_OUTPUT_ARTIFICIAL + fileName+ ".txt," + "OutputPathRegular=" + FileConstants.MOSEL_OUTPUT_REAL
				+ fileName + ".txt,";

		model.execParams = parameters;
	}

	private void compile() {
		try {
			this.mosel.compile(FileConstants.PROBLEM_FOLDER + this.moselFileName);
			this.model = this.mosel.loadModel(FileConstants.PROBLEM_FOLDER + this.bimFileName);
		}

		catch (Exception e) {
			System.out.println("Could not compile mosel file");
		}

	}

	public Constants.SolverType getSolverType(){
		return this.solverType;
	}

	@Override
	public String getInfo() {
		return this.moselFileName;
	}

	@Override
	public ArrayList<String> getResults() {
		 ArrayList<String> currResults = new ArrayList<String>();
        try (Stream<String> stream = Files.lines(Paths.get(FileConstants.STATIC_RUN_STATS))) {
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
            gapString = df.format(gap) + "%";
            bestSolution = df.format(Double.parseDouble(currResults.get(0)));
        }
        
        return new ArrayList<String>(){{
			add(bestSolution);
			add(currResults.get(1));
			add(gapString);
			add(df.format(Double.parseDouble(currResults.get(2))));
			add(df.format(Double.parseDouble(currResults.get(3))));
			add(df.format(Double.parseDouble(currResults.get(4))));
		}};
	}

}
