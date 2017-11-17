package tio4500.solvers;

import constants.Constants;
import tio4500.StaticProblem;
//import com.dashoptimization.*;

public class MoselSolver extends Solver {

	private final String moselFileName;
	private final String bimFileName;
	private final Constants.SolverType solverType = Constants.SolverType.MOSEL;

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
		//System.out.println("Solving " + problem.getFilePath());
		//this.model.run();
		//this.model.reset();
	}

	private void fixDataFile(String dataFile) {
		String parameters = "DataFile=" + dataFile + ".txt";
		//this.model.execParams += parameters;
	}

	private void fixParameters() {
		String parameters = "printParams=" + Constants.PRINT_MOSEL_PARAMETERS + "," + "printResults="
				+ Constants.PRINT_MOSEL_RESULTS + "," + "MaxSolveTimeSeconds=" + Constants.MAX_SOLVE_TIME_MOSEL_SECONDS
				+ "," + "OutputPathArtificial=" + Constants.MOSEL_OUTPUT + Constants.OUTPUT_ARTIFICIAL_SERVICE_PATHS
				+ Constants.EXAMPLE_NUMBER + ".txt," + "OutputPathRegular=" + Constants.MOSEL_OUTPUT
				+ Constants.OUTPUT_REAL_SERVICE_PATHS + Constants.EXAMPLE_NUMBER + ".txt,";

		//model.execParams = parameters;
	}

	private void compile() {
		try {
			//this.mosel.compile(Constants.PROBLEM_FOLDER +
			//this.moselFileName);
			//this.model = this.mosel.loadModel(Constants.PROBLEM_FOLDER +
			//this.bimFileName);
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

}